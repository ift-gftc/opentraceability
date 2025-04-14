namespace OpenTraceability.MSC.Events
{
    /// <summary>
    /// This is a GDST event that has an ILMD property.
    /// </summary>
    public interface IMSCILMDEvent : IMSCEvent
    {
        MSCILMD? ILMD { get; }
    }
}