using System.Collections.Generic;
using System.Threading.Tasks;
using OpenTraceability.Interfaces;
using OpenTraceability.Models.Events;
using OpenTraceability.Queries;

namespace OpenTraceability.TestServer.Core.Data
{
    /// <summary>
    /// Repository over the traceability store. Shared by the ASP.NET controllers and the
    /// WireMock test host so query/resolution behavior never diverges.
    /// </summary>
    public interface ITraceabilityStore
    {
        /// <summary>Ensures the database/schema exists.</summary>
        Task InitializeAsync();

        /// <summary>
        /// Upserts events (and their search index rows) into the given dataset. <paramref name="namespaces"/>
        /// are the source document's JSON-LD namespaces, needed to serialize prefixed KDEs (e.g. gdst, cbvmda).
        /// </summary>
        Task UpsertEventsAsync(string datasetId, IEnumerable<IEvent> events, IDictionary<string, string>? namespaces = null);

        /// <summary>Upserts master data vocabulary elements into the given dataset.</summary>
        Task UpsertMasterDataAsync(string datasetId, IEnumerable<IVocabularyElement> masterData);

        /// <summary>
        /// Queries events in the dataset using the EPCIS query parameters. Returns an
        /// EPCIS Query Document containing the matching events.
        /// </summary>
        Task<EPCISQueryDocument> QueryEventsAsync(string datasetId, EPCISQueryParameters parameters);

        /// <summary>Looks up a single master data element by identifier.</summary>
        Task<IVocabularyElement?> GetMasterDataAsync(string datasetId, string identifier);

        /// <summary>Returns all master data of a given vocabulary type in the dataset.</summary>
        Task<List<IVocabularyElement>> GetMasterDataByTypeAsync(string datasetId, VocabularyType type);

        /// <summary>Returns true if the dataset contains any data for the identifier (master data or event participation).</summary>
        Task<bool> IdentifierExistsAsync(string datasetId, string identifier);
    }
}
