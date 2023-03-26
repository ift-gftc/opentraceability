using System;
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
        /// Performs a trace-back on a single EPC. This will go up to 100 products back.
        /// </summary>
        /// <param name="options">Options for talking to the EPCIS Query Interface.</param>
        /// <param name="epc">The EPC to perform the traceback on.</param>
        /// <returns>The summarized EPCIS query results.</returns>
        public static async Task<EPCISQueryResults> Traceback(EPCISQueryInterfaceOptions options, EPC epc, EPCISQueryParameters? additionalParameters = null)
        {
            HashSet<EPC> queried_epcs = new HashSet<EPC>() { epc };

            // query for all events pertaining to the EPC
            var paramters = new EPCISQueryParameters(epc);
            if (additionalParameters != null)
            {
                paramters.Merge(additionalParameters);
            }


            var results = await QueryEvents(options, paramters);

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
                    var r = await QueryEvents(options, p);

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

            return results;
        }

        /// <summary>
        /// Performs a single request against the EPCIS Query Interface using the options
        /// provided.
        /// </summary>
        /// <param name="options">The options that power the request.</param>
        /// <returns>EPCIS Query Results</returns>
		public static async Task<EPCISQueryResults> QueryEvents(EPCISQueryInterfaceOptions options, EPCISQueryParameters parameters)
        {
            // determine the mapper for deserialize the contents
            IEPCISQueryDocumentMapper mapper = OpenTraceabilityMappers.EPCISQueryDocument.JSON;
            if (options.Format == EPCISDataFormat.XML)
            {
                mapper = OpenTraceabilityMappers.EPCISQueryDocument.XML;

                if (options.Version != EPCISVersion.V1)
                {
                    throw new Exception("The data format is set to XML, but the EPCIS version is not set to 1.2");
                }
            }

            // get the http client from a pool
            using (var clientItem = HttpClientPool.GetClient())
            {
                HttpClient client = clientItem.Value;

                // build the HTTP request
                HttpRequestMessage request = new HttpRequestMessage();
                request.RequestUri = new Uri(options.URL + "/events" + parameters.ToQueryParameters());
                request.Method = HttpMethod.Get;

                if (!string.IsNullOrWhiteSpace(options.XAPIKey))
                {
                    request.Headers.Add("X-API-Key", options.XAPIKey);
                }

                if (!string.IsNullOrWhiteSpace(options.BearerToken))
                {
                    request.Headers.Add("Authorization", "Bearer " + options.BearerToken);
                }

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
}

