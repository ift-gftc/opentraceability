using System.Net.Http;
using System.Net.Http.Headers;
using OpenTraceability.Mappers;
using OpenTraceability.Models.Events;
using OpenTraceability.Queries.Diagnostics;
using OpenTraceability.Queries.Diagnostics.Rules;

namespace OpenTraceability.Tests.Queries.Diagnostics.Rules;

[TestFixture]
[Category("UnitTest")]
public class EPCISHttpRequestRuleTests
{
    private EPCISHttpRequestRule _rule;

    [SetUp]
    public void SetUp()
    {
        _rule = new EPCISHttpRequestRule();
    }

    [Test]
    public async Task ExecuteAsync_WithValidEPCISV2Headers_ShouldPassValidation()
    {
        // Arrange
        var request = new HttpRequestMessage(HttpMethod.Get, "https://example.com");
        request.Headers.Host = "example.com";
        request.Headers.Accept.Add(new MediaTypeWithQualityHeaderValue("application/json"));
        request.Headers.Add("GS1-EPCIS-Version", "2.0");
        request.Headers.Add("GS1-EPCIS-Min", "2.0");
        request.Headers.Add("GS1-EPCIS-Max", "2.0");
        request.Headers.Add("GS1-CBV-Version", "2.0");
        request.Headers.Add("GS1-CBV-XML-Format", "ALWAYS_URN");

        // Act
        var results = await _rule.ExecuteAsync(request.Headers, EPCISVersion.V2, EPCISDataFormat.JSON);

        // Assert
        Assert.That(results, Is.Not.Null);
        Assert.That(results.Count, Is.EqualTo(0)); // No validation errors
    }

    [Test]
    public async Task ExecuteAsync_WithValidEPCISV1Headers_ShouldPassValidation()
    {
        // Arrange
        var request = new HttpRequestMessage(HttpMethod.Get, "https://example.com");
        request.Headers.Host = "example.com";
        request.Headers.Accept.Add(new MediaTypeWithQualityHeaderValue("application/xml"));
        request.Headers.Add("GS1-EPCIS-Version", "1.2");
        request.Headers.Add("GS1-EPCIS-Min", "1.2");
        request.Headers.Add("GS1-EPCIS-Max", "1.2");
        request.Headers.Add("GS1-CBV-Version", "1.2");
        request.Headers.Add("GS1-CBV-XML-Format", "ALWAYS_URN");

        // Act
        var results = await _rule.ExecuteAsync(request.Headers, EPCISVersion.V1, EPCISDataFormat.XML);

        // Assert
        Assert.That(results, Is.Not.Null);
        Assert.That(results.Count, Is.EqualTo(0)); // No validation errors
    }

    [Test]
    public async Task ExecuteAsync_WithMissingEPCISVersionHeaders_ShouldReturnErrors()
    {
        // Arrange
        var request = new HttpRequestMessage(HttpMethod.Get, "https://example.com");
        request.Headers.Host = "example.com";
        request.Headers.Accept.Add(new MediaTypeWithQualityHeaderValue("application/json"));
        // Missing EPCIS headers

        // Act
        var results = await _rule.ExecuteAsync(request.Headers, EPCISVersion.V2, EPCISDataFormat.JSON);

        // Assert
        Assert.That(results, Is.Not.Null);
        Assert.That(results.Count, Is.GreaterThan(0));
        Assert.That(results.Any(r => r.Message.Contains("Missing required EPCIS")), Is.True);
        Assert.That(results.All(r => r.Level == LogLevel.Error), Is.True);
        Assert.That(results.All(r => r.Type == DiagnosticsValidationType.HttpError), Is.True);
    }

    [Test]
    public async Task ExecuteAsync_WithMissingAcceptHeader_ShouldReturnError()
    {
        // Arrange
        var request = new HttpRequestMessage(HttpMethod.Get, "https://example.com");
        request.Headers.Host = "example.com";
        // No Accept header

        // Act
        var results = await _rule.ExecuteAsync(request.Headers, EPCISVersion.V2, EPCISDataFormat.JSON);

        // Assert
        Assert.That(results, Is.Not.Null);
        Assert.That(results.Any(r => r.Message.Contains("Accept header is missing")), Is.True);
    }

    [Test]
    public async Task ExecuteAsync_WithWrongAcceptHeaderForJSON_ShouldReturnWarning()
    {
        // Arrange
        var request = new HttpRequestMessage(HttpMethod.Get, "https://example.com");
        request.Headers.Host = "example.com";
        request.Headers.Accept.Add(new MediaTypeWithQualityHeaderValue("application/xml")); // Wrong for JSON

        // Act
        var results = await _rule.ExecuteAsync(request.Headers, EPCISVersion.V2, EPCISDataFormat.JSON);

        // Assert
        Assert.That(results, Is.Not.Null);
        Assert.That(results.Any(r => r.Message.Contains("does not contain expected media type")), Is.True);
    }

    // Host header is populated dynamically by the client to allow proper SSL handling with redirects
    // Checking for it before the request is executed is not necessary and forces the caller to manaully
    // set the host which causes further downstream issues with SSL hanlding.
    //[Test]
    //public async Task ExecuteAsync_WithMissingHostHeader_ShouldReturnError()
    //{
    //    // Arrange
    //    var request = new HttpRequestMessage(HttpMethod.Get, "https://example.com");
    //    request.Headers.Accept.Add(new MediaTypeWithQualityHeaderValue("application/json"));
    //    // No Host header

    //    // Act
    //    var results = await _rule.ExecuteAsync(request.Headers, EPCISVersion.V2, EPCISDataFormat.JSON);

    //    // Assert
    //    Assert.That(results, Is.Not.Null);
    //    Assert.That(results.Any(r => r.Message.Contains("Host header is missing")), Is.True);
    //}

    [Test]
    public void ExecuteAsync_WithInsufficientParameters_ShouldThrowArgumentException()
    {
        // Act / Assert
        var ex = Assert.ThrowsAsync<ArgumentException>(async () => await _rule.ExecuteAsync(new HttpRequestMessage().Headers));
        Assert.That(ex!.Message, Does.Contain("Insufficient parameters"));
    }

    [Test]
    public void ExecuteAsync_WithNullHeaders_ShouldThrowArgumentException()
    {
        // Act / Assert
        var ex = Assert.ThrowsAsync<ArgumentException>(async () => await _rule.ExecuteAsync(null, EPCISVersion.V2, EPCISDataFormat.JSON));
        Assert.That(ex!.Message, Does.Contain("null or invalid"));
    }

    [Test]
    public void Key_ShouldHaveCorrectValue()
    {
        // Assert
        Assert.That(_rule.Key, Is.EqualTo("OT_DIAG_RULE_EPCIS_HTTP_REQUEST"));
    }
}
