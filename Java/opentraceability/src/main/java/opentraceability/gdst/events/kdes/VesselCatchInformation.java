package opentraceability.gdst.events.kdes;

import opentraceability.Constants;
import opentraceability.interfaces.IEventKDE;
import opentraceability.utility.Country;
import opentraceability.utility.attributes.*;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;

public class VesselCatchInformation {
    @OpenTraceabilityJsonAttribute(name = "cbvmda:catchArea")
    @OpenTraceabilityAttribute(ns = Constants.CBVMDA_NAMESPACE, name = "catchArea")
    private String catchArea;

    @OpenTraceabilityJsonAttribute(name = "cbvmda:economicZone")
    @OpenTraceabilityAttribute(ns = Constants.CBVMDA_NAMESPACE, name = "economicZone")
    private String economicZone;

    @OpenTraceabilityJsonAttribute(name = "cbvmda:fishingGearTypeCode")
    @OpenTraceabilityAttribute(ns = Constants.CBVMDA_NAMESPACE, name = "fishingGearTypeCode")
    private String gearType;

    @OpenTraceabilityJsonAttribute(name = "cbvmda:vesselFlagState")
    @OpenTraceabilityAttribute(ns = Constants.CBVMDA_NAMESPACE, name = "vesselFlagState")
    private Country vesselFlagState;

    @OpenTraceabilityJsonAttribute(name = "cbvmda:vesselID")
    @OpenTraceabilityAttribute(ns = Constants.CBVMDA_NAMESPACE, name = "vesselID")
    private String vesselID;

    @OpenTraceabilityJsonAttribute(name = "cbvmda:vesselName")
    @OpenTraceabilityAttribute(ns = Constants.CBVMDA_NAMESPACE, name = "vesselName")
    private String vesselName;

    @OpenTraceabilityJsonAttribute(name = "gdst:fisheryImprovementProject")
    @OpenTraceabilityAttribute(ns = Constants.GDST_NAMESPACE, name = "fisheryImprovementProject")
    private String FIP;

    @OpenTraceabilityJsonAttribute(name = "gdst:gpsAvailability")
    @OpenTraceabilityAttribute(ns = Constants.GDST_NAMESPACE, name = "gpsAvailability")
    private boolean GPSAvailability;

    @OpenTraceabilityJsonAttribute(name = "gdst:imoNumber")
    @OpenTraceabilityAttribute(ns = Constants.GDST_NAMESPACE, name = "imoNumber")
    private String IMONumber;

    @OpenTraceabilityJsonAttribute(name = "gdst:rfmoArea")
    @OpenTraceabilityAttribute(ns = Constants.GDST_NAMESPACE, name = "rfmoArea")
    private String RFMO;

    @OpenTraceabilityJsonAttribute(name = "gdst:satelliteTrackingAuthority")
    @OpenTraceabilityAttribute(ns = Constants.GDST_NAMESPACE, name = "satelliteTrackingAuthority")
    private String satelliteTrackingAuthority;

    @OpenTraceabilityJsonAttribute(name = "gdst:subnationalPermitArea")
    @OpenTraceabilityAttribute(ns = Constants.GDST_NAMESPACE, name = "subnationalPermitArea")
    private String subNationalPermitArea;

    @OpenTraceabilityJsonAttribute(name = "gdst:vesselPublicRegistry")
    @OpenTraceabilityAttribute(ns = Constants.GDST_NAMESPACE, name = "vesselPublicRegistry")
    private String vesselPublicRegistry;

    @OpenTraceabilityAttribute(ns = Constants.GDST_NAMESPACE, name = "vesselTripDate")
    private OffsetDateTime vesselTripDate;

    @OpenTraceabilityExtensionElementsAttribute
    private List<IEventKDE> KDEs = new ArrayList<>();

    // Add constructor, getters, and setters here
}
