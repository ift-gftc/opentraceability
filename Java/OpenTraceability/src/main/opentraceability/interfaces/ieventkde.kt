package interfaces

import com.fasterxml.jackson.core.JsonToken
import java.lang.reflect.Type
import java.util.concurrent.ConcurrentHashMap
import javax.xml.bind.annotation.XmlElement

interface IEventKDE {

    companion object {
    }

    var RegisteredKDEs: ConcurrentHashMap<String, Any>

    fun <T> RegisterKDE(ns: String, name: String) {
        val key: String = "$ns:$name"

        TODO("Not yet implemented")

        if (!RegisteredKDEs.contains(key)) {
            //RegisteredKDEs.put(key, T::class.java)
        } else {
            var fullName = "" //RegisteredKDEs[key].toString()
            throw Exception("The KDE $key is already registered with type $fullName")
        }

    }

    fun InitializeKDE(ns: String, name: String): IEventKDE? {
        var kde: IEventKDE? = null

        val key: String = "$ns:$name"

        TODO("Not yet implemented")

        var kdeType = RegisteredKDEs.getValue(key)

        if (kdeType != null) {
            kde = IEventKDE::class.java.newInstance()
        }

        if (kde != null) {
            kde.Namespace = ns;
            kde.Name = name;
        }

        return kde
    }

    var Namespace: String

    var Name: String

    var ValueType: Type

    fun SetFromJson(json: JsonToken): Void

    fun GetJson(): JsonToken?

    fun SetFromXml(xml: XmlElement): Void

    fun GetXml(): XmlElement?
}
