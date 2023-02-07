using System;
using System.Collections.Concurrent;
using System.Collections.Generic;
using System.Data;
using System.Runtime.Serialization;
using System.Text;
using System.Xml.Linq;

namespace OpenTraceability.Utility
{
    /// <remarks/>
    [System.Xml.Serialization.XmlTypeAttribute(AnonymousType = true)]
    [System.Xml.Serialization.XmlRootAttribute(Namespace = "", IsNullable = false)]
    public static class UOMS
    {
        #region Static
        [Obsolete("Use built in conversion support")]
        public static double ConvertToKilograms(UOM uom, double netweight)
        {
            try
            {
                switch (uom.ID)
                {
                    case 1: return netweight / 1000.0;      // 1000 grams per kilogram
                    case 2: return netweight * 0.453592;    // 0.453592 kilograms per pound
                    case 3: return netweight;
                    case 4: return netweight * 1000;        // 1000 kilograms in a metric ton
                    case 8: return netweight / 1000000;     // 1,000,000 milligrams in a kilogram
                    case 9: return netweight / 100000;      // 100,000 centigrams in a kilogram
                    case 10: return netweight / 10000;      // 10,000 decigrams in a kilogram
                    case 11: return netweight / 100;        // 100 dekagrams in a kilogram
                    case 12: return netweight / 10;         // 10 hectograms in a kilogram
                    case 13: return netweight / 35.274;     // 35.274 ounces in a kilogram
                    default: throw new Exception(string.Format("Failed to convert UOM (id: {0}, name: {1}) into kilograms because this unit is not a weight.", uom.ID, uom.Name));
                }
            }
            catch (Exception Ex)
            {
                OTLogger.Error(Ex);
                throw;
            }
        }
        #endregion

        private static ConcurrentDictionary<string, UOM> uomsAbbrevDict;
        private static ConcurrentDictionary<string, UOM> uomsUNCodeDict;
        private static object _locker = new object();

        static UOMS()
        {
            _locker = new object();
            uomsAbbrevDict = new ConcurrentDictionary<string, UOM>();
            uomsUNCodeDict = new ConcurrentDictionary<string, UOM>();
            Load();

        }

        static private void Load()
        {
            try
            {
                // load the subscriptions xml
                EmbeddedResourceLoader loader = new EmbeddedResourceLoader();
                XDocument xUOMs = loader.ReadXML("OpenTraceability", "OpenTraceability.Utility.Data.UOMs.xml");
                if (xUOMs.Root == null)
                {
                    throw new Exception("Failed to load UOMs.xml, the XDocument.Root is null.");
                }

                foreach (XElement x in xUOMs.Root.Elements())
                {
                    UOM uom = new UOM(x);
                    if (!uomsAbbrevDict.ContainsKey(uom.Abbreviation.ToLower()))
                    {
                        uomsAbbrevDict.TryAdd(uom.Abbreviation.ToLower(), uom);
                    }
                    else
                    {
                        System.Diagnostics.Trace.WriteLine("Duplicate Unit abbreviation detected:" + uom.Abbreviation);
                    }
                    if (!uomsUNCodeDict.ContainsKey(uom.UNCode.ToUpper()))
                    {
                        uomsUNCodeDict.TryAdd(uom.UNCode.ToUpper(), uom);
                    }
                    else
                    {
                        System.Diagnostics.Trace.WriteLine("Duplicate Unit UNCode detected:" + uom.UNCode);
                    }
                }
            }
            catch (Exception ex)
            {
                OTLogger.Error(ex);
                throw;
            }
        }

        static public UOM GetBase(UOM uom)
        {
            return (GetBase(uom.UnitDimension));
        }

        static private UOM GetBase(string dimension)
        {
            foreach (KeyValuePair<string, UOM> kvp in uomsAbbrevDict)
            {
                if (kvp.Value.UnitDimension == dimension)
                {
                    if (kvp.Value.IsBase())
                    {
                        return (kvp.Value);
                    }
                }
            }
            throw new Exception("Failed to get base for dimension = " + dimension);
        }

        static public UOM? GetUOMFromName(string Name)
        {
            UOM? uom = null;
            Name = Name.ToLower();
            if (Name == "count")
            {
                Name = "ea";
            }
            if (Name == "pound" || Name == "pounds" || Name == "ib")
            {
                Name = "lb";
            }
            if (Name[Name.Length - 1] == '.')
            {
                Name = Name.Substring(0, Name.Length - 1);
            }
            if (uomsAbbrevDict.ContainsKey(Name))
            {
                uom = uomsAbbrevDict[Name];
            }
            else
            {
                foreach (KeyValuePair<string, UOM> kvp in uomsAbbrevDict)
                {
                    if (kvp.Value.Name.ToLower() == Name)
                    {
                        uom = kvp.Value;
                        break;
                    }
                }
                if (uom == null)
                {
                    if (Name[Name.Length - 1] == 's')
                    {
                        Name = Name.Substring(0, Name.Length - 1);
                        foreach (KeyValuePair<string, UOM> kvp in uomsAbbrevDict)
                        {
                            if (kvp.Value.Name.ToLower() == Name)
                            {
                                uom = kvp.Value;
                                break;
                            }
                        }
                    }
                }
            }
            return (uom);
        }

        static public UOM? GetUOMFromUNCode(string Name)
        {
            UOM? uom = null;
            Name = Name.ToUpper();
            if (uomsUNCodeDict.ContainsKey(Name))
            {
                uom = uomsUNCodeDict[Name];
            }
            else
            {
                foreach (KeyValuePair<string, UOM> kvp in uomsAbbrevDict)
                {
                    if (kvp.Value.UNCode.ToLower() == Name.ToLower())
                    {
                        uom = kvp.Value;
                        break;
                    }
                }
            }
            return (uom);
        }

        static public List<UOM> UOMList
        {
            get
            {
                List<UOM> lst = new List<UOM>();
                lock (_locker)
                {
                    foreach (KeyValuePair<string, UOM> kvp in uomsAbbrevDict)
                    {
                        lst.Add(kvp.Value);
                    }
                    return lst;
                }
            }
        }

        static public void AddUOM(UOM uom)
        {
            lock (_locker)
            {
                if (uom == null)
                {
                    throw new ArgumentNullException("uom argument can not be null");
                }
                if (!uomsAbbrevDict.ContainsKey(uom.Name))
                {
                    uomsAbbrevDict.TryAdd(uom.Name, uom);
                }
                else
                {
                    throw new Exception("Unit already exists in units collection");
                }
            }
        }
    }

    /// <remarks/>
    [System.Xml.Serialization.XmlTypeAttribute(AnonymousType = true)]
    [DataContract]
    public partial class UOM
    {
        /* STATIC */
        private static object _locker = new object();
        public static UOM LookUpFromUNCode(string unCode)
        {
            try
            {
                lock (_locker)
                {
                    UOM? uom = UOMS.UOMList.Find(u => u.UNCode == unCode);
                    if (uom == null)
                    {
                        uom = new UOM();
                    }
                    return (uom);
                }
            }
            catch (Exception Ex)
            {
                OTLogger.Error(Ex);
                throw;
            }
        }

        public static bool IsNullOrEmpty(UOM? uom)
        {
            if (uom == null)
            {
                return (true);
            }
            else if (string.IsNullOrEmpty(uom.Abbreviation))
            {
                return (true);
            }
            else
            {
                return (false);
            }
        }

        public static UOM ParseFromName(string name)
        {
            UOM? u = null;
            try
            {
                if (string.IsNullOrEmpty(name))
                {
                    throw new ArgumentNullException(nameof(name));
                }

                UOM? uom = UOMS.GetUOMFromName(name);
                if (uom != null)
                {
                    u = new UOM();
                    u.ID = uom.ID;
                    u.Name = uom.Name;
                    u.Abbreviation = uom.Abbreviation;
                    u.UnitDimension = uom.UnitDimension;
                    u.UNCode = uom.UNCode;
                    u.LegacyID = uom.LegacyID;
                    u.SubGroup = uom.SubGroup;
                    u.A = uom.A;
                    u.B = uom.B;
                    u.C = uom.C;
                    u.D = uom.D;
                }
                else
                {
                    uom = UOMS.GetUOMFromUNCode(name.ToUpper());
                    if (uom != null)
                    {
                        u = new UOM();
                        u.ID = uom.ID;
                        u.Name = uom.Name;
                        u.Abbreviation = uom.Abbreviation;
                        u.UnitDimension = uom.UnitDimension;
                        u.SubGroup = uom.SubGroup;
                        u.UNCode = uom.UNCode;
                        u.LegacyID = uom.LegacyID;
                        u.A = uom.A;
                        u.B = uom.B;
                        u.C = uom.C;
                        u.D = uom.D;
                    }
                    else
                    {
                        throw new Exception("Failed to parse UOM");
                    }
                }

                return u;
            }
            catch (Exception ex)
            {
                OTLogger.Error(ex);
                throw;
            }
        }

        public UOM(UOM uom)
        {
            this.Abbreviation = uom.Abbreviation;
            this.Name = uom.Name;
            this.UnitDimension = uom.UnitDimension;
            this.UNCode = uom.UNCode;
            this.SubGroup = uom.SubGroup;
            this.ID = uom.ID;
            this.A = uom.A;
            this.B = uom.B;
            this.C = uom.C;
            this.D = uom.D;
            this.LegacyID = uom.LegacyID;
        }

        public void CopyFrom(UOM uom)
        {
            this.Abbreviation = uom.Abbreviation;
            this.Name = uom.Name;
            this.UnitDimension = uom.UnitDimension;
            this.UNCode = uom.UNCode;
            this.SubGroup = uom.SubGroup;
            this.ID = uom.ID;
            this.A = uom.A;
            this.B = uom.B;
            this.C = uom.C;
            this.D = uom.D;
            this.LegacyID = uom.LegacyID;
        }
        public bool IsBase()
        {
            if (A == 0.0 && B == 1.0 && C == 1.0 && D == 0.0)
            {
                return (true);
            }
            else
            {
                return (false);
            }
        }
        public override bool Equals(object? obj)
        {
            if (obj != null)
            {
                if (obj is UOM)
                {
                    UOM other = (UOM)obj;
                    if (UNCode == other.UNCode)
                    {
                        return (true);
                    }
                }
            }
            return (false);
        }

        public override int GetHashCode()
        {
            if (UNCode != null)
            {
                return (UNCode.GetHashCode());
            }
            else
            {
                return (base.GetHashCode());
            }

        }

        public Int64 ID { get; set; }

        public int LegacyID { get; set; }

        public string Key
        {
            get
            {
                return (Abbreviation);
            }
        }

        public string Name { get; private set; }

        public string Abbreviation { get; private set; }

        public string UnitDimension { get; private set; }

        public string SubGroup { get; private set; }

        public string UNCode { get; private set; }

        public double A { get; private set; }

        public double B { get; private set; }

        public double C { get; private set; }

        public double D { get; private set; }

        public UOM()
        {
            A = 0.0;
            B = 1.0;
            C = 1.0;
            D = 0.0;
        }

        public UOM(XElement xmlUOM)
        {
            A = 0.0;
            B = 1.0;
            C = 1.0;
            D = 0.0;

            Name = xmlUOM.Attribute("Name")?.Value ?? string.Empty;
            Abbreviation = xmlUOM.Attribute("Abbreviation")?.Value ?? string.Empty;
            UnitDimension = xmlUOM.Attribute("Dimension")?.Value ?? string.Empty;
            UNCode = xmlUOM.Attribute("UNCode")?.Value ?? string.Empty;
            SubGroup = xmlUOM.Attribute("SubGroup")?.Value ?? string.Empty;

            if (xmlUOM.Attribute("Factor") != null)
            {
                A = 0.0;
                B = double.Parse(xmlUOM.Attribute("Factor")?.Value ?? string.Empty);
                C = 1.0;
                D = 0.0;
            }
            else if (xmlUOM.Attribute("Numerator") != null)
            {
                A = 0.0;
                B = double.Parse(xmlUOM.Attribute("Numerator")?.Value ?? string.Empty);
                C = double.Parse(xmlUOM.Attribute("Denominator")?.Value ?? string.Empty);
                D = 0.0;
            }
            else if (xmlUOM.Attribute("A") != null)
            {
                A = double.Parse(xmlUOM.Attribute("A")?.Value ?? string.Empty);
                B = double.Parse(xmlUOM.Attribute("B")?.Value ?? string.Empty);
                C = double.Parse(xmlUOM.Attribute("C")?.Value ?? string.Empty);
                D = double.Parse(xmlUOM.Attribute("D")?.Value ?? string.Empty);
            }
        }

        public double Convert(double value, UOM to)
        {
            double valueBase = this.ToBase(value);
            double valueNew = to.FromBase(valueBase);
            return (valueNew);
        }

        static public double Convert(double value, UOM from, UOM to)
        {
            double valueBase = from.ToBase(value);
            double valueNew = to.FromBase(valueBase);
            return (valueNew);
        }

        public double ToBase(double value)
        {
            double baseValue = (A + B * value) / (C + D * value);
            return baseValue;
        }

        public double FromBase(double baseValue)
        {
            double value = (A - C * baseValue) / (D * baseValue - B);
            return value;
        }

        public override String ToString()
        {
            return Name + " [" + Abbreviation + "]";
        }
    }
}
