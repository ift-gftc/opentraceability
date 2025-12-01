#nullable enable
using DiagnosticsTool;
using DiagnosticsTool.Services;
using Microsoft.AspNetCore.Hosting;
using Microsoft.Extensions.Configuration;
using Microsoft.Extensions.DependencyInjection;

namespace OpenTraceability.Tests.Diagnostics
{
    [TestFixture]
    [Category("Integration")]
    public class DiagnosticsToolTestServiceTests
    {
        private static IConfiguration config;

        static DiagnosticsToolTestServiceTests()
        {
            ConfigurationBuilder builder = new ConfigurationBuilder();
            builder.AddInMemoryCollection(new Dictionary<string, string?>()
            {
                { "DiagnosticsTool:URL", "https://" }
            });

            config = builder.Build();
        }

        [TestCase("bad-digital-link-content-type")]
        public async Task ExecuteDiagnosticsTests(string testId)
        {
            using var host = WebServiceFactory.Create("https://localhost:9076", config);
            using var scope = host.Services.CreateScope();

            using var client = new HttpClient();
            client.BaseAddress = new Uri("https://localhost:9076");

            var testService = CreateTestService(scope, client);

            var cache = scope.ServiceProvider.GetRequiredService<IDiagnosticsEnvelopeCache>();

            var result = await testService.ExecuteTestAsync(testId, client.BaseAddress ?? throw new Exception("No base address on client."));

            Assert.That(result, Is.Not.Null, "Test execution result should not be null.");
            Assert.That(result.DiagnosticsId, Is.Not.Null.And.Not.Empty, "Diagnostics ID should be provided.");

            var found = cache.TryGet(result.DiagnosticsId!, out var envelope, out _);
            Assert.That(found, Is.True, "Diagnostics envelope should exist in cache after execution.");
            Assert.That(envelope, Is.Not.Null, "Envelope should not be null.");

            if (testId.Equals("bad-digital-link-content-type", StringComparison.OrdinalIgnoreCase))
            {
                Assert.That(envelope!.Diagnostics.Validations.Count, Is.GreaterThanOrEqualTo(0));
                Assert.That(envelope.Diagnostics.Requests, Is.Not.Empty, "Bad digital link test should capture request diagnostics.");
                var request = envelope.Diagnostics.Requests.First();
                if(request.HttpRequest?.Headers.TryGetValues("x-api-key", out var apiKeyHeader) == true)
                {
                    var headerValue = apiKeyHeader.FirstOrDefault();
                    Assert.That(apiKeyHeader, Is.Not.Null, "x-api-key header should be present in the request.");
                }
                else
                {
                    Assert.Fail("Missing x-api-key header in the request.");
                }
            }
        }

        private static TestService CreateTestService(IServiceScope scope, HttpClient client)
        {
            var env = scope.ServiceProvider.GetRequiredService<IWebHostEnvironment>();
            var httpClientFactory = new StaticHttpClientFactory(client);
            return new TestService(env, httpClientFactory);
        }

        private sealed class StaticHttpClientFactory : IHttpClientFactory
        {
            private readonly HttpClient _client;

            public StaticHttpClientFactory(HttpClient client)
            {
                _client = client;
            }

            public HttpClient CreateClient(string name) => _client;
        }
    }
}
