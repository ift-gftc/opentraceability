package opentraceability.utility

import java.io.*
import java.util.*
import java.util.jar.JarInputStream
import kotlin.collections.HashMap
import kotlin.collections.set
import org.w3c.dom.Document
import java.net.URLClassLoader
import javax.xml.parsers.DocumentBuilder
import javax.xml.parsers.DocumentBuilderFactory
import javax.xml.parsers.ParserConfigurationException

class EmbeddedResourceLoader {
    var assemblyMap: MutableMap<String, ClassLoader> = mutableMapOf()

    private fun getAssembly(assemblyName: String): ClassLoader {
        var classLoader: ClassLoader? = assemblyMap[assemblyName]
        if (classLoader == null) {
            val assembly = EmbeddedResourceLoader::class.java.classLoader
            val resourceAsStream = assembly.getResourceAsStream("$assemblyName.jar")
            classLoader = URLClassLoader(arrayOf(assembly.getResource("$assemblyName.jar")), assembly)
            assemblyMap[assemblyName] = classLoader
        }
        return classLoader
    }

    fun readBytes(assemblyName: String, resourceName: String): ByteArray {
        var raw: ByteArray? = null
        try {
            val classLoader = getAssembly(assemblyName)
            val stream = classLoader.getResourceAsStream(resourceName)
                ?: throw Exception("Failed to find the resource in the assembly $assemblyName with the resource name $resourceName.")
            raw = stream.readBytes()
        } catch (ex: Exception) {
            println(ex)
            throw ex
        }
        return raw
    }

    fun readString(assemblyName: String, resourceName: String): String {
        var result = ""
        try {
            val classLoader = getAssembly(assemblyName)
            val stream = classLoader.getResourceAsStream(resourceName)
                ?: throw Exception("Failed to find the resource in the assembly $assemblyName with the resource name $resourceName.")
            result = stream.bufferedReader().use(BufferedReader::readText)
        } catch (ex: Exception) {
            println(ex)
            throw ex
        }
        return result
    }

    fun readXML(assemblyName: String, resourceName: String): Document {
        val xmlStr = readString(assemblyName, resourceName)
        return try {
            val factory: DocumentBuilderFactory = DocumentBuilderFactory.newInstance()
            val builder: DocumentBuilder = factory.newDocumentBuilder()
            builder.parse(ByteArrayInputStream(xmlStr.toByteArray()))
        } catch (ex: Exception) {
            println(ex)
            throw ex
        }
    }

    fun readStream(assemblyName: String, resourceName: String): InputStream? {
        var stream: InputStream? = null
        try {
            val classLoader = getAssembly(assemblyName)
            stream = classLoader.getResourceAsStream(resourceName)
        } catch (ex: Exception) {
            println(ex)
            throw ex
        }
        return stream
    }
}
