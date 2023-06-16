package opentraceability.mappers;

import gs1.mappers.epcis.*;
import opentraceability.interfaces.*;
import opentraceability.mappers.epcis.json.*;
import opentraceability.mappers.masterdata.*;
import opentraceability.models.events.*;
import opentraceability.*;

public final class OpenTraceabilityMappers
{
	public static EPCISDocumentMappers EPCISDocument = new EPCISDocumentMappers();
	public static EPCISQueryDocumentMappers EPCISQueryDocument = new EPCISQueryDocumentMappers();
	public static MasterDataMappers MasterData = new MasterDataMappers();
}
