package models.identifiers

import models.identifiers.*
import java.lang.reflect.Type

//[JsonConverter(typeof(EPCConverter))]
class EPC {

    internal var _epcStr: String = ""

    lateinit var Type: EPCType
    var GTIN: GTIN? = null
    var SerialLotNumber: String? = null


    constructor(epcStr: String?) {
        TODO("Not yet implemented")
    }

    constructor(type: EPCType, gtin: GTIN, lotOrSerial: String) {
        TODO("Not yet implemented")
    }

    companion object {
        fun DetectEPCIssue(epcStr: String?): String? {
            TODO("Not yet implemented")
        }

        fun TryParse(epcStr: String?, epc: EPC?, error: String?): Boolean {
            TODO("Not yet implemented")
        }
    }


    /*
        /// <summary>
        /// This method will perform a matching process with the EPC if both are either a Class/Instance type EPC.
        /// If both EPCs are equal, it returns TRUE.
        /// If the source contains a "*" for the serial/lot number, and the source and the target have matching GTINs, then it returns TRUE.
        /// </summary>
        public bool Matches(EPC targetEPC)
        {
            if (this.Equals(targetEPC))
            {
                return true;
            }
            else if (this.SerialLotNumber == "*" && this.GTIN == targetEPC.GTIN)
            {
                return true;
            }
            return false;
        }

        public object Clone()
        {
            EPC epc = new EPC(this.ToString());
            return epc;
        }

        #region Overrides

        public static bool operator ==(EPC? obj1, EPC? obj2)
        {
            try
            {
                if (Object.ReferenceEquals(null, obj1) && Object.ReferenceEquals(null, obj2))
                {
                    return true;
                }

                if (!Object.ReferenceEquals(null, obj1) && Object.ReferenceEquals(null, obj2))
                {
                    return false;
                }

                if (Object.ReferenceEquals(null, obj1) && !Object.ReferenceEquals(null, obj2))
                {
                    return false;
                }

                if (obj1 == null)
                {
                    return false;
                }

                return obj1.Equals(obj2);
            }
            catch (Exception Ex)
            {
                OTLogger.Error(Ex);
                throw;
            }
        }

        public static bool operator !=(EPC? obj1, EPC? obj2)
        {
            try
            {
                if (Object.ReferenceEquals(null, obj1) && Object.ReferenceEquals(null, obj2))
                {
                    return false;
                }

                if (!Object.ReferenceEquals(null, obj1) && Object.ReferenceEquals(null, obj2))
                {
                    return true;
                }

                if (Object.ReferenceEquals(null, obj1) && !Object.ReferenceEquals(null, obj2))
                {
                    return true;
                }

                if (obj1 == null)
                {
                    return true;
                }

                return !obj1.Equals(obj2);
            }
            catch (Exception Ex)
            {
                OTLogger.Error(Ex);
                throw;
            }
        }

        public override bool Equals(object? obj)
        {
            try
            {
                if (Object.ReferenceEquals(null, obj))
                {
                    return false;
                }

                if (Object.ReferenceEquals(this, obj))
                {
                    return true;
                }

                if (obj.GetType() != this.GetType())
                {
                    return false;
                }

                return this.IsEquals((EPC)obj);
            }
            catch (Exception Ex)
            {
                OTLogger.Error(Ex);
                throw;
            }
        }

        public override int GetHashCode()
        {
            try
            {
                int hash = this.ToString().GetInt32HashCode();
                return hash;
            }
            catch (Exception Ex)
            {
                OTLogger.Error(Ex);
                throw;
            }
        }

        public override string ToString()
        {
            try
            {
                return _epcStr.ToLower();
            }
            catch (Exception Ex)
            {
                OTLogger.Error(Ex);
                throw;
            }
        }

        #endregion Overrides

        #region IEquatable<EPC>

        public bool Equals(EPC? epc)
        {
            try
            {
                if (Object.ReferenceEquals(null, epc))
                {
                    return false;
                }

                if (Object.ReferenceEquals(this, epc))
                {
                    return true;
                }

                return this.IsEquals(epc);
            }
            catch (Exception Ex)
            {
                OTLogger.Error(Ex);
                throw;
            }
        }

        private bool IsEquals(EPC? epc)
        {
            try
            {
                if (Object.ReferenceEquals(null, epc))
                {
                    return false;
                }

                if (this.ToString().ToLower() == epc.ToString().ToLower())
                {
                    return true;
                }
                else
                {
                    return false;
                }
            }
            catch (Exception Ex)
            {
                OTLogger.Error(Ex);
                throw;
            }
        }

        #endregion IEquatable<EPC>

        #region IComparable

        public int CompareTo(EPC epc)
        {
            try
            {
                if (Object.ReferenceEquals(null, epc))
                {
                    throw new ArgumentNullException(nameof(epc));
                }

                long myInt64Hash = this.ToString().GetInt64HashCode();
                long otherInt64Hash = epc.ToString().GetInt64HashCode();

                if (myInt64Hash > otherInt64Hash) return -1;
                if (myInt64Hash == otherInt64Hash) return 0;

                return 1;
            }
            catch (Exception Ex)
            {
                OTLogger.Error(Ex);
                throw;
            }
        }

        #endregion IComparable
     */

}
