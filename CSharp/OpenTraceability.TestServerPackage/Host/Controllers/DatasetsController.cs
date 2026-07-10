using Microsoft.AspNetCore.Authorization;
using Microsoft.AspNetCore.Mvc;
using OpenTraceability.TestServer.Core.Data;
using OpenTraceability.TestServer.Core.Models;

namespace OpenTraceability.TestServer.Controllers;

/// <summary>
/// Management API for datasets and their module sets. A dataset must exist before any data can be
/// read from or written to it: the dataset record's modules drive module minification of every
/// response served under /{datasetId}/..., which is how one deployed server serves core-only,
/// seafood, seafood+wildcaught and seafood+wildcaught+aquaculture data side by side.
/// </summary>
[ApiController]
[Authorize]
[Route("datasets")]
public class DatasetsController : ControllerBase
{
    // Dataset ids appear as the leading path segment of /{datasetId}/... routes, so ids matching a
    // top-level route root would be unreachable or ambiguous.
    private static readonly HashSet<string> _reservedIds = new(StringComparer.OrdinalIgnoreCase)
    {
        "datasets", "epcis", "masterdata", "digitallink", "traceback", "capability-test", "health", "swagger"
    };

    private readonly ITraceabilityStore _store;

    public DatasetsController(ITraceabilityStore store)
    {
        _store = store;
    }

    [HttpGet]
    public async Task<IActionResult> List()
    {
        var datasets = await _store.ListDatasetsAsync();
        return Ok(datasets.Select(DatasetModel.FromDataset).ToList());
    }

    [HttpGet("{datasetId}")]
    public async Task<IActionResult> Get(string datasetId)
    {
        var dataset = await _store.GetDatasetAsync(datasetId);
        if (dataset == null)
        {
            return NotFound($"Unknown dataset '{datasetId}'.");
        }
        return Ok(DatasetModel.FromDataset(dataset));
    }

    /// <summary>Creates a new dataset. Fails with 409 if the dataset already exists.</summary>
    [HttpPost]
    public async Task<IActionResult> Create([FromBody] DatasetModel request)
    {
        if (request == null || string.IsNullOrWhiteSpace(request.DatasetId))
        {
            return BadRequest("datasetId is required.");
        }
        if (!TryValidate(request.DatasetId, request.Modules, out var modules, out var error))
        {
            return BadRequest(error);
        }
        if (await _store.GetDatasetAsync(request.DatasetId) != null)
        {
            return Conflict($"Dataset '{request.DatasetId}' already exists.");
        }

        var created = await _store.UpsertDatasetAsync(new Dataset
        {
            DatasetId = request.DatasetId,
            Modules = modules,
            Description = request.Description
        });
        return CreatedAtAction(nameof(Get), new { datasetId = created.DatasetId }, DatasetModel.FromDataset(created));
    }

    /// <summary>Creates or updates a dataset (idempotent upsert of its modules/description).</summary>
    [HttpPut("{datasetId}")]
    public async Task<IActionResult> Upsert(string datasetId, [FromBody] UpsertDatasetRequest request)
    {
        if (!TryValidate(datasetId, request?.Modules, out var modules, out var error))
        {
            return BadRequest(error);
        }

        var dataset = await _store.UpsertDatasetAsync(new Dataset
        {
            DatasetId = datasetId,
            Modules = modules,
            Description = request?.Description
        });
        return Ok(DatasetModel.FromDataset(dataset));
    }

    /// <summary>Deletes a dataset record; with purgeData=true its events and master data are deleted too.</summary>
    [HttpDelete("{datasetId}")]
    public async Task<IActionResult> Delete(string datasetId, [FromQuery] bool purgeData = false)
    {
        bool deleted = await _store.DeleteDatasetAsync(datasetId, purgeData);
        if (!deleted)
        {
            return NotFound($"Unknown dataset '{datasetId}'.");
        }
        return NoContent();
    }

    /// <summary>Deletes all events and master data in a dataset, keeping the dataset record.</summary>
    [HttpPost("{datasetId}/clear")]
    public async Task<IActionResult> Clear(string datasetId)
    {
        if (await _store.GetDatasetAsync(datasetId) == null)
        {
            return NotFound($"Unknown dataset '{datasetId}'.");
        }
        await _store.ClearDatasetDataAsync(datasetId);
        return NoContent();
    }

    private static bool TryValidate(string datasetId, IEnumerable<string>? moduleNames, out List<string> modules, out string error)
    {
        modules = new List<string>();
        if (_reservedIds.Contains(datasetId))
        {
            error = $"'{datasetId}' is a reserved route name and cannot be used as a dataset id.";
            return false;
        }
        if (datasetId.Any(c => c == '/' || c == '\\' || c == '?' || c == '#' || c == '%' || char.IsWhiteSpace(c)))
        {
            error = "Dataset ids cannot contain slashes, whitespace, or the characters ? # %.";
            return false;
        }
        if (!ModuleNames.TryParseStrict(moduleNames, out modules, out var invalid))
        {
            error = $"Unknown module names: {string.Join(", ", invalid)}. Valid modules: Seafood, Wildcaught, Aquaculture.";
            return false;
        }
        error = string.Empty;
        return true;
    }
}
