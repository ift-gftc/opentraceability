using System.Collections.Generic;
using OpenTraceability.Utility;

namespace OpenTraceability.Queries.Diagnostics.Rules;

/// <summary>
/// Turns a schema validation result into diagnostics validations.
///
/// Every rule that validates against a JSON schema reports it the same way, so the mapping lives
/// here rather than being repeated at each call site.
/// </summary>
internal static class SchemaValidationResultMapper
{
    /// <summary>
    /// Adds one warning per offending element, and a final warning naming how many were left out
    /// when the validator capped the list.
    /// </summary>
    /// <param name="messagePrefix">
    /// Rule-specific lead-in, for example "EPCIS JSON schema validation warning".
    /// </param>
    public static void AddSchemaWarnings(
        List<DiagnosticsValidationResult> results,
        JsonSchemaValidationResult validation,
        string ruleKey,
        string messagePrefix)
    {
        foreach (JsonSchemaValidationError error in validation.Errors)
        {
            results.Add(new DiagnosticsValidationResult
            {
                Level = LogLevel.Warning,
                Type = DiagnosticsValidationType.SchemaError,
                RuleKey = ruleKey,
                InstanceLocation = error.InstanceLocation,
                Value = error.Value,
                Message = $"{messagePrefix}: {error}"
            });
        }

        if (validation.OmittedErrorCount > 0)
        {
            string plural = validation.OmittedErrorCount == 1 ? "error" : "errors";

            results.Add(new DiagnosticsValidationResult
            {
                Level = LogLevel.Warning,
                Type = DiagnosticsValidationType.SchemaError,
                RuleKey = ruleKey,
                Message = $"{messagePrefix}: ... and {validation.OmittedErrorCount} more {plural} not shown."
            });
        }
    }
}
