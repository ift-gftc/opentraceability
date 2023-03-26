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
            var events = FilterEvents(doc, parameters);

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
            throw;
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
                Format = format
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

    private List<IEvent> FilterEvents(EPCISDocument doc, EPCISQueryParameters parameters)
    {
        List<IEvent> events = new List<IEvent>();

        foreach (var evt in doc.Events)
        {
            // filter: GE_eventTime
            if (parameters.query.GE_eventTime != null)
            {
                if (evt.EventTime == null || evt.EventTime < parameters.query.GE_eventTime)
                {
                    continue;
                }
            }

            // filter: LE_eventTime
            if (parameters.query.LE_eventTime != null)
            {
                if (evt.EventTime == null || evt.EventTime > parameters.query.LE_eventTime)
                {
                    continue;
                }
            }

            // filter: GE_recordTime
            if (parameters.query.GE_recordTime != null)
            {
                if (evt.RecordTime == null || evt.RecordTime < parameters.query.GE_recordTime)
                {
                    continue;
                }
            }

            // filter: LE_recordTime
            if (parameters.query.LE_recordTime != null)
            {
                if (evt.RecordTime == null || evt.RecordTime > parameters.query.LE_recordTime)
                {
                    continue;
                }
            }

            // filter: EQ_bizStep
            if (parameters.query.EQ_bizStep != null && parameters.query.EQ_bizStep.Count > 0)
            {
                if (!HasUriMatch(evt.BusinessStep, parameters.query.EQ_bizStep, "https://ref.gs1.org/cbv/BizStep-", "urn:epcglobal:cbv:bizstep:"))
                {
                    continue;
                }
            }

            // filter: EQ_bizLocation
            if (parameters.query.EQ_bizLocation != null && parameters.query.EQ_bizLocation.Count > 0)
            {
                if (evt.Location?.GLN == null || !parameters.query.EQ_bizLocation.Select(e => e.ToString().ToLower()).Contains(evt.Location.GLN.ToString().ToLower()))
                {
                    continue;
                }
            }

            // filter: MATCH_anyEPC
            if (parameters.query.MATCH_anyEPC != null && parameters.query.MATCH_anyEPC.Count > 0)
            {
                if (!HasMatch(evt, parameters.query.MATCH_anyEPC))
                {
                    continue;
                }
            }

            // filter: MATCH_anyEPCClass
            if (parameters.query.MATCH_anyEPCClass != null && parameters.query.MATCH_anyEPCClass.Count > 0)
            {
                if (!HasMatch(evt, parameters.query.MATCH_anyEPCClass))
                {
                    continue;
                }
            }

            // filter: MATCH_epc
            if (parameters.query.MATCH_epc != null && parameters.query.MATCH_epc.Count > 0)
            {
                if (!HasMatch(evt, parameters.query.MATCH_epc, EventProductType.Reference, EventProductType.Child))
                {
                    continue;
                }
            }

            // filter: MATCH_epcClass
            if (parameters.query.MATCH_epcClass != null && parameters.query.MATCH_epcClass.Count > 0)
            {
                if (!HasMatch(evt, parameters.query.MATCH_epcClass, EventProductType.Reference, EventProductType.Child))
                {
                    continue;
                }
            }

            events.Add(evt);
        }

        return events;
    }

    private bool HasMatch(IEvent evt, List<string> epcs, params EventProductType[] allowedTypes)
    {
        foreach (var epc_matchStr in epcs)
        {
            EPC epc_match = new EPC(epc_matchStr);
            foreach (var product in evt.Products)
            {
                if (allowedTypes.Count() == 0 || allowedTypes.Contains(product.Type))
                {
                    if (epc_match.Matches(product.EPC))
                    {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    private bool HasUriMatch(Uri? uri, List<string> filter, string prefix, string replacePrefix)
    {
        // make sure all of the EQ_bizStep are converted into URI format before comparing
        for (int i = 0; i < filter.Count; i++)
        {
            string bizStep = filter[i];
            if (!Uri.TryCreate(bizStep, UriKind.Absolute, out Uri? u))
            {
                filter[i] = replacePrefix + bizStep;
            }
            else if (bizStep.StartsWith(prefix))
            {
                filter[i] = replacePrefix + bizStep.Split('-').Last();
            }
        }

        // we need to handle the various formats that the bizStep can occur in
        if (uri != null)
        {
            Uri bizStep = new Uri(uri.ToString());
            if (bizStep.ToString().StartsWith(prefix))
            {
                bizStep = new Uri(replacePrefix + uri.ToString().Split('-').Last());
            }

            List<Uri> filter_uris = filter.Select(x => new Uri(x)).ToList();
            if (!filter_uris.Select(u => u.ToString().ToLower()).Contains(bizStep.ToString().ToLower()))
            {
                return false;
            }
        }
        else
        {
            return false;
        }

        return true;
    }
}

