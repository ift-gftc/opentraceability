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
                .GroupBy(m => m.ID.ToLower())
                .Select(g => g.Last())
                .ToList();
            if (list.Count == 0) return;

            using var ctx = await _contextFactory.CreateDbContextAsync();

            foreach (var element in list)
            {
                string elementId = element.ID.ToLower();
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

        public async Task<EPCISQueryDocument> QueryEventsAsync(string datasetId, EPCISQueryParameters parameters)
        {
            using var ctx = await _contextFactory.CreateDbContextAsync();

            // Use the indexed search table to narrow candidates for EPC-based queries; precise
            // filtering (time, bizStep formats, product types) is done in-memory by FilterEvents.
            List<string>? candidateIds = await GetCandidateEventIdsAsync(ctx, datasetId, parameters);

            IQueryable<TraceabilityEvent> eventsQuery = ctx.Events.Where(e => e.DatasetId == datasetId);
            if (candidateIds != null)
            {
                if (candidateIds.Count == 0)
                {
                    return BuildQueryDocument(new List<IEvent>(), new Dictionary<string, string>(), new List<string>());
                }
                eventsQuery = eventsQuery.Where(e => candidateIds.Contains(e.EventId));
            }

            var records = await eventsQuery.ToListAsync();

            // Deserialize each stored single-event document and collect events + namespaces.
            var allEvents = new List<IEvent>();
            var namespaces = new Dictionary<string, string>();
            var contexts = new List<string>();
            foreach (var record in records)
            {
                // Reading our own stored output; skip schema validation.
                var doc = OpenTraceabilityMappers.EPCISQueryDocument.JSON.Map(record.EventJson, checkSchema: false);
                allEvents.AddRange(doc.Events);
                foreach (var ns in doc.Namespaces)
                {
                    namespaces[ns.Key] = ns.Value;
                }
                foreach (var c in doc.Contexts)
                {
                    if (!contexts.Contains(c)) contexts.Add(c);
                }
            }

            // Apply the full set of EPCIS query parameters precisely.
            var container = new EPCISDocument { Events = allEvents };
            var filtered = container.FilterEvents(parameters);

            return BuildQueryDocument(filtered, namespaces, contexts);
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
                if (GTIN.TryParse(element.ID, out GTIN gtin, out _))
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

        // ---- helpers ----

        private static async Task<List<string>?> GetCandidateEventIdsAsync(TraceabilityDbContext ctx, string datasetId, EPCISQueryParameters parameters)
        {
            var q = parameters.query;

            var exactMatches = new List<string>();
            var prefixes = new List<string>();

            void AddTerms(List<string>? terms)
            {
                if (terms == null) return;
                foreach (var t in terms)
                {
                    if (string.IsNullOrWhiteSpace(t)) continue;
                    if (t.EndsWith("*")) prefixes.Add(t.Substring(0, t.IndexOf('*')).ToLower());
                    else exactMatches.Add(t.ToLower());
                }
            }

            AddTerms(q.MATCH_anyEPC);
            AddTerms(q.MATCH_epc);
            AddTerms(q.MATCH_anyEPCClass);
            AddTerms(q.MATCH_epcClass);

            // No EPC-based narrowing terms -> let the caller load all events for the dataset.
            if (exactMatches.Count == 0 && prefixes.Count == 0)
            {
                return null;
            }

            var ids = new HashSet<string>();

            if (exactMatches.Count > 0)
            {
                var matchIds = await ctx.EventSearchEntries
                    .Where(s => s.DatasetId == datasetId &&
                                (exactMatches.Contains(s.EPC) || exactMatches.Contains(s.ProductGTIN)))
                    .Select(s => s.EventId)
                    .Distinct()
                    .ToListAsync();
                foreach (var id in matchIds) ids.Add(id);
            }

            foreach (var prefix in prefixes)
            {
                var matchIds = await ctx.EventSearchEntries
                    .Where(s => s.DatasetId == datasetId && s.EPC.StartsWith(prefix))
                    .Select(s => s.EventId)
                    .Distinct()
                    .ToListAsync();
                foreach (var id in matchIds) ids.Add(id);
            }

            return ids.ToList();
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
                EventId = evt.EventID.ToString(),
                EventJson = json,
                BizStep = evt.BusinessStep?.ToString().ToLower() ?? string.Empty,
                Action = evt.Action?.ToString()?.ToLower() ?? string.Empty,
                EventTime = evt.EventTime,
                RecordTime = evt.RecordTime?.UtcDateTime ?? DateTime.UtcNow
            };
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
