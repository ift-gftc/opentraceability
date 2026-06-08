using Microsoft.AspNetCore.Authorization;
using Microsoft.AspNetCore.Mvc;
using OpenTraceability.Interfaces;
using OpenTraceability.TestServer.Core.Services;
using OpenTraceability.TestServer.Infrastructure;

namespace OpenTraceability.TestServer.Controllers;

[ApiController]
[Authorize]
[Route("masterdata")]
[Route("{datasetId}/masterdata")]
public class MasterDataController : ControllerBase
{
    private readonly MasterDataService _masterData;
    private readonly IngestionService _ingestionService;
    private readonly SupportedModules _modules;

    public MasterDataController(MasterDataService masterData, IngestionService ingestionService, SupportedModules modules)
    {
        _masterData = masterData;
        _ingestionService = ingestionService;
        _modules = modules;
    }

    /// <summary>Returns the module-minified master data definition for a single identifier.</summary>
    [HttpGet("{type}/{identifier}")]
    public async Task<IActionResult> GetMasterData(string type, string identifier)
    {
        string datasetId = Request.GetDatasetId();
        string? json = await _masterData.GetMasterDataJsonAsync(datasetId, identifier, _modules.Modules);
        if (json == null)
        {
            return NotFound($"Did not find master data for identifier {identifier}");
        }
        return Content(json, "application/json");
    }

    /// <summary>Returns all master data definitions of a given type (product / location / party).</summary>
    [HttpGet("{type}")]
    public async Task<IActionResult> GetMasterDataDefinitions(string type)
    {
        if (!TryMapVocabularyType(type, out var vocabType))
        {
            return BadRequest($"unknown master data type {type}");
        }
        string datasetId = Request.GetDatasetId();
        string json = await _masterData.GetMasterDataDefinitionsJsonAsync(datasetId, vocabType, _modules.Modules);
        return Content(json, "application/json");
    }

    /// <summary>Ingests GS1 Web Vocab JSON-LD master data (single object or array).</summary>
    [HttpPost]
    public async Task<IActionResult> PostMasterData()
    {
        try
        {
            Request.EnableBuffering();
            Request.Body.Position = 0;
            string rawBody = await new StreamReader(Request.Body).ReadToEndAsync();

            string datasetId = Request.GetDatasetId();
            int count = await _ingestionService.IngestMasterDataAsync(datasetId, rawBody);
            return Ok(new { masterDataStored = count });
        }
        catch (Exception ex)
        {
            return BadRequest("Failed to ingest master data: " + ex.Message);
        }
    }

    private static bool TryMapVocabularyType(string type, out VocabularyType vocabType)
    {
        switch (type.ToLower())
        {
            case "product":
            case "tradeitem":
                vocabType = VocabularyType.Tradeitem; return true;
            case "location":
                vocabType = VocabularyType.Location; return true;
            case "party":
            case "tradingparty":
                vocabType = VocabularyType.TradingParty; return true;
            default:
                vocabType = VocabularyType.Unknown; return false;
        }
    }
}
