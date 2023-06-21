package opentraceability;

import opentraceability.interfaces.IVocabularyElement;
import opentraceability.models.events.*;
import opentraceability.models.masterdata.Location;
import opentraceability.models.masterdata.TradeItem;
import opentraceability.models.masterdata.TradingParty;
import opentraceability.utility.Countries;
import opentraceability.utility.ReflectionUtility;
import opentraceability.utility.UOMS;


import java.util.*;

public class Setup {
    public static List<OpenTraceabilityEventProfile> Profiles = new ArrayList<>();
    public static Map<String, Class> MasterDataTypes = new HashMap<>();
    public static Map<Class, Class> MasterDataTypeDefault = new HashMap<>();
    private static volatile boolean _isInitialized = false;

    public static synchronized void Initialize() throws Exception
    {
        if (!_isInitialized) {
            UOMS.load();
            Countries.load();

            registerEventProfile(
                new OpenTraceabilityEventProfile(
                    ObjectEvent.class,
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
        Profiles.sort(Comparator.comparingInt(OpenTraceabilityEventProfile::getSpecificityScore).reversed());
    }

    public static void registerMasterDataType(Class type, Class defaultFor) throws Exception {

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

    public static Class getMasterDataTypeDefault(Class type) {
        return MasterDataTypeDefault.get(type);
    }
}