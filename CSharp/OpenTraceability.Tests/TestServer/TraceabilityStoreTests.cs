using System;
using System.Collections.Generic;
using System.Linq;
using System.Threading.Tasks;
using Microsoft.Data.Sqlite;
using Microsoft.EntityFrameworkCore;
using OpenTraceability.Interfaces;
using OpenTraceability.Mappers;
using OpenTraceability.Models.Events;
using OpenTraceability.Models.Identifiers;
using OpenTraceability.Queries;
using OpenTraceability.TestServer.Core.Data;
using OpenTraceability.TestServer.Core.Services;
using OpenTraceability.Utility;

namespace OpenTraceability.Tests.TestServer
{
    /// <summary>
    /// Tests for <see cref="TraceabilityStore"/>, in particular that the SQL filtering in
    /// QueryEventsAsync returns exactly the events that the shared in-memory filter
    /// <see cref="EPCISBaseDocument.FilterEvents"/> would select.
    /// </summary>
    [TestFixture]
    [Category("UnitTest")]
    public class TraceabilityStoreTests
    {
        private sealed class Factory : IDbContextFactory<TraceabilityDbContext>
        {
            private readonly DbContextOptions<TraceabilityDbContext> _options;
            public Factory(DbContextOptions<TraceabilityDbContext> options) => _options = options;
            public TraceabilityDbContext CreateDbContext() => new TraceabilityDbContext(_options);
        }

        private TraceabilityStore _store = null!;
        private SqliteConnection _keepAlive = null!;
        private EPCISQueryDocument _allEventsDoc = null!;
        private int _ingestedCount;

        /// <summary>
        /// Builds one in-memory store for the fixture, ingests the GDST sample document, and
        /// captures the full stored event set (via an unfiltered query) as the oracle input.
        /// </summary>
        [OneTimeSetUp]
        public async Task Setup()
        {
            OpenTraceability.Setup.Initialize();
            OpenTraceability.GDST.Setup.Initialize();

            string cs = $"DataSource=store-{Guid.NewGuid():N};Mode=Memory;Cache=Shared";
            _keepAlive = new SqliteConnection(cs);
            _keepAlive.Open();
            var options = new DbContextOptionsBuilder<TraceabilityDbContext>().UseSqlite(cs).Options;
            _store = new TraceabilityStore(new Factory(options));
            await _store.InitializeAsync();

            var ingestion = new IngestionService(_store);
            _ingestedCount = await ingestion.IngestEpcisDocumentAsync("default", LoadSampleDocument(), EPCISDataFormat.JSON, checkSchema: false);
            _allEventsDoc = await _store.QueryEventsAsync("default", new EPCISQueryParameters());
        }

        [OneTimeTearDown]
        public void TearDown()
        {
            _keepAlive.Close();
        }

        private static string LoadSampleDocument()
        {
            var loader = new EmbeddedResourceLoader();
            return loader.ReadString("OpenTraceability.Tests", "OpenTraceability.Tests.Data.gdst_data_withmasterdata.jsonld");
        }

        /// <summary>
        /// Asserts that the store's SQL-filtered query returns exactly the events that
        /// FilterEvents selects from the full stored event set (the oracle).
        /// </summary>
        /// <param name="createParameters">Factory for the query parameters; a fresh instance is built per side because FilterEvents mutates its filter lists.</param>
        /// <param name="expectResults">When true, also asserts the query matches at least one event so the equivalence check is not vacuous.</param>
        private async Task AssertQueryMatchesFilterEventsAsync(Func<EPCISQueryParameters> createParameters, bool expectResults = true)
        {
            // Oracle: apply the shared in-memory filter to every stored event.
            var container = new EPCISDocument { Events = _allEventsDoc.Events.ToList() };
            List<IEvent> expected = container.FilterEvents(createParameters());
            List<string> expectedIds = expected.Select(e => e.EventID!.ToString()).ToList();

            var actualDoc = await _store.QueryEventsAsync("default", createParameters());
            List<string> actualIds = actualDoc.Events.Select(e => e.EventID!.ToString()).ToList();

            Assert.That(actualIds, Is.EquivalentTo(expectedIds), "SQL filtering should return exactly the events FilterEvents selects");
            if (expectResults)
            {
                Assert.That(actualIds, Is.Not.Empty, "the test parameters should match at least one event; otherwise the equivalence check is vacuous");
            }
        }

        /// <summary>
        /// The ingested sample document must round-trip in full through an unfiltered query.
        /// </summary>
        [Test]
        public void IngestAndQuery_NoParameters_RoundTripsAllEvents()
        {
            Assert.That(_ingestedCount, Is.GreaterThan(0), "should have ingested events");
            Assert.That(_allEventsDoc.Events.Count, Is.EqualTo(_ingestedCount), "query should return all ingested events");
        }

        /// <summary>
        /// MATCH_anyEPC with an exact EPC must return the same events as FilterEvents.
        /// </summary>
        [Test]
        public async Task QueryEventsAsync_MatchAnyEpcExact_MatchesFilterEvents()
        {
            string epc = _allEventsDoc.Events.First(e => e.Products.Count > 0).Products.First().EPC.ToString();

            await AssertQueryMatchesFilterEventsAsync(() =>
            {
                var parameters = new EPCISQueryParameters();
                parameters.query.MATCH_anyEPC = new List<string> { epc };
                return parameters;
            });
        }

        /// <summary>
        /// MATCH_epc only considers reference and child products, so a transformation event must
        /// be excluded when queried by one of its input EPCs, while MATCH_anyEPC includes it.
        /// </summary>
        [Test]
        public async Task QueryEventsAsync_MatchEpcOnInputEpc_ExcludesTransformationEvent()
        {
            IEvent transformationEvent = _allEventsDoc.Events.First(e => e.Products.Any(p => p.Type == EventProductType.Input));
            string transformationEventId = transformationEvent.EventID!.ToString();
            string inputEpc = transformationEvent.Products.First(p => p.Type == EventProductType.Input).EPC.ToString();

            EPCISQueryParameters CreateMatchEpc()
            {
                var parameters = new EPCISQueryParameters();
                parameters.query.MATCH_epc = new List<string> { inputEpc };
                return parameters;
            }

            await AssertQueryMatchesFilterEventsAsync(CreateMatchEpc, expectResults: false);

            var matchEpcDoc = await _store.QueryEventsAsync("default", CreateMatchEpc());
            Assert.That(matchEpcDoc.Events.Select(e => e.EventID!.ToString()), Does.Not.Contain(transformationEventId), "MATCH_epc must not match input products");

            var anyEpcParameters = new EPCISQueryParameters();
            anyEpcParameters.query.MATCH_anyEPC = new List<string> { inputEpc };
            var anyEpcDoc = await _store.QueryEventsAsync("default", anyEpcParameters);
            Assert.That(anyEpcDoc.Events.Select(e => e.EventID!.ToString()), Does.Contain(transformationEventId), "MATCH_anyEPC must match products of every type");
        }

        /// <summary>
        /// A class-level EPC with a '*' serial/lot wildcard must match every product sharing the
        /// same GTIN, exactly like EPC.Matches does for FilterEvents.
        /// </summary>
        [Test]
        public async Task QueryEventsAsync_MatchAnyEpcClassWildcard_MatchesByGtin()
        {
            EPC classEpc = _allEventsDoc.Events.SelectMany(e => e.Products).Select(p => p.EPC).First(e => e.Type == EPCType.Class && e.GTIN != null && !string.IsNullOrEmpty(e.SerialLotNumber));
            string epcStr = classEpc.ToString();
            string wildcard = epcStr.Substring(0, epcStr.LastIndexOf('.') + 1) + "*";

            await AssertQueryMatchesFilterEventsAsync(() =>
            {
                var parameters = new EPCISQueryParameters();
                parameters.query.MATCH_anyEPCClass = new List<string> { wildcard };
                return parameters;
            });
        }

        /// <summary>
        /// EQ_bizStep must accept the bare CBV term, the CBV URN, and the GS1 web URI form
        /// interchangeably, matching the URI normalization in FilterEvents.
        /// </summary>
        [Test]
        [TestCase("shipping")]
        [TestCase("urn:epcglobal:cbv:bizstep:shipping")]
        [TestCase("https://ref.gs1.org/cbv/BizStep-shipping")]
        public async Task QueryEventsAsync_EqBizStepAnyForm_MatchesFilterEvents(string bizStepTerm)
        {
            await AssertQueryMatchesFilterEventsAsync(() =>
            {
                var parameters = new EPCISQueryParameters();
                parameters.query.EQ_bizStep = new List<string> { bizStepTerm };
                return parameters;
            });
        }

        /// <summary>
        /// A non-CBV bizStep URN (GDST vocabulary) must match events stored with that URN.
        /// </summary>
        [Test]
        public async Task QueryEventsAsync_EqBizStepGdstUrn_MatchesFilterEvents()
        {
            await AssertQueryMatchesFilterEventsAsync(() =>
            {
                var parameters = new EPCISQueryParameters();
                parameters.query.EQ_bizStep = new List<string> { "urn:gdst:bizStep:fishingEvent" };
                return parameters;
            });
        }

        /// <summary>
        /// EQ_bizLocation must match on the event's business location GLN.
        /// </summary>
        [Test]
        public async Task QueryEventsAsync_EqBizLocation_MatchesFilterEvents()
        {
            IEvent located = _allEventsDoc.Events.First(e => e.Location?.GLN != null);
            Uri bizLocation = new Uri(located.Location!.GLN!.ToString());

            await AssertQueryMatchesFilterEventsAsync(() =>
            {
                var parameters = new EPCISQueryParameters();
                parameters.query.EQ_bizLocation = new List<Uri> { bizLocation };
                return parameters;
            });
        }

        /// <summary>
        /// EQ_transformationID must match transformation events case-insensitively and exclude
        /// every other event type.
        /// </summary>
        [Test]
        public async Task QueryEventsAsync_EqTransformationId_MatchesFilterEvents()
        {
            ITransformationEvent transformationEvent = _allEventsDoc.Events.OfType<ITransformationEvent>().First(t => !string.IsNullOrWhiteSpace(t.TransformationID));
            string transformationId = transformationEvent.TransformationID!;

            foreach (string term in new[] { transformationId, transformationId.ToUpper() })
            {
                await AssertQueryMatchesFilterEventsAsync(() =>
                {
                    var parameters = new EPCISQueryParameters();
                    parameters.query.EQ_transformationID = new List<string> { term };
                    return parameters;
                });
            }
        }

        /// <summary>
        /// A GE/LT event-time window must return the same events as FilterEvents.
        /// </summary>
        [Test]
        public async Task QueryEventsAsync_EventTimeWindow_MatchesFilterEvents()
        {
            List<DateTimeOffset> times = _allEventsDoc.Events.Where(e => e.EventTime != null).Select(e => e.EventTime!.Value).OrderBy(t => t).ToList();
            DateTimeOffset ge = times[times.Count / 3];
            DateTimeOffset lt = times[2 * times.Count / 3];

            await AssertQueryMatchesFilterEventsAsync(() =>
            {
                var parameters = new EPCISQueryParameters();
                parameters.query.GE_eventTime = ge;
                parameters.query.LT_eventTime = lt;
                return parameters;
            });
        }

        /// <summary>
        /// At an event's exact event time, GE and the deprecated LE bounds are inclusive and LT is
        /// exclusive; this proves the SQLite text comparison is chronologically correct at the
        /// boundary. A parameter with a shifted offset must behave identically, proving the UTC
        /// normalization of query parameters.
        /// </summary>
        [Test]
        public async Task QueryEventsAsync_EventTimeAtExactBoundary_GeAndLeIncludeAndLtExcludes()
        {
            IEvent evt = _allEventsDoc.Events.First(e => e.EventTime != null);
            string eventId = evt.EventID!.ToString();
            DateTimeOffset t = evt.EventTime!.Value;

            var geParameters = new EPCISQueryParameters();
            geParameters.query.GE_eventTime = t;
            var geDoc = await _store.QueryEventsAsync("default", geParameters);
            Assert.That(geDoc.Events.Select(e => e.EventID!.ToString()), Does.Contain(eventId), "GE_eventTime is inclusive");

            var geShiftedParameters = new EPCISQueryParameters();
            geShiftedParameters.query.GE_eventTime = t.ToOffset(TimeSpan.FromHours(5));
            var geShiftedDoc = await _store.QueryEventsAsync("default", geShiftedParameters);
            Assert.That(geShiftedDoc.Events.Select(e => e.EventID!.ToString()), Does.Contain(eventId), "a shifted offset representing the same instant must behave identically");

            var ltParameters = new EPCISQueryParameters();
            ltParameters.query.LT_eventTime = t;
            var ltDoc = await _store.QueryEventsAsync("default", ltParameters);
            Assert.That(ltDoc.Events.Select(e => e.EventID!.ToString()), Does.Not.Contain(eventId), "LT_eventTime is exclusive");

#pragma warning disable CS0618 // The deprecated LE bound must stay inclusive for callers that have not moved to LT.
            var leParameters = new EPCISQueryParameters();
            leParameters.query.LE_eventTime = t;
#pragma warning restore CS0618
            var leDoc = await _store.QueryEventsAsync("default", leParameters);
            Assert.That(leDoc.Events.Select(e => e.EventID!.ToString()), Does.Contain(eventId), "LE_eventTime is inclusive");
        }

        /// <summary>
        /// Combining EPC, time, and bizStep filters must AND them together, matching FilterEvents.
        /// </summary>
        [Test]
        public async Task QueryEventsAsync_CombinedFilters_MatchesFilterEvents()
        {
            IEvent evt = _allEventsDoc.Events.First(e => e.EventTime != null && e.Products.Count > 0 && e.BusinessStep != null);
            string epc = evt.Products.First().EPC.ToString();
            DateTimeOffset t = evt.EventTime!.Value;
            string bizStep = evt.BusinessStep!.ToString();

            await AssertQueryMatchesFilterEventsAsync(() =>
            {
                var parameters = new EPCISQueryParameters();
                parameters.query.MATCH_anyEPC = new List<string> { epc };
                parameters.query.GE_eventTime = t;
                parameters.query.LT_eventTime = t.AddSeconds(1);
                parameters.query.EQ_bizStep = new List<string> { bizStep };
                return parameters;
            });
        }

        /// <summary>
        /// A query for an EPC that exists nowhere must return an empty document, not throw.
        /// </summary>
        [Test]
        public async Task QueryEventsAsync_NoMatchingEpc_ReturnsEmptyDocument()
        {
            var parameters = new EPCISQueryParameters();
            parameters.query.MATCH_anyEPC = new List<string> { "urn:epc:id:sgtin:0000000.000000.NONE" };

            var doc = await _store.QueryEventsAsync("default", parameters);

            Assert.That(doc.Events, Is.Empty);
        }
    }
}
