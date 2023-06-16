package opentraceability.mappers.epcis.xml;

import Newtonsoft.Json.*;
import opentraceability.interfaces.*;
import opentraceability.models.events.*;
import opentraceability.models.identifiers.*;
import opentraceability.utility.*;
import opentraceability.utility.attributes.*;
import opentraceability.*;
import opentraceability.mappers.*;
import opentraceability.mappers.epcis.*;
import java.util.*;

public class EPCISXmlEventWriter
{
	public static XElement WriteEvent(IEvent e, EPCISVersion epcisVersion)
	{
		ArrayList<EPCISMappingKDE> kdes = EPCISMappingKDE.getMappingKDEs().get("Base");
//C# TO JAVA CONVERTER WARNING: Nullable reference types have no equivalent in Java:
//ORIGINAL LINE: Type? eventType = null;
		Type eventType = null;
		XElement xEvent = null;

		if (e instanceof ObjectEvent)
		{
			xEvent = new XElement("ObjectEvent");
			eventType = ObjectEvent.class;
			kdes.addAll(EPCISMappingKDE.getMappingKDEs().get("ObjectEvent"));
		}
		else if (e instanceof AggregationEvent)
		{
			xEvent = new XElement("AggregationEvent");
			eventType = AggregationEvent.class;
			kdes.addAll(EPCISMappingKDE.getMappingKDEs().get("AggregationEvent"));
		}
		else if (e instanceof TransformationEvent)
		{
			xEvent = new XElement("TransformationEvent");
			eventType = TransformationEvent.class;
			kdes.addAll(EPCISMappingKDE.getMappingKDEs().get("TransformationEvent"));
		}
		else if (e instanceof TransactionEvent)
		{
			xEvent = new XElement("TransactionEvent");
			eventType = TransactionEvent.class;
			kdes.addAll(EPCISMappingKDE.getMappingKDEs().get("TransactionEvent"));
		}
		else if (e instanceof AssociationEvent)
		{
			xEvent = new XElement("AssociationEvent");
			eventType = AssociationEvent.class;
			kdes.addAll(EPCISMappingKDE.getMappingKDEs().get("AssociationEvent"));
		}
		else
		{
			throw new RuntimeException("Unrecognized event type = " + e.getClass().getName());
		}

		kdes = kdes.stream().filter(k -> k.Version == null || k.Version == epcisVersion).collect(Collectors.toList());

		// go through each item in the kdes list...
		for (var kde : kdes)
		{
			XElement x = xEvent;

			// check if the property on the event has a value and is not null...
			if (kde.getProperty() != null)
			{
//C# TO JAVA CONVERTER WARNING: Nullable reference types have no equivalent in Java:
//ORIGINAL LINE: object? value = GetPropertyValue(e, kde.Property);
				Object value = GetPropertyValue(e, kde.getProperty());
				if (value != null)
				{
					if (value instanceof List && kde.getRequired() == false && ((List)value).isEmpty())
					{
						continue;
					}

					// make sure we have created the xml element correctly.
					ArrayList<String> xParts = kde.getXPath().split("[/]", -1).ToList();
					while (xParts.size() > 1)
					{
						String p = xParts.get(0);
						xParts.remove(0);
						if (x.Element(p) == null)
						{
							x.Add(new XElement(p));
						}
//C# TO JAVA CONVERTER TASK: Throw expressions are not converted by C# to Java Converter:
//ORIGINAL LINE: x = x.Element(p) ?? throw new Exception("Failed to add xml element, p=" + p);
						x = x.Element(p) != null ? x.Element(p) : throw new RuntimeException("Failed to add xml element, p=" + p);
					}

					// convert the C# property value into XML
					switch (kde.getType())
					{
						case "EventTimeZoneOffset":
							WriteEventTimeZoneOffset(x, kde, value instanceof Double ? (Double)value : null);
							break;
						case "OffsetDateTime":
							WriteDateTimeOffset(x, kde, value instanceof OffsetDateTime ? (OffsetDateTime)value : null);
							break;
						case "URI":
						case "Action":
						case "String":
						case "GLN":
							WriteString(x, kde, value);
							break;
						default:
						{
								if (value != null)
								{
									XElement xe = WriteObject(kde.getXPath().split("[/]", -1).Last(), value, kde.getRequired());
									if (xe != null)
									{
										x.Add(xe);
									}
								}
								break;
						}
					}
				}
			}
			else if (Objects.equals(kde.getType(), "ParentID"))
			{
				WriteParentID(kde, e, x);
			}
			else if (Objects.equals(kde.getType(), "EPCList"))
			{
				String xname = kde.getXPath().split("[/]", -1).Last();
				switch (xname)
				{
					case "epcList":
						WriteEPCList(kde, e, x, EventProductType.Reference, kde.getRequired());
						break;
					case "outputEPCList":
						WriteEPCList(kde, e, x, EventProductType.Output, kde.getRequired());
						break;
					case "inputEPCList":
						WriteEPCList(kde, e, x, EventProductType.Input, kde.getRequired());
						break;
					case "childEPCs":
						WriteEPCList(kde, e, x, EventProductType.Child, kde.getRequired());
						break;
					default:
						throw new RuntimeException("Did not recognize epc list xpath. " + JsonConvert.SerializeObject(kde));
				}
			}
			else if (Objects.equals(kde.getType(), "QuantityList"))
			{
				String xname = kde.getXPath().split("[/]", -1).Last();
				switch (xname)
				{
					case "quantityList":
						WriteQuantityList(kde, e, x, EventProductType.Reference);
						break;
					case "outputQuantityList":
						WriteQuantityList(kde, e, x, EventProductType.Output);
						break;
					case "inputQuantityList":
						WriteQuantityList(kde, e, x, EventProductType.Input);
						break;
					case "childQuantityList":
						WriteQuantityList(kde, e, x, EventProductType.Child);
						break;
					default:
						throw new RuntimeException("Did not recognize epc list xpath. " + JsonConvert.SerializeObject(kde));
				}
			}
			else
			{
				throw new RuntimeException("Unable to process EPCIS KDE Mapping. " + JsonConvert.SerializeObject(kde));
			}
		}

		// write the extension KDEs
		for (var kde : e.getKDEs())
		{
			XElement xKDE = kde.GetXml();
			if (xKDE != null)
			{
				xEvent.Add(xKDE);
			}
		}

		if (e instanceof TransformationEvent && epcisVersion == EPCISVersion.V1)
		{
			xEvent = new XElement("extension", xEvent);
		}

		return xEvent;
	}

//C# TO JAVA CONVERTER WARNING: Nullable reference types have no equivalent in Java:
//ORIGINAL LINE: private static object? GetPropertyValue(object o, string propertyName)
	private static Object GetPropertyValue(Object o, String propertyName)
	{
//C# TO JAVA CONVERTER WARNING: Nullable reference types have no equivalent in Java:
//ORIGINAL LINE: object? v = null;
		Object v = null;
		ArrayList<String> parts = propertyName.split(java.util.regex.Pattern.quote("."), -1).ToList();
		while (parts.size() > 1)
		{
			String pName = parts.get(0);
//C# TO JAVA CONVERTER TASK: Throw expressions are not converted by C# to Java Converter:
//ORIGINAL LINE: PropertyInfo p = o.GetType().GetProperty(pName) ?? throw new Exception(string.Format("Failed to find property on {0} with name {1}", o.GetType().FullName, pName));
			PropertyInfo p = (o.getClass().GetProperty(pName)) != null ? o.getClass().GetProperty(pName) : throw new RuntimeException(String.format("Failed to find property on %1$s with name %2$s", o.getClass().getName(), pName));
//C# TO JAVA CONVERTER WARNING: Nullable reference types have no equivalent in Java:
//ORIGINAL LINE: object? o2 = p.get(o);
			Object o2 = p.get(o);
			if (o2 == null)
			{
//C# TO JAVA CONVERTER TASK: Throw expressions are not converted by C# to Java Converter:
//ORIGINAL LINE: o2 = Activator.CreateInstance(p.PropertyType) ?? throw new Exception("Failed to create instance of " + p.PropertyType.FullName);
				o2 = p.PropertyType.newInstance() != null ? p.PropertyType.newInstance() : throw new RuntimeException("Failed to create instance of " + p.PropertyType.FullName);
				p.SetValue(o, o2);
			}
			o = o2;
			parts.remove(0);
		}

//C# TO JAVA CONVERTER TASK: Throw expressions are not converted by C# to Java Converter:
//ORIGINAL LINE: PropertyInfo prop = o.GetType().GetProperty(parts.First()) ?? throw new Exception(string.Format("Failed to find property on {0} with name {1}", o.GetType().FullName, parts.First()));
		PropertyInfo prop = (o.getClass().GetProperty(parts.get(0))) != null ? o.getClass().GetProperty(parts.get(0)) : throw new RuntimeException(String.format("Failed to find property on %1$s with name %2$s", o.getClass().getName(), parts.get(0)));
		v = prop.get(o);
		return v;
	}

	private static void WriteEventTimeZoneOffset(XElement x, EPCISMappingKDE kde, Double hours)
	{
		if (hours != null)
		{
			TimeSpan ts = TimeSpan.FromHours(Math.abs(hours.doubleValue()));
			String offset = String.format("%1$s:%2$s", tangible.StringHelper.padLeft(String.valueOf(ts.Hours), 2, '0'), tangible.StringHelper.padLeft(String.valueOf(ts.Minutes), 2, '0'));
			if (hours.doubleValue() >= 0)
			{
				offset = "+" + offset;
			}
			else
			{
				offset = "-" + offset;
			}

			x.Add(new XElement(kde.getXPath(), offset));
		}
	}

	private static void WriteDateTimeOffset(XElement x, EPCISMappingKDE kde, OffsetDateTime value)
	{
		if (value != null)
		{
			x.Add(new XElement(kde.getXPath().split("[/]", -1).Last(), value.getValue().toString("o")));
		}
	}

//C# TO JAVA CONVERTER WARNING: Nullable reference types have no equivalent in Java:
//ORIGINAL LINE: private static void WriteString(XElement x, EPCISMappingKDE kde, object? value)
	private static void WriteString(XElement x, EPCISMappingKDE kde, Object value)
	{
		if ((value == null ? null : value.toString()) != null)
		{
			x.Add(new XElement(kde.getXPath().split("[/]", -1).Last(), value.toString()));
		}
	}


	private static void WriteEPCList(EPCISMappingKDE kde, IEvent e, XElement x, EventProductType productType)
	{
		WriteEPCList(kde, e, x, productType, false);
	}

//C# TO JAVA CONVERTER NOTE: Java does not support optional parameters. Overloaded method(s) are created above:
//ORIGINAL LINE: private static void WriteEPCList(EPCISMappingKDE kde, IEvent e, XElement x, EventProductType productType, bool required=false)
	private static void WriteEPCList(EPCISMappingKDE kde, IEvent e, XElement x, EventProductType productType, boolean required)
	{
		ArrayList<EventProduct> products = e.getProducts().stream().filter(p -> p.Type == productType && p.Quantity == null).collect(Collectors.toList());
		if (!products.isEmpty() || required == true)
		{
			// make sure we have created the xml element correctly.
			ArrayList<String> xParts = kde.getXPath().split("[/]", -1).ToList();
			while (xParts.size() > 1)
			{
				String p = xParts.get(0);
				xParts.remove(0);
				if (x.Element(p) == null)
				{
					x.Add(new XElement(p));
				}
//C# TO JAVA CONVERTER TASK: Throw expressions are not converted by C# to Java Converter:
//ORIGINAL LINE: x = x.Element(p) ?? throw new Exception("Failed to add xml element, p=" + p);
				x = x.Element(p) != null ? x.Element(p) : throw new RuntimeException("Failed to add xml element, p=" + p);
			}

			String xName = kde.getXPath().split("[/]", -1).Last();
			XElement xEPCList = new XElement(xName);
			for (EventProduct prod : products)
			{
				if (prod.EPC != null)
				{
					xEPCList.Add(new XElement("epc", prod.EPC.toString()));
				}
			}
			x.Add(xEPCList);
		}
	}

	private static void WriteQuantityList(EPCISMappingKDE kde, IEvent e, XElement x, EventProductType productType)
	{
		ArrayList<EventProduct> products = e.getProducts().stream().filter(p -> p.Type == productType && p.Quantity != null).collect(Collectors.toList());
		if (!products.isEmpty())
		{
			// make sure we have created the xml element correctly.
			ArrayList<String> xParts = kde.getXPath().split("[/]", -1).ToList();
			while (xParts.size() > 1)
			{
				String p = xParts.get(0);
				xParts.remove(0);
				if (x.Element(p) == null)
				{
					x.Add(new XElement(p));
				}
//C# TO JAVA CONVERTER TASK: Throw expressions are not converted by C# to Java Converter:
//ORIGINAL LINE: x = x.Element(p) ?? throw new Exception("Failed to add xml element, p=" + p);
				x = x.Element(p) != null ? x.Element(p) : throw new RuntimeException("Failed to add xml element, p=" + p);
			}

			String xName = kde.getXPath().split("[/]", -1).Last();
			XElement xQuantityList = new XElement(xName);
			for (EventProduct product : products)
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
			x.Add(xQuantityList);
		}
	}

	private static void WriteParentID(EPCISMappingKDE kde, IEvent e, XElement x)
	{
		String xName = kde.getXPath().split("[/]", -1).Last();
//C# TO JAVA CONVERTER WARNING: Nullable reference types have no equivalent in Java:
//ORIGINAL LINE: EventProduct? parent = e.Products.FirstOrDefault(p => p.Type == EventProductType.Parent);
		EventProduct parent = e.getProducts().FirstOrDefault(p -> p.Type == EventProductType.Parent);
		if ((parent == null ? null : parent.getEPC()) != null)
		{
			x.Add(new XElement(xName, parent.EPC.toString()));
		}
	}


	private static XElement WriteObject(XName xname, Object value)
	{
		return WriteObject(xname, value, false);
	}

//C# TO JAVA CONVERTER WARNING: Nullable reference types have no equivalent in Java:
//ORIGINAL LINE: private static System.Nullable<XElement> WriteObject(XName xname, object? value, bool required=false)
//C# TO JAVA CONVERTER NOTE: Java does not support optional parameters. Overloaded method(s) are created above:
	private static XElement WriteObject(XName xname, Object value, boolean required)
	{
		XElement xvalue = null;
		//if (value != null)
		//{
		//    if (value is IList)
		//    {
		//        IList list = (IList)value;
		//        if (list.Count > 0 || required == true)
		//        {
		//            xvalue = new XElement(xname);
		//            Type t = list[0]?.GetType() ?? throw new Exception("Failed to get list item type.");
		//            XName xchildname = t.GetCustomAttribute<OpenTraceabilityAttribute>()?.Name ?? throw new Exception("Failed to get xname from type. type = " + t.FullName);
		//            foreach (var v in list)
		//            {
		//                XElement? xListValue = WriteObject(xchildname, v);
		//                if (xListValue != null)
		//                {
		//                    xvalue.Add(xListValue);
		//                }
		//            }
		//        }
		//    }
		//    else
		//    {
		//        xvalue = new XElement(xname);
		//        Type t = value.GetType();
		//        OTMappingTypeInformation typeInfo = OTMappingTypeInformation.GetTypeInfo(t);
		//        foreach (var kvp in typeInfo.XmlAttributes)
		//        {
		//            object? obj = kvp.Value.get(value);
		//            if (obj != null)
		//            {
		//                string xchildname = kvp.Key.Name.ToString();
		//                if (xchildname.StartsWith("@"))
		//                {
		//                    string objStr = WriteObjectToString(obj);
		//                    if (!string.IsNullOrWhiteSpace(objStr))
		//                    {
		//                        XAttribute xatt = new XAttribute(xchildname.TrimStart('@'), objStr);
		//                        xvalue.Add(xatt);
		//                    }
		//                }
		//                else if (xchildname == "text()")
		//                {
		//                    string objStr = WriteObjectToString(obj);
		//                    if (!string.IsNullOrWhiteSpace(objStr))
		//                    {
		//                        xvalue.Value = objStr;
		//                    }
		//                }
		//                else if (kvp.Value.GetCustomAttribute<OpenTraceabilityObjectAttribute>() != null)
		//                {
		//                    XElement? xchild = WriteObject(xchildname, obj);
		//                    if (xchild != null)
		//                    {
		//                        xvalue.Add(xchild);
		//                    }
		//                }
		//                else if (kvp.Value.GetCustomAttribute<OpenTraceabilityArrayAttribute>() != null)
		//                {
		//                    foreach (var o in (IList)obj)
		//                    {
		//                        if (o.GetType() == typeof(Uri))
		//                        {
		//                            string objStr = WriteObjectToString(o);
		//                            if (!string.IsNullOrWhiteSpace(objStr))
		//                            {
		//                                XElement xchild = new XElement(xchildname, objStr);
		//                                xvalue.Add(xchild);
		//                            }
		//                        }
		//                        else
		//                        {
		//                            XElement? xchild = WriteObject(xchildname, o);
		//                            if (xchild != null)
		//                            {
		//                                xvalue.Add(xchild);
		//                            }
		//                        }
		//                    }
		//                }
		//                else
		//                {
		//                    string objStr = WriteObjectToString(obj);
		//                    if (!string.IsNullOrWhiteSpace(objStr))
		//                    {
		//                        XElement xchild = new XElement(xchildname, objStr);
		//                        xvalue.Add(xchild);
		//                    }
		//                }
		//            }
		//        }

		//        if (typeInfo.ExtensionKDEs != null)
		//        {
		//            object? obj = typeInfo.ExtensionKDEs.get(value);
		//            if (obj != null && obj is IList<IEventKDE>)
		//            {
		//                IList<IEventKDE>? kdes = obj as IList<IEventKDE>;
		//                if (kdes != null)
		//                {
		//                    foreach (var kde in kdes)
		//                    {
		//                        XElement? xchild = kde.GetXml();
		//                        if (xchild != null)
		//                        {
		//                            xvalue.Add(xchild);
		//                        }
		//                    }
		//                }
		//            }
		//        }

		//        if (typeInfo.ExtensionAttributes != null)
		//        {
		//            object? obj = typeInfo.ExtensionAttributes.get(value);
		//            if (obj != null && obj is IList<IEventKDE>)
		//            {
		//                IList<IEventKDE>? kdes = obj as IList<IEventKDE>;
		//                if (kdes != null)
		//                {
		//                    foreach (IEventKDE kde in kdes)
		//                    {
		//                        XElement? xKDE = kde.GetXml();
		//                        if (xKDE != null)
		//                        {
		//                            xvalue.Add(new XAttribute(xKDE.Name, xKDE.Value));
		//                        }
		//                    }
		//                }
		//            }
		//        }
		//    }
		//}
		return xvalue;
	}

	private static String WriteObjectToString(Object obj)
	{
		if (obj instanceof OffsetDateTime)
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
		else
		{
			return obj.toString() != null ? obj.toString() : "";
		}
	}
}
