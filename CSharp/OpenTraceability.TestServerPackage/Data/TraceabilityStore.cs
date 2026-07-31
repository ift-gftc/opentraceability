using System;
using System.Collections.Generic;
using System.Linq;
using System.Threading.Tasks;
using Microsoft.EntityFrameworkCore;
using OpenTraceability.Interfaces;
using OpenTraceability.Mappers;
using OpenTraceability.Models.Common;
using OpenTraceability.Models.Events;
using OpenTraceability.Models.Identifiers;
using OpenTraceability.Queries;
using OpenTraceability.TestServer.Core.Data.Entities;
using OpenTraceability.TestServer.Core.Models;
using OpenTraceability.Utility;

namespace OpenTraceability.TestServer.Core.Data
{
    /// <summary>
    /// EF Core (SQLite) implementation of <see cref="ITraceabilityStore"/>.
    /// </summary>
    public class TraceabilityStore : ITraceabilityStore
    {
        private readonly IDbContextFactory<TraceabilityDbContext> _contextFactory;

        public TraceabilityStore(IDbContextFactory<TraceabilityDbContext> contextFactory)
        {
            _contextFactory = contextFactory;
        }

        public async Task InitializeAsync()
        {
            using var ctx = await _contextFactory.CreateDbContextAsync();
            await ctx.Database.EnsureCreatedAsync();

            // EnsureCreated no-ops on an existing database, so tables added after a deployment's
            // epcis.db was first created must be applied explicitly. The DDL mirrors what
            // EnsureCreated generates for DatasetRecord on a fresh database.
            await ctx.Database.ExecuteSqlRawAsync(
                "CREATE TABLE IF NOT EXISTS \"Datasets\" (" +
                "\"Id\" INTEGER NOT NULL CONSTRAINT \"PK_Datasets\" PRIMARY KEY AUTOINCREMENT, " +
                "\"DatasetId\" TEXT NOT NULL, " +
                "\"Modules\" TEXT NOT NULL, " +
                "\"Description\" TEXT NULL, " +
                "\"CreatedUtc\" TEXT NOT NULL, " +
                "\"UpdatedUtc\" TEXT NOT NULL)");
            await ctx.Database.ExecuteSqlRawAsync(
                "CREATE UNIQUE INDEX IF NOT EXISTS \"IX_Datasets_DatasetId\" ON \"Datasets\" (\"DatasetId\")");

            // Columns added after a deployment's epcis.db was first created must also be applied
            // explicitly. When any are added, the derived query columns and search rows are rebuilt
            // from the stored event JSON so the upgraded database keeps returning correct results.
            bool upgraded = false;
            upgraded |= await AddColumnIfMissingAsync(ctx, "Events", "BizLocationGLN", "TEXT NOT NULL DEFAULT ''");
            upgraded |= await AddColumnIfMissingAsync(ctx, "Events", "TransformationId", "TEXT NOT NULL DEFAULT ''");
            upgraded |= await AddColumnIfMissingAsync(ctx, "EventSearchEntries", "EpcType", "TEXT NOT NULL DEFAULT ''");

            if (upgraded)
            {
                await ctx.Database.ExecuteSqlRawAsync("CREATE INDEX IF NOT EXISTS \"IX_Events_BizLocationGLN\" ON \"Events\" (\"BizLocationGLN\")");
                await ctx.Database.ExecuteSqlRawAsync("CREATE INDEX IF NOT EXISTS \"IX_Events_TransformationId\" ON \"Events\" (\"TransformationId\")");
                await ctx.Database.ExecuteSqlRawAsync("CREATE INDEX IF NOT EXISTS \"IX_Events_EventTime\" ON \"Events\" (\"EventTime\")");
                await RebuildDerivedEventDataAsync(ctx);
            }
        }

        /// <summary>
        /// Adds the column to the table when it does not exist yet. Returns true when the column was added.
        /// </summary>
        private static async Task<bool> AddColumnIfMissingAsync(TraceabilityDbContext ctx, string tableName, string columnName, string columnDefinition)
        {
            // Identifiers cannot be parameterized in DDL or pragma calls; all arguments are
            // compile-time constants from this class, never user input.
#pragma warning disable EF1002
            int exists = await ctx.Database.SqlQueryRaw<int>($"SELECT COUNT(*) AS \"Value\" FROM pragma_table_info('{tableName}') WHERE name = '{columnName}'").SingleAsync();
            if (exists > 0)
            {
                return false;
            }

            await ctx.Database.ExecuteSqlRawAsync($"ALTER TABLE \"{tableName}\" ADD COLUMN \"{columnName}\" {columnDefinition}");
#pragma warning restore EF1002
            return true;
        }

        /// <summary>
        /// Rebuilds the derived query columns and search rows for every stored event from its JSON
        /// (the source of truth). Runs when a schema upgrade adds columns to an existing database,
        /// so older deployments keep returning correct query results.
        /// </summary>
        private static async Task RebuildDerivedEventDataAsync(TraceabilityDbContext ctx)
        {
            var records = await ctx.Events.ToListAsync();
            var eventsByDataset = new Dictionary<string, List<IEvent>>();

            foreach (var record in records)
            {
                // Reading our own stored output; skip schema validation.
                var doc = OpenTraceabilityMappers.EPCISQueryDocument.JSON.Map(record.EventJson, checkSchema: false);
                IEvent evt = doc.Events.Single();

                record.BizLocationGLN = GetBizLocationGLN(evt);
                record.TransformationId = GetTransformationId(evt);

                // Older databases stored EventTime as DateTimeOffset TEXT (with an offset suffix),
                // which no longer compares correctly against the UTC DateTime format written today.
                // Re-stamp and force the update so every row is rewritten in the current format.
                record.EventTime = evt.EventTime?.UtcDateTime;
                ctx.Entry(record).Property(r => r.EventTime).IsModified = true;

                if (!eventsByDataset.TryGetValue(record.DatasetId, out List<IEvent>? datasetEvents))
                {
                    datasetEvents = new List<IEvent>();
                    eventsByDataset[record.DatasetId] = datasetEvents;
                }
                datasetEvents.Add(evt);
            }

            await ctx.EventSearchEntries.ExecuteDeleteAsync();
            foreach (var kvp in eventsByDataset)
            {
                ctx.EventSearchEntries.AddRange(EventSearchEntry.CreateSearchEntries(kvp.Key, kvp.Value));
            }
            await ctx.SaveChangesAsync();
        }

        public async Task UpsertEventsAsync(string datasetId, IEnumerable<IEvent> events, IDictionary<string, string>? namespaces = null)
        {
            var list = events.ToList();
            if (list.Count == 0) return;

            var ns = MergeNamespaces(namespaces);

            // EPCIS eventID is optional. Assign a deterministic id (derived from the event's content)
            // to any event missing one so storage keys are stable and re-ingesting the same event is
            // idempotent rather than creating duplicate rows.
            EnsureEventIds(list, ns);

            // Deduplicate by EventID within the batch, keeping the last occurrence (upsert: last wins).
            list = list.GroupBy(e => e.EventID!.ToString()).Select(g => g.Last()).ToList();

            using var ctx = await _contextFactory.CreateDbContextAsync();

            var eventIds = list.Select(e => e.EventID!.ToString()).ToList();

            var existing = await ctx.Events
                .Where(e => e.DatasetId == datasetId && eventIds.Contains(e.EventId))
                .ToDictionaryAsync(e => e.EventId, e => e);

            foreach (var evt in list)
            {
                var record = ToEventRecord(datasetId, evt, ns);
                if (existing.TryGetValue(record.EventId, out var existingRecord))
                {
                    record.Id = existingRecord.Id;
                    ctx.Entry(existingRecord).CurrentValues.SetValues(record);
                }
                else
                {
                    ctx.Events.Add(record);
                }
            }

            // Replace search index rows for these events.
            var existingSearch = await ctx.EventSearchEntries
                .Where(s => s.DatasetId == datasetId && eventIds.Contains(s.EventId))
                .ToListAsync();
            ctx.EventSearchEntries.RemoveRange(existingSearch);
            ctx.EventSearchEntries.AddRange(EventSearchEntry.CreateSearchEntries(datasetId, list));

            await ctx.SaveChangesAsync();
        }

        public async Task UpsertMasterDataAsync(string datasetId, IEnumerable<IVocabularyElement> masterData)
        {
            // Deduplicate by identifier within the batch (last wins). The identifier is the storage key,
            // so a document that declares the same id more than once is an upsert, not a constraint error.
            var list = masterData
                .Where(m => !string.IsNullOrEmpty(m.ID))
                .GroupBy(m => m.ID!.ToLower())
                .Select(g => g.Last())
                .ToList();
            if (list.Count == 0) return;

            using var ctx = await _contextFactory.CreateDbContextAsync();

            foreach (var element in list)
            {
                string elementId = element.ID!.ToLower();
                var record = new MasterDataRecord
                {
                    DatasetId = datasetId,
                    ElementId = elementId,
                    VocabularyType = element.VocabularyType.ToString().ToLower(),
                    ElementType = element.GetType().AssemblyQualifiedName ?? element.GetType().FullName ?? string.Empty,
                    ElementJson = OpenTraceabilityMappers.MasterData.GS1WebVocab.Map(element)
                };

                var existing = await ctx.MasterDataRecords
                    .FirstOrDefaultAsync(m => m.DatasetId == datasetId && m.ElementId == elementId);

                if (existing == null)
                {
                    ctx.MasterDataRecords.Add(record);
                }
                else
                {
                    record.Id = existing.Id;
                    ctx.Entry(existing).CurrentValues.SetValues(record);
                }
            }

            await ctx.SaveChangesAsync();
        }

        /// <summary>
        /// Queries events for a dataset, applying the EPCIS query parameters in SQL so only
        /// matching rows are loaded and deserialized.
        /// </summary>
        /// <remarks>
        /// Every filter mirrors the semantics of <see cref="EPCISBaseDocument.FilterEvents"/>:
        /// time bounds compare against the UTC EventTime/RecordTime columns, EQ_bizStep terms are
        /// expanded to their accepted CBV URN and GS1 web URI forms, EQ_bizLocation and
        /// EQ_transformationID compare against dedicated columns, and MATCH_* parameters run as
        /// EXISTS subqueries over the per-product search rows. The eventTypes and EQ_action
        /// parameters are not supported (FilterEvents ignores them too).
        /// </remarks>
        public async Task<EPCISQueryDocument> QueryEventsAsync(string datasetId, EPCISQueryParameters parameters)
        {
            using var ctx = await _contextFactory.CreateDbContextAsync();
            var q = parameters.query;

            IQueryable<TraceabilityEvent> eventsQuery = ctx.Events.Where(e => e.DatasetId == datasetId);
            eventsQuery = ApplyTimeFilters(eventsQuery, q);
            eventsQuery = ApplyBizStepFilter(eventsQuery, q.EQ_bizStep);
            eventsQuery = ApplyBizLocationFilter(eventsQuery, q.EQ_bizLocation);
            eventsQuery = ApplyTransformationIdFilter(eventsQuery, q.EQ_transformationID);

            // Each MATCH parameter is a separate ANDed condition, exactly like FilterEvents applies them.
            eventsQuery = ApplyEpcMatchFilter(ctx, eventsQuery, datasetId, q.MATCH_anyEPC, restrictToReferenceAndChild: false);
            eventsQuery = ApplyEpcMatchFilter(ctx, eventsQuery, datasetId, q.MATCH_anyEPCClass, restrictToReferenceAndChild: false);
            eventsQuery = ApplyEpcMatchFilter(ctx, eventsQuery, datasetId, q.MATCH_epc, restrictToReferenceAndChild: true);
            eventsQuery = ApplyEpcMatchFilter(ctx, eventsQuery, datasetId, q.MATCH_epcClass, restrictToReferenceAndChild: true);

            var records = await eventsQuery.ToListAsync();

            // Deserialize each stored single-event document and collect events + namespaces.
            var events = new List<IEvent>();
            var namespaces = new Dictionary<string, string>();
            var contexts = new List<string>();
            foreach (var record in records)
            {
                // Reading our own stored output; skip schema validation.
                var doc = OpenTraceabilityMappers.EPCISQueryDocument.JSON.Map(record.EventJson, checkSchema: false);
                events.AddRange(doc.Events);
                foreach (var ns in doc.Namespaces)
                {
                    namespaces[ns.Key] = ns.Value;
                }
                foreach (var c in doc.Contexts)
                {
                    if (!contexts.Contains(c)) contexts.Add(c);
                }
            }

            return BuildQueryDocument(events, namespaces, contexts);
        }

        public async Task<IVocabularyElement?> GetMasterDataAsync(string datasetId, string identifier)
        {
            using var ctx = await _contextFactory.CreateDbContextAsync();
            string idLower = identifier.ToLower();

            // exact match first
            var record = await ctx.MasterDataRecords
                .FirstOrDefaultAsync(m => m.DatasetId == datasetId && m.ElementId == idLower);

            if (record != null)
            {
                return Deserialize(record);
            }

            // fallback: try GTIN-14 matching for trade items (digital link / numeric GTIN form)
            var tradeitems = await ctx.MasterDataRecords
                .Where(m => m.DatasetId == datasetId && m.VocabularyType == "tradeitem")
                .ToListAsync();

            foreach (var ti in tradeitems)
            {
                var element = Deserialize(ti);
                if (element?.ID == null) continue;
                if (GTIN.TryParse(element.ID, out GTIN? gtin, out _))
                {
                    string? gtin14 = gtin.ToGTIN14();
                    if (!string.IsNullOrEmpty(gtin14) && gtin14 == identifier)
                    {
                        return element;
                    }
                }
            }

            return null;
        }

        public async Task<List<IVocabularyElement>> GetMasterDataByTypeAsync(string datasetId, VocabularyType type)
        {
            using var ctx = await _contextFactory.CreateDbContextAsync();
            string typeStr = type.ToString().ToLower();
            var records = await ctx.MasterDataRecords
                .Where(m => m.DatasetId == datasetId && m.VocabularyType == typeStr)
                .ToListAsync();
            return records.Select(Deserialize).Where(m => m != null).Select(m => m!).ToList();
        }

        public async Task<bool> IdentifierExistsAsync(string datasetId, string identifier)
        {
            using var ctx = await _contextFactory.CreateDbContextAsync();
            string idLower = identifier.ToLower();

            if (await ctx.MasterDataRecords.AnyAsync(m => m.DatasetId == datasetId && m.ElementId == idLower))
                return true;

            return await ctx.EventSearchEntries.AnyAsync(s => s.DatasetId == datasetId &&
                (s.EPC == idLower || s.ProductGTIN == idLower || s.LocationGLN == idLower || s.PartyPGLN == idLower));
        }

        public async Task<Dataset?> GetDatasetAsync(string datasetId)
        {
            using var ctx = await _contextFactory.CreateDbContextAsync();
            var record = await ctx.Datasets.FirstOrDefaultAsync(d => d.DatasetId == datasetId);
            return record == null ? null : ToDataset(record);
        }

        public async Task<List<Dataset>> ListDatasetsAsync()
        {
            using var ctx = await _contextFactory.CreateDbContextAsync();
            var records = await ctx.Datasets.OrderBy(d => d.DatasetId).ToListAsync();
            return records.Select(ToDataset).ToList();
        }

        public async Task<Dataset> UpsertDatasetAsync(Dataset dataset)
        {
            using var ctx = await _contextFactory.CreateDbContextAsync();
            var now = DateTime.UtcNow;
            var existing = await ctx.Datasets.FirstOrDefaultAsync(d => d.DatasetId == dataset.DatasetId);
            if (existing == null)
            {
                existing = new DatasetRecord
                {
                    DatasetId = dataset.DatasetId,
                    CreatedUtc = now
                };
                ctx.Datasets.Add(existing);
            }
            existing.Modules = string.Join(",", dataset.Modules);
            existing.Description = dataset.Description;
            existing.UpdatedUtc = now;
            await ctx.SaveChangesAsync();
            return ToDataset(existing);
        }

        public async Task<bool> DeleteDatasetAsync(string datasetId, bool purgeData)
        {
            using var ctx = await _contextFactory.CreateDbContextAsync();
            var record = await ctx.Datasets.FirstOrDefaultAsync(d => d.DatasetId == datasetId);
            if (record == null) return false;

            using var tx = await ctx.Database.BeginTransactionAsync();
            ctx.Datasets.Remove(record);
            await ctx.SaveChangesAsync();
            if (purgeData)
            {
                await PurgeDatasetDataAsync(ctx, datasetId);
            }
            await tx.CommitAsync();
            return true;
        }

        public async Task ClearDatasetDataAsync(string datasetId)
        {
            using var ctx = await _contextFactory.CreateDbContextAsync();
            using var tx = await ctx.Database.BeginTransactionAsync();
            await PurgeDatasetDataAsync(ctx, datasetId);
            await tx.CommitAsync();
        }

        private static async Task PurgeDatasetDataAsync(TraceabilityDbContext ctx, string datasetId)
        {
            await ctx.Events.Where(e => e.DatasetId == datasetId).ExecuteDeleteAsync();
            await ctx.EventSearchEntries.Where(s => s.DatasetId == datasetId).ExecuteDeleteAsync();
            await ctx.MasterDataRecords.Where(m => m.DatasetId == datasetId).ExecuteDeleteAsync();
        }

        private static Dataset ToDataset(DatasetRecord record)
        {
            return new Dataset
            {
                DatasetId = record.DatasetId,
                Modules = record.Modules
                    .Split(',', StringSplitOptions.RemoveEmptyEntries | StringSplitOptions.TrimEntries)
                    .ToList(),
                Description = record.Description,
                CreatedUtc = DateTime.SpecifyKind(record.CreatedUtc, DateTimeKind.Utc),
                UpdatedUtc = DateTime.SpecifyKind(record.UpdatedUtc, DateTimeKind.Utc)
            };
        }

        // ---- helpers ----

        /// <summary>
        /// Applies the GE/LE/LT event-time and record-time bounds, mirroring the corresponding
        /// rules in <see cref="EPCISBaseDocument.FilterEvents"/> (GE and the deprecated LE bounds
        /// are inclusive, LT is exclusive, and events without an event time never match).
        /// One deliberate difference: events ingested without a recordTime carry their ingestion
        /// time in the RecordTime column, so record-time bounds match them by that timestamp
        /// (FilterEvents would exclude them outright).
        /// </summary>
        /// <remarks>
        /// SQLite stores DateTime as TEXT and compares it as a string, which is only
        /// chronologically correct when both sides share the same time zone and format. Stored
        /// EventTime/RecordTime values are written in UTC, so every parameter is normalized to
        /// UTC before entering the predicate.
        /// </remarks>
        private static IQueryable<TraceabilityEvent> ApplyTimeFilters(IQueryable<TraceabilityEvent> eventsQuery, EPCISQuery q)
        {
            if (q.GE_eventTime != null)
            {
                DateTime geEventTime = q.GE_eventTime.Value.UtcDateTime;
                eventsQuery = eventsQuery.Where(e => e.EventTime >= geEventTime);
            }

            // The deprecated LE_ bounds are still honored (inclusive), matching FilterEvents.
#pragma warning disable CS0618
            if (q.LE_eventTime != null)
            {
                DateTime leEventTime = q.LE_eventTime.Value.UtcDateTime;
                eventsQuery = eventsQuery.Where(e => e.EventTime <= leEventTime);
            }

            if (q.LE_recordTime != null)
            {
                DateTime leRecordTime = q.LE_recordTime.Value.UtcDateTime;
                eventsQuery = eventsQuery.Where(e => e.RecordTime <= leRecordTime);
            }
#pragma warning restore CS0618

            if (q.LT_eventTime != null)
            {
                DateTime ltEventTime = q.LT_eventTime.Value.UtcDateTime;
                eventsQuery = eventsQuery.Where(e => e.EventTime < ltEventTime);
            }

            if (q.GE_recordTime != null)
            {
                DateTime geRecordTime = q.GE_recordTime.Value.UtcDateTime;
                eventsQuery = eventsQuery.Where(e => e.RecordTime >= geRecordTime);
            }

            if (q.LT_recordTime != null)
            {
                DateTime ltRecordTime = q.LT_recordTime.Value.UtcDateTime;
                eventsQuery = eventsQuery.Where(e => e.RecordTime < ltRecordTime);
            }

            return eventsQuery;
        }

        /// <summary>
        /// Applies the EQ_bizStep filter, mirroring the URI normalization of
        /// <see cref="EPCISBaseDocument.FilterEvents"/>: each term is accepted in both its CBV URN
        /// form and its GS1 web URI form, compared against the lowercased stored business step.
        /// </summary>
        private static IQueryable<TraceabilityEvent> ApplyBizStepFilter(IQueryable<TraceabilityEvent> eventsQuery, List<string>? bizSteps)
        {
            if (bizSteps == null || bizSteps.Count == 0)
            {
                return eventsQuery;
            }

            List<string> acceptedBizSteps = ExpandBizStepTerms(bizSteps);
            return eventsQuery.Where(e => acceptedBizSteps.Contains(e.BizStep));
        }

        /// <summary>
        /// Expands each EQ_bizStep term into the set of lowercased strings it may be stored as: a
        /// bare term becomes a CBV URN, a GS1 web URI is converted to its CBV URN, and every CBV
        /// URN also accepts the equivalent GS1 web URI form (stored values keep their source form).
        /// </summary>
        private static List<string> ExpandBizStepTerms(List<string> bizSteps)
        {
            const string webUriPrefix = "https://ref.gs1.org/cbv/bizstep-";
            const string urnPrefix = "urn:epcglobal:cbv:bizstep:";

            var accepted = new List<string>();
            foreach (string term in bizSteps.Where(t => !string.IsNullOrWhiteSpace(t)))
            {
                string lowered = term.ToLower();

                string urn;
                if (!Uri.TryCreate(term, UriKind.Absolute, out Uri? _))
                {
                    urn = urnPrefix + lowered;
                }
                else if (lowered.StartsWith(webUriPrefix))
                {
                    urn = urnPrefix + lowered.Split('-').Last();
                }
                else
                {
                    urn = lowered;
                }

                accepted.Add(urn);
                if (urn.StartsWith(urnPrefix))
                {
                    accepted.Add(webUriPrefix + urn.Substring(urnPrefix.Length));
                }
            }

            return accepted.Distinct().ToList();
        }

        /// <summary>
        /// Applies the EQ_bizLocation filter against the event's business location GLN, mirroring
        /// <see cref="EPCISBaseDocument.FilterEvents"/> (source/destination GLNs never match).
        /// </summary>
        private static IQueryable<TraceabilityEvent> ApplyBizLocationFilter(IQueryable<TraceabilityEvent> eventsQuery, List<Uri>? bizLocations)
        {
            if (bizLocations == null || bizLocations.Count == 0)
            {
                return eventsQuery;
            }

            List<string> glns = bizLocations.Select(u => u.ToString().ToLower()).ToList();
            return eventsQuery.Where(e => glns.Contains(e.BizLocationGLN));
        }

        /// <summary>
        /// Applies the EQ_transformationID filter, mirroring <see cref="EPCISBaseDocument.FilterEvents"/>:
        /// only transformation events carry a transformation ID, so all other events are excluded
        /// as soon as this parameter is present.
        /// </summary>
        private static IQueryable<TraceabilityEvent> ApplyTransformationIdFilter(IQueryable<TraceabilityEvent> eventsQuery, List<string>? transformationIds)
        {
            if (transformationIds == null || transformationIds.Count == 0)
            {
                return eventsQuery;
            }

            // Whitespace-only terms are dropped so they can never match the empty column value
            // that non-transformation events carry.
            List<string> ids = transformationIds.Where(t => !string.IsNullOrWhiteSpace(t)).Select(t => t.ToLower()).ToList();
            return eventsQuery.Where(e => ids.Contains(e.TransformationId));
        }

        /// <summary>
        /// Applies one MATCH_* parameter as an EXISTS subquery over the per-product search rows,
        /// mirroring <see cref="EPC.Matches"/>: every term matches its exact lowercased string
        /// form, and a term whose serial/lot component is the '*' wildcard also matches any
        /// product sharing the same GTIN (or, when the term has no GTIN, products without one).
        /// </summary>
        private static IQueryable<TraceabilityEvent> ApplyEpcMatchFilter(TraceabilityDbContext ctx, IQueryable<TraceabilityEvent> eventsQuery, string datasetId, List<string>? matchTerms, bool restrictToReferenceAndChild)
        {
            if (matchTerms == null || matchTerms.Count == 0)
            {
                return eventsQuery;
            }

            var exactEpcs = new List<string>();
            var wildcardGtins = new List<string>();
            bool matchProductsWithoutGtin = false;

            foreach (string term in matchTerms)
            {
                EPC epc = new EPC(term);
                exactEpcs.Add(epc.ToString());
                if (epc.SerialLotNumber == "*")
                {
                    if (epc.GTIN != null)
                    {
                        wildcardGtins.Add(epc.GTIN.ToString().ToLower());
                    }
                    else
                    {
                        matchProductsWithoutGtin = true;
                    }
                }
            }

            // MATCH_epc / MATCH_epcClass only consider reference and child products; the MATCH_any
            // variants consider products of every type, exactly like FilterEvents.
            if (restrictToReferenceAndChild)
            {
                return eventsQuery.Where(e => ctx.EventSearchEntries.Any(s => s.DatasetId == datasetId && s.EventId == e.EventId && s.EPC != string.Empty
                    && (s.EpcType == "reference" || s.EpcType == "child")
                    && (exactEpcs.Contains(s.EPC) || wildcardGtins.Contains(s.ProductGTIN) || (matchProductsWithoutGtin && s.ProductGTIN == string.Empty))));
            }

            return eventsQuery.Where(e => ctx.EventSearchEntries.Any(s => s.DatasetId == datasetId && s.EventId == e.EventId && s.EPC != string.Empty
                && (exactEpcs.Contains(s.EPC) || wildcardGtins.Contains(s.ProductGTIN) || (matchProductsWithoutGtin && s.ProductGTIN == string.Empty))));
        }

        // Standard GDST / CBV namespaces needed to serialize prefixed KDEs when the caller does not
        // supply the source document's namespaces.
        private static readonly Dictionary<string, string> _defaultNamespaces = new()
        {
            ["cbvmda"] = "urn:epcglobal:cbv:mda",
            ["gdst"] = "https://traceability-dialogue.org/epcis",
            ["sbdh"] = "http://www.unece.org/cefact/namespaces/StandardBusinessDocumentHeader",
            ["epcisq"] = "urn:epcglobal:epcis-query:xsd:1"
        };

        private static Dictionary<string, string> MergeNamespaces(IDictionary<string, string>? namespaces)
        {
            var merged = new Dictionary<string, string>(_defaultNamespaces);
            if (namespaces != null)
            {
                foreach (var kvp in namespaces)
                {
                    merged[kvp.Key] = kvp.Value;
                }
            }
            return merged;
        }

        // EPCIS eventID is optional. Give every event lacking one a deterministic synthetic id derived
        // from its serialized content, so the same event always maps to the same storage key.
        private static void EnsureEventIds(List<IEvent> events, Dictionary<string, string> namespaces)
        {
            foreach (var evt in events)
            {
                if (evt.EventID == null)
                {
                    evt.EventID = new Uri(EventHashGenerator.GenerateHash(evt));
                }
            }
        }

        private static string ComputeEventFingerprint(IEvent evt, Dictionary<string, string> namespaces)
        {
            var doc = new EPCISQueryDocument
            {
                EPCISVersion = EPCISVersion.V2,
                Header = StandardBusinessDocumentHeader.DummyHeader,
                Namespaces = new Dictionary<string, string>(namespaces)
            };
            doc.Events.Add(evt);
            string json = OpenTraceabilityMappers.EPCISQueryDocument.JSON.Map(doc, checkSchema: false);

            byte[] hash = System.Security.Cryptography.SHA256.HashData(System.Text.Encoding.UTF8.GetBytes(json));
            return Convert.ToHexString(hash).ToLowerInvariant();
        }

        private static TraceabilityEvent ToEventRecord(string datasetId, IEvent evt, Dictionary<string, string> namespaces)
        {
            var doc = new EPCISQueryDocument
            {
                EPCISVersion = EPCISVersion.V2,
                Header = StandardBusinessDocumentHeader.DummyHeader,
                // Clone: the serializer mutates doc.Namespaces, so each event needs its own copy.
                Namespaces = new Dictionary<string, string>(namespaces)
            };
            doc.Events.Add(evt);
            // Serializing internal storage; skip schema validation (we control the data).
            string json = OpenTraceabilityMappers.EPCISQueryDocument.JSON.Map(doc, checkSchema: false);

            return new TraceabilityEvent
            {
                DatasetId = datasetId,
                EventId = evt.EventID?.ToString() ?? throw new Exception("The event has no EventID; events must have an EventID stamped before they are stored."),
                EventJson = json,
                BizStep = evt.BusinessStep?.ToString().ToLower() ?? string.Empty,
                Action = evt.Action?.ToString()?.ToLower() ?? string.Empty,
                BizLocationGLN = GetBizLocationGLN(evt),
                TransformationId = GetTransformationId(evt),
                EventTime = evt.EventTime?.UtcDateTime,
                RecordTime = evt.RecordTime?.UtcDateTime ?? DateTime.UtcNow
            };
        }

        /// <summary>
        /// Gets the lowercased GLN of the event's business location, or empty when the event has none.
        /// </summary>
        private static string GetBizLocationGLN(IEvent evt)
        {
            return evt.Location?.GLN?.ToString().ToLower() ?? string.Empty;
        }

        /// <summary>
        /// Gets the lowercased transformation ID when the event is a transformation event, otherwise empty.
        /// </summary>
        private static string GetTransformationId(IEvent evt)
        {
            if (evt is ITransformationEvent transformationEvent && !string.IsNullOrWhiteSpace(transformationEvent.TransformationID))
            {
                return transformationEvent.TransformationID.ToLower();
            }
            return string.Empty;
        }

        private static EPCISQueryDocument BuildQueryDocument(List<IEvent> events, Dictionary<string, string> namespaces, List<string> contexts)
        {
            return new EPCISQueryDocument
            {
                EPCISVersion = EPCISVersion.V2,
                CreationDate = DateTimeOffset.UtcNow,
                Header = StandardBusinessDocumentHeader.DummyHeader,
                Events = events,
                Namespaces = namespaces,
                Contexts = contexts
            };
        }

        private static IVocabularyElement? Deserialize(MasterDataRecord record)
        {
            Type? t = Type.GetType(record.ElementType);
            if (t == null) return null;
            return OpenTraceabilityMappers.MasterData.GS1WebVocab.Map(t, record.ElementJson);
        }
    }
}
