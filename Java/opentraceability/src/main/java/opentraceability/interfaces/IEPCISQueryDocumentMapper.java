package opentraceability.interfaces;

import opentraceability.models.events.EPCISQueryDocument;

public interface IEPCISQueryDocumentMapper {
    EPCISQueryDocument map(String strValue, boolean checkSchema) throws Exception;
    String map(EPCISQueryDocument doc) throws Exception;
}