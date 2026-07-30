using System;
using System.Collections.Generic;
using System.Net;
using System.Net.Http;
using System.Threading.Tasks;

namespace OpenTraceability.Queries.Diagnostics.Rules;

/// <summary>
/// This rule validates the HTTP response from an EPCIS Query Interface.
/// 
/// EXPECTED PARAMETERS:
/// - parameters[0] HttpResponseMessage - the HTTP response to validate
/// 
/// VALIDATION #1: HTTP Status Code Validation
/// - If the response status code is not in the 2xx range, the rule will return 
///   a validation result with type "HttpError" and level "Error"
/// 
/// VALIDATION #2: Content-Type Header Validation
/// - Validates that the Content-Type header matches the expected format 
///   (application/xml or application/json)
/// </summary>
public class EPCISHttpResponseRule : IDiagnosticsRequestRule
{
    public string Key { get; set; } = "OT_DIAG_RULE_EPCIS_HTTP_RESPONSE";

    public Task<List<DiagnosticsValidationResult>> ExecuteAsync(params object?[] obj)
    {
        if (obj == null || obj.Length < 1)
        {
            throw new ArgumentException("HttpResponseMessage parameter is required.", nameof(obj));
        }

        if (obj[0] is not HttpResponseMessage response)
        {
            throw new ArgumentException("HttpResponseMessage parameter is null or invalid.", nameof(obj));
        }

        var results = new List<DiagnosticsValidationResult>();

        // VALIDATION #1: HTTP Status Code Validation
        if (!IsSuccessStatusCode(response.StatusCode))
        {
            results.Add(new DiagnosticsValidationResult
            {
                Level = LogLevel.Error,
                Type = DiagnosticsValidationType.HttpError,
                RuleKey = Key,
                Message = $"EPCIS Query Interface returned non-success status code: {(int)response.StatusCode} {response.StatusCode}"
            });
        }

        // VALIDATION #2: Content-Type Header Validation (only check if status code was success)
        if (response.IsSuccessStatusCode)
        {
            ValidateContentType(response, results);
        }

        return Task.FromResult(results);
    }

    private bool IsSuccessStatusCode(HttpStatusCode statusCode)
    {
        return ((int)statusCode >= 200) && ((int)statusCode <= 299);
    }

    private void ValidateContentType(HttpResponseMessage response, List<DiagnosticsValidationResult> results)
    {
        if (response.Content?.Headers.ContentType == null)
        {
            results.Add(new DiagnosticsValidationResult
            {
                Level = LogLevel.Warning,
                Type = DiagnosticsValidationType.HttpError,
                RuleKey = Key,
                Message = "Content-Type header is missing from EPCIS response."
            });
            return;
        }

        var contentType = response.Content.Headers.ContentType.MediaType?.ToLower();
        if (contentType != null && !string.IsNullOrEmpty(contentType) &&
            contentType != "application/json" &&
            contentType != "application/xml" &&
            !contentType.Contains("json") &&
            !contentType.Contains("xml"))
        {
            results.Add(new DiagnosticsValidationResult
            {
                Level = LogLevel.Warning,
                Type = DiagnosticsValidationType.HttpError,
                RuleKey = Key,
                Message = $"Unexpected Content-Type header: {contentType}. Expected application/json or application/xml."
            });
        }
    }
}