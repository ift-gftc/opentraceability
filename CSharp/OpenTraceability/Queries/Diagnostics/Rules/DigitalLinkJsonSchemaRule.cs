using System;
using System.Collections.Generic;
using System.Threading.Tasks;
using OpenTraceability.Utility;

namespace OpenTraceability.Queries.Diagnostics.Rules;

/// <summary>
/// This rule validates that the JSON response from a Digital Link resolver 
/// conforms to the expected Digital Link JSON schema format.
/// 
/// EXPECTED PARAMETERS:
/// - parameters[0] string - the JSON response string to validate
/// 
/// VALIDATION #1: JSON Format Validation
/// - If the JSON is malformed or cannot be parsed, the rule will return 
///   a validation result with type "SchemaError" and level "Error".
/// 
/// VALIDATION #2: JSON Schema Validation
/// - Validates the JSON against the Digital Link JSON schema using JsonSchemaChecker.
/// - Returns warning-level validation results for schema violations.
/// </summary>
public class DigitalLinkJsonSchemaRule : IDiagnosticsRequestRule
{
    public string Key { get; set; } = "OT_DIAG_RULE_DL_JSON_SCHEMA";

    public async Task<List<DiagnosticsValidationResult>> ExecuteAsync(params object?[] obj)
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
            return results;
        }

        if (!ValidateJsonFormat(jsonContent!, results))
        {
            return results;
        }

        await ValidateJsonSchemaAsync(jsonContent!, results);

        return results;
    }

    private bool ValidateJsonFormat(string jsonContent, List<DiagnosticsValidationResult> results)
    {
        try
        {
            var jsonObj = Newtonsoft.Json.JsonConvert.DeserializeObject(jsonContent);
            return true;
        }
        catch (Newtonsoft.Json.JsonException ex)
        {
            results.Add(new DiagnosticsValidationResult
            {
                Level = LogLevel.Error,
                Type = DiagnosticsValidationType.SchemaError,
                RuleKey = Key,
                Message = $"Invalid JSON format in Digital Link response: {ex.Message}"
            });
            return false;
        }
    }

    private async Task ValidateJsonSchemaAsync(string jsonContent, List<DiagnosticsValidationResult> results)
    {
        try
        {
            var schemaErrors = await JsonSchemaChecker.IsValidAsync(jsonContent, "DigitalLink");
            foreach (var error in schemaErrors)
            {
                results.Add(new DiagnosticsValidationResult
                {
                    Level = LogLevel.Warning,
                    Type = DiagnosticsValidationType.SchemaError,
                    RuleKey = Key,
                    Message = $"JSON schema validation warning: {error}"
                });
            }
        }
        catch (Exception ex)
        {
            results.Add(new DiagnosticsValidationResult
            {
                Level = LogLevel.Error,
                Type = DiagnosticsValidationType.SchemaError,
                RuleKey = Key,
                Message = $"Digital Link JSON schema validation failed: {ex.Message}"
            });
        }
    }

}
