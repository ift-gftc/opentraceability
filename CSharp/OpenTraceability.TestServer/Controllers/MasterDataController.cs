using Microsoft.AspNetCore.Http.Extensions;
using Microsoft.AspNetCore.Mvc;
using Newtonsoft.Json.Linq;
using OpenTraceability.Interfaces;
using OpenTraceability.Mappers;
using OpenTraceability.Models.Events;
using OpenTraceability.Models.Identifiers;
using OpenTraceability.Models.MasterData;
using OpenTraceability.Queries;
using OpenTraceability.TestServer.Models;
using OpenTraceability.TestServer.Services.Interfaces;
using OpenTraceability.Utility;

namespace OpenTraceability.TestServer.Controllers;

[Route("masterdata")]
public class MasterDataController : ControllerBase
{
    IEPCISBlobService _blobService;
    IConfiguration _config;

    public MasterDataController(IEPCISBlobService blobService, IConfiguration config)
    {
        _blobService = blobService;
        _config = config;
    }

    [HttpGet]
    [Route("{blob_id}/{identifier}")]
    public async Task<IActionResult> GetMasterData(string blob_id, string identifier)
    {
        try
        {
            // load the blob
            var blob = await _blobService.LoadBlob(blob_id);

            // throw an error if the blob does not exist
            if (blob == null)
            {
                return BadRequest("blob does not exist");
            }

            // convert blob into EPCIS Document
            var doc = blob.ToEPCISDocument();

            var masterDataItem = doc.MasterData.FirstOrDefault(m => m.ID?.ToLower() == identifier.ToLower());
            if (masterDataItem == null)
            {
                return NotFound($"Did not find master data for identifier {identifier}");
            }
            else
            {
                string json = OpenTraceabilityMappers.MasterData.GS1WebVocab.Map(masterDataItem);

                await this.Response.WriteAsync(json);

                return Empty;
            }
        }
        catch (Exception ex)
        {
            Console.WriteLine(ex);
            throw;
        }
    }
}

