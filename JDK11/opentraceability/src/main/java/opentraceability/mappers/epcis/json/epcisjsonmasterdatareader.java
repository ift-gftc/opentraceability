package opentraceability.mappers.epcis.json;

import opentraceability.interfaces.*;
import opentraceability.models.common.*;
import opentraceability.models.events.kdes.*;
import opentraceability.models.events.*;
import opentraceability.models.identifiers.*;
import opentraceability.models.masterdata.*;
import opentraceability.models.masterdata.kdes.MasterDataKDEObject;
import opentraceability.models.masterdata.kdes.MasterDataKDEString;
import opentraceability.utility.attributes.*;
import opentraceability.utility.*;
import opentraceability.*;
import opentraceability.mappers.*;
import opentraceability.mappers.epcis.*;
import org.apache.commons.codec.language.bm.Lang;
import org.json.JSONArray;
import org.json.JSONObject;

import java.lang.reflect.Field;
import java.lang.reflect.Type;
import java.net.URI;
import java.util.*;

/** 
 Used for reading master data from EPCIS JSON-LD file.
*/
public final class EPCISJsonMasterDataReader
{
	private static Field p;

	public static void ReadMasterData(EPCISBaseDocument doc, JSONObject jMasterData) throws Exception {
		JSONArray jVocabList = jMasterData.getJSONArray("vocabularyList");
		if (jVocabList != null)
		{
			for (int i = 0; i < jVocabList.length(); i++)
			{
				JSONObject jVocabListItem = jVocabList.getJSONObject(i);
				if (jVocabListItem != null)
				{
					String type = jVocabListItem.getString("type").toLowerCase();
					if (type != null)
					{
						JSONArray jVocabElementaryList = jVocabListItem.getJSONArray("vocabularyElementList");
						if (jVocabElementaryList != null)
						{
							for (int k = 0; k < jVocabElementaryList.length(); k++)
							{
								JSONObject jVocabEle = jVocabElementaryList.getJSONObject(k);
								switch (type)
								{
									case "urn:epcglobal:epcis:vtype:epcclass":
										ReadTradeitem(doc, jVocabEle, type);
										break;
									case "urn:epcglobal:epcis:vtype:location":
										ReadLocation(doc, jVocabEle, type);
										break;
									case "urn:epcglobal:epcis:vtype:party":
										ReadTradingParty(doc, jVocabEle, type);
										break;
									default:
										ReadUnknown(doc, jVocabEle, type);
										break;
								}
							}
						}
					}
				}
			}
		}
	}

	private static void ReadTradeitem(EPCISBaseDocument doc, JSONObject jTradeItem, String type) throws Exception {
		Type javaType = Setup.MasterDataTypes.get(type);
		if (javaType == null)
		{
			throw new Exception("Failed to read java type.");
		}

		Object o = javaType.getClass().newInstance();
		if (o instanceof  TradeItem)
		{
			TradeItem md = (TradeItem)o;
			md.id = jTradeItem.getString("id");
			md.gtin = new opentraceability.models.identifiers.GTIN(md.id);
			md.epcisType = type;

			// read the object
			ReadMasterDataObject(md, jTradeItem);
			doc.masterData.add(md);
		}
		else
		{
			throw new Exception(javaType.getTypeName() + " is not instance of TradeItem.");
		}
	}

	private static void ReadLocation(EPCISBaseDocument doc, JSONObject jLoc, String type) throws Exception {
		Type javaType = Setup.MasterDataTypes.get(type);
		if (javaType == null)
		{
			throw new Exception("Failed to read java type.");
		}

		Object o = javaType.getClass().newInstance();
		if (o instanceof  Location)
		{
			Location md = (Location)o;
			md.id = jLoc.getString("id");
			md.gln = new opentraceability.models.identifiers.GLN(md.id);
			md.epcisType = type;

			// read the object
			ReadMasterDataObject(md, jLoc);
			doc.masterData.add(md);
		}
		else
		{
			throw new Exception(javaType.getTypeName() + " is not instance of Location.");
		}
	}

	private static void ReadTradingParty(EPCISBaseDocument doc, JSONObject jTradingParty, String type) throws Exception {
		Type javaType = Setup.MasterDataTypes.get(type);
		if (javaType == null)
		{
			throw new Exception("Failed to read java type.");
		}

		Object o = javaType.getClass().newInstance();
		if (o instanceof  TradingParty)
		{
			TradingParty md = (TradingParty)o;
			md.id = jTradingParty.getString("id");
			md.pgln = new opentraceability.models.identifiers.PGLN(md.id);
			md.epcisType = type;

			// read the object
			ReadMasterDataObject(md, jTradingParty);
			doc.masterData.add(md);
		}
		else
		{
			throw new Exception(javaType.getTypeName() + " is not instance of TradingParty.");
		}
	}

	private static void ReadUnknown(EPCISBaseDocument doc, JSONObject jVocabEle, String type) throws Exception {
		// read the pgln from the id
		String id = jVocabEle.getString("id");
		VocabularyElement ele = new VocabularyElement();
		ele.id = id;
		ele.epcisType = type;

		// read the object
		ReadMasterDataObject(ele, jVocabEle);
		doc.masterData.add(ele);
	}


	private static void ReadMasterDataObject(IVocabularyElement md, JSONObject jMasterData) throws Exception {
		ReadMasterDataObject(md, jMasterData, true);
	}

//C# TO JAVA CONVERTER NOTE: Java does not support optional parameters. Overloaded method(s) are created above:
//ORIGINAL LINE: private static void ReadMasterDataObject(IVocabularyElement md, JSONObject jMasterData, bool readKDEs = true)
	private static void ReadMasterDataObject(IVocabularyElement md, JSONObject jMasterData, boolean readKDEs) throws Exception {
		var mappedProperties = OTMappingTypeInformation.getMasterDataJsonTypeInfo(md.getClass());

		JSONArray jAttributes = JSONExtensions.queryForArray(jMasterData,"attributes");
		if (jAttributes == null)
		{
			return;
		}

		// work on expanded objects...
		// these are objects on the vocab element represented by one or more attributes in the EPCIS master data
		ArrayList<String> ignoreAttributes = new ArrayList<String>();
		for (var property : mappedProperties.properties)
		{
			var subMappedProperties = OTMappingTypeInformation.getMasterDataJsonTypeInfo(property.field.getType());
			boolean setAttribute = false;

			Object subObject = property.field.getType().newInstance();
			if (subObject != null)
			{
				for (Object item : jAttributes)
				{
					if (item instanceof  JSONObject)
					{
						JSONObject jAtt = (JSONObject) item;
						String id = jAtt.getString("id");

						var propMapping = subMappedProperties.get(id);
						if (propMapping != null)
						{
							if (!TrySetValueType(jAtt.get("attribute").toString(), propMapping.field, subObject))
							{
								Object value = ReadKDEObject(jAtt, propMapping.field.getType());
								propMapping.field.set(subObject, value);
							}
							setAttribute = true;
							ignoreAttributes.add(id);
						}
					}
				}
				if (setAttribute)
				{
					property.field.set(md, subObject);
				}
			}
		}

		// go through each standard attribute...
		for (Object item : jAttributes)
		{
			if (item instanceof  JSONObject) {
				JSONObject jAtt = (JSONObject) item;
				String id = jAtt.getString("id");

				if (ignoreAttributes.contains(id)) {
					continue;
				}

				var propMapping = mappedProperties.get(id);
				if (propMapping != null) {
					if (!TrySetValueType(jAtt.get("attribute").toString(), propMapping.field, md))
					{
						Object value = ReadKDEObject(jAtt, propMapping.field.getType());
						propMapping.field.set(md, value);
					}
				}
				else if (readKDEs)
				{
					Object jAttValue = jAtt.get("attribute");
					if (jAttValue != null) {
						if (jAttValue instanceof JSONObject)
						{
							// serialize into object kde...
							IMasterDataKDE kdeObject = new MasterDataKDEObject("", id);
							kdeObject.setFromGS1WebVocabJson((JSONObject)jAttValue);
							md.kdes.add(kdeObject);
						} else
						{
							// serialize into string kde
							MasterDataKDEString kdeString = new MasterDataKDEString("", id);
							kdeString.value = jAttValue.toString();
							md.kdes.add(kdeString);
						}
					}
				}
			}
		}
	}

	private static Object ReadKDEObject(Object j, Type t) throws Exception {
//C# TO JAVA CONVERTER TASK: Throw expressions are not converted by C# to Java Converter:
//ORIGINAL LINE: object value = Activator.CreateInstance(t) ?? throw new Exception("Failed to create instance of " + t.FullName);
		Object value = ReflectionUtility.constructType(t);
		if (value == null)
		{
			throw new RuntimeException("Failed to create instance of " + t.getName());
		}

		if (value instanceof List)
		{
			List list = (List)value;
			if (j instanceof JSONArray)
			{
				for (Object item : (JSONArray)j)
				{
					if (item instanceof JSONObject)
					{
						Type itemType = ReflectionUtility.getItemType(t);
						if (itemType == null)
						{
							throw new Exception("Failed to read generic item type of list.");
						}

						JSONObject xchild = (JSONObject) item;
						Object child = ReadKDEObject(xchild, itemType.getClass());
						list.add(child);
					}
				}
			}
		}
		else
		{
			JSONObject jobj = null;
			if (j instanceof JSONObject)
			{
				jobj = (JSONObject)j;
			}
			else
			{
				throw new Exception("expecting j to be JSONObject.");
			}

			// go through each property...
			for (Field p : t.getFields())
			{
//C# TO JAVA CONVERTER WARNING: Nullable reference types have no equivalent in Java:
//ORIGINAL LINE: OpenTraceabilityAttribute? xmlAtt = p.GetCustomAttribute<OpenTraceabilityAttribute>();
				OpenTraceabilityAttribute xmlAtt = ReflectionUtility.getFieldAnnotation(p, OpenTraceabilityAttribute.class);
				if (xmlAtt != null)
				{
					Object x = jobj.get(xmlAtt.name());
					if (x != null)
					{
//C# TO JAVA CONVERTER WARNING: Nullable reference types have no equivalent in Java:
//ORIGINAL LINE: OpenTraceabilityObjectAttribute? objAtt = p.GetCustomAttribute<OpenTraceabilityObjectAttribute>();
						OpenTraceabilityObjectAttribute objAtt = ReflectionUtility.getFieldAnnotation(p, OpenTraceabilityObjectAttribute.class);
						if (objAtt != null)
						{
							Object o = ReadKDEObject(x, p.getType());
						}
						else if (!TrySetValueType(x.toString(), p, value))
						{
							throw new RuntimeException(String.format("Failed to set value type while reading KDE object. property = %1$s, type = %2$s, json = %3$s", p.getName(), t.getName(), x.toString()));
						}
					}
				}
			}
		}

		return value;
	}

	private static boolean TrySetValueType(String val, Field p, Object o) throws IllegalAccessException {
		if (p.getType() == String.class)
		{
			p.set(o, val);
			return true;
		}
		else if (ReflectionUtility.isListOf(p.getType(), String.class))
		{
			Object tempVar = p.get(o);
			ArrayList<String> cur = tempVar instanceof ArrayList<String> ? (ArrayList<String>)tempVar : null;
			if (cur == null)
			{
				cur = new ArrayList<String>();
				p.set(o, cur);
			}
			cur.add(val);
			return true;
		}
		else if (p.getType() == Boolean.class || p.getType() == Boolean.class)
		{
			boolean v = Boolean.parseBoolean(val);
			p.set(o, v);
			return true;
		}
		else if (p.getType() == Double.class || p.getType() == Double.class)
		{
			double v = Double.parseDouble(val);
			p.set(o, v);
			return true;
		}
		else if (p.getType() == URI.class)
		{
			URI v = URI.create((val);
			p.set(o, v);
			return true;
		}
		else if (ReflectionUtility.isListOf(p.getType(), LanguageString.class))
		{
			ArrayList<LanguageString> l = new ArrayList<LanguageString>();
			LanguageString tempVar2 = new LanguageString();
			tempVar2.language = "en-US";
			tempVar2.value = val;
			l.add(tempVar2);
			p.set(o, l);
			return true;
		}
		else if (p.getType() == Country.class)
		{
			Country v = Countries.parse(val);
			p.set(o, v);
			return true;
		}
		else if (p.getType() == PGLN.class)
		{
			PGLN v = new PGLN(val);
			p.set(o, v);
			return true;
		}
		else
		{
			return false;
		}
	}
}
