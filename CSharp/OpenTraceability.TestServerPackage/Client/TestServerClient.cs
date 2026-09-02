using System;
using System.Collections.Generic;
using System.Net;
using System.Net.Http;
using System.Text;
using System.Threading;
using System.Threading.Tasks;
using Newtonsoft.Json;
using Newtonsoft.Json.Linq;
using OpenTraceability.TestServer.Core.Models;

namespace OpenTraceability.TestServer.Core.Client
{
    /// <summary>
    /// Typed HTTP client for the OpenTraceability test server, aligned 1:1 with its controllers.
    /// Server-level operations (health, dataset management, capability test) live here; use
    /// <see cref="ForDataset"/> to get a <see cref="TestServerDatasetClient"/> for data operations
    /// scoped to one dataset via the /{datasetId}/... path prefix.
    /// </summary>
    public sealed class TestServerClient : IDisposable
    {
        private readonly HttpClient _http;
        private readonly bool _ownsHttpClient;
        private readonly string? _apiKey;

        public string BaseUrl { get; }

        /// <summary>Creates a client that owns its own HttpClient.</summary>
        public TestServerClient(string baseUrl, string? apiKey = null)
            : this(new HttpClient(), baseUrl, apiKey)
        {
            _ownsHttpClient = true;
        }

        /// <summary>Creates a client over a caller-managed HttpClient (e.g. from IHttpClientFactory).</summary>
        public TestServerClient(HttpClient httpClient, string baseUrl, string? apiKey = null)
        {
            _http = httpClient ?? throw new ArgumentNullException(nameof(httpClient));
            BaseUrl = (baseUrl ?? throw new ArgumentNullException(nameof(baseUrl))).TrimEnd('/');
            _apiKey = apiKey;
        }

        /// <summary>Returns a client for data operations scoped to the given dataset.</summary>
        public TestServerDatasetClient ForDataset(string datasetId)
        {
            if (string.IsNullOrWhiteSpace(datasetId)) throw new ArgumentException("datasetId is required.", nameof(datasetId));
            return new TestServerDatasetClient(this, datasetId.Trim('/'));
        }

        // ---- health ----

        /// <summary>GET /health. Returns true when the server reports healthy.</summary>
        public async Task<bool> HealthAsync(CancellationToken ct = default)
        {
            using var resp = await SendAsync(HttpMethod.Get, "/health", null, ct);
            return resp.IsSuccessStatusCode;
        }

        // ---- dataset management ----

        /// <summary>GET /datasets.</summary>
        public async Task<List<DatasetModel>> ListDatasetsAsync(CancellationToken ct = default)
        {
            string body = await SendForBodyAsync(HttpMethod.Get, "/datasets", null, ct);
            return JsonConvert.DeserializeObject<List<DatasetModel>>(body) ?? new List<DatasetModel>();
        }

        /// <summary>GET /datasets/{datasetId}. Returns null when the dataset does not exist.</summary>
        public async Task<DatasetModel?> GetDatasetAsync(string datasetId, CancellationToken ct = default)
        {
            using var resp = await SendAsync(HttpMethod.Get, $"/datasets/{Uri.EscapeDataString(datasetId)}", null, ct);
            if (resp.StatusCode == HttpStatusCode.NotFound) return null;
            string body = await EnsureSuccessAsync(resp, "GET", "/datasets/" + datasetId);
            return JsonConvert.DeserializeObject<DatasetModel>(body);
        }

        /// <summary>POST /datasets. Fails with a 409 <see cref="TestServerApiException"/> when the dataset already exists.</summary>
        public async Task<DatasetModel> CreateDatasetAsync(DatasetModel dataset, CancellationToken ct = default)
        {
            string body = await SendForBodyAsync(HttpMethod.Post, "/datasets", JsonContent(dataset), ct);
            return JsonConvert.DeserializeObject<DatasetModel>(body)!;
        }

        /// <summary>PUT /datasets/{datasetId} — idempotent create-or-update of the dataset's modules/description.</summary>
        public async Task<DatasetModel> UpsertDatasetAsync(string datasetId, UpsertDatasetRequest request, CancellationToken ct = default)
        {
            string body = await SendForBodyAsync(HttpMethod.Put, $"/datasets/{Uri.EscapeDataString(datasetId)}", JsonContent(request), ct);
            return JsonConvert.DeserializeObject<DatasetModel>(body)!;
        }

        /// <summary>DELETE /datasets/{datasetId}?purgeData=...</summary>
        public async Task DeleteDatasetAsync(string datasetId, bool purgeData = false, CancellationToken ct = default)
        {
            await SendForBodyAsync(HttpMethod.Delete, $"/datasets/{Uri.EscapeDataString(datasetId)}?purgeData={(purgeData ? "true" : "false")}", null, ct);
        }

        /// <summary>POST /datasets/{datasetId}/clear — purges the dataset's data, keeping the record.</summary>
        public async Task ClearDatasetAsync(string datasetId, CancellationToken ct = default)
        {
            await SendForBodyAsync(HttpMethod.Post, $"/datasets/{Uri.EscapeDataString(datasetId)}/clear", null, ct);
        }

        // ---- plumbing (shared with TestServerDatasetClient) ----

        internal async Task<HttpResponseMessage> SendAsync(HttpMethod method, string relativePath, HttpContent? content, CancellationToken ct, Action<HttpRequestMessage>? configure = null)
        {
            var request = new HttpRequestMessage(method, BaseUrl + relativePath);
            if (!string.IsNullOrEmpty(_apiKey))
            {
                request.Headers.Add("X-API-Key", _apiKey);
            }
            if (content != null)
            {
                request.Content = content;
            }
            configure?.Invoke(request);
            return await _http.SendAsync(request, ct);
        }

        internal async Task<string> SendForBodyAsync(HttpMethod method, string relativePath, HttpContent? content, CancellationToken ct, Action<HttpRequestMessage>? configure = null)
        {
            using var resp = await SendAsync(method, relativePath, content, ct, configure);
            return await EnsureSuccessAsync(resp, method.Method, relativePath);
        }

        internal static async Task<string> EnsureSuccessAsync(HttpResponseMessage resp, string method, string url)
        {
            string body = await resp.Content.ReadAsStringAsync();
            if (!resp.IsSuccessStatusCode)
            {
                throw new TestServerApiException(resp.StatusCode, method, url, body);
            }
            return body;
        }

        internal static StringContent JsonContent(object value)
            => new StringContent(JsonConvert.SerializeObject(value), Encoding.UTF8, "application/json");

        public void Dispose()
        {
            if (_ownsHttpClient)
            {
                _http.Dispose();
            }
        }
    }
}
