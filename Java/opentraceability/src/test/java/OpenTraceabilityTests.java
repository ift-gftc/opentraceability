import opentraceability.utility.EmbeddedResourceLoader;

public class OpenTraceabilityTests
{
    public static String LoadTestData(String name)
    {
        EmbeddedResourceLoader loader = new EmbeddedResourceLoader();
        String str = loader.readString(OpenTraceabilityTests.class, "/tests/" + name);
        return str;
    }
}
