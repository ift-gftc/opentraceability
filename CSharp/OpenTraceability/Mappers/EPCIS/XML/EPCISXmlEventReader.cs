using Newtonsoft.Json;
using Newtonsoft.Json.Linq;
using OpenTraceability.Interfaces;
using OpenTraceability.Models.Events;
using OpenTraceability.Models.Events.KDEs;
using OpenTraceability.Models.Identifiers;
using OpenTraceability.Models.MasterData;
using OpenTraceability.Utility;
using OpenTraceability.Utility.Attributes;
using System;
using System.Collections;
using System.Collections.Generic;
using System.Globalization;
using System.Linq;
using System.Numerics;
using System.Reflection;
using System.Security.Cryptography.X509Certificates;
using System.Text;
using System.Threading.Tasks;
using System.Xml.Linq;
using System.Xml.XPath;

namespace OpenTraceability.Mappers.EPCIS.XML
{
    /// <summary>
    /// This class is used for reading an event from XML and converting it into an IEvent.
    /// </summary>
    public static class EPCISXmlEventReader
    {
    //    internal static Dictionary<Type, Dictionary<string, PropertyInfo>> ObjectPropertyMappings = new Dictionary<Type, Dictionary<string, PropertyInfo>>();
    //    static EPCISXmlEventReader()
    //    {
    //        Action<Type> func = (t) =>
    //        {
    //            Dictionary<string, PropertyInfo> mappedProperties = new Dictionary<string, PropertyInfo>();
    //            foreach (PropertyInfo p in t.GetProperties())
    //            {
    //                var att = p.GetCustomAttribute<OpenTraceabilityAttribute>();
    //                if (att != null)
    //                {
    //                    mappedProperties.Add(att.Name, p);
    //                }
    //            }
    //            ObjectPropertyMappings.Add(t, mappedProperties);
    //        };

    //        func(typeof(ObjectEvent));
    //        func(typeof(TransformationEvent));
    //        func(typeof(TransactionEvent));
    //        func(typeof(AggregationEvent));
    //        func(typeof(AssociationEvent));
    //    }

        // https://ref.gs1.org/standards/epcis/epcglobal-epcis-2_0.xsd
        public static IEvent ReadEvent(XElement xEvent, EPCISVersion epcisVersion)
        {
            IEvent? e = null;

            // profile the event
            e = CreateEventFromProfile(xEvent);

            // get the mapping information for the event

            // use the OpenTraceabilityXmlMapper to read the event from the XML
            e = (IEvent)OpenTraceabilityXmlMapper.FromXml(xEvent, e.GetType(), epcisVersion);

            //if (xEvent.Name == "ObjectEvent")
            //{
            //    kdes.AddRange(EPCISMappingKDE.MappingKDEs["ObjectEvent"].ToList());
            //}
            //else if (xEvent.Name == "TransformationEvent" || xEvent.Element("TransformationEvent") != null)
            //{
            //    if (xEvent.Name == "extension")
            //    {
            //        xEvent = xEvent.Element("TransformationEvent") ?? throw new Exception("Failed to get extension/TransformationEvent.");
            //    }
            //    kdes.AddRange(EPCISMappingKDE.MappingKDEs["TransformationEvent"].ToList());
            //}
            //else if (xEvent.Name == "TransactionEvent")
            //{
            //    kdes.AddRange(EPCISMappingKDE.MappingKDEs["TransactionEvent"].ToList());
            //}
            //else if (xEvent.Name == "AggregationEvent")
            //{
            //    kdes.AddRange(EPCISMappingKDE.MappingKDEs["AggregationEvent"].ToList());
            //}
            //else if (xEvent.Name == "AssociationEvent")
            //{
            //    kdes.AddRange(EPCISMappingKDE.MappingKDEs["AssociationEvent"].ToList());
            //}
            //else
            //{
            //    throw new Exception("Unrecognized event type = " + xEvent.Name);
            //}

            //kdes = kdes.Where(k => k.Version == null || k.Version == epcisVersion).ToList();

            //var propertyMappings = ObjectPropertyMappings[e.GetType()];

            //// go through each KDE in our list...
            //int i = 0;
            //foreach (XElement x in xEvent.Elements())
            //{
            //    bool readSomething = false;

            //    // move through our list of KDEs until we find that the current element matches it...
            //    while (i < kdes.Count)
            //    {
            //        // here we need to handle if we are processing a child element like baseExtension or extension
            //        if (kdes[i].XPath.Contains("/"))
            //        {
            //            string[] parts = kdes[i].XPath.Split("/");
            //            if (x.Name == parts[0])
            //            {
            //                while (x.Name == parts[0])
            //                {
            //                    // once we are here, we need to i++ until we leave this while loop
            //                    foreach (XElement xChild in x.Elements())
            //                    {
            //                        if (xChild.Name == parts[1])
            //                        {
            //                            // TODO: process the KDe and move to next KDE...
            //                            ReadKDE(kdes[i], e, xChild);
            //                            readSomething = true;
            //                        }
            //                    }
            //                    i++;
            //                    if (i < kdes.Count && kdes[i].XPath.Contains("/"))
            //                    {
            //                        parts = kdes[i].XPath.Split("/");
            //                    }
            //                    else
            //                    {
            //                        break;
            //                    }
            //                }
            //                break;
            //            }
            //            else
            //            {
            //                i++;
            //            }
            //        }
            //        // processing a standard root KDE...
            //        else if (x.Name == kdes[i].XPath)
            //        {
            //            // process the KDe and move to next KDE...
            //            ReadKDE(kdes[i], e, x);
            //            readSomething = true;

            //            i++;
            //            break;
            //        }
            //        // did not find a match, so it might not exist in the XML, so lets move to the next KDE...
            //        else
            //        {
            //            i++;
            //        }
            //    }

            //    if (readSomething) continue;

            //    // if we reached the end of our list of kdes... assume it is an extension KDE...
            //    if (i >= kdes.Count)
            //    {
            //        // see if this is from an object property mapping, otherwise add it to the extension KDEs...
            //        if (propertyMappings.ContainsKey(x.Name.ToString()))
            //        {
            //            var propertyMapping = propertyMappings[x.Name.ToString()];
            //            object o = ReadObject(x, propertyMapping.PropertyType);
            //            propertyMapping.SetValue(e, o);
            //        }
            //        else
            //        {
            //            IEventKDE kde = ReadKDE(x);
            //            e.AddKDE(kde);
            //        }
            //    }
            //}

            return e;
        }

        

        //private static void ReadKDE(EPCISMappingKDE kde, IEvent e, XElement x)
        //{
        //    try
        //    {
        //        if (kde.Property != null)
        //        {
        //            // if we are looking at a complex property on the C#, then drill down to it
        //            object obj = e;
        //            List<string> parts = kde.Property.Split(".").ToList();
        //            while (parts.Count > 1)
        //            {
        //                string pName = parts.First();
        //                PropertyInfo p = obj.GetType().GetProperty(pName) ?? throw new Exception($"Failed to find property on {obj.GetType().FullName} with name {pName}");
        //                object? o2 = p.GetValue(obj);
        //                if (o2 == null)
        //                {
        //                    o2 = Activator.CreateInstance(p.PropertyType) ?? throw new Exception("Failed to create instance of " + p.PropertyType.FullName);
        //                    p.SetValue(obj, o2);
        //                }
        //                obj = o2;
        //                parts.RemoveAt(0);
        //            }
        //            PropertyInfo pInfo = obj.GetType().GetProperty(parts.First()) ?? throw new Exception($"Failed to find property on {obj.GetType().FullName} with name {parts.First()}");

        //            try
        //            {
        //                switch (kde.Type)
        //                {
        //                    case "EventTimeZoneOffset": ReadEventTimeZoneOffset(kde, obj, x, pInfo); break;
        //                    case "DateTimeOffset": ReadDateTimeOffset(kde, obj, x, pInfo); break;
        //                    case "URI": ReadURI(kde, obj, x, pInfo); break;
        //                    case "Action": ReadAction(kde, obj, x, pInfo); break;
        //                    case "String": ReadString(kde, obj, x, pInfo); break;
        //                    case "GLN": ReadGLN(kde, obj, x, pInfo); break;
        //                    default:
        //                        {
        //                            object o = ReadObject(x, pInfo.PropertyType);
        //                            pInfo.SetValue(obj, o);
        //                            break;
        //                        }
        //                }
        //            }
        //            catch (Exception ex)
        //            {
        //                OTLogger.Error(ex);
        //                throw;
        //            }
        //        }
        //        else if (kde.Type == "ParentID")
        //        {
        //            ReadParentID(e, x);
        //        }
        //        else if (kde.Type == "EPCList")
        //        {
        //            string xname = kde.XPath.Split('/').Last();
        //            switch (xname)
        //            {
        //                case "epcList": ReadEPCList(e, x, EventProductType.Reference); break;
        //                case "outputEPCList": ReadEPCList(e, x, EventProductType.Output); break;
        //                case "inputEPCList": ReadEPCList(e, x, EventProductType.Input); break;
        //                case "childEPCs": ReadEPCList(e, x, EventProductType.Child); break;
        //                default: throw new Exception("Did not recognize epc list xpath. " + JsonConvert.SerializeObject(kde));
        //            }
        //        }
        //        else if (kde.Type == "QuantityList")
        //        {
        //            string xname = kde.XPath.Split('/').Last();
        //            switch (xname)
        //            {
        //                case "quantityList": ReadQuantityList(e, x, EventProductType.Reference); break;
        //                case "outputQuantityList": ReadQuantityList(e, x, EventProductType.Output); break;
        //                case "inputQuantityList": ReadQuantityList(e, x, EventProductType.Input); break;
        //                case "childQuantityList": ReadQuantityList(e, x, EventProductType.Child); break;
        //                default: throw new Exception("Did not recognize quantity list xpath. " + JsonConvert.SerializeObject(kde));
        //            }
        //        }
        //    }
        //    catch (Exception ex)
        //    {
        //        Exception exception = new Exception($"Failed to read the KDE.\nkde={JsonConvert.SerializeObject(kde)}\nx={x.ToString()}", ex);
        //        OTLogger.Error(exception);
        //        throw exception;
        //    }
        //}

        //private static void ReadEventTimeZoneOffset(EPCISMappingKDE kde, object e, XElement x, PropertyInfo pInfo)
        //{
        //    string hours = x.Value.Substring(1);
        //    TimeSpan ts = TimeSpan.Parse(hours);
        //    double dbl = ts.TotalHours;
        //    if (x.Value[0] == '-')
        //    {
        //        dbl = -dbl;
        //    }
        //    pInfo.SetValue(e, dbl);
        //}

        //private static void ReadDateTimeOffset(EPCISMappingKDE kde, object e, XElement x, PropertyInfo pInfo)
        //{
        //    DateTimeOffset? dt = x.Value.TryConvertToDateTimeOffset();
        //    pInfo.SetValue(e, dt);
        //}

        //private static void ReadURI(EPCISMappingKDE kde, object e, XElement x, PropertyInfo pInfo)
        //{
        //    Uri uri = new Uri(x.Value);
        //    pInfo.SetValue(e, uri);
        //}

        //private static void ReadAction(EPCISMappingKDE kde, object e, XElement x, PropertyInfo pInfo)
        //{
        //    EventAction action = Enum.Parse<EventAction>(x.Value);  
        //    pInfo.SetValue(e, action);
        //}

        //private static void ReadString(EPCISMappingKDE kde, object e, XElement x, PropertyInfo pInfo)
        //{
        //    pInfo.SetValue(e, x.Value);
        //}

        //private static void ReadGLN(EPCISMappingKDE kde, object e, XElement x, PropertyInfo pInfo)
        //{
        //    GLN gln = new GLN(x.Value);
        //    pInfo.SetValue(e, gln);
        //}

        //private static void ReadEPCList(IEvent e, XElement x, EventProductType productType)
        //{
        //    foreach (var xEPC in x.Elements("epc"))
        //    {
        //        EventProduct product = new EventProduct();
        //        product.Type = productType;
        //        product.EPC = new EPC(xEPC.Value);
        //        e.AddProduct(product);
        //    }
        //}

        //private static void ReadQuantityList(IEvent e, XElement x, EventProductType productType)
        //{
        //    foreach (var xQuantity in x.Elements("quantityElement"))
        //    {
        //        EventProduct product = new EventProduct();
        //        product.Type = productType;
        //        product.EPC = new EPC(xQuantity.Element("epcClass")?.Value ?? string.Empty);

        //        double quantity = double.Parse(xQuantity.Element("quantity")?.Value ?? string.Empty);
        //        string uom = xQuantity.Element("uom")?.Value ?? "EA";
        //        product.Quantity = new Measurement(quantity, uom);

        //        e.AddProduct(product);
        //    }
        //}

        //private static void ReadParentID(IEvent e, XElement x)
        //{
        //    EventProduct product = new EventProduct();
        //    product.EPC = new EPC(x.Value);
        //    product.Type = EventProductType.Parent;
        //    e.AddProduct(product);
        //}

        //private static object ReadObject(XElement x, Type t)
        //{
        //    object value = Activator.CreateInstance(t) ?? throw new Exception("Failed to create instance of type " + t.FullName);

        //    try
        //    {
        //        // if this is a list, then we will make a list of the objects...
        //        if (value is IList)
        //        {
        //            IList list = (IList)value;
        //            OpenTraceabilityAttribute? att = t.GetCustomAttribute<OpenTraceabilityAttribute>();
        //            if (att != null)
        //            {
        //                foreach (XElement xchild in x.Elements(att.Name))
        //                {
        //                    object childvalue = ReadObject(xchild, t.GenericTypeArguments.First());
        //                    list.Add(childvalue);
        //                }
        //            }
        //            else
        //            {
        //                foreach (XElement xchild in x.Elements())
        //                {
        //                    object childvalue = ReadObject(xchild, t.GenericTypeArguments.First());
        //                    list.Add(childvalue);
        //                }
        //            }
        //        }
        //        // else, try and parse the object...
        //        else
        //        {
        //            OTMappingTypeInformation typeInfo = OTMappingTypeInformation.GetTypeInfo(t);

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

        //            foreach (XAttribute xatt in x.Attributes())
        //            {
        //                if (typeInfo.XmlAttributes.Keys.ToList().Exists(x => x.Name.TrimStart('@') == xatt.Name))
        //                {
        //                    var kvp = typeInfo.XmlAttributes.FirstOrDefault(x => x.Key.Name.TrimStart('@') == xatt.Name);
        //                    string xchildname = kvp.Key.Name.ToString();
        //                    string? attValue = x.Attribute(xchildname.TrimStart('@'))?.Value;
        //                    if (!string.IsNullOrEmpty(attValue))
        //                    {
        //                        object o = ReadObjectFromString(attValue, kvp.Value.PropertyType);
        //                        kvp.Value.SetValue(value, o);
        //                    }
        //                }
        //                else if (extensionAttributes != null)
        //                {
        //                    IEventKDE kde = ReadKDE(xatt);
        //                    extensionAttributes.Add(kde);
        //                }
        //            }

        //            if (typeInfo.XmlAttributes.Keys.ToList().Exists(x => x.Name == "text()"))
        //            {
        //                var kvp = typeInfo.XmlAttributes.FirstOrDefault(x => x.Key.Name == "text()");
        //                string? eleText = x.Value;
        //                if (!string.IsNullOrWhiteSpace(eleText))
        //                {
        //                    object o = ReadObjectFromString(eleText, kvp.Value.PropertyType);
        //                    kvp.Value.SetValue(value, o);
        //                }
        //            }
        //            else
        //            {
        //                foreach (XElement xe in x.Elements())
        //                {
        //                    if (typeInfo.XmlAttributes.Keys.ToList().Exists(x => x.Name == xe.Name))
        //                    {
        //                        var kvp = typeInfo.XmlAttributes.FirstOrDefault(x => x.Key.Name == xe.Name);
        //                        string xchildname = kvp.Key.Name.ToString();
        //                        if (kvp.Value.GetCustomAttribute<OpenTraceabilityObjectAttribute>() != null)
        //                        {
        //                            XElement? xchild = x.Element(xchildname);
        //                            if (xchild != null)
        //                            {
        //                                object o = ReadObject(xchild, kvp.Value.PropertyType);
        //                                kvp.Value.SetValue(value, o);
        //                            }
        //                        }
        //                        else if (kvp.Value.GetCustomAttribute<OpenTraceabilityArrayAttribute>() != null)
        //                        {
        //                            if (x.Element(xchildname) != null)
        //                            {
        //                                IList list = (IList)(Activator.CreateInstance(kvp.Value.PropertyType) ?? throw new Exception("Failed to create instance of " + kvp.Value.PropertyType.FullName));
        //                                foreach (XElement xchild in x.Elements(xchildname))
        //                                {
        //                                    Type t2 = kvp.Value.PropertyType.GenericTypeArguments[0];
        //                                    if (t2 == typeof(Uri))
        //                                    {
        //                                        object o = ReadObjectFromString(xchild.Value, t2);
        //                                        list.Add(o);
        //                                    }
        //                                    else
        //                                    {
        //                                        object o = ReadObject(xchild, t2);
        //                                        list.Add(o);
        //                                    }
        //                                }
        //                                kvp.Value.SetValue(value, list);
        //                            }
        //                        }
        //                        else
        //                        {
        //                            string? eleText = x.Element(xchildname)?.Value;
        //                            if (!string.IsNullOrWhiteSpace(eleText))
        //                            {
        //                                object o = ReadObjectFromString(eleText, kvp.Value.PropertyType);
        //                                kvp.Value.SetValue(value, o);
        //                            }
        //                        }
        //                    }
        //                    else if (extensionKDEs != null)
        //                    {
        //                        IEventKDE kde = ReadKDE(xe);
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

        //private static object ReadObjectFromString(string value, Type t)
        //{
        //    if (t == typeof(DateTimeOffset) || t == typeof(DateTimeOffset?))
        //    {
        //        DateTimeOffset dt = value.TryConvertToDateTimeOffset() ?? throw new Exception("Failed to convert string to datetimeoffset where value = " + value);
        //        return dt;
        //    }
        //    else if (t == typeof(UOM))
        //    {
        //        UOM uom = UOM.LookUpFromUNCode(value);
        //        return uom;
        //    }
        //    else if (t == typeof(bool) || t == typeof(bool?))
        //    {
        //        bool v = bool.Parse(value);
        //        return v;
        //    }
        //    else if (t == typeof(double) || t == typeof(double?))
        //    {
        //        double v = double.Parse(value);
        //        return v;
        //    }
        //    else if (t == typeof(Uri))
        //    {
        //        Uri v = new Uri(value);
        //        return v;
        //    }
        //    else
        //    {
        //        return value;
        //    }
        //}

        //private static IEventKDE ReadKDE(XElement x)
        //{
        //    // we need to parse the xml into an event KDE here...

        //    // check if it is a registered KDE...
        //    IEventKDE? kde = IEventKDE.InitializeKDE(x.Name.NamespaceName, x.Name.LocalName);

        //    // if not, then check if the data type is specified and we recognize it
        //    if (kde == null)
        //    {
        //        XAttribute? xsiType = x.Attribute((XNamespace)Constants.XSI_NAMESPACE + "type");
        //        if (xsiType != null)
        //        {
        //            switch (xsiType.Value)
        //            {
        //                case "string": kde = new EventKDEString(x.Name.NamespaceName, x.Name.LocalName); break;
        //                case "boolean": kde = new EventKDEBoolean(x.Name.NamespaceName, x.Name.LocalName); break;
        //                case "number": kde = new EventKDEDouble(x.Name.NamespaceName, x.Name.LocalName); break;
        //            }
        //        }
        //    }

        //    // if not, check if it is a simple value or an object
        //    if (kde == null)
        //    {
        //        if (x.Elements().Count() > 0)
        //        {
        //            kde = new EventKDEObject(x.Name.NamespaceName, x.Name.LocalName);
        //        }
        //        // else if simple value, then we will consume it as a string
        //        else
        //        {
        //            kde = new EventKDEString(x.Name.NamespaceName, x.Name.LocalName);
        //        }
        //    }

        //    if (kde != null)
        //    {
        //        kde.SetFromXml(x);
        //    }
        //    else
        //    {
        //        throw new Exception("Failed to initialize KDE from XML = " + x.ToString());
        //    }

        //    return kde;
        //}

        //private static IEventKDE ReadKDE(XAttribute x)
        //{
        //    // we need to parse the xml into an event KDE here...

        //    // check if it is a registered KDE...
        //    IEventKDE? kde = IEventKDE.InitializeKDE(x.Name.NamespaceName, x.Name.LocalName);

        //    // if not, check if it is a simple value or an object
        //    if (kde == null)
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
