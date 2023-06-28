package opentraceability.utility

import java.io.BufferedReader
import java.io.InputStreamReader

object StaticData {
    /**
     * Reads static data files from embedded resources.
     *
     * @param path The name of the file in the OpenTraceability.Utility.Data folder.
     * @return The static data file contents as a string.
     * @throws Exception Throws an exception if it fails to find the embedded resource file.
     */
    fun readData(path: String): String {
        val result = StringBuilder()
        val inputStream = javaClass.classLoader.getResourceAsStream("opentraceability.utility.data.$path")
            ?: throw Exception("Failed to read static data from embedded resource at path $path")

        BufferedReader(InputStreamReader(inputStream)).use { reader ->
            var line: String? = reader.readLine()
            while (line != null) {
                result.append(line)
                line = reader.readLine()
            }
        }

        return result.toString()
    }
}
