using OpenTraceability.Models.Identifiers;
using OpenTraceability.Queries;

namespace DiagnosticsTool.Models.Requests;

public class QueryEventsRequest
{
    public EPCISQueryInterfaceOptions Options { get; set; } = new EPCISQueryInterfaceOptions();
    public EPCISQueryParameters Parameters { get; set; } = new EPCISQueryParameters();
}

public class TracebackRequest
{
    public DigitalLinkQueryOptions Options { get; set; } = new DigitalLinkQueryOptions();
    public string EPC { get; set; } = string.Empty;
    public EPCISQueryParameters? AdditionalParameters { get; set; }
    public bool ResolveMasterData { get; set; }
}
