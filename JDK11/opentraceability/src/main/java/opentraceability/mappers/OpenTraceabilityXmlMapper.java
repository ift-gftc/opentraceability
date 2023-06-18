package opentraceability.mappers;

import opentraceability.interfaces.*;
import opentraceability.models.common.*;
import opentraceability.models.events.*;
import opentraceability.models.events.kdes.*;
import opentraceability.models.identifiers.*;
import opentraceability.utility.*;
import opentraceability.utility.attributes.*;
import opentraceability.*;
import org.json.JSONArray;
import org.json.JSONObject;
import org.w3c.dom.Element;


import java.sql.Ref;
import java.time.Duration;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

/** 
 This is a generic XML mapper that utilizes the open traceability attributes to map.
*/
public final class OpenTraceabilityXmlMapper
{
	public static XElement ToXml(String ns, String name, Object value, EPCISVersion version) throws Exception {
		return ToXml(ns, name, value, version, false);
	}

	public static XElement ToXml(String ns, String name, Object value, EPCISVersion version, boolean required) throws Exception {
		if (value != null)
		{
			XElement x = new XElement(ns, name);
			XElement xvalue = x;

			// make sure we have created the xml XElement correctly.
			List<String> xParts = StringExtensions.splitXPath(name);
			while (xParts.size() > 1)
			{
				String p = xParts.get(0);
				xParts.remove(0);
				if (xvalue.Element(p) == null)
				{
					xvalue.Add(new XElement(p));
				}
				xvalue = xvalue.Element(p);
			}
			name = xParts.get(0);

			if (value instanceof List)
			{
				List list = (List)value;
				if (!list.isEmpty())
				{
					Class t = list.get(0).getClass();
					OpenTraceabilityAttribute childAtt = ReflectionUtility.getAnnotation(t, OpenTraceabilityAttribute.class);
					for (var v : list)
					{
						XElement xListValue = ToXml(childAtt.ns(), childAtt.name(), v, version, required);
						if (xListValue != null)
						{
							xvalue.Add(xListValue);
						}
					}
				}
				else if (!required)
				{
					x = null;
				}
			}
			else
			{
				Class t = value.getClass();
				OTMappingTypeInformation typeInfo = OTMappingTypeInformation.getXmlTypeInfo(t);
				for (var property : typeInfo.properties.stream().filter(p -> p.version == null || p.version == version).collect(Collectors.toList()))
				{
					Object obj = property.field.get(value);
					if (obj != null)
					{
						XElement xvaluepointer = xvalue;
						xParts = StringExtensions.splitXPath(property.name);
						while (xParts.size() > 1)
						{
							String p = xParts.get(0);
							xParts.remove(0);
							if (xvaluepointer.Element(p) == null)
							{
								xvaluepointer.Add(new XElement(p));
							}
							xvaluepointer = xvaluepointer.Element(p);
						}
						String xchildname = xParts.get(0);

						if (xchildname.startsWith("@"))
						{
							String objStr = WriteObjectToString(obj);
							if (!(objStr == null || objStr.isBlank()))
							{
								XAttribute xatt = new XAttribute(tangible.StringHelper.trimStart(xchildname, '@'), objStr);
								xvaluepointer.Add(xatt);
							}
						}
						else if (Objects.equals(xchildname, "text()"))
						{
							String objStr = WriteObjectToString(obj);
							if (!(objStr == null || objStr.isBlank()))
							{
								xvaluepointer.setValue(objStr);
							}
						}
						else if (property.isQuantityList)
						{
							ArrayList<EventProduct> products = (ArrayList<EventProduct>)obj;
							var filteredProducts = products.stream().filter(p -> p.Quantity != null && p.Type == property.productType).collect(Collectors.toList());
							if (!filteredProducts.isEmpty())
							{
								XElement xQuantityList = new XElement(xchildname);
								for (var product : filteredProducts)
								{
									if (product.EPC != null && product.Quantity != null)
									{
										XElement xQuantity = new XElement("quantityXElement", new XElement("epcClass", product.EPC.toString()), new XElement("quantity", product.Quantity.value));

										if (!Objects.equals(product.Quantity.uom.UNCode, "EA"))
										{
											xQuantity.Add(new XElement("uom", product.Quantity.uom.UNCode));
										}

										xQuantityList.Add(xQuantity);
									}
								}
								xvaluepointer.Add(xQuantityList);
							}
						}
						else if (property.isEPCList)
						{
							ArrayList<EventProduct> products = (ArrayList<EventProduct>)obj;
							var filteredProducts = products.stream().filter(p -> p.Quantity == null && p.Type == property.productType).collect(Collectors.toList());
							if (!filteredProducts.isEmpty() || property.required)
							{
								XElement xEPCList = new XElement(property.name);
								for (var product : filteredProducts)
								{
									if (product.EPC != null)
									{
										XElement xEPC = new XElement("epc");
										xEPC.setValue(product.EPC.toString());
										xEPCList.Add(xEPC);
									}
								}
								xvaluepointer.Add(xEPCList);
							}
						}
						else if (property.isArray)
						{
							List list = (List)obj;
							XElement xlist = xvaluepointer;
							if (!list.isEmpty() || property.required)
							{
								if (property.itemName != null)
								{
									XElement xl = new XElement(xchildname);
									xvaluepointer.Add(xl);
									xlist = xl;
								}
							}

							for (var o : list)
							{
								if (property.isObject)
								{
									XElement xchild = ToXml(null, property.itemName != null ? property.itemName : xchildname, o, version, property.required);
									if (!xchild.IsNull)
									{
										xlist.Add(xchild);
									}
								}
								else
								{
//C# TO JAVA CONVERTER WARNING: Nullable reference types have no equivalent in Java:
//ORIGINAL LINE: string? objStr = WriteObjectToString(o);
									String objStr = WriteObjectToString(o);
									if (!(objStr == null || objStr.isBlank()))
									{
										XElement xchild = new XElement(property.itemName != null ? property.itemName : xchildname, objStr);
										xlist.Add(xchild);
									}
								}
							}
						}
						else if (property.isObject)
						{
							XElement xchild = ToXml(null, xchildname, obj, version, property.required);
							if (!xchild.IsNull)
							{
								xvaluepointer.Add(xchild);
							}
						}
						else
						{
							String objStr = WriteObjectToString(obj);
							if (!(objStr == null || objStr.isBlank()))
							{
								XElement xchild = new XElement(xchildname, objStr);
								xvaluepointer.Add(xchild);
							}
						}
					}
					else if (property.required)
					{
						XElement xvaluepointer = xvalue;
						xParts = StringExtensions.splitXPath(property.name);
						while (xParts.size() > 1)
						{
							String p = xParts.get(0);
							xParts.remove(0);
							if (xvaluepointer.Element(p) == null)
							{
								xvaluepointer.Add(new XElement(p));
							}
							xvaluepointer = xvaluepointer.Element(p);
						}
						String xchildname = xParts.get(0);
						XElement xchild = new XElement(xchildname);
						xvaluepointer.Add(xchild);
					}
				}

				if (typeInfo.extensionKDEs != null)
				{
					Object obj = typeInfo.extensionKDEs.get(value);
					if (obj != null)
					{
						ArrayList<IEventKDE> kdes = (ArrayList<IEventKDE>)obj;
						if (kdes != null)
						{
							for (var kde : kdes)
							{
								XElement xchild = kde.getXml();
								if (xchild != null)
								{
									xvalue.Add(xchild);
								}
							}
						}
					}
				}

				if (typeInfo.extensionAttributes != null)
				{
					Object obj = typeInfo.extensionAttributes.get(value);
					if (obj != null)
					{
						ArrayList<IEventKDE> kdes = (ArrayList<IEventKDE>)obj;
						if (kdes != null)
						{
							for (var kde : kdes) {
								XElement xKDE = kde.getXml();
								if (xKDE != null) {
									xvalue.Add(new XAttribute(xKDE.getNamespaceUri(), xKDE.getLocalName(), xKDE.getNodeValue()));
								}
							}
						}
					}
				}
			}

			return x;
		}
		else if (required)
		{
			XElement x = new XElement(ns, name);
			return x;
		}
		else
		{
			return null;
		}
	}

	public static Object FromXml(XElement x, EPCISVersion version, Class type) throws Exception
	{
		Object value = ReflectionUtility.constructType(type);

		OTMappingTypeInformation mappingInfo = OTMappingTypeInformation.getXmlTypeInfo(type);

		// if this is a list, then we will make a list of the objects...
		if (value instanceof List)
		{
			List list = (List)value;
			OpenTraceabilityAttribute att = ReflectionUtility.getAnnotation(type, OpenTraceabilityAttribute.class);
			if (att != null)
			{
				for (XElement xchild : x.Elements(att.name()))
				{
					Class itemType = ReflectionUtility.getItemType(type);
					Object childvalue = FromXml(xchild, version, itemType);
					list.add(childvalue);
				}
			}
			else
			{
				for (XElement xchild : x.Elements())
				{
					Class itemType = ReflectionUtility.getItemType(type);
					Object childvalue = FromXml(xchild, version, itemType);
					list.add(childvalue);
				}
			}
		}
		// else, try and parse the object...
		else
		{
			OTMappingTypeInformation typeInfo = OTMappingTypeInformation.getXmlTypeInfo(type);

			ArrayList<IEventKDE> extensionKDEs = null;
			ArrayList<IEventKDE> extensionAttributes = null;

			if (typeInfo.extensionAttributes != null)
			{
				extensionAttributes = new ArrayList<IEventKDE>();
			}

			if (typeInfo.extensionKDEs != null)
			{
				extensionKDEs = new ArrayList<IEventKDE>();
			}

			OTMappingTypeInformationProperty mappingProp;

			for (var xatt : x.Attributes())
			{
				mappingProp = typeInfo.get("@" + xatt.Name);
				if (mappingProp != null)
				{
					String xchildname = mappingProp.name;
					String attValue = x.Attribute(tangible.StringHelper.trimStart(xchildname, '@')) == null ? null : x.Attribute(tangible.StringHelper.trimStart(xchildname, '@'));
					if (!tangible.StringHelper.isNullOrEmpty(attValue))
					{
						Object o = ReadObjectFromString(attValue, mappingProp.field.getDeclaringClass());
						mappingProp.field.set(value, o);
					}
				}
				else if (extensionAttributes != null)
				{
					IEventKDE kde = ReadKDE(xatt.Namespace, xatt.Name, xatt.Value);
					extensionAttributes.add(kde);
				}
			}

			mappingProp = typeInfo.get("text()");
			if (mappingProp != null)
			{
				String eleText = x.getNodeValue();
				if (!(eleText == null || eleText.isBlank()))
				{
					Object o = ReadObjectFromString(eleText, mappingProp.field.getDeclaringClass());
					mappingProp.field.set(value, o);
				}
			}
			else
			{
				for (XElement xc : x.Elements())
				{
					XElement xchild = xc;
					String tagName = xchild.getTagName();

					mappingProp = typeInfo.get(xchild.getTagName());
					if (mappingProp == null && tangible.ListHelper.exists(typeInfo.properties, p -> StringExtensions.splitXPath(p.name).get(0) == tagName))
					{
						// see if we have a parent matching way...
						for (var mp : typeInfo.properties.stream().filter(p -> StringExtensions.splitXPath(p.name).get(0) == tagName).collect(Collectors.toList()))
						{
							XElement xgrandchild = x.Element(mp.name);
							if (xgrandchild != null)
							{
								ReadPropertyMapping(mp, xgrandchild, value, version);
							}
						}
					}
					else if (mappingProp != null)
					{
						ReadPropertyMapping(mappingProp, xchild, value, version);
					}
					else if (extensionKDEs != null)
					{
						IEventKDE kde = ReadKDE(xchild);
						extensionKDEs.add(kde);
					}
				}
			}

			if (typeInfo.extensionAttributes != null)
			{
				typeInfo.extensionAttributes.set(value, extensionAttributes);
			}

			if (typeInfo.extensionKDEs != null)
			{
				typeInfo.extensionKDEs.set(value, extensionKDEs);
			}
		}

		return value;
	}

//C# TO JAVA CONVERTER WARNING: Nullable reference types have no equivalent in Java:
//ORIGINAL LINE: private static string? WriteObjectToString(object obj)
	private static String WriteObjectToString(Object obj)
	{
		if (obj == null)
		{
			return null;
		}
		// check if obj is ArrayList<LanguageString>...
		else if (obj instanceof ArrayList)
		{
			ArrayList l = (ArrayList)obj;
			if (!l.isEmpty())
			{
				JSONArray jArr = new JSONArray();
				for (Object o: l) {
					if (o instanceof LanguageString) {
						return ((LanguageString) o).value;
					} else {
						return o.toString();
					}
				}
			}
			return null;
		}
		else if (obj instanceof OffsetDateTime)
		{
			OffsetDateTime dt = (OffsetDateTime)obj;
			return dt.format(DateTimeFormatter.ISO_DATE_TIME);
		}
		else if (obj instanceof UOM)
		{
			UOM uom = (UOM)obj;
			return uom.UNCode;
		}
		else if (obj instanceof Double)
		{
			return obj.toString();
		}
		else if (obj instanceof Boolean)
		{
			return obj.toString();
		}
		else if (obj instanceof Country)
		{
			Country b = (Country)obj;
			return b.abbreviation;
		}
		else if (obj instanceof Duration)
		{
			String timeStr = StringExtensions.fromDuration((Duration)obj);
			return timeStr;
		}
		else
		{
			return obj.toString() != null ? obj.toString() : "";
		}
	}

	private static void ReadPropertyMapping(OTMappingTypeInformationProperty mappingProp, XElement xchild, Object value, EPCISVersion version) throws Exception {
		if (mappingProp.isQuantityList)
		{
			IEvent e = (IEvent)value;
			for (var xQuantity : xchild.Elements("quantityXElement"))
			{
				EPC epc = new EPC(xQuantity.Element("epcClass").getValue());
				EventProduct product = new EventProduct(epc);
				product.Type = mappingProp.productType;

				double quantity = Double.parseDouble(xQuantity.Element("quantity").getValue());
				String uom = xQuantity.Element("uom").getValue();
				if (uom == null)
				{
					uom = "EA";
				}
				product.Quantity = new Measurement(quantity, uom);

				e.addProduct(product);
			}
		}
		else if (mappingProp.isEPCList)
		{
			IEvent e = (IEvent)value;
			for (var xEPC : xchild.Elements("epc"))
			{
				EPC epc = new EPC(xEPC.getValue());
				EventProduct product = new EventProduct(epc);
				product.Type = mappingProp.productType;
				e.addProduct(product);
			}
		}
		else if (mappingProp.isArray)
		{
			Object tempVar = mappingProp.field.get(value);
			List list = tempVar instanceof List ? (List)tempVar : null;
			if (list == null)
			{
				list = (List)ReflectionUtility.constructType(mappingProp.field.getDeclaringClass());
				mappingProp.field.set(value, list);
			}

			Class itemType = ReflectionUtility.getItemType(mappingProp.field.getDeclaringClass());
			if (mappingProp.itemName != null)
			{
				for (XElement xitem : xchild.Elements(mappingProp.itemName))
				{
					if (mappingProp.isObject)
					{
						Object o = FromXml(xitem, version, itemType);
						list.add(o);
					}
					else
					{
						Object o = ReadObjectFromString(xitem.getValue(), itemType);
						list.add(o);
					}
				}
			}
			else
			{
				if (mappingProp.isObject)
				{
					Object o = FromXml(xchild, version, itemType);
					list.add(o);
				}
				else
				{
					Object o = ReadObjectFromString(xchild.getValue(), itemType);
					list.add(o);
				}
			}
		}
		else if (mappingProp.isObject)
		{
			Object o = FromXml(xchild, version, mappingProp.field.getDeclaringClass());
			mappingProp.field.set(value, o);
		}
		else
		{
			String eleText = xchild.getValue();
			if (!(eleText == null || eleText.isBlank()))
			{
				Object o = ReadObjectFromString(eleText, mappingProp.field.getDeclaringClass());
				mappingProp.field.set(value, o);
			}
		}
	}

	private static Object ReadObjectFromString(String value, Class t) throws Exception {
		return ReflectionUtility.parseFromString(t, value);
	}

	private static IEventKDE ReadKDE(XElement x) throws Exception {
		// we need to parse the xml into an event KDE here...

		// check if it is a registered KDE...
		IEventKDE kde = IEventKDE.initializeKDE(x.getNamespaceUri(), x.getLocalName());

		// if not, then check if the data type is specified and we recognize it
		if (kde == null)
		{
			String xsiType = x.Attribute(Constants.XSI_NAMESPACE,"type");
			if (xsiType != null)
			{
				switch (xsiType)
				{
					case "string":
						kde = new EventKDEString(x.getNamespaceUri(), x.getLocalName());
						break;
					case "boolean":
						kde = new EventKDEBoolean(x.getNamespaceUri(), x.getLocalName());
						break;
					case "number":
						kde = new EventKDEDouble(x.getNamespaceUri(), x.getLocalName());
						break;
				}
			}
		}

		// if not, check if it is a simple value or an object
		if (kde == null)
		{
			if (x.Elements().size() > 0)
			{
				kde = new EventKDEObject(x.getNamespaceUri(), x.getLocalName());
			}
			// else if simple value, then we will consume it as a string
			else
			{
				kde = new EventKDEString(x.getNamespaceUri(), x.getLocalName());
			}
		}

		if (kde != null)
		{
			kde.setFromXml(x);
		}
		else
		{
			throw new RuntimeException("Failed to initialize KDE from XML = " + x.toString());
		}

		return kde;
	}

	private static IEventKDE ReadKDE(String ns, String name, String value) throws Exception {
		// we need to parse the xml into an event KDE here...

		// check if it is a registered KDE...
		IEventKDE kde = IEventKDE.initializeKDE(ns, name);

		// if not, check if it is a simple value or an object
		if (kde == null)
		{
			kde = new EventKDEString(ns, name);
		}

		if (kde != null)
		{
			XElement xe = new XElement(ns, name);
			kde.setFromXml(xe);
		}
		else
		{
			throw new RuntimeException("Failed to initialize KDE from XML Attribute = " + ns + " and name is " + name + " and value is " + value);
		}

		return kde;
	}
}
