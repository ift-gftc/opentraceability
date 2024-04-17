using Newtonsoft.Json.Linq;
using OpenTraceability;
using System;
using System.Collections;
using System.Collections.Generic;
using System.ComponentModel;
using System.Linq;
using System.Reflection;
using System.Runtime.Serialization;
using System.Xml.Linq;

namespace OpenTraceability.Utility
{
    [DataContract]
    public class Measurement : IComparable<Measurement>, IComparable
    {
        public double Value { get; set; }

        public UOM UoM { get; set; }

        static Measurement()
        {
        }

        public Measurement()
        {
            Value = 0;
            UoM = new UOM();
        }

        public Measurement(XElement xmlElement)
        {
            Value = double.Parse(xmlElement.Attribute("Value")?.Value ?? string.Empty);
            UoM = UOM.ParseFromName(xmlElement.Attribute("UoM")?.Value ?? string.Empty);
        }

        public Measurement(Measurement copyFrom)
        {
            Value = copyFrom.Value;
            UoM = new UOM(copyFrom.UoM);
        }

        public Measurement(double value, UOM unitCode)
        {
            Value = value;
            UoM = unitCode;
        }

        public Measurement(double value, string unitCode)
        {
            Value = value;
            UoM = UOM.ParseFromName(unitCode);
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
                throw new Exception($"All operands must be of the same unit dimension. Left UoM = ${left.UoM.UNCode} | Right UoM = ${right.UoM.UNCode}.");
            }

            double rightValue = right.UoM.Convert(right.Value, left.UoM);
            double sum = left.Value + rightValue;

            return (new Measurement(sum, left.UoM));
        }

        public static Measurement operator -(Measurement left, Measurement right)
        {
            if (left.UoM.UnitDimension != right.UoM.UnitDimension)
            {
                throw new Exception("All operands must be of the same unit dimension");
            }

            double rightValue = right.UoM.Convert(right.Value, left.UoM);
            double diff = left.Value - rightValue;
            return (new Measurement(diff, left.UoM));
        }

        public static Measurement operator *(Measurement left, double factor)
        {
            double newValue = left.Value * factor;
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

        public override string ToString()
        {
            try
            {
                string str = this.Value.ToString();
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
                string str = this.Value.ToString();
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
                    throw new Exception("Invalid Measurment string encountered, value=" + strValue + ". String must have a value and the UOM UN Code.");
                }
                string numberStr = strParts[0];
                string uomStr = strParts[1];

                numberStr = numberStr.Trim();
                uomStr = uomStr.Trim();

                List<UOM> uoms = UOMS.List;

                double dblValue = double.Parse(numberStr);
                UOM uom = UOMS.GetUOMFromUNCode(uomStr);
                if (uom == null)
                {
                    uom = uoms.Find(u => u.Abbreviation.ToLower() == uomStr.ToLower() || uomStr.ToLower() == u.Name.ToLower());
                }

                if (uom == null)
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
                Exception exception = new Exception("Failed to parse measurement. strValue = " + strValue, Ex);
                OTLogger.Error(exception);
#endif
            }
            return measure;
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

        public override bool Equals(object obj)
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

        public int CompareTo(object obj)
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

        public int CompareTo(Measurement other)
        {
            if (ReferenceEquals(other, null))
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

        public override int GetHashCode()
        {
            return (Value.GetHashCode() + (UoM?.GetHashCode() ?? 0));
        }
    }
}