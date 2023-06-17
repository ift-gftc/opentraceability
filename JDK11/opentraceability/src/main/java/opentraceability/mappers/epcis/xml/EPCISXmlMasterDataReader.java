package opentraceability.mappers.epcis.xml;

import opentraceability.interfaces.*;
import opentraceability.models.common.*;
import opentraceability.models.events.*;
import opentraceability.models.events.kdes.*;
import opentraceability.models.identifiers.*;
import opentraceability.models.masterdata.*;
import opentraceability.models.masterdata.kdes.MasterDataKDEObject;
import opentraceability.models.masterdata.kdes.MasterDataKDEString;
import opentraceability.utility.*;
import opentraceability.utility.attributes.*;
import opentraceability.*;
import opentraceability.mappers.*;
import opentraceability.mappers.epcis.*;
import org.json.JSONObject;

import javax.xml.xpath.XPathExpressionException;
import java.lang.reflect.Field;
import java.lang.reflect.Type;
import java.net.URI;
import java.util.*;
import java.util.stream.Collectors;

public final class EPCISXmlMasterDataReader
{
	public static void ReadMasterData(EPCISBaseDocument doc, XElement xMasterData) throws Exception {
		//<VocabularyList>
		//        <Vocabulary type="urn:epcglobal:epcis:vtype:EPCClass">
		//        <Vocabulary type="urn:epcglobal:epcis:vtype:Location">
		//        <Vocabulary type="urn:epcglobal:epcis:vtype:Party">

		XElement xVocabList = xMasterData.Element("VocabularyList");
		if (xVocabList != null)
		{
			for (XElement xVocab : xVocabList.Elements())
			{
				String type = xVocab.Attribute("type").toLowerCase();
				if (type != null)
				{
					XElement xVocabElementaryList = xVocab.Element("VocabularyElementList");
					if (xVocabElementaryList != null)
					{
						for (XElement xVocabElement : xVocabElementaryList.Elements())
						{
							switch (type)
							{
								case "urn:epcglobal:epcis:vtype:epcclass":
									ReadTradeitem(doc, xVocabElement, type);
									break;
								case "urn:epcglobal:epcis:vtype:location":
									ReadLocation(doc, xVocabElement, type);
									break;
								case "urn:epcglobal:epcis:vtype:party":
									ReadTradingParty(doc, xVocabElement, type);
									break;
								default:
									ReadUnknown(doc, xVocabElement, type);
									break;
							}
						}
					}
				}
			}
		}
	}

	private static void ReadTradeitem(EPCISBaseDocument doc, XElement xTradeitem, String type) throws Exception {
		Type javaType = Setup.MasterDataTypes.get(type);
		if (javaType == null)
		{
			throw new Exception("Failed to read java type.");
		}

		Object o = javaType.getClass().newInstance();
		if (o instanceof  TradeItem)
		{
			TradeItem md = (TradeItem)o;
			md.id = xTradeitem.Element("id").getValue();
			md.gtin = new opentraceability.models.identifiers.GTIN(md.id);
			md.epcisType = type;

			// read the object
			ReadMasterDataObject(md, xTradeitem);
			doc.masterData.add(md);
		}
		else
		{
			throw new Exception(javaType.getTypeName() + " is not instance of TradeItem.");
		}
	}

	private static void ReadLocation(EPCISBaseDocument doc, XElement xLocation, String type) throws Exception {
		Type javaType = Setup.MasterDataTypes.get(type);
		if (javaType == null)
		{
			throw new Exception("Failed to read java type.");
		}

		Object o = javaType.getClass().newInstance();
		if (o instanceof  Location)
		{
			Location md = (Location)o;
			md.id = xLocation.Element("id").getValue();
			md.gln = new opentraceability.models.identifiers.GLN(md.id);
			md.epcisType = type;

			// read the object
			ReadMasterDataObject(md, xLocation);
			doc.masterData.add(md);
		}
		else
		{
			throw new Exception(javaType.getTypeName() + " is not instance of Location.");
		}
	}

	private static void ReadTradingParty(EPCISBaseDocument doc, XElement xTradingParty, String type) throws Exception {
		Type javaType = Setup.MasterDataTypes.get(type);
		if (javaType == null)
		{
			throw new Exception("Failed to read java type.");
		}

		Object o = javaType.getClass().newInstance();
		if (o instanceof  TradingParty)
		{
			TradingParty md = (TradingParty)o;
			md.id = xTradingParty.Element("id").getValue();
			md.pgln = new opentraceability.models.identifiers.PGLN(md.id);
			md.epcisType = type;

			// read the object
			ReadMasterDataObject(md, xTradingParty);
			doc.masterData.add(md);
		}
		else
		{
			throw new Exception(javaType.getTypeName() + " is not instance of TradingParty.");
		}
	}

	private static void ReadUnknown(EPCISBaseDocument doc, XElement xUnknown, String type) throws Exception {
		// read the pgln from the id
		String id = xUnknown.Element("id").getValue();
		VocabularyElement ele = new VocabularyElement();
		ele.id = id;
		ele.epcisType = type;

		// read the object
		ReadMasterDataObject(ele, xUnknown);
		doc.masterData.add(ele);
	}


	private static void ReadMasterDataObject(IVocabularyElement md, XElement xMasterData) throws Exception {
		ReadMasterDataObject(md, xMasterData, true);
	}

	private static void ReadMasterDataObject(IVocabularyElement md, XElement xMasterData, boolean readKDEs) throws Exception {
		var mappedProperties = OTMappingTypeInformation.getMasterDataXmlTypeInfo(md.getClass());

		// work on expanded objects...
		// these are objects on the vocab element represented by one or more attributes in the EPCIS master data
		ArrayList<String> ignoreAttributes = new ArrayList<String>();
		for (var property : mappedProperties.properties.stream().filter(p -> p.name.equals("")).collect(Collectors.toList()))
		{
			var subMappedProperties = OTMappingTypeInformation.getMasterDataXmlTypeInfo(property.field.getType());
			boolean setAttribute = false;

			Object subObject = ReflectionUtility.constructType(property.field.getType());
			if (subObject != null)
			{
				for (XElement xeAtt : xMasterData.Elements("attribute"))
				{
					String id = xeAtt.Attribute("id");
					var propMapping = subMappedProperties.get(id);
					if (propMapping != null)
					{
						if (!TrySetValueType(xeAtt.getValue(), propMapping.field, subObject))
						{
							Object value = ReadKDEObject(xeAtt, propMapping.field.getType());
							propMapping.field.set(subObject, value);
						}
						setAttribute = true;
						ignoreAttributes.add(id);
					}
				}
				if (setAttribute)
				{
					property.field.set(md, subObject);
				}
			}
		}

		// go through each standard attribute...
		for (XElement xeAtt : xMasterData.Elements("attribute"))
		{
			String id = xeAtt.Attribute("id");

			if (ignoreAttributes.contains(id))
			{
				continue;
			}

			var propMapping = mappedProperties.get(id);
			if (propMapping != null)
			{
				if (!TrySetValueType(xeAtt.getValue(), propMapping.field, md))
				{
					Object value = ReadKDEObject(xeAtt, propMapping.field.getType());
					propMapping.field.set(md, value);
				}
			}
			else if (readKDEs)
			{
				if (xeAtt.HasElements())
				{
					// serialize into object kde...
					IMasterDataKDE kdeObject = new MasterDataKDEObject("", id);
					kdeObject.setFromEPCISXml(xeAtt);
					md.kdes.add(kdeObject);
				}
				else
				{
					// serialize into string kde
					IMasterDataKDE kdeString = new MasterDataKDEString("", id);
					kdeString.setFromEPCISXml(xeAtt);
					md.kdes.add(kdeString);
				}
			}
		}
	}

	private static Object ReadKDEObject(XElement xeAtt, Type t) throws Exception {
		Object value = ReflectionUtility.constructType(t);

		if (value instanceof List)
		{
			List list = (List)value;
			for (XElement xchild : xeAtt.Elements())
			{
				Type itemType = ReflectionUtility.getItemType(t);
				if (itemType == null)
				{
					throw new Exception("Failed to read generic item type of list.");
				}

				Object child = ReadKDEObject(xchild, itemType);
				list.add(child);
			}
		}
		else
		{
			// go through each property...
			for (Field p : t.getClass().getFields())
			{
				OpenTraceabilityAttribute xmlAtt = ReflectionUtility.getFieldAnnotation(p, OpenTraceabilityAttribute.class);
				if (xmlAtt != null)
				{
					XElement x = xeAtt.Element(xmlAtt.name());
					if (x != null)
					{
						OpenTraceabilityObjectAttribute objAtt = ReflectionUtility.getFieldAnnotation(p, OpenTraceabilityObjectAttribute.class);
						if (objAtt != null)
						{
							Object o = ReadKDEObject(x, p.getType());
						}
						else if (!TrySetValueType(x.getValue(), p, value))
						{
							throw new RuntimeException(String.format("Failed to set value type while reading KDE object. property = %1$s, type = %2$s, xml = %3$s", p.getName(), t.getTypeName(), x.toString()));
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
			ArrayList cur = tempVar instanceof ArrayList ? (ArrayList)tempVar : null;
			if (cur == null || cur.isEmpty())
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
			URI v = URI.create(val);
			p.set(o, v);
			return true;
		}
		else if (ReflectionUtility.isListOf(p.getType(), LanguageString.class))
		{
			ArrayList<LanguageString> l = LanguageString.fromJSON(val);
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
