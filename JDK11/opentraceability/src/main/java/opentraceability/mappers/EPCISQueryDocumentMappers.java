package opentraceability.mappers;

import opentraceability.interfaces.IEPCISQueryDocumentMapper;
import opentraceability.mappers.epcis.json.EPCISQueryDocumentJsonMapper;
import opentraceability.mappers.epcis.xml.EPCISQueryDocumentXMLMapper;

public class EPCISQueryDocumentMappers {
    public IEPCISQueryDocumentMapper XML = new EPCISQueryDocumentXMLMapper();
    public IEPCISQueryDocumentMapper JSON = new EPCISQueryDocumentJsonMapper();
}