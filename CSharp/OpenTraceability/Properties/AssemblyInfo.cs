using System.Runtime.CompilerServices;

// Expose internal members (e.g. EventHashGenerator.GeneratePreHashString and the
// internal XML mapper) to the test project for white-box testing.
[assembly: InternalsVisibleTo("OpenTraceability.Tests")]
