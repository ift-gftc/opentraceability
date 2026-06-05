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

namespace OpenTraceability.TestServer.Services
{
    /// <summary>The request body for triggering a GDST 2.0 capability test run.</summary>
    public class CapabilityTestRequest
    {
        /// <summary>The capability tool base URL (e.g. https://capabilitytool-beta-service.azurewebsites.net).</summary>
        public string ToolUrl { get; set; } = string.Empty;

        /// <summary>The X-API-Key to authenticate against the capability tool.</summary>
        public string ToolApiKey { get; set; } = string.Empty;

        /// <summary>Optional digital-link resolver URL of the tool used to fetch generated data. Defaults to {ToolUrl}/digitallink/.</summary>
        public string? ToolResolverUrl { get; set; }

        public string SolutionName { get; set; } = string.Empty;
        public string? Version { get; set; }
        public string Pgln { get; set; } = string.Empty;

        /// <summary>Module names: Seafood, Wildcaught, Aquaculture.</summary>
        public List<string> Modules { get; set; } = new List<string>();

        /// <summary>Optional EPCs of the solution provider's own data to be validated.</summary>
        public List<string> SolutionProviderEPCs { get; set; } = new List<string>();
    }

    /// <summary>
    /// Drives the GDST 2.0 capability test as a solution-provider client: start the test, fetch the
    /// generated data and store it locally, advance the test, then poll the report to completion.
    /// </summary>
    public class CapabilityTestClientService
    {
        private readonly IHttpClientFactory _httpClientFactory;
        private readonly IConfiguration _config;
        private readonly TracebackService _tracebackService;
        private readonly ILogger<CapabilityTestClientService> _logger;

        public CapabilityTestClientService(
            IHttpClientFactory httpClientFactory,
            IConfiguration config,
            TracebackService tracebackService,
            ILogger<CapabilityTestClientService> logger)
        {
            _httpClientFactory = httpClientFactory;
            _config = config;
            _tracebackService = tracebackService;
            _logger = logger;
        }

        public async Task<JObject> RunAsync(CapabilityTestRequest request)
        {
            string toolUrl = request.ToolUrl.TrimEnd('/');
            string ourBaseUrl = (_config["BaseURL"] ?? string.Empty).TrimEnd('/');
            string ourResolverUrl = ourBaseUrl + "/digitallink/";
            string ourApiKey = _config.GetSection("Authentication:APIKey:ValidKeys").Get<List<string>>()?.FirstOrDefault() ?? string.Empty;

            var client = _httpClientFactory.CreateClient();
            client.Timeout = TimeSpan.FromMinutes(2);
            client.DefaultRequestHeaders.Add("X-API-Key", request.ToolApiKey);

            // 1. Start the test.
            var startBody = new JObject
            {
                ["SolutionName"] = request.SolutionName,
                ["Version"] = request.Version ?? "1.0.0",
                ["APIKey"] = ourApiKey,
                ["URL"] = ourResolverUrl,
                ["PGLN"] = request.Pgln,
                ["GDSTVersion"] = 20,
                ["Modules"] = new JArray(request.Modules.Select(ToModuleId).Where(id => id > 0).Cast<object>().ToArray()),
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

            var generatedEpcs = (startResult["EPCs"] as JArray)?.Select(t => t.ToString()).ToList() ?? new List<string>();
            _logger.LogInformation("Capability test started. UUID={Uuid}, generated EPCs={Count}", uuid, generatedEpcs.Count);

            // 2. Fetch the generated data from the tool's resolver and store it locally.
            string toolResolverUrl = request.ToolResolverUrl ?? $"{toolUrl}/digitallink/";
            if (generatedEpcs.Count > 0)
            {
                var traceResult = await _tracebackService.ExecuteAsync(new TracebackRequest
                {
                    Epcs = generatedEpcs,
                    ResolverUrl = toolResolverUrl,
                    ApiKey = request.ToolApiKey,
                    Format = "JSON",
                    Version = "2.0"
                });
                _logger.LogInformation("Stored generated data: {Events} events, {MasterData} master data",
                    traceResult.EventsStored, traceResult.MasterDataStored);
            }

            // 3. Advance the test to the processing stage.
            var nextBody = new JObject { ["EPCs"] = new JArray(generatedEpcs.Cast<object>().ToArray()) };
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

        private static int ToModuleId(string moduleName)
        {
            // Matches GDSTTools SolutionV2Module: Seafood=1, WildCaught=2, Aquaculture=3.
            return (moduleName?.Trim().ToLower()) switch
            {
                "seafood" => 1,
                "wildcaught" => 2,
                "aquaculture" => 3,
                _ => 0
            };
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
