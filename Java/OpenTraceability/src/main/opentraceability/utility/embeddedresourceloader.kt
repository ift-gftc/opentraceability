package utility

import com.intellij.psi.xml.XmlDocument
import java.util.stream.Stream

class EmbeddedResourceLoader {

    var m_assemblyMap: MutableMap<String, String> = mutableMapOf()

    constructor() {
        m_assemblyMap = mutableMapOf()
    }


    fun ReadBytes(assemblyName: String, resourceName: String): ArrayList<Byte> {
        TODO("Not yet implemented")
    }

    fun ReadString(assemblyName: String, resourceName: String): String {
        TODO("Not yet implemented")
    }

    fun ReadXML(assemblyName: String, resourceName: String): XmlDocument {
        TODO("Not yet implemented")
    }

    fun ReadStream(assemblyName: String, resourceName: String): String? {
        TODO("Not yet implemented")
    }

    internal fun GetAssembly(assemblyName: String): String {
        TODO("Not yet implemented")
    }
}
