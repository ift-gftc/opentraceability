using Microsoft.AspNetCore.Mvc;
using Microsoft.AspNetCore.Mvc.Filters;
using OpenTraceability.TestServer.Core.Data;

namespace OpenTraceability.TestServer.Infrastructure
{
    /// <summary>
    /// Resolves the request's dataset (route segment, then X-Dataset-Id header, then "default")
    /// against the persisted dataset records and populates <see cref="DatasetContext"/>. Unknown
    /// datasets are rejected with 404 for every verb: a dataset must be created (via the /datasets
    /// API or a SeedData manifest) before it can be read or written, so its module set is always
    /// known. Apply with [ServiceFilter(typeof(DatasetResolutionFilter))].
    /// </summary>
    public class DatasetResolutionFilter : IAsyncActionFilter
    {
        private readonly ITraceabilityStore _store;
        private readonly DatasetContext _datasetContext;

        public DatasetResolutionFilter(ITraceabilityStore store, DatasetContext datasetContext)
        {
            _store = store;
            _datasetContext = datasetContext;
        }

        public async Task OnActionExecutionAsync(ActionExecutingContext context, ActionExecutionDelegate next)
        {
            string datasetId = context.HttpContext.Request.GetDatasetId();
            var dataset = await _store.GetDatasetAsync(datasetId);
            if (dataset == null)
            {
                context.Result = new NotFoundObjectResult(new ProblemDetails
                {
                    Title = "Unknown dataset",
                    Detail = $"Unknown dataset '{datasetId}'. Create it via PUT /datasets/{datasetId} or a SeedData manifest.",
                    Status = StatusCodes.Status404NotFound
                });
                return;
            }

            _datasetContext.DatasetId = dataset.DatasetId;
            _datasetContext.Record = dataset;
            _datasetContext.Modules = dataset.GetExpandedModules();

            await next();
        }
    }
}
