using OpenTraceability;
using System.Collections;
using System.ComponentModel;
using System.Reflection;
using System.Runtime.Serialization;

namespace OpenTraceability.Utility
{
    [DataContract]
    public class Measurement : IComparable<Measurement>, IComparable
    {
        protected double? _value;
        protected UOM _uom;

        static Measurement()
        {
        }

        public static Measurement EmptyMeasurement()
        {
            Measurement empty = new Measurement();
            empty._value = null;
            empty._uom = null;
            return (empty);
        }

        public static bool IsNullOrEmpty(Measurement measurement)
        {
            if (measurement == null)
            {
                return (true);
            }
            else if (UOM.IsNullOrEmpty(measurement.UoM))
            {
                return (true);
            }
            else
            {
                return (false);
            }
        }

        public Measurement()
        {
            _value = 0;
            _uom = new UOM();
        }

        public Measurement(DSXML xmlElement)
        {
            if (!xmlElement.IsNull)
            {
                _value = xmlElement.AttributeDoubleValueEx("Value");
                _uom = UOM.ParseFromName(xmlElement.Attribute("UoM"));
            }
            else
            {
                _value = null;
                _uom = new UOM();
            }
        }

        public Measurement(Measurement copyFrom)
        {
            if (copyFrom != null)
            {
                _value = copyFrom.Value;
                _uom = new UOM(copyFrom.UoM);
            }
            else
            {
                _value = null;
                _uom = new UOM();
            }
        }

        public Measurement(double? value, UOM unitCode)
        {
            _value = value;
            _uom = unitCode;
        }

        public Measurement(double? value, string unitCode)
        {
            _value = value;
            _uom = UOM.ParseFromName(unitCode);
        }

        public Measurement(Int32 value)
        {
            _value = value;
            _uom = null;
        }

        public bool IsNullOrEmpty()
        {
            if (_value.HasValue && _uom != null && !string.IsNullOrEmpty(this.UoM.Key))
            {
                return (false);
            }
            return (true);
        }

        public bool ShouldSerializeValue()
        {
            return true;
        }

        public void Add(Measurement measurement)
        {
            try
            {
                if (measurement != null)
                {
                    Value += measurement.Value;
                }
            }
            catch (Exception Ex)
            {
                OTLogger.Error(Ex);
                throw;
            }
        }

        public static Measurement operator +(Measurement left, Measurement right)
        {
            if (left.UoM.UnitDimension != right.UoM.UnitDimension)
            {
                throw new DSException($"All operands must be of the same unit dimension. Left UoM = ${left.UoM.UNCode} | Right UoM = ${right.UoM.UNCode}.");
            }
            if (left.IsNullOrEmpty() || right.IsNullOrEmpty())
            {
                return new Measurement(null, left.UoM);
            }
            double? rightValue = right.UoM.Convert(right.Value, left.UoM);
            double? sum = left.Value.Value + rightValue.Value;
            return (new Measurement(sum, left.UoM));
        }

        public static Measurement operator -(Measurement left, Measurement right)
        {
            if (left.UoM.UnitDimension != right.UoM.UnitDimension)
            {
                throw new DSException("All operands must be of the same unit dimension");
            }
            if (left.IsNullOrEmpty() || right.IsNullOrEmpty())
            {
                return new Measurement(null, left.UoM);
            }
            double? rightValue = right.UoM.Convert(right.Value, left.UoM);
            double? diff = left.Value.Value - rightValue.Value;
            return (new Measurement(diff, left.UoM));
        }

        public static Measurement operator *(Measurement left, double factor)
        {
            if (left.IsNullOrEmpty())
            {
                return new Measurement(null, left.UoM);
            }
            double newValue = left.Value.Value * factor;
            return (new Measurement(newValue, left.UoM));
        }

        public Measurement ToBase()
        {
            // if the UoM is NULL or EMPTY then we just return this
            if (String.IsNullOrWhiteSpace(this.UoM?.UNCode)) return this;

            // otherwise then we convert to base
            Measurement trBase = new Measurement();
            trBase.UoM = UOMS.GetBase(this.UoM);

            // lets make sure we looked up the base UoM
            if (String.IsNullOrWhiteSpace(this.UoM?.UNCode)) throw new NullReferenceException("Failed to look up base UoM. UNCode=" + this.UoM.UNCode);

            trBase.Value = UoM.Convert(this.Value, trBase.UoM);
            trBase.Value = trBase.Value.Round();
            return (trBase);
        }

        public Measurement ToSystem(UnitSystem unitSystem)
        {
            Measurement trBase = new Measurement();
            trBase.UoM = unitSystem.GetSystemUOM(this.UoM);
            trBase.Value = UoM.Convert(this.Value, trBase.UoM);
            trBase.Value = trBase.Value.Round();
            return (trBase);
        }

        public Measurement ToSystem(UnitSystem unitSystem, string subGroup)
        {
            Measurement trBase = new Measurement();
            trBase.UoM = unitSystem.GetSystemUOM(this.UoM, subGroup);
            trBase.Value = UoM.Convert(this.Value, trBase.UoM);
            trBase.Value = trBase.Value.Round();
            return (trBase);
        }

        public Measurement ConvertTo(string uomStr)
        {
            if (string.IsNullOrEmpty(uomStr))
            {
                return (this);
            }
            Measurement trBase = new Measurement();
            trBase.UoM = UOMS.GetUOMFromUNCode(uomStr);
            if (trBase.UoM == null)
            {
                return (this);
            }
            trBase.Value = UoM.Convert(this.Value, trBase.UoM);
            trBase.Value = trBase.Value.Round();
            return (trBase);
        }

        public Measurement ConvertTo(UOM uom)
        {
            if (uom == null)
            {
                return (this);
            }
            Measurement trBase = new Measurement();
            trBase.UoM = uom;
            trBase.Value = UoM.Convert(this.Value, trBase.UoM);
            trBase.Value = trBase.Value.Round();
            return (trBase);
        }

        [DataMember]
        public double? Value
        {
            get
            {
                return (_value);
            }
            set
            {
                _value = value;
            }
        }

        [DataMember]
        [DefaultValue(null)]
        public UOM UoM
        {
            get
            {
                return (_uom);
            }
            set
            {
                _uom = value;
            }
        }

        public override string ToString()
        {
            try
            {
                if (this.IsNullOrEmpty())
                {
                    return ("");
                }

                string str = this.Value.DSToString();
                str += " " + this.UoM.UNCode;
                return str;
            }
            catch (Exception Ex)
            {
                OTLogger.Error(Ex);
                throw;
            }
        }

        public string ToStringEx()
        {
            try
            {
                if (this.IsNullOrEmpty())
                {
                    return ("");
                }

                string str = this.Value.DSToString();
                str += " " + this.UoM.Abbreviation;
                return str;
            }
            catch (Exception Ex)
            {
                OTLogger.Error(Ex);
                throw;
            }
        }

        public string GetUniquenessKey(int iVersion)
        {
            try
            {
                if (this.Value == null && this.UoM == null)
                {
                    return "";
                }
                else
                {
                    return (this.ToBase().ToString()).Trim();
                }
            }
            catch (Exception Ex)
            {
                OTLogger.Error(Ex);
                throw;
            }
        }

        public static Measurement Parse(string strValue)
        {
            try
            {
                if (String.IsNullOrWhiteSpace(strValue))
                {
                    Measurement emptyMeasurement = new Measurement();
                    return (emptyMeasurement);
                }

                strValue = strValue.Trim();
                string[] strParts = strValue.Split(' ');
                if (strParts.Count() != 2)
                {
                    throw new DSException("Invalid Measurment string encountered, value=" + strValue + ". String must have a value and the UOM UN Code.");
                }
                string numberStr = strParts[0];
                string uomStr = strParts[1];

                numberStr = numberStr.Trim();
                uomStr = uomStr.Trim();

                List<UOM> uoms = UOMS.UOMList;

                double dblValue = double.Parse(numberStr);
                UOM uom = UOMS.GetUOMFromUNCode(uomStr);
                if (uom == null)
                {
                    uom = uoms.Find(u => u.Abbreviation.ToLower() == uomStr.ToLower() || uomStr.ToLower() == u.Name.ToLower());
                }

                if (uom == null && !String.IsNullOrWhiteSpace(uomStr))
                {
                    throw new Exception(String.Format("Failed to recognize UoM while parsing a TRMeasurement from a string. String={0}, Value={1}, UoM={2}", strValue, numberStr, uomStr));
                }

                Measurement measurement = new Measurement();
                measurement.Value = dblValue;
                measurement.UoM = uom;
                return measurement;
            }
            catch (Exception Ex)
            {
                OTLogger.Error(Ex);
                throw;
            }
        }

        /// <summary>
        /// Tries to parse the Measurement.
        /// </summary>
        /// <param name="strValue"></param>
        /// <returns></returns>
        public static Measurement TryParse(string strValue)
        {
            Measurement measure = null;
            try
            {
                if (!string.IsNullOrEmpty(strValue))
                {
                    measure = Measurement.Parse(strValue);
                }
            }
            catch (Exception Ex)
            {
#if DEBUG
                DSLogger.Log(5, "Failed to parse a measurement. strValue=" + strValue);
                DSLogger.Log(5, Ex);
#endif
            }
            return measure;
        }

        /// <summary>
        /// This method will reflect through an object looking for all TRMeasurement properties
        /// in an effort to convert all of them to the default unit system. This is useful for when
        /// editing an object, we can run it through this method so that when it's loaded into the
        /// editor it will be in the default unit system for that account.
        /// </summary>
        /// <param name="obj"></param>
        /// <param name="unitSystem"></param>
        public static void ConvertMeasurementsToUnitSystem(object obj, UnitSystem unitSystem)
        {
            try
            {
                if (obj is IList)
                {
                    foreach (object value in (IList)obj)
                    {
                        ConvertMeasurementsToUnitSystem(value, unitSystem);
                    }
                }
                else
                {
                    foreach (PropertyInfo pInfo in obj.GetType().GetProperties().Where(p => p.GetIndexParameters().Length == 0))
                    {
                        if (pInfo.PropertyType != typeof(string)
                            && !pInfo.PropertyType.IsValueType
                            && pInfo.GetCustomAttribute(typeof(DataMemberAttribute)) != null)
                        {
                            object propValue = pInfo.GetValue(obj);
                            if (propValue != null)
                            {
                                if (propValue is Measurement)
                                {
                                    Measurement measurement = (Measurement)propValue;
                                    if (!Measurement.IsNullOrEmpty(measurement))
                                    {
                                        string subGroup = "Medium";
                                        DSMeasurementAttribute measureAttribute = pInfo.GetCustomAttribute<DSMeasurementAttribute>();
                                        if (measureAttribute != null)
                                        {
                                            subGroup = measureAttribute.SubGroup;
                                        }
                                        measurement = measurement.ToSystem(unitSystem, subGroup);
                                        pInfo.SetValue(obj, measurement);
                                    }
                                }
                                else if (propValue is IList)
                                {
                                    foreach (object value in (IList)propValue)
                                    {
                                        if (value != null)
                                        {
                                            ConvertMeasurementsToUnitSystem(value, unitSystem);
                                        }
                                    }
                                }
                                else if (!pInfo.PropertyType.IsValueType && pInfo.PropertyType != typeof(string))
                                {
                                    ConvertMeasurementsToUnitSystem(propValue, unitSystem);
                                }
                            }
                        }
                    }
                }
            }
            catch (Exception Ex)
            {
                OTLogger.Error(Ex);
                throw;
            }
        }

        public static bool operator ==(Measurement lhs, Measurement rhs)
        {
            if (ReferenceEquals(lhs, rhs))
            {
                return (true);
            }
            if (ReferenceEquals(lhs, null))
            {
                return (false);
            }
            int iCompare = lhs.CompareTo(rhs);
            if (iCompare == 0)
            {
                return (true);
            }
            else
            {
                return (false);
            }
        }

        public static bool operator !=(Measurement lhs, Measurement rhs)
        {
            if (lhs == rhs)
            {
                return (false);
            }
            else
            {
                return (true);
            }
        }

        public static bool operator >(Measurement lhs, Measurement rhs)
        {
            if (ReferenceEquals(lhs, rhs))
            {
                return (false);
            }
            if (ReferenceEquals(lhs, null))
            {
                return (true);
            }

            int iCompare = lhs.CompareTo(rhs);
            if (iCompare == 1)
            {
                return (true);
            }
            else
            {
                return (false);
            }
        }

        public static bool operator <(Measurement lhs, Measurement rhs)
        {
            if (ReferenceEquals(lhs, rhs))
            {
                return (false);
            }
            if (ReferenceEquals(lhs, null))
            {
                return (true);
            }

            int iCompare = lhs.CompareTo(rhs);
            if (iCompare == -1)
            {
                return (true);
            }
            else
            {
                return (false);
            }
        }

        public static bool operator <=(Measurement lhs, Measurement rhs)
        {
            if (ReferenceEquals(lhs, rhs))
            {
                return (true);
            }
            if (ReferenceEquals(lhs, null))
            {
                return (false);
            }
            int iCompare = lhs.CompareTo(rhs);
            if (iCompare != 1)
            {
                return (true);
            }
            else
            {
                return (false);
            }
        }

        public static bool operator >=(Measurement lhs, Measurement rhs)
        {
            if (ReferenceEquals(lhs, rhs))
            {
                return (true);
            }
            if (ReferenceEquals(lhs, null))
            {
                return (false);
            }
            int iCompare = lhs.CompareTo(rhs);
            if (iCompare != -1)
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
            if (!ReferenceEquals(obj, null))
            {
                if (obj is Measurement)
                {
                    Measurement other = (Measurement)obj;
                    if (Value == other.Value && UoM == other.UoM)
                    {
                        return (true);
                    }
                }
            }
            return (false);
        }

        public int CompareTo(object? obj)
        {
            if (!ReferenceEquals(obj, null))
            {
                return (1);
            }
            else if (obj is Measurement)
            {
                Measurement other = (Measurement)obj;
                return (CompareTo(other));
            }
            else
            {
                throw new Exception("object is not of type TRMeasurement");
            }
        }

        public int CompareTo(Measurement? other)
        {
            if (ReferenceEquals(other, null))
            {
                return 1;
            }

            //If this instance has a value;
            if (this.Value.HasValue)
            {
                if (!other.Value.HasValue)
                {
                    return 1;
                }
                Measurement thisBase = this.ToBase();
                Measurement otherBase = other.ToBase();
                if (thisBase.Value == otherBase.Value)
                {
                    return (0);
                }
                else if (thisBase.Value < otherBase.Value)
                {
                    return (-1);
                }
                else
                {
                    return (1);
                }
            }
            else if (other.Value.HasValue)
            {
                return (-1);
            }
            else
            {
                return (0);
            }
        }

        public override int GetHashCode()
        {
            return (Value.GetHashCode() + (UoM?.GetHashCode() ?? 0));
        }

        public DSXML ToXML(DSXML xmlParent, string Name)
        {
            DSXML xmlElement = xmlParent.AddChild(Name);
            xmlElement.Attribute("Value", Value);
            xmlElement.Attribute("UoM", UoM.UNCode);
            xmlElement.Attribute("UoMAbbrev", UoM.Abbreviation);
            return (xmlElement);
        }

        public static Measurement FromXML(DSXML xmlElement)
        {
            if (xmlElement != null && !xmlElement.IsNull)
            {
                double? val = xmlElement.AttributeDoubleValueEx("Value");
                string strUOM = xmlElement.Attribute("UoM");
                UOM uom = UOM.LookUpFromUNCode(strUOM);
                return new Measurement(val, uom);
            }
            else
            {
                return (null);
            }
        }
    }

    public class DSMeasurementAttribute : System.Attribute
    {
        public string? SubGroup { get; set; }
    }
}