package opentraceability.mappers;

import gs1.mappers.epcis.*;
import opentraceability.interfaces.*;
import opentraceability.mappers.epcis.json.*;
import opentraceability.mappers.masterdata.*;
import opentraceability.models.events.*;
import opentraceability.*;

public class EPCISQueryDocumentMappers
{
	public IEPCISQueryDocumentMapper XML = new EPCISQueryDocumentXMLMapper();
	public IEPCISQueryDocumentMapper JSON = new EPCISQueryDocumentJsonMapper();
}
