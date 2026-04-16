using Microsoft.AspNetCore.Mvc;
using OpenTraceability.Mappers;
using OpenTraceability.Models.Identifiers;
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
    [Route("{blob_id}/{type}/{identifier}")]
    public async Task<IActionResult> GetMasterData(string blob_id, string type, string identifier)
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

            // create a digital link URI version of the identifier as well.
            string dlUriId;
            switch (type)
            {
                case "product": dlUriId = $"https://id.gs1.org/01/{identifier}"; break;
                case "location": dlUriId = $"https://id.gs1.org/414/{identifier}"; break;
                case "party": dlUriId = $"https://id.gs1.org/417/{identifier}"; break;
                default: return BadRequest($"unknown master data type {type}");
            }

            var masterDataItem = doc.MasterData.FirstOrDefault(m => m.ID?.ToLower() == identifier.ToLower() || m.ID?.ToLower() == dlUriId.ToLower());
            if (masterDataItem == null && type == "product")
            {
                masterDataItem = doc.MasterData.FirstOrDefault(m =>
                {
                    if (m.VocabularyType != Interfaces.VocabularyType.Tradeitem)
                        return false;

                    GTIN gtin = new GTIN(m.ID);
                    string? gtin14 = gtin.ToGTIN14();
                    if (!string.IsNullOrEmpty(gtin14) && gtin14 == identifier)
                        return true;

                    return false;
                });
            }
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