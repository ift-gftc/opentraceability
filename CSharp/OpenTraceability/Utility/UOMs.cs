using System;
using System.Collections.Concurrent;
using System.Collections.Generic;
using System.Data;
using System.Runtime.Serialization;
using System.Text;

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

        static public DataTable GetDataTable()
        {
            DataTable dt = new DataTable("UOMS");
            dt.Columns.Add("Name");
            dt.Columns.Add("UNCode");
            dt.Columns.Add("Abbreviation");
            foreach (UOM item in UOMList)
            {
                DataRow row = dt.NewRow();
                row["Name"] = item.Name;
                row["UNCode"] = item.UNCode;
                row["Abbreviation"] = item.Abbreviation;
                dt.Rows.Add(row);
            }
            return (dt);
        }

        static private void Load()
        {
            try
            {
                // load the subscriptions xml
                EmbeddedResourceLoader loader = new EmbeddedResourceLoader();
                DSXML xUOMs = loader.ReadXML("DSUtilities", "DSUtil.StaticData.Data.UOMs.xml");
                foreach (DSXML xmlUOM in xUOMs)
                {
                    UOM uom = new UOM(xmlUOM);
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
                DSLogger.Log(0, ex);
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
            return (null);
        }

        static public UOM GetUOMFromID(int ID)
        {
            UOM uom = null;
            foreach (KeyValuePair<string, UOM> kvp in uomsAbbrevDict)
            {
                if (kvp.Value.LegacyID == ID)
                {
                    uom = kvp.Value;
                    break;
                }
            }
            if (uom == null)
            {
                DSLogger.Log(0, "Failed to locate UoM by legacyID=" + ID);
            }
            return (uom);
        }

        static public UOM GetUOMFromName(string Name)
        {
            UOM uom = null;
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

        static public UOM GetUOMFromUNCode(string Name)
        {
            UOM uom = null;
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

        /// <remarks/>

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
                    UOM uom = UOMS.UOMList.Find(u => u.UNCode == unCode);
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

        public static bool IsNullOrEmpty(UOM uom)
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
            UOM u = null;
            try
            {
                if (string.IsNullOrEmpty(name))
                {
                    return null;
                }
                UOM uom = UOMS.GetUOMFromName(name);
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
                        DSLogger.Log(1, "Unit conversion failed, Name=" + name);
                    }
                }
            }
            catch (Exception ex)
            {
                u = null;
                string Message = ex.Message;
                DSLogger.Log(1, "Unit conversion failed, Name=" + name);
            }
            return u;
        }

        /*
        public UOM(string abbreviation, string name, string unitDimension, string unCode, Int64 id)
        {
            Abbreviation = abbreviation;
            Name = name;
            UnitDimension = unitDimension;
            UNCode = unCode;
            ID = id;
        }*/

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


        [DataMember]
        [System.Xml.Serialization.XmlIgnore]
        public Int64 ID { get; set; }

        [System.Xml.Serialization.XmlIgnore]
        public int LegacyID { get; set; }

        [System.Xml.Serialization.XmlIgnore]
        public string Key
        {
            get
            {
                return (Abbreviation);
            }
        }

        [DataMember]
        [System.Xml.Serialization.XmlIgnore]
        public string Name { get; private set; }

        [DataMember]
        [System.Xml.Serialization.XmlIgnore]
        public string Abbreviation { get; private set; }

        [DataMember]
        [System.Xml.Serialization.XmlIgnore]
        public string UnitDimension { get; private set; }

        [DataMember]
        [System.Xml.Serialization.XmlIgnore]
        public string SubGroup { get; private set; }

        [DataMember]
        [System.Xml.Serialization.XmlIgnore]
        public string UNCode { get; private set; }

        [DataMember]
        [System.Xml.Serialization.XmlIgnore]
        public double A { get; private set; }

        [DataMember]
        [System.Xml.Serialization.XmlIgnore]
        public double B { get; private set; }

        [DataMember]
        [System.Xml.Serialization.XmlIgnore]
        public double C { get; private set; }


        [DataMember]
        [System.Xml.Serialization.XmlIgnore]
        public double D { get; private set; }

        [DataMember]
        public string StrValue
        {
            get
            {
                return (this.UNCode);
            }
            set
            {
                if (value != this.UNCode)
                {
                    UOM UOM = UOM.ParseFromName(value);
                    this.CopyFrom(UOM);
                }
            }
        }

        /* INSTANCE */
        private DateTime createdField;
        private DateTime updatedField;

        public UOM()
        {
            A = 0.0;
            B = 1.0;
            C = 1.0;
            D = 0.0;
        }

        public UOM(DSXML xmlUOM)
        {
            A = 0.0;
            B = 1.0;
            C = 1.0;
            D = 0.0;
            ID = xmlUOM.AttributeInt64Value("ID");
            Name = xmlUOM.Attribute("Name");
            Abbreviation = xmlUOM.Attribute("Abbreviation");
            UnitDimension = xmlUOM.Attribute("Dimension");
            UNCode = xmlUOM.Attribute("UNCode");
            SubGroup = xmlUOM.Attribute("SubGroup");
            LegacyID = xmlUOM.AttributeInt32Value("LegacyID");
            if (xmlUOM.HasAttribute("Factor"))
            {
                A = 0.0;
                B = xmlUOM.AttributeDoubleValue("Factor");
                C = 1.0;
                D = 0.0;
            }
            else if (xmlUOM.HasAttribute("Numerator"))
            {
                A = 0.0;
                B = xmlUOM.AttributeDoubleValue("Numerator");
                C = xmlUOM.AttributeDoubleValue("Denominator");
                D = 0.0;
            }
            else if (xmlUOM.HasAttribute("A"))
            {
                A = xmlUOM.AttributeDoubleValue("A");
                B = xmlUOM.AttributeDoubleValue("B");
                C = xmlUOM.AttributeDoubleValue("C");
                D = xmlUOM.AttributeDoubleValue("D");
            }

        }

        public double? Convert(double? value, UOM to)
        {
            if (value == null)
            {
                return (null);
            }
            double? valueBase = this.ToBase(value.Value);
            double? valueNew = to.FromBase(valueBase);
            return (valueNew);
        }

        static public double? Convert(double? value, UOM from, UOM to)
        {
            if (value == null)
            {
                return (null);
            }
            double? valueBase = from.ToBase(value.Value);
            double? valueNew = to.FromBase(valueBase);
            return (valueNew);
        }

        public double? ToBase(double? value)
        {
            if (value == null)
            {
                return (null);
            }
            double baseValue = (A + B * value.Value) / (C + D * value.Value);
            return baseValue;
        }
        public double? FromBase(double? baseValue)
        {
            if (baseValue == null)
            {
                return (null);
            }

            double value = (A - C * baseValue.Value) / (D * baseValue.Value - B);
            return value;
        }

        public override String ToString()
        {
            return Name + " [" + Abbreviation + "]";
        }

        private void Copy(UOM source)
        {
            if (source != null)
            {
                this.ID = source.ID;
                this.LegacyID = source.LegacyID;
                this.Name = source.Name;
                this.Abbreviation = source.Abbreviation;
                this.UnitDimension = source.UnitDimension;
                this.UNCode = source.UNCode;
                this.SubGroup = source.SubGroup;
                this.A = source.A;
                this.B = source.B;
                this.C = source.C;
                this.D = source.D;
            }
        }
        [System.Xml.Serialization.XmlIgnore]
        public Int64 TRIDSetter
        {
            get
            {
                return (LegacyID);
            }
            set
            {
                try
                {
                    ID = value;
                    UOM uom = UOMS.GetUOMFromID((Int32)ID);
                    this.Copy(uom);

                }
                catch (Exception ex)
                {
                    string Message = ex.Message;
                    DSLogger.Log(1, "Unit conversion failed, ID=" + value);
                }
            }

        }

        [System.Xml.Serialization.XmlIgnore]
        public String NameSetter
        {
            get
            {
                return (Name);
            }
            set
            {
                try
                {
                    UOM uom = UOMS.GetUOMFromName(value);
                    if (uom == null)
                    {
                        uom = UOMS.GetUOMFromUNCode(value);
                    }
                    this.Copy(uom);
                }
                catch (Exception ex)
                {
                    string Message = ex.Message;
                    DSLogger.Log(1, "Unit conversion failed, ID=" + value);
                }
            }

        }

        [System.Xml.Serialization.XmlIgnore]
        public DateTime Updated
        {
            get
            {
                return this.updatedField;
            }
            set
            {
                this.updatedField = value;
            }
        }

        /// <remarks/>
        [System.Xml.Serialization.XmlIgnore]
        public DateTime Created
        {
            get
            {
                return this.createdField;
            }
            set
            {
                this.createdField = value;
            }
        }

        public DSXML ToXML(DSXML xmlParent, string name = "UOM")
        {
            DSXML xmlUOM = xmlParent.AddChild(name);
            xmlUOM.Attribute("ID", ID);
            xmlUOM.Attribute("Name", Name);
            xmlUOM.Attribute("Abbreviation", Abbreviation);
            xmlUOM.Attribute("UnitDimension", UnitDimension);
            xmlUOM.Attribute("UNCode", UNCode);
            xmlUOM.Attribute("LegacyID", LegacyID);
            xmlUOM.Attribute("A", A);
            xmlUOM.Attribute("B", B);
            xmlUOM.Attribute("C", C);
            xmlUOM.Attribute("D", D);
            xmlUOM.Attribute("SubGroup", SubGroup);
            return (xmlUOM);
        }

        public static UOM FromXML(DSXML xmlUOM)
        {
            UOM uom = new UOM();
            if (xmlUOM == null || xmlUOM.IsNull)
            {
                return (uom);
            }
            uom.ID = xmlUOM.AttributeInt64Value("ID");
            uom.Name = xmlUOM.Attribute("Name");
            uom.Abbreviation = xmlUOM.Attribute("Abbreviation");
            uom.UnitDimension = xmlUOM.Attribute("Dimension");
            uom.UNCode = xmlUOM.Attribute("UNCode");
            uom.A = xmlUOM.AttributeDoubleValue("A");
            uom.B = xmlUOM.AttributeDoubleValue("B");
            uom.C = xmlUOM.AttributeDoubleValue("C");
            uom.D = xmlUOM.AttributeDoubleValue("D");
            uom.SubGroup = xmlUOM.Attribute("SubGroup");
            uom.LegacyID = xmlUOM.AttributeInt32Value("LegacyID");
            return (uom);
        }
    }
}
