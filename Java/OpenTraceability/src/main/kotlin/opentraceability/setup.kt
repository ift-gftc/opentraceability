import com.google.gson.GsonBuilder
import interfaces.IVocabularyElement
import models.events.*
import models.identifiers.*
import models.masterdata.*
import utility.*
import java.lang.reflect.Type

class Setup {
    companion object {

        var Profiles: ArrayList<OpenTraceabilityEventProfile> = ArrayList()
        var MasterDataTypes: MutableMap<String, Type> = mutableMapOf()
        var MasterDataTypeDefault: MutableMap<Type, Type> = mutableMapOf()

        @Volatile
        var _isInitialized: Boolean = false

        @Synchronized
        fun Initialize() {

            if (!_isInitialized) {

                registerEventProfile(
                    OpenTraceabilityEventProfile(
                        ObjectEvent<EventILMD>()::class.java,
                        EventType.ObjectEvent
                    )
                )
                registerEventProfile(
                    OpenTraceabilityEventProfile(
                        TransactionEvent::class.java,
                        EventType.TransactionEvent
                    )
                )
                registerEventProfile(
                    OpenTraceabilityEventProfile(
                        TransformationEvent<EventILMD>()::class.java,
                        EventType.TransformationEvent
                    )
                )
                registerEventProfile(
                    OpenTraceabilityEventProfile(
                        AggregationEvent<EventILMD>()::class.java,
                        EventType.AggregationEvent
                    )
                )
                registerEventProfile(
                    OpenTraceabilityEventProfile(
                        AssociationEvent::class.java,
                        EventType.AssociationEvent
                    )
                )

                registerMasterDataType<Tradeitem>();
                registerMasterDataType<Location>();
                registerMasterDataType<TradingParty>();


                val gson = GsonBuilder()
                    .registerTypeAdapter(EPC::class.java, EPCConverter())
                    .registerTypeAdapter(GTIN::class.java, GTINConverter())
                    .registerTypeAdapter(GLN::class.java, GLNConverter())
                    .registerTypeAdapter(PGLN::class.java, PGLNConverter())
                    .create()


                _isInitialized = true
            }

        }

        @Synchronized
        fun registerEventProfile(profile: OpenTraceabilityEventProfile) {

            Profiles.forEach { element ->
                if (element.toString() == profile.toString()) {
                    Profiles.remove(element)
                }
            }

            Profiles.add(profile)
        }

        inline fun <reified T> registerMasterDataType(defaultFor: Type? = null) {

            var v: IVocabularyElement? = T::class.java.newInstance() as IVocabularyElement

            if (v == null) {
                throw Exception("Failed to create an instance of type T using Activator.CreateInstance")
            }

            var type: String = ""

            if (v.EPCISType != null) {
                type = v.EPCISType!!.toLowerCase()
            } else {
                throw Exception("The 'Type' property on the instance of T returned a NULL value.")
            }

            if (MasterDataTypes.getValue(type) == null) {
                MasterDataTypes.put(type, T::class.java)
            } else {
                MasterDataTypes[type] = T::class.java
            }

            if (defaultFor != null) {
                if (MasterDataTypeDefault.getValue(defaultFor) == null) {
                    MasterDataTypeDefault.put(defaultFor, T::class.java)
                } else {
                    MasterDataTypeDefault[defaultFor] = T::class.java
                }
            }
        }


        inline fun <reified T, reified TDefaultFor> registerMasterDataType() {
            registerMasterDataType<T>(TDefaultFor::class.java);
        }

        fun getMasterDataTypeDefault(type: Type): Type? {
            return MasterDataTypeDefault.getValue(type)
        }
    }
}
