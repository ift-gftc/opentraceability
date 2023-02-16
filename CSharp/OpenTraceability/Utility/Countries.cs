using System.Collections.Concurrent;
using System.Data;
using System.Runtime.Serialization;
using System.Text;
using System.Xml.Linq;

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
            string? data = null;
            data = StaticData.ReadData("Countries.xml");
            XDocument xmlCountries = XDocument.Parse(data);
            foreach (XElement x in xmlCountries.Root.Elements())
            {
                Country country = new Country(x);
                _dirCountries.TryAdd(country.Abbreviation.ToUpper(), country);
                _dirNameCountries.TryAdd(country.Name.ToUpper(), country);
                if (!string.IsNullOrEmpty(country.Alpha3))
                {
                    _dirAlpha3Countries.TryAdd(country.Alpha3.ToUpper(), country);
                }
            }
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

        public string CultureInfoCode { get; set; } = string.Empty;

        public string Name { get; set; } = string.Empty;

        public string AlternativeName { get; set; } = string.Empty;

        public string Abbreviation { get; set; } = string.Empty;

        public string Alpha3 { get; set; } = string.Empty;

        public int ISO { get; set; }

        public Country()
        {
        }

        public Country(Country other)
        {
            this.Abbreviation = other.Abbreviation;
            this.Alpha3 = other.Alpha3;
            this.ISO = other.ISO;
            this.Name = other.Name;
            this.AlternativeName = other.AlternativeName;
            this.CultureInfoCode = other.CultureInfoCode;
        }

        public Country(XElement xmlCountry)
        {
            this.Name = xmlCountry.Attribute("Name")?.Value ?? string.Empty;
            this.AlternativeName = xmlCountry.Attribute("AlternativeName")?.Value ?? string.Empty;
            this.Abbreviation = xmlCountry.Attribute("Abbreviation")?.Value ?? string.Empty;
            this.Alpha3 = xmlCountry.Attribute("Alpha3")?.Value ?? string.Empty;
            if (int.TryParse(xmlCountry.Attribute("ISO")?.Value, out int iso))
            {
                this.ISO = iso;
            }
            this.CultureInfoCode = xmlCountry.Attribute("CultureInfoCode")?.Value ?? string.Empty;
        }

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

        public override string ToString()
        {
            return this.ISO.ToString();
        }

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
            return (ISO.CompareTo(other.ISO));
        }
    }
}