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
    public enum EPCISDataFormat
    {
        XML,
        JSON
    }

    public class OTMappingTypeInformation
    {
        private static object _locker = new object();
        private static Dictionary<Type, OTMappingTypeInformation> _XmlTypeInfos = new Dictionary<Type, OTMappingTypeInformation>();
        private static Dictionary<Type, OTMappingTypeInformation> _JsonTypeInfos = new Dictionary<Type, OTMappingTypeInformation>();
        private static Dictionary<Type, OTMappingTypeInformation> _masterDataXmlTypeInfos = new Dictionary<Type, OTMappingTypeInformation>();
        private static Dictionary<Type, OTMappingTypeInformation> _masterDataJsonTypeInfos = new Dictionary<Type, OTMappingTypeInformation>();

        public static OTMappingTypeInformation GetXmlTypeInfo(Type t)
        {
            if (!_XmlTypeInfos.ContainsKey(t))
            {
                lock (_locker)
                {
                    if (!_XmlTypeInfos.ContainsKey(t))
                    {
                        OTMappingTypeInformation typeInfo = new OTMappingTypeInformation(t, EPCISDataFormat.XML);
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
                        OTMappingTypeInformation typeInfo = new OTMappingTypeInformation(t, EPCISDataFormat.JSON);
                        _JsonTypeInfos.Add(t, typeInfo);
                    }
                }
            }
            return _JsonTypeInfos[t];
        }
        public static OTMappingTypeInformation GetMasterDataXmlTypeInfo(Type t)
        {
            if (!_masterDataXmlTypeInfos.ContainsKey(t))
            {
                lock (_locker)
                {
                    if (!_masterDataXmlTypeInfos.ContainsKey(t))
                    {
                        OTMappingTypeInformation typeInfo = new OTMappingTypeInformation(t, EPCISDataFormat.XML, true);
                        _masterDataXmlTypeInfos.Add(t, typeInfo);
                    }
                }
            }
            return _masterDataXmlTypeInfos[t];
        }
        public static OTMappingTypeInformation GetMasterDataJsonTypeInfo(Type t)
        {
            if (!_masterDataJsonTypeInfos.ContainsKey(t))
            {
                lock (_locker)
                {
                    if (!_masterDataJsonTypeInfos.ContainsKey(t))
                    {
                        OTMappingTypeInformation typeInfo = new OTMappingTypeInformation(t, EPCISDataFormat.JSON, true);
                        _masterDataJsonTypeInfos.Add(t, typeInfo);
                    }
                }
            }
            return _masterDataJsonTypeInfos[t];
        }


        private Dictionary<string, OTMappingTypeInformationProperty> _dic = new Dictionary<string, OTMappingTypeInformationProperty>();


        public Type Type { get; set; }
        public List<OTMappingTypeInformationProperty> Properties { get; set; } = new List<OTMappingTypeInformationProperty>();
        public PropertyInfo? ExtensionKDEs { get; set; }
        public PropertyInfo? ExtensionAttributes { get; set; }

        public OTMappingTypeInformation(Type type, EPCISDataFormat format, bool isMasterDataMapping = false)
        {
            Type = type;

            foreach (PropertyInfo p in type.GetProperties())
            {
                if (format == EPCISDataFormat.XML && p.GetCustomAttribute<OpenTraceabilityXmlIgnoreAttribute>() != null)
                {
                    continue;
                }

                if (isMasterDataMapping == true)
                {
                    var mdAtt = p.GetCustomAttribute<OpenTraceabilityMasterDataAttribute>();

                    if (mdAtt != null)
                    {
                        OTMappingTypeInformationProperty property = new OTMappingTypeInformationProperty(p, mdAtt, format);
                        this.Properties.Add(property);
                        _dic.Add(property.Name, property);
                    }
                }
                else
                {
                    var atts = p.GetCustomAttributes<OpenTraceabilityAttribute>();
                    var jsonAtt = p.GetCustomAttribute<OpenTraceabilityJsonAttribute>();
                    var productAtts = p.GetCustomAttributes<OpenTraceabilityProductsAttribute>();

                    if (atts.Count() > 0)
                    {
                        foreach (var att in atts)
                        {
                            OTMappingTypeInformationProperty property = new OTMappingTypeInformationProperty(p, att, format);
                            if (!_dic.ContainsKey(property.Name))
                            {
                                this.Properties.Add(property);
                                _dic.Add(property.Name, property);
                            }
                        }
                    }
                    else if (jsonAtt != null && format == EPCISDataFormat.JSON)
                    {
                        OTMappingTypeInformationProperty property = new OTMappingTypeInformationProperty(p, jsonAtt, format);
                        if (!_dic.ContainsKey(property.Name))
                        {
                            this.Properties.Add(property);
                            _dic.Add(property.Name, property);
                        }
                    }
                    else if (productAtts.Count() > 0)
                    {
                        foreach (var att in productAtts)
                        {
                            OTMappingTypeInformationProperty property = new OTMappingTypeInformationProperty(p, att, format);
                            if (!_dic.ContainsKey(property.Name))
                            {
                                this.Properties.Add(property);
                                _dic.Add(property.Name, property);
                            }
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
        public OTMappingTypeInformationProperty(PropertyInfo property, OpenTraceabilityMasterDataAttribute att, EPCISDataFormat format)
        {
            this.Property = property;
            this.IsObject = property.GetCustomAttribute<OpenTraceabilityObjectAttribute>() != null;
            this.IsRepeating = property.GetCustomAttribute<OpenTraceabilityRepeatingAttribute>() != null;
            this.Name = att.Name;

            var arrayAttribute = property.GetCustomAttribute<OpenTraceabilityArrayAttribute>();
            if (arrayAttribute != null)
            {
                this.IsArray = true;
                this.ItemName = arrayAttribute.ItemName;
            }
        }

        public OTMappingTypeInformationProperty(PropertyInfo property, OpenTraceabilityAttribute att, EPCISDataFormat format)
        {
            this.Property = property;
            this.IsObject = property.GetCustomAttribute<OpenTraceabilityObjectAttribute>() != null;
            this.IsRepeating = property.GetCustomAttribute<OpenTraceabilityRepeatingAttribute>() != null;
            this.Name = att.Name;
            this.Version = att.Version;
            this.SequenceOrder = att.SequenceOrder;

            var arrayAttribute = property.GetCustomAttribute<OpenTraceabilityArrayAttribute>();
            if (arrayAttribute != null)
            {
                this.IsArray = true;
                this.ItemName = arrayAttribute.ItemName;
            }

            if (format == EPCISDataFormat.JSON)
            {
                var jsonAtt = property.GetCustomAttribute<OpenTraceabilityJsonAttribute>();
                if (jsonAtt != null)
                {
                    this.Name = jsonAtt.Name;
                }
            }
        }

        public OTMappingTypeInformationProperty(PropertyInfo property, OpenTraceabilityJsonAttribute att, EPCISDataFormat format)
        {
            this.Property = property;
            this.IsObject = property.GetCustomAttribute<OpenTraceabilityObjectAttribute>() != null;
            this.IsRepeating = property.GetCustomAttribute<OpenTraceabilityRepeatingAttribute>() != null;
            this.Name = att.Name;

            var arrayAttribute = property.GetCustomAttribute<OpenTraceabilityArrayAttribute>();
            if (arrayAttribute != null)
            {
                this.IsArray = true;
                this.ItemName = arrayAttribute.ItemName;
            }

            if (format == EPCISDataFormat.JSON)
            {
                var jsonAtt = property.GetCustomAttribute<OpenTraceabilityJsonAttribute>();
                if (jsonAtt != null)
                {
                    this.Name = jsonAtt.Name;
                }
            }
        }

        public OTMappingTypeInformationProperty(PropertyInfo property, OpenTraceabilityProductsAttribute att, EPCISDataFormat format)
        {
            this.Property = property;
            this.Name = att.Name;
            this.Version = att.Version;
            this.SequenceOrder = att.SequenceOrder;
            this.IsEPCList = att.ListType == OpenTraceabilityProductsListType.EPCList;
            this.IsQuantityList = att.ListType == OpenTraceabilityProductsListType.QuantityList;
            this.ProductType = att.ProductType;
            this.Required = att.Required;

            if (format == EPCISDataFormat.JSON)
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
        public bool IsRepeating { get; internal set; } = false;
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
