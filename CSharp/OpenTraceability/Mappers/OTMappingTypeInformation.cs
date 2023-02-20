using OpenTraceability.Models.Events;
using OpenTraceability.Utility.Attributes;
using System;
using System.Collections.Generic;
using System.Linq;
using System.Reflection;
using System.Security.Permissions;
using System.Text;
using System.Threading.Tasks;

namespace OpenTraceability.Mappers
{
    public enum OTMappingFormat
    {
        XML,
        JSON
    }

    public class OTMappingTypeInformation
    {
        private static object _locker = new object();
        private static Dictionary<Type, OTMappingTypeInformation> _XmlTypeInfos = new Dictionary<Type, OTMappingTypeInformation>();
        private static Dictionary<Type, OTMappingTypeInformation> _JsonTypeInfos = new Dictionary<Type, OTMappingTypeInformation>();
        public static OTMappingTypeInformation GetXmlTypeInfo(Type t)
        {
            if (!_XmlTypeInfos.ContainsKey(t))
            {
                lock(_locker)
                {
                    if (!_XmlTypeInfos.ContainsKey(t))
                    {
                        OTMappingTypeInformation typeInfo = new OTMappingTypeInformation(t, OTMappingFormat.XML);
                        _XmlTypeInfos.Add(t, typeInfo);
                    }
                }
            }
            return _XmlTypeInfos[t];
        }
        public static OTMappingTypeInformation GetJsonTypeInfo(Type t)
        {
            if (!_JsonTypeInfos.ContainsKey(t))
            {
                lock (_locker)
                {
                    if (!_JsonTypeInfos.ContainsKey(t))
                    {
                        OTMappingTypeInformation typeInfo = new OTMappingTypeInformation(t, OTMappingFormat.JSON);
                        _JsonTypeInfos.Add(t, typeInfo);
                    }
                }
            }
            return _JsonTypeInfos[t];
        }

        private Dictionary<string, OTMappingTypeInformationProperty> _dic = new Dictionary<string, OTMappingTypeInformationProperty>();

        public Type Type { get; set; }
        public List<OTMappingTypeInformationProperty> Properties { get; set; } = new List<OTMappingTypeInformationProperty>();
        public PropertyInfo? ExtensionKDEs { get; set; }
        public PropertyInfo? ExtensionAttributes { get; set; }

        public OTMappingTypeInformation(Type type, OTMappingFormat format)
        {
            Type = type;

            foreach (PropertyInfo p in type.GetProperties())
            {
                var atts = p.GetCustomAttributes<OpenTraceabilityAttribute>();
                var productAtts = p.GetCustomAttributes<OpenTraceabilityProductsAttribute>();
                if (atts.Count() > 0)
                {
                    foreach (var att in atts)
                    {
                        OTMappingTypeInformationProperty property = new OTMappingTypeInformationProperty(p, att, format);
                        this.Properties.Add(property);
                        _dic.Add(property.Name, property);
                    }
                }
                else if (productAtts.Count() > 0)
                {
                    foreach (var att in productAtts)
                    {
                        OTMappingTypeInformationProperty property = new OTMappingTypeInformationProperty(p, att, format);
                        this.Properties.Add(property);
                        _dic.Add(property.Name, property);
                    }
                }
                else if (p.GetCustomAttribute<OpenTraceabilityExtensionElementsAttribute>() != null)
                {
                    this.ExtensionKDEs = p;
                }
                else if (p.GetCustomAttribute<OpenTraceabilityExtensionAttributesAttribute>() != null)
                {
                    this.ExtensionAttributes = p;
                }
                this.Properties = this.Properties.OrderBy(p => p.SequenceOrder == null).ThenBy(p => p.SequenceOrder).ToList();
            }
        }

        public OTMappingTypeInformationProperty? this[string name]
        {
            get
            {
                if (_dic.ContainsKey(name))
                {
                    return _dic[name];
                }
                else
                {
                    return null;
                }
            }
        }
    }

    public class OTMappingTypeInformationProperty
    {
        public OTMappingTypeInformationProperty(PropertyInfo property, OpenTraceabilityAttribute att, OTMappingFormat format)
        {
            this.Property = property;
            this.IsObject = property.GetCustomAttribute<OpenTraceabilityObjectAttribute>() != null;
            this.Name = att.Name;
            this.Version = att.Version;
            this.SequenceOrder = att.SequenceOrder;

            var arrayAttribute = property.GetCustomAttribute<OpenTraceabilityArrayAttribute>();
            if (arrayAttribute != null)
            {
                this.IsArray = true;
                this.ItemName = arrayAttribute.ItemName;
            }

            if (format == OTMappingFormat.JSON)
            {
                var jsonAtt = property.GetCustomAttribute<OpenTraceabilityJsonAttribute>();
                if (jsonAtt != null)
                {
                    this.Name = jsonAtt.Name;
                }
            }
        }

        public OTMappingTypeInformationProperty(PropertyInfo property, OpenTraceabilityProductsAttribute att, OTMappingFormat format)
        {
            this.Property = property;
            this.Name = att.Name;
            this.Version = att.Version;
            this.SequenceOrder = att.SequenceOrder;
            this.IsEPCList = att.ListType == OpenTraceabilityProductsListType.EPCList;
            this.IsQuantityList = att.ListType == OpenTraceabilityProductsListType.QuantityList;
            this.ProductType = att.ProductType;
            this.Required = att.Required;

            if (format == OTMappingFormat.JSON)
            {
                var jsonAtt = property.GetCustomAttribute<OpenTraceabilityJsonAttribute>();
                if (jsonAtt != null)
                {
                    this.Name = jsonAtt.Name;
                }
            }
        }

        public PropertyInfo Property { get; internal set; }

        public bool Required { get; internal set; } = false;
        public bool IsObject { get; internal set; } = false;
        public bool IsArray { get; internal set; } = false;
        public bool IsEPCList { get; internal set; } = false;
        public bool IsQuantityList { get; internal set; } = false;
        public EventProductType ProductType { get; internal set; }
        public string Name { get; internal set; } = string.Empty;
        public string? ItemName { get; internal set; } = null;
        public EPCISVersion? Version { get; internal set; } = null;
        public int? SequenceOrder { get; internal set; } = null;
        public string? CURIEMapping { get; internal set; }
    }
}
