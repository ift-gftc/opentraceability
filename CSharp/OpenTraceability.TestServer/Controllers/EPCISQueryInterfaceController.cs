using Microsoft.AspNetCore.Http.Extensions;
using Microsoft.AspNetCore.Mvc;
using OpenTraceability.Interfaces;
using OpenTraceability.Mappers;
using OpenTraceability.Models.Events;
using OpenTraceability.Models.Identifiers;
using OpenTraceability.Queries;
using OpenTraceability.TestServer.Models;
using OpenTraceability.TestServer.Services.Interfaces;
using OpenTraceability.Utility;

namespace OpenTraceability.TestServer.Controllers;

[ApiController]
[Route("epcis")]
public class EPCISQueryInterfaceController : ControllerBase
{
    IEPCISBlobService _blobService;
    IConfiguration _config;

    public EPCISQueryInterfaceController(IEPCISBlobService blobService, IConfiguration config)
    {
        _blobService = blobService;
        _config = config;
    }

    [HttpGet]
    [Route("{blob_id}/events")]
    public async Task<IActionResult> GetEvents(string blob_id)
    {
        try
        {
            string url = this.HttpContext.Request.GetEncodedUrl();
            Uri relativeUri = new Uri(url, UriKind.Absolute);
            EPCISQueryParameters parameters = new EPCISQueryParameters(relativeUri);

            // load the blob
            var blob = await _blobService.LoadBlob(blob_id);

            // throw an error if the blob does not exist
            if (blob == null)
            {
                return BadRequest("blob does not exist");
            }

            // convert blob into EPCIS Document
            var doc = blob.ToEPCISDocument();

            // apply filters to the blob
            var events = doc.FilterEvents(parameters);

            // convert results into EPCIS Query Document
            EPCISQueryDocument queryDoc = new EPCISQueryDocument();
            queryDoc.Attributes = doc.Attributes;
            queryDoc.Namespaces = doc.Namespaces;
            queryDoc.Contexts = doc.Contexts;
            queryDoc.CreationDate = DateTime.UtcNow;
            queryDoc.EPCISVersion = doc.EPCISVersion;
            queryDoc.Events = events;
            queryDoc.QueryName = blob_id;
            queryDoc.SubscriptionID = blob_id;

            // TODO: set headers on the response

            // write the response into the body
            IEPCISQueryDocumentMapper mapper = OpenTraceabilityMappers.EPCISQueryDocument.XML;
            if (blob.Format == EPCISDataFormat.JSON)
            {
                mapper = OpenTraceabilityMappers.EPCISQueryDocument.JSON;
            }
            var rawDoc = mapper.Map(queryDoc);
            await this.HttpContext.Response.WriteAsync(rawDoc);

            // return empty result (IActionResult)
            return Empty;
        }
        catch (Exception ex)
        {
            Console.WriteLine(ex);

            // if the write to the response has already begun, we cannot alter the response
            if (HttpContext.Response.HasStarted)
            {
                throw;
            }

            return StatusCode(500, ex.ToString());
        }
    }

    /// <summary>
    /// Expects the events posted to be a valid EPCIS Document. (not an EPCIS Query Document)
    /// </summary>
    [HttpPost]
    [Route("{blob_id}/events")]
    public async Task<IActionResult> PostEvents(string blob_id)
    {
        string? versionStr = this.HttpContext.Request.Headers["GS1-EPCIS-Version"].FirstOrDefault();

        if (versionStr == null)
        {
            return BadRequest("no GS1-EPCIS-Version header provided.");
        }

        EPCISVersion version = EPCISVersion.V2;
        EPCISDataFormat format = EPCISDataFormat.JSON;
        IEPCISDocumentMapper mapper;
        if (versionStr == "1.2")
        {
            version = EPCISVersion.V1;
            format = EPCISDataFormat.XML;
            mapper = OpenTraceabilityMappers.EPCISDocument.XML;
        }
        else if (versionStr == "2.0")
        {
            version = EPCISVersion.V2;

            string? contentType = this.HttpContext.Request.ContentType?.Split(';').First().Trim();
            if (contentType == null)
            {
                return BadRequest("Content Type header is missing.");
            }
            else if (contentType == "application/json")
            {
                format = EPCISDataFormat.JSON;
                mapper = OpenTraceabilityMappers.EPCISDocument.JSON;
            }
            else if (contentType == "application/xml")
            {
                format = EPCISDataFormat.XML;
                mapper = OpenTraceabilityMappers.EPCISDocument.XML;
            }
            else
            {
                return BadRequest("Content Type must either be application/json or application/xml");
            }
        }
        else
        {
            return BadRequest("GS1-EPCIS-Version header must either be 1.2 or 2.0");
        }

        // read the EPCISDocument from the body
        try
        {
            this.HttpContext.Request.EnableBuffering();
            this.HttpContext.Request.Body.Position = 0;
            var rawRequestBody = await new StreamReader(this.HttpContext.Request.Body).ReadToEndAsync();

            var doc = mapper.Map(rawRequestBody);

            // create the blob
            EPCISBlob blob = new EPCISBlob()
            {
                ID = blob_id,
                RawData = rawRequestBody,
                Version = version,
                Format = format,
                Created = DateTime.UtcNow
            };

            // save the blob
            if (await _blobService.SaveBlob(blob))
            {
                return Ok();
            }
            else
            {
                return BadRequest("failed to save");
            }
        }
        catch (OpenTraceabilitySchemaException ex)
        {
            Console.WriteLine(ex);
            return BadRequest("Schema errors found: " + ex.Message);
        }
        catch (Exception ex)
        {
            Console.WriteLine(ex);
            return BadRequest("Unhandled exception occured.");
        }
    }


}

