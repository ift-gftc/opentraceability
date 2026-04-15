using System;
using System.Collections.Generic;
using System.Linq;
using System.Net.Http.Headers;
using System.Threading.Tasks;
using OpenTraceability.Models.Events;
using OpenTraceability.Mappers;

namespace OpenTraceability.Queries.Diagnostics.Rules;

/// <summary>
/// This rule validates the HTTP request headers for EPCIS Query Interface requests.
/// 
/// EXPECTED PARAMETERS:
/// - parameters[0] HttpRequestHeaders - the request headers to validate
/// - parameters[1] EPCISVersion - the EPCIS version being used
/// - parameters[2] EPCISDataFormat - the data format being requested
/// 
/// VALIDATION #1: EPCIS Version Headers
/// - For EPCIS v1.2: Validates presence of GS1-EPCIS-Version, GS1-EPCIS-Min, 
///   GS1-EPCIS-Max, GS1-CBV-Version, GS1-CBV-XML-Format headers
/// - For EPCIS v2.0: Validates presence of corresponding v2.0 headers
/// 
/// VALIDATION #2: Accept Header Validation
/// - Validates that Accept header matches the requested format (application/xml 
///   or application/json)
/// 
/// VALIDATION #3: Host Header Validation
/// - Validates that Host header is present and not empty
/// </summary>
public class EPCISHttpRequestRule : IDiagnosticsRequestRule
{
    public string Key { get; set; } = "OT_DIAG_RULE_EPCIS_HTTP_REQUEST";

    public async Task<List<DiagnosticsValidationResult>> ExecuteAsync(params object[] obj)
    {
        if (obj == null)
        {
            throw new ArgumentException("Parameters array is null. Expected 3 parameters: HttpRequestHeaders, EPCISVersion, EPCISDataFormat.");
        }

        if (obj.Length < 3)
        {
            throw new ArgumentException("Insufficient parameters provided to EPCISHttpRequestRule. Expected 3 parameters: HttpRequestHeaders, EPCISVersion, EPCISDataFormat.");
        }

        var headers = obj[0] as HttpRequestHeaders;
        var version = obj[1] as EPCISVersion?;
        var format = obj[2] as EPCISDataFormat?;

        if (headers == null)
        {
            throw new ArgumentException("HttpRequestHeaders parameter is null or invalid.");
        }

        var results = new List<DiagnosticsValidationResult>();

        // VALIDATION #1: EPCIS Version Headers
        if (version == EPCISVersion.V1)
        {
            ValidateEPCISV1Headers(headers, results);
        }
        else if (version == EPCISVersion.V2)
        {
            ValidateEPCISV2Headers(headers, results);
        }

        // VALIDATION #2: Accept Header Validation
        ValidateAcceptHeader(headers, format, results);

        // Host header is populated dynamically by the client to allow proper SSL handling with redirects
        // checking for it before the request is executed is not necessary and forces the caller to manaully
        // set the host which causes further downstream issues with SSL hanlding.
        //// VALIDATION #3: Host Header Validation
        //ValidateHostHeader(headers, results);

        return results;
    }

    private void ValidateEPCISV1Headers(HttpRequestHeaders headers, List<DiagnosticsValidationResult> results)
    {
        var requiredHeaders = new[] { "GS1-EPCIS-Version", "GS1-EPCIS-Min", "GS1-EPCIS-Max", "GS1-CBV-Version", "GS1-CBV-XML-Format" };
        foreach (var headerName in requiredHeaders)
        {
            if (!headers.Contains(headerName))
            {
                results.Add(new DiagnosticsValidationResult
                {
                    Level = LogLevel.Error,
                    Type = DiagnosticsValidationType.HttpError,
                    RuleKey = Key,
                    Message = $"Missing required EPCIS v1.2 header: {headerName}"
                });
            }
        }
    }

    private void ValidateEPCISV2Headers(HttpRequestHeaders headers, List<DiagnosticsValidationResult> results)
    {
        var requiredHeaders = new[] { "GS1-EPCIS-Version", "GS1-EPCIS-Min", "GS1-EPCIS-Max", "GS1-CBV-Version", "GS1-CBV-XML-Format" };
        foreach (var headerName in requiredHeaders)
        {
            if (!headers.Contains(headerName))
            {
                results.Add(new DiagnosticsValidationResult
                {
                    Level = LogLevel.Error,
                    Type = DiagnosticsValidationType.HttpError,
                    RuleKey = Key,
                    Message = $"Missing required EPCIS v2.0 header: {headerName}"
                });
            }
        }
    }

    private void ValidateAcceptHeader(HttpRequestHeaders headers, EPCISDataFormat? format, List<DiagnosticsValidationResult> results)
    {
        if (headers.Accept == null || !headers.Accept.Any())
        {
            results.Add(new DiagnosticsValidationResult
            {
                Level = LogLevel.Error,
                Type = DiagnosticsValidationType.HttpError,
                RuleKey = Key,
                Message = "Accept header is missing."
            });
            return;
        }

        var expectedMediaType = format == EPCISDataFormat.XML ? "application/xml" : "application/json";
        if (!headers.Accept.Any(h => h.MediaType?.Contains(expectedMediaType) == true))
        {
            results.Add(new DiagnosticsValidationResult
            {
                Level = LogLevel.Warning,
                Type = DiagnosticsValidationType.HttpError,
                RuleKey = Key,
                Message = $"Accept header does not contain expected media type: {expectedMediaType}"
            });
        }
    }

    private void ValidateHostHeader(HttpRequestHeaders headers, List<DiagnosticsValidationResult> results)
    {
        if (string.IsNullOrWhiteSpace(headers.Host))
        {
            results.Add(new DiagnosticsValidationResult
            {
                Level = LogLevel.Error,
                Type = DiagnosticsValidationType.HttpError,
                RuleKey = Key,
                Message = "Host header is missing or empty."
            });
        }
    }
}