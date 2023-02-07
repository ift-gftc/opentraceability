using System;
using System.Collections.Concurrent;
using System.Collections.Generic;
using System.Runtime.Serialization;
using System.Text;
using System.Xml.Linq;

namespace OpenTraceability.Utility
{
    public static class UnitSystems
    {
        private static ConcurrentDictionary<string, UnitSystem> m_unitSystem = null;
        private static object m_Locker = new object();

        private static ConcurrentDictionary<string, UnitSystem> UnitSystemMap
        {
            get
            {
                if (m_unitSystem == null)
                {
                    lock (m_Locker)
                    {
                        //check to make sure this thread is the first one to obtain the lock;
                        if (m_unitSystem == null)
                        {
                            m_unitSystem = Load();
                        }
                    }
                }
                return (m_unitSystem);
            }
        }
        public static List<UnitSystem> UnitSystemList
        {
            get
            {
                List<UnitSystem> lst = new List<UnitSystem>();
                foreach (KeyValuePair<string, UnitSystem> kvp in UnitSystemMap)
                {
                    lst.Add(kvp.Value);
                }
                return (lst);
            }
        }
        public static UnitSystem GetUnitSystem(string systemName)
        {
            UnitSystem language = null;
            if (!String.IsNullOrWhiteSpace(systemName))
            {
                if (UnitSystemMap.ContainsKey(systemName))
                {
                    language = UnitSystemMap[systemName];
                }
            }
            return (language);
        }

        public static UnitSystem GetUnitSystem(Int32 id)
        {
            UnitSystem? unitSystem = null;
            foreach (KeyValuePair<string, UnitSystem> kvp in UnitSystemMap)
            {
                if (kvp.Value.ID == id)
                {
                    unitSystem = kvp.Value;
                }
            }
            if (unitSystem == null)
            {
                throw new Exception("Failed to located UnitSystem with id=" + id);
            }
            return (unitSystem);
        }


        private static ConcurrentDictionary<string, UnitSystem> Load()
        {
            ConcurrentDictionary<string, UnitSystem> unitSystems = new ConcurrentDictionary<string, UnitSystem>();
            EmbeddedResourceLoader loader = new EmbeddedResourceLoader();
            XDocument xmlUnitSystems = loader.ReadXML("DSUtil", "DSUtil.StaticData.Data.UnitSystems.xml");
            if (xmlUnitSystems.Root == null)
            {
                throw new Exception("Failed to load UnitSystems.xml because the XDocument.Root is null.");
            }

            foreach (XElement x in xmlUnitSystems.Elements())
            {
                UnitSystem unitSystem = new UnitSystem(x);
                unitSystems.TryAdd(unitSystem.Name, unitSystem);
            }

            return (unitSystems);
        }
    }

    [DataContract]
    public class UnitSystem
    {
        Dictionary<string, string> m_DimensionMap;

        public UnitSystem()
        {
            m_DimensionMap = new Dictionary<string, string>();
        }

        public UnitSystem(XElement xml)
        {
            m_DimensionMap = new Dictionary<string, string>();
            Name = xml.Attribute("Name")?.Value ?? string.Empty;
            ID = int.Parse(xml.Attribute("ID")?.Value ?? string.Empty);

            foreach (XElement xDim in xml.Elements())
            {
                string dimension = xDim.Attribute("Dimension")?.Value ?? string.Empty;
                string subGroup = xDim.Attribute("SubGroup")?.Value ?? string.Empty;
                string unit = xDim.Attribute("Unit")?.Value ?? string.Empty;
                
                string key = "";
                if (!string.IsNullOrEmpty(subGroup))
                {
                    key = dimension + "!" + subGroup;
                    m_DimensionMap.Add(key, unit);
                    if (dimension == subGroup)
                    {
                        m_DimensionMap.Add(dimension, unit);
                    }
                }
                else
                {
                    m_DimensionMap.Add(dimension, unit);
                }

                // add to list of subgroups
                UnitSystemSubGroup sg = new UnitSystemSubGroup();
                sg.Dimension = dimension;
                sg.SubGroup = subGroup;
                sg.Unit = unit;
                this.SubGroups.Add(sg);
            }
        }

        public string Name { get; set; }

        public Int32 ID { get; set; }

        public List<UnitSystemSubGroup> SubGroups { get; set; } = new List<UnitSystemSubGroup>();

        public UOM? GetSystemUOM(UOM uom)
        {
            string uomUN = GetUOM(uom.UnitDimension, uom.SubGroup);
            UOM? systemUOM = UOMS.GetUOMFromUNCode(uomUN);
            return (systemUOM);
        }

        public UOM? GetSystemUOM(UOM uom, string subGroup)
        {
            string uomUN = GetUOM(uom.UnitDimension, subGroup);
            UOM? systemUOM = UOMS.GetUOMFromUNCode(uomUN);
            return (systemUOM);
        }

        public string GetUOM(string dimension, string subGroup = "")
        {
            string key = "";
            string strVal = "";
            if (!string.IsNullOrEmpty(subGroup))
            {
                key = dimension + "!" + subGroup;
            }
            else
            {
                key = dimension;
            }
            if (m_DimensionMap.ContainsKey(key))
            {
                strVal = m_DimensionMap[key];
            }
            return (strVal);
        }
    }

    [DataContract]
    public class UnitSystemSubGroup
    {
        public string Dimension { get; set; } = string.Empty;

        public string SubGroup { get; set; } = string.Empty;

        public string Unit { get; set; } = string.Empty;
    }
}
