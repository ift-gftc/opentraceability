using System;
using System.Collections.Generic;
using System.Net.Http;
using System.Threading.Tasks;

namespace OpenTraceability.Queries.Diagnostics;

/// <summary>
/// The type of request that was performed when querying for traceability data.
/// </summary>
public enum RequestType
{
    /// <summary>
    /// The request was performed against a Digital Link.
    /// </summary>  
    DigitalLink,

    /// <summary>
    /// The request was performed against an EPCIS Query Interface.
    /// </summary>
    EPCIS,

    /// <summary>
    /// The request was performed against a Master Data Resolver.
    /// </summary>
    MasterData
}

public class DiagnosticsRequest
{
    /// <summary>
    /// The title of the request.
    /// </summary>
    public string Title { get; set; } = string.Empty;

    /// <summary>
    /// The start of the request.
    /// </summary>
    public DateTime Start { get; set; }

    /// <summary>
    /// The end of the request.
    /// </summary>
    public DateTime End { get; set; }

    /// <summary>
    /// The options that were used to perform the request.
    /// </summary>
    public object? RequestOptions { get; set; } = null;

    /// <summary>
    /// The HTTP request used in the request, if applicable.
    /// </summary>
    public HttpRequestMessage? HttpRequest { get; set; } = null;

    /// <summary>
    /// The HTTP response received from the request, if applicable.
    /// </summary>
    public HttpResponseMessage? HttpResponse { get; set; } = null;

    /// <summary>
    /// The body of the response received from the request, if applicable.
    /// </summary>
    public string ResponseBody { get; set; } = string.Empty;

    /// <summary>
    /// The list of validations that were performed against the singular request.
    /// </summary>
    public List<DiagnosticsValidationResult> Validations { get; set; } = new List<DiagnosticsValidationResult>();

    /// <summary>
    /// Executes a rule against the request.
    /// </summary>
    /// <typeparam name="T">The type of the rule to execute.</typeparam>
    /// <param name="parameters">The parameters to pass to the rule.</param>
    /// <exception cref="Exception"></exception>
    public async Task ExecuteRuleAsync<T>(params object[] parameters) where T : IDiagnosticsRequestRule
    {
        // Construct the rule
        var rule = Activator.CreateInstance(typeof(T)) as IDiagnosticsRequestRule;
        if (rule == null)
        {
            throw new Exception($"Rule {typeof(T).Name} not found.");
        }

        // Execute the rule
        var results = await rule.ExecuteAsync(parameters);
        Validations.AddRange(results);
    }

    public void AddException(Exception ex)
    {
        var validation = new DiagnosticsValidationResult
        {
            Level = LogLevel.Error,
            Message = ex.Message,
            RuleKey = "EXCEPTION",
            Type = DiagnosticsValidationType.GeneralError
        };
        Validations.Add(validation);
    }
}
