package utility

import java.io.ByteArrayOutputStream
import java.io.IOException
import java.io.InputStream
import java.io.InputStreamReader
import java.io.BufferedReader
import java.nio.charset.StandardCharsets
import org.w3c.dom.Document
import org.w3c.dom.Element
import org.w3c.dom.Node
import org.w3c.dom.NodeList
import javax.xml.parsers.DocumentBuilderFactory

class EmbeddedResourceLoader {
    private val assemblyMap: MutableMap<String, ClassLoader> = HashMap()

    private fun getClassLoader(assemblyName: String): ClassLoader {
        return assemblyMap.getOrPut(assemblyName) { Thread.currentThread().contextClassLoader }
    }

    private fun readBytesFromStream(stream: InputStream): ByteArray {
        val buffer = ByteArray(8192)
        val outputStream = ByteArrayOutputStream()
        var bytesRead: Int
        try {
            while (stream.read(buffer).also { bytesRead = it } != -1) {
                outputStream.write(buffer, 0, bytesRead)
            }
        } catch (e: IOException) {
            e.printStackTrace()
        } finally {
            try {
                stream.close()
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
        return outputStream.toByteArray()
    }

    fun readBytes(assemblyName: String, resourceName: String): ByteArray {
        try {
            val classLoader = getClassLoader(assemblyName)
            val stream = classLoader.getResourceAsStream(resourceName)
                ?: throw Exception("Failed to find the resource in the assembly $assemblyName with the resource name $resourceName.")
            return readBytesFromStream(stream)
        } catch (ex: Exception) {
            ex.printStackTrace()
            throw ex
        }
    }

    fun readString(assemblyName: String, resourceName: String): String {
        try {
            val classLoader = getClassLoader(assemblyName)
            val stream = classLoader.getResourceAsStream(resourceName)
                ?: throw Exception("Failed to find the resource in the assembly $assemblyName with the resource name $resourceName.")
            val reader = BufferedReader(InputStreamReader(stream, StandardCharsets.UTF_8))
            return reader.use { it.readText() }
        } catch (ex: Exception) {
            ex.printStackTrace()
            throw ex
        }
    }

    fun readXML(assemblyName: String, resourceName: String): Document {
        val xmlStr = readString(assemblyName, resourceName)
        val factory = DocumentBuilderFactory.newInstance()
        val builder = factory.newDocumentBuilder()
        return builder.parse(xmlStr.byteInputStream())
    }

    fun readStream(assemblyName: String, resourceName: String): InputStream? {
        try {
            val classLoader = getClassLoader(assemblyName)
            return classLoader.getResourceAsStream(resourceName)
        } catch (ex: Exception) {
            ex.printStackTrace()
            throw ex
        }
    }
}

