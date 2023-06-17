package opentraceability;

import opentraceability.interfaces.IVocabularyElement;
import opentraceability.models.events.*;
import opentraceability.models.masterdata.Location;
import opentraceability.models.masterdata.TradeItem;
import opentraceability.models.masterdata.TradingParty;
import opentraceability.utility.ReflectionUtility;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Setup {
    public static List<OpenTraceabilityEventProfile> Profiles = new ArrayList<>();
    public static Map<String, Type> MasterDataTypes = new HashMap<>();
    public static Map<Type, Type> MasterDataTypeDefault = new HashMap<>();
    private static volatile boolean _isInitialized = false;

    public static synchronized void Initialize() {

        if (!_isInitialized) {

            registerEventProfile(
                new OpenTraceabilityEventProfile(
                    EventILMD.class,
                    EventType.ObjectEvent
                )
            );
            registerEventProfile(
                new OpenTraceabilityEventProfile(
                    TransactionEvent.class,
                    EventType.TransactionEvent
                )
            );
            registerEventProfile(
                new OpenTraceabilityEventProfile(
                    TransformationEvent.class,
                    EventType.TransformationEvent
                )
            );
            registerEventProfile(
                new OpenTraceabilityEventProfile(
                    AggregationEvent.class,
                    EventType.AggregationEvent
                )
            );
            registerEventProfile(
                new OpenTraceabilityEventProfile(
                    AssociationEvent.class,
                    EventType.AssociationEvent
                )
            );

            registerMasterDataType(TradeItem.class, TradeItem.class);
            registerMasterDataType(Location.class, Location.class);
            registerMasterDataType(TradingParty.class, TradingParty.class);
            
//            GsonBuilder builder = new GsonBuilder();
//            builder.registerTypeAdapter(EPC.class, new EPCConverter());
//            builder.registerTypeAdapter(GTIN.class, new GTINConverter());
//            builder.registerTypeAdapter(GLN.class, new GLNConverter());
//            builder.registerTypeAdapter(PGLN.class, new PGLNConverter());
//            builder.create();

            _isInitialized = true;
        }
    }

    public static synchronized void registerEventProfile(OpenTraceabilityEventProfile profile) {

        Profiles.forEach(element -> {
            if (element.toString().equals(profile.toString())) {
                Profiles.remove(element);
            }
        });

        Profiles.add(profile);
    }

    public static void registerMasterDataType(Type type, Type defaultFor) throws Exception {

        if (!ReflectionUtility.isTypeInherited(defaultFor, IVocabularyElement.class))
        {
            throw new IllegalArgumentException("Failure to register master data type " + defaultFor.getTypeName() + " because " +
                    "it does not inherit from " + IVocabularyElement.class.getTypeName());
        }

        if (!ReflectionUtility.isTypeInherited(type, defaultFor))
        {
            throw new IllegalArgumentException("Failure to register master data type " + type.getTypeName() + " because " +
                    "it does not inherit from " + defaultFor.getTypeName());
        }

        IVocabularyElement v = (IVocabularyElement)ReflectionUtility.constructType(type);

        if (v == null) {
            throw new RuntimeException("Failed to create an instance of type " + type.getTypeName() + " using ReflectionUtility.constructType()");
        }

        String epcisTypeValue = "";
        if (v.epcisType != null) {
            epcisTypeValue = v.epcisType.toLowerCase();
        } else {
            throw new RuntimeException("The 'epcisType' property on the instance of " + type.getTypeName() + " returned a NULL value.");
        }

        MasterDataTypes.put(epcisTypeValue, type);

        if (defaultFor != null) {
            if (MasterDataTypeDefault.get(defaultFor) == null) {
                MasterDataTypeDefault.put(defaultFor, type);
            } else {
                MasterDataTypeDefault.put(defaultFor, type);
            }
        }
    }

    public static <T extends TDefaultFor, TDefaultFor extends IVocabularyElement> void registerMasterDataType(Class<T> type, Class<TDefaultFor> defaultFor) {
        registerMasterDataType(type, defaultFor);
    }

    public static Type getMasterDataTypeDefault(Type type) {
        return MasterDataTypeDefault.get(type);
    }
}