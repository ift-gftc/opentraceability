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
	public static void WriteMasterData(JSONObject jDoc, EPCISBaseDocument doc)
	{
		if (!doc.masterData.isEmpty())
		{
			JSONObject xEPCISHeader = jDoc["epcisHeader"] instanceof JSONObject ? (JSONObject)jDoc["epcisHeader"] : null;
			if (xEPCISHeader == null)
			{
				jDoc["epcisHeader"] = new JSONObject(new JProperty("epcisMasterData", new JSONObject(new JProperty("vocabularyList", new JSONArray()))));
			}
			else
			{
				xEPCISHeader["epcisMasterData"] = new JSONObject(new JProperty("vocabularyList", new JSONArray()));
			}
			Object tempVar = ((jDoc["epcisHeader"]["epcisMasterData"] == null ? null : jDoc["epcisHeader"]["epcisMasterData"]["vocabularyList"]));
//C# TO JAVA CONVERTER TASK: Throw expressions are not converted by C# to Java Converter:
//ORIGINAL LINE: JSONArray jVocabList = jDoc["epcisHeader"] == null ? null : tempVar instanceof JSONArray ? (JSONArray)tempVar : null ?? throw new Exception("Failed to grab json object epcisHeader.epcisMasterData.vocabularyList");
			JSONArray jVocabList = jDoc["epcisHeader"] == null ? null : tempVar instanceof JSONArray ? (JSONArray)tempVar : (null != null ? null : throw new RuntimeException("Failed to grab json object epcisHeader.epcisMasterData.vocabularyList"));

			for (var mdList : doc.masterData.GroupBy(m -> m.EPCISType))
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

	private static void WriteMasterDataList(ArrayList<IVocabularyElement> data, JSONArray xVocabList, String type)
	{
		if (!data.isEmpty())
		{
			JSONObject jVocab = new JSONObject(new JProperty("type", type), new JProperty("vocabularyElementList", new JSONArray()));
//C# TO JAVA CONVERTER TASK: Throw expressions are not converted by C# to Java Converter:
//ORIGINAL LINE: JSONArray xVocabEleList = jVocab["vocabularyElementList"] instanceof JSONArray ? (JSONArray)jVocab["vocabularyElementList"] : null ?? throw new Exception("Failed to grab the array vocabularyElementList");
			JSONArray xVocabEleList = jVocab["vocabularyElementList"] instanceof JSONArray ? (JSONArray)jVocab["vocabularyElementList"] : (null != null ? null : throw new RuntimeException("Failed to grab the array vocabularyElementList"));

			for (IVocabularyElement md : data)
			{
				JSONObject xMD = WriteMasterDataObject(md);
				xVocabEleList.Add(xMD);
			}

			xVocabList.Add(jVocab);
		}
	}

	private static JSONObject WriteMasterDataObject(IVocabularyElement md)
	{
		JSONObject jVocabElement = new JSONObject(new JProperty("id", md.getID() != null ? md.getID() : ""), new JProperty("attributes", new JSONArray()));
//C# TO JAVA CONVERTER TASK: Throw expressions are not converted by C# to Java Converter:
//ORIGINAL LINE: JSONArray jAttributes = jVocabElement["attributes"] instanceof JSONArray ? (JSONArray)jVocabElement["attributes"] : null ?? throw new Exception("Failed to grab attributes array.");
		JSONArray jAttributes = jVocabElement["attributes"] instanceof JSONArray ? (JSONArray)jVocabElement["attributes"] : (null != null ? null : throw new RuntimeException("Failed to grab attributes array."));

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
									JSONObject jAttribute = new JSONObject(new JProperty("id", subID), new JProperty("attribute", str));
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
									JSONObject jAttribute = new JSONObject(new JProperty("id", subID), new JProperty("attribute", str));
									jAttributes.Add(jAttribute);
								}
							}
						}
					}
				}
				else if (p.<OpenTraceabilityObjectAttribute>GetCustomAttribute() != null)
				{
					JSONObject jAttribute = new JSONObject(new JProperty("id", id), new JProperty("attribute", WriteObject(p.PropertyType, o)));
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
							JSONObject jAttribute = new JSONObject(new JProperty("id", id), new JProperty("attribute", str));
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
						JSONObject jAttribute = new JSONObject(new JProperty("id", id), new JProperty("attribute", str));
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
						JSONObject jAttribute = new JSONObject(new JProperty("id", id), new JProperty("attribute", str));
						jAttributes.Add(jAttribute);
					}
				}
			}
		}

		for (IMasterDataKDE kde : md.getKDEs())
		{
			Object jKDE = kde.GetGS1WebVocabJson();
			if (jKDE != null)
			{
				JSONObject jAttribute = new JSONObject(new JProperty("id", kde.getName()), new JProperty("attribute", jKDE));
				jAttributes.Add(jAttribute);
			}
		}

		return jVocabElement;
	}

	private static JSONObject WriteObject(Type t, Object o)
	{
		JSONObject j = new JSONObject();
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
