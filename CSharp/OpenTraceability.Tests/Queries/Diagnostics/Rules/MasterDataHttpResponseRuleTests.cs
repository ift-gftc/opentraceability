using System.Net;
using System.Net.Http;
using OpenTraceability.Queries.Diagnostics;
using OpenTraceability.Queries.Diagnostics.Rules;

namespace OpenTraceability.Tests.Queries.Diagnostics.Rules;

[TestFixture]
[Category("UnitTest")]
public class MasterDataHttpResponseRuleTests
{
    private MasterDataHttpResponseRule _rule;

    [SetUp]
    public void SetUp()
    {
        _rule = new MasterDataHttpResponseRule();
    }

    [Test]
    public async Task ExecuteAsync_WithSuccessStatusCode_ShouldPassValidation()
    {
        // Arrange
        var response = new HttpResponseMessage(HttpStatusCode.OK);

        // Act
        var results = await _rule.ExecuteAsync(response);

        // Assert
        Assert.That(results, Is.Not.Null);
        Assert.That(results.Count, Is.EqualTo(0)); // No validation errors for success status
    }

    [Test]
    public async Task ExecuteAsync_WithErrorStatusCode_ShouldReturnError()
    {
        // Arrange
        var response = new HttpResponseMessage(HttpStatusCode.NotFound);

        // Act
        var results = await _rule.ExecuteAsync(response);

        // Assert
        Assert.That(results, Is.Not.Null);
        Assert.That(results.Count, Is.EqualTo(1));
        Assert.That(results[0].Level, Is.EqualTo(LogLevel.Error));
        Assert.That(results[0].Type, Is.EqualTo(DiagnosticsValidationType.HttpError));
        Assert.That(results[0].RuleKey, Is.EqualTo("OT_DIAG_RULE_MD_HTTP_RESPONSE"));
        Assert.That(results[0].Message, Does.Contain("404"));
        Assert.That(results[0].Message, Does.Contain("could not be found"));
    }

    [Test]
    public async Task ExecuteAsync_WithServerError_ShouldReturnError()
    {
        // Arrange
        var response = new HttpResponseMessage(HttpStatusCode.InternalServerError);

        // Act
        var results = await _rule.ExecuteAsync(response);

        // Assert
        Assert.That(results, Is.Not.Null);
        Assert.That(results.Count, Is.EqualTo(1));
        Assert.That(results[0].Level, Is.EqualTo(LogLevel.Error));
        Assert.That(results[0].Type, Is.EqualTo(DiagnosticsValidationType.HttpError));
        Assert.That(results[0].Message, Does.Contain("500"));
        Assert.That(results[0].Message, Does.Contain("InternalServerError"));
    }

    [Test]
    public async Task ExecuteAsync_WithRedirection_ShouldReturnError()
    {
        // Arrange
        var response = new HttpResponseMessage(HttpStatusCode.Found);

        // Act
        var results = await _rule.ExecuteAsync(response);

        // Assert
        Assert.That(results, Is.Not.Null);
        Assert.That(results.Count, Is.EqualTo(1));
        Assert.That(results[0].Level, Is.EqualTo(LogLevel.Error));
        Assert.That(results[0].Type, Is.EqualTo(DiagnosticsValidationType.HttpError));
        Assert.That(results[0].Message, Does.Contain("302"));
    }

    [Test]
    public void ExecuteAsync_WithNullParameter_ShouldThrowArgumentException()
    {
        // Act & Assert
        var ex = Assert.ThrowsAsync<ArgumentException>(async () => 
            await _rule.ExecuteAsync((object?)null));
        Assert.That(ex.Message, Does.Contain("null or invalid"));
    }

    [Test]
    public void ExecuteAsync_WithNoParameters_ShouldThrowArgumentException()
    {
        // Act & Assert
        var ex = Assert.ThrowsAsync<ArgumentException>(async () => 
            await _rule.ExecuteAsync());
        Assert.That(ex.Message, Does.Contain("parameter is required"));
    }

    [Test]
    public void Key_ShouldHaveCorrectValue()
    {
        // Assert
        Assert.That(_rule.Key, Is.EqualTo("OT_DIAG_RULE_MD_HTTP_RESPONSE"));
    }
}
