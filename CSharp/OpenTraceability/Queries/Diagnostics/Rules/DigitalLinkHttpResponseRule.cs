using System;
using System.Collections.Generic;
using System.Net;
using System.Net.Http;
using System.Threading.Tasks;

namespace OpenTraceability.Queries.Diagnostics.Rules;

/// <summary>
/// This rule validates the HTTP response from Digital Link resolver responses.
/// 
/// EXPECTED PARAMETERS:
/// - parameters[0] HttpResponseMessage - the HTTP response to validate
/// 
/// VALIDATION #1: Status Code Validation
/// - If the response status code is not in the 2xx range, the rule will log 
///   an error with type "HttpError" and level "Error".
/// 
/// VALIDATION #2: Content-Type Header Validation
/// - If the Content-Type header is missing or doesn't contain "application/json", 
///   the rule will log a warning with type "HttpError" and level "Warning".
/// 
/// EXCEPTION HANDLING:
/// - Throws ArgumentException if parameters are invalid instead of returning validation results.
/// </summary>
public class DigitalLinkHttpResponseRule : IDiagnosticsRequestRule
{
    public string Key { get; set; } = "OT_DIAG_RULE_DL_HTTP_RESPONSE";

    public Task<List<DiagnosticsValidationResult>> ExecuteAsync(params object?[] obj)
    {
        var results = new List<DiagnosticsValidationResult>();

        if (obj == null || obj.Length < 1)
        {
            throw new ArgumentException("HttpResponseMessage parameter is required.", nameof(obj));
        }

        var response = obj[0] as HttpResponseMessage;
        if (response == null)
        {
            throw new ArgumentException("HttpResponseMessage parameter is null or invalid.", nameof(obj));
        }

        // VALIDATION #1: Status Code Validation
        ValidateStatusCode(response, results);

        // VALIDATION #2: Content-Type Header Validation
        ValidateContentTypeHeader(response, results);

        return Task.FromResult(results);
    }

    private void ValidateStatusCode(HttpResponseMessage response, List<DiagnosticsValidationResult> results)
    {
        if (!response.IsSuccessStatusCode)
        {
            results.Add(new DiagnosticsValidationResult
            {
                Level = LogLevel.Error,
                Type = DiagnosticsValidationType.HttpError,
                RuleKey = Key,
                Message = $"Digital Link resolver returned non-success status code: {(int)response.StatusCode} {response.StatusCode}"
            });
        }
    }

    private void ValidateContentTypeHeader(HttpResponseMessage response, List<DiagnosticsValidationResult> results)
    {
        if (response.Content?.Headers.ContentType == null)
        {
            results.Add(new DiagnosticsValidationResult
            {
                Level = LogLevel.Warning,
                Type = DiagnosticsValidationType.HttpError,
                RuleKey = Key,
                Message = "Content-Type header is missing from Digital Link response."
            });
            return;
        }

        var contentType = response.Content.Headers.ContentType.MediaType?.ToLower();
        if (!string.IsNullOrEmpty(contentType) && !contentType!.Contains("application/json"))
        {
            results.Add(new DiagnosticsValidationResult
            {
                Level = LogLevel.Warning,
                Type = DiagnosticsValidationType.HttpError,
                RuleKey = Key,
                Message = $"Unexpected Content-Type header: {contentType}. Expected application/json for Digital Link response."
            });
        }
    }
}
