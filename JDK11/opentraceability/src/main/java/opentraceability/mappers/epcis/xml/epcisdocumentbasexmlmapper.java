package opentraceability.mappers.epcis.xml;

import Newtonsoft.Json.Linq.*;
import opentraceability.interfaces.*;
import opentraceability.models.common.*;
import opentraceability.models.events.*;
import opentraceability.models.events.kdes.*;
import opentraceability.utility.*;
import opentraceability.*;
import opentraceability.mappers.*;
import opentraceability.mappers.epcis.*;
import java.util.*;

public class EPCISDocumentBaseXMLMapper
{
	public static XmlSchemaChecker _schemaChecker = new XmlSchemaChecker();

//C# TO JAVA CONVERTER TASK: The C# 'new()' constraint has no equivalent in Java:
//ORIGINAL LINE: public static T ReadXml<T>(string strValue, out XDocument xDoc) where T : EPCISBaseDocument, new()
	public static <T extends EPCISBaseDocument> T ReadXml(String strValue, tangible.OutObject<XDocument> xDoc)
	{
		// convert into XDocument
		xDoc.outArgValue = XDocument.Parse(strValue);
		if (xDoc.outArgValue.Root == null)
		{
			throw new RuntimeException("Failed to parse EPCISBaseDocument from xml string because after parsing the XDocument the Root property was null.");
		}

		// read all of the attributes
		T document = System.Activator.<T>CreateInstance();
		for (XAttribute xatt : xDoc.outArgValue.Root.Attributes())
		{
			if (xatt.Name.equals("creationDate") || xatt.Name.equals("schemaVersion"))
			{
				continue;
			}
			else
			{
				if (Objects.equals(xatt.Name.Namespace, Constants.XMLNS_NAMEPSACE))
				{
					document.getNamespaces().put(xatt.Name.LocalName, xatt.Value);
				}
				else
				{
					document.getAttributes().put(xatt.Name.toString(), xatt.Value);
				}
			}
		}

		// determine epcis version
		if (document.getNamespaces().values().ToList().Contains(Constants.EPCIS_2_NAMESPACE) || document.getNamespaces().values().ToList().Contains(Constants.EPCISQUERY_2_NAMESPACE))
		{
			document.setEPCISVersion(EPCISVersion.V2);
		}
		else if (document.getNamespaces().values().ToList().Contains(Constants.EPCIS_1_NAMESPACE) || document.getNamespaces().values().ToList().Contains(Constants.EPCISQUERY_1_NAMESPACE))
		{
			document.setEPCISVersion(EPCISVersion.V1);
		}

		if (document.getEPCISVersion() == null)
		{
			throw new RuntimeException(String.format("Failed to determine the EPCIS version of the XML document. Must contain a namespace with either '%1$s' or '%2$s' or '%3$s' or '%4$s'", Constants.EPCIS_2_NAMESPACE, Constants.EPCIS_1_NAMESPACE, Constants.EPCISQUERY_2_NAMESPACE, Constants.EPCISQUERY_1_NAMESPACE));
		}

		// read the creation date
//C# TO JAVA CONVERTER WARNING: Nullable reference types have no equivalent in Java:
//ORIGINAL LINE: string? creationDateAttributeStr = xDoc.Root.Attribute("creationDate") == null ? null : xDoc.Root.Attribute("creationDate").Value;
		String creationDateAttributeStr = xDoc.outArgValue.Root.Attribute("creationDate") == null ? null : xDoc.outArgValue.Root.Attribute("creationDate").Value;
		if (!(creationDateAttributeStr == null || creationDateAttributeStr.isBlank()))
		{
			document.setCreationDate(StringExtensions.TryConvertToDateTimeOffset(creationDateAttributeStr));
		}

		// read the standard business document header
		XElement xHeader = xDoc.outArgValue.Root.Element("EPCISHeader") == null ? null : xDoc.outArgValue.Root.Element("EPCISHeader").Element(Constants.SBDH_XNAMESPACE + "StandardBusinessDocumentHeader");
		if (xHeader != null)
		{
			document.setHeader(OpenTraceabilityXmlMapper.<StandardBusinessDocumentHeader>FromXml(xHeader, document.getEPCISVersion()));
		}

		// read the master data
		XElement xMasterData = xDoc.outArgValue.Root.Element("EPCISHeader") == null ? null : ((xDoc.outArgValue.Root.Element("EPCISHeader").Element("extension") == null ? null : xDoc.outArgValue.Root.Element("EPCISHeader").Element("extension").Element("EPCISMasterData")));
		if (xMasterData != null)
		{
			EPCISXmlMasterDataReader.ReadMasterData(document, xMasterData);
		}

		return document;
	}

	public static XDocument WriteXml(EPCISBaseDocument doc, XNamespace epcisNS, String rootEleName)
	{
		if (doc.getEPCISVersion() == null)
		{
			throw new RuntimeException("doc.EPCISVersion is NULL. This must be set to a version.");
		}

		// create a new xdocument with all of the namespaces...
		XDocument xDoc = new XDocument(new XElement(epcisNS + rootEleName, doc.getAttributes().Select(a -> new XAttribute(a.Key, a.Value))));
		if (xDoc.Root == null)
		{
			throw new RuntimeException("Failed to convert EPCIS Document into XML because the XDoc.Root is NULL. This should not happen.");
		}

		for (var ns : doc.getNamespaces().entrySet())
		{
			if (Objects.equals(ns.getValue(), Constants.CBVMDA_NAMESPACE) || Objects.equals(ns.getValue(), Constants.EPCISQUERY_1_NAMESPACE) || Objects.equals(ns.getValue(), Constants.EPCISQUERY_2_NAMESPACE) || Objects.equals(ns.getValue(), Constants.EPCIS_1_NAMESPACE) || Objects.equals(ns.getValue(), Constants.EPCIS_2_NAMESPACE))
			{
				continue;
			}
			else
			{
				xDoc.Root.Add(new XAttribute(Constants.XMLNS_XNAMESPACE + ns.getKey(), ns.getValue()));
			}
		}

		// set the creation date
		if (doc.getCreationDate() != null)
		{
			xDoc.Root.Add(new XAttribute("creationDate", doc.getCreationDate().getValue().toString("o")));
		}

		xDoc.Root.SetAttributeValue(Constants.XMLNS_XNAMESPACE + "epcis", null);
		xDoc.Root.SetAttributeValue(Constants.XMLNS_XNAMESPACE + "cbvmda", null);

		if (doc.getEPCISVersion() == EPCISVersion.V2)
		{
			xDoc.Root.Add(new XAttribute("schemaVersion", "2.0"));
			if (doc instanceof EPCISQueryDocument)
			{
				xDoc.Root.Add(new XAttribute(Constants.XMLNS_XNAMESPACE + "epcisq", Constants.EPCISQUERY_2_NAMESPACE));
				xDoc.Root.Add(new XAttribute(Constants.XMLNS_XNAMESPACE + "cbvmda", Constants.CBVMDA_NAMESPACE));
			}
			else
			{
				xDoc.Root.Add(new XAttribute(Constants.XMLNS_XNAMESPACE + "epcis", Constants.EPCIS_2_NAMESPACE));
				xDoc.Root.Add(new XAttribute(Constants.XMLNS_XNAMESPACE + "cbvmda", Constants.CBVMDA_NAMESPACE));
			}
		}
		else if (doc.getEPCISVersion() == EPCISVersion.V1)
		{
			xDoc.Root.Add(new XAttribute("schemaVersion", "1.2"));
			if (doc instanceof EPCISQueryDocument)
			{
				xDoc.Root.Add(new XAttribute(Constants.XMLNS_XNAMESPACE + "epcisq", Constants.EPCISQUERY_1_NAMESPACE));
				xDoc.Root.Add(new XAttribute(Constants.XMLNS_XNAMESPACE + "cbvmda", Constants.CBVMDA_NAMESPACE));
			}
			else
			{
				xDoc.Root.Add(new XAttribute(Constants.XMLNS_XNAMESPACE + "epcis", Constants.EPCIS_1_NAMESPACE));
				xDoc.Root.Add(new XAttribute(Constants.XMLNS_XNAMESPACE + "cbvmda", Constants.CBVMDA_NAMESPACE));
			}
		}

		// write the standard business document header
		if (doc.getHeader() != null)
		{
			String xname = ((Constants.SBDH_XNAMESPACE) + "StandardBusinessDocumentHeader");
			XElement xHeader = OpenTraceabilityXmlMapper.ToXml(xname, doc.getHeader(), doc.getEPCISVersion());
			if (xHeader != null)
			{
				xDoc.Root.Add(new XElement("EPCISHeader", xHeader));
			}
		}

		// write the master data
		EPCISXmlMasterDataWriter.WriteMasterData(xDoc.Root, doc);

		return xDoc;
	}

	public static java.lang.Class GetEventTypeFromProfile(XElement xEvent)
	{
		var action;
//C# TO JAVA CONVERTER TASK: The following method call contained an unresolved 'out' keyword - these cannot be converted using the 'OutObject' helper class unless the method is within the code being modified:
		Enum.<EventAction>TryParse((xEvent.Element("action") == null ? null : xEvent.Element("action").Value), out action);
//C# TO JAVA CONVERTER WARNING: Nullable reference types have no equivalent in Java:
//ORIGINAL LINE: string? bizStep = xEvent.Element("bizStep") == null ? null : xEvent.Element("bizStep").Value;
		String bizStep = xEvent.Element("bizStep") == null ? null : xEvent.Element("bizStep").Value;
		String eventType = xEvent.Name.LocalName;

		if (Objects.equals(eventType, "extension"))
		{
			eventType = xEvent.Elements().First().Name.LocalName;
		}

		var profiles = Setup.Profiles.Where(p -> Objects.equals(p.EventType.toString(), eventType) && (p.Action == null || p.Action == action) && (p.BusinessStep == null || Objects.equals(p.BusinessStep.toLowerCase(), (bizStep == null ? null : bizStep.toLowerCase())))).OrderByDescending(p -> p.SpecificityScore).ToList();
		if (profiles.size() == 0)
		{
			throw new RuntimeException("Failed to create event from profile. Type=" + eventType + " and BizStep=" + bizStep + " and Action=" + action);
		}
		else
		{
			for (var profile : profiles.stream().filter(p -> p.KDEProfiles != null).collect(Collectors.toList()))
			{
				for (var kdeProfile : profile.KDEProfiles)
				{
					if (XElementExtensions.QueryXPath(xEvent, kdeProfile.XPath_V1) == null)
					{
						profiles.remove(profile);
					}
				}
			}

			if (profiles.size() == 0)
			{
				throw new RuntimeException("Failed to create event from profile. Type=" + eventType + " and BizStep=" + bizStep + " and Action=" + action);
			}

			return profiles.get(0).EventClassType;
		}
	}

	public static String GetEventXName(IEvent e)
	{
		if (e.getEventType() == EventType.ObjectEvent)
		{
			return "ObjectEvent";
		}
		else if (e.getEventType() == EventType.TransformationEvent)
		{
			return "TransformationEvent";
		}
		else if (e.getEventType() == EventType.TransactionEvent)
		{
			return "TransactionEvent";
		}
		else if (e.getEventType() == EventType.AggregationEvent)
		{
			return "AggregationEvent";
		}
		else if (e.getEventType() == EventType.AssociationEvent)
		{
			return "AssociationEvent";
		}
		else
		{
			throw new RuntimeException("Failed to determine the event xname. Event C# type is " + e.getClass().getName());
		}
	}

	public static void ValidateEPCISDocumentSchema(XDocument xdoc, EPCISVersion version)
	{
		if (version == EPCISVersion.V1)
		{
			// validate the schema depending on the version in the document
			String error;
			tangible.OutObject<String> tempOut_error = new tangible.OutObject<String>();
//C# TO JAVA CONVERTER WARNING: Nullable reference types have no equivalent in Java:
//ORIGINAL LINE: if (!EPCISDocumentBaseXMLMapper._schemaChecker.Validate(xdoc, "https://raw.githubusercontent.com/ift-gftc/doc.gdst/master/schemas/xml/epcis_1_2/EPCglobal-epcis-1_2.xsd", out string? error))
			if (!EPCISDocumentBaseXMLMapper._schemaChecker.Validate(xdoc, "https://raw.githubusercontent.com/ift-gftc/doc.gdst/master/schemas/xml/epcis_1_2/EPCglobal-epcis-1_2.xsd", tempOut_error))
			{
			error = tempOut_error.outArgValue;
				throw new OpenTraceabilitySchemaException(String.format("Failed to validate the XML schema for the EPCIS XML.\n") + error);
			}
		else
		{
			error = tempOut_error.outArgValue;
		}
		}
		else
		{
			// https://ref.gs1.org/standards/epcis/epcglobal-epcis-2_0.xsd
			String error;
			tangible.OutObject<String> tempOut_error2 = new tangible.OutObject<String>();
//C# TO JAVA CONVERTER WARNING: Nullable reference types have no equivalent in Java:
//ORIGINAL LINE: if (!EPCISDocumentBaseXMLMapper._schemaChecker.Validate(xdoc, "https://ref.gs1.org/standards/epcis/epcglobal-epcis-2_0.xsd", out string? error))
			if (!EPCISDocumentBaseXMLMapper._schemaChecker.Validate(xdoc, "https://ref.gs1.org/standards/epcis/epcglobal-epcis-2_0.xsd", tempOut_error2))
			{
			error = tempOut_error2.outArgValue;
				throw new OpenTraceabilitySchemaException(String.format("Failed to validate the XML schema for the EPCIS XML.\n") + error);
			}
		else
		{
			error = tempOut_error2.outArgValue;
		}
		}
	}

	public static void ValidateEPCISQueryDocumentSchema(XDocument xdoc, EPCISVersion version)
	{
		if (version == EPCISVersion.V1)
		{
			// validate the schema depending on the version in the document
			String error;
			tangible.OutObject<String> tempOut_error = new tangible.OutObject<String>();
//C# TO JAVA CONVERTER WARNING: Nullable reference types have no equivalent in Java:
//ORIGINAL LINE: if (!EPCISDocumentBaseXMLMapper._schemaChecker.Validate(xdoc, "https://raw.githubusercontent.com/ift-gftc/doc.gdst/master/schemas/xml/epcis_1_2/EPCglobal-epcis-query-1_2.xsd", out string? error))
			if (!EPCISDocumentBaseXMLMapper._schemaChecker.Validate(xdoc, "https://raw.githubusercontent.com/ift-gftc/doc.gdst/master/schemas/xml/epcis_1_2/EPCglobal-epcis-query-1_2.xsd", tempOut_error))
			{
			error = tempOut_error.outArgValue;
				throw new RuntimeException(String.format("Failed to validate the XML schema for the EPCIS XML.\n") + error);
			}
		else
		{
			error = tempOut_error.outArgValue;
		}
		}
		else
		{
			// https://ref.gs1.org/standards/epcis/epcglobal-epcis-query-2_0.xsd
			String error;
			tangible.OutObject<String> tempOut_error2 = new tangible.OutObject<String>();
//C# TO JAVA CONVERTER WARNING: Nullable reference types have no equivalent in Java:
//ORIGINAL LINE: if (!EPCISDocumentBaseXMLMapper._schemaChecker.Validate(xdoc, "https://ref.gs1.org/standards/epcis/epcglobal-epcis-query-2_0.xsd", out string? error))
			if (!EPCISDocumentBaseXMLMapper._schemaChecker.Validate(xdoc, "https://ref.gs1.org/standards/epcis/epcglobal-epcis-query-2_0.xsd", tempOut_error2))
			{
			error = tempOut_error2.outArgValue;
				throw new RuntimeException(String.format("Failed to validate the XML schema for the EPCIS XML.\n") + error);
			}
		else
		{
			error = tempOut_error2.outArgValue;
		}
		}
	}
}
