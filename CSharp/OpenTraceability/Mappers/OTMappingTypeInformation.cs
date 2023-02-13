using OpenTraceability.Utility.Attributes;
using System;
using System.Collections.Generic;
using System.Linq;
using System.Reflection;
using System.Text;
using System.Threading.Tasks;

namespace OpenTraceability.Mappers
{
    public class OTMappingTypeInformation
    {
        private static object _locker = new object();
        private static Dictionary<Type, OTMappingTypeInformation> _typeInfos = new Dictionary<Type, OTMappingTypeInformation>();
        public static OTMappingTypeInformation GetTypeInfo(Type t)
        {
            if (!_typeInfos.ContainsKey(t))
            {
                lock(_locker)
                {
                    if (!_typeInfos.ContainsKey(t))
                    {
                        OTMappingTypeInformation typeInfo = new OTMappingTypeInformation();
                        typeInfo.Type = t;
                        foreach (PropertyInfo p in t.GetProperties())
                        {
                            OpenTraceabilityXmlAttribute? att = p.GetCustomAttribute<OpenTraceabilityXmlAttribute>();
                            if (att != null)
                            {
                                typeInfo.XmlAttributes.Add(att, p);
                            }
                            else
                            {
                                if (p.GetCustomAttribute<OpenTraceabilityExtensionElementsAttribute>() != null)
                                {
                                    typeInfo.ExtensionKDEs = p;
                                }
                                else if (p.GetCustomAttribute<OpenTraceabilityExtensionAttributesAttribute>() != null)
                                {
                                    typeInfo.ExtensionAttributes = p;
                                }
                            }
                        }
                        typeInfo.XmlAttributes = new Dictionary<OpenTraceabilityXmlAttribute, PropertyInfo>(typeInfo.XmlAttributes.OrderBy(x => x.Key.SequenceOrder));
                        _typeInfos.Add(t, typeInfo);
                    }
                }
            }
            return _typeInfos[t];
        }

        public Type Type { get; set; }
        public Dictionary<OpenTraceabilityXmlAttribute, PropertyInfo> XmlAttributes { get; set; } = new Dictionary<OpenTraceabilityXmlAttribute, PropertyInfo>();
        public PropertyInfo? ExtensionKDEs { get; set; }
        public PropertyInfo? ExtensionAttributes { get; set; }
    }
}
