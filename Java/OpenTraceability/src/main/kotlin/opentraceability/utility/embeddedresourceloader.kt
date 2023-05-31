package utility

import org.w3c.dom.Document
import java.util.stream.Stream

import java.io.ByteArrayOutputStream
import java.io.InputStreamReader
import java.nio.charset.StandardCharsets
import java.util.*

class EmbeddedResourceLoader {
    private val assemblyMap: MutableMap<String, Assembly> = HashMap()

    fun getAssembly(assemblyName: String): Assembly {
        return assemblyMap.getOrPut(assemblyName) {
            Assembly.Load(assemblyName)
        }
    }

    fun readBytes(assemblyName: String, resourceName: String): ByteArray {
        try {
            val assembly = getAssembly(assemblyName)
            val stream = assembly.GetManifestResourceStream(resourceName)
                ?: throw Exception("Failed to find the resource in the assembly $assemblyName with the resource name $resourceName.")
            val buffer = ByteArray(4096)
            val output = ByteArrayOutputStream()
            var bytesRead: Int
            while (stream.read(buffer).also { bytesRead = it } != -1) {
                output.write(buffer, 0, bytesRead)
            }
            return output.toByteArray()
        } catch (ex: Exception) {
            OTLogger.error(ex)
            throw ex
        }
    }

    fun readString(assemblyName: String, resourceName: String): String {
        try {
            val assembly = getAssembly(assemblyName)
            val stream = assembly.GetManifestResourceStream(resourceName)
                ?: throw Exception("Failed to find the resource in the assembly $assemblyName with the resource name $resourceName.")
            val reader = InputStreamReader(stream, StandardCharsets.UTF_8)
            return reader.readText()
        } catch (ex: Exception) {
            OTLogger.error(ex)
            throw ex
        }
    }

    fun readXML(assemblyName: String, resourceName: String): Document {
        val xmlStr = readString(assemblyName, resourceName)
        return Document.Parse(xmlStr)
    }

    fun readStream(assemblyName: String, resourceName: String): Stream? {
        try {
            val assembly = getAssembly(assemblyName)
            return assembly.GetManifestResourceStream(resourceName)
        } catch (ex: Exception) {
            OTLogger.error(ex)
            throw ex
        }
    }
}
