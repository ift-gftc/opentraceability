package opentraceability.gdst.masterdata;

import opentraceability.models.masterdata.Location;
import opentraceability.utility.Country;
import opentraceability.utility.attributes.*;

public class GDSTLocation extends Location {
    @OpenTraceabilityJsonAttribute(name = "cbvmda:vesselFlagState")
    @OpenTraceabilityMasterDataAttribute(name = "urn:epcglobal:cbv:mda#vesselFlagState")
    private Country vesselFlagState;

    @OpenTraceabilityJsonAttribute(name = "cbvmda:vesselID")
    @OpenTraceabilityMasterDataAttribute(name = "urn:epcglobal:cbv:mda#vesselID")
    private String vesselID;

    @OpenTraceabilityJsonAttribute(name = "cbvmda:vesselName")
    @OpenTraceabilityMasterDataAttribute(name = "urn:epcglobal:cbv:mda#vesselName")
    private String vesselName;

    @OpenTraceabilityJsonAttribute(name = "gdst:imoNumber")
    @OpenTraceabilityMasterDataAttribute(name = "urn:gdst:kde#imoNumber")
    private String IMONumber;

    @OpenTraceabilityJsonAttribute(name = "gdst:vesselPublicRegistry")
    @OpenTraceabilityMasterDataAttribute(name = "urn:gdst:kde#vesselPublicRegistry")
    private String vesselPublicRegistry;

    @OpenTraceabilityJsonAttribute(name = "gdst:satelliteTracking")
    @OpenTraceabilityMasterDataAttribute(name = "urn:gdst:kde#satelliteTracking")
    private String satelliteTrackingAuthority;
}
