namespace OpenTraceability.TestServer.Core.Modules
{
    /// <summary>
    /// The GDST 2.0 modules a server can be configured to support. Core is always included.
    /// Wildcaught and Aquaculture both imply Seafood.
    /// </summary>
    public enum GdstModule
    {
        Core = 0,
        Seafood = 1,
        Wildcaught = 2,
        Aquaculture = 3
    }
}
