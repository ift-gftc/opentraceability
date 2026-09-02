using System;
using System.Collections.Generic;
using System.Linq;
using System.Net.Http.Headers;
using System.Threading.Tasks;

namespace OpenTraceability.Queries.Diagnostics.Rules;

/// <summary>
/// This rule validates the HTTP request headers for Digital Link resolver requests.
/// 
/// EXPECTED PARAMETERS:
/// - parameters[0] HttpRequestHeaders - the request headers to validate
/// 
/// VALIDATION #1: Accept Header Validation
/// - If the Accept header is missing or doesn't contain "application/json", 
///   the rule will return a validation result with type "HttpError" and level "Warning".
/// 
/// VALIDATION #2: Host Header Validation  
/// - If the Host header is missing or empty, the rule will return a validation 
///   result with type "HttpError" and level "Warning".
/// </summary>
public class DigitalLinkHttpRequestRule : IDiagnosticsRequestRule
{
    public string Key { get; set; } = "OT_DIAG_RULE_DL_HTTP_REQUEST";

    public Task<List<DiagnosticsValidationResult>> ExecuteAsync(params object?[] obj)
    {
        var results = new List<DiagnosticsValidationResult>();

        if (obj.Length < 1)
        {
            throw new ArgumentException("HttpRequestHeaders parameter is required.", nameof(obj));
        }

        var headers = obj[0] as HttpRequestHeaders;
        if (headers == null)
        {
            throw new ArgumentNullException(nameof(obj), "HttpRequestHeaders parameter is null or invalid.");
        }

        // VALIDATION #1: Accept Header Validation
        ValidateAcceptHeader(headers, results);

        // VALIDATION #2: Host Header Validation
        ValidateHostHeader(headers, results);

        return Task.FromResult(results);
    }

    private void ValidateAcceptHeader(HttpRequestHeaders headers, List<DiagnosticsValidationResult> results)
    {
        if (headers.Accept == null || !headers.Accept.Any())
        {
            results.Add(new DiagnosticsValidationResult
            {
                Level = LogLevel.Warning,
                Type = DiagnosticsValidationType.HttpError,
                RuleKey = Key,
                Message = "Accept header is missing from Digital Link request."
            });
            return;
        }

        if (!headers.Accept.Any(h => h.MediaType?.Contains("application/json") == true))
        {
            results.Add(new DiagnosticsValidationResult
            {
                Level = LogLevel.Warning,
                Type = DiagnosticsValidationType.HttpError,
                RuleKey = Key,
                Message = "Accept header must contain 'application/json' for Digital Link requests."
            });
        }
    }

    private void ValidateHostHeader(HttpRequestHeaders headers, List<DiagnosticsValidationResult> results)
    {
        if (string.IsNullOrWhiteSpace(headers.Host))
        {
            results.Add(new DiagnosticsValidationResult
            {
                Level = LogLevel.Warning,
                Type = DiagnosticsValidationType.HttpError,
                RuleKey = Key,
                Message = "Host header is missing or empty in Digital Link request."
            });
        }
    }
}
