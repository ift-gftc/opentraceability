package opentraceability;

public class OpenTraceabilityEventKDEProfile {
    public String XPath_V1 = "";
    public String XPath_V2 = "";
    public String JPath = "";

    public OpenTraceabilityEventKDEProfile() {}

    public OpenTraceabilityEventKDEProfile(String xPath_V1, String xPath_V2, String jPath) {
        XPath_V1 = xPath_V1;
        XPath_V2 = xPath_V2;
        JPath = jPath;
    }

    @Override
    public String toString() {
        return XPath_V1 + ":" + XPath_V2 + ":" + JPath;
    }
}