package opentraceability

import com.google.gson.GsonBuilder
import opentraceability.interfaces.IEvent
import opentraceability.interfaces.IVocabularyElement
import opentraceability.models.events.*
import opentraceability.models.identifiers.*
import opentraceability.models.masterdata.*
import opentraceability.utility.*
import java.lang.reflect.Type
import kotlin.reflect.KClass
import kotlin.reflect.KType
import kotlin.reflect.full.createType
import kotlin.reflect.full.starProjectedType

class Setup {
    companion object {

        var Profiles: MutableList<OpenTraceabilityEventProfile> = ArrayList()
        var MasterDataTypes: MutableMap<String, KType> = mutableMapOf()
        var MasterDataTypeDefault: MutableMap<KType, KType> = mutableMapOf()

        @Volatile
        var _isInitialized: Boolean = false

        @Synchronized
        fun Initialize() {

            if (!_isInitialized) {

                registerEventProfile(
                    OpenTraceabilityEventProfile(
                        ObjectEvent<EventILMD>()::class.createType() as KClass<IEvent>,
                        EventType.ObjectEvent
                    )
                )
                registerEventProfile(
                    OpenTraceabilityEventProfile(
                        TransactionEvent::class.createType() as KClass<IEvent>,
                        EventType.TransactionEvent
                    )
                )
                registerEventProfile(
                    OpenTraceabilityEventProfile(
                        TransformationEvent<EventILMD>()::class.createType() as KClass<IEvent>,
                        EventType.TransformationEvent
                    )
                )
                registerEventProfile(
                    OpenTraceabilityEventProfile(
                        AggregationEvent<EventILMD>()::class.createType() as KClass<IEvent>,
                        EventType.AggregationEvent
                    )
                )
                registerEventProfile(
                    OpenTraceabilityEventProfile(
                        AssociationEvent::class.createType() as KClass<IEvent>,
                        EventType.AssociationEvent
                    )
                )

                registerMasterDataType<TradeItem>();
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

        inline fun <reified T : IVocabularyElement> registerMasterDataType(defaultFor: KType? = null) {

            var v: IVocabularyElement? = T::class.java.newInstance() as IVocabularyElement

            if (v == null) {
                throw Exception("Failed to create an instance of type T using Activator.CreateInstance")
            }

            var type: String = ""

            if (v.epcisType != null) {
                type = v.epcisType!!.toLowerCase()
            } else {
                throw Exception("The 'Type' property on the instance of T returned a NULL value.")
            }

            if (MasterDataTypes.getValue(type) == null) {
                MasterDataTypes.put(type, T::class.starProjectedType)
            } else {
                MasterDataTypes[type] = T::class.starProjectedType
            }

            if (defaultFor != null) {
                if (MasterDataTypeDefault.getValue(defaultFor) == null) {
                    MasterDataTypeDefault.put(defaultFor, T::class.starProjectedType)
                } else {
                    MasterDataTypeDefault[defaultFor] = T::class.starProjectedType
                }
            }
        }

        inline fun <reified T : TDefaultFor, reified TDefaultFor : IVocabularyElement> registerMasterDataType() {
            registerMasterDataType<T>(TDefaultFor::class.starProjectedType);
        }

        fun getMasterDataTypeDefault(type: KType): KClass<IVocabularyElement>? {
            return MasterDataTypeDefault.getValue(type) as KClass<IVocabularyElement>
        }
    }
}
