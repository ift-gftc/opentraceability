package opentraceability.mappers.epcis.xml;

import Newtonsoft.Json.Linq.*;
import opentraceability.interfaces.*;
import opentraceability.models.common.*;
import opentraceability.models.events.*;
import opentraceability.models.events.kdes.*;
import opentraceability.models.identifiers.*;
import opentraceability.models.masterdata.*;
import opentraceability.utility.*;
import opentraceability.utility.attributes.*;
import opentraceability.*;
import opentraceability.mappers.*;
import opentraceability.mappers.epcis.*;
import java.util.*;

public final class EPCISXmlMasterDataReader
{
	public static void ReadMasterData(EPCISBaseDocument doc, XElement xMasterData)
	{
		//<VocabularyList>
		//        <Vocabulary type="urn:epcglobal:epcis:vtype:EPCClass">
		//        <Vocabulary type="urn:epcglobal:epcis:vtype:Location">
		//        <Vocabulary type="urn:epcglobal:epcis:vtype:Party">

		XElement xVocabList = xMasterData.Element("VocabularyList");
		if (xVocabList != null)
		{
			for (XElement xVocab : xVocabList.Elements())
			{
//C# TO JAVA CONVERTER WARNING: Nullable reference types have no equivalent in Java:
//ORIGINAL LINE: string? type = xVocab.Attribute("type") == null ? null : xVocab.Attribute("type").Value.ToLower();
				String type = xVocab.Attribute("type") == null ? null : xVocab.Attribute("type").Value.toLowerCase();
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

	private static void ReadTradeitem(EPCISBaseDocument doc, XElement xTradeitem, String type)
	{
		// read the GTIN from the id
		String id = xTradeitem.Attribute("id") == null ? null : ((xTradeitem.Attribute("id").Value) != null ? xTradeitem.Attribute("id").Value : "");
		Tradeitem tradeitem = new Tradeitem();
		tradeitem.setGTIN(new models.identifiers.GTIN(id));
		tradeitem.setEPCISType(type);

		// read the object
		ReadMasterDataObject(tradeitem, xTradeitem);
		doc.masterData.add(tradeitem);
	}

	private static void ReadLocation(EPCISBaseDocument doc, XElement xLocation, String type)
	{
		// read the GLN from the id
		String id = xLocation.Attribute("id") == null ? null : ((xLocation.Attribute("id").Value) != null ? xLocation.Attribute("id").Value : "");
		Type t = Setup.MasterDataTypes.get(type);
		if (!(t.newInstance() instanceof Location))
		{
		Location loc = (Location)t.newInstance();
			throw new RuntimeException(String.format("Failed to create instance of Location from type %1$s", t));
		}
		else
		{
			loc.GLN = new models.identifiers.GLN(id);
			loc.EPCISType = type;

			// read the object
			ReadMasterDataObject(loc, xLocation);
			doc.masterData.add(loc);
		}
	}

	private static void ReadTradingParty(EPCISBaseDocument doc, XElement xTradingParty, String type)
	{
		// read the PGLN from the id
		String id = xTradingParty.Attribute("id") == null ? null : ((xTradingParty.Attribute("id").Value) != null ? xTradingParty.Attribute("id").Value : "");
		TradingParty tp = new TradingParty();
		tp.setPGLN(new models.identifiers.PGLN(id));
		tp.setEPCISType(type);

		// read the object
		ReadMasterDataObject(tp, xTradingParty);
		doc.masterData.add(tp);
	}

	private static void ReadUnknown(EPCISBaseDocument doc, XElement xVocabElement, String type)
	{
		// read the PGLN from the id
		String id = xVocabElement.Attribute("id") == null ? null : ((xVocabElement.Attribute("id").Value) != null ? xVocabElement.Attribute("id").Value : "");
		VocabularyElement ele = new VocabularyElement();
		ele.ID = id;
		ele.setEPCISType(type);

		// read the object
		ReadMasterDataObject(ele, xVocabElement);
		doc.masterData.add(ele);
	}


	private static void ReadMasterDataObject(IVocabularyElement md, XElement xMasterData)
	{
		ReadMasterDataObject(md, xMasterData, true);
	}

//C# TO JAVA CONVERTER NOTE: Java does not support optional parameters. Overloaded method(s) are created above:
//ORIGINAL LINE: private static void ReadMasterDataObject(IVocabularyElement md, XElement xMasterData, bool readKDEs = true)
	private static void ReadMasterDataObject(IVocabularyElement md, XElement xMasterData, boolean readKDEs)
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
				for (XElement xeAtt : xMasterData.Elements("attribute"))
				{
					String id = xeAtt.Attribute("id") == null ? null : ((xeAtt.Attribute("id").Value) != null ? xeAtt.Attribute("id").Value : "");
					var propMapping = subMappedProperties.get(id);
					if (propMapping != null)
					{
						if (!TrySetValueType(xeAtt.Value, propMapping.Property, subObject))
						{
							Object value = ReadKDEObject(xeAtt, propMapping.Property.PropertyType);
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
		for (XElement xeAtt : xMasterData.Elements("attribute"))
		{
			String id = xeAtt.Attribute("id") == null ? null : ((xeAtt.Attribute("id").Value) != null ? xeAtt.Attribute("id").Value : "");

			if (ignoreAttributes.contains(id))
			{
				continue;
			}

			var propMapping = mappedProperties.get(id);
			if (propMapping != null)
			{
				if (!TrySetValueType(xeAtt.Value, propMapping.Property, md))
				{
					Object value = ReadKDEObject(xeAtt, propMapping.Property.PropertyType);
					propMapping.Property.SetValue(md, value);
				}
			}
			else if (readKDEs)
			{
				if (xeAtt.HasElements)
				{
					// serialize into object kde...
					IMasterDataKDE kdeObject = new MasterDataKDEObject("", id);
					kdeObject.SetFromEPCISXml(xeAtt);
					md.getKDEs().add(kdeObject);
				}
				else
				{
					// serialize into string kde
					IMasterDataKDE kdeString = new MasterDataKDEString("", id);
					kdeString.SetFromEPCISXml(xeAtt);
					md.getKDEs().add(kdeString);
				}
			}
		}
	}

	private static Object ReadKDEObject(XElement xeAtt, Type t)
	{
//C# TO JAVA CONVERTER TASK: Throw expressions are not converted by C# to Java Converter:
//ORIGINAL LINE: object value = Activator.CreateInstance(t) ?? throw new Exception("Failed to create instance of " + t.FullName);
		Object value = t.newInstance() != null ? t.newInstance() : throw new RuntimeException("Failed to create instance of " + t.FullName);

		if (value instanceof List)
		{
			List list = (List)value;
			for (XElement xchild : xeAtt.Elements())
			{
				Object child = ReadKDEObject(xchild, t.GenericTypeArguments[0]);
				list.add(child);
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
					XElement x = xeAtt.Element(xmlAtt.getName());
					if (x != null)
					{
//C# TO JAVA CONVERTER WARNING: Nullable reference types have no equivalent in Java:
//ORIGINAL LINE: OpenTraceabilityObjectAttribute? objAtt = p.GetCustomAttribute<OpenTraceabilityObjectAttribute>();
						OpenTraceabilityObjectAttribute objAtt = p.<OpenTraceabilityObjectAttribute>GetCustomAttribute();
						if (objAtt != null)
						{
							Object o = ReadKDEObject(x, p.PropertyType);
						}
						else if (!TrySetValueType(x.getValue(), p, value))
						{
							throw new RuntimeException(String.format("Failed to set value type while reading KDE object. property = %1$s, type = %2$s, xml = %3$s", p.Name, t.FullName, x.toString()));
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
			Object tempVar = p.get(o);
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
