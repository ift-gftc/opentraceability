using System;
using Newtonsoft.Json;
using OpenTraceability.Interfaces;
using OpenTraceability.Mappers;
using OpenTraceability.Models.Events;
using OpenTraceability.Models.Identifiers;
using OpenTraceability.Models.MasterData;
using OpenTraceability.Utility;

namespace OpenTraceability.Queries
{
    /// <summary>
    /// This is a static class can be used to help resolve traceability data from
    /// an EPCIS Query Interface.
    /// </summary>
    public static class EPCISTraceabilityResolver
    {
        /// <summary>
        /// Tries to get the EPCIS Query Interface URL from the Digital Link Resolver for the given EPC.
        /// </summary>
        /// <param name="options">Options for querying a digital link resolver.</param>
        /// <param name="epc">The EPC to include in the relative URL.</param>
        /// <returns>The URI if it finds one, otherwise returns NULL.</returns>
        public static async Task<Uri?> GetEPCISQueryInterfaceURL(DigitalLinkQueryOptions options, EPC epc, HttpClient client)
        {
            if (options.URL == null)
            {
                throw new Exception("options.Uri is null on the DigitalLinkQueryOptions");
            }

            string? relativeUrl = null;
            switch (epc.Type)
            {
                case EPCType.Class: relativeUrl = epc.GTIN?.ToDigitalLinkURL() + "/10/" + epc.SerialLotNumber; break;
                case EPCType.Instance: relativeUrl = epc.GTIN?.ToDigitalLinkURL() + "/21/" + epc.SerialLotNumber; break;
                case EPCType.SSCC: relativeUrl = "00/" + epc.ToString(); break;
                default: throw new Exception($"Cannot build Digital Link URL with EPC {epc}. We need either GTIN+LOT, GTIN+SERIAL, or SSCC.");
            }

            relativeUrl += "?linkType=gs1:epcis";

            HttpRequestMessage request = new HttpRequestMessage();
            request.RequestUri = new Uri(options.URL + relativeUrl);
            request.Method = HttpMethod.Get;

            var response = await client.SendAsync(request);
            if (response.IsSuccessStatusCode)
            {
                string json = await response.Content.ReadAsStringAsync();
                var link = JsonConvert.DeserializeObject<List<DigitalLink>>(json)?.FirstOrDefault();
                if (link != null)
                {
                    return new Uri(link.link.TrimEnd('/') + '/');
                }
            }

            return null;
        }

        /// <summary>
        /// Tries to get the EPCIS Query Interface URL from the Digital Link Resolver for the given PGLN.
        /// </summary>
        /// <param name="options">Options for querying a digital link resolver.</param>
        /// <param name="pgln">The PGLN to include in the relative URL.</param>
        /// <returns>The URI if it finds one, otherwise returns NULL.</returns>
        public static async Task<Uri?> GetEPCISQueryInterfaceURL(DigitalLinkQueryOptions options, PGLN pgln, HttpClient client)
        {
            if (options.URL == null)
            {
                throw new Exception("options.Uri is null on the DigitalLinkQueryOptions");
            }

            string relativeUrl = pgln.ToDigitalLinkURL();

            relativeUrl += "?linkType=gs1:epcis";

            HttpRequestMessage request = new HttpRequestMessage();
            request.RequestUri = new Uri(options.URL + relativeUrl);
            request.Method = HttpMethod.Get;

            var response = await client.SendAsync(request);
            if (response.IsSuccessStatusCode)
            {
                string json = await response.Content.ReadAsStringAsync();
                var link = JsonConvert.DeserializeObject<List<DigitalLink>>(json)?.FirstOrDefault();
                if (link != null)
                {
                    return new Uri(link.link.TrimEnd('/') + '/');
                }
            }

            return null;
        }

        /// <summary>
        /// Performs a trace-back on a single EPC. This will go up to 100 products back.
        /// </summary>
        /// <param name="options">Options for talking to the EPCIS Query Interface.</param>
        /// <param name="epc">The EPC to perform the traceback on.</param>
        /// <returns>The summarized EPCIS query results.</returns>
        public static async Task<EPCISQueryResults> Traceback(EPCISQueryInterfaceOptions options, EPC epc, HttpClient client, EPCISQueryParameters? additionalParameters = null)
        {
            HashSet<EPC> queried_epcs = new HashSet<EPC>() { epc };

            // query for all events pertaining to the EPC
            var paramters = new EPCISQueryParameters(epc);
            if (additionalParameters != null)
            {
                paramters.Merge(additionalParameters);
            }

            var results = await QueryEvents(options, paramters, client);

            // if an error occured, lets stop here and return the results that we have
            if (results.Errors.Count > 0)
            {
                return results;
            }

            if (results.Document == null)
            {
                throw new NullReferenceException($"The results.Document is NULL, and this should not happen.");
            }

            // find all epcs we have not queried for and query for events pertaining to them
            List<EPC> epcs_to_query = new List<EPC>();
            var potential_epcs = results.Document.Events.SelectMany(e => e.Products)
                                                        .Where(p => p.Type == EventProductType.Child || p.Type == EventProductType.Input)
                                                        .Select(p => p.EPC)
                                                        .Distinct();
            foreach (var e in potential_epcs)
            {
                if (!queried_epcs.Contains(e))
                {
                    epcs_to_query.Add(e);
                    queried_epcs.Add(e);
                }
            }

            // repeat until we have no more unknown inputs / children
            for (int stack = 0; stack < 100; stack++)
            {
                if (epcs_to_query.Count > 0)
                {
                    var p = new EPCISQueryParameters(epcs_to_query.ToArray());
                    if (additionalParameters != null)
                    {
                        p.Merge(additionalParameters);
                    }
                    var r = await QueryEvents(options, p, client);

                    results.Merge(r);

                    if (r.Document != null)
                    {
                        epcs_to_query = new List<EPC>();
                        potential_epcs = r.Document.Events.SelectMany(e => e.Products)
                                                          .Where(p => p.Type == EventProductType.Child || p.Type == EventProductType.Input)
                                                          .Select(p => p.EPC)
                                                          .Distinct();

                        foreach (var e in potential_epcs)
                        {
                            if (!queried_epcs.Contains(e))
                            {
                                epcs_to_query.Add(e);
                                queried_epcs.Add(e);
                            }
                        }
                    }
                    else
                    {
                        break;
                    }
                }
                else
                {
                    break;
                }
            }

            // fill in missing events that occurred to aggregated containers
            // find each time an EPC was aggregated into an SSCC
            // find the next event the EPC was recorded in an event after being aggregated
            // search for events that occurred to the SSCC, and add them to the results
            // repeat until we find nothing...
            for (int stack = 0; stack < 100; stack++)
            {
                // find aggregate events where we have not queried for the parent ID
                var agg_events = results.Document.Events.Where(e => e is IAggregationEvent && e.Action == EventAction.ADD && !queried_epcs.Contains(((IAggregationEvent)e).ParentID)).ToList();

                if (agg_events.Count() > 0)
                {
                    foreach (var agg_evt in agg_events)
                    {
                        // find the next event recorded where one of the children is recorded in the event
                        var parent_id = ((IAggregationEvent)agg_evt).ParentID;
                        var child_epcs = agg_evt.Products.Where(p => p.Type == EventProductType.Child).Select(p => p.EPC);
                        var next_evt = results.Document.Events.Where(e => e.EventTime > agg_evt.EventTime && e.Products.Any(p => child_epcs.Contains(p.EPC))).OrderBy(e => e.EventTime).FirstOrDefault();
                        DateTimeOffset? next_evt_time = next_evt?.EventTime;

                        // if we couldn't find a next event time, take the max event time of all events
                        if (next_evt_time == null)
                        {
                            next_evt_time = results.Document.Events.Max(e => e.EventTime);
                        }

                        // query for events that occurred to the parent ID
                        var p = new EPCISQueryParameters(parent_id);
                        p.query.LE_eventTime = next_evt_time;
                        if (additionalParameters != null)
                        {
                            p.Merge(additionalParameters);
                        }
                        var r = await QueryEvents(options, p, client);
                        results.Merge(r);
                        queried_epcs.Add(parent_id);
                    }
                }
                else
                {
                    break;
                }
            }

            return results;
        }

        /// <summary>
        /// Performs a single request against the EPCIS Query Interface using the options
        /// provided.
        /// </summary>
        /// <param name="options">The options that power the request.</param>
        /// <returns>EPCIS Query Results</returns>
		public static async Task<EPCISQueryResults> QueryEvents(EPCISQueryInterfaceOptions options, EPCISQueryParameters parameters, HttpClient client)
        {
            // determine the mapper for deserialize the contents
            IEPCISQueryDocumentMapper mapper = OpenTraceabilityMappers.EPCISQueryDocument.JSON;
            if (options.Format == EPCISDataFormat.XML)
            {
                mapper = OpenTraceabilityMappers.EPCISQueryDocument.XML;
            }

            // build the HTTP request
            HttpRequestMessage request = new HttpRequestMessage();
            request.RequestUri = new Uri(options.URL?.ToString().TrimEnd('/') + "/events" + parameters.ToQueryParameters());
            request.Method = HttpMethod.Get;

            if (options.Version == EPCISVersion.V1) 
            {
                request.Headers.Add("Accept", "application/xml");
                request.Headers.Add("GS1-EPCIS-Version", "1.2");
                request.Headers.Add("GS1-EPCIS-Min", "1.2");
                request.Headers.Add("GS1-EPCIS-Max", "1.2");
                request.Headers.Add("GS1-CBV-Version", "1.2");
                request.Headers.Add("GS1-CBV-XML-Format", "ALWAYS_URN");
            }
            else if (options.Version == EPCISVersion.V2)
            {
                if (options.Format == EPCISDataFormat.XML)
                {
                    request.Headers.Add("Accept", "application/xml");
                }
                else
                {
                    request.Headers.Add("Accept", "application/json");
                }
                request.Headers.Add("GS1-EPCIS-Version", "2.0");
                request.Headers.Add("GS1-EPCIS-Min", "2.0");
                request.Headers.Add("GS1-EPCIS-Max", "2.0");
                request.Headers.Add("GS1-CBV-Version", "2.0");
                request.Headers.Add("GS1-CBV-XML-Format", "ALWAYS_URN");
            }
            else
            {
                throw new Exception($"Unrecognized EPCISVersion {options.Version} on the options.");
            }

            EPCISQueryResults results = new EPCISQueryResults();

            // execute the request
            HttpResponseMessage? response = null;
            string? responseBody = null;
            try
            {
                response = await client.SendAsync(request);
                responseBody = await response.Content.ReadAsStringAsync();
                if (response.IsSuccessStatusCode)
                {
                    try
                    {
                        var doc = mapper.Map(responseBody);
                        results.Document = doc;
                    }
                    catch (OpenTraceabilitySchemaException schemaEx)
                    {
                        results.Errors.Add(new EPCISQueryError()
                        {
                            Type = EPCISQueryErrorType.Schema,
                            Details = schemaEx.Message
                        });
                    }
                }
                else
                {
                    // if it fails, record the error
                    results.Errors.Add(new EPCISQueryError()
                    {
                        Type = EPCISQueryErrorType.HTTP,
                        Details = $"{(int)response.StatusCode} - {response.StatusCode} - {responseBody}"
                    });
                }
            }
            catch (Exception ex)
            {
                results.Errors.Add(new EPCISQueryError()
                {
                    Type = EPCISQueryErrorType.Exception,
                    Details = ex.Message
                });
            }

            // if stack trace is enabled, record the stack trace item
            if (options.EnableStackTrace)
            {
                EPCISQueryStackTraceItem item = new EPCISQueryStackTraceItem()
                {
                    RelativeURL = request.RequestUri,
                    RequestHeaders = request.Headers.ToList(),
                    ResponseStatusCode = response?.StatusCode,
                    ResponseBody = responseBody,
                    ResponseHeaders = response?.Headers.ToList()
                };

                results.StackTrace.Add(item);

                foreach (var e in results.Errors)
                {
                    e.StackTraceItemID = item.ID;
                }
            }

            return results;
        }
    }
}