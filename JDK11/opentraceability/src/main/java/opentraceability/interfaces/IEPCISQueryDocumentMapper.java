package opentraceability.interfaces;

import opentraceability.models.events.EPCISQueryDocument;

public interface IEPCISQueryDocumentMapper {
    public EPCISQueryDocument map(String strValue, boolean checkSchema) throws Exception;
    public String map(EPCISQueryDocument doc) throws Exception;
}