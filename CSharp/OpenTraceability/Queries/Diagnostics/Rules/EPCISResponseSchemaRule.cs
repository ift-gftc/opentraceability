using System;
using System.Collections.Generic;
using System.Threading.Tasks;
using System.Xml;
using System.Xml.Linq;
using Newtonsoft.Json;
using OpenTraceability.Mappers;
using OpenTraceability.Models.Events;
using OpenTraceability.Utility;

namespace OpenTraceability.Queries.Diagnostics.Rules;

public class EPCISResponseSchemaRule : IDiagnosticsRequestRule
{
    public string Key { get; set; } = "OT_DIAG_RULE_EPCIS_RESPONSE_SCHEMA";

    public async Task<List<DiagnosticsValidationResult>> ExecuteAsync(params object?[] obj)
    {
        if (obj == null || obj.Length < 3)
        {
            throw new ArgumentException("EPCISResponseSchemaRule requires 3 parameters: content(string), format(EPCISDataFormat), version(EPCISVersion).", nameof(obj));
        }
        if (obj[0] != null && obj[0] is not string)
        {
            throw new ArgumentException("Parameter 0 must be a string containing the EPCIS response content.", nameof(obj));
        }
        if (obj[1] is not EPCISDataFormat format)
        {
            throw new ArgumentException("Parameter 1 must be an EPCISDataFormat enum value.", nameof(obj));
        }
        if (obj[2] is not EPCISVersion version)
        {
            throw new ArgumentException("Parameter 2 must be an EPCISVersion enum value.", nameof(obj));
        }

        var results = new List<DiagnosticsValidationResult>();
        var content = (string?)obj[0];

        if (string.IsNullOrWhiteSpace(content))
        {
            results.Add(new DiagnosticsValidationResult
            {
                Level = LogLevel.Error,
                Type = DiagnosticsValidationType.SchemaError,
                RuleKey = Key,
                Message = "EPCIS response content is null or empty."
            });
            return results;
        }

        ValidateContentFormat(content!, format, results);

        bool hasFatalFormatError = results.Exists(r => r.Level == LogLevel.Error && r.Type == DiagnosticsValidationType.SchemaError && (r.Message.StartsWith("Invalid JSON") || r.Message.StartsWith("Invalid XML")));
        if (!hasFatalFormatError)
        {
            await ValidateSchemaComplianceAsync(content!, format, version, results);
        }

        return results;
    }

    private void ValidateContentFormat(string content, EPCISDataFormat format, List<DiagnosticsValidationResult> results)
    {
        if (format == EPCISDataFormat.JSON)
        {
            try
            {
                JsonConvert.DeserializeObject(content);
            }
            catch (JsonException ex)
            {
                results.Add(new DiagnosticsValidationResult
                {
                    Level = LogLevel.Error,
                    Type = DiagnosticsValidationType.SchemaError,
                    RuleKey = Key,
                    Message = $"Invalid JSON content in EPCIS response: {ex.Message}"
                });
            }
        }
        else if (format == EPCISDataFormat.XML)
        {
            try
            {
                var doc = new XmlDocument();
                doc.LoadXml(content);
            }
            catch (XmlException ex)
            {
                results.Add(new DiagnosticsValidationResult
                {
                    Level = LogLevel.Error,
                    Type = DiagnosticsValidationType.SchemaError,
                    RuleKey = Key,
                    Message = $"Invalid XML content in EPCIS response: {ex.Message}"
                });
            }
        }
    }

    private async Task ValidateSchemaComplianceAsync(string content, EPCISDataFormat format, EPCISVersion version, List<DiagnosticsValidationResult> results)
    {
        try
        {
            if (format == EPCISDataFormat.JSON)
            {
                if (version == EPCISVersion.V2)
                {
                    var schemaErrors = await JsonSchemaChecker.IsValidAsync(content, "https://ref.gs1.org/standards/epcis/epcis-json-schema.json");
                    foreach (var err in schemaErrors)
                    {
                        results.Add(new DiagnosticsValidationResult
                        {
                            Level = LogLevel.Warning,
                            Type = DiagnosticsValidationType.SchemaError,
                            RuleKey = Key,
                            Message = $"EPCIS JSON schema validation warning: {err}"
                        });
                    }
                }
            }
            else if (format == EPCISDataFormat.XML)
            {
                string schemaURL = version == EPCISVersion.V1
                    ? "https://raw.githubusercontent.com/ift-gftc/doc.gdst/master/schemas/xml/epcis_1_2/EPCglobal-epcis-query-1_2.xsd"
                    : "https://ref.gs1.org/standards/epcis/epcglobal-epcis-query-2_0.xsd";

                try
                {
                    var xdoc = XDocument.Parse(content);
                    var checker = new XmlSchemaChecker();
                    if (!checker.Validate(xdoc, schemaURL, out string error) && !string.IsNullOrWhiteSpace(error))
                    {
                        foreach (var line in error.Split(new[] { '\r', '\n' }, StringSplitOptions.RemoveEmptyEntries))
                        {
                            results.Add(new DiagnosticsValidationResult
                            {
                                Level = LogLevel.Warning,
                                Type = DiagnosticsValidationType.SchemaError,
                                RuleKey = Key,
                                Message = $"EPCIS XML schema validation warning: {line.Trim()}"
                            });
                        }
                    }
                }
                catch (Exception ex)
                {
                    results.Add(new DiagnosticsValidationResult
                    {
                        Level = LogLevel.Warning,
                        Type = DiagnosticsValidationType.SchemaError,
                        RuleKey = Key,
                        Message = $"EPCIS XML schema validation warning: {ex.Message}"
                    });
                }
            }
        }
        catch (Exception ex)
        {
            results.Add(new DiagnosticsValidationResult
            {
                Level = LogLevel.Warning,
                Type = DiagnosticsValidationType.GeneralError,
                RuleKey = Key,
                Message = $"Error validating EPCIS content: {ex.Message}"
            });
        }
    }
}