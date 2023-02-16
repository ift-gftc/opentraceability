using Newtonsoft.Json;
using OpenTraceability.Interfaces;
using OpenTraceability.Models.Events;
using OpenTraceability.Models.Identifiers;
using OpenTraceability.Utility;
using OpenTraceability.Utility.Attributes;
using System;
using System.Collections;
using System.Reflection;
using System.Runtime.Intrinsics.X86;
using System.Xml.Linq;
using static System.Runtime.InteropServices.JavaScript.JSType;

namespace OpenTraceability.Mappers.EPCIS.XML
{
    public class EPCISXmlEventWriter
    {
        public static XElement WriteEvent(IEvent e, EPCISVersion epcisVersion)
        {
            List<EPCISMappingKDE> kdes = EPCISMappingKDE.MappingKDEs["Base"].ToList();
            Type? eventType = null;
            XElement? xEvent = null;

            if (e is ObjectEvent)
            {
                xEvent = new XElement("ObjectEvent");
                eventType = typeof(ObjectEvent);
                kdes.AddRange(EPCISMappingKDE.MappingKDEs["ObjectEvent"].ToList());
            }
            else if (e is AggregationEvent)
            {
                xEvent = new XElement("AggregationEvent");
                eventType = typeof(AggregationEvent);
                kdes.AddRange(EPCISMappingKDE.MappingKDEs["AggregationEvent"].ToList());
            }
            else if (e is TransformationEvent)
            {
                xEvent = new XElement("TransformationEvent");
                eventType = typeof(TransformationEvent);
                kdes.AddRange(EPCISMappingKDE.MappingKDEs["TransformationEvent"].ToList());
            }
            else if (e is TransactionEvent)
            {
                xEvent = new XElement("TransactionEvent");
                eventType = typeof(TransactionEvent);
                kdes.AddRange(EPCISMappingKDE.MappingKDEs["TransactionEvent"].ToList());
            }
            else if (e is AssociationEvent)
            {
                xEvent = new XElement("AssociationEvent");
                eventType = typeof(AssociationEvent);
                kdes.AddRange(EPCISMappingKDE.MappingKDEs["AssociationEvent"].ToList());
            }
            else
            {
                throw new Exception("Unrecognized event type = " + e.GetType().FullName);
            }

            kdes = kdes.Where(k => k.Version == null || k.Version == epcisVersion).ToList();

            // go through each item in the kdes list...
            foreach (var kde in kdes)
            {
                XElement x = xEvent;

                // check if the property on the event has a value and is not null...
                if (kde.Property != null)
                {
                    object? value = GetPropertyValue(e, kde.Property);
                    if (value != null)
                    {
                        if (value is IList && kde.Required == false && ((IList)value).Count == 0)
                        {
                            continue;
                        }

                        // make sure we have created the xml element correctly.
                        List<string> xParts = kde.XPath.Split('/').ToList();
                        while (xParts.Count > 1)
                        {
                            string p = xParts.First();
                            xParts.RemoveAt(0);
                            if (x.Element(p) == null)
                            {
                                x.Add(new XElement(p));
                            }
                            x = x.Element(p) ?? throw new Exception("Failed to add xml element, p=" + p);
                        }

                        // convert the C# property value into XML
                        switch (kde.Type)
                        {
                            case "EventTimeZoneOffset": WriteEventTimeZoneOffset(x, kde, value as double?); break;
                            case "DateTimeOffset": WriteDateTimeOffset(x, kde, value as DateTimeOffset?); break;
                            case "URI":
                            case "Action":
                            case "String":
                            case "GLN": WriteString(x, kde, value); break;
                            default:
                                {
                                    if (value != null)
                                    {
                                        XElement? xe = WriteObject(kde.XPath.Split('/').Last(), value, kde.Required);
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
                else if (kde.Type == "ParentID")
                {
                    WriteParentID(kde, e, x);
                }
                else if (kde.Type == "EPCList")
                {
                    string xname = kde.XPath.Split('/').Last();
                    switch (xname)
                    {
                        case "epcList": WriteEPCList(kde, e, x, EventProductType.Reference, kde.Required); break;
                        case "outputEPCList": WriteEPCList(kde, e, x, EventProductType.Output, kde.Required); break;
                        case "inputEPCList": WriteEPCList(kde, e, x, EventProductType.Input, kde.Required); break;
                        case "childEPCs": WriteEPCList(kde, e, x, EventProductType.Child, kde.Required); break;
                        default: throw new Exception("Did not recognize epc list xpath. " + JsonConvert.SerializeObject(kde));
                    }
                }
                else if (kde.Type == "QuantityList")
                {
                    string xname = kde.XPath.Split('/').Last();
                    switch (xname)
                    {
                        case "quantityList": WriteQuantityList(kde, e, x, EventProductType.Reference); break;
                        case "outputQuantityList": WriteQuantityList(kde, e, x, EventProductType.Output); break;
                        case "inputQuantityList": WriteQuantityList(kde, e, x, EventProductType.Input); break;
                        case "childQuantityList": WriteQuantityList(kde, e, x, EventProductType.Child); break;
                        default: throw new Exception("Did not recognize epc list xpath. " + JsonConvert.SerializeObject(kde));
                    }
                }
                else
                {
                    throw new Exception("Unable to process EPCIS KDE Mapping. " + JsonConvert.SerializeObject(kde));
                }
            }

            // write the extension KDEs
            foreach (var kde in e.KDEs)
            {
                XElement? xKDE = kde.GetXml();
                if (xKDE != null)
                {
                    xEvent.Add(xKDE);
                }
            }

            if (e is TransformationEvent && epcisVersion == EPCISVersion.V1)
            {
                xEvent = new XElement("extension", xEvent);
            }

            return xEvent;
        }

        private static object? GetPropertyValue(object o, string propertyName)
        {
            object? v = null;
            List<string> parts = propertyName.Split(".").ToList();
            while (parts.Count > 1)
            {
                string pName = parts.First();
                PropertyInfo p = o.GetType().GetProperty(pName) ?? throw new Exception($"Failed to find property on {o.GetType().FullName} with name {pName}");
                object? o2 = p.GetValue(o);
                if (o2 == null)
                {
                    o2 = Activator.CreateInstance(p.PropertyType) ?? throw new Exception("Failed to create instance of " + p.PropertyType.FullName);
                    p.SetValue(o, o2);
                }
                o = o2;
                parts.RemoveAt(0);
            }

            PropertyInfo prop = o.GetType().GetProperty(parts.First()) ?? throw new Exception($"Failed to find property on {o.GetType().FullName} with name {parts.First()}");
            v = prop.GetValue(o);
            return v;
        }

        private static void WriteEventTimeZoneOffset(XElement x, EPCISMappingKDE kde, double? hours)
        {
            if (hours != null)
            {
                TimeSpan ts = TimeSpan.FromHours(Math.Abs(hours.Value));
                string offset = $"{ts.Hours.ToString().PadLeft(2, '0')}:{ts.Minutes.ToString().PadLeft(2, '0')}";
                if (hours.Value >= 0) offset = "+" + offset;
                else offset = "-" + offset;

                x.Add(new XElement(kde.XPath, offset));
            }
        }

        private static void WriteDateTimeOffset(XElement x, EPCISMappingKDE kde, DateTimeOffset? value)
        {
            if (value != null)
            {
                x.Add(new XElement(kde.XPath.Split('/').Last(), value.Value.ToString("o")));
            }
        }

        private static void WriteString(XElement x, EPCISMappingKDE kde, object? value)
        {
            if (value?.ToString() != null)
            {
                x.Add(new XElement(kde.XPath.Split('/').Last(), value.ToString()));
            }
        }

        private static void WriteEPCList(EPCISMappingKDE kde, IEvent e, XElement x, EventProductType productType, bool required=false)
        {
            List<EventProduct> products = e.Products.Where(p => p.Type == productType && p.Quantity == null).ToList();
            if (products.Count > 0 || required == true)
            {
                // make sure we have created the xml element correctly.
                List<string> xParts = kde.XPath.Split('/').ToList();
                while (xParts.Count > 1)
                {
                    string p = xParts.First();
                    xParts.RemoveAt(0);
                    if (x.Element(p) == null)
                    {
                        x.Add(new XElement(p));
                    }
                    x = x.Element(p) ?? throw new Exception("Failed to add xml element, p=" + p);
                }

                string xName = kde.XPath.Split('/').Last();
                XElement xEPCList = new XElement(xName);
                foreach (EventProduct prod in products)
                {
                    if (prod.EPC != null)
                    {
                        xEPCList.Add(new XElement("epc", prod.EPC.ToString()));
                    }
                }
                x.Add(xEPCList);
            }
        }

        private static void WriteQuantityList(EPCISMappingKDE kde, IEvent e, XElement x, EventProductType productType)
        {
            List<EventProduct> products = e.Products.Where(p => p.Type == productType && p.Quantity != null).ToList();
            if (products.Count > 0)
            {
                // make sure we have created the xml element correctly.
                List<string> xParts = kde.XPath.Split('/').ToList();
                while (xParts.Count > 1)
                {
                    string p = xParts.First();
                    xParts.RemoveAt(0);
                    if (x.Element(p) == null)
                    {
                        x.Add(new XElement(p));
                    }
                    x = x.Element(p) ?? throw new Exception("Failed to add xml element, p=" + p);
                }

                string xName = kde.XPath.Split('/').Last();
                XElement xQuantityList = new XElement(xName);
                foreach (EventProduct product in products)
                {
                    if (product.EPC != null && product.Quantity != null)
                    {
                        XElement xQuantity = new XElement("quantityElement",
                            new XElement("epcClass", product.EPC.ToString()),
                            new XElement("quantity", product.Quantity.Value));

                        if (product.Quantity.UoM.UNCode != "EA")
                        {
                            xQuantity.Add(new XElement("uom", product.Quantity.UoM.UNCode));
                        }

                        xQuantityList.Add(xQuantity);
                    }
                }
                x.Add(xQuantityList);
            }
        }

        private static void WriteParentID(EPCISMappingKDE kde, IEvent e, XElement x)
        {
            string xName = kde.XPath.Split('/').Last();
            EventProduct? parent = e.Products.FirstOrDefault(p => p.Type == EventProductType.Parent);
            if (parent?.EPC != null)
            {
                x.Add(new XElement(xName, parent.EPC.ToString()));
            }
        }

        private static XElement? WriteObject(XName xname, object? value, bool required=false)
        {
            XElement? xvalue = null;
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
            //            object? obj = kvp.Value.GetValue(value);
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
            //            object? obj = typeInfo.ExtensionKDEs.GetValue(value);
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
            //            object? obj = typeInfo.ExtensionAttributes.GetValue(value);
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

        private static string WriteObjectToString(object obj)
        {
            if (obj is DateTimeOffset)
            {
                DateTimeOffset dt = (DateTimeOffset)obj;
                return dt.ToString("O");
            }
            else if (obj is UOM)
            {
                UOM uom = (UOM)obj;
                return uom.UNCode;
            }
            else if (obj is bool)
            {
                bool b = (bool)obj;
                return b.ToString()?.ToLower() ?? string.Empty;
            }
            else if (obj is Country)
            {
                Country b = (Country)obj;
                return b.Abbreviation;
            }
            else
            {
                return obj.ToString() ?? string.Empty;
            }
        }
    }
}