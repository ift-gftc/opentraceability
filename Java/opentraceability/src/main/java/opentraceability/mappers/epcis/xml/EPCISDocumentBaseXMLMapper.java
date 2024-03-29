package opentraceability.mappers.epcis.xml;


import opentraceability.interfaces.*;
import opentraceability.models.common.*;
import opentraceability.models.events.*;
import opentraceability.utility.*;
import opentraceability.*;
import opentraceability.mappers.*;
import tangible.StringHelper;

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

	public static XElement WriteXml(EPCISBaseDocument doc, String epcisPrefix, String rootEleName) throws Exception {
		if (doc.epcisVersion == null)
		{
			throw new RuntimeException("doc.EPCISVersion is NULL. This must be set to a version.");
		}

		// create a new Document with all the namespaces...
		XElement xDoc = new XElement(epcisPrefix + ":" + rootEleName);
		for (var a: doc.attributes.entrySet())
		{
			xDoc.Add(new XAttribute(a.getKey(), a.getValue()));
		}

		for (var ns : doc.namespaces.entrySet())
		{
			String prefix = ns.getKey();
			String uri = ns.getValue();
			if (uri.equals(Constants.CBVMDA_NAMESPACE) || uri.equals(Constants.EPCISQUERY_1_NAMESPACE) || uri.equals(Constants.EPCISQUERY_2_NAMESPACE) || uri.equals(Constants.EPCIS_1_NAMESPACE) || uri.equals(Constants.EPCIS_2_NAMESPACE))
			{
				continue;
			}
			else
			{
				xDoc.AddNamespace(prefix, uri);
			}
		}

		// set the creation date
		if (doc.creationDate != null)
		{
			xDoc.Add(new XAttribute("creationDate", doc.creationDate.format(DateTimeFormatter.ISO_OFFSET_DATE_TIME)));
		}

		xDoc.RemoveNamespace("epcis");
		xDoc.RemoveNamespace("cbvmda");

		if (doc.epcisVersion == EPCISVersion.V2)
		{
			xDoc.Add(new XAttribute("schemaVersion", "2.0"));
			if (doc instanceof EPCISQueryDocument)
			{
				xDoc.AddNamespace("epcisq", Constants.EPCISQUERY_2_NAMESPACE);
				xDoc.AddNamespace("cbvmda", Constants.CBVMDA_NAMESPACE);
			}
			else
			{
				xDoc.AddNamespace("epcis", Constants.EPCIS_2_NAMESPACE);
				xDoc.AddNamespace("cbvmda", Constants.CBVMDA_NAMESPACE);
			}
		}
		else if (doc.epcisVersion == EPCISVersion.V1)
		{
			xDoc.Add(new XAttribute("schemaVersion", "1.2"));
			if (doc instanceof EPCISQueryDocument)
			{
				xDoc.AddNamespace("epcisq", Constants.EPCISQUERY_1_NAMESPACE);
				xDoc.AddNamespace("cbvmda", Constants.CBVMDA_NAMESPACE);
			}
			else
			{
				xDoc.AddNamespace("epcis", Constants.EPCIS_1_NAMESPACE);
				xDoc.AddNamespace("cbvmda", Constants.CBVMDA_NAMESPACE);
			}
		}

		// write the standard business document header
		if (doc.header != null)
		{
			String prefix = xDoc.GetNamespacesAndPrefixesMap().get(Constants.SBDH_XNAMESPACE);
			XElement xHeader = OpenTraceabilityXmlMapper.ToXml(null,prefix + ":StandardBusinessDocumentHeader", doc.header, doc.epcisVersion, false);
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
		String eventType = xEvent.getTagName();

		if (bizStep == null)
		{
			bizStep = "";
		}

		String finalBizStep = bizStep.toLowerCase();
		var profiles = Setup.Profiles.stream().filter(p -> p.EventType.toString().toLowerCase().equals(eventType.toLowerCase())
				&& (p.Action == null || p.Action.toString().equals(action))
				&& (StringHelper.isNullOrEmpty(p.BusinessStep) || p.BusinessStep.toLowerCase().equals(finalBizStep)));

		List<OpenTraceabilityEventProfile> finalProfiles = profiles.collect(Collectors.toList());

		if (finalProfiles.size() == 0)
		{
			throw new RuntimeException("Failed to create event from profile. Type=" + eventType + " and BizStep=" + bizStep + " and Action=" + action);
		}
		else
		{
			for (var profile : finalProfiles.stream().filter(p -> p.KDEProfiles != null).collect(Collectors.toList()))
			{
				if (profile.KDEProfiles != null)
				{
					for (var kdeProfile : profile.KDEProfiles)
					{
						try
						{
							if (xEvent.Element(kdeProfile.XPath_V1).IsNull && xEvent.Element(kdeProfile.XPath_V2).IsNull)
							{
								finalProfiles.remove(profile);
							}
						}
						catch (Exception ex)
						{
							if (!ex.getMessage().contains("Prefix must resolve to a namespace"))
							{
								throw ex;
							}
						}
					}
				}
			}

			if (finalProfiles.size() == 0)
			{
				throw new RuntimeException("Failed to create event from profile. Type=" + eventType + " and BizStep=" + bizStep + " and Action=" + action);
			}

			finalProfiles.sort(Comparator.comparingInt(OpenTraceabilityEventProfile::getSpecificityScore).reversed());
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

	public static void ValidateEPCISDocumentSchema(XElement xdoc, EPCISVersion version) throws OpenTraceabilitySchemaException
	{
		if (version == EPCISVersion.V1)
		{
			// validate the schema depending on the version in the document
			String error;
			tangible.OutObject<String> tempOut_error = new tangible.OutObject<String>();

			if (!XmlSchemaChecker.validate(xdoc, "/schema/epcis_1_2/EPCglobal-epcis-1_2.xsd", tempOut_error))
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
			if (!XmlSchemaChecker.validate(xdoc, "/schema/epcis_2_0/epcglobal-epcis-2_0.xsd", tempOut_error2))
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
			String error;
			tangible.OutObject<String> tempOut_error = new tangible.OutObject<String>();
			if (!XmlSchemaChecker.validate(xdoc, "/schema/epcis_1_2/EPCglobal-epcis-query-1_2.xsd", tempOut_error))
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
			String error;
			tangible.OutObject<String> tempOut_error2 = new tangible.OutObject<String>();
			if (!XmlSchemaChecker.validate(xdoc, "/schema/epcis_2_0/epcglobal-epcis-query-2_0.xsd", tempOut_error2))
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
