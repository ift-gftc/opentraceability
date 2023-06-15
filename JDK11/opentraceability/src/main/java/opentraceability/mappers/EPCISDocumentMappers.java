package opentraceability.mappers;

import opentraceability.interfaces.IEPCISDocumentMapper;
import opentraceability.mappers.epcis.json.EPCISDocumentJsonMapper;
import opentraceability.mappers.epcis.xml.EPCISDocumentXMLMapper;

public class EPCISDocumentMappers {
    public IEPCISDocumentMapper XML = new EPCISDocumentXMLMapper();
    public IEPCISDocumentMapper JSON = new EPCISDocumentJsonMapper();
}