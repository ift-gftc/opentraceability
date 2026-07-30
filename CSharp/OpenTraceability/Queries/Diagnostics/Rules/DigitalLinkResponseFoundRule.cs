using System;
using System.Collections.Generic;
using System.Threading.Tasks;
using OpenTraceability.Models.MasterData;

namespace OpenTraceability.Queries.Diagnostics.Rules;

/// <summary>
/// This rule validates that a valid Digital Link response was found and processed.
/// 
/// EXPECTED PARAMETERS:
/// - parameters[0] string - the JSON response string to validate
/// 
/// VALIDATION #1: Digital Link Schema Validation
/// - If the JSON doesn't contain the expected Digital Link structure (array of 
///   objects with 'link' properties), the rule will return a validation result 
///   with type "SchemaError" and level "Error".
/// 
/// VALIDATION #2: Link Property Validation
/// - If any Digital Link object is missing required 'link' property or the 
///   link is not a valid URL, the rule will return a validation result with 
///   type "SchemaError" and level "Error" or "Warning".
/// </summary>
public class DigitalLinkResponseFoundRule : IDiagnosticsRequestRule
{
    public string Key { get; set; } = "OT_DIAG_RULE_DL_RESPONSE_FOUND";

    public Task<List<DiagnosticsValidationResult>> ExecuteAsync(params object?[] obj)
    {
        if (obj == null || obj.Length < 1)
        {
            throw new ArgumentException("JSON response string parameter is required.", nameof(obj));
        }

        if (obj[0] != null && obj[0] is not string)
        {
            throw new ArgumentException("JSON response string parameter is null or invalid.", nameof(obj));
        }

        var results = new List<DiagnosticsValidationResult>();
        string? jsonContent = obj[0] as string;

        if (string.IsNullOrWhiteSpace(jsonContent))
        {
            results.Add(new DiagnosticsValidationResult
            {
                Level = LogLevel.Error,
                Type = DiagnosticsValidationType.SchemaError,
                RuleKey = Key,
                Message = "JSON response content is null or empty."
            });
            return Task.FromResult(results);
        }

        ValidateDigitalLinkSchema(jsonContent!, results);

        return Task.FromResult(results);
    }

    private void ValidateDigitalLinkSchema(string jsonContent, List<DiagnosticsValidationResult> results)
    {
        try
        {
            var digitalLinks = Newtonsoft.Json.JsonConvert.DeserializeObject<List<DigitalLink>>(jsonContent);

            if (digitalLinks == null)
            {
                results.Add(new DiagnosticsValidationResult
                {
                    Level = LogLevel.Error,
                    Type = DiagnosticsValidationType.SchemaError,
                    RuleKey = Key,
                    Message = "Digital Link response is not a valid array of Digital Link objects."
                });
                return;
            }

            // VALIDATION #2: Link Property Validation
            foreach (var link in digitalLinks)
            {
                if (string.IsNullOrWhiteSpace(link.link))
                {
                    results.Add(new DiagnosticsValidationResult
                    {
                        Level = LogLevel.Error,
                        Type = DiagnosticsValidationType.SchemaError,
                        RuleKey = Key,
                        Message = "Digital Link object is missing required 'link' property or it is empty."
                    });
                }
                else if (!Uri.IsWellFormedUriString(link.link, UriKind.Absolute))
                {
                    results.Add(new DiagnosticsValidationResult
                    {
                        Level = LogLevel.Warning,
                        Type = DiagnosticsValidationType.SchemaError,
                        RuleKey = Key,
                        Message = $"Digital Link 'link' property is not a valid URL: {link.link}"
                    });
                }
            }
        }
        catch (Newtonsoft.Json.JsonException ex)
        {
            results.Add(new DiagnosticsValidationResult
            {
                Level = LogLevel.Error,
                Type = DiagnosticsValidationType.SchemaError,
                RuleKey = Key,
                Message = $"Failed to parse Digital Link response as array: {ex.Message}"
            });
        }
    }
}
