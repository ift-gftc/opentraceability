using System.Threading.Tasks;
using Newtonsoft.Json.Linq;
using OpenTraceability.Interfaces;
using OpenTraceability.Mappers;
using OpenTraceability.Models.Events;
using OpenTraceability.TestServer.Core.Data;

namespace OpenTraceability.TestServer.Core.Services
{
    /// <summary>
    /// Handles ingestion of posted EPCIS documents and master data into the store.
    /// </summary>
    public class IngestionService
    {
        private readonly ITraceabilityStore _store;

        public IngestionService(ITraceabilityStore store)
        {
            _store = store;
        }

        /// <summary>
        /// Parses and stores an EPCIS Document (events + any master data in the header).
        /// When <paramref name="checkSchema"/> is true (default) the document is validated against
        /// the EPCIS schema (throws <see cref="OpenTraceability.Utility.OpenTraceabilitySchemaException"/>
        /// on invalid input). Pass false for trusted/seed data or offline test hosts.
        /// </summary>
        public async Task<int> IngestEpcisDocumentAsync(string datasetId, string rawBody, EPCISDataFormat format, bool checkSchema = true)
        {
            EPCISDocument doc = ParseDocument(rawBody, format, checkSchema);

            if (doc.Events.Count > 0)
            {
                await _store.UpsertEventsAsync(datasetId, doc.Events, doc.Namespaces);
            }
            if (doc.MasterData.Count > 0)
            {
                await _store.UpsertMasterDataAsync(datasetId, doc.MasterData);
            }

            return doc.Events.Count;
        }

        /// <summary>
        /// Parses an EPCIS Document. When schema validation is not desired, JSON documents are read
        /// through the query-document mapper (which exposes a no-schema read path and parses events +
        /// header master data identically) and converted back to an EPCIS Document.
        /// </summary>
        private static EPCISDocument ParseDocument(string rawBody, EPCISDataFormat format, bool checkSchema)
        {
            if (format == EPCISDataFormat.XML)
            {
                return OpenTraceabilityMappers.EPCISDocument.XML.Map(rawBody);
            }

            if (checkSchema)
            {
                return OpenTraceabilityMappers.EPCISDocument.JSON.Map(rawBody);
            }

            // Re-shape the EPCIS Document JSON into EPCIS Query Document JSON so it can be read via the
            // query-document mapper, which exposes a no-schema read path. The header master data path is
            // identical; only the event list location differs.
            JObject json = JObject.Parse(rawBody);
            json["type"] = "EPCISQueryDocument";
            if (json["epcisBody"] is JObject body && body["eventList"] is JArray eventList)
            {
                body.Remove("eventList");
                body["queryResults"] = new JObject
                {
                    ["resultsBody"] = new JObject { ["eventList"] = eventList }
                };
            }
            var queryDoc = OpenTraceabilityMappers.EPCISQueryDocument.JSON.Map(json.ToString(), checkSchema: false);
            return queryDoc.ToEPCISDocument();
        }

        /// <summary>
        /// Parses and stores GS1 Web Vocab JSON-LD master data (single object or array).
        /// </summary>
        public async Task<int> IngestMasterDataAsync(string datasetId, string rawBody)
        {
            var elements = MasterDataParser.ParseMany(rawBody);
            if (elements.Count > 0)
            {
                await _store.UpsertMasterDataAsync(datasetId, elements);
            }
            return elements.Count;
        }
    }
}
