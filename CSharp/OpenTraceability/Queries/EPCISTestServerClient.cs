using System;
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
		string _baseURL;
		EPCISDataFormat _format;
		EPCISVersion _version;

		public EPCISTestServerClient(string baseURL, EPCISDataFormat format, EPCISVersion version)
		{
            _baseURL = baseURL;
			_version = version;
			_format = format;
		}

		/// <summary>
		/// Posts an EPCIS Document to the Test Server and returns
		/// a blob ID. You will need this blob ID when querying for events
		/// after.
		/// </summary>
		/// <param name="doc"></param>
		/// <returns>The blob ID of the uploaded traceability data.</returns>
		public async Task<string> Post(EPCISDocument doc, string? blob_id = null)
		{
			if (blob_id == null)
            {
                blob_id = Guid.NewGuid().ToString();
            }

            string url = $"{_baseURL.TrimEnd('/')}/epcis/{blob_id}/events";

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

                // calculate the host field for the request
                string host = new Uri(url).Host;
                request.Headers.Host = host;

                var response = await client.SendAsync(request);
                if (!response.IsSuccessStatusCode)
                {
                    string contentStr = await response.Content.ReadAsStringAsync();
                    throw new Exception($"{(int)response.StatusCode} - {response.StatusCode} - {contentStr}");
                }

                return blob_id;
            } 
        }

		/// <summary>
		/// Queries the test server blob for events that match the parameters.
		/// </summary>
		/// <param name="blob_id">The ID of the blob to query.</param>
		/// <param name="parameters">The EPCIS Query parameters.</param>
		/// <returns>The EPCIS Query results.</returns>
		public async Task<EPCISQueryResults> QueryEvents(string blob_id, EPCISQueryParameters parameters)
		{
            using (var clientItem = HttpClientPool.GetClient())
            {
                var client = clientItem.Value;
                string url = $"{_baseURL.TrimEnd('/')}/epcis/{blob_id}";
                EPCISQueryInterfaceOptions options = new EPCISQueryInterfaceOptions()
                {
                    URL = new Uri(url),
                    Format = _format,
                    Version = _version,
                    EnableStackTrace = true
                };

                return await EPCISTraceabilityResolver.QueryEvents(options, parameters, client);
            }
        }

		/// <summary>
		/// Queries and performs a traceback against the test server blob given the EPC.
		/// </summary>
		/// <param name="blob_id">The ID of the blob to query.</param>
		/// <param name="epc">The EPC to perform the traceback on.</param>
		/// <returns>The epcis query results.</returns>
		public async Task<EPCISQueryResults> Traceback(string blob_id, EPC epc)
        {
            using (var clientItem = HttpClientPool.GetClient())
            {
                var client = clientItem.Value;
                string url = $"{_baseURL.TrimEnd('/')}/epcis/{blob_id}";
                EPCISQueryInterfaceOptions options = new EPCISQueryInterfaceOptions()
                {
                    URL = new Uri(url),
                    Format = _format,
                    Version = _version,
                    EnableStackTrace = true
                };

                return await EPCISTraceabilityResolver.Traceback(options, epc, client);
            }
        }

        /// <summary>
        /// Resolves all the unknown master data in the EPCIS document.
        /// </summary>
        /// <param name="blob_id">The ID of the blob to query.</param>
        /// <param name="doc">The EPCIS document to resolve the master data for.</param>
        public async Task ResolveMasterData(string blob_id, EPCISBaseDocument doc)
        {
            using (var clientItem = HttpClientPool.GetClient())
            {
                var client = clientItem.Value;
                string url = $"{_baseURL.TrimEnd('/')}/digitallink/{blob_id}";
                DigitalLinkQueryOptions options = new DigitalLinkQueryOptions()
                {
                    URL = new Uri(url),
                    EnableStackTrace = true
                };

                await MasterDataResolver.ResolveMasterData(options, doc, client);
            }
        }
    }
}

