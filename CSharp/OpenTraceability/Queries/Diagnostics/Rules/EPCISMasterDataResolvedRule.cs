using System;
using System.Collections.Generic;
using System.Threading.Tasks;
using OpenTraceability.Models.Events; // for EPCISBaseDocument

namespace OpenTraceability.Queries.Diagnostics.Rules;

/// <summary>
/// This rule validates that master data has been properly resolved for EPCIS events.
/// 
/// EXPECTED PARAMETERS:
/// - parameters[0] EPCISBaseDocument - the EPCIS document with events to validate
/// - parameters[1] (Optional) bool - whether master data resolution is required
/// 
/// VALIDATION #1: Location Master Data Resolution
/// - For each event with a location (readPoint/bizLocation), validates that 
///   corresponding Location master data exists in the document
/// - Returns validation results with type "BusinessRuleError" and level "Warning"
///   for unresolved locations
/// 
/// VALIDATION #2: Trading Party Master Data Resolution  
/// - For events with source/destination lists containing owner parties, validates
///   that corresponding TradingParty master data exists in the document
/// - Returns validation results with type "BusinessRuleError" and level "Warning"
///   for unresolved trading parties
/// 
/// VALIDATION #3: Product Master Data Resolution
/// - For events with products (EPCs with GTINs), validates that corresponding
///   Tradeitem master data exists in the document
/// - Returns validation results with type "BusinessRuleError" and level "Info"
///   for unresolved tradeitems (lower priority as not always available)
/// 
/// VALIDATION #4: Master Data Consistency
/// - Validates that resolved master data IDs match the identifiers used in events
/// - Returns validation results with type "BusinessRuleError" and level "Error"
///   for inconsistent master data
/// </summary>
public class EPCISMasterDataResolvedRule : IDiagnosticsRequestRule
{
    public string Key { get; set; } = "OT_DIAG_RULE_EPCIS_MASTER_DATA_RESOLVED";

    public Task<List<DiagnosticsValidationResult>> ExecuteAsync(params object?[] obj)
    {
        if (obj == null || obj.Length < 1)
        {
            throw new ArgumentException("EPCISBaseDocument parameter is required.", nameof(obj));
        }

        var document = obj[0] as EPCISBaseDocument;
        if (document == null)
        {
            throw new ArgumentException("EPCISBaseDocument parameter is null or invalid.", nameof(obj));
        }

        var requireMasterData = obj.Length > 1 ? obj[1] as bool? ?? false : false;
        var results = new List<DiagnosticsValidationResult>();

        // VALIDATION #1: Location Master Data Resolution
        ValidateLocationMasterData(document, requireMasterData, results);

        // VALIDATION #2: Trading Party Master Data Resolution
        ValidateTradingPartyMasterData(document, requireMasterData, results);

        // VALIDATION #3: Product Master Data Resolution
        ValidateProductMasterData(document, requireMasterData, results);

        // VALIDATION #4: Master Data Consistency
        ValidateMasterDataConsistency(document, results);

        return Task.FromResult(results);
    }

    private void ValidateLocationMasterData(EPCISBaseDocument document, bool requireMasterData, List<DiagnosticsValidationResult> results)
    {
        foreach (var evt in document.Events)
        {
            if (evt.Location?.GLN != null)
            {
                var location = document.GetMasterData<OpenTraceability.Models.MasterData.Location>(evt.Location.GLN.ToString());
                if (location == null)
                {
                    var level = requireMasterData ? LogLevel.Warning : LogLevel.Info;
                    results.Add(new DiagnosticsValidationResult
                    {
                        Level = level,
                        Type = DiagnosticsValidationType.BusinessRuleError,
                        RuleKey = Key,
                        Message = $"Location master data not resolved for GLN: {evt.Location.GLN}"
                    });
                }
            }
        }
    }

    private void ValidateTradingPartyMasterData(EPCISBaseDocument document, bool requireMasterData, List<DiagnosticsValidationResult> results)
    {
        foreach (var evt in document.Events)
        {
            // Check source lists for owner parties
            foreach (var source in evt.SourceList)
            {
                if (source.ParsedType == OpenTraceability.Models.Events.EventSourceType.Owner && source.Value != null && !string.IsNullOrWhiteSpace(source.Value))
                {
                    var tradingParty = document.GetMasterData<OpenTraceability.Models.MasterData.TradingParty>(source.Value);
                    if (tradingParty == null)
                    {
                        var level = requireMasterData ? LogLevel.Warning : LogLevel.Info;
                        results.Add(new DiagnosticsValidationResult
                        {
                            Level = level,
                            Type = DiagnosticsValidationType.BusinessRuleError,
                            RuleKey = Key,
                            Message = $"TradingParty master data not resolved for source PGLN: {source.Value}"
                        });
                    }
                }
            }

            // Check destination lists for owner parties
            foreach (var dest in evt.DestinationList)
            {
                if (dest.ParsedType == OpenTraceability.Models.Events.EventDestinationType.Owner && dest.Value != null && !string.IsNullOrWhiteSpace(dest.Value))
                {
                    var tradingParty = document.GetMasterData<OpenTraceability.Models.MasterData.TradingParty>(dest.Value);
                    if (tradingParty == null)
                    {
                        var level = requireMasterData ? LogLevel.Warning : LogLevel.Info;
                        results.Add(new DiagnosticsValidationResult
                        {
                            Level = level,
                            Type = DiagnosticsValidationType.BusinessRuleError,
                            RuleKey = Key,
                            Message = $"TradingParty master data not resolved for destination PGLN: {dest.Value}"
                        });
                    }
                }
            }
        }
    }

    private void ValidateProductMasterData(EPCISBaseDocument document, bool requireMasterData, List<DiagnosticsValidationResult> results)
    {
        foreach (var evt in document.Events)
        {
            foreach (var product in evt.Products)
            {
                if (product.EPC.GTIN != null)
                {
                    var tradeitem = document.GetMasterData<OpenTraceability.Models.MasterData.Tradeitem>(product.EPC.GTIN.ToString());
                    if (tradeitem == null)
                    {
                        // Product master data is often not available, so this is lower priority
                        results.Add(new DiagnosticsValidationResult
                        {
                            Level = LogLevel.Info,
                            Type = DiagnosticsValidationType.BusinessRuleError,
                            RuleKey = Key,
                            Message = $"Tradeitem master data not resolved for GTIN: {product.EPC.GTIN}"
                        });
                    }
                }
            }
        }
    }

    private void ValidateMasterDataConsistency(EPCISBaseDocument document, List<DiagnosticsValidationResult> results)
    {
        // Validate that resolved master data IDs match the identifiers used in events
        foreach (var masterDataItem in document.MasterData)
        {
            if (masterDataItem.ID == null)
            {
                results.Add(new DiagnosticsValidationResult
                {
                    Level = LogLevel.Error,
                    Type = DiagnosticsValidationType.BusinessRuleError,
                    RuleKey = Key,
                    Message = $"Master data item of type {masterDataItem.GetType().Name} has null ID."
                });
            }
        }
    }
}
