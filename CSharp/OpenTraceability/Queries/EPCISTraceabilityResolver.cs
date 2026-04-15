using System;
using System.Collections.Generic;
using System.Linq;
using System.Net.Http;
using System.Net.Http.Headers;
using System.Threading.Tasks;
using Newtonsoft.Json;
using OpenTraceability.Interfaces;
using OpenTraceability.Mappers;
using OpenTraceability.Models.Events;
using OpenTraceability.Models.Identifiers;
using OpenTraceability.Models.MasterData;
using OpenTraceability.Queries.Diagnostics;
using OpenTraceability.Queries.Diagnostics.Rules;
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
        /// <param name="client">The HTTP client to use to make the request.</param>
        /// <param name="report">If this is not NULL, it will be used to track diagnostics information that can be used to debug the request.</param>
        /// <returns>The URI if it finds one, otherwise returns NULL.</returns>
        public static async Task<Uri?> GetEPCISQueryInterfaceURL(DigitalLinkQueryOptions options, EPC epc, HttpClient client, DiagnosticsReport? report = null)
        {
            // DIAGNOSTICS: Create a new request.
            report?.NewRequest("Requesting EPCIS Query Interface URL (w/ EPC)", options);

            if (options.URL == null)
            {
                throw new Exception("options.Uri is null on the DigitalLinkQueryOptions");
            }

            string? relativeUrl = epc.ToDigitalLinkURI();

            relativeUrl += "?linkType=gs1:epcis";
            string fullURL = string.Join("/", [options.URL.ToString().TrimEnd('/'), relativeUrl.TrimStart('/')]);

            HttpRequestMessage request = new HttpRequestMessage();
            request.RequestUri = new Uri(fullURL);
            request.Method = HttpMethod.Get;

            // Don't manually set the host, as this can cause issues with redirects
            //// calculate the host field for the request
            //string host = options.URL.Host;
            //request.Headers.Host = host;

            // set accept to "application/json"
            request.Headers.Accept.Add(new MediaTypeWithQualityHeaderValue("application/json"));

            // DIAGNOSTICS: Execute the rule to validate the Http Headers.   
            if (report != null)
            {
                report.CurrentRequest.HttpRequest = request;
                await report.CurrentRequest.ExecuteRuleAsync<DigitalLinkHttpRequestRule>(request.Headers);
            }

            // Send the request.
            var response = await client.SendAsync(request);

            // DIAGNOSTICS: Execute the rule to validate the Http Headers.   
            if (report != null)
            {
                report.CurrentRequest.HttpResponse = response;
                report.CurrentRequest.End = DateTime.UtcNow;
                await report.CurrentRequest.ExecuteRuleAsync<DigitalLinkHttpResponseRule>(response);
            }

            if (response.IsSuccessStatusCode)
            {
                string json = await response.Content.ReadAsStringAsync();

                // DIAGNOSTICS: Execute the rule to validate the JSON.
                if (report != null)
                {
                    report.CurrentRequest.ResponseBody = await response.Content.ReadAsStringAsync();
                    await report.CurrentRequest.ExecuteRuleAsync<DigitalLinkJsonSchemaRule>(json);
                }

                // DIAGNOSTICS: Execute the rule to validate a response was found.
                if (report != null)
                {
                    await report.CurrentRequest.ExecuteRuleAsync<DigitalLinkResponseFoundRule>(json);
                }

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
        /// <param name="client">The HTTP client to use to make the request.</param>
        /// <param name="report">If this is not NULL, it will be used to track diagnostics information that can be used to debug the request.</param>
        /// <returns>The URI if it finds one, otherwise returns NULL.</returns>
        public static async Task<Uri?> GetEPCISQueryInterfaceURL(DigitalLinkQueryOptions options, PGLN pgln, HttpClient client, DiagnosticsReport? report = null)
        {
            // DIAGNOSTICS: Create a new request.
            report?.NewRequest("Request EPCIS Query Interface URL (w/ PGLN)", options);

            if (options.URL == null)
            {
                throw new Exception("options.Uri is null on the DigitalLinkQueryOptions");
            }

            string relativeUrl = pgln.ToDigitalLinkURL();

            relativeUrl += "?linkType=gs1:epcis";

            HttpRequestMessage request = new HttpRequestMessage();
            request.RequestUri = new Uri(options.URL + relativeUrl);
            request.Method = HttpMethod.Get;

            // Do not manually set the host, as this can cause issues with redirects
            //// calculate the host field for the request
            //string host = options.URL.Host;
            //request.Headers.Host = host;

            // set accept to "application/json"
            request.Headers.Accept.Add(new MediaTypeWithQualityHeaderValue("application/json"));

            // DIAGNOSTICS: Execute the rule to validate the Http Headers.   
            if (report?.CurrentRequest != null)
            {
                report.CurrentRequest.HttpRequest = request;
                await report.CurrentRequest.ExecuteRuleAsync<DigitalLinkHttpRequestRule>(request.Headers);
            }

            var response = await client.SendAsync(request);

            // DIAGNOSTICS: Execute the rule to validate the Http Headers.   
            if (report?.CurrentRequest != null)
            {
                report.CurrentRequest.HttpResponse = response;
                report.CurrentRequest.End = DateTime.UtcNow;
                await report.CurrentRequest.ExecuteRuleAsync<DigitalLinkHttpResponseRule>(response);
            }

            if (response.IsSuccessStatusCode)
            {
                string json = await response.Content.ReadAsStringAsync();

                // DIAGNOSTICS: Execute the rule to validate the JSON.
                if (report?.CurrentRequest != null)
                {
                    report.CurrentRequest.ResponseBody = await response.Content.ReadAsStringAsync();
                    await report.CurrentRequest.ExecuteRuleAsync<DigitalLinkJsonSchemaRule>(json);
                }

                // DIAGNOSTICS: Execute the rule to validate a response was found.
                if (report?.CurrentRequest != null)
                {
                    await report.CurrentRequest.ExecuteRuleAsync<DigitalLinkResponseFoundRule>(json);
                }

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
        /// <param name="report">If this is not NULL, it will be used to track diagnostics information that can be used to debug the request.</param>
        /// <returns>The URI if it finds one, otherwise returns NULL.</returns>
        /// <returns>The summarized EPCIS query results.</returns>
        public static async Task<EPCISQueryResults> Traceback(EPCISQueryInterfaceOptions options, EPC epc, HttpClient client, EPCISQueryParameters? additionalParameters = null, DiagnosticsReport? report = null)
        {
            HashSet<EPC> queried_epcs = new HashSet<EPC>() { epc };

            // query for all events pertaining to the EPC
            var paramters = new EPCISQueryParameters(epc);
            if (additionalParameters != null)
            {
                paramters.Merge(additionalParameters);
            }

            var results = await QueryEvents(options, paramters, client, report);

            // if an error occured, lets stop here and return the results that we have
            if (results.Errors.Count() > 0)
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
                    var r = await QueryEvents(options, p, client, report);

                    results.Merge(r);

                    if (r.Document != null)
                    {
                        epcs_to_query = new List<EPC>();
                        potential_epcs = r.Document.Events.SelectMany(e => e.Products)
                                                          .Where(product => product.Type == EventProductType.Child || product.Type == EventProductType.Input)
                                                          .Select(product => product.EPC)
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
                        var child_epcs = agg_evt.Products.Where(product => product.Type == EventProductType.Child).Select(product => product.EPC);
                        var next_evt = results.Document.Events.Where(e => e.EventTime > agg_evt.EventTime && e.Products.Any(product => child_epcs.Contains(product.EPC))).OrderBy(e => e.EventTime).FirstOrDefault();
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
                        var r = await QueryEvents(options, p, client, report);
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
        /// <param name="report">If this is not NULL, it will be used to track diagnostics information that can be used to debug the request.</param>
        /// <param name="enforceSchema">Whether to enforce strict schema validation. If false, parsing errors will be logged but not block processing.</param>
        /// <returns>EPCIS Query Results</returns>
		public static async Task<EPCISQueryResults> QueryEvents(EPCISQueryInterfaceOptions options, EPCISQueryParameters parameters, HttpClient client, DiagnosticsReport? report = null)
        {
            // DIAGNOSTICS: Create a new request.
            report?.NewRequest("Query EPCIS Events", options);
                                             
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

            //// ensure Host header is set BEFORE diagnostics rule executes so the rule does not report a missing host
            //if (request.RequestUri != null)
            //{
            //    request.Headers.Host = request.RequestUri.Host;
            //}

            // DIAGNOSTICS: Execute the rule to validate the HTTP request headers.
            if (report != null)
            {
                report.CurrentRequest.HttpRequest = request;
                await report.CurrentRequest.ExecuteRuleAsync<EPCISHttpRequestRule>(request.Headers, options.Version, options.Format);
            }

            EPCISQueryResults results = new EPCISQueryResults();

            // execute the request
            HttpResponseMessage? response = null;
            string? responseBody = null;
            try
            {
                // Don't manually set the host, as this can cause issues with redirects
                //// calculate the host field for the request
                //string host = request.RequestUri?.Host ?? "localhost";
                //request.Headers.Host = host;

                response = await client.SendAsync(request);
                responseBody = await response.Content.ReadAsStringAsync();

                // DIAGNOSTICS: Execute the rule to validate the HTTP response.
                if (report != null)
                {
                    report.CurrentRequest.HttpResponse = response;
                    report.CurrentRequest.End = DateTime.UtcNow;
                    await report.CurrentRequest.ExecuteRuleAsync<EPCISHttpResponseRule>(response);
                }

                if (response.IsSuccessStatusCode)
                {
                    // DIAGNOSTICS: Execute the rule to validate the response schema.
                    if (report != null && responseBody != null)
                    {
                        report.CurrentRequest.ResponseBody = responseBody;
                        await report.CurrentRequest.ExecuteRuleAsync<EPCISResponseSchemaRule>(responseBody, options.Format, options.Version);
                    }

                    var doc = mapper.Map(responseBody ?? string.Empty, false);
                    results.Document = doc;

                    // DIAGNOSTICS: Execute the rule to validate for duplicate event IDs.
                    if (report != null && doc != null)
                    {
                        await report.CurrentRequest.ExecuteRuleAsync<EPCISDuplicateEventIDsRule>(doc);
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
                if (report != null)
                {
                    report.CurrentRequest.AddException(ex);
                }

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
                    RelativeURL = request.RequestUri!,
                    RequestHeaders = request.Headers.ToList(),
                    ResponseStatusCode = response?.StatusCode,
                    ResponseBody = responseBody ?? string.Empty,
                    ResponseHeaders = response?.Headers?.ToList() ?? new List<System.Collections.Generic.KeyValuePair<string, System.Collections.Generic.IEnumerable<string>>>()
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