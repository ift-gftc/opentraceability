using System.Collections.Generic;
using System.Threading.Tasks;
using Newtonsoft.Json.Linq;
using OpenTraceability.Interfaces;
using OpenTraceability.Mappers;
using OpenTraceability.TestServer.Core.Data;
using OpenTraceability.TestServer.Core.Modules;

namespace OpenTraceability.TestServer.Core.Services
{
    /// <summary>
    /// Resolves master data from the store and returns module-minified JSON-LD.
    /// </summary>
    public class MasterDataService
    {
        private readonly ITraceabilityStore _store;

        public MasterDataService(ITraceabilityStore store)
        {
            _store = store;
        }

        /// <summary>
        /// Returns the minified GS1 Web Vocab JSON-LD for a single master data element, or null
        /// if not found.
        /// </summary>
        public async Task<string?> GetMasterDataJsonAsync(string datasetId, string identifier, ISet<GdstModule> allowedModules)
        {
            var element = await _store.GetMasterDataAsync(datasetId, identifier);
            if (element == null) return null;
            string json = OpenTraceabilityMappers.MasterData.GS1WebVocab.Map(element);
            return ModuleMinifier.Minify(json, allowedModules);
        }

        /// <summary>
        /// Returns all master data definitions of a vocabulary type as a minified JSON-LD array.
        /// </summary>
        public async Task<string> GetMasterDataDefinitionsJsonAsync(string datasetId, VocabularyType type, ISet<GdstModule> allowedModules)
        {
            var elements = await _store.GetMasterDataByTypeAsync(datasetId, type);
            var array = new JArray();
            foreach (var element in elements)
            {
                string json = OpenTraceabilityMappers.MasterData.GS1WebVocab.Map(element);
                array.Add(JToken.Parse(json));
            }
            return ModuleMinifier.Minify(array.ToString(), allowedModules);
        }
    }
}
