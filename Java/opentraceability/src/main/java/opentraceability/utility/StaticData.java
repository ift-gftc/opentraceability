package opentraceability.utility;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;

public class StaticData {

    /**
     * Reads static data files from embedded resources.
     *
     * @param path The name of the file in the OpenTraceability.Utility.Data folder.
     * @return The static data file contents as a string.
     * @throws Exception Throws an exception if it fails to find the embedded resource file.
     */
    public static String readData(String path) throws Exception {
        EmbeddedResourceLoader loader = new EmbeddedResourceLoader();
        String result = loader.readString(StaticData.class, path);
        return result;
    }
}