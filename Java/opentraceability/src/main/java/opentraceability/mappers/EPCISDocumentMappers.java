package opentraceability.mappers;

import opentraceability.interfaces.*;
import opentraceability.mappers.epcis.json.*;
import opentraceability.mappers.epcis.xml.EPCISDocumentXMLMapper;
import opentraceability.mappers.masterdata.*;
import opentraceability.models.events.*;
import opentraceability.*;

public class EPCISDocumentMappers
{
	public IEPCISDocumentMapper XML = new EPCISDocumentXMLMapper();
	public IEPCISDocumentMapper JSON = new EPCISDocumentJsonMapper();
}
