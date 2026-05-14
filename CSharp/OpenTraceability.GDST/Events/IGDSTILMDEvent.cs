namespace OpenTraceability.GDST.Events
{
    public interface IGDSTILMDEvent : IGDSTEvent
    {
        GDSTILMD? ILMD { get; }
    }
}
