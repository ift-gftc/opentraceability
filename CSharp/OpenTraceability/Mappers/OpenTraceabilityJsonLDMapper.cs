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
using Newtonsoft.Json;
using OpenTraceability.Models.Common;

namespace OpenTraceability.Mappers
{
    public static class OpenTraceabilityJsonLDMapper
    {
        /// <summary>
        /// Converts an object into JSON.
        /// </summary>
        public static JToken ToJson(object value, Dictionary<string, string> namespacesReversed, bool required = false)
        {
            try
            {
                if (value != null)
                {
                    JToken json = new JObject();
                    JToken jpointer = json;

                    Type t = value.GetType();
                    OTMappingTypeInformation typeInfo = OTMappingTypeInformation.GetJsonTypeInfo(t);
                    foreach (var property in typeInfo.Properties.Where(p => p.Version == null || p.Version == EPCISVersion.V2))
                    {
                        object obj = property.Property.GetValue(value);
                        if (obj != null)
                        {
                            JToken jvaluepointer = jpointer;
                            string xchildname = property.Name;

                            if (property.IsQuantityList)
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
                                    jvaluepointer[xchildname] = xQuantityList;
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
                                    jvaluepointer[xchildname] = xEPCList;
                                }
                            }
                            else if (property.IsArray)
                            {
                                IList list = (IList)obj;
                                JArray xlist = new JArray();

                                if (list.Count > 0 || property.Required == true)
                                {
                                    if (property.IsRepeating && list.Count == 1)
                                    {
                                        JToken jt = WriteObjectToJToken(list[0]);
                                        if (jt != null)
                                        {
                                            jvaluepointer[xchildname] = jt;
                                        }
                                    }
                                    else
                                    {
                                        foreach (var o in list)
                                        {
                                            if (property.IsObject)
                                            {
                                                JToken xchild = ToJson(o, namespacesReversed, property.Required);
                                                if (xchild != null)
                                                {
                                                    xlist.Add(xchild);
                                                }
                                            }
                                            else
                                            {
                                                JToken jt = WriteObjectToJToken(o);
                                                if (jt != null)
                                                {
                                                    xlist.Add(jt);
                                                }
                                            }
                                        }

                                        jvaluepointer[xchildname] = xlist;
                                    }
                                }
                            }
                            else if (property.IsObject)
                            {
                                JToken xchild = ToJson(obj, namespacesReversed, property.Required);
                                if (xchild != null)
                                {
                                    jvaluepointer[xchildname] = xchild;
                                }
                            }
                            else
                            {
                                JToken jt = WriteObjectToJToken(obj);
                                if (jt != null)
                                {
                                    jvaluepointer[xchildname] = jt;
                                }
                            }
                        }
                    }

                    if (typeInfo.ExtensionKDEs != null)
                    {
                        object obj = typeInfo.ExtensionKDEs.GetValue(value);
                        if (obj != null && obj is IList<IEventKDE>)
                        {
                            IList<IEventKDE> kdes = obj as IList<IEventKDE>;
                            if (kdes != null)
                            {
                                foreach (var kde in kdes)
                                {
                                    JToken xchild = kde.GetJson();
                                    if (xchild != null)
                                    {
                                        string name = kde.Name;
                                        if (!string.IsNullOrWhiteSpace(kde.Namespace))
                                        {
                                            if (!namespacesReversed.ContainsKey(kde.Namespace))
                                            {
                                                throw new Exception($"The namespace {kde.Namespace} is not recognized in the EPCIS Document / EPCIS Query Document.");
                                            }
                                            name = namespacesReversed[kde.Namespace] + ":" + name;
                                        }

                                        jpointer[name] = xchild;
                                    }
                                }
                            }
                        }
                    }

                    if (typeInfo.ExtensionAttributes != null)
                    {
                        object obj = typeInfo.ExtensionAttributes.GetValue(value);
                        if (obj != null && obj is IList<IEventKDE>)
                        {
                            IList<IEventKDE> kdes = obj as IList<IEventKDE>;
                            if (kdes != null)
                            {
                                foreach (IEventKDE kde in kdes)
                                {
                                    JToken xchild = kde.GetJson();
                                    if (xchild != null)
                                    {
                                        string name = kde.Name;
                                        if (kde.Namespace != null)
                                        {
                                            if (!namespacesReversed.ContainsKey(kde.Namespace))
                                            {
                                                throw new Exception($"The namespace {kde.Namespace} is not recognized in the EPCIS Document / EPCIS Query Document.");
                                            }
                                            name = namespacesReversed[kde.Namespace] + ":" + name;
                                        }

                                        jpointer[name] = xchild;
                                    }
                                }
                            }
                        }
                    }

                    return json;
                }
                else
                {
                    return null;
                }
            }
            catch (Exception ex)
            {
                Exception e = new Exception($"Failed to parse json. value={value}", ex);
                OTLogger.Error(e);
                throw e;
            }
        }

        /// <summary>
        /// Converts a JSON object into the generic type specified.
        /// </summary>
        public static T FromJson<T>(JToken json, Dictionary<string, string> namespaces)
        {
            T o = (T)FromJson(json, typeof(T), namespaces);
            return o;
        }

        /// <summary>
        /// Converts a JSON object into the type specified.
        /// </summary>
        public static object FromJson(JToken json, Type type, Dictionary<string, string> namespaces)
        {
            object value = Activator.CreateInstance(type) ?? throw new Exception("Failed to create instance of type " + type.FullName);

            try
            {
                OTMappingTypeInformation typeInfo = OTMappingTypeInformation.GetJsonTypeInfo(type);

                List<IEventKDE> extensionKDEs = null;
                List<IEventKDE> extensionAttributes = null;

                if (typeInfo.ExtensionAttributes != null)
                {
                    extensionAttributes = new List<IEventKDE>();
                }

                if (typeInfo.ExtensionKDEs != null)
                {
                    extensionKDEs = new List<IEventKDE>();
                }

                OTMappingTypeInformationProperty mappingProp;

                JObject jobj = json as JObject;
                if (jobj != null)
                {
                    foreach (JProperty jprop in jobj.Properties())
                    {
                        mappingProp = typeInfo[jprop.Name];

                        if (mappingProp != null && mappingProp.Property.SetMethod == null)
                        {
                            continue;
                        }

                        JToken jchild = jobj[jprop.Name];
                        if (jchild != null)
                        {
                            if (mappingProp != null)
                            {
                                ReadPropertyMapping(mappingProp, jchild, value, namespaces);
                            }
                            else if (extensionKDEs != null)
                            {
                                IEventKDE kde = ReadKDE(jprop.Name, jchild, namespaces);
                                extensionKDEs.Add(kde);
                            }
                            else if (extensionAttributes != null)
                            {
                                IEventKDE kde = ReadKDE(jprop.Name, jchild, namespaces);
                                extensionAttributes.Add(kde);
                            }
                        }
                    }
                }

                if (typeInfo.ExtensionAttributes != null)
                {
                    typeInfo.ExtensionAttributes.SetValue(value, extensionAttributes);
                }

                if (typeInfo.ExtensionKDEs != null)
                {
                    typeInfo.ExtensionKDEs.SetValue(value, extensionKDEs);
                }
            }
            catch (Exception ex)
            {
                OTLogger.Error(ex);
                throw;
            }

            return value;
        }

        private static JToken WriteObjectToJToken(object obj)
        {
            if (obj == null)
            {
                return null;
            }
            else if (obj is List<LanguageString>)
            {
                string json = JsonConvert.SerializeObject(obj);
                return JArray.Parse(json);
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
            else if (obj is double)
            {
                return JToken.FromObject(obj);
            }
            else if (obj is bool)
            {
                return JToken.FromObject(obj);
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

        private static void ReadPropertyMapping(OTMappingTypeInformationProperty mappingProp, JToken json, object value, Dictionary<string, string> namespaces)
        {
            if (mappingProp.IsQuantityList)
            {
                IEvent e = (IEvent)value;
                JArray jQuantityList = json as JArray;
                if (jQuantityList != null)
                {
                    foreach (JObject jQuantity in jQuantityList)
                    {
                        EPC epc = new EPC(jQuantity["epcClass"]?.Value<string>() ?? string.Empty);

                        EventProduct product = new EventProduct(epc);
                        product.Type = mappingProp.ProductType;

                        double quantity = jQuantity.Value<double>("quantity");
                        string uom = jQuantity.Value<string>("uom") ?? "EA";
                        product.Quantity = new Measurement(quantity, uom);

                        e.AddProduct(product);
                    }
                }
            }
            else if (mappingProp.IsEPCList)
            {
                IEvent e = (IEvent)value;
                JArray jEPCList = json as JArray;
                if (jEPCList != null)
                {
                    foreach (JToken jEPC in jEPCList)
                    {
                        EPC epc = new EPC(jEPC.ToString());
                        EventProduct product = new EventProduct(epc);
                        product.Type = mappingProp.ProductType;
                        e.AddProduct(product);
                    }
                }
            }
            else if (mappingProp.IsArray)
            {
                IList list = mappingProp.Property.GetValue(value) as IList;
                if (list == null)
                {
                    list = (IList)(Activator.CreateInstance(mappingProp.Property.PropertyType)
                        ?? throw new Exception("Failed to create instance of " + mappingProp.Property.PropertyType.FullName));

                    mappingProp.Property.SetValue(value, list);
                }

                Type itemType = list.GetType().GenericTypeArguments[0];

                if (mappingProp.IsRepeating && !(json is JArray))
                {
                    string v = json.ToString();
                    if (!string.IsNullOrWhiteSpace(v))
                    {
                        object o = ReadObjectFromString(v, itemType);
                        list.Add(o);
                    }
                }
                else
                {
                    JArray jArr = json as JArray;
                    if (jArr != null)
                    {
                        foreach (JToken j in jArr)
                        {
                            if (mappingProp.IsObject)
                            {
                                object o = FromJson(j, itemType, namespaces);
                                list.Add(o);
                            }
                            else
                            {
                                object o = ReadObjectFromString(j.ToString(), itemType);
                                list.Add(o);
                            }
                        }
                    }
                }
            }
            else if (mappingProp.IsObject)
            {
                object o = FromJson(json, mappingProp.Property.PropertyType, namespaces);
                mappingProp.Property.SetValue(value, o);
            }
            else if (mappingProp.Property.PropertyType == typeof(List<LanguageString>))
            {
                List<LanguageString> languageStrings = JsonConvert.DeserializeObject<List<LanguageString>>(json.ToString());
                if (languageStrings != null)
                {
                    mappingProp.Property.SetValue(value, languageStrings);
                }
            }
            else
            {
                string v = json.ToString();
                if (!string.IsNullOrWhiteSpace(v))
                {
                    object o = ReadObjectFromString(v, mappingProp.Property.PropertyType);
                    mappingProp.Property.SetValue(value, o);
                }
            }
        }

        private static object ReadObjectFromString(string value, Type t)
        {
            try
            {
                if (t == typeof(DateTimeOffset) || t == typeof(DateTimeOffset?))
                {
                    DateTimeOffset dt = value.TryConvertToDateTimeOffset() ?? throw new Exception("Failed to convert string to datetimeoffset where value = " + value);
                    return dt;
                }
                else if (t == typeof(UOM))
                {
                    UOM uom = UOM.LookUpFromUNCode(value);
                    return uom;
                }
                else if (t == typeof(bool) || t == typeof(bool?))
                {
                    bool v = bool.Parse(value);
                    return v;
                }
                else if (t == typeof(double) || t == typeof(double?))
                {
                    double v = double.Parse(value);
                    return v;
                }
                else if (t == typeof(Uri))
                {
                    Uri v = new Uri(value);
                    return v;
                }
                else if (t == typeof(TimeSpan) || t == typeof(TimeSpan?))
                {
                    if (value.StartsWith("+")) value = value.Substring(1);
                    TimeSpan ts = TimeSpan.Parse(value);
                    return ts;
                }
                else if (t == typeof(EventAction) || t == typeof(EventAction?))
                {
                    EventAction action = (EventAction)Enum.Parse(typeof(EventAction), value);
                    return action;
                }
                else if (t == typeof(PGLN))
                {
                    PGLN pgln = new PGLN(value);
                    return pgln;
                }
                else if (t == typeof(GLN))
                {
                    GLN gln = new GLN(value);
                    return gln;
                }
                else if (t == typeof(GTIN))
                {
                    GTIN gtin = new GTIN(value);
                    return gtin;
                }
                else if (t == typeof(EPC))
                {
                    EPC epc = new EPC(value);
                    return epc;
                }
                else if (t == typeof(Country))
                {
                    Country c = Countries.Parse(value);
                    return c;
                }
                else
                {
                    return value;
                }
            }
            catch (Exception ex)
            {
                Exception e = new Exception($"Failed to convert string into object. value={value} and t={t}", ex);
                OTLogger.Error(e);
                throw e;
            }
        }

        private static IEventKDE ReadKDE(string name, JToken json, Dictionary<string, string> namespaces)
        {
            IEventKDE kde = null;

            //if not, check if it is a simple value or an object
            if (kde == null)
            {
                string ns = string.Empty;
                if (name.Contains(":"))
                {
                    ns = name.Split(':').First();
                    name = name.Split(':').Last();

                    if (!namespaces.ContainsKey(ns))
                    {
                        throw new Exception("The KDE has a namespace prefix, but there is no such namespace in the dictionary. " + ns);
                    }
                    ns = namespaces[ns];
                }

                if (json is JObject || json is JArray)
                {
                    kde = new EventKDEObject(ns, name);
                }
                //else if simple value, then we will consume it as a string
                else
                {
                    kde = new EventKDEString(ns, name);
                }
            }

            if (kde != null)
            {
                kde.SetFromJson(json);
            }
            else
            {
                throw new Exception("Failed to initialize KDE from JSON = " + json.ToString());
            }

            return kde;
        }
    }
}