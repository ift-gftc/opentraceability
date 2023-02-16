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
        public static Dictionary<Type, Dictionary<string, PropertyInfo>> ObjectPropertyMappings = new Dictionary<Type, Dictionary<string, PropertyInfo>>();
        static EPCISXmlMasterDataReader()
        {
            Action<Type> func = (t) =>
            {
                Dictionary<string, PropertyInfo> mappedProperties = new Dictionary<string, PropertyInfo>();
                foreach (PropertyInfo p in t.GetProperties())
                {
                    var att = p.GetCustomAttribute<OpenTraceabilityAttribute>();
                    if (att != null)
                    {
                        mappedProperties.Add(att.Name, p);
                    }
                }
                ObjectPropertyMappings.Add(t, mappedProperties);
            };

            func(typeof(Tradeitem));
            func(typeof(Location));
            func(typeof(TradingParty));
        }

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
            tradeitem.Type = type;

            // read the object
            ReadMasterDataObject(tradeitem, xTradeitem);
            doc.MasterData.Add(tradeitem);
        }

        private static void ReadLocation(EPCISBaseDocument doc, XElement xLocation, string type)
        {
            // read the GLN from the id
            string id = xLocation.Attribute("id")?.Value ?? string.Empty;
            Location loc = new Location();
            loc.GLN = new Models.Identifiers.GLN(id);
            loc.Type = type;

            // read the object
            ReadMasterDataObject(loc, xLocation);
            doc.MasterData.Add(loc);
        }

        private static void ReadTradingParty(EPCISBaseDocument doc, XElement xTradingParty, string type)
        {
            // read the PGLN from the id
            string id = xTradingParty.Attribute("id")?.Value ?? string.Empty;
            TradingParty tp = new TradingParty();
            tp.PGLN = new Models.Identifiers.PGLN(id);
            tp.Type = type;

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
            ele.Type = type;

            // read the object
            ReadMasterDataObject(ele, xVocabElement);
            doc.MasterData.Add(ele);
        }

        private static void ReadMasterDataObject(IVocabularyElement md, XElement xMasterData)
        {
            var mappedProperties = ObjectPropertyMappings[md.GetType()];

            // go through each attribute...
            foreach (XElement xeAtt in xMasterData.Elements("attribute")) 
            {
                string id = xeAtt.Attribute("id")?.Value ?? string.Empty;
                if (mappedProperties.ContainsKey(id))
                {
                    PropertyInfo pInfo = mappedProperties[id];
                    if (!TrySetValueType(xeAtt.Value, pInfo, md))
                    {
                        object value = ReadKDEObject(xeAtt, pInfo.PropertyType);
                        pInfo.SetValue(md, value);
                    }
                }
                else
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
