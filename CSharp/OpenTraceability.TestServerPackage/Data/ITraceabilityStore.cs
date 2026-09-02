using OpenTraceability.Interfaces;
using OpenTraceability.Models.Events;
using OpenTraceability.Queries;
using OpenTraceability.TestServer.Core.Models;

namespace OpenTraceability.TestServer.Core.Data
{
    /// <summary>
    /// Repository over the traceability store.
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

        /// <summary>Looks up a dataset record by id, or null if the dataset has not been created.</summary>
        Task<Dataset?> GetDatasetAsync(string datasetId);

        /// <summary>Returns all dataset records.</summary>
        Task<List<Dataset>> ListDatasetsAsync();

        /// <summary>
        /// Creates or updates a dataset record (its module set and description). CreatedUtc is
        /// preserved on update.
        /// </summary>
        Task<Dataset> UpsertDatasetAsync(Dataset dataset);

        /// <summary>
        /// Deletes a dataset record. When <paramref name="purgeData"/> is true the dataset's
        /// events, search entries and master data are deleted too. Returns false if no record exists.
        /// </summary>
        Task<bool> DeleteDatasetAsync(string datasetId, bool purgeData);

        /// <summary>Deletes all events, search entries and master data in a dataset, keeping the record.</summary>
        Task ClearDatasetDataAsync(string datasetId);
    }
}
