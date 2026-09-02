using OpenTraceability.Interfaces;
using OpenTraceability.Models.Events;
using OpenTraceability.Queries;

namespace OpenTraceability.Tests.Events
{
    /// <summary>
    /// Unit tests for <see cref="EPCISBaseDocument.FilterEvents"/>, focused on the time-bound
    /// parameters and the transformation ID filter.
    /// </summary>
    /// <remarks>
    /// The LT_ parameters are the ones defined by EPCIS and are exclusive of the bound; the LE_
    /// parameters are deprecated but must keep behaving inclusively for callers that still target
    /// GDST 1.2 solutions. These tests pin both semantics so a future cleanup cannot quietly change
    /// the deprecated behavior.
    /// </remarks>
    [TestFixture]
    [Category("UnitTest")]
    public class FilterEventsTests
    {
        private static readonly DateTimeOffset Early = new DateTimeOffset(2026, 3, 1, 8, 0, 0, TimeSpan.Zero);
        private static readonly DateTimeOffset Boundary = new DateTimeOffset(2026, 3, 2, 8, 0, 0, TimeSpan.Zero);
        private static readonly DateTimeOffset Late = new DateTimeOffset(2026, 3, 3, 8, 0, 0, TimeSpan.Zero);

        /// <summary>
        /// Builds a document with one event at each of the three fixed timestamps, using the same
        /// value for both event time and record time so one fixture serves both sets of filters.
        /// </summary>
        private static EPCISQueryDocument BuildDocument()
        {
            EPCISQueryDocument doc = new EPCISQueryDocument();
            doc.Events.Add(BuildObjectEvent("urn:uuid:early", Early));
            doc.Events.Add(BuildObjectEvent("urn:uuid:boundary", Boundary));
            doc.Events.Add(BuildObjectEvent("urn:uuid:late", Late));
            return doc;
        }

        private static ObjectEvent<EventILMD> BuildObjectEvent(string eventID, DateTimeOffset timestamp)
        {
            return new ObjectEvent<EventILMD>
            {
                EventID = new Uri(eventID),
                EventTime = timestamp,
                RecordTime = timestamp
            };
        }

        private static List<string> EventIDs(List<IEvent> events)
        {
            return events.Select(e => e.EventID!.ToString()).ToList();
        }

        // --- LT_eventTime / LT_recordTime: exclusive of the bound ---

        [Test]
        public void FilterEvents_LTEventTime_ExcludesTheEventAtTheBound()
        {
            // Arrange
            EPCISQueryDocument doc = BuildDocument();
            EPCISQueryParameters parameters = new EPCISQueryParameters();
            parameters.query.LT_eventTime = Boundary;

            // Act
            List<IEvent> results = doc.FilterEvents(parameters);

            // Assert
            Assert.That(EventIDs(results), Is.EqualTo(new List<string> { "urn:uuid:early" }), "LT_eventTime must exclude an event whose event time equals the bound.");
        }

        [Test]
        public void FilterEvents_LTRecordTime_ExcludesTheEventAtTheBound()
        {
            // Arrange
            EPCISQueryDocument doc = BuildDocument();
            EPCISQueryParameters parameters = new EPCISQueryParameters();
            parameters.query.LT_recordTime = Boundary;

            // Act
            List<IEvent> results = doc.FilterEvents(parameters);

            // Assert
            Assert.That(EventIDs(results), Is.EqualTo(new List<string> { "urn:uuid:early" }), "LT_recordTime must exclude an event whose record time equals the bound.");
        }

        [Test]
        public void FilterEvents_GEAndLTEventTimeWindow_ReturnsOnlyEventsInsideTheWindow()
        {
            // Arrange - the standard's required combination bounds a half-open window.
            EPCISQueryDocument doc = BuildDocument();
            EPCISQueryParameters parameters = new EPCISQueryParameters();
            parameters.query.GE_eventTime = Boundary;
            parameters.query.LT_eventTime = Late;

            // Act
            List<IEvent> results = doc.FilterEvents(parameters);

            // Assert
            Assert.That(EventIDs(results), Is.EqualTo(new List<string> { "urn:uuid:boundary" }), "The window must include the lower bound and exclude the upper bound.");
        }

        // --- LE_eventTime / LE_recordTime: deprecated, but still inclusive ---

        [Test]
        public void FilterEvents_DeprecatedLEEventTime_StillIncludesTheEventAtTheBound()
        {
            // Arrange
            EPCISQueryDocument doc = BuildDocument();
            EPCISQueryParameters parameters = new EPCISQueryParameters();
#pragma warning disable CS0618
            parameters.query.LE_eventTime = Boundary;
#pragma warning restore CS0618

            // Act
            List<IEvent> results = doc.FilterEvents(parameters);

            // Assert
            Assert.That(EventIDs(results), Is.EqualTo(new List<string> { "urn:uuid:early", "urn:uuid:boundary" }), "The deprecated LE_eventTime must keep its inclusive behavior.");
        }

        [Test]
        public void FilterEvents_DeprecatedLERecordTime_StillIncludesTheEventAtTheBound()
        {
            // Arrange
            EPCISQueryDocument doc = BuildDocument();
            EPCISQueryParameters parameters = new EPCISQueryParameters();
#pragma warning disable CS0618
            parameters.query.LE_recordTime = Boundary;
#pragma warning restore CS0618

            // Act
            List<IEvent> results = doc.FilterEvents(parameters);

            // Assert
            Assert.That(EventIDs(results), Is.EqualTo(new List<string> { "urn:uuid:early", "urn:uuid:boundary" }), "The deprecated LE_recordTime must keep its inclusive behavior.");
        }

        // --- EQ_transformationID ---

        [Test]
        public void FilterEvents_EQTransformationID_ReturnsOnlyTheMatchingTransformationEvent()
        {
            // Arrange
            EPCISQueryDocument doc = BuildDocument();
            doc.Events.Add(new TransformationEvent<EventILMD>
            {
                EventID = new Uri("urn:uuid:transform-a"),
                EventTime = Early,
                RecordTime = Early,
                TransformationID = "transform-a"
            });
            doc.Events.Add(new TransformationEvent<EventILMD>
            {
                EventID = new Uri("urn:uuid:transform-b"),
                EventTime = Early,
                RecordTime = Early,
                TransformationID = "transform-b"
            });

            EPCISQueryParameters parameters = new EPCISQueryParameters();
            parameters.query.EQ_transformationID = new List<string> { "transform-a" };

            // Act
            List<IEvent> results = doc.FilterEvents(parameters);

            // Assert - the non-transformation events are excluded too, because they carry no ID to match.
            Assert.That(EventIDs(results), Is.EqualTo(new List<string> { "urn:uuid:transform-a" }));
        }

        [Test]
        public void FilterEvents_EQTransformationID_MatchesCaseInsensitively()
        {
            // Arrange
            EPCISQueryDocument doc = new EPCISQueryDocument();
            doc.Events.Add(new TransformationEvent<EventILMD>
            {
                EventID = new Uri("urn:uuid:transform-a"),
                EventTime = Early,
                RecordTime = Early,
                TransformationID = "Transform-A"
            });

            EPCISQueryParameters parameters = new EPCISQueryParameters();
            parameters.query.EQ_transformationID = new List<string> { "transform-a" };

            // Act
            List<IEvent> results = doc.FilterEvents(parameters);

            // Assert
            Assert.That(results.Count, Is.EqualTo(1));
        }

        [Test]
        public void FilterEvents_EQTransformationID_NoMatch_ReturnsEmpty()
        {
            // Arrange
            EPCISQueryDocument doc = BuildDocument();
            EPCISQueryParameters parameters = new EPCISQueryParameters();
            parameters.query.EQ_transformationID = new List<string> { "does-not-exist" };

            // Act
            List<IEvent> results = doc.FilterEvents(parameters);

            // Assert
            Assert.That(results, Is.Empty);
        }

        [Test]
        public void FilterEvents_NoTimeOrTransformationParameters_ReturnsEveryEvent()
        {
            // Arrange
            EPCISQueryDocument doc = BuildDocument();

            // Act
            List<IEvent> results = doc.FilterEvents(new EPCISQueryParameters());

            // Assert
            Assert.That(results.Count, Is.EqualTo(3));
        }
    }
}
