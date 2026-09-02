using NUnit.Framework;
using OpenTraceability.Interfaces;
using OpenTraceability.Mappers;
using OpenTraceability.Models.Events;
using OpenTraceability.Utility;
using System.Collections.Generic;

namespace OpenTraceability.Tests.Events
{
    /// <summary>
    /// Validates the EPCIS Event Hash ID generator against the GS1 CBV 2.0 worked example
    /// (Example_9.6.1-ObjectEvent.jsonld).
    ///
    /// NOTE: the eventIDs embedded in that file were produced by an early draft of the algorithm
    /// and are now stale. The expected values below were produced by the GS1 reference
    /// implementation (https://github.com/RalphTro/epcis-event-hash-generator) running the current
    /// CBV 2.0 algorithm, and are confirmed to be representation-independent (the same values are
    /// produced from the XML and JSON-LD bindings of these events).
    /// </summary>
    [TestFixture]
    [Category("UnitTest")]
    public class EventHashGeneratorTests
    {
        // Reference-implementation hashes for the two events in Example_9.6.1-ObjectEvent.jsonld.
        private static readonly string[] ExpectedHashes =
        {
            "ni:///sha-256;df6523665bc5e5803d6c7b84f5a04e103694d8220f2abc4f2c74310e89f31bc6?ver=CBV2.0",
            "ni:///sha-256;32547b2344d525ea60ab1b899d82a9011fd9c39bb26ed936e113066146f49941?ver=CBV2.0"
        };

        [Test]
        public void Hash_Matches_GS1_Reference_Vectors()
        {
            string json = OpenTraceabilityTests.ReadTestData("Example_9.6.1-ObjectEvent.jsonld");
            EPCISDocument doc = OpenTraceabilityMappers.EPCISDocument.JSON.Map(json);

            Assert.That(doc.Events.Count, Is.EqualTo(ExpectedHashes.Length),
                "Unexpected number of events in the test document.");

            for (int i = 0; i < doc.Events.Count; i++)
            {
                IEvent e = doc.Events[i];
                string actual = EventHashGenerator.GenerateHash(e);
                Assert.That(actual, Is.EqualTo(ExpectedHashes[i]),
                    $"Hash mismatch for event {i}.\nExpected: {ExpectedHashes[i]}\nActual:   {actual}\nPre-hash: {EventHashGenerator.GeneratePreHashString(e)}");
            }
        }

        /// <summary>
        /// Every event type must produce a syntactically valid, deterministic "ni" URI hash.
        /// </summary>
        [TestCase("object_event_all_possible_fields.xml")]
        [TestCase("aggregation_event_all_possible_fields.xml")]
        [TestCase("transformation_event_all_possible_fields.xml")]
        [TestCase("association_event_all_possible_fields.xml")]
        public void Hash_Is_Valid_And_Deterministic_For_All_Event_Types(string xmlFile)
        {
            EPCISDocument doc = OpenTraceabilityMappers.EPCISDocument.XML.Map(OpenTraceabilityTests.ReadTestData(xmlFile));
            Assert.That(doc.Events.Count, Is.GreaterThan(0));

            foreach (IEvent e in doc.Events)
            {
                string hash = EventHashGenerator.GenerateHash(e);
                Assert.That(hash, Does.Match(@"^ni:///sha-256;[0-9a-f]{64}\?ver=CBV2\.0$"),
                    $"Unexpected hash format: {hash}");
                // deterministic: hashing the same event again yields the same value
                Assert.That(EventHashGenerator.GenerateHash(e), Is.EqualTo(hash));
            }
        }

        /// <summary>
        /// Diagnostic helper: dumps the canonical pre-hash string and hash for each event so they
        /// can be diffed against the reference implementation output. Not run by default.
        /// </summary>
        [Explicit]
        [TestCase("Example_9.6.1-ObjectEvent.jsonld")]
        public void Dump_PreHash_Strings(string file)
        {
            string json = OpenTraceabilityTests.ReadTestData(file);
            EPCISDocument doc = OpenTraceabilityMappers.EPCISDocument.JSON.Map(json);

            foreach (IEvent e in doc.Events)
            {
                TestContext.Out.WriteLine(EventHashGenerator.GeneratePreHashString(e));
                TestContext.Out.WriteLine(EventHashGenerator.GenerateHash(e));
                TestContext.Out.WriteLine("---");
            }
        }
    }
}
