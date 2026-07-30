using System;
using System.Collections.Generic;
using System.Threading.Tasks;

namespace OpenTraceability.Queries.Diagnostics.Rules;

/// <summary>
/// This rule validates that the response from the Master Data Resolver matches what was queried.
/// 
/// EXPECTED PARAMETERS:
/// - parameters[0] the identifier of the master data item that was queried.
/// - parameters[1] the returned results.
/// 
/// VALIDATION #1: If the parameters[0] is PGLN: 
/// - If parameters[1] is NULL or NOT a TradingParty, or the PGLN of the TradingParty does not 
///   match the parameters[0], then the rule will return a validation result with a type of 
///   "BusinessRuleError" and a log level of "Error".
/// 
/// VALIDATION #2: If the parameters[0] is GTIN:
/// - If parameters[1] is NULL or NOT a Tradeitem, or the GTIN of the Tradeitem does not match 
///   the parameters[0], then the rule will return a validation result with a type of 
///   "BusinessRuleError" and a log level of "Error".
/// 
/// VALIDATION #3: If the parameters[0] is GLN:
/// - If parameters[1] is NULL or NOT a Location, or the GLN of the Location does not match 
///   the parameters[0], then the rule will return a validation result with a type of 
///   "BusinessRuleError" and a log level of "Error".
/// </summary>
public class MasterDataValidResponseRule : IDiagnosticsRequestRule
{
    public string Key { get; set; } = "OT_DIAG_RULE_MD_VALID_RESPONSE";

    public Task<List<DiagnosticsValidationResult>> ExecuteAsync(params object?[] obj)
    {
        var results = new List<DiagnosticsValidationResult>();

        if (obj.Length < 2)
        {
            throw new ArgumentException("Insufficient parameters. Expected identifier and returned master data item.");
        }

        var identifier = obj[0];
        var item = obj[1];

        if (identifier == null)
        {
            throw new ArgumentException("Identifier parameter is null.", nameof(obj));
        }

        if (item == null)
        {
            results.Add(new DiagnosticsValidationResult
            {
                Level = LogLevel.Warning,
                Type = DiagnosticsValidationType.BusinessRuleError,
                RuleKey = Key,
                Message = "Master data item returned is null."
            });
            return Task.FromResult(results);
        }

        // VALIDATION #1: PGLN validation
        if (identifier is OpenTraceability.Models.Identifiers.PGLN pgln)
        {
            ValidatePGLNResponse(pgln, item, results);
        }
        // VALIDATION #2: GTIN validation  
        else if (identifier is OpenTraceability.Models.Identifiers.GTIN gtin)
        {
            ValidateGTINResponse(gtin, item, results);
        }
        // VALIDATION #3: GLN validation
        else if (identifier is OpenTraceability.Models.Identifiers.GLN gln)
        {
            ValidateGLNResponse(gln, item, results);
        }
        else
        {
            results.Add(new DiagnosticsValidationResult
            {
                Level = LogLevel.Warning,
                Type = DiagnosticsValidationType.GeneralError,
                RuleKey = Key,
                Message = $"Unsupported identifier type: {identifier.GetType().Name}"
            });
        }

        return Task.FromResult(results);
    }

    private void ValidatePGLNResponse(OpenTraceability.Models.Identifiers.PGLN pgln, object item, List<DiagnosticsValidationResult> results)
    {
        var tradingParty = item as OpenTraceability.Models.MasterData.TradingParty;
        if (tradingParty == null)
        {
            results.Add(new DiagnosticsValidationResult
            {
                Level = LogLevel.Error,
                Type = DiagnosticsValidationType.BusinessRuleError,
                RuleKey = Key,
                Message = "Expected TradingParty master data for PGLN query, but received different type."
            });
            return;
        }

        if (tradingParty.PGLN == null || !tradingParty.PGLN.Equals(pgln))
        {
            results.Add(new DiagnosticsValidationResult
            {
                Level = LogLevel.Error,
                Type = DiagnosticsValidationType.BusinessRuleError,
                RuleKey = Key,
                Message = $"TradingParty PGLN ({tradingParty.PGLN}) does not match queried PGLN ({pgln})."
            });
        }
    }

    private void ValidateGTINResponse(OpenTraceability.Models.Identifiers.GTIN gtin, object item, List<DiagnosticsValidationResult> results)
    {
        var tradeitem = item as OpenTraceability.Models.MasterData.Tradeitem;
        if (tradeitem == null)
        {
            results.Add(new DiagnosticsValidationResult
            {
                Level = LogLevel.Error,
                Type = DiagnosticsValidationType.BusinessRuleError,
                RuleKey = Key,
                Message = "Expected Tradeitem master data for GTIN query, but received different type."
            });
            return;
        }

        if (tradeitem.GTIN == null || !tradeitem.GTIN.Equals(gtin))
        {
            results.Add(new DiagnosticsValidationResult
            {
                Level = LogLevel.Error,
                Type = DiagnosticsValidationType.BusinessRuleError,
                RuleKey = Key,
                Message = $"Tradeitem GTIN ({tradeitem.GTIN}) does not match queried GTIN ({gtin})."
            });
        }
    }

    private void ValidateGLNResponse(OpenTraceability.Models.Identifiers.GLN gln, object item, List<DiagnosticsValidationResult> results)
    {
        var location = item as OpenTraceability.Models.MasterData.Location;
        if (location == null)
        {
            results.Add(new DiagnosticsValidationResult
            {
                Level = LogLevel.Error,
                Type = DiagnosticsValidationType.BusinessRuleError,
                RuleKey = Key,
                Message = "Expected Location master data for GLN query, but received different type."
            });
            return;
        }

        if (location.GLN == null || !location.GLN.Equals(gln))
        {
            results.Add(new DiagnosticsValidationResult
            {
                Level = LogLevel.Error,
                Type = DiagnosticsValidationType.BusinessRuleError,
                RuleKey = Key,
                Message = $"Location GLN ({location.GLN}) does not match queried GLN ({gln})."
            });
        }
    }
}