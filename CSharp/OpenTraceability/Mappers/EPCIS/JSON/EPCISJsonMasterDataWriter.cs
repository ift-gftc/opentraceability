using Newtonsoft.Json.Linq;
using OpenTraceability.Interfaces;
using OpenTraceability.Models.Common;
using OpenTraceability.Models.Events;
using OpenTraceability.Utility.Attributes;
using System;
using System.Collections;
using System.Collections.Generic;
using System.Linq;
using System.Reflection;
using System.Text;
using System.Threading.Tasks;

namespace OpenTraceability.Mappers.EPCIS.JSON
{
    /// <summary>
    /// Used for writing master data into an EPCIS JSON-LD file.
    /// </summary>
    public static class EPCISJsonMasterDataWriter
    {
        public static void WriteMasterData(JObject jDoc, EPCISBaseDocument doc)
        {
            if (doc.MasterData.Count > 0)
            {
                JObject xEPCISHeader = jDoc["epcisHeader"] as JObject;
                if (xEPCISHeader == null)
                {
                    jDoc["epcisHeader"] = new JObject(new JProperty("epcisMasterData", new JObject(new JProperty("vocabularyList", new JArray()))));
                }
                else
                {
                    xEPCISHeader["epcisMasterData"] = new JObject(new JProperty("vocabularyList", new JArray()));
                }
                JArray jVocabList = jDoc["epcisHeader"]?["epcisMasterData"]?["vocabularyList"] as JArray ?? throw new Exception("Failed to grab json object epcisHeader.epcisMasterData.vocabularyList");

                foreach (var mdList in doc.MasterData.GroupBy(m => m.EPCISType))
                {
                    if (mdList.Key != null)
                    {
                        WriteMasterDataList(mdList.ToList(), jVocabList, mdList.Key);
                    }
                    else
                    {
                        throw new Exception("There are master data vocabulary elements where the Type is NULL.");
                    }
                }
            }
        }

        private static void WriteMasterDataList(List<IVocabularyElement> data, JArray xVocabList, string type)
        {
            if (data.Count > 0)
            {
                JObject jVocab = new JObject(new JProperty("type", type), new JProperty("vocabularyElementList", new JArray()));
                JArray xVocabEleList = jVocab["vocabularyElementList"] as JArray ?? throw new Exception("Failed to grab the array vocabularyElementList");

                foreach (IVocabularyElement md in data)
                {
                    JObject xMD = WriteMasterDataObject(md);
                    xVocabEleList.Add(xMD);
                }

                xVocabList.Add(jVocab);
            }
        }

        private static JObject WriteMasterDataObject(IVocabularyElement md)
        {
            JObject jVocabElement = new JObject(new JProperty("id", md.ID ?? string.Empty), new JProperty("attributes", new JArray()));
            JArray jAttributes = jVocabElement["attributes"] as JArray ?? throw new Exception("Failed to grab attributes array.");

            var mappings = OTMappingTypeInformation.GetMasterDataXmlTypeInfo(md.GetType());

            foreach (var mapping in mappings.Properties)
            {
                string id = mapping.Name;
                PropertyInfo p = mapping.Property;

                object o = p.GetValue(md);
                if (o != null)
                {
                    if (id == string.Empty)
                    {
                        var subMappings = OTMappingTypeInformation.GetMasterDataXmlTypeInfo(o.GetType());
                        foreach (var subMapping in subMappings.Properties)
                        {
                            string subID = subMapping.Name;
                            PropertyInfo subProperty = subMapping.Property;
                            object subObj = subProperty.GetValue(o);
                            if (subObj != null)
                            {
                                if (subObj.GetType() == typeof(List<LanguageString>))
                                {
                                    List<LanguageString> l = (List<LanguageString>)subObj;
                                    string str = l.FirstOrDefault()?.Value;
                                    if (str != null)
                                    {
                                        JObject jAttribute = new JObject(new JProperty("id", subID), new JProperty("attribute", str));
                                        jAttributes.Add(jAttribute);
                                    }
                                }
                                else
                                {
                                    string str = subObj.ToString();
                                    if (str != null)
                                    {
                                        JObject jAttribute = new JObject(new JProperty("id", subID), new JProperty("attribute", str));
                                        jAttributes.Add(jAttribute);
                                    }
                                }
                            }
                        }
                    }
                    else if (p.GetCustomAttribute<OpenTraceabilityObjectAttribute>() != null)
                    {
                        JObject jAttribute = new JObject(new JProperty("id", id), new JProperty("attribute", WriteObject(p.PropertyType, o)));
                        jAttributes.Add(jAttribute);
                    }
                    else if (p.GetCustomAttribute<OpenTraceabilityArrayAttribute>() != null)
                    {
                        IList l = (IList)o;
                        foreach (var i in l)
                        {
                            string str = i.ToString();
                            if (str != null)
                            {
                                JObject jAttribute = new JObject(new JProperty("id", id), new JProperty("attribute", str));
                                jAttributes.Add(jAttribute);
                            }
                        }
                    }
                    else if (o.GetType() == typeof(List<LanguageString>))
                    {
                        List<LanguageString> l = (List<LanguageString>)o;
                        string str = l.FirstOrDefault()?.Value;
                        if (str != null)
                        {
                            JObject jAttribute = new JObject(new JProperty("id", id), new JProperty("attribute", str));
                            jAttributes.Add(jAttribute);
                        }
                    }
                    else
                    {
                        string str = o.ToString();
                        if (str != null)
                        {
                            JObject jAttribute = new JObject(new JProperty("id", id), new JProperty("attribute", str));
                            jAttributes.Add(jAttribute);
                        }
                    }
                }
            }

            foreach (IMasterDataKDE kde in md.KDEs)
            {
                JToken jKDE = kde.GetGS1WebVocabJson();
                if (jKDE != null)
                {
                    JObject jAttribute = new JObject(new JProperty("id", kde.Name), new JProperty("attribute", jKDE));
                    jAttributes.Add(jAttribute);
                }
            }

            return jVocabElement;
        }

        private static JObject WriteObject(Type t, object o)
        {
            JObject j = new JObject();
            foreach (var property in t.GetProperties())
            {
                object value = property.GetValue(o);
                if (value != null)
                {
                    OpenTraceabilityAttribute xmlAtt = property.GetCustomAttribute<OpenTraceabilityAttribute>();
                    if (xmlAtt != null)
                    {
                        if (property.GetCustomAttribute<OpenTraceabilityObjectAttribute>() != null)
                        {
                            j[xmlAtt.Name] = WriteObject(property.PropertyType, value);
                        }
                        else
                        {
                            string str = value.ToString();
                            if (str != null)
                            {
                                j[xmlAtt.Name] = str;
                            }
                        }
                    }
                }
            }
            return j;
        }
    }
}
