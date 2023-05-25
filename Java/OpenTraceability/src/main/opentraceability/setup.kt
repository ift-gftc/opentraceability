import interfaces.IVocabularyElement
import models.events.*
import models.masterdata.Location
import models.masterdata.Tradeitem
import models.masterdata.TradingParty
import java.lang.reflect.Type

class Setup {
    companion object {

        var Profiles: ArrayList<OpenTraceabilityEventProfile> = ArrayList()
        var MasterDataTypes: MutableMap<String, Type> = mutableMapOf()
        var MasterDataTypeDefault: MutableMap<Type, Type> = mutableMapOf()

        var _locker: Object = Object()
        var _isInitialized: Boolean = false

        fun Initialize() {
            //TODO: _locker not yet implemented

            //lock (_locker){
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

                //TODO: JsonConvert not yet implemented
                /*
                JsonConvert.DefaultSettings = () =>
                {
                    JsonSerializerSettings settings = new JsonSerializerSettings();
                    settings.Converters.Add(new EPCConverter());
                    settings.Converters.Add(new GTINConverter());
                    settings.Converters.Add(new GLNConverter());
                    settings.Converters.Add(new PGLNConverter());

                    return settings;
                };
                */

                _isInitialized = true
            }
            //}
        }

        fun RegisterEventProfile(profile: OpenTraceabilityEventProfile) {

            //TODO: _locker not yet implemented
            //lock (_locker){

            Profiles.forEach { element ->
                if (element.toString() == profile.toString()) {
                    Profiles.remove(element)
                }
            }

            Profiles.add(profile)

            //}
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
