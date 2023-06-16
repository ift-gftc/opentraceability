package opentraceability.interfaces;

import opentraceability.models.events.EPCISDocument;

public interface IEPCISDocumentMapper {
    EPCISDocument map(String strValue);
    String map(EPCISDocument doc) throws Exception;
}