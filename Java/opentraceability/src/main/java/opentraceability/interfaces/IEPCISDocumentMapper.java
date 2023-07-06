package opentraceability.interfaces;

import opentraceability.models.events.EPCISDocument;

public interface IEPCISDocumentMapper {
    EPCISDocument map(String strValue, Boolean checkSchema) throws Exception;
    String map(EPCISDocument doc, Boolean checkSchema) throws Exception;
}