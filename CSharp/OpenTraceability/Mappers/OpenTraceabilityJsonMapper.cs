using OpenTraceability.Interfaces;
using OpenTraceability.Models.Events.KDEs;
using OpenTraceability.Models.Events;
using OpenTraceability.Models.Identifiers;
using OpenTraceability.Utility.Attributes;
using OpenTraceability.Utility;
using System;
using System.Collections;
using System.Collections.Generic;
using System.Linq;
using System.Reflection;
using System.Text;
using System.Threading.Tasks;
using System.Xml.Linq;
using Newtonsoft.Json.Linq;

namespace OpenTraceability.Mappers
{
    public static class OpenTraceabilityJsonMapper
    {
        public static JToken? ToJson(string xname, object? value, EPCISVersion version, bool required = false)
        {
            if (value != null)
            {
                JToken? x = new JObject(xname);
                JToken xvalue = x;

                // make sure we have created the xml element correctly.
                List<string> xParts = xname.SplitXPath();
                while (xParts.Count > 1)
                {
                    string p = xParts.First();
                    xParts.RemoveAt(0);
                    if (xvalue[p] == null)
                    {
                        xvalue[p] = new JObject();
                    }
                    xvalue = xvalue[p] ?? throw new Exception("Failed to add jobject, p=" + p);
                }
                xname = xParts.First();

                Type t = value.GetType();
                OTMappingTypeInformation typeInfo = OTMappingTypeInformation.GetTypeInfo(t);
                foreach (var property in typeInfo.Properties.Where(p => p.Version == null || p.Version == version))
                {
                    object? obj = property.Property.GetValue(value);
                    if (obj != null)
                    {
                        JToken xvaluepointer = xvalue;
                        xParts = property.Name.SplitXPath();
                        while (xParts.Count > 1)
                        {
                            string p = xParts.First();
                            xParts.RemoveAt(0);
                            if (xvaluepointer[p] == null)
                            {
                                xvaluepointer[p] = new JObject();
                            }
                            xvaluepointer = xvaluepointer[p] ?? throw new Exception("Failed to add xml element, p=" + p);
                        }
                        string xchildname = xParts.First();

                        if (xchildname.StartsWith("@"))
                        {
                            string? objStr = WriteObjectToString(obj);
                            if (!string.IsNullOrWhiteSpace(objStr))
                            {
                                xvaluepointer[xchildname] = objStr;
                            }
                        }
                        else if (xchildname == "text()")
                        {
                            string? objStr = WriteObjectToString(obj);
                            if (!string.IsNullOrWhiteSpace(objStr))
                            {
                                xvaluepointer["value"] = objStr;
                            }
                        }
                        else if (property.IsQuantityList)
                        {
                            List<EventProduct> products = (List<EventProduct>)obj;
                            products = products.Where(p => p.Quantity != null && p.Type == property.ProductType).ToList();
                            if (products.Count > 0)
                            {
                                JArray xQuantityList = new JArray();
                                foreach (var product in products)
                                {
                                    if (product.EPC != null && product.Quantity != null)
                                    {
                                        JObject xQuantity = new JObject();
                                        xQuantity["epcClass"] = product.EPC.ToString();
                                        xQuantity["quantity"] = product.Quantity.Value;
                                        if (product.Quantity.UoM.UNCode != "EA")
                                        {
                                            xQuantity["uom"] = product.Quantity.UoM.UNCode;
                                        }
                                        xQuantityList.Add(xQuantity);
                                    }
                                }
                                xvaluepointer[xchildname] = xQuantityList;
                            }
                        }
                        else if (property.IsEPCList)
                        {
                            List<EventProduct> products = (List<EventProduct>)obj;
                            products = products.Where(p => p.Quantity == null && p.Type == property.ProductType).ToList();
                            if (products.Count > 0 || property.Required)
                            {
                                JArray xEPCList = new JArray();
                                foreach (var product in products)
                                {
                                    if (product.EPC != null)
                                    {
                                        xEPCList.Add(product.EPC.ToString());
                                    }
                                }
                                xvaluepointer[xchildname] = xEPCList;
                            }
                        }
                        else if (property.IsArray)
                        {
                            IList list = (IList)obj;
                            JArray xlist = new JArray();
                            if (list.Count > 0 || property.Required)
                            {
                                if (property.ItemName != null)
                                {
                                    xvaluepointer[xchildname] = new JArray();
                                    xlist = xvaluepointer[xchildname] as JArray;
                                }
                            }

                            foreach (var o in list)
                            {
                                if (property.IsObject)
                                {
                                    JToken? xchild = ToJson(property.ItemName ?? xchildname, o, version, property.Required);
                                    if (xchild != null)
                                    {
                                        xlist.Add(xchild);
                                    }
                                }
                                else
                                {
                                    string? objStr = WriteObjectToString(o);
                                    if (!string.IsNullOrWhiteSpace(objStr))
                                    {
                                        XElement xchild = new XElement(property.ItemName ?? xchildname, objStr);
                                        xlist.Add(xchild);
                                    }
                                }
                            }
                        }
                        else if (property.IsObject)
                        {
                            JToken? xchild = ToJson(xchildname, obj, version, property.Required);
                            if (xchild != null)
                            {
                                xvaluepointer[xchildname] = xchild;
                            }
                        }
                        else
                        {
                            string? objStr = WriteObjectToString(obj);
                            if (!string.IsNullOrWhiteSpace(objStr))
                            {
                                xvaluepointer[xchildname] = objStr;
                            }
                        }
                    }
                }

                if (typeInfo.ExtensionKDEs != null)
                {
                    object? obj = typeInfo.ExtensionKDEs.GetValue(value);
                    if (obj != null && obj is IList<IEventKDE>)
                    {
                        IList<IEventKDE>? kdes = obj as IList<IEventKDE>;
                        if (kdes != null)
                        {
                            foreach (var kde in kdes)
                            {
                                JToken? xchild = kde.GetJson();
                                if (xchild != null)
                                {
                                    xvalue[kde.Name] = xchild;
                                }
                            }
                        }
                    }
                }

                if (typeInfo.ExtensionAttributes != null)
                {
                    object? obj = typeInfo.ExtensionAttributes.GetValue(value);
                    if (obj != null && obj is IList<IEventKDE>)
                    {
                        IList<IEventKDE>? kdes = obj as IList<IEventKDE>;
                        if (kdes != null)
                        {
                            foreach (IEventKDE kde in kdes)
                            {
                                JToken? xchild = kde.GetJson();
                                if (xchild != null)
                                {
                                    xvalue[kde.Name] = xchild;
                                }
                            }
                        }
                    }
                }

                return x;
            }
            //else if (required == true)
            //{
            //    XElement x = new XElement(xname);
            //    return x;
            //}
            else
            {
                return null;
            }
        }

        //public static T FromXml<T>(XElement x, EPCISVersion version)
        //{
        //    T o = (T)FromXml(x, typeof(T), version);
        //    return o;
        //}

        //public static object FromXml(XElement x, Type type, EPCISVersion version)
        //{
        //    object value = Activator.CreateInstance(type) ?? throw new Exception("Failed to create instance of type " + type.FullName);

        //    try
        //    {
        //        OTMappingTypeInformation mappingInfo = OTMappingTypeInformation.GetTypeInfo(type);

        //        // if this is a list, then we will make a list of the objects...
        //        if (value is IList)
        //        {
        //            IList list = (IList)value;
        //            OpenTraceabilityAttribute? att = type.GetCustomAttribute<OpenTraceabilityAttribute>();
        //            if (att != null)
        //            {
        //                foreach (XElement xchild in x.Elements(att.Name))
        //                {
        //                    object childvalue = FromXml(xchild, type.GenericTypeArguments.First(), version);
        //                    list.Add(childvalue);
        //                }
        //            }
        //            else
        //            {
        //                foreach (XElement xchild in x.Elements())
        //                {
        //                    object childvalue = FromXml(xchild, type.GenericTypeArguments.First(), version);
        //                    list.Add(childvalue);
        //                }
        //            }
        //        }
        //        // else, try and parse the object...
        //        else
        //        {
        //            OTMappingTypeInformation typeInfo = OTMappingTypeInformation.GetTypeInfo(type);

        //            List<IEventKDE>? extensionKDEs = null;
        //            List<IEventKDE>? extensionAttributes = null;

        //            if (typeInfo.ExtensionAttributes != null)
        //            {
        //                extensionAttributes = new List<IEventKDE>();
        //            }

        //            if (typeInfo.ExtensionKDEs != null)
        //            {
        //                extensionKDEs = new List<IEventKDE>();
        //            }

        //            OTMappingTypeInformationProperty? mappingProp;

        //            foreach (XAttribute xatt in x.Attributes())
        //            {
        //                mappingProp = typeInfo["@" + xatt.Name];
        //                if (mappingProp != null)
        //                {
        //                    string xchildname = mappingProp.Name.ToString();
        //                    string? attValue = x.Attribute(xchildname.TrimStart('@'))?.Value;
        //                    if (!string.IsNullOrEmpty(attValue))
        //                    {
        //                        object o = ReadObjectFromString(attValue, mappingProp.Property.PropertyType);
        //                        mappingProp.Property.SetValue(value, o);
        //                    }
        //                }
        //                else if (extensionAttributes != null)
        //                {
        //                    IEventKDE kde = ReadKDE(xatt);
        //                    extensionAttributes.Add(kde);
        //                }
        //            }

        //            mappingProp = typeInfo["text()"];
        //            if (mappingProp != null)
        //            {
        //                string? eleText = x.Value;
        //                if (!string.IsNullOrWhiteSpace(eleText))
        //                {
        //                    object o = ReadObjectFromString(eleText, mappingProp.Property.PropertyType);
        //                    mappingProp.Property.SetValue(value, o);
        //                }
        //            }
        //            else
        //            {
        //                foreach (XElement xc in x.Elements())
        //                {
        //                    XElement xchild = xc;

        //                    mappingProp = typeInfo[xchild.Name.ToString()];
        //                    if (mappingProp == null && typeInfo.Properties.Exists(p => p.Name.Split('/').First() == xchild.Name))
        //                    {
        //                        // see if we have a parent matching way...
        //                        foreach (var mp in typeInfo.Properties.Where(p => p.Name.Split('/').First() == xchild.Name))
        //                        {
        //                            XElement? xgrandchild = x.XPathSelectElement(mp.Name);
        //                            if (xgrandchild != null)
        //                            {
        //                                ReadPropertyMapping(mp, xgrandchild, value, version);
        //                            }
        //                        }
        //                    }
        //                    else if (mappingProp != null)
        //                    {
        //                        ReadPropertyMapping(mappingProp, xchild, value, version);
        //                    }
        //                    else if (extensionKDEs != null)
        //                    {
        //                        IEventKDE kde = ReadKDE(xchild);
        //                        extensionKDEs.Add(kde);
        //                    }
        //                }
        //            }

        //            if (typeInfo.ExtensionAttributes != null)
        //            {
        //                typeInfo.ExtensionAttributes.SetValue(value, extensionAttributes);
        //            }

        //            if (typeInfo.ExtensionKDEs != null)
        //            {
        //                typeInfo.ExtensionKDEs.SetValue(value, extensionKDEs);
        //            }
        //        }
        //    }
        //    catch (Exception ex)
        //    {
        //        OTLogger.Error(ex);
        //        throw;
        //    }

        //    return value;
        //}

        private static string? WriteObjectToString(object obj)
        {
            if (obj == null)
            {
                return null;
            }
            else if (obj is DateTimeOffset)
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
            else if (obj is TimeSpan)
            {
                TimeSpan timespan = (TimeSpan)obj;
                if (timespan.Ticks < 0)
                {
                    return "-" + timespan.Negate().TotalHours.ToString("#00") + ":" + timespan.Minutes.ToString("00");
                }
                else
                {
                    return "+" + timespan.TotalHours.ToString("#00") + ":" + timespan.Minutes.ToString("00");
                }
            }
            else
            {
                return obj.ToString() ?? string.Empty;
            }
        }

        //        private static void ReadPropertyMapping(OTMappingTypeInformationProperty mappingProp, XElement xchild, object value, EPCISVersion version)
        //        {
        //            if (mappingProp.IsQuantityList)
        //            {
        //                IEvent e = (IEvent)value;
        //                foreach (var xQuantity in xchild.Elements("quantityElement"))
        //                {
        //                    EventProduct product = new EventProduct();
        //                    product.Type = mappingProp.ProductType;
        //                    product.EPC = new EPC(xQuantity.Element("epcClass")?.Value ?? string.Empty);

        //                    double quantity = double.Parse(xQuantity.Element("quantity")?.Value ?? string.Empty);
        //                    string uom = xQuantity.Element("uom")?.Value ?? "EA";
        //                    product.Quantity = new Measurement(quantity, uom);

        //                    e.AddProduct(product);
        //                }
        //            }
        //            else if (mappingProp.IsEPCList)
        //            {
        //                IEvent e = (IEvent)value;
        //                foreach (var xEPC in xchild.Elements("epc"))
        //                {
        //                    EventProduct product = new EventProduct();
        //                    product.Type = mappingProp.ProductType;
        //                    product.EPC = new EPC(xEPC.Value);
        //                    e.AddProduct(product);
        //                }
        //            }
        //            else if (mappingProp.IsArray)
        //            {
        //                IList? list = mappingProp.Property.GetValue(value) as IList;
        //                if (list == null)
        //                {
        //                    list = (IList)(Activator.CreateInstance(mappingProp.Property.PropertyType)
        //                        ?? throw new Exception("Failed to create instance of " + mappingProp.Property.PropertyType.FullName));

        //                    mappingProp.Property.SetValue(value, list);
        //                }

        //                Type itemType = mappingProp.Property.PropertyType.GenericTypeArguments[0];
        //                if (mappingProp.ItemName != null)
        //                {
        //                    foreach (XElement xitem in xchild.Elements(mappingProp.ItemName))
        //                    {
        //                        if (mappingProp.IsObject)
        //                        {
        //                            object o = FromXml(xitem, itemType, version);
        //                            list.Add(o);
        //                        }
        //                        else
        //                        {
        //                            object o = ReadObjectFromString(xitem.Value, itemType);
        //                            list.Add(o);
        //                        }
        //                    }
        //                }
        //                else
        //                {
        //                    if (mappingProp.IsObject)
        //                    {
        //                        object o = FromXml(xchild, itemType, version);
        //                        list.Add(o);
        //                    }
        //                    else
        //                    {
        //                        object o = ReadObjectFromString(xchild.Value, itemType);
        //                        list.Add(o);
        //                    }
        //                }
        //            }
        //            else if (mappingProp.IsObject)
        //            {
        //                object o = FromXml(xchild, mappingProp.Property.PropertyType, version);
        //                mappingProp.Property.SetValue(value, o);
        //            }
        //            else
        //            {
        //                string? eleText = xchild.Value;
        //                if (!string.IsNullOrWhiteSpace(eleText))
        //                {
        //                    object o = ReadObjectFromString(eleText, mappingProp.Property.PropertyType);
        //                    mappingProp.Property.SetValue(value, o);
        //                }
        //            }
        //        }

        //        private static object ReadObjectFromString(string value, Type t)
        //        {
        //            if (t == typeof(DateTimeOffset) || t == typeof(DateTimeOffset?))
        //            {
        //                DateTimeOffset dt = value.TryConvertToDateTimeOffset() ?? throw new Exception("Failed to convert string to datetimeoffset where value = " + value);
        //                return dt;
        //            }
        //            else if (t == typeof(UOM))
        //            {
        //                UOM uom = UOM.LookUpFromUNCode(value);
        //                return uom;
        //            }
        //            else if (t == typeof(bool) || t == typeof(bool?))
        //            {
        //                bool v = bool.Parse(value);
        //                return v;
        //            }
        //            else if (t == typeof(double) || t == typeof(double?))
        //            {
        //                double v = double.Parse(value);
        //                return v;
        //            }
        //            else if (t == typeof(Uri))
        //            {
        //                Uri v = new Uri(value);
        //                return v;
        //            }
        //            else if (t == typeof(TimeSpan) || t == typeof(TimeSpan?))
        //            {
        //                if (value.StartsWith("+")) value = value.Substring(1);
        //                TimeSpan ts = TimeSpan.Parse(value);
        //                return ts;
        //            }
        //            else if (t == typeof(EventAction) || t == typeof(EventAction?))
        //            {
        //                EventAction action = Enum.Parse<EventAction>(value);
        //                return action;
        //            }
        //            else if (t == typeof(PGLN))
        //            {
        //                PGLN pgln = new PGLN(value);
        //                return pgln;
        //            }
        //            else if (t == typeof(GLN))
        //            {
        //                GLN gln = new GLN(value);
        //                return gln;
        //            }
        //            else if (t == typeof(EPC))
        //            {
        //                EPC epc = new EPC(value);
        //                return epc;
        //            }
        //            else if (t == typeof(Country))
        //            {
        //                Country c = Countries.Parse(value);
        //                return c;
        //            }
        //            else
        //            {
        //                return value;
        //            }
        //        }

        //        private static IEventKDE ReadKDE(XElement x)
        //        {
        //            we need to parse the xml into an event KDE here...

        //             check if it is a registered KDE...
        //            IEventKDE? kde = IEventKDE.InitializeKDE(x.Name.NamespaceName, x.Name.LocalName);

        //             if not, then check if the data type is specified and we recognize it
        //            if (kde == null)
        //            {
        //                XAttribute? xsiType = x.Attribute((XNamespace)Constants.XSI_NAMESPACE + "type");
        //                if (xsiType != null)
        //                {
        //                    switch (xsiType.Value)
        //                    {
        //                        case "string": kde = new EventKDEString(x.Name.NamespaceName, x.Name.LocalName); break;
        //                        case "boolean": kde = new EventKDEBoolean(x.Name.NamespaceName, x.Name.LocalName); break;
        //                        case "number": kde = new EventKDEDouble(x.Name.NamespaceName, x.Name.LocalName); break;
        //                    }
        //}
        //            }

        //             if not, check if it is a simple value or an object
        //            if (kde == null)
        //{
        //    if (x.Elements().Count() > 0)
        //    {
        //        kde = new EventKDEObject(x.Name.NamespaceName, x.Name.LocalName);
        //    }
        //    else if simple value, then we will consume it as a string
        //                else
        //    {
        //        kde = new EventKDEString(x.Name.NamespaceName, x.Name.LocalName);
        //    }
        //}

        //if (kde != null)
        //{
        //    kde.SetFromXml(x);
        //}
        //else
        //{
        //    throw new Exception("Failed to initialize KDE from XML = " + x.ToString());
        //}

        //return kde;
        //        }

        //        private static IEventKDE ReadKDE(XAttribute x)
        //{
        //    we need to parse the xml into an event KDE here...

        //             check if it is a registered KDE...
        //            IEventKDE? kde = IEventKDE.InitializeKDE(x.Name.NamespaceName, x.Name.LocalName);

        //    if not, check if it is a simple value or an object
        //            if (kde == null)
        //    {
        //        kde = new EventKDEString(x.Name.NamespaceName, x.Name.LocalName);
        //    }

        //    if (kde != null)
        //    {
        //        XElement xe = new XElement(x.Name, x.Value);
        //        kde.SetFromXml(xe);
        //    }
        //    else
        //    {
        //        throw new Exception("Failed to initialize KDE from XML Attribute = " + x.ToString());
        //    }

        //    return kde;
        //}
    }
}
