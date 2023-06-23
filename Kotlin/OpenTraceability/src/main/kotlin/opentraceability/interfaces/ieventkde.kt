package opentraceability.interfaces

import org.json.*
import org.w3c.dom.Element
import java.lang.reflect.Type
import kotlin.reflect.KType

interface IEventKDE {

    companion object {
        var registeredKDEs : MutableMap<String, Type> = mutableMapOf()

        inline fun <reified T> RegisterKDE(ns: String, name: String) {
            val key: String = "$ns:$name"

            if (!registeredKDEs.contains(key)) {
                registeredKDEs.put(key, T::class.java)
            } else {
                var fullName = registeredKDEs[key].toString()
                throw Exception("The KDE $key is already registered with type $fullName")
            }
        }

        fun initializeKDE(ns: String, name: String): IEventKDE? {
            var kde: IEventKDE? = null

            val key: String = "$ns:$name"

            var kdeType = registeredKDEs.getValue(key)

            if (kdeType != null) {
                kde = Class.forName(key) as IEventKDE
            }

            if (kde != null) {
                kde.namespace = ns;
                kde.name = name;
            }

            return kde
        }
    }



    var namespace: String

    var name: String

    var valueType: KType

    fun setFromJson(json: JSONObject)

    fun getJson(): Any?

    fun setFromXml(xml: Element)

    fun getXml(): Element?
}
