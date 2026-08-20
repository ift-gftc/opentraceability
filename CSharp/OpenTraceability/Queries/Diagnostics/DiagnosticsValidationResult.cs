namespace OpenTraceability.Queries.Diagnostics;

public enum DiagnosticsValidationType
{
    /// <summary>
    /// Indicates that the validation result is a general error.
    /// </summary>
    GeneralError,

    /// <summary>
    /// Indicates that the validation result is an HTTP error.
    /// </summary>
    HttpError,

    /// <summary>
    /// Indicates that the validation result is a schema error.
    /// </summary>
    SchemaError,

    /// <summary>
    /// Indicates that the validation result is a business rule error.
    /// </summary>
    BusinessRuleError
}

/// <summary>
/// A validation result returned from a diagnostics rule.
/// </summary>
public class DiagnosticsValidationResult
{
    /// <summary>
    /// The level of the validation result.
    /// </summary>
    public LogLevel Level { get; set; } = LogLevel.Error;

    /// <summary>
    /// The type of the validation result.
    /// </summary>
    public DiagnosticsValidationType Type { get; set; } = DiagnosticsValidationType.GeneralError;

    /// <summary>
    /// The key of the rule that returned the validation result.
    /// </summary>
    public string RuleKey { get; set; } = string.Empty;

    /// <summary>
    /// The message of the validation result.
    /// </summary>
    public string Message { get; set; } = string.Empty;

    /// <summary>
    /// JSON Pointer to the element that caused the result, for example "/bizStep". Empty for the
    /// document root, null when the result is not tied to one element.
    ///
    /// The message already names the location. This carries it separately so a caller can group,
    /// filter or link to it without parsing text.
    /// </summary>
    public string? InstanceLocation { get; set; }

    /// <summary>
    /// The value found at <see cref="InstanceLocation"/>, when it is a scalar.
    /// </summary>
    public string? Value { get; set; }
}