using System;
using System.Collections.Generic;
using System.Linq;
using System.Net.Http;
using System.Text;
using System.Threading.Tasks;
using Microsoft.Extensions.Configuration;
using Microsoft.Extensions.Logging;
using Newtonsoft.Json;
using Newtonsoft.Json.Linq;
using OpenTraceability.GDST;
using OpenTraceability.TestServer.Core.Data;
using OpenTraceability.TestServer.Core.Models;

namespace OpenTraceability.TestServer.Services
{
    /// <summary>
    /// Drives the GDST 2.0 capability test as a solution-provider client: start the test, fetch the
    /// generated data and store it locally, advance the test, then poll the report to completion.
    /// </summary>
    public class CapabilityTestClientService
    {
        private readonly IHttpClientFactory _httpClientFactory;
        private readonly IConfiguration _config;
        private readonly TracebackService _tracebackService;
        private readonly ITraceabilityStore _store;
        private readonly ILogger<CapabilityTestClientService> _logger;

        public CapabilityTestClientService(
            IHttpClientFactory httpClientFactory,
            IConfiguration config,
            TracebackService tracebackService,
            ITraceabilityStore store,
            ILogger<CapabilityTestClientService> logger)
        {
            _httpClientFactory = httpClientFactory;
            _config = config;
            _tracebackService = tracebackService;
            _store = store;
            _logger = logger;
        }

        public async Task<JObject> RunAsync(CapabilityTestRequest request, Dataset dataset)
        {
            string toolUrl = request.ToolUrl.TrimEnd('/');
            string ourBaseUrl = (_config["BaseURL"] ?? string.Empty).TrimEnd('/');
            if (string.IsNullOrWhiteSpace(ourBaseUrl))
            {
                throw new Exception("BaseURL is not configured; the capability tool needs a publicly reachable digital-link resolver URL for this server.");
            }

            // Embedding the dataset in the resolver root is the only dataset selector the tool
            // honors — it appends identifier paths to this URL verbatim and sends no dataset or
            // module headers. Every request it makes stays scoped to this dataset, whose persisted
            // modules drive both the minification it sees and the module list sent below.
            string ourResolverUrl = $"{ourBaseUrl}/{dataset.DatasetId}/digitallink/";
            string ourApiKey = _config.GetSection("Authentication:APIKey:ValidKeys").Get<List<string>>()?.FirstOrDefault() ?? string.Empty;

            if (request.ClearDatasetBeforeRun)
            {
                _logger.LogInformation("Clearing dataset {DatasetId} before capability test run", dataset.DatasetId);
                await _store.ClearDatasetDataAsync(dataset.DatasetId);
            }

            var client = _httpClientFactory.CreateClient();
            client.Timeout = TimeSpan.FromMinutes(2);
            client.DefaultRequestHeaders.Add("X-API-Key", request.ToolApiKey);

            // Module ids match GDSTTools SolutionV2Module (Seafood=1, Wildcaught=2, Aquaculture=3);
            // Core is implicit and never sent.
            var moduleIds = dataset.GetExpandedModules()
                .Where(m => m != GdstModule.Core)
                .Select(m => (int)m)
                .OrderBy(id => id)
                .Cast<object>()
                .ToArray();

            // 1. Start the test.
            var startBody = new JObject
            {
                ["SolutionName"] = request.SolutionName,
                ["Version"] = request.Version ?? "1.0.0",
                ["APIKey"] = ourApiKey,
                ["URL"] = ourResolverUrl,
                ["PGLN"] = request.Pgln,
                ["GDSTVersion"] = 20,
                ["Modules"] = new JArray(moduleIds),
                ["SolutionProviderEPCs"] = new JArray(request.SolutionProviderEPCs.Cast<object>().ToArray())
            };

            var startResp = await client.PostAsync($"{toolUrl}/v2/process/start",
                new StringContent(startBody.ToString(), Encoding.UTF8, "application/json"));
            string startContent = await startResp.Content.ReadAsStringAsync();
            if (!startResp.IsSuccessStatusCode)
            {
                throw new Exception($"capability test start failed: {(int)startResp.StatusCode} {startContent}");
            }

            JObject startResult = ParseObjectOrEmpty(startContent);
            string uuid = startResp.Headers.TryGetValues("X-Capability-Process-UUID", out var headerVals)
                ? headerVals.FirstOrDefault() ?? string.Empty
                : startResult["ComplianceProcessUUID"]?.ToString() ?? string.Empty;

            var generatedEpcs = (startResult["epCs"] as JArray)?.Select(t => t.ToString()).ToList() ?? new List<string>();
            _logger.LogInformation("Capability test started. UUID={Uuid}, generated EPCs={Count}", uuid, generatedEpcs.Count);

            if (generatedEpcs.Count < 1)
            {
                throw new Exception("Failed to parse generated EPCs from the capability tool's start response.");
            }

            // 2. Fetch the generated data from the tool's resolver and store it locally.
            string toolResolverUrl = request.ToolResolverUrl ?? $"{toolUrl}/digitallink/";

            var traceResult = await _tracebackService.ExecuteAsync(new TracebackRequest
            {
                Epcs = generatedEpcs,
                ResolverUrl = toolResolverUrl,
                ApiKey = request.ToolApiKey,
                Format = "JSON",
                Version = "2.0",
                DatasetId = dataset.DatasetId,
                CapabilityProcessUUID = uuid
            });
            _logger.LogInformation("Stored generated data: {Events} events, {MasterData} master data",
                traceResult.EventsStored, traceResult.MasterDataStored);

            // 3. Advance the test to the processing stage.
            var nextBody = new JObject { ["SolutionProviderEPCs"] = new JArray(request.SolutionProviderEPCs.Cast<object>().ToArray()) };
            using (var nextReq = new HttpRequestMessage(HttpMethod.Post, $"{toolUrl}/v2/process/next"))
            {
                nextReq.Headers.Add("X-Capability-Process-UUID", uuid);
                nextReq.Content = new StringContent(nextBody.ToString(), Encoding.UTF8, "application/json");
                var nextResp = await client.SendAsync(nextReq);
                if (!nextResp.IsSuccessStatusCode)
                {
                    string nextContent = await nextResp.Content.ReadAsStringAsync();
                    throw new Exception($"capability test next failed: {(int)nextResp.StatusCode} {nextContent}");
                }
            }

            // 4. Poll the report until the test is no longer in the Started state.
            JObject report = new JObject();
            for (int i = 0; i < 300; i++)
            {
                await Task.Delay(1000);
                using var reportReq = new HttpRequestMessage(HttpMethod.Get, $"{toolUrl}/v2/process/report");
                reportReq.Headers.Add("X-Capability-Process-UUID", uuid);
                var reportResp = await client.SendAsync(reportReq);
                if (!reportResp.IsSuccessStatusCode) continue;

                string reportContent = await reportResp.Content.ReadAsStringAsync();
                report = ParseObjectOrEmpty(reportContent);

                if (!IsStarted(report["Status"]))
                {
                    break;
                }
            }

            return report;
        }

        private static bool IsStarted(JToken? statusToken)
        {
            if (statusToken == null) return false;
            if (statusToken.Type == JTokenType.Integer) return statusToken.Value<int>() == 0;
            return string.Equals(statusToken.ToString(), "Started", StringComparison.OrdinalIgnoreCase)
                || statusToken.ToString() == "0";
        }

        private static JObject ParseObjectOrEmpty(string content)
        {
            try
            {
                return string.IsNullOrWhiteSpace(content) ? new JObject() : JObject.Parse(content);
            }
            catch
            {
                return new JObject();
            }
        }
    }
}
