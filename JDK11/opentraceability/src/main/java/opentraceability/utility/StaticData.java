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
        StringBuilder result = new StringBuilder();
        InputStream inputStream = StaticData.class.getClassLoader().getResourceAsStream("opentraceability.utility.data." + path);

        if (inputStream == null) {
            throw new Exception("Failed to read static data from embedded resource at path " + path);
        }

        BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
        String line = reader.readLine();

        while (line != null) {
            result.append(line);
            line = reader.readLine();
        }

        return result.toString();
    }
}