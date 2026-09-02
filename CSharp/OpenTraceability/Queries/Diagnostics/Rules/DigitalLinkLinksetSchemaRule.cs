using System;
using System.Collections.Generic;
using System.Linq;
using System.Threading.Tasks;
using OpenTraceability.Models.MasterData;
using OpenTraceability.Utility;

namespace OpenTraceability.Queries.Diagnostics.Rules;

/// <summary>
/// This rule validates that the response from a GS1-Conformant resolver requested as an RFC 9264
/// linkset (<c>application/linkset+json</c>) conforms to the expected linkset shape.
///
/// EXPECTED PARAMETERS:
/// - parameters[0] string - the JSON response string to validate
///
/// VALIDATION #1: JSON Format Validation
/// - If the JSON is malformed or cannot be parsed, the rule returns a validation result with
///   type "SchemaError" and level "Error".
///
/// VALIDATION #2: Linkset Schema Validation
/// - Validates the JSON against the linkset JSON schema using JsonSchemaChecker, emitting
///   warning-level results for schema violations.
///
/// VALIDATION #3: Linkset Content Validation
/// - Confirms at least one anchor is present and that each declared link relation type carries a
///   non-empty target href, so a downstream resolve has something to act on.
/// </summary>
/// <remarks>
/// This is the <see cref="ResolverVersion.ResolverStandard_1_2_0"/> counterpart to
/// <see cref="DigitalLinkJsonSchemaRule"/> / <see cref="DigitalLinkResponseFoundRule"/>, which
/// validate the legacy flat digital link array used by <see cref="ResolverVersion.ResolverStandard_1_1_2"/>.
/// </remarks>
public class DigitalLinkLinksetSchemaRule : IDiagnosticsRequestRule
{
    public string Key { get; set; } = "OT_DIAG_RULE_DL_LINKSET_SCHEMA";

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
                Message = "Linkset response content is null or empty."
            });
            return results;
        }

        Linkset? linkset = ValidateLinksetFormat(jsonContent!, results);
        if (linkset == null)
        {
            return results;
        }

        await ValidateLinksetSchemaAsync(jsonContent!, results);
        ValidateLinksetContent(linkset, results);

        return results;
    }

    private Linkset? ValidateLinksetFormat(string jsonContent, List<DiagnosticsValidationResult> results)
    {
        try
        {
            return Newtonsoft.Json.JsonConvert.DeserializeObject<Linkset>(jsonContent);
        }
        catch (Newtonsoft.Json.JsonException ex)
        {
            results.Add(new DiagnosticsValidationResult
            {
                Level = LogLevel.Error,
                Type = DiagnosticsValidationType.SchemaError,
                RuleKey = Key,
                Message = $"Invalid JSON format in linkset response: {ex.Message}"
            });
            return null;
        }
    }

    private async Task ValidateLinksetSchemaAsync(string jsonContent, List<DiagnosticsValidationResult> results)
    {
        try
        {
            var schemaErrors = await JsonSchemaChecker.IsValidAsync(jsonContent, "Linkset");
            foreach (var error in schemaErrors)
            {
                results.Add(new DiagnosticsValidationResult
                {
                    Level = LogLevel.Warning,
                    Type = DiagnosticsValidationType.SchemaError,
                    RuleKey = Key,
                    Message = $"Linkset schema validation warning: {error}"
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
                Message = $"Linkset schema validation failed: {ex.Message}"
            });
        }
    }

    private void ValidateLinksetContent(Linkset linkset, List<DiagnosticsValidationResult> results)
    {
        if (linkset.linkset == null || linkset.linkset.Count == 0)
        {
            results.Add(new DiagnosticsValidationResult
            {
                Level = LogLevel.Error,
                Type = DiagnosticsValidationType.SchemaError,
                RuleKey = Key,
                Message = "Linkset response contains no anchors."
            });
            return;
        }

        // Every declared link relation type should carry a usable target href.
        foreach (var item in linkset.linkset)
        {
            foreach (var linkType in item.linkTypes.Keys)
            {
                foreach (var link in item.GetLinks(linkType))
                {
                    if (string.IsNullOrWhiteSpace(link.href))
                    {
                        results.Add(new DiagnosticsValidationResult
                        {
                            Level = LogLevel.Error,
                            Type = DiagnosticsValidationType.SchemaError,
                            RuleKey = Key,
                            Message = $"Linkset link of type '{linkType}' is missing its 'href' target."
                        });
                    }
                    else if (!Uri.IsWellFormedUriString(link.href, UriKind.Absolute))
                    {
                        results.Add(new DiagnosticsValidationResult
                        {
                            Level = LogLevel.Warning,
                            Type = DiagnosticsValidationType.SchemaError,
                            RuleKey = Key,
                            Message = $"Linkset link 'href' is not a valid absolute URL: {link.href}"
                        });
                    }
                }
            }
        }
    }
}
