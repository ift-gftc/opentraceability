package opentraceability.mappers.epcis.xml;

import opentraceability.models.common.*;
import opentraceability.utility.*;
import opentraceability.*;
import opentraceability.mappers.*;
import opentraceability.mappers.epcis.*;

public final class EPCISXmlHeaderWriter
{
	public static XElement WriteHeader(StandardBusinessDocumentHeader header)
	{
		XElement xHeader = new XElement(Constants.SBDH_XNAMESPACE + "StandardBusinessDocumentHeader");

		if (!(header.getHeaderVersion() == null || header.getHeaderVersion().isBlank()))
		{
			xHeader.Add(new XElement(Constants.SBDH_XNAMESPACE + "HeaderVersion", header.getHeaderVersion()));
		}

		WriteHeaderOrganization("Sender", header.Sender, xHeader);
		WriteHeaderOrganization("Receiver", header.Receiver, xHeader);

		if (header.DocumentIdentification != null)
		{
			XElement xDocId = new XElement(Constants.SBDH_XNAMESPACE + "DocumentIdentification");
			XElementExtensions.AddStringElement(xDocId, Constants.SBDH_XNAMESPACE + "Standard", header.DocumentIdentification.getStandard());
			XElementExtensions.AddStringElement(xDocId, Constants.SBDH_XNAMESPACE + "TypeVersion", header.DocumentIdentification.getTypeVersion());
			XElementExtensions.AddStringElement(xDocId, Constants.SBDH_XNAMESPACE + "InstanceIdentifier", header.DocumentIdentification.getInstanceIdentifier());
			XElementExtensions.AddStringElement(xDocId, Constants.SBDH_XNAMESPACE + "Type", header.DocumentIdentification.getType());
			XElementExtensions.AddStringElement(xDocId, Constants.SBDH_XNAMESPACE + "MultipleType", header.DocumentIdentification.getMultipleType());
			XElementExtensions.AddDateTimeOffsetISOElement(xDocId, Constants.SBDH_XNAMESPACE + "CreationDateAndTime", header.DocumentIdentification.getCreationDateAndTime());
			xHeader.Add(xDocId);
		}

		return xHeader;
	}

//C# TO JAVA CONVERTER WARNING: Nullable reference types have no equivalent in Java:
//ORIGINAL LINE: private static void WriteHeaderOrganization(string xName, SBDHOrganization? org, XElement xHeader)
	private static void WriteHeaderOrganization(String xName, SBDHOrganization org, XElement xHeader)
	{
		if (org != null)
		{
			XElement xOrg = new XElement(Constants.SBDH_XNAMESPACE + xName);
			if (!(org.Identifier() == null || org.Identifier().isBlank()))
			{
				xOrg.Add(new XElement(Constants.SBDH_XNAMESPACE + "Identifier", org.Identifier()));
			}
			if (!(org.ContactName == null || org.ContactName.isBlank()) || !(org.EmailAddress == null || org.EmailAddress.isBlank()))
			{
				XElement xContactInfo = new XElement(Constants.SBDH_XNAMESPACE + "ContactInformation");
				if (!(org.ContactName == null || org.ContactName.isBlank()))
				{
					xContactInfo.Add(new XElement(Constants.SBDH_XNAMESPACE + "Contact", org.ContactName));
				}
				if (!(org.EmailAddress == null || org.EmailAddress.isBlank()))
				{
					xContactInfo.Add(new XElement(Constants.SBDH_XNAMESPACE + "EmailAddress", org.EmailAddress));
				}
				xOrg.Add(xContactInfo);
			}
			xHeader.Add(xOrg);
		}
	}
}
