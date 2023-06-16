package opentraceability.mappers.epcis.json;

import opentraceability.interfaces.*;
import opentraceability.models.common.*;
import opentraceability.models.events.kdes.*;
import opentraceability.models.events.*;
import opentraceability.models.identifiers.*;
import opentraceability.models.masterdata.*;
import opentraceability.utility.attributes.*;
import opentraceability.utility.*;
import Newtonsoft.Json.Linq.*;
import opentraceability.*;
import opentraceability.mappers.*;
import opentraceability.mappers.epcis.*;
import java.util.*;

/** 
 Used for reading master data from EPCIS JSON-LD file.
*/
public final class EPCISJsonMasterDataReader
{
	public static void ReadMasterData(EPCISBaseDocument doc, JObject jMasterData)
	{
		if (jMasterData["vocabularyList"] instanceof JArray)
		{
		JArray jVocabList = (JArray)jMasterData["vocabularyList"];
			for (JObject jVocabListItem : jVocabList)
			{
//C# TO JAVA CONVERTER WARNING: Nullable reference types have no equivalent in Java:
//ORIGINAL LINE: string? type = jVocabListItem["type"] == null ? null : ((jVocabListItem["type"].ToString() == null ? null : jVocabListItem["type"].ToString().ToLower()));
				String type = jVocabListItem["type"] == null ? null : ((jVocabListItem["type"].toString() == null ? null : jVocabListItem["type"].toString().toLowerCase()));
				if (type != null)
				{
					if (jVocabListItem["vocabularyElementList"] instanceof JArray)
					{
					JArray jVocabElementaryList = (JArray)jVocabListItem["vocabularyElementList"];
						for (JObject jVocabEle : jVocabElementaryList)
						{
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

	private static void ReadTradeitem(EPCISBaseDocument doc, JObject xTradeitem, String type)
	{
		// read the GTIN from the id
		String id = xTradeitem["id"] == null ? null : ((xTradeitem["id"].toString()) != null ? xTradeitem["id"].toString() : "");
		Tradeitem tradeitem = new Tradeitem();
		tradeitem.setGTIN(new models.identifiers.GTIN(id));
		tradeitem.setEPCISType(type);

		// read the object
		ReadMasterDataObject(tradeitem, xTradeitem);
		doc.getMasterData().add(tradeitem);
	}

	private static void ReadLocation(EPCISBaseDocument doc, JObject xLocation, String type)
	{
		// read the GLN from the id
		String id = xLocation["id"] == null ? null : ((xLocation["id"].toString()) != null ? xLocation["id"].toString() : "");
		java.lang.Class t = Setup.MasterDataTypes.get(type);
		if (!(t.newInstance() instanceof Location))
		{
		Location loc = (Location)t.newInstance();
			throw new RuntimeException(String.format("Failed to activate instance Location of %1$s", t));
		}
		else
		{
			loc.GLN = new models.identifiers.GLN(id);
			loc.EPCISType = type;

			// read the object
			ReadMasterDataObject(loc, xLocation);
			doc.getMasterData().add(loc);
		}
	}

	private static void ReadTradingParty(EPCISBaseDocument doc, JObject xTradingParty, String type)
	{
		// read the PGLN from the id
		String id = xTradingParty["id"] == null ? null : ((xTradingParty["id"].toString()) != null ? xTradingParty["id"].toString() : "");
		TradingParty tp = new TradingParty();
		tp.setPGLN(new models.identifiers.PGLN(id));
		tp.setEPCISType(type);

		// read the object
		ReadMasterDataObject(tp, xTradingParty);
		doc.getMasterData().add(tp);
	}

	private static void ReadUnknown(EPCISBaseDocument doc, JObject xVocabElement, String type)
	{
		// read the PGLN from the id
		String id = xVocabElement["id"] == null ? null : ((xVocabElement["id"].toString()) != null ? xVocabElement["id"].toString() : "");
		VocabularyElement ele = new VocabularyElement();
		ele.ID = id;
		ele.setEPCISType(type);

		// read the object
		ReadMasterDataObject(ele, xVocabElement);
		doc.getMasterData().add(ele);
	}


	private static void ReadMasterDataObject(IVocabularyElement md, JObject jMasterData)
	{
		ReadMasterDataObject(md, jMasterData, true);
	}

//C# TO JAVA CONVERTER NOTE: Java does not support optional parameters. Overloaded method(s) are created above:
//ORIGINAL LINE: private static void ReadMasterDataObject(IVocabularyElement md, JObject jMasterData, bool readKDEs = true)
	private static void ReadMasterDataObject(IVocabularyElement md, JObject jMasterData, boolean readKDEs)
	{
		var mappedProperties = OTMappingTypeInformation.GetMasterDataXmlTypeInfo(md.getClass());

		// work on expanded objects...
		// these are objects on the vocab element represented by one or more attributes in the EPCIS master data
		ArrayList<String> ignoreAttributes = new ArrayList<String>();
		for (var property : mappedProperties.getProperties().Where(p -> p.Name.equals("")))
		{
			var subMappedProperties = OTMappingTypeInformation.GetMasterDataXmlTypeInfo(property.Property.PropertyType);
			boolean setAttribute = false;
//C# TO JAVA CONVERTER WARNING: Nullable reference types have no equivalent in Java:
//ORIGINAL LINE: object? subObject = Activator.CreateInstance(property.Property.PropertyType);
			Object subObject = property.Property.PropertyType.newInstance();
			if (subObject != null)
			{
				for (JObject jAtt : jMasterData["attributes"] instanceof JArray ? (JArray)jMasterData["attributes"] : null)
				{
					String id = jAtt["id"] == null ? null : ((jAtt["id"].toString()) != null ? jAtt["id"].toString() : "");
					var propMapping = subMappedProperties.get(id);
					if (propMapping != null)
					{
						if (!TrySetValueType((jAtt["attribute"] == null ? null : jAtt["attribute"].toString()) != null ? (jAtt["attribute"] == null ? null : jAtt["attribute"].toString()) : "", propMapping.Property, subObject))
						{
							Object value = ReadKDEObject(jAtt, propMapping.Property.PropertyType);
							propMapping.Property.SetValue(subObject, value);
						}
						setAttribute = true;
						ignoreAttributes.add(id);
					}
				}
				if (setAttribute)
				{
					property.Property.SetValue(md, subObject);
				}
			}
		}

		// go through each standard attribute...
		for (JObject jAtt : jMasterData["attributes"] instanceof JArray ? (JArray)jMasterData["attributes"] : null)
		{
			String id = jAtt["id"] == null ? null : ((jAtt["id"].toString()) != null ? jAtt["id"].toString() : "");

			if (ignoreAttributes.contains(id))
			{
				continue;
			}

			var propMapping = mappedProperties.get(id);
			if (propMapping != null)
			{
				if (!TrySetValueType((jAtt["attribute"] == null ? null : jAtt["attribute"].toString()) != null ? (jAtt["attribute"] == null ? null : jAtt["attribute"].toString()) : "", propMapping.Property, md))
				{
					Object value = ReadKDEObject(jAtt, propMapping.Property.PropertyType);
					propMapping.Property.SetValue(md, value);
				}
			}
			else if (readKDEs)
			{
				JToken jAttValue = jAtt["attribute"];
				if (jAttValue != null)
				{
					if (jAttValue instanceof JObject)
					{
						// serialize into object kde...
						IMasterDataKDE kdeObject = new MasterDataKDEObject("", id);
						kdeObject.SetFromGS1WebVocabJson(jAttValue);
						md.getKDEs().add(kdeObject);
					}
					else
					{
						// serialize into string kde
						IMasterDataKDE kdeString = new MasterDataKDEString("", id);
						kdeString.SetFromGS1WebVocabJson(jAttValue);
						md.getKDEs().add(kdeString);
					}
				}
			}
		}
	}

	private static Object ReadKDEObject(JToken j, java.lang.Class t)
	{
//C# TO JAVA CONVERTER TASK: Throw expressions are not converted by C# to Java Converter:
//ORIGINAL LINE: object value = Activator.CreateInstance(t) ?? throw new Exception("Failed to create instance of " + t.FullName);
		Object value = t.newInstance() != null ? t.newInstance() : throw new RuntimeException("Failed to create instance of " + t.FullName);

		if (value instanceof List)
		{
			List list = (List)value;
			if (j instanceof JArray)
			{
				for (JObject xchild : (JArray)j)
				{
					Object child = ReadKDEObject(xchild, t.GenericTypeArguments[0]);
					list.add(child);
				}
			}
		}
		else
		{
			// go through each property...
			for (PropertyInfo p : t.GetProperties())
			{
//C# TO JAVA CONVERTER WARNING: Nullable reference types have no equivalent in Java:
//ORIGINAL LINE: OpenTraceabilityAttribute? xmlAtt = p.GetCustomAttribute<OpenTraceabilityAttribute>();
				OpenTraceabilityAttribute xmlAtt = p.<OpenTraceabilityAttribute>GetCustomAttribute();
				if (xmlAtt != null)
				{
					JToken x = j[xmlAtt.getName()];
					if (x != null)
					{
//C# TO JAVA CONVERTER WARNING: Nullable reference types have no equivalent in Java:
//ORIGINAL LINE: OpenTraceabilityObjectAttribute? objAtt = p.GetCustomAttribute<OpenTraceabilityObjectAttribute>();
						OpenTraceabilityObjectAttribute objAtt = p.<OpenTraceabilityObjectAttribute>GetCustomAttribute();
						if (objAtt != null)
						{
							Object o = ReadKDEObject(x, p.PropertyType);
						}
						else if (!TrySetValueType(x.toString(), p, value))
						{
							throw new RuntimeException(String.format("Failed to set value type while reading KDE object. property = %1$s, type = %2$s, json = %3$s", p.Name, t.FullName, x.toString()));
						}
					}
				}
			}
		}

		return value;
	}

	private static boolean TrySetValueType(String val, PropertyInfo p, Object o)
	{
		if (p.PropertyType == String.class)
		{
			p.SetValue(o, val);
			return true;
		}
		else if (p.PropertyType == ArrayList<String>.class)
		{
			Object tempVar = p.GetValue(o);
//C# TO JAVA CONVERTER WARNING: Nullable reference types have no equivalent in Java:
//ORIGINAL LINE: List<string>? cur = tempVar instanceof java.util.ArrayList<string> ? (java.util.ArrayList<string>)tempVar : null;
			ArrayList<String> cur = tempVar instanceof ArrayList<String> ? (ArrayList<String>)tempVar : null;
			if (cur == null)
			{
				cur = new ArrayList<String>();
				p.SetValue(o, cur);
			}
			cur.add(val);
			return true;
		}
		else if (p.PropertyType == Boolean.class || p.PropertyType == Boolean.class)
		{
			boolean v = Boolean.parseBoolean(val);
			p.SetValue(o, v);
			return true;
		}
		else if (p.PropertyType == Double.class || p.PropertyType == Double.class)
		{
			double v = Double.parseDouble(val);
			p.SetValue(o, v);
			return true;
		}
		else if (p.PropertyType == Uri.class)
		{
			Uri v = new Uri(val);
			p.SetValue(o, v);
			return true;
		}
		else if (p.PropertyType == ArrayList<LanguageString>.class)
		{
			ArrayList<LanguageString> l = new ArrayList<LanguageString>();
			LanguageString tempVar2 = new LanguageString();
			tempVar2.setLanguage("en-US");
			tempVar2.setValue(val);
			l.add(tempVar2);
			p.SetValue(o, l);
			return true;
		}
		else if (p.PropertyType == Country.class)
		{
			Country v = Countries.Parse(val);
			p.SetValue(o, v);
			return true;
		}
		else if (p.PropertyType == PGLN.class)
		{
			PGLN v = new PGLN(val);
			p.SetValue(o, v);
			return true;
		}
		else
		{
			return false;
		}
	}
}
