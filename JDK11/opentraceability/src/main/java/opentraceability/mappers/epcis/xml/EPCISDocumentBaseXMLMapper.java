package opentraceability.mappers.epcis.xml;


import opentraceability.interfaces.*;
import opentraceability.models.common.*;
import opentraceability.models.events.*;
import opentraceability.utility.*;
import opentraceability.*;
import opentraceability.mappers.*;

import javax.xml.xpath.XPathExpressionException;

import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

public class EPCISDocumentBaseXMLMapper
{
	public static XmlSchemaChecker _schemaChecker = new XmlSchemaChecker();

//C# TO JAVA CONVERTER TASK: The C# 'new()' constraint has no equivalent in Java:
//ORIGINAL LINE: public static T ReadXml<T>(string strValue, out XElement xDoc) where T : EPCISBaseDocument, new()
	public static <T extends EPCISBaseDocument> T ReadXml(String strValue, tangible.OutObject<XElement> xDoc, Class<T> clazz) throws Exception {
		// convert into XElement
		xDoc.outArgValue = XElement.Parse(strValue);

		// read all of the attributes
		T document = (T)ReflectionUtility.constructType(clazz);

		for (XAttribute xatt : xDoc.outArgValue.Attributes())
		{
			if (xatt.Name.equals("creationDate") || xatt.Name.equals("schemaVersion"))
			{
				continue;
			}
			else
			{
				if (Objects.equals(xatt.Namespace, Constants.XMLNS_NAMEPSACE))
				{
					document.namespaces.put(xatt.LocalName, xatt.Value);
				}
				else
				{
					document.attributes.put(xatt.Name, xatt.Value);
				}
			}
		}

		// determine epcis version
		if (document.namespaces.containsValue(Constants.EPCIS_2_NAMESPACE) || document.namespaces.containsValue(Constants.EPCISQUERY_2_NAMESPACE))
		{
			document.epcisVersion = EPCISVersion.V2;
		}
		else if (document.namespaces.containsValue(Constants.EPCIS_1_NAMESPACE) || document.namespaces.containsValue(Constants.EPCISQUERY_1_NAMESPACE))
		{
			document.epcisVersion = EPCISVersion.V1;
		}

		if (document.epcisVersion == null)
		{
			throw new RuntimeException(String.format("Failed to determine the EPCIS version of the XML document. Must contain a namespace with either '%1$s' or '%2$s' or '%3$s' or '%4$s'", Constants.EPCIS_2_NAMESPACE, Constants.EPCIS_1_NAMESPACE, Constants.EPCISQUERY_2_NAMESPACE, Constants.EPCISQUERY_1_NAMESPACE));
		}

		// read the creation date
//C# TO JAVA CONVERTER WARNING: Nullable reference types have no equivalent in Java:
//ORIGINAL LINE: string? creationDateAttributeStr = xDoc.Attribute("creationDate") == null ? null : xDoc.Attribute("creationDate").Value;
		document.creationDate = xDoc.outArgValue.AttributeOffsetDateTime("creationDate");

		// read the standard business document header
		XElement xHeader = xDoc.outArgValue.Element("EPCISHeader")
				                           .Element(Constants.SBDH_XNAMESPACE, "StandardBusinessDocumentHeader");

		if (!xHeader.IsNull)
		{
			document.header = (StandardBusinessDocumentHeader)OpenTraceabilityXmlMapper.FromXml(xHeader, document.epcisVersion, StandardBusinessDocumentHeader.class);
		}

		// read the master data
		XElement xMasterData = xDoc.outArgValue.Element("EPCISHeader/extension/EPCISMasterData");
		EPCISXmlMasterDataReader.ReadMasterData(document, xMasterData);

		return document;
	}

	public static XElement WriteXml(EPCISBaseDocument doc, String epcisNS, String rootEleName) throws Exception {
		if (doc.epcisVersion == null)
		{
			throw new RuntimeException("doc.EPCISVersion is NULL. This must be set to a version.");
		}

		// create a new xdocument with all of the namespaces...
		XElement xDoc = new XElement(epcisNS, rootEleName);
		for (var a: doc.attributes.entrySet())
		{
			xDoc.Add(new XAttribute(a.getKey(), a.getValue()));
		}

		for (var ns : doc.namespaces.entrySet())
		{
			if (Objects.equals(ns.getValue(), Constants.CBVMDA_NAMESPACE) || Objects.equals(ns.getValue(), Constants.EPCISQUERY_1_NAMESPACE) || Objects.equals(ns.getValue(), Constants.EPCISQUERY_2_NAMESPACE) || Objects.equals(ns.getValue(), Constants.EPCIS_1_NAMESPACE) || Objects.equals(ns.getValue(), Constants.EPCIS_2_NAMESPACE))
			{
				continue;
			}
			else
			{
				xDoc.Add(new XAttribute(Constants.XMLNS_XNAMESPACE, ns.getKey(), ns.getValue()));
			}
		}

		// set the creation date
		if (doc.creationDate != null)
		{
			xDoc.Add(new XAttribute("creationDate", doc.creationDate.format(DateTimeFormatter.ISO_OFFSET_DATE_TIME)));
		}

		xDoc.SetAttributeValue(Constants.XMLNS_XNAMESPACE,  "epcis", null);
		xDoc.SetAttributeValue(Constants.XMLNS_XNAMESPACE,  "cbvmda", null);

		if (doc.epcisVersion == EPCISVersion.V2)
		{
			xDoc.Add(new XAttribute("schemaVersion", "2.0"));
			if (doc instanceof EPCISQueryDocument)
			{
				xDoc.Add(new XAttribute(Constants.XMLNS_XNAMESPACE,  "epcisq", Constants.EPCISQUERY_2_NAMESPACE));
				xDoc.Add(new XAttribute(Constants.XMLNS_XNAMESPACE,  "cbvmda", Constants.CBVMDA_NAMESPACE));
			}
			else
			{
				xDoc.Add(new XAttribute(Constants.XMLNS_XNAMESPACE,  "epcis", Constants.EPCIS_2_NAMESPACE));
				xDoc.Add(new XAttribute(Constants.XMLNS_XNAMESPACE,  "cbvmda", Constants.CBVMDA_NAMESPACE));
			}
		}
		else if (doc.epcisVersion == EPCISVersion.V1)
		{
			xDoc.Add(new XAttribute("schemaVersion", "1.2"));
			if (doc instanceof EPCISQueryDocument)
			{
				xDoc.Add(new XAttribute(Constants.XMLNS_XNAMESPACE,  "epcisq", Constants.EPCISQUERY_1_NAMESPACE));
				xDoc.Add(new XAttribute(Constants.XMLNS_XNAMESPACE,  "cbvmda", Constants.CBVMDA_NAMESPACE));
			}
			else
			{
				xDoc.Add(new XAttribute(Constants.XMLNS_XNAMESPACE,  "epcis", Constants.EPCIS_1_NAMESPACE));
				xDoc.Add(new XAttribute(Constants.XMLNS_XNAMESPACE,  "cbvmda", Constants.CBVMDA_NAMESPACE));
			}
		}

		// write the standard business document header
		if (doc.header != null)
		{
			XElement xHeader = OpenTraceabilityXmlMapper.ToXml(Constants.SBDH_XNAMESPACE, "StandardBusinessDocumentHeader", doc.header, doc.epcisVersion);
			if (xHeader != null)
			{
				xDoc.Add("EPCISHeader").Add(xHeader);
			}
		}

		// write the master data
		EPCISXmlMasterDataWriter.WriteMasterData(xDoc, doc);

		return xDoc;
	}

	public static Class GetEventTypeFromProfile(XElement xEvent) throws XPathExpressionException {
		String action = xEvent.Element("action").getValue();
		String bizStep = xEvent.Element("bizStep").getValue();
		String eventType = xEvent.Element("type").getValue();

		if (bizStep == null)
		{
			bizStep = "";
		}

		String finalBizStep = bizStep;
		var profiles = Setup.Profiles.stream().filter(p -> Objects.equals(p.EventType.toString(), eventType)
				&& (p.Action == null || p.Action.toString() == action)
				&& (p.BusinessStep == null || Objects.equals(p.BusinessStep.toLowerCase(), finalBizStep)));

		List<OpenTraceabilityEventProfile> finalProfiles = profiles.collect(Collectors.toList());

		if (finalProfiles.size() == 0)
		{
			throw new RuntimeException("Failed to create event from profile. Type=" + eventType + " and BizStep=" + bizStep + " and Action=" + action);
		}
		else
		{
			for (var profile : finalProfiles.stream().filter(p -> p.KDEProfiles != null).collect(Collectors.toList()))
			{
				for (var kdeProfile : profile.KDEProfiles)
				{
					if (xEvent.Element(kdeProfile.XPath_V1).IsNull)
					{
						finalProfiles.remove(profile);
					}
				}
			}

			if (finalProfiles.size() == 0)
			{
				throw new RuntimeException("Failed to create event from profile. Type=" + eventType + " and BizStep=" + bizStep + " and Action=" + action);
			}

			return ((OpenTraceabilityEventProfile)finalProfiles.toArray()[0]).EventClassType;
		}
	}

	public static String GetEventXName(IEvent e)
	{
		if (e.eventType == EventType.ObjectEvent)
		{
			return "ObjectEvent";
		}
		else if (e.eventType == EventType.TransformationEvent)
		{
			return "TransformationEvent";
		}
		else if (e.eventType == EventType.TransactionEvent)
		{
			return "TransactionEvent";
		}
		else if (e.eventType == EventType.AggregationEvent)
		{
			return "AggregationEvent";
		}
		else if (e.eventType == EventType.AssociationEvent)
		{
			return "AssociationEvent";
		}
		else
		{
			throw new RuntimeException("Failed to determine the event xname. Event C# type is " + e.getClass().getName());
		}
	}

	public static void ValidateEPCISDocumentSchema(XElement xdoc, EPCISVersion version) throws OpenTraceabilitySchemaException {
		if (version == EPCISVersion.V1)
		{
			// validate the schema depending on the version in the document
			String error;
			tangible.OutObject<String> tempOut_error = new tangible.OutObject<String>();
//C# TO JAVA CONVERTER WARNING: Nullable reference types have no equivalent in Java:
//ORIGINAL LINE: if (!EPCISDocumentBaseXMLMapper._schemaChecker.Validate(xdoc, "https://raw.githubusercontent.com/ift-gftc/doc.gdst/master/schemas/xml/epcis_1_2/EPCglobal-epcis-1_2.xsd", out string? error))
			if (!XmlSchemaChecker.validate(xdoc, "https://raw.githubusercontent.com/ift-gftc/doc.gdst/master/schemas/xml/epcis_1_2/EPCglobal-epcis-1_2.xsd", tempOut_error))
			{
			error = tempOut_error.outArgValue;
				throw new OpenTraceabilitySchemaException("Failed to validate the XML schema for the EPCIS XML.\n" + error);
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
			if (!XmlSchemaChecker.validate(xdoc, "https://ref.gs1.org/standards/epcis/epcglobal-epcis-2_0.xsd", tempOut_error2))
			{
			error = tempOut_error2.outArgValue;
				throw new OpenTraceabilitySchemaException("Failed to validate the XML schema for the EPCIS XML.\n" + error);
			}
		else
		{
			error = tempOut_error2.outArgValue;
		}
		}
	}

	public static void ValidateEPCISQueryDocumentSchema(XElement xdoc, EPCISVersion version)
	{
		if (version == EPCISVersion.V1)
		{
			// validate the schema depending on the version in the document
			String error;
			tangible.OutObject<String> tempOut_error = new tangible.OutObject<String>();
//C# TO JAVA CONVERTER WARNING: Nullable reference types have no equivalent in Java:
//ORIGINAL LINE: if (!EPCISDocumentBaseXMLMapper._schemaChecker.Validate(xdoc, "https://raw.githubusercontent.com/ift-gftc/doc.gdst/master/schemas/xml/epcis_1_2/EPCglobal-epcis-query-1_2.xsd", out string? error))
			if (!XmlSchemaChecker.validate(xdoc, "https://raw.githubusercontent.com/ift-gftc/doc.gdst/master/schemas/xml/epcis_1_2/EPCglobal-epcis-query-1_2.xsd", tempOut_error))
			{
			error = tempOut_error.outArgValue;
				throw new RuntimeException("Failed to validate the XML schema for the EPCIS XML.\n" + error);
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
			if (!XmlSchemaChecker.validate(xdoc, "https://ref.gs1.org/standards/epcis/epcglobal-epcis-query-2_0.xsd", tempOut_error2))
			{
			error = tempOut_error2.outArgValue;
				throw new RuntimeException("Failed to validate the XML schema for the EPCIS XML.\n" + error);
			}
		else
		{
			error = tempOut_error2.outArgValue;
		}
		}
	}
}
