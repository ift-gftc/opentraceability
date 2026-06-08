using System;
using System.Net.Http;
using System.Net.Http.Headers;
using System.Threading.Tasks;
using OpenTraceability.Interfaces;
using OpenTraceability.Mappers;
using OpenTraceability.Models.Events;
using OpenTraceability.Models.Identifiers;
using OpenTraceability.Utility;

namespace OpenTraceability.Queries
{
    /// <summary>
    /// A client for talking to an Open Traceability Test Server for
    /// posting and querying traceability data.
    /// </summary>
    public class EPCISTestServerClient
    {
        protected string _baseURL;
        protected EPCISDataFormat _format;
        protected EPCISVersion _version;
        protected string _apiKey;
        protected string _dataset;

        /// <summary>
        /// Creates a new client. Each client instance is bound to a single dataset on the server,
        /// identified by the X-Dataset-Id header. When no dataset id is provided a unique one is
        /// generated so that callers get an isolated dataset by default.
        /// </summary>
        public EPCISTestServerClient(string baseURL, string apiKey, EPCISDataFormat format, EPCISVersion version, string? datasetID = null)
        {
            _baseURL = baseURL;
            _version = version;
            _format = format;
            _apiKey = apiKey;
            _dataset = string.IsNullOrEmpty(datasetID) ? Guid.NewGuid().ToString() : datasetID!;
        }

        /// <summary>
        /// Posts an EPCIS Document to the Test Server under this client's dataset.
        /// </summary>
        /// <param name="doc">The EPCIS document to post.</param>
        /// <returns>The dataset id the data was posted under. Use this when querying for events after.</returns>
        public async Task<string> Post(EPCISDocument doc)
        {
            string url = $"{_baseURL.TrimEnd('/')}/epcis/events";

            IEPCISDocumentMapper mapper = OpenTraceabilityMappers.EPCISDocument.XML;
            string contentType = "application/xml";
            if (_format == EPCISDataFormat.JSON)
            {
                contentType = "application/json";
                mapper = OpenTraceabilityMappers.EPCISDocument.JSON;
            }

            using (var clientItem = HttpClientPool.GetClient())
            {
                var client = clientItem.Value;

                HttpRequestMessage request = new HttpRequestMessage();
                request.RequestUri = new Uri(url);
                request.Headers.Add("X-API-Key", _apiKey);
                request.Headers.Add("X-Dataset-Id", _dataset);

                if (_version == EPCISVersion.V1)
                {
                    request.Headers.Add("Accept", "application/xml");
                    request.Headers.Add("GS1-EPCIS-Version", "1.2");
                    request.Headers.Add("GS1-EPCIS-Min", "1.2");
                    request.Headers.Add("GS1-EPCIS-Max", "1.2");
                    request.Headers.Add("GS1-CBV-Version", "1.2");
                    request.Headers.Add("GS1-CBV-XML-Format", "ALWAYS_URN");
                }
                else if (_version == EPCISVersion.V2)
                {
                    if (_format == EPCISDataFormat.XML)
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
                    throw new Exception($"Unrecognized EPCISVersion {_version} on the options.");
                }


                StringContent content = new StringContent(mapper.Map(doc), System.Text.Encoding.UTF8, contentType);
                request.Content = content;
                request.Method = HttpMethod.Post;

                var response = await client.SendAsync(request);
                if (!response.IsSuccessStatusCode)
                {
                    string contentStr = await response.Content.ReadAsStringAsync();
                    throw new Exception($"{(int)response.StatusCode} - {response.StatusCode} - {contentStr}");
                }

                return _dataset;
            }
        }

        /// <summary>
        /// Queries this client's dataset for events that match the parameters.
        /// </summary>
        /// <param name="parameters">The EPCIS Query parameters.</param>
        /// <returns>The EPCIS Query results.</returns>
        public async Task<EPCISQueryResults> QueryEvents(EPCISQueryParameters parameters)
        {
            using (var clientItem = HttpClientPool.GetClient())
            {
                var client = clientItem.Value;
                EPCISQueryInterfaceOptions options = BuildQueryOptions();
                return await EPCISTraceabilityResolver.QueryEvents(options, parameters, client);
            }
        }

        /// <summary>
        /// Queries and performs a traceback against this client's dataset given the EPC.
        /// </summary>
        /// <param name="epc">The EPC to perform the traceback on.</param>
        /// <returns>The epcis query results.</returns>
        public async Task<EPCISQueryResults> Traceback(EPC epc)
        {
            using (var clientItem = HttpClientPool.GetClient())
            {
                var client = clientItem.Value;
                EPCISQueryInterfaceOptions options = BuildQueryOptions();
                return await EPCISTraceabilityResolver.Traceback(options, epc, client);
            }
        }

        /// <summary>
        /// Resolves all the unknown master data in the EPCIS document.
        /// </summary>
        /// <param name="doc">The EPCIS document to resolve the master data for.</param>
        public async Task ResolveMasterData(EPCISBaseDocument doc)
        {
            using (var clientItem = HttpClientPool.GetClient())
            {
                var client = clientItem.Value;
                DigitalLinkQueryOptions options = BuildDigitalLinkOptions();
                await MasterDataResolver.ResolveMasterData(options, doc, client);
            }
        }

        /// <summary>
        /// Builds the EPCIS query interface options for this client's dataset, including the
        /// API key and X-Dataset-Id header.
        /// </summary>
        protected EPCISQueryInterfaceOptions BuildQueryOptions()
        {
            return new EPCISQueryInterfaceOptions()
            {
                URL = new Uri($"{_baseURL.TrimEnd('/')}/epcis"),
                Format = _format,
                Version = _version,
                APIKey = _apiKey,
                Headers = { ["X-Dataset-Id"] = _dataset },
                EnableStackTrace = true
            };
        }

        /// <summary>
        /// Builds the digital link query options for this client's dataset, including the
        /// API key and X-Dataset-Id header.
        /// </summary>
        protected DigitalLinkQueryOptions BuildDigitalLinkOptions()
        {
            return new DigitalLinkQueryOptions()
            {
                URL = new Uri($"{_baseURL.TrimEnd('/')}/digitallink"),
                APIKey = _apiKey,
                Headers = { ["X-Dataset-Id"] = _dataset },
                EnableStackTrace = true
            };
        }
    }
}
