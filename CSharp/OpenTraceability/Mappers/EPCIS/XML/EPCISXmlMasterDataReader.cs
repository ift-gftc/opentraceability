using Newtonsoft.Json.Linq;
using OpenTraceability.Interfaces;
using OpenTraceability.Models.Common;
using OpenTraceability.Models.Events;
using OpenTraceability.Models.Events.KDEs;
using OpenTraceability.Models.Identifiers;
using OpenTraceability.Models.MasterData;
using OpenTraceability.Utility;
using OpenTraceability.Utility.Attributes;
using System;
using System.Collections;
using System.Collections.Generic;
using System.Linq;
using System.Net.NetworkInformation;
using System.Reflection;
using System.Text;
using System.Threading.Tasks;
using System.Xml.Linq;

namespace OpenTraceability.Mappers.EPCIS.XML
{
    public static class EPCISXmlMasterDataReader
    {
        public static void ReadMasterData(EPCISBaseDocument doc, XElement xMasterData)
        {
            //<VocabularyList>
            //        <Vocabulary type="urn:epcglobal:epcis:vtype:EPCClass">
            //        <Vocabulary type="urn:epcglobal:epcis:vtype:Location">
            //        <Vocabulary type="urn:epcglobal:epcis:vtype:Party">

            XElement? xVocabList = xMasterData.Element("VocabularyList");
            if (xVocabList != null)
            {
                foreach (XElement xVocab in xVocabList.Elements())
                {
                    string? type = xVocab.Attribute("type")?.Value.ToLower();
                    if (type != null)
                    {
                        XElement? xVocabElementaryList = xVocab.Element("VocabularyElementList");
                        if (xVocabElementaryList != null)
                        {
                            foreach (XElement xVocabElement in xVocabElementaryList.Elements())
                            {
                                switch (type)
                                {
                                    case "urn:epcglobal:epcis:vtype:epcclass": ReadTradeitem(doc, xVocabElement, type); break;
                                    case "urn:epcglobal:epcis:vtype:location": ReadLocation(doc, xVocabElement, type); break;
                                    case "urn:epcglobal:epcis:vtype:party": ReadTradingParty(doc, xVocabElement, type); break;
                                    default: ReadUnknown(doc, xVocabElement, type); break;
                                }
                            }
                        }
                    }
                }
            }
        }

        private static void ReadTradeitem(EPCISBaseDocument doc, XElement xTradeitem, string type)
        {
            // read the GTIN from the id
            string id = xTradeitem.Attribute("id")?.Value ?? string.Empty;
            Tradeitem tradeitem = new Tradeitem();
            tradeitem.GTIN = new Models.Identifiers.GTIN(id);
            tradeitem.EPCISType = type;

            // read the object
            ReadMasterDataObject(tradeitem, xTradeitem);
            doc.MasterData.Add(tradeitem);
        }

        private static void ReadLocation(EPCISBaseDocument doc, XElement xLocation, string type)
        {
            // read the GLN from the id
            string id = xLocation.Attribute("id")?.Value ?? string.Empty;
            Type t = Setup.MasterDataTypes[type];
            if (Activator.CreateInstance(t) is not Location loc)
            {
                throw new Exception($"Failed to create instance of Location from type {t}");
            }
            else
            {
                loc.GLN = new Models.Identifiers.GLN(id);
                loc.EPCISType = type;

                // read the object
                ReadMasterDataObject(loc, xLocation);
                doc.MasterData.Add(loc);
            }
        }

        private static void ReadTradingParty(EPCISBaseDocument doc, XElement xTradingParty, string type)
        {
            // read the PGLN from the id
            string id = xTradingParty.Attribute("id")?.Value ?? string.Empty;
            TradingParty tp = new TradingParty();
            tp.PGLN = new Models.Identifiers.PGLN(id);
            tp.EPCISType = type;

            // read the object
            ReadMasterDataObject(tp, xTradingParty);
            doc.MasterData.Add(tp);
        }

        private static void ReadUnknown(EPCISBaseDocument doc, XElement xVocabElement, string type)
        {
            // read the PGLN from the id
            string id = xVocabElement.Attribute("id")?.Value ?? string.Empty;
            VocabularyElement ele = new VocabularyElement();
            ele.ID = id;
            ele.EPCISType = type;

            // read the object
            ReadMasterDataObject(ele, xVocabElement);
            doc.MasterData.Add(ele);
        }

        private static void ReadMasterDataObject(IVocabularyElement md, XElement xMasterData, bool readKDEs = true)
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
                    foreach (XElement xeAtt in xMasterData.Elements("attribute"))
                    {
                        string id = xeAtt.Attribute("id")?.Value ?? string.Empty;
                        var propMapping = subMappedProperties[id];
                        if (propMapping != null)
                        {
                            if (!TrySetValueType(xeAtt.Value, propMapping.Property, subObject))
                            {
                                object value = ReadKDEObject(xeAtt, propMapping.Property.PropertyType);
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
            foreach (XElement xeAtt in xMasterData.Elements("attribute"))
            {
                string id = xeAtt.Attribute("id")?.Value ?? string.Empty;

                if (ignoreAttributes.Contains(id))
                {
                    continue;
                }

                var propMapping = mappedProperties[id];
                if (propMapping != null)
                {
                    if (!TrySetValueType(xeAtt.Value, propMapping.Property, md))
                    {
                        object value = ReadKDEObject(xeAtt, propMapping.Property.PropertyType);
                        propMapping.Property.SetValue(md, value);
                    }
                }
                else if (readKDEs)
                {
                    if (xeAtt.HasElements)
                    {
                        // serialize into object kde...
                        IMasterDataKDE kdeObject = new MasterDataKDEObject(string.Empty, id);
                        kdeObject.SetFromEPCISXml(xeAtt);
                        md.KDEs.Add(kdeObject);
                    }
                    else
                    {
                        // serialize into string kde
                        IMasterDataKDE kdeString = new MasterDataKDEString(string.Empty, id);
                        kdeString.SetFromEPCISXml(xeAtt);
                        md.KDEs.Add(kdeString);
                    }
                }
            }
        }

        private static object ReadKDEObject(XElement xeAtt, Type t)
        {
            object value = Activator.CreateInstance(t) ?? throw new Exception("Failed to create instance of " + t.FullName);

            if (value is IList)
            {
                IList list = (IList)value;
                foreach (XElement xchild in xeAtt.Elements())
                {
                    object child = ReadKDEObject(xchild, t.GenericTypeArguments[0]);
                    list.Add(child);
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
                        XElement? x = xeAtt.Element(xmlAtt.Name);
                        if (x != null)
                        {
                            OpenTraceabilityObjectAttribute? objAtt = p.GetCustomAttribute<OpenTraceabilityObjectAttribute>();
                            if (objAtt != null)
                            {
                                object o = ReadKDEObject(x, p.PropertyType);
                            }
                            else if (!TrySetValueType(x.Value, p, value))
                            {
                                throw new Exception($"Failed to set value type while reading KDE object. property = {p.Name}, type = {t.FullName}, xml = {x.ToString()}");
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