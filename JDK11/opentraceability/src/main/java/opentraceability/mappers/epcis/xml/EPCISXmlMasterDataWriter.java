package opentraceability.mappers.epcis.xml;


import opentraceability.interfaces.*;
import opentraceability.models.common.*;
import opentraceability.models.events.*;
import opentraceability.models.identifiers.*;
import opentraceability.models.masterdata.*;
import opentraceability.utility.ReflectionUtility;
import opentraceability.utility.XElement;
import opentraceability.utility.attributes.*;
import opentraceability.*;
import opentraceability.mappers.*;
import org.json.JSONObject;

import javax.xml.xpath.XPathExpressionException;

import java.util.*;
import java.util.stream.Collectors;

public class EPCISXmlMasterDataWriter
{
	public static void WriteMasterData(XElement xDocument, EPCISBaseDocument doc) throws Exception {
		if (!doc.masterData.isEmpty())
		{
			XElement xEPCISHeader = xDocument.Element("EPCISHeader");
			if (xEPCISHeader.IsNull)
			{
				xDocument.Add(new XElement("EPCISHeader", new XElement("extension", new XElement("EPCISMasterData", new XElement("VocabularyList")))));
			}
			else
			{
				xEPCISHeader.Add(new XElement("extension", new XElement("EPCISMasterData", new XElement("VocabularyList"))));
			}
			XElement xVocabList = xDocument.Element("EPCISHeader/extension/EPCISMasterData/VocabularyList");

			for (var mdList : doc.masterData.stream().collect(Collectors.groupingBy(IVocabularyElement::getEpcisType)).entrySet())
			{
				if (mdList.getKey() != null)
				{
					WriteMasterDataList(mdList.getValue(), xVocabList, mdList.getKey());
				}
				else
				{
					throw new RuntimeException("There are master data vocabulary elements where the Type is NULL.");
				}
			}
		}
	}

	private static void WriteMasterDataList(List<IVocabularyElement> data, XElement xVocabList, String type) throws Exception {
		if (!data.isEmpty())
		{
			XElement xVocab = new XElement("Vocabulary", new XElement("VocabularyElementList"));
			xVocab.SetAttributeValue("type", type);
			XElement xVocabEleList = xVocab.Element("VocabularyElementList");

			for (IVocabularyElement md : data)
			{
				XElement xMD = WriteMasterDataObject(md);
				xVocabEleList.Add(xMD);
			}

			xVocabList.Add(xVocab);
		}
	}

	private static XElement WriteMasterDataObject(IVocabularyElement md) throws Exception {
		XElement xVocabEle = new XElement("VocabularyElement");
		xVocabEle.SetAttributeValue("id", md.id);

		var mappings = OTMappingTypeInformation.getMasterDataXmlTypeInfo(md.getClass());

		for (var mapping : mappings.properties)
		{
			Object o = mapping.field.get(md);
			if (o != null)
			{
				if (Objects.equals(mapping.name, ""))
				{
					var subMappings = OTMappingTypeInformation.getMasterDataXmlTypeInfo(o.getClass());
					for (var subMapping : subMappings.properties)
					{
						Object subObj = subMapping.field.get(o);
						if (subObj != null)
						{
							if (subObj instanceof ArrayList)
							{
								ArrayList list = (ArrayList)subObj;
								if (list.isEmpty())
								{
									var i = list.get(0);
									if (i instanceof LanguageString)
									{
										String str = ((LanguageString)i).value;
										XElement x = xVocabEle.Add("attribute");
										x.SetAttributeValue("id", subMapping.name);
										x.setValue(str);
									}
								}
							}
							else
							{
								String str = subObj.toString();
								if (str != null)
								{
									XElement x = xVocabEle.Add("attribute");
									x.SetAttributeValue("id", subMapping.name);
									x.setValue(str);
								}
							}
						}
					}
				}
				else if (ReflectionUtility.getFieldAnnotation(mapping.field, OpenTraceabilityObjectAttribute.class) != null)
				{
					XElement x = xVocabEle.Add("attribute");
					x.SetAttributeValue("id", mapping.name);
					WriteObject(x, mapping.field.getDeclaringClass(), o);
				}
				else if (ReflectionUtility.getFieldAnnotation(mapping.field, OpenTraceabilityArrayAttribute.class) != null)
				{
					List l = (List)o;
					for (var i : l)
					{
						String str = i.toString();
						if (str != null)
						{
							XElement x = xVocabEle.Add("attribute");
							x.SetAttributeValue("id", mapping.name);
							x.setValue(str);
						}
					}
				}
				else if (o instanceof ArrayList)
				{
					ArrayList list = (ArrayList)o;
					if (list.isEmpty())
					{
						var i = list.get(0);
						if (i instanceof LanguageString)
						{
							String str = ((LanguageString)i).value;
							XElement x = xVocabEle.Add("attribute");
							x.SetAttributeValue("id", mapping.name);
							x.setValue(str);
						}
					}
				}
				else
				{
					String str = o.toString();
					if (str != null)
					{
						XElement x = xVocabEle.Add("attribute");
						x.SetAttributeValue("id", mapping.name);
						x.setValue(str);
					}
				}
			}
		}

		for (IMasterDataKDE kde : md.kdes)
		{
			XElement xKDE = kde.getEPCISXml();
			if (xKDE != null)
			{
				xVocabEle.Add(xKDE);
			}
		}

		return xVocabEle;
	}

	private static void WriteObject(XElement x, Class t, Object o) throws Exception
	{
		for (var property : t.getClass().getFields())
		{
			Object value = property.get(o);
			if (value != null)
			{
				OpenTraceabilityAttribute xmlAtt = ReflectionUtility.getFieldAnnotation(property, OpenTraceabilityAttribute.class);
				if (xmlAtt != null)
				{
					XElement xchild = new XElement(xmlAtt.ns(), xmlAtt.name());
					if (ReflectionUtility.getFieldAnnotation(property, OpenTraceabilityObjectAttribute.class) != null)
					{
						WriteObject(xchild, property.getType(), value);
					}
					else
					{
						String str = value.toString();
						if (str != null)
						{
							xchild.setValue(str);
						}
					}
					x.Add(xchild);
				}
			}
		}
	}
}
