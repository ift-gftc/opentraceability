package opentraceability.interfaces;

import opentraceability.models.events.EPCISQueryDocument;

public interface IEPCISQueryDocumentMapper {
    public EPCISQueryDocument map(String strValue, boolean checkSchema);
    public String map(EPCISQueryDocument doc);
}