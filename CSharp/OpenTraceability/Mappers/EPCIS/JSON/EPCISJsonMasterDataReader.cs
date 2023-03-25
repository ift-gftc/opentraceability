using OpenTraceability.Interfaces;
using OpenTraceability.Models.Common;
using OpenTraceability.Models.Events.KDEs;
using OpenTraceability.Models.Events;
using OpenTraceability.Models.Identifiers;
using OpenTraceability.Models.MasterData;
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

namespace OpenTraceability.Mappers.EPCIS.JSON
{
    /// <summary>
    /// Used for reading master data from EPCIS JSON-LD file.
    /// </summary>
    public static class EPCISJsonMasterDataReader
    {
        public static void ReadMasterData(EPCISBaseDocument doc, JObject jMasterData)
        {
            JArray? jVocabList = jMasterData["vocabularyList"] as JArray;
            if (jVocabList != null)
            {
                foreach (JObject jVocabListItem in jVocabList)
                {
                    string? type = jVocabListItem["type"]?.ToString()?.ToLower();
                    if (type != null)
                    {
                        JArray? jVocabElementaryList = jVocabListItem["vocabularyElementList"] as JArray;
                        if (jVocabElementaryList != null)
                        {
                            foreach (JObject jVocabEle in jVocabElementaryList)
                            {
                                switch (type)
                                {
                                    case "urn:epcglobal:epcis:vtype:epcclass": ReadTradeitem(doc, jVocabEle, type); break;
                                    case "urn:epcglobal:epcis:vtype:location": ReadLocation(doc, jVocabEle, type); break;
                                    case "urn:epcglobal:epcis:vtype:party": ReadTradingParty(doc, jVocabEle, type); break;
                                    default: ReadUnknown(doc, jVocabEle, type); break;
                                }
                            }
                        }
                    }
                }
            }
        }

        private static void ReadTradeitem(EPCISBaseDocument doc, JObject xTradeitem, string type)
        {
            // read the GTIN from the id
            string id = xTradeitem["id"]?.ToString() ?? string.Empty;
            Tradeitem tradeitem = new Tradeitem();
            tradeitem.GTIN = new Models.Identifiers.GTIN(id);
            tradeitem.EPCISType = type;

            // read the object
            ReadMasterDataObject(tradeitem, xTradeitem);
            doc.MasterData.Add(tradeitem);
        }

        private static void ReadLocation(EPCISBaseDocument doc, JObject xLocation, string type)
        {
            // read the GLN from the id
            string id = xLocation["id"]?.ToString() ?? string.Empty;
            Type t = Setup.MasterDataTypes[type];
            Location loc = (Location)Activator.CreateInstance(t);
            loc.GLN = new Models.Identifiers.GLN(id);
            loc.EPCISType = type;

            // read the object
            ReadMasterDataObject(loc, xLocation);
            doc.MasterData.Add(loc);
        }

        private static void ReadTradingParty(EPCISBaseDocument doc, JObject xTradingParty, string type)
        {
            // read the PGLN from the id
            string id = xTradingParty["id"]?.ToString() ?? string.Empty;
            TradingParty tp = new TradingParty();
            tp.PGLN = new Models.Identifiers.PGLN(id);
            tp.EPCISType = type;

            // read the object
            ReadMasterDataObject(tp, xTradingParty);
            doc.MasterData.Add(tp);
        }

        private static void ReadUnknown(EPCISBaseDocument doc, JObject xVocabElement, string type)
        {
            // read the PGLN from the id
            string id = xVocabElement["id"]?.ToString() ?? string.Empty;
            VocabularyElement ele = new VocabularyElement();
            ele.ID = id;
            ele.EPCISType = type;

            // read the object
            ReadMasterDataObject(ele, xVocabElement);
            doc.MasterData.Add(ele);
        }

        private static void ReadMasterDataObject(IVocabularyElement md, JObject jMasterData, bool readKDEs = true)
        {
            var mappedProperties = OTMappingTypeInformation.GetMasterDataXmlTypeInfo(md.GetType());

            // work on expanded objects...
            // these are objects on the vocab element represented by one or more attributes in the EPCIS master data
            List<string> ignoreAttributes = new List<string>();
            foreach (var property in mappedProperties.Properties.Where(p => p.Name == string.Empty))
            {
                var subMappedProperties = OTMappingTypeInformation.GetMasterDataXmlTypeInfo(property.Property.PropertyType);
                bool setAttribute = false;
                object? subObject = Activator.CreateInstance(property.Property.PropertyType);
                if (subObject != null)
                {
                    foreach (JObject jAtt in jMasterData["attributes"] as JArray)
                    {
                        string id = jAtt["id"]?.ToString() ?? string.Empty;
                        var propMapping = subMappedProperties[id];
                        if (propMapping != null)
                        {
                            if (!TrySetValueType(jAtt["attribute"]?.ToString() ?? string.Empty, propMapping.Property, subObject))
                            {
                                object value = ReadKDEObject(jAtt, propMapping.Property.PropertyType);
                                propMapping.Property.SetValue(subObject, value);
                            }
                            setAttribute = true;
                            ignoreAttributes.Add(id);
                        }
                    }
                    if (setAttribute)
                    {
                        property.Property.SetValue(md, subObject);
                    }
                }
            }

            // go through each standard attribute...
            foreach (JObject jAtt in jMasterData["attributes"] as JArray)
            {
                string id = jAtt["id"]?.ToString() ?? string.Empty;

                if (ignoreAttributes.Contains(id))
                {
                    continue;
                }

                var propMapping = mappedProperties[id];
                if (propMapping != null)
                {
                    if (!TrySetValueType(jAtt["attribute"]?.ToString() ?? string.Empty, propMapping.Property, md))
                    {
                        object value = ReadKDEObject(jAtt, propMapping.Property.PropertyType);
                        propMapping.Property.SetValue(md, value);
                    }
                }
                else if (readKDEs)
                {
                    JToken? jAttValue = jAtt["attribute"];
                    if (jAttValue != null)
                    {
                        if (jAttValue is JObject)
                        {
                            // serialize into object kde...
                            IMasterDataKDE kdeObject = new MasterDataKDEObject(string.Empty, id);
                            kdeObject.SetFromGS1WebVocabJson(jAttValue);
                            md.KDEs.Add(kdeObject);
                        }
                        else
                        {
                            // serialize into string kde
                            IMasterDataKDE kdeString = new MasterDataKDEString(string.Empty, id);
                            kdeString.SetFromGS1WebVocabJson(jAttValue);
                            md.KDEs.Add(kdeString);
                        }
                    }
                }
            }
        }

        private static object ReadKDEObject(JToken j, Type t)
        {
            object value = Activator.CreateInstance(t) ?? throw new Exception("Failed to create instance of " + t.FullName);

            if (value is IList)
            {
                IList list = (IList)value;
                if (j is JArray)
                {
                    foreach (JObject xchild in (JArray)j)
                    {
                        object child = ReadKDEObject(xchild, t.GenericTypeArguments[0]);
                        list.Add(child);
                    }
                }
            }
            else
            {
                // go through each property...
                foreach (PropertyInfo p in t.GetProperties())
                {
                    OpenTraceabilityAttribute? xmlAtt = p.GetCustomAttribute<OpenTraceabilityAttribute>();
                    if (xmlAtt != null)
                    {
                        JToken? x = j[xmlAtt.Name];
                        if (x != null)
                        {
                            OpenTraceabilityObjectAttribute? objAtt = p.GetCustomAttribute<OpenTraceabilityObjectAttribute>();
                            if (objAtt != null)
                            {
                                object o = ReadKDEObject(x, p.PropertyType);
                            }
                            else if (!TrySetValueType(x.ToString(), p, value))
                            {
                                throw new Exception($"Failed to set value type while reading KDE object. property = {p.Name}, type = {t.FullName}, json = {x.ToString()}");
                            }
                        }
                    }
                }
            }

            return value;
        }

        private static bool TrySetValueType(string val, PropertyInfo p, object o)
        {
            if (p.PropertyType == typeof(string))
            {
                p.SetValue(o, val);
                return true;
            }
            else if (p.PropertyType == typeof(List<string>))
            {
                List<string>? cur = p.GetValue(o) as List<string>;
                if (cur == null)
                {
                    cur = new List<string>();
                    p.SetValue(o, cur);
                }
                cur.Add(val);
                return true;
            }
            else if (p.PropertyType == typeof(bool) || p.PropertyType == typeof(bool?))
            {
                bool v = bool.Parse(val);
                p.SetValue(o, v);
                return true;
            }
            else if (p.PropertyType == typeof(double) || p.PropertyType == typeof(double?))
            {
                double v = double.Parse(val);
                p.SetValue(o, v);
                return true;
            }
            else if (p.PropertyType == typeof(Uri))
            {
                Uri v = new Uri(val);
                p.SetValue(o, v);
                return true;
            }
            else if (p.PropertyType == typeof(List<LanguageString>))
            {
                List<LanguageString> l = new List<LanguageString>();
                l.Add(new LanguageString()
                {
                    Language = "en-US",
                    Value = val
                });
                p.SetValue(o, l);
                return true;
            }
            else if (p.PropertyType == typeof(Country))
            {
                Country v = Countries.Parse(val);
                p.SetValue(o, v);
                return true;
            }
            else if (p.PropertyType == typeof(PGLN))
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
}
