﻿using Microsoft.AspNetCore.Http.Extensions;
using Microsoft.AspNetCore.Mvc;
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

[Route("digitallink")]
public class DigitalLinkController : ControllerBase
{
    [HttpGet]
    [Route("{blob_id}/417/{pgln}")]
    [Route("{blob_id}/pgln/{pgln}")]
    public Task<IActionResult> GetPartyLinks(string blob_id, string pgln, [FromQuery] string linkType)
    {
        try
        {
            string baseURL = Request.GetDisplayUrl().Split("/digitallink/").First();

            List<DigitalLink> links = new List<DigitalLink>();

            if (linkType == null || linkType.ToLower() == "gs1:epcis")
            {
                links.Add(new DigitalLink()
                {
                    link = baseURL + $"/epcis/{blob_id}",
                    linkType = "gs1:epcis",
                    authRequired = true
                });
            }

            if (linkType == null || linkType.ToLower() == "gs1:masterdata")
            {
                links.Add(new DigitalLink()
                {
                    link = baseURL + $"/masterdata/{blob_id}/{pgln}",
                    linkType = "gs1:epcis",
                    authRequired = true
                });
            }

            return Task.FromResult<IActionResult>(Ok(links));
        }
        catch (Exception ex)
        {
            Console.WriteLine(ex);
            throw;
        }
    }

    [HttpGet]
    [Route("{blob_id}/414/{gln}")]
    [Route("{blob_id}/gln/{gln}")]
    public Task<IActionResult> GetLocationLinks(string blob_id, string gln, [FromQuery] string linkType)
    {
        try
        {
            string baseURL = Request.GetDisplayUrl().Split("/digitallink/").First();

            List<DigitalLink> links = new List<DigitalLink>();

            if (linkType == null || linkType.ToLower() == "gs1:epcis")
            {
                links.Add(new DigitalLink()
                {
                    link = baseURL + $"/epcis/{blob_id}",
                    linkType = "gs1:epcis",
                    authRequired = true
                });
            }

            if (linkType == null || linkType.ToLower() == "gs1:masterdata")
            {
                links.Add(new DigitalLink()
                {
                    link = baseURL + $"/masterdata/{blob_id}/{gln}",
                    linkType = "gs1:epcis",
                    authRequired = true
                });
            }

            return Task.FromResult<IActionResult>(Ok(links));
        }
        catch (Exception ex)
        {
            Console.WriteLine(ex);
            throw;
        }
    }

    [HttpGet]
    [Route("{blob_id}/01/{gtin}")]
    [Route("{blob_id}/gtin/{gtin}")]
    public Task<IActionResult> GetTradeitemLinks(string blob_id, string gtin, [FromQuery] string linkType)
    {
        try
        {
            string baseURL = Request.GetDisplayUrl().Split("/digitallink/").First();

            List<DigitalLink> links = new List<DigitalLink>();

            if (linkType == null || linkType.ToLower() == "gs1:epcis")
            {
                links.Add(new DigitalLink()
                {
                    link = baseURL + $"/epcis/{blob_id}",
                    linkType = "gs1:epcis",
                    authRequired = true
                });
            }

            if (linkType == null || linkType.ToLower() == "gs1:masterdata")
            {
                links.Add(new DigitalLink()
                {
                    link = baseURL + $"/masterdata/{blob_id}/{gtin}",
                    linkType = "gs1:epcis",
                    authRequired = true
                });
            }

            return Task.FromResult<IActionResult>(Ok(links));
        }
        catch (Exception ex)
        {
            Console.WriteLine(ex);
            throw;
        }
    }

    [HttpGet]
    [Route("{blob_id}/00/{sscc}")]
    [Route("{blob_id}/sscc/{sscc}")]
    public Task<IActionResult> GetSSCCLinks(string blob_id, string sscc, [FromQuery] string linkType)
    {
        try
        {
            string baseURL = Request.GetDisplayUrl().Split("/digitallink/").First();

            List<DigitalLink> links = new List<DigitalLink>();

            if (linkType == null || linkType.ToLower() == "gs1:epcis")
            {
                links.Add(new DigitalLink()
                {
                    link = baseURL + $"/epcis/{blob_id}",
                    linkType = "gs1:epcis",
                    authRequired = true
                });
            }

            return Task.FromResult<IActionResult>(Ok(links));
        }
        catch (Exception ex)
        {
            Console.WriteLine(ex);
            throw;
        }
    }

    [HttpGet]
    [Route("{blob_id}/01/{gtin}/10/{lot}")]
    [Route("{blob_id}/gtin/{gtin}/lot/{lot}")]
    public Task<IActionResult> GetEPCClassLinks(string blob_id, string gtin, string lot, [FromQuery] string linkType)
    {
        try
        {
            string baseURL = Request.GetDisplayUrl().Split("/digitallink/").First();

            List<DigitalLink> links = new List<DigitalLink>();

            if (linkType == null || linkType.ToLower() == "gs1:epcis")
            {
                links.Add(new DigitalLink()
                {
                    link = baseURL + $"/epcis/{blob_id}",
                    linkType = "gs1:epcis",
                    authRequired = true
                });
            }

            if (linkType == null || linkType.ToLower() == "gs1:masterdata")
            {
                links.Add(new DigitalLink()
                {
                    link = baseURL + $"/masterdata/{blob_id}/{gtin}",
                    linkType = "gs1:epcis",
                    authRequired = true
                });
            }

            return Task.FromResult<IActionResult>(Ok(links));
        }
        catch (Exception ex)
        {
            Console.WriteLine(ex);
            throw;
        }
    }

    [HttpGet]
    [Route("{blob_id}/01/{gtin}/21/{serial}")]
    [Route("{blob_id}/gtin/{gtin}/serial/{serial}")]
    public Task<IActionResult> GetEPCInstanceLinks(string blob_id, string gtin, string serial, [FromQuery] string linkType)
    {
        try
        {
            string baseURL = Request.GetDisplayUrl().Split("/digitallink/").First();

            List<DigitalLink> links = new List<DigitalLink>();

            if (linkType == null || linkType.ToLower() == "gs1:epcis")
            {
                links.Add(new DigitalLink()
                {
                    link = baseURL + $"/epcis/{blob_id}",
                    linkType = "gs1:epcis",
                    authRequired = true
                });
            }

            if (linkType == null || linkType.ToLower() == "gs1:masterdata")
            {
                links.Add(new DigitalLink()
                {
                    link = baseURL + $"/masterdata/{blob_id}/{gtin}",
                    linkType = "gs1:epcis",
                    authRequired = true
                });
            }

            return Task.FromResult<IActionResult>(Ok(links));
        }
        catch (Exception ex)
        {
            Console.WriteLine(ex);
            throw;
        }
    }
}