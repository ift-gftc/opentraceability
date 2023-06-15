package opentraceability.models.masterdata;

import java.util.ArrayList;
import java.util.List;

import opentraceability.models.common.LanguageString;
import opentraceability.utility.Country;
import opentraceability.utility.attributes.OpenTraceabilityJsonAttribute;
import opentraceability.utility.attributes.OpenTraceabilityMasterDataAttribute;

public class Address {
    @OpenTraceabilityJsonAttribute(name = "@type")
    public String type = "gs1:PostalAddress";

    @OpenTraceabilityMasterDataAttribute(name = "urn:epcglobal:cbv:mda#streetAddressOne")
    @OpenTraceabilityJsonAttribute(name = "streetAddress")
    public List<LanguageString> address1 = new ArrayList<>();

    @OpenTraceabilityMasterDataAttribute(name = "urn:epcglobal:cbv:mda#streetAddressTwo")
    public List<LanguageString> address2 = new ArrayList<>();

    @OpenTraceabilityMasterDataAttribute(name = "urn:epcglobal:cbv:mda#city")
    @OpenTraceabilityJsonAttribute(name = "addressLocality")
    public List<LanguageString> city = new ArrayList<>();

    @OpenTraceabilityMasterDataAttribute(name = "urn:epcglobal:cbv:mda#state")
    @OpenTraceabilityJsonAttribute(name = "addressRegion")
    public List<LanguageString> state = new ArrayList<>();

    @OpenTraceabilityMasterDataAttribute(name = "urn:epcglobal:cbv:mda#countryCode")
    @OpenTraceabilityJsonAttribute(name = "countryCode")
    public Country country;
}