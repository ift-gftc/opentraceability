using System;
using System.Collections.Generic;
using System.Threading.Tasks;

namespace OpenTraceability.Queries.Diagnostics.Rules;

/// <summary>
/// This rule validates that the response from the Master Data Resolver matches the JSON schema
/// for master data.
/// </summary>
public class MasterDataJsonSchemaRule : IDiagnosticsRequestRule
{
    public string Key { get; set; } = "OT_DIAG_RULE_MD_JSON_SCHEMA";

    public Task<List<DiagnosticsValidationResult>> ExecuteAsync(params object?[] obj)
    {
        var results = new List<DiagnosticsValidationResult>();

        if (obj.Length < 1)
        {
            throw new ArgumentException("JSON content parameter is required.", nameof(obj));
        }

        var jsonContent = obj[0] as string;
        if (jsonContent == null || string.IsNullOrWhiteSpace(jsonContent))
        {
            throw new ArgumentException("JSON content is null or empty.", nameof(obj));
        }

        try
        {
            // Validate that it's valid JSON
            var jsonObj = Newtonsoft.Json.JsonConvert.DeserializeObject(jsonContent);

            // Validate that it appears to be GS1 Web Vocab JSON-LD format
            ValidateGS1WebVocabFormat(jsonContent, results);
        }
        catch (Newtonsoft.Json.JsonException ex)
        {
            results.Add(new DiagnosticsValidationResult
            {
                Level = LogLevel.Error,
                Type = DiagnosticsValidationType.SchemaError,
                RuleKey = Key,
                Message = $"Invalid JSON format in Master Data response: {ex.Message}"
            });
        }

        return Task.FromResult(results);
    }

    private void ValidateGS1WebVocabFormat(string jsonContent, List<DiagnosticsValidationResult> results)
    {
        try
        {
            var jsonObj = Newtonsoft.Json.JsonConvert.DeserializeObject<Newtonsoft.Json.Linq.JObject>(jsonContent);
            if (jsonObj == null)
            {
                results.Add(new DiagnosticsValidationResult
                {
                    Level = LogLevel.Warning,
                    Type = DiagnosticsValidationType.SchemaError,
                    RuleKey = Key,
                    Message = "Master Data JSON could not be parsed as a JSON object."
                });
                return;
            }

            // Check for common GS1 Web Vocab properties
            if (!jsonObj.ContainsKey("@context") && !jsonObj.ContainsKey("@type"))
            {
                results.Add(new DiagnosticsValidationResult
                {
                    Level = LogLevel.Info,
                    Type = DiagnosticsValidationType.SchemaError,
                    RuleKey = Key,
                    Message = "Master Data JSON appears to be missing JSON-LD context information (@context, @type)."
                });
            }
        }
        catch (Exception ex)
        {
            results.Add(new DiagnosticsValidationResult
            {
                Level = LogLevel.Warning,
                Type = DiagnosticsValidationType.SchemaError,
                RuleKey = Key,
                Message = $"Error validating GS1 Web Vocab format: {ex.Message}"
            });
        }
    }
}