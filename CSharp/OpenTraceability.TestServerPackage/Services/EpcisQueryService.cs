using System.Collections.Generic;
using System.Threading.Tasks;
using OpenTraceability.GDST;
using OpenTraceability.Mappers;
using OpenTraceability.Queries;
using OpenTraceability.TestServer.Core.Data;
using OpenTraceability.GDST.Modules;

namespace OpenTraceability.TestServer.Core.Services
{
    /// <summary>
    /// Executes EPCIS queries against the store and returns module-minified JSON-LD.
    /// </summary>
    public class EpcisQueryService
    {
        private readonly ITraceabilityStore _store;

        public EpcisQueryService(ITraceabilityStore store)
        {
            _store = store;
        }

        /// <summary>
        /// Queries events and returns the minified EPCIS Query Document as JSON-LD.
        /// </summary>
        public async Task<string> QueryEventsJsonAsync(string datasetId, EPCISQueryParameters parameters, ISet<GdstModule> allowedModules)
        {
            var doc = await _store.QueryEventsAsync(datasetId, parameters);
            doc.QueryName = "SimpleEventQuery";
            // We are producing output (not validating input), so skip schema validation.
            string json = OpenTraceabilityMappers.EPCISQueryDocument.JSON.Map(doc, checkSchema: false);
            return ModuleMinifier.Minify(json, allowedModules);
        }
    }
}
