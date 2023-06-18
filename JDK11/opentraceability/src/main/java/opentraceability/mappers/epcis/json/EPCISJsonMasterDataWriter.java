package opentraceability.mappers.epcis.json;

import opentraceability.interfaces.*;
import opentraceability.models.common.*;
import opentraceability.models.events.*;
import opentraceability.utility.JSONExtensions;
import opentraceability.utility.ReflectionUtility;
import opentraceability.utility.attributes.*;
import opentraceability.*;
import opentraceability.mappers.*;
import org.json.JSONArray;
import org.json.JSONObject;


import java.util.*;
import java.util.stream.Collectors;

/** 
 Used for writing master data into an EPCIS JSON-LD file.
*/
public final class EPCISJsonMasterDataWriter
{
	public static void WriteMasterData(JSONObject jDoc, EPCISBaseDocument doc) throws Exception {
		if (!doc.masterData.isEmpty())
		{
			JSONObject xEPCISHeader = JSONExtensions.queryForObject(jDoc, "epcisHeader");
			if (xEPCISHeader == null)
			{
				JSONObject j = new JSONObject();
				j.put("vocabularyList", new JSONArray());
				JSONObject j2 = new JSONObject();
				j2.put("epcisMasterData", j);
				jDoc.put("epcisHeader", j2);
			}
			else
			{
				JSONObject j = new JSONObject();
				j.put("vocabularyList", new JSONArray());
				xEPCISHeader.put("epcisMasterData", j);
			}

			JSONArray jVocabList = JSONExtensions.queryForArray(jDoc,"epcisHeader.epcisMasterData.vocabularyList");

			for (var mdList : doc.masterData.stream().collect(Collectors.groupingBy(IVocabularyElement::getEpcisType)).entrySet())
			{
				if (mdList.getKey() != null)
				{
					WriteMasterDataList(mdList.getValue(), jVocabList, mdList.getKey());
				}
				else
				{
					throw new RuntimeException("There are master data vocabulary elements where the Type is NULL.");
				}
			}
		}
	}

	private static void WriteMasterDataList(List<IVocabularyElement> data, JSONArray xVocabList, String type) throws Exception {
		if (!data.isEmpty())
		{
			JSONArray xVocabEleList = new JSONArray();
			JSONObject jVocab = new JSONObject();
			jVocab.put("type", type);

			for (IVocabularyElement md : data)
			{
				JSONObject xMD = WriteMasterDataObject(md);
				xVocabEleList.put(xMD);
			}

			xVocabList.put(jVocab);
		}
	}

	private static JSONObject WriteMasterDataObject(IVocabularyElement md) throws Exception {
		JSONArray jAttributes = new JSONArray();
		JSONObject jVocabElement = new JSONObject();
		jVocabElement.put("id", md.id);
		jVocabElement.put("attributes", jAttributes);

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
						String subID = subMapping.name;
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
										JSONObject jAtt = new JSONObject();
										jAtt.put("id", subID);
										jAtt.put("attributes", str);
										jAttributes.put(jAtt);
									}
								}
							}
							else
							{
								String str = subObj.toString();
								if (str != null)
								{
									JSONObject jAtt = new JSONObject();
									jAtt.put("id", subID);
									jAtt.put("attributes", str);
									jAttributes.put(jAtt);
								}
							}
						}
					}
				}
				else if (ReflectionUtility.getFieldAnnotation(mapping.field, OpenTraceabilityObjectAttribute.class) != null)
				{
					Object val = WriteObject(mapping.field.getDeclaringClass(), o);

					JSONObject jAtt = new JSONObject();
					jAtt.put("id", mapping.name);
					jAtt.put("attributes", val);
					jAttributes.put(jAtt);
				}
				else if (ReflectionUtility.getFieldAnnotation(mapping.field, OpenTraceabilityArrayAttribute.class) != null)
				{
					List l = (List)o;
					for (var i : l)
					{
						String str = i.toString();
						if (str != null)
						{
							JSONObject jAtt = new JSONObject();
							jAtt.put("id", mapping.name);
							jAtt.put("attributes", str);
							jAttributes.put(jAtt);
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
							JSONObject jAtt = new JSONObject();
							jAtt.put("id", mapping.name);
							jAtt.put("attributes", str);
							jAttributes.put(jAtt);
						}
					}
				}
				else
				{
					String str = o.toString();
					if (str != null)
					{
						JSONObject jAtt = new JSONObject();
						jAtt.put("id", mapping.name);
						jAtt.put("attributes", str);
						jAttributes.put(jAtt);
					}
				}
			}
		}

		for (IMasterDataKDE kde : md.kdes)
		{
			Object jKDE = kde.getGS1WebVocabJson();
			if (jKDE != null)
			{
				JSONObject jAtt = new JSONObject();
				jAtt.put("id", kde.name);
				jAtt.put("attributes", jKDE);
				jAttributes.put(jAtt);
			}
		}

		return jVocabElement;
	}

	private static JSONObject WriteObject(Class t, Object o) throws Exception {
		JSONObject j = new JSONObject();
		for (var property : t.getClass().getFields())
		{
			Object value = property.get(o);
			if (value != null)
			{
				OpenTraceabilityAttribute att = ReflectionUtility.getFieldAnnotation(property, OpenTraceabilityAttribute.class);
				if (att != null)
				{
					if (ReflectionUtility.getFieldAnnotation(property, OpenTraceabilityObjectAttribute.class) != null)
					{
						var propertyObj = WriteObject(property.getType(), value);
						j.put(att.name(), propertyObj);
					}
					else
					{
						String str = value.toString();
						if (str != null)
						{
							j.put(att.name(), str);
						}
					}
				}
			}
		}
		return j;
	}
}
