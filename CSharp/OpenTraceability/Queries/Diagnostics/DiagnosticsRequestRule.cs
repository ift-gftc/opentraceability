using System;
using System.Collections.Generic;
using System.Data;
using System.Threading.Tasks;

namespace OpenTraceability.Queries.Diagnostics;

public interface IDiagnosticsRequestRule
{
    /// <summary>
    /// The key of the rule.
    /// </summary>
    string Key { get; set; }

    /// <summary>
    /// The method to execute the rule and return one or more validation results.
    /// </summary>
    Task<List<DiagnosticsValidationResult>> ExecuteAsync(params object?[] obj);
}
