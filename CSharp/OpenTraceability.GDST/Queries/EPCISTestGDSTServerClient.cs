using OpenTraceability.Mappers;
using OpenTraceability.Models.Events;
using OpenTraceability.Queries;
using OpenTraceability.Utility;

namespace OpenTraceability.GDST.Queries
{
    /// <summary>
    /// A test server client that additionally knows how to resolve GDST-specific master data.
    /// All of the posting/querying/traceback behaviour is inherited from <see cref="EPCISTestServerClient"/>;
    /// only the GDST master data resolution is added here (it lives in this assembly because it depends
    /// on the GDST master data types, which the core library does not reference).
    /// </summary>
    public class EPCISTestGDSTServerClient : EPCISTestServerClient
    {
        public EPCISTestGDSTServerClient(string baseURL, string apiKey, EPCISDataFormat format, EPCISVersion version, string? datasetID = null, IEnumerable<string>? modules = null)
            : base(baseURL, apiKey, format, version, datasetID, modules)
        {
        }

        /// <summary>
        /// Resolves all the unknown GDST master data in the EPCIS document.
        /// </summary>
        /// <param name="doc">The EPCIS document to resolve the master data for.</param>
        public async Task ResolveGDSTMasterData(EPCISBaseDocument doc)
        {
            using (var clientItem = HttpClientPool.GetClient())
            {
                var client = clientItem.Value;
                await EnsureDatasetAsync(client);
                DigitalLinkQueryOptions options = BuildDigitalLinkOptions();
                await GDSTMasterDataResolver.ResolveGDSTMasterData(options, doc, client);
            }
        }
    }
}
