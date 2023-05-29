package opentraceability

import com.google.gson.GsonBuilder
import opentraceability.interfaces.IVocabularyElement
import opentraceability.models.events.*
import opentraceability.models.identifiers.EPC
import opentraceability.models.identifiers.GLN
import opentraceability.models.identifiers.GTIN
import opentraceability.models.identifiers.PGLN
import opentraceability.models.masterdata.Location
import opentraceability.models.masterdata.Tradeitem
import opentraceability.models.events.*
import opentraceability.models.masterdata.TradingParty
import opentraceability.utility.EPCConverter
import opentraceability.utility.GLNConverter
import opentraceability.utility.GTINConverter
import opentraceability.utility.PGLNConverter
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

                RegisterEventProfile(
                    OpenTraceabilityEventProfile(
                        ObjectEvent<EventILMD>()::class.java,
                        EventType.ObjectEvent
                    )
                )
                RegisterEventProfile(
                    OpenTraceabilityEventProfile(
                        TransactionEvent::class.java,
                        EventType.TransactionEvent
                    )
                )
                RegisterEventProfile(
                    OpenTraceabilityEventProfile(
                        TransformationEvent<EventILMD>()::class.java,
                        EventType.TransformationEvent
                    )
                )
                RegisterEventProfile(
                    OpenTraceabilityEventProfile(
                        AggregationEvent<EventILMD>()::class.java,
                        EventType.AggregationEvent
                    )
                )
                RegisterEventProfile(
                    OpenTraceabilityEventProfile(
                        AssociationEvent::class.java,
                        EventType.AssociationEvent
                    )
                )

                RegisterMasterDataType<Tradeitem>();
                RegisterMasterDataType<Location>();
                RegisterMasterDataType<TradingParty>();


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
        fun RegisterEventProfile(profile: OpenTraceabilityEventProfile) {

            Profiles.forEach { element ->
                if (element.toString() == profile.toString()) {
                    Profiles.remove(element)
                }
            }

            Profiles.add(profile)
        }

        inline fun <reified T> RegisterMasterDataType(defaultFor: Type? = null) {

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


        inline fun <reified T, reified TDefaultFor> RegisterMasterDataType() {
            RegisterMasterDataType<T>(TDefaultFor::class.java);
        }

        fun GetMasterDataTypeDefault(type: Type): Type? {
            return MasterDataTypeDefault.getValue(type)
        }
    }
}
