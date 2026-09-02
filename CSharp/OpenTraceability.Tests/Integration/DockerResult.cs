namespace OpenTraceability.Tests.Integration;

/// <summary>
/// The captured result of a single docker CLI invocation.
/// </summary>
/// <remarks>
/// Both streams are kept because the docker CLI splits its output in a way that matters to the
/// image probes: buildx writes build progress (including the "transferring context" line) to
/// standard error, while commands such as "run --entrypoint find" write their payload to standard
/// output. A caller asserting on either one needs to know which stream it came from.
/// </remarks>
/// <param name="ExitCode">The exit code the docker process returned.</param>
/// <param name="StdOut">Everything the process wrote to standard output.</param>
/// <param name="StdErr">Everything the process wrote to standard error.</param>
internal sealed record DockerResult(int ExitCode, string StdOut, string StdErr);
