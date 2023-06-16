package opentraceability.mappers.epcis.xml;

import Newtonsoft.Json.Linq.*;
import opentraceability.interfaces.*;
import opentraceability.models.common.*;
import opentraceability.models.events.*;
import opentraceability.models.identifiers.*;
import opentraceability.models.masterdata.*;
import opentraceability.utility.attributes.*;
import opentraceability.*;
import opentraceability.mappers.*;
import opentraceability.mappers.epcis.*;
import java.util.*;

public class EPCISXmlMasterDataWriter
{
	public static void WriteMasterData(XElement xDocument, EPCISBaseDocument doc)
	{
		if (!doc.masterData.isEmpty())
		{
			XElement xEPCISHeader = xDocument.Element("EPCISHeader");
			if (xEPCISHeader == null)
			{
				xDocument.Add(new XElement("EPCISHeader", new XElement("extension", new XElement("EPCISMasterData", new XElement("VocabularyList")))));
			}
			else
			{
				xEPCISHeader.Add(new XElement("extension", new XElement("EPCISMasterData", new XElement("VocabularyList"))));
			}
//C# TO JAVA CONVERTER TASK: Throw expressions are not converted by C# to Java Converter:
//ORIGINAL LINE: XElement xVocabList = xDocument.XPathSelectElement("EPCISHeader/extension/EPCISMasterData/VocabularyList") ?? throw new Exception("Failed to grab the element EPCISHeader/extension/EPCISMasterData/VocabularyList.");
			XElement xVocabList = xDocument.XPathSelectElement("EPCISHeader/extension/EPCISMasterData/VocabularyList") != null ? xDocument.XPathSelectElement("EPCISHeader/extension/EPCISMasterData/VocabularyList") : throw new RuntimeException("Failed to grab the element EPCISHeader/extension/EPCISMasterData/VocabularyList.");

			for (var mdList : doc.masterData.GroupBy(m -> m.EPCISType))
			{
				if (mdList.Key != null)
				{
					WriteMasterDataList(mdList.ToList(), xVocabList, mdList.Key);
				}
				else
				{
					throw new RuntimeException("There are master data vocabulary elements where the Type is NULL.");
				}
			}
		}
	}

	private static void WriteMasterDataList(ArrayList<IVocabularyElement> data, XElement xVocabList, String type)
	{
		if (!data.isEmpty())
		{
			XElement xVocab = new XElement("Vocabulary", new XAttribute("type", type), new XElement("VocabularyElementList"));
//C# TO JAVA CONVERTER TASK: Throw expressions are not converted by C# to Java Converter:
//ORIGINAL LINE: XElement xVocabEleList = xVocab.Element("VocabularyElementList") ?? throw new Exception("Failed to grab the element VocabularyElementList");
			XElement xVocabEleList = xVocab.Element("VocabularyElementList") != null ? xVocab.Element("VocabularyElementList") : throw new RuntimeException("Failed to grab the element VocabularyElementList");

			for (IVocabularyElement md : data)
			{
				XElement xMD = WriteMasterDataObject(md);
				xVocabEleList.Add(xMD);
			}

			xVocabList.Add(xVocab);
		}
	}

	private static XElement WriteMasterDataObject(IVocabularyElement md)
	{
		XElement xVocabEle = new XElement("VocabularyElement");
		xVocabEle.Add(new XAttribute("id", md.getID() != null ? md.getID() : ""));

		var mappings = OTMappingTypeInformation.GetMasterDataXmlTypeInfo(md.getClass());

		for (var mapping : mappings.getProperties())
		{
			String id = mapping.getName();
			PropertyInfo p = mapping.getProperty();

//C# TO JAVA CONVERTER WARNING: Nullable reference types have no equivalent in Java:
//ORIGINAL LINE: object? o = p.get(md);
			Object o = p.get(md);
			if (o != null)
			{
				if (Objects.equals(id, ""))
				{
					var subMappings = OTMappingTypeInformation.GetMasterDataXmlTypeInfo(o.getClass());
					for (var subMapping : subMappings.getProperties())
					{
						String subID = subMapping.getName();
						PropertyInfo subProperty = subMapping.getProperty();
//C# TO JAVA CONVERTER WARNING: Nullable reference types have no equivalent in Java:
//ORIGINAL LINE: object? subObj = subProperty.get(o);
						Object subObj = subProperty.get(o);
						if (subObj != null)
						{
							if (subObj.getClass() == ArrayList<LanguageString>.class)
							{
								ArrayList<LanguageString> l = (ArrayList<LanguageString>)subObj;
//C# TO JAVA CONVERTER WARNING: Nullable reference types have no equivalent in Java:
//ORIGINAL LINE: string? str = l.FirstOrDefault() == null ? null : l.FirstOrDefault().Value;
								String str = l.FirstOrDefault() == null ? null : l.FirstOrDefault().Value;
								if (str != null)
								{
									XElement xAtt = new XElement("attribute", new XAttribute("id", subID));
									xAtt.Value = str;
									xVocabEle.Add(xAtt);
								}
							}
							else
							{
//C# TO JAVA CONVERTER WARNING: Nullable reference types have no equivalent in Java:
//ORIGINAL LINE: string? str = subObj.ToString();
								String str = subObj.toString();
								if (str != null)
								{
									XElement xAtt = new XElement("attribute", new XAttribute("id", subID));
									xAtt.Value = str;
									xVocabEle.Add(xAtt);
								}
							}
						}
					}
				}
				else if (p.<OpenTraceabilityObjectAttribute>GetCustomAttribute() != null)
				{
					XElement xAtt = new XElement("attribute", new XAttribute("id", id));
					WriteObject(xAtt, p.PropertyType, o);
					xVocabEle.Add(xAtt);
				}
				else if (p.<OpenTraceabilityArrayAttribute>GetCustomAttribute() != null)
				{
					List l = (List)o;
					for (var i : l)
					{
//C# TO JAVA CONVERTER WARNING: Nullable reference types have no equivalent in Java:
//ORIGINAL LINE: string? str = i.ToString();
						String str = i.toString();
						if (str != null)
						{
							XElement xAtt = new XElement("attribute", new XAttribute("id", id));
							xAtt.Value = str;
							xVocabEle.Add(xAtt);
						}
					}
				}
				else if (o.getClass() == ArrayList<LanguageString>.class)
				{
					ArrayList<LanguageString> l = (ArrayList<LanguageString>)o;
//C# TO JAVA CONVERTER WARNING: Nullable reference types have no equivalent in Java:
//ORIGINAL LINE: string? str = l.FirstOrDefault() == null ? null : l.FirstOrDefault().Value;
					String str = l.FirstOrDefault() == null ? null : l.FirstOrDefault().Value;
					if (str != null)
					{
						XElement xAtt = new XElement("attribute", new XAttribute("id", id));
						xAtt.Value = str;
						xVocabEle.Add(xAtt);
					}
				}
				else
				{
//C# TO JAVA CONVERTER WARNING: Nullable reference types have no equivalent in Java:
//ORIGINAL LINE: string? str = o.ToString();
					String str = o.toString();
					if (str != null)
					{
						XElement xAtt = new XElement("attribute", new XAttribute("id", id));
						xAtt.Value = str;
						xVocabEle.Add(xAtt);
					}
				}
			}
		}

		for (IMasterDataKDE kde : md.getKDEs())
		{
			XElement xKDE = kde.GetEPCISXml();
			if (xKDE != null)
			{
				xVocabEle.Add(xKDE);
			}
		}

		return xVocabEle;
	}

	private static void WriteObject(XElement x, Type t, Object o)
	{
		for (var property : t.GetProperties())
		{
//C# TO JAVA CONVERTER WARNING: Nullable reference types have no equivalent in Java:
//ORIGINAL LINE: object? value = property.get(o);
			Object value = property.get(o);
			if (value != null)
			{
//C# TO JAVA CONVERTER WARNING: Nullable reference types have no equivalent in Java:
//ORIGINAL LINE: OpenTraceabilityAttribute? xmlAtt = property.GetCustomAttribute<OpenTraceabilityAttribute>();
				OpenTraceabilityAttribute xmlAtt = property.<OpenTraceabilityAttribute>GetCustomAttribute();
				if (xmlAtt != null)
				{
					XElement xchild = new XElement(xmlAtt.getName());
					if (property.<OpenTraceabilityObjectAttribute>GetCustomAttribute() != null)
					{
						WriteObject(xchild, property.PropertyType, value);
					}
					else
					{
//C# TO JAVA CONVERTER WARNING: Nullable reference types have no equivalent in Java:
//ORIGINAL LINE: string? str = value.ToString();
						String str = value.toString();
						if (str != null)
						{
							xchild.Value = str;
						}
					}
					x.Add(xchild);
				}
			}
		}
	}
}
