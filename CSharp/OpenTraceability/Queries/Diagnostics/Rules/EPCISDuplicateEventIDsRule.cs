using System;
using System.Collections.Generic;
using System.Linq;
using System.Threading.Tasks;
using OpenTraceability.Queries.Diagnostics;

namespace OpenTraceability.Queries.Diagnostics.Rules;

/// <summary>
/// This rule validates duplicate and missing event IDs within an EPCIS document.
/// 
/// EXPECTED PARAMETERS:
/// - parameters[0] EPCISBaseDocument - the EPCIS document to validate (required)
/// 
/// VALIDATION #1: Duplicate Event IDs
/// - Scans events that have a non-null EventID and groups by the string value
/// - For every EventID that appears more than once an error is returned
/// 
/// VALIDATION #2: Missing Event IDs
/// - Counts events that have a null EventID and returns a single warning if any exist
/// </summary>
public class EPCISDuplicateEventIDsRule : IDiagnosticsRequestRule
{
    public string Key { get; set; } = "OT_DIAG_RULE_EPCIS_DUPLICATE_EVENT_IDS";

    public Task<List<DiagnosticsValidationResult>> ExecuteAsync(params object?[] obj)
    {
        // Parameter validation (throwing exceptions instead of returning diagnostics results)
        if (obj == null || obj.Length != 1)
        {
            throw new ArgumentException("Expected exactly one parameter of type EPCISBaseDocument.");
        }

        if (obj[0] is not OpenTraceability.Models.Events.EPCISBaseDocument document)
        {
            throw new ArgumentException("Parameter 0 must be of type EPCISBaseDocument.");
        }

        var results = new List<DiagnosticsValidationResult>();

        ValidateDuplicateEventIds(document, results);
        ValidateMissingEventIds(document, results);

        return Task.FromResult(results);
    }

    private void ValidateDuplicateEventIds(OpenTraceability.Models.Events.EPCISBaseDocument document, List<DiagnosticsValidationResult> results)
    {
        // Group by EventID string where EventID != null
        var duplicateIds = document.Events
            .Where(e => e.EventID != null)
            .GroupBy(e => e.EventID!.ToString())
            .Where(g => g.Count() > 1)
            .Select(g => g.Key)
            .ToList();

        foreach (var id in duplicateIds)
        {
            results.Add(new DiagnosticsValidationResult
            {
                Level = LogLevel.Error,
                Type = DiagnosticsValidationType.BusinessRuleError,
                RuleKey = Key,
                Message = $"Duplicate event ID found within document: {id}"
            });
        }
    }

    private void ValidateMissingEventIds(OpenTraceability.Models.Events.EPCISBaseDocument document, List<DiagnosticsValidationResult> results)
    {
        int missingCount = document.Events.Count(e => e.EventID == null);
        if (missingCount > 0)
        {
            results.Add(new DiagnosticsValidationResult
            {
                Level = LogLevel.Warning,
                Type = DiagnosticsValidationType.SchemaError,
                RuleKey = Key,
                Message = $"{missingCount} event(s) are missing required eventID field."
            });
        }
    }
}
