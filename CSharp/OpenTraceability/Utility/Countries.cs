using System.Collections.Concurrent;
using System.Data;
using System.Runtime.Serialization;
using System.Text;

namespace OpenTraceability.Utility
{
    public static class Countries
    {
        private static ConcurrentDictionary<string, Country> _dirCountries;
        private static ConcurrentDictionary<string, Country> _dirAlpha3Countries;
        private static ConcurrentDictionary<string, Country> _dirNameCountries;
        private static object _locker = new object();

        static Countries()
        {
            _dirCountries = new ConcurrentDictionary<string, Country>();
            _dirAlpha3Countries = new ConcurrentDictionary<string, Country>();
            _dirNameCountries = new ConcurrentDictionary<string, Country>();
            Load();
        }

        private static void Load()
        {
            string data = null;
            data = StaticData.ReadData("Countries.xml");
            DSXML xmlCountries = new DSXML();
            xmlCountries.LoadFromString(data);
            foreach (DSXML xmlCountry in xmlCountries)
            {
                Country country = new Country(xmlCountry);
                _dirCountries.TryAdd(country.Abbreviation.ToUpper(), country);
                _dirNameCountries.TryAdd(country.Name.ToUpper(), country);
                if (!string.IsNullOrEmpty(country.Alpha3))
                {
                    _dirAlpha3Countries.TryAdd(country.Alpha3.ToUpper(), country);
                }
            }
        }

        public static DataTable GetDataTable()
        {
            DataTable dt = new DataTable("Countries");
            dt.Columns.Add("Name");
            dt.Columns.Add("ISO");
            dt.Columns.Add("Abbreviation");
            dt.Columns.Add("Alpha3");
            dt.Columns.Add("AlternativeName");
            foreach (Country item in CountryList)
            {
                DataRow row = dt.NewRow();
                row["Name"] = item.Name;
                row["ISO"] = item.ISO;
                row["Abbreviation"] = item.Abbreviation;
                row["Alpha3"] = item.Alpha3;
                row["AlternativeName"] = item.AlternativeName;
                dt.Rows.Add(row);
            }
            return (dt);
        }

        public static string CountryListToISOString(List<Country> countriesList)
        {
            string result = "";
            if (countriesList != null)
            {
                StringBuilder sb = new StringBuilder();
                List<Country> lstOrdered = countriesList.OrderBy(x => x.ISO).ToList();
                foreach (Country country in lstOrdered)
                {
                    if (country != null)
                    {
                        sb.Append(country.ISO);
                        sb.Append(";");
                    }
                }
                result = sb.ToString();
            }
            return (result);
        }

        public static string CountryListToNameString(List<Country> countriesList, char seperator = ';')
        {
            string result = "";
            if (countriesList != null)
            {
                StringBuilder sb = new StringBuilder();
                List<Country> lstOrdered = countriesList.OrderBy(x => x.Name).ToList();
                foreach (Country country in lstOrdered)
                {
                    if (country != null)
                    {
                        sb.Append(country.Name);
                        sb.Append(seperator);
                    }
                }
                result = sb.ToString();
            }
            return (result);
        }

        /// <summary>
        /// Takes a list of countries and returns a list of Int32 (nullabl) Iso Codes
        /// </summary>
        /// <param name="countriesList"></param>
        /// <returns></returns>
        public static List<Int32?> CountryListToISOs(List<Country> countriesList)
        {
            List<Int32?> lst = new List<int?>();
            if (countriesList != null)
            {
                List<Country> lstOrdered = countriesList.OrderBy(x => x.ISO).ToList();
                foreach (Country country in lstOrdered)
                {
                    if (country != null)
                    {
                        lst.Add(country.ISO);
                    }
                }
            }
            return (lst);
        }

        /// <summary>
        /// Takes a list of Int32 ? representing ISO codes and returns a list of Countries;
        /// </summary>
        /// <param name="countriesList"></param>
        /// <returns></returns>
        public static List<Country> CountryListFromISOs(List<Int32?> isoList)
        {
            List<Country> lst = new List<Country>();
            if (isoList != null)
            {
                foreach (Int32? iso in isoList)
                {
                    if (iso.HasValue)
                    {
                        Country country = Countries.FromCountryIso(iso.Value);
                        if (country != null)
                        {
                            lst.Add(country);
                        }
                    }
                }
            }
            return (lst);
        }

        public static List<Country> CountryListFromISOString(string strListOfIsoCodes)
        {
            List<Country> countries = new List<Country>();
            if (!string.IsNullOrEmpty(strListOfIsoCodes))
            {
                string[] strISOs = strListOfIsoCodes.Split(';');
                foreach (string strISO in strISOs)
                {
                    if (!string.IsNullOrEmpty(strISO))
                    {
                        try
                        {
                            Int32 isoCode = System.Convert.ToInt32(strISO);
                            Country country = Countries.FromCountryIso(isoCode);
                            if (country != null)
                            {
                                countries.Add(country);
                            }
                        }
                        catch
                        {
                        }
                    }
                }
            }
            return (countries);
        }

        public static List<Country> CountryListFromString(string strListOfCountries, char seperator = ';')
        {
            List<Country> countries = new List<Country>();
            if (!string.IsNullOrEmpty(strListOfCountries))
            {
                string[] strCountires = strListOfCountries.Split(seperator);
                foreach (string strCountry in strCountires)
                {
                    if (!string.IsNullOrEmpty(strCountry))
                    {
                        Country country = Countries.TryGetCountry(strCountry);
                        if (country != null)
                        {
                            countries.Add(country);
                        }
                    }
                }
            }
            return (countries);
        }

        public static List<Country> CountryList
        {
            get
            {
                List<Country> list = new List<Country>();
                foreach (KeyValuePair<string, Country> kvp in _dirNameCountries)
                {
                    list.Add(kvp.Value);
                }
                return (list);
            }
        }

        public static Country FromAbbreviation(string code)
        {
            Country country = null;
            if (!string.IsNullOrEmpty(code))
            {
                if (_dirCountries != null)
                {
                    if (_dirCountries.ContainsKey(code.ToUpper()))
                    {
                        country = _dirCountries[code.ToUpper()];
                    }
                }
            }
            return (country);
        }

        public static Country FromAlpha3(string code)
        {
            Country country = null;
            if (!string.IsNullOrEmpty(code))
            {
                if (_dirCountries != null)
                {
                    if (_dirAlpha3Countries.ContainsKey(code.ToUpper()))
                    {
                        country = _dirAlpha3Countries[code.ToUpper()];
                    }
                }
            }
            return (country);
        }

        public static Country FromCountryName(string name)
        {
            Country country = null;
            if (_dirCountries != null && !String.IsNullOrWhiteSpace(name))
            {
                if (_dirNameCountries.ContainsKey(name.ToUpper()))
                {
                    country = _dirNameCountries[name.ToUpper()];
                }
                else
                {
                    foreach (KeyValuePair<string, Country> kvp in _dirNameCountries)
                    {
                        if (kvp.Value.AlternativeName != null)
                        {
                            if (kvp.Value.AlternativeName.ToUpper() == name.ToUpper())
                            {
                                country = kvp.Value;
                                break;
                            }
                        }
                    }
                }
            }
            return country;
        }

        public static Country FromCountryIso(int iso)
        {
            Country country = null;
            if (_dirCountries != null)
            {
                foreach (KeyValuePair<string, Country> kvp in _dirCountries)
                {
                    if (kvp.Value.ISO == iso)
                    {
                        country = kvp.Value;
                        break;
                    }
                }
            }

            return (country);
        }

        public static Country TryGetCountry(string strValue)
        {
            Country country = null;
            if (string.IsNullOrEmpty(strValue))
            {
                return (country);
            }

            country = FromAbbreviation(strValue);
            if (country != null)
            {
                return (country);
            }

            country = FromAlpha3(strValue);
            if (country != null)
            {
                return (country);
            }
            country = FromCountryName(strValue);
            if (country != null)
            {
                return (country);
            }

            try
            {
                int Iso = System.Convert.ToInt32(strValue);
                country = FromCountryIso(Iso);
            }
            catch
            {
                country = null;
            }
            return (country);
        }

        public static List<int?> CountryCodeList(string stringList)
        {
            List<int?> lst = new List<int?>();
            try
            {
                if (!string.IsNullOrEmpty(stringList))
                {
                    string[] values = stringList.Split(';');
                    foreach (string strCode in values)
                    {
                        if (!string.IsNullOrEmpty(strCode))
                        {
                            if (strCode.IsInteger())
                            {
                                int iIso = System.Convert.ToInt32(strCode);
                                Country country = FromCountryIso(iIso);
                                if (country != null)
                                {
                                    lst.Add(country.ISO);
                                }
                            }
                            else
                            {
                                Country country = TryGetCountry(strCode);
                                if (country != null)
                                {
                                    lst.Add(country.ISO);
                                }
                            }
                        }
                    }
                }
            }
            catch (Exception ex)
            {
                DSLogger.Log(0, ex);
            }
            return (lst);
        }

        public static Country Parse(string strValue)
        {
            if (int.TryParse(strValue, out int iso))
            {
                return FromCountryIso(iso);
            }
            else
            {
                return FromAbbreviation(strValue) ?? FromAlpha3(strValue) ?? FromCountryName(strValue);
            }
        }
    }

    /// <remarks/>
    [System.Xml.Serialization.XmlTypeAttribute(AnonymousType = true)]
    [DataContract]
    public class Country : IEquatable<Country>, IComparable<Country>
    {
        private Int64 idField;

        private string? nameField;

        private string? abbreviationField;
        private string? alpha3;
        private string? cultureInfoCode;

        private int isoField = 0;

        private DateTime createdField;
        private DateTime updatedField;

        public Country()
        {
        }

        public Country(Country other)
        {
            this.Abbreviation = other.Abbreviation;
            this.Alpha3 = other.Alpha3;
            this.Created = other.Created;
            this.ID = other.ID;
            this.ISO = other.ISO;
            this.Name = other.Name;
            this.AlternativeName = other.AlternativeName;
            this.CultureInfoCode = other.CultureInfoCode;
            this.Updated = other.Updated;
            this.Region = other.Region;
            this.subregion = other.subregion;
            this.intermediateregion = other.intermediateregion;
        }

        public Country(DSXML xmlCountry)
        {
            this.ID = xmlCountry.AttributeInt64Value("ID");
            this.Name = xmlCountry.Attribute("Name");
            this.AlternativeName = xmlCountry.Attribute("AlternativeName");
            this.Abbreviation = xmlCountry.Attribute("Abbreviation");
            this.Alpha3 = xmlCountry.Attribute("Alpha3");
            this.ISO = xmlCountry.AttributeInt32Value("ISO");
            this.CultureInfoCode = xmlCountry.Attribute("CultureInfoCode");
            this.Region = xmlCountry.Attribute("region");
            this.subregion = xmlCountry.Attribute("sub-region");
            this.intermediateregion = xmlCountry.Attribute("intermediate-region");
        }

        /// <remarks/>

        [DataMember]
        public Int64 ID
        {
            get
            {
                return this.idField;
            }
            set
            {
                this.idField = value;
            }
        }

        [DataMember]
        public String CultureInfoCode
        {
            get
            {
                return this.cultureInfoCode;
            }
            set
            {
                this.cultureInfoCode = value;
            }
        }

        [DataMember]
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

        [DataMember]
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

        /// <remarks/>

        [DataMember]
        public string Name
        {
            get
            {
                return this.nameField;
            }
            set
            {
                this.nameField = value;
            }
        }

        [DataMember]
        public string AlternativeName { get; set; }

        public string Key
        {
            get
            {
                return (Abbreviation);
            }
        }

        /// <remarks/>

        [DataMember]
        public string Abbreviation
        {
            get
            {
                return this.abbreviationField;
            }
            set
            {
                this.abbreviationField = value;
            }
        }

        [DataMember]
        public string Alpha3
        {
            get
            {
                return this.alpha3;
            }
            set
            {
                this.alpha3 = value;
            }
        }

        /// <remarks/>

        [DataMember]
        public int ISO
        {
            get
            {
                return this.isoField;
            }
            set
            {
                this.isoField = value;
            }
        }

        /// <remarks/>

        [DataMember]
        public string Region { get; set; }

        [DataMember]
        public string subregion { get; set; }

        [DataMember]
        public string intermediateregion { get; set; }

        public Country Clone()
        {
            try
            {
                Country c = new Country(this);
                return c;
            }
            catch (Exception Ex)
            {
                OTLogger.Error(Ex);
                throw;
            }
        }

        public DSXML ToXML(DSXML xmlParent, string name = "Country")
        {
            DSXML xmlCountry = xmlParent.AddChild(name);
            xmlCountry.Attribute("ID", ID);
            xmlCountry.Attribute("Name", Name);
            xmlCountry.Attribute("AlternativeName", AlternativeName);
            xmlCountry.Attribute("Abbreviation", Abbreviation);
            xmlCountry.Attribute("Alpha3", Alpha3);
            xmlCountry.Attribute("ISO", ISO);
            xmlCountry.Attribute("CultureInfoCode", CultureInfoCode);
            xmlCountry.Attribute("region", Region);
            xmlCountry.Attribute("sub-region", subregion);
            xmlCountry.Attribute("intermediate-region", intermediateregion);

            return (xmlCountry);
        }

        public override string ToString()
        {
            return this.ISO.ToString();
        }

        public static Country FromXML(DSXML xmlCountry)
        {
            Country country = new Country(xmlCountry);
            return (country);
        }

        //overriding Equals
        public override bool Equals(object? obj)
        {
            if (!(obj is Country))
            {
                return false;
            }
            if (obj == null)
            {
                return false;
            }
            Country otherCountry = (Country)obj;
            if (this.ISO == otherCountry.ISO)
            {
                return true;
            }
            else
            {
                return false;
            }
        }

        //overriding GetHashCode
        public override int GetHashCode()
        {
            return this.ISO.GetHashCode();
        }

        public bool Equals(Country? other)
        {
            if (other == null) return false;
            return (ISO == other.ISO);
        }

        public int CompareTo(Country? other)
        {
            if (other == null) return 1;
            return (Key.CompareTo(other.Key));
        }
    }
}