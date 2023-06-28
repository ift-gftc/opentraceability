package opentraceability.utility;

public class URLHelper
{
    public static String Combine(String url1, String url2)
    {
        String url = TrimEndSlash(url1) + "/" + TrimBeginningSlash(url2);
        return url;
    }

    public static String TrimEndSlash(String url1)
    {
        if (url1.endsWith("/"))
        {
            url1 = url1.substring(0, url1.length() - 1);
        }
        return url1;
    }

    public static String TrimBeginningSlash(String url1)
    {
        if (url1.startsWith("/"))
        {
            url1 = url1.substring(1);
        }
        return url1;
    }

    public static String TrimSlashes(String url1)
    {
        return TrimBeginningSlash(TrimEndSlash(url1));
    }
}
