using Microsoft.AspNetCore.Mvc;
using OpenTraceability.Mappers;
using OpenTraceability.TestServer.Services.Interfaces;

namespace OpenTraceability.TestServer.Controllers;

[Route("masterdata")]
public class MasterDataController : ControllerBase
{
    private IEPCISBlobService _blobService;
    private IConfiguration _config;

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