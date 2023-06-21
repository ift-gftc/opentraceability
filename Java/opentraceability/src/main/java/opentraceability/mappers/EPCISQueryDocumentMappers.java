package opentraceability.mappers;


import opentraceability.interfaces.*;
import opentraceability.mappers.epcis.json.*;
import opentraceability.mappers.epcis.xml.EPCISQueryDocumentXMLMapper;
import opentraceability.mappers.masterdata.*;
import opentraceability.models.events.*;
import opentraceability.*;

public class EPCISQueryDocumentMappers
{
	public IEPCISQueryDocumentMapper XML = new EPCISQueryDocumentXMLMapper();
	public IEPCISQueryDocumentMapper JSON = new EPCISQueryDocumentJsonMapper();
}
