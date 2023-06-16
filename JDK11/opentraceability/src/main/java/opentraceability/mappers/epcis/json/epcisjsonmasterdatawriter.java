package opentraceability.mappers.epcis.json;

import Newtonsoft.Json.Linq.*;
import opentraceability.interfaces.*;
import opentraceability.models.common.*;
import opentraceability.models.events.*;
import opentraceability.utility.attributes.*;
import opentraceability.*;
import opentraceability.mappers.*;
import opentraceability.mappers.epcis.*;
import java.util.*;

/** 
 Used for writing master data into an EPCIS JSON-LD file.
*/
public final class EPCISJsonMasterDataWriter
{
	public static void WriteMasterData(JObject jDoc, EPCISBaseDocument doc)
	{
		if (!doc.getMasterData().isEmpty())
		{
			JObject xEPCISHeader = jDoc["epcisHeader"] instanceof JObject ? (JObject)jDoc["epcisHeader"] : null;
			if (xEPCISHeader == null)
			{
				jDoc["epcisHeader"] = new JObject(new JProperty("epcisMasterData", new JObject(new JProperty("vocabularyList", new JArray()))));
			}
			else
			{
				xEPCISHeader["epcisMasterData"] = new JObject(new JProperty("vocabularyList", new JArray()));
			}
			Object tempVar = ((jDoc["epcisHeader"]["epcisMasterData"] == null ? null : jDoc["epcisHeader"]["epcisMasterData"]["vocabularyList"]));
//C# TO JAVA CONVERTER TASK: Throw expressions are not converted by C# to Java Converter:
//ORIGINAL LINE: JArray jVocabList = jDoc["epcisHeader"] == null ? null : tempVar instanceof JArray ? (JArray)tempVar : null ?? throw new Exception("Failed to grab json object epcisHeader.epcisMasterData.vocabularyList");
			JArray jVocabList = jDoc["epcisHeader"] == null ? null : tempVar instanceof JArray ? (JArray)tempVar : (null != null ? null : throw new RuntimeException("Failed to grab json object epcisHeader.epcisMasterData.vocabularyList"));

			for (var mdList : doc.getMasterData().GroupBy(m -> m.EPCISType))
			{
				if (mdList.Key != null)
				{
					WriteMasterDataList(mdList.ToList(), jVocabList, mdList.Key);
				}
				else
				{
					throw new RuntimeException("There are master data vocabulary elements where the Type is NULL.");
				}
			}
		}
	}

	private static void WriteMasterDataList(ArrayList<IVocabularyElement> data, JArray xVocabList, String type)
	{
		if (!data.isEmpty())
		{
			JObject jVocab = new JObject(new JProperty("type", type), new JProperty("vocabularyElementList", new JArray()));
//C# TO JAVA CONVERTER TASK: Throw expressions are not converted by C# to Java Converter:
//ORIGINAL LINE: JArray xVocabEleList = jVocab["vocabularyElementList"] instanceof JArray ? (JArray)jVocab["vocabularyElementList"] : null ?? throw new Exception("Failed to grab the array vocabularyElementList");
			JArray xVocabEleList = jVocab["vocabularyElementList"] instanceof JArray ? (JArray)jVocab["vocabularyElementList"] : (null != null ? null : throw new RuntimeException("Failed to grab the array vocabularyElementList"));

			for (IVocabularyElement md : data)
			{
				JObject xMD = WriteMasterDataObject(md);
				xVocabEleList.Add(xMD);
			}

			xVocabList.Add(jVocab);
		}
	}

	private static JObject WriteMasterDataObject(IVocabularyElement md)
	{
		JObject jVocabElement = new JObject(new JProperty("id", md.getID() != null ? md.getID() : ""), new JProperty("attributes", new JArray()));
//C# TO JAVA CONVERTER TASK: Throw expressions are not converted by C# to Java Converter:
//ORIGINAL LINE: JArray jAttributes = jVocabElement["attributes"] instanceof JArray ? (JArray)jVocabElement["attributes"] : null ?? throw new Exception("Failed to grab attributes array.");
		JArray jAttributes = jVocabElement["attributes"] instanceof JArray ? (JArray)jVocabElement["attributes"] : (null != null ? null : throw new RuntimeException("Failed to grab attributes array."));

		var mappings = OTMappingTypeInformation.GetMasterDataXmlTypeInfo(md.getClass());

		for (var mapping : mappings.getProperties())
		{
			String id = mapping.getName();
			PropertyInfo p = mapping.getProperty();

//C# TO JAVA CONVERTER WARNING: Nullable reference types have no equivalent in Java:
//ORIGINAL LINE: object? o = p.GetValue(md);
			Object o = p.GetValue(md);
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
//ORIGINAL LINE: object? subObj = subProperty.GetValue(o);
						Object subObj = subProperty.GetValue(o);
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
									JObject jAttribute = new JObject(new JProperty("id", subID), new JProperty("attribute", str));
									jAttributes.Add(jAttribute);
								}
							}
							else
							{
//C# TO JAVA CONVERTER WARNING: Nullable reference types have no equivalent in Java:
//ORIGINAL LINE: string? str = subObj.ToString();
								String str = subObj.toString();
								if (str != null)
								{
									JObject jAttribute = new JObject(new JProperty("id", subID), new JProperty("attribute", str));
									jAttributes.Add(jAttribute);
								}
							}
						}
					}
				}
				else if (p.<OpenTraceabilityObjectAttribute>GetCustomAttribute() != null)
				{
					JObject jAttribute = new JObject(new JProperty("id", id), new JProperty("attribute", WriteObject(p.PropertyType, o)));
					jAttributes.Add(jAttribute);
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
							JObject jAttribute = new JObject(new JProperty("id", id), new JProperty("attribute", str));
							jAttributes.Add(jAttribute);
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
						JObject jAttribute = new JObject(new JProperty("id", id), new JProperty("attribute", str));
						jAttributes.Add(jAttribute);
					}
				}
				else
				{
//C# TO JAVA CONVERTER WARNING: Nullable reference types have no equivalent in Java:
//ORIGINAL LINE: string? str = o.ToString();
					String str = o.toString();
					if (str != null)
					{
						JObject jAttribute = new JObject(new JProperty("id", id), new JProperty("attribute", str));
						jAttributes.Add(jAttribute);
					}
				}
			}
		}

		for (IMasterDataKDE kde : md.getKDEs())
		{
			JToken jKDE = kde.GetGS1WebVocabJson();
			if (jKDE != null)
			{
				JObject jAttribute = new JObject(new JProperty("id", kde.getName()), new JProperty("attribute", jKDE));
				jAttributes.Add(jAttribute);
			}
		}

		return jVocabElement;
	}

	private static JObject WriteObject(java.lang.Class t, Object o)
	{
		JObject j = new JObject();
		for (var property : t.GetProperties())
		{
//C# TO JAVA CONVERTER WARNING: Nullable reference types have no equivalent in Java:
//ORIGINAL LINE: object? value = property.GetValue(o);
			Object value = property.GetValue(o);
			if (value != null)
			{
//C# TO JAVA CONVERTER WARNING: Nullable reference types have no equivalent in Java:
//ORIGINAL LINE: OpenTraceabilityAttribute? xmlAtt = property.GetCustomAttribute<OpenTraceabilityAttribute>();
				OpenTraceabilityAttribute xmlAtt = property.<OpenTraceabilityAttribute>GetCustomAttribute();
				if (xmlAtt != null)
				{
					if (property.<OpenTraceabilityObjectAttribute>GetCustomAttribute() != null)
					{
						j[xmlAtt.getName()] = WriteObject(property.PropertyType, value);
					}
					else
					{
//C# TO JAVA CONVERTER WARNING: Nullable reference types have no equivalent in Java:
//ORIGINAL LINE: string? str = value.ToString();
						String str = value.toString();
						if (str != null)
						{
							j[xmlAtt.getName()] = str;
						}
					}
				}
			}
		}
		return j;
	}
}
