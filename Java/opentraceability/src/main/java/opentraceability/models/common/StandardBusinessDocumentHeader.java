package opentraceability.models.common;

import opentraceability.Constants;
import opentraceability.utility.attributes.OpenTraceabilityAttribute;
import opentraceability.utility.attributes.OpenTraceabilityObjectAttribute;
import java.time.OffsetDateTime;

public class StandardBusinessDocumentHeader {

    @OpenTraceabilityAttribute(ns = Constants.SBDH_NAMESPACE, name = "HeaderVersion")
    public String HeaderVersion = "";

    @OpenTraceabilityObjectAttribute
    @OpenTraceabilityAttribute(ns = Constants.SBDH_NAMESPACE, name = "Sender")
    public SBDHOrganization Sender;

    @OpenTraceabilityObjectAttribute
    @OpenTraceabilityAttribute(ns = Constants.SBDH_NAMESPACE, name = "Receiver")
    public SBDHOrganization Receiver;

    @OpenTraceabilityObjectAttribute
    @OpenTraceabilityAttribute(ns = Constants.SBDH_NAMESPACE, name = "DocumentIdentification")
    public SBDHDocumentIdentification DocumentIdentification;

    public static StandardBusinessDocumentHeader DummyHeader = new StandardBusinessDocumentHeader();
    static {
        DummyHeader.HeaderVersion = "x";

        DummyHeader.Sender = new SBDHOrganization();
        DummyHeader.Sender.Identifier = "x";
        DummyHeader.Sender.ContactInformation = new SBDHContact();
        DummyHeader.Sender.ContactInformation.ContactName = "x";
        DummyHeader.Sender.ContactInformation.EmailAddress = "x";

        DummyHeader.Receiver = new SBDHOrganization();
        DummyHeader.Receiver.Identifier = "x";
        DummyHeader.Receiver.ContactInformation = new SBDHContact();
        DummyHeader.Receiver.ContactInformation.ContactName = "x";
        DummyHeader.Receiver.ContactInformation.EmailAddress = "x";

        DummyHeader.DocumentIdentification = new SBDHDocumentIdentification();
        DummyHeader.DocumentIdentification.CreationDateAndTime = OffsetDateTime.now();
        DummyHeader.DocumentIdentification.InstanceIdentifier = "x";
        DummyHeader.DocumentIdentification.MultipleType = "false";
        DummyHeader.DocumentIdentification.Standard = "x";
        DummyHeader.DocumentIdentification.Type = "x";
        DummyHeader.DocumentIdentification.TypeVersion = "3.0";
    }
}