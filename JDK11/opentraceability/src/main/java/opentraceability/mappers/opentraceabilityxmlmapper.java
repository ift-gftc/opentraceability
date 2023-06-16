package opentraceability.mappers;

import opentraceability.interfaces.*;
import opentraceability.models.common.*;
import opentraceability.models.events.*;
import opentraceability.models.events.kdes.*;
import opentraceability.models.identifiers.*;
import opentraceability.utility.*;
import opentraceability.utility.attributes.*;
import opentraceability.*;
import java.util.*;

/** 
 This is a generic XML mapper that utilizes the open traceability attributes to map.
*/
public final class OpenTraceabilityXmlMapper
{

	public static XElement ToXml(String xname, Object value, EPCISVersion version)
	{
		return ToXml(xname, value, version, false);
	}

//C# TO JAVA CONVERTER WARNING: Nullable reference types have no equivalent in Java:
//ORIGINAL LINE: public static System.Nullable<XElement> ToXml(string xname, object? value, EPCISVersion version, bool required = false)
//C# TO JAVA CONVERTER NOTE: Java does not support optional parameters. Overloaded method(s) are created above:
	public static XElement ToXml(String xname, Object value, EPCISVersion version, boolean required)
	{
		if (value != null)
		{
			XElement x = new XElement(xname);
			XElement xvalue = x;

			// make sure we have created the xml element correctly.
			ArrayList<String> xParts = StringExtensions.SplitXPath(xname);
			while (xParts.size() > 1)
			{
				String p = xParts.get(0);
				xParts.remove(0);
				if (xvalue.Element(p) == null)
				{
					xvalue.Add(new XElement(p));
				}
//C# TO JAVA CONVERTER TASK: Throw expressions are not converted by C# to Java Converter:
//ORIGINAL LINE: xvalue = xvalue.Element(p) ?? throw new Exception("Failed to add xml element, p=" + p);
				xvalue = xvalue.Element(p) != null ? xvalue.Element(p) : throw new RuntimeException("Failed to add xml element, p=" + p);
			}
			xname = xParts.get(0);

			if (value instanceof List)
			{
				List list = (List)value;
				if (!list.isEmpty())
				{
//C# TO JAVA CONVERTER TASK: Throw expressions are not converted by C# to Java Converter:
//ORIGINAL LINE: Type t = list[0] == null ? null : list[0].GetType() ?? throw new Exception("Failed to get list item type.");
					Type t = list.get(0) == null ? null : ((list.get(0).getClass()) != null ? list.get(0).getClass() : throw new RuntimeException("Failed to get list item type."));
//C# TO JAVA CONVERTER TASK: Throw expressions are not converted by C# to Java Converter:
//ORIGINAL LINE: XName xchildname = t.GetCustomAttribute<OpenTraceabilityAttribute>() == null ? null : t.GetCustomAttribute<OpenTraceabilityAttribute>().Name ?? throw new Exception("Failed to get xname from type. type = " + t.FullName);
					XName xchildname = t.<OpenTraceabilityAttribute>GetCustomAttribute() == null ? null : ((t.<OpenTraceabilityAttribute>GetCustomAttribute().Name) != null ? t.<OpenTraceabilityAttribute>GetCustomAttribute().Name : throw new RuntimeException("Failed to get xname from type. type = " + t.FullName));
					for (var v : list)
					{
						XElement xListValue = ToXml(xchildname.toString(), v, version, required);
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
				Type t = value.getClass();
				OTMappingTypeInformation typeInfo = OTMappingTypeInformation.GetXmlTypeInfo(t);
				for (var property : typeInfo.getProperties().Where(p -> p.Version == null || p.Version == version))
				{
//C# TO JAVA CONVERTER WARNING: Nullable reference types have no equivalent in Java:
//ORIGINAL LINE: object? obj = property.Property.get(value);
					Object obj = property.Property.get(value);
					if (obj != null)
					{
						XElement xvaluepointer = xvalue;
						xParts = StringExtensions.SplitXPath(property.Name);
						while (xParts.size() > 1)
						{
							String p = xParts.get(0);
							xParts.remove(0);
							if (xvaluepointer.Element(p) == null)
							{
								xvaluepointer.Add(new XElement(p));
							}
//C# TO JAVA CONVERTER TASK: Throw expressions are not converted by C# to Java Converter:
//ORIGINAL LINE: xvaluepointer = xvaluepointer.Element(p) ?? throw new Exception("Failed to add xml element, p=" + p);
							xvaluepointer = xvaluepointer.Element(p) != null ? xvaluepointer.Element(p) : throw new RuntimeException("Failed to add xml element, p=" + p);
						}
						String xchildname = xParts.get(0);

						if (xchildname.startsWith("@"))
						{
//C# TO JAVA CONVERTER WARNING: Nullable reference types have no equivalent in Java:
//ORIGINAL LINE: string? objStr = WriteObjectToString(obj);
							String objStr = WriteObjectToString(obj);
							if (!(objStr == null || objStr.isBlank()))
							{
								XAttribute xatt = new XAttribute(tangible.StringHelper.trimStart(xchildname, '@'), objStr);
								xvaluepointer.Add(xatt);
							}
						}
						else if (Objects.equals(xchildname, "text()"))
						{
//C# TO JAVA CONVERTER WARNING: Nullable reference types have no equivalent in Java:
//ORIGINAL LINE: string? objStr = WriteObjectToString(obj);
							String objStr = WriteObjectToString(obj);
							if (!(objStr == null || objStr.isBlank()))
							{
								xvaluepointer.Value = objStr;
							}
						}
						else if (property.isQuantityList)
						{
							ArrayList<EventProduct> products = (ArrayList<EventProduct>)obj;
							products = products.stream().filter(p -> p.Quantity != null && p.Type == property.ProductType).collect(Collectors.toList());
							if (!products.isEmpty())
							{
								XElement xQuantityList = new XElement(xchildname);
								for (var product : products)
								{
									if (product.EPC != null && product.Quantity != null)
									{
										XElement xQuantity = new XElement("quantityElement", new XElement("epcClass", product.EPC.toString()), new XElement("quantity", product.Quantity.getValue()));

										if (!Objects.equals(product.Quantity.getUoM().getUNCode(), "EA"))
										{
											xQuantity.Add(new XElement("uom", product.Quantity.getUoM().getUNCode()));
										}

										xQuantityList.Add(xQuantity);
									}
								}
								xvaluepointer.Add(xQuantityList);
							}
						}
						else if (property.IsEPCList)
						{
							ArrayList<EventProduct> products = (ArrayList<EventProduct>)obj;
							products = products.stream().filter(p -> p.Quantity == null && p.Type == property.ProductType).collect(Collectors.toList());
							if (!products.isEmpty() || property.Required)
							{
								XElement xEPCList = new XElement(property.Name);
								for (var product : products)
								{
									if (product.EPC != null)
									{
										xEPCList.Add(new XElement("epc", product.EPC.toString()));
									}
								}
								xvaluepointer.Add(xEPCList);
							}
						}
						else if (property.IsArray)
						{
							List list = (List)obj;
							XElement xlist = xvaluepointer;
							if (!list.isEmpty() || property.Required)
							{
								if (property.ItemName != null)
								{
									XElement xl = new XElement(xchildname);
									xvaluepointer.Add(xl);
									xlist = xl;
								}
							}

							for (var o : list)
							{
								if (property.IsObject)
								{
									XElement xchild = ToXml(property.ItemName != null ? property.ItemName : xchildname, o, version, property.Required);
									if (xchild != null)
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
										XElement xchild = new XElement(property.ItemName != null ? property.ItemName : xchildname, objStr);
										xlist.Add(xchild);
									}
								}
							}
						}
						else if (property.IsObject)
						{
							XElement xchild = ToXml(xchildname, obj, version, property.Required);
							if (xchild != null)
							{
								xvaluepointer.Add(xchild);
							}
						}
						else
						{
//C# TO JAVA CONVERTER WARNING: Nullable reference types have no equivalent in Java:
//ORIGINAL LINE: string? objStr = WriteObjectToString(obj);
							String objStr = WriteObjectToString(obj);
							if (!(objStr == null || objStr.isBlank()))
							{
								XElement xchild = new XElement(xchildname, objStr);
								xvaluepointer.Add(xchild);
							}
						}
					}
					else if (property.Required)
					{
						XElement xvaluepointer = xvalue;
						xParts = StringExtensions.SplitXPath(property.Name);
						while (xParts.size() > 1)
						{
							String p = xParts.get(0);
							xParts.remove(0);
							if (xvaluepointer.Element(p) == null)
							{
								xvaluepointer.Add(new XElement(p));
							}
//C# TO JAVA CONVERTER TASK: Throw expressions are not converted by C# to Java Converter:
//ORIGINAL LINE: xvaluepointer = xvaluepointer.Element(p) ?? throw new Exception("Failed to add xml element, p=" + p);
							xvaluepointer = xvaluepointer.Element(p) != null ? xvaluepointer.Element(p) : throw new RuntimeException("Failed to add xml element, p=" + p);
						}
						String xchildname = xParts.get(0);
						XElement xchild = new XElement(xchildname);
						xvaluepointer.Add(xchild);
					}
				}

				if (typeInfo.extensionKDEs != null)
				{
//C# TO JAVA CONVERTER WARNING: Nullable reference types have no equivalent in Java:
//ORIGINAL LINE: object? obj = typeInfo.ExtensionKDEs.get(value);
					Object obj = typeInfo.extensionKDEs.get(value);
					if (obj != null && obj instanceof List<IEventKDE>)
					{
//C# TO JAVA CONVERTER WARNING: Nullable reference types have no equivalent in Java:
//ORIGINAL LINE: IList<IEventKDE>? kdes = obj instanceof java.util.List<IEventKDE> ? (java.util.List<IEventKDE>)obj : null;
						List<IEventKDE> kdes = obj instanceof List<IEventKDE> ? (List<IEventKDE>)obj : null;
						if (kdes != null)
						{
							for (var kde : kdes)
							{
								XElement xchild = kde.GetXml();
								if (xchild != null)
								{
									xvalue.Add(xchild);
								}
							}
						}
					}
				}

				if (typeInfo.getExtensionAttributes() != null)
				{
//C# TO JAVA CONVERTER WARNING: Nullable reference types have no equivalent in Java:
//ORIGINAL LINE: object? obj = typeInfo.ExtensionAttributes.get(value);
					Object obj = typeInfo.getExtensionAttributes().get(value);
					if (obj != null && obj instanceof List<IEventKDE>)
					{
//C# TO JAVA CONVERTER WARNING: Nullable reference types have no equivalent in Java:
//ORIGINAL LINE: IList<IEventKDE>? kdes = obj instanceof java.util.List<IEventKDE> ? (java.util.List<IEventKDE>)obj : null;
						List<IEventKDE> kdes = obj instanceof List<IEventKDE> ? (List<IEventKDE>)obj : null;
						if (kdes != null)
						{
							for (IEventKDE kde : kdes)
							{
								XElement xKDE = kde.GetXml();
								if (xKDE != null)
								{
									xvalue.Add(new XAttribute(xKDE.Name, xKDE.getValue()));
								}
							}
						}
					}
				}
			}

			return x;
		}
		else if (required == true)
		{
			XElement x = new XElement(xname);
			return x;
		}
		else
		{
			return null;
		}
	}

	public static <T> T FromXml(XElement x, EPCISVersion version)
	{
		T o = (T)FromXml(x, T.class, version);
		return o;
	}

	public static Object FromXml(XElement x, Type type, EPCISVersion version)
	{
//C# TO JAVA CONVERTER TASK: Throw expressions are not converted by C# to Java Converter:
//ORIGINAL LINE: object value = Activator.CreateInstance(type) ?? throw new Exception("Failed to create instance of type " + type.FullName);
		Object value = type.newInstance() != null ? type.newInstance() : throw new RuntimeException("Failed to create instance of type " + type.FullName);

		try
		{
			OTMappingTypeInformation mappingInfo = OTMappingTypeInformation.GetXmlTypeInfo(type);

			// if this is a list, then we will make a list of the objects...
			if (value instanceof List)
			{
				List list = (List)value;
//C# TO JAVA CONVERTER WARNING: Nullable reference types have no equivalent in Java:
//ORIGINAL LINE: OpenTraceabilityAttribute? att = type.GetCustomAttribute<OpenTraceabilityAttribute>();
				OpenTraceabilityAttribute att = type.<OpenTraceabilityAttribute>GetCustomAttribute();
				if (att != null)
				{
					for (XElement xchild : x.Elements(att.getName()))
					{
						Object childvalue = FromXml(xchild, type.GenericTypeArguments.First(), version);
						list.add(childvalue);
					}
				}
				else
				{
					for (XElement xchild : x.Elements())
					{
						Object childvalue = FromXml(xchild, type.GenericTypeArguments.First(), version);
						list.add(childvalue);
					}
				}
			}
			// else, try and parse the object...
			else
			{
				OTMappingTypeInformation typeInfo = OTMappingTypeInformation.GetXmlTypeInfo(type);

//C# TO JAVA CONVERTER WARNING: Nullable reference types have no equivalent in Java:
//ORIGINAL LINE: List<IEventKDE>? extensionKDEs = null;
				ArrayList<IEventKDE> extensionKDEs = null;
//C# TO JAVA CONVERTER WARNING: Nullable reference types have no equivalent in Java:
//ORIGINAL LINE: List<IEventKDE>? extensionAttributes = null;
				ArrayList<IEventKDE> extensionAttributes = null;

				if (typeInfo.getExtensionAttributes() != null)
				{
					extensionAttributes = new ArrayList<IEventKDE>();
				}

				if (typeInfo.extensionKDEs != null)
				{
					extensionKDEs = new ArrayList<IEventKDE>();
				}

//C# TO JAVA CONVERTER WARNING: Nullable reference types have no equivalent in Java:
//ORIGINAL LINE: OTMappingTypeInformationProperty? mappingProp;
				OTMappingTypeInformationProperty mappingProp;

				for (XAttribute xatt : x.Attributes())
				{
					mappingProp = typeInfo.get("@" + xatt.Name);
					if (mappingProp != null)
					{
						String xchildname = mappingProp.getName().toString();
//C# TO JAVA CONVERTER WARNING: Nullable reference types have no equivalent in Java:
//ORIGINAL LINE: string? attValue = x.Attribute(xchildname.TrimStart('@')) == null ? null : x.Attribute(xchildname.TrimStart('@')).Value;
						String attValue = x.Attribute(tangible.StringHelper.trimStart(xchildname, '@')) == null ? null : x.Attribute(tangible.StringHelper.trimStart(xchildname, '@')).Value;
						if (!tangible.StringHelper.isNullOrEmpty(attValue))
						{
							Object o = ReadObjectFromString(attValue, mappingProp.field.getType());
							mappingProp.field.set(value, o);
						}
					}
					else if (extensionAttributes != null)
					{
						IEventKDE kde = ReadKDE(xatt);
						extensionAttributes.add(kde);
					}
				}

				mappingProp = typeInfo.get("text()");
				if (mappingProp != null)
				{
//C# TO JAVA CONVERTER WARNING: Nullable reference types have no equivalent in Java:
//ORIGINAL LINE: string? eleText = x.Value;
					String eleText = x.Value;
					if (!(eleText == null || eleText.isBlank()))
					{
						Object o = ReadObjectFromString(eleText, mappingProp.field.getType());
						mappingProp.field.set(value, o);
					}
				}
				else
				{
					for (XElement xc : x.Elements())
					{
						XElement xchild = xc;

						mappingProp = typeInfo.get(xchild.Name.toString());
						if (mappingProp == null && tangible.ListHelper.exists(typeInfo.getProperties(), p -> StringExtensions.SplitXPath(p.Name).get(0) == xchild.Name))
						{
							// see if we have a parent matching way...
							for (var mp : typeInfo.getProperties().Where(p -> StringExtensions.SplitXPath(p.Name).get(0) == xchild.Name))
							{
								XElement xgrandchild = x.XPathSelectElement(mp.Name);
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

				if (typeInfo.getExtensionAttributes() != null)
				{
					typeInfo.getExtensionAttributes().SetValue(value, extensionAttributes);
				}

				if (typeInfo.extensionKDEs != null)
				{
					typeInfo.extensionKDEs.SetValue(value, extensionKDEs);
				}
			}
		}
		catch (RuntimeException ex)
		{
			OTLogger.Error(ex);
			throw ex;
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
		else if (obj instanceof ArrayList<LanguageString>)
		{
			var l = (ArrayList<LanguageString>)obj;
			if (l.isEmpty())
			{
				return null;
			}
			else
			{
				return l.get(0).Value;
			}
		}
		else if (obj instanceof OffsetDateTime)
		{
			OffsetDateTime dt = (OffsetDateTime)obj;
			return dt.toString("O");
		}
		else if (obj instanceof UOM)
		{
			UOM uom = (UOM)obj;
			return uom.getUNCode();
		}
		else if (obj instanceof Boolean)
		{
			boolean b = (Boolean)obj;
			return (String.valueOf(b) == null ? null : String.valueOf(b).toLowerCase()) != null ? (String.valueOf(b) == null ? null : String.valueOf(b).toLowerCase()) : "";
		}
		else if (obj instanceof Country)
		{
			Country b = (Country)obj;
			return b.getAbbreviation();
		}
		else if (obj instanceof TimeSpan)
		{
			TimeSpan timespan = (TimeSpan)obj;
			if (timespan.Ticks < 0)
			{
				return "-" + (new Double(timespan.Negate().TotalHours)).toString("#00") + ":" + (new Integer(timespan.Minutes)).toString("00");
			}
			else
			{
				return "+" + (new Double(timespan.TotalHours)).toString("#00") + ":" + (new Integer(timespan.Minutes)).toString("00");
			}
		}
		else
		{
			return obj.toString() != null ? obj.toString() : "";
		}
	}

	private static void ReadPropertyMapping(OTMappingTypeInformationProperty mappingProp, XElement xchild, Object value, EPCISVersion version)
	{
		if (mappingProp.isQuantityList())
		{
			IEvent e = (IEvent)value;
			for (var xQuantity : xchild.Elements("quantityElement"))
			{
				EPC epc = new EPC((xQuantity.Element("epcClass") == null ? null : xQuantity.Element("epcClass").Value) != null ? (xQuantity.Element("epcClass") == null ? null : xQuantity.Element("epcClass").Value) : "");
				EventProduct product = new EventProduct(epc);
				product.Type = mappingProp.productType;

				double quantity = Double.parseDouble((xQuantity.Element("quantity") == null ? null : xQuantity.Element("quantity").Value) != null ? (xQuantity.Element("quantity") == null ? null : xQuantity.Element("quantity").Value) : "");
				String uom = xQuantity.Element("uom") == null ? null : ((xQuantity.Element("uom").Value) != null ? xQuantity.Element("uom").Value : "EA");
				product.setQuantity(new Measurement(quantity, uom));

				e.addProduct(product);
			}
		}
		else if (mappingProp.isEPCList())
		{
			IEvent e = (IEvent)value;
			for (var xEPC : xchild.Elements("epc"))
			{
				EPC epc = new EPC(xEPC.Value);
				EventProduct product = new EventProduct(epc);
				product.Type = mappingProp.productType;
				e.addProduct(product);
			}
		}
		else if (mappingProp.isArray())
		{
			Object tempVar = mappingProp.field.get(value);
//C# TO JAVA CONVERTER WARNING: Nullable reference types have no equivalent in Java:
//ORIGINAL LINE: IList? list = tempVar instanceof java.util.List ? (java.util.List)tempVar : null;
			List list = tempVar instanceof List ? (List)tempVar : null;
			if (list == null)
			{
//C# TO JAVA CONVERTER TASK: Throw expressions are not converted by C# to Java Converter:
//ORIGINAL LINE: list = (IList)(Activator.CreateInstance(mappingProp.Property.PropertyType) ?? throw new Exception("Failed to create instance of " + mappingProp.Property.PropertyType.FullName));
				list = (List)(mappingProp.field.getType().newInstance() != null ? mappingProp.field.getType().newInstance() : throw new RuntimeException("Failed to create instance of " + mappingProp.field.getType().FullName));

				mappingProp.field.set(value, list);
			}

			Type itemType = mappingProp.field.getType().GenericTypeArguments[0];
			if (mappingProp.getItemName() != null)
			{
				for (XElement xitem : xchild.Elements(mappingProp.getItemName()))
				{
					if (mappingProp.isObject())
					{
						Object o = FromXml(xitem, itemType, version);
						list.add(o);
					}
					else
					{
						Object o = ReadObjectFromString(xitem.Value, itemType);
						list.add(o);
					}
				}
			}
			else
			{
				if (mappingProp.isObject())
				{
					Object o = FromXml(xchild, itemType, version);
					list.add(o);
				}
				else
				{
					Object o = ReadObjectFromString(xchild.Value, itemType);
					list.add(o);
				}
			}
		}
		else if (mappingProp.isObject())
		{
			Object o = FromXml(xchild, mappingProp.field.getType(), version);
			mappingProp.field.set(value, o);
		}
		else
		{
//C# TO JAVA CONVERTER WARNING: Nullable reference types have no equivalent in Java:
//ORIGINAL LINE: string? eleText = xchild.Value;
			String eleText = xchild.Value;
			if (!(eleText == null || eleText.isBlank()))
			{
				Object o = ReadObjectFromString(eleText, mappingProp.field.getType());
				mappingProp.field.set(value, o);
			}
		}
	}

	private static Object ReadObjectFromString(String value, Type t)
	{
		if (t == OffsetDateTime.class || t == OffsetDateTime.class)
		{
			OffsetDateTime tempVar = StringExtensions.TryConvertToDateTimeOffset(value);
//C# TO JAVA CONVERTER TASK: Throw expressions are not converted by C# to Java Converter:
//ORIGINAL LINE: OffsetDateTime dt = value.TryConvertToDateTimeOffset() ?? throw new Exception("Failed to convert string to datetimeoffset where value = " + value);
			OffsetDateTime dt = tempVar != null ? tempVar : throw new RuntimeException("Failed to convert string to datetimeoffset where value = " + value);
			return dt;
		}
		else if (t == ArrayList<LanguageString>.class)
		{
			ArrayList<LanguageString> l = new ArrayList<LanguageString>();
			LanguageString tempVar2 = new LanguageString();
			tempVar2.setLanguage("en-US");
			tempVar2.setValue(value);
			l.add(tempVar2);
			return l;
		}
		else if (t == UOM.class)
		{
			UOM uom = UOM.LookUpFromUNCode(value);
			return uom;
		}
		else if (t == Boolean.class || t == Boolean.class)
		{
			boolean v = Boolean.parseBoolean(value);
			return v;
		}
		else if (t == Double.class || t == Double.class)
		{
			double v = Double.parseDouble(value);
			return v;
		}
		else if (t == Uri.class)
		{
			Uri v = new Uri(value);
			return v;
		}
		else if (t == TimeSpan.class || t == TimeSpan.class)
		{
			if (value.startsWith("+"))
			{
				value = value.substring(1);
			}
			TimeSpan ts = TimeSpan.Parse(value);
			return ts;
		}
		else if (t == EventAction.class || t == EventAction.class)
		{
			EventAction action = Enum.<EventAction>Parse(value);
			return action;
		}
		else if (t == PGLN.class)
		{
			PGLN pgln = new PGLN(value);
			return pgln;
		}
		else if (t == GLN.class)
		{
			GLN gln = new GLN(value);
			return gln;
		}
		else if (t == EPC.class)
		{
			EPC epc = new EPC(value);
			return epc;
		}
		else if (t == Country.class)
		{
			Country c = Countries.Parse(value);
			return c;
		}
		else
		{
			return value;
		}
	}

	private static IEventKDE ReadKDE(XElement x)
	{
		// we need to parse the xml into an event KDE here...

		// check if it is a registered KDE...
//C# TO JAVA CONVERTER WARNING: Nullable reference types have no equivalent in Java:
//ORIGINAL LINE: IEventKDE? kde = IEventKDE.InitializeKDE(x.Name.NamespaceName, x.Name.LocalName);
		IEventKDE kde = IEventKDE.InitializeKDE(x.Name.NamespaceName, x.Name.LocalName);

		// if not, then check if the data type is specified and we recognize it
		if (kde == null)
		{
			XAttribute xsiType = x.Attribute((String)Constants.XSI_NAMESPACE + "type");
			if (xsiType != null)
			{
				switch (xsiType.getValue())
				{
					case "string":
						kde = new EventKDEString(x.Name.NamespaceName, x.Name.LocalName);
						break;
					case "boolean":
						kde = new EventKDEBoolean(x.Name.NamespaceName, x.Name.LocalName);
						break;
					case "number":
						kde = new EventKDEDouble(x.Name.NamespaceName, x.Name.LocalName);
						break;
				}
			}
		}

		// if not, check if it is a simple value or an object
		if (kde == null)
		{
			if (x.Elements().Count() > 0)
			{
				kde = new EventKDEObject(x.Name.NamespaceName, x.Name.LocalName);
			}
			// else if simple value, then we will consume it as a string
			else
			{
				kde = new EventKDEString(x.Name.NamespaceName, x.Name.LocalName);
			}
		}

		if (kde != null)
		{
			kde.SetFromXml(x);
		}
		else
		{
			throw new RuntimeException("Failed to initialize KDE from XML = " + x.toString());
		}

		return kde;
	}

	private static IEventKDE ReadKDE(XAttribute x)
	{
		// we need to parse the xml into an event KDE here...

		// check if it is a registered KDE...
//C# TO JAVA CONVERTER WARNING: Nullable reference types have no equivalent in Java:
//ORIGINAL LINE: IEventKDE? kde = IEventKDE.InitializeKDE(x.Name.NamespaceName, x.Name.LocalName);
		IEventKDE kde = IEventKDE.InitializeKDE(x.Name.NamespaceName, x.Name.LocalName);

		// if not, check if it is a simple value or an object
		if (kde == null)
		{
			kde = new EventKDEString(x.Name.NamespaceName, x.Name.LocalName);
		}

		if (kde != null)
		{
			XElement xe = new XElement(x.Name, x.Value);
			kde.SetFromXml(xe);
		}
		else
		{
			throw new RuntimeException("Failed to initialize KDE from XML Attribute = " + x.toString());
		}

		return kde;
	}
}
