package interfaces

import com.fasterxml.jackson.core.JsonToken
import org.json.simple.JSONObject
import org.w3c.dom.Element
import java.lang.reflect.Type
import java.util.concurrent.ConcurrentHashMap
import javax.xml.bind.annotation.XmlElement

interface IEventKDE {

    companion object {
        var RegisteredKDEs : MutableMap<String, Type> = mutableMapOf()

        inline fun <reified T> RegisterKDE(ns: String, name: String) {
            val key: String = "$ns:$name"

            if (!RegisteredKDEs.contains(key)) {
                RegisteredKDEs.put(key, T::class.java)
            } else {
                var fullName = RegisteredKDEs[key].toString()
                throw Exception("The KDE $key is already registered with type $fullName")
            }
        }

        fun InitializeKDE(ns: String, name: String): IEventKDE? {
            var kde: IEventKDE? = null

            val key: String = "$ns:$name"

            var kdeType = RegisteredKDEs.getValue(key)

            if (kdeType != null) {
                kde = Class.forName(key) as IEventKDE
            }

            if (kde != null) {
                kde.Namespace = ns;
                kde.Name = name;
            }

            return kde
        }
    }



    var Namespace: String

    var Name: String

    var ValueType: Type

    fun SetFromJson(json: JSONObject)

    fun GetJson(): Any?

    fun SetFromXml(xml: Element)

    fun GetXml(): Element?
}
