using System;
using OpenTraceability.Interfaces;
using OpenTraceability.Mappers;
using OpenTraceability.Models.Events;
using OpenTraceability.Models.Identifiers;

namespace OpenTraceability.Queries
{
	/// <summary>
	/// A client for talking to an Open Traceability Test Server for
	/// posting and querying traceability data.
	/// </summary>
	public class EPCISTestServerClient
	{
		string _baseURL;
		string _user_id;
		EPCISDataFormat _format;
		EPCISVersion _version;

		public EPCISTestServerClient(string baseURL, EPCISDataFormat format, EPCISVersion version, string user_id)
		{
            _baseURL = baseURL;
			_user_id = user_id;
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
		public async Task<string> Post(EPCISDocument doc)
		{
			string blob_id = Guid.NewGuid().ToString();
            string url = $"{_baseURL.TrimEnd('/')}/epcis/{_user_id}/{blob_id}/events";

			IEPCISDocumentMapper mapper = OpenTraceabilityMappers.EPCISDocument.XML;
			string contentType = "application/xml";
			if (_format == EPCISDataFormat.JSON)
			{
                contentType = "application/json";
                mapper = OpenTraceabilityMappers.EPCISDocument.JSON;
			}

			HttpClient client = new HttpClient();

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

			var response = await client.SendAsync(request);
			if (!response.IsSuccessStatusCode)
			{
				string contentStr = await response.Content.ReadAsStringAsync();
				throw new Exception($"{(int)response.StatusCode} - {response.StatusCode} - {contentStr}");
			}

			return blob_id;
        }

		/// <summary>
		/// Queries the test server blob for events that match the parameters.
		/// </summary>
		/// <param name="blob_id">The ID of the blob to query.</param>
		/// <param name="parameters">The EPCIS Query parameters.</param>
		/// <returns>The EPCIS Query results.</returns>
		public async Task<EPCISQueryResults> QueryEvents(string blob_id, EPCISQueryParameters parameters)
		{
			string url = $"{_baseURL.TrimEnd('/')}/epcis/{_user_id}/{blob_id}";
			EPCISQueryInterfaceOptions options = new EPCISQueryInterfaceOptions()
			{
				URL = new Uri(url),
				Format = _format,
				Version = _version,
				EnableStackTrace = true
			};

            return await EPCISTraceabilityResolver.QueryEvents(options, parameters);
        }

		/// <summary>
		/// Queries and performs a traceback against the test server blob given the EPC.
		/// </summary>
		/// <param name="blob_id">The ID of the blob to query.</param>
		/// <param name="epc">The EPC to perform the traceback on.</param>
		/// <returns>The epcis query results.</returns>
		public async Task<EPCISQueryResults> Traceback(string blob_id, EPC epc)
		{
            string url = $"{_baseURL.TrimEnd('/')}/{_user_id}/{blob_id}";
            EPCISQueryInterfaceOptions options = new EPCISQueryInterfaceOptions()
            {
                URL = new Uri(url),
                Format = _format,
                Version = _version,
                EnableStackTrace = true
            };

			return await EPCISTraceabilityResolver.Traceback(options, epc);
        }
    }
}

