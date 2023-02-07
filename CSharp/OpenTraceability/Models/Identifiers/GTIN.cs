using OpenTraceability.Utility;
using System.Runtime.Serialization;

namespace OpenTraceability.Models.Identifiers
{
    [DataContract]
    public class GTIN : IEquatable<GTIN>, IComparable<GTIN>
    {
        private string _gtinStr;

        public GTIN()
        {
        }

        public GTIN(string gtinStr)
        {
            try
            {
                string error = GTIN.DetectGTINIssue(gtinStr);
                if (!string.IsNullOrWhiteSpace(error))
                {
                    throw new Exception($"The GTIN {gtinStr} is invalid. {error}");
                }
                this._gtinStr = gtinStr;
            }
            catch (Exception Ex)
            {
                OTLogger.Error(Ex);
                throw;
            }
        }

        public static bool TryParse(string gtinStr, out GTIN gtin, out string error)
        {
            try
            {
                error = GTIN.DetectGTINIssue(gtinStr);
                if (string.IsNullOrWhiteSpace(error))
                {
                    gtin = new GTIN(gtinStr);
                    return true;
                }
                else
                {
                    gtin = null;
                    return false;
                }
            }
            catch (Exception Ex)
            {
                OTLogger.Error(Ex);
                throw;
            }
        }

        public string ToDigitalLinkURL(string baseURL)
        {
            try
            {
                return $"{baseURL}/gln/{this._gtinStr}";
            }
            catch (Exception Ex)
            {
                OTLogger.Error(Ex);
                throw;
            }
        }

        /// <summary>
        /// This function will analyze a GTIN and try to return feedback with why a GTIN is not valid.
        /// </summary>
        /// <param name="gtinStr">The GTIN string.</param>
        /// <returns>An error if a problem is detected, otherwise returns NULL if no problem detected and the GTIN is valid.</returns>
        public static string DetectGTINIssue(string gtinStr)
        {
            try
            {
                if (string.IsNullOrEmpty(gtinStr))
                {
                    return ("GTIN is NULL or EMPTY.");
                }
                else if (gtinStr.IsURICompatibleChars() == false)
                {
                    return ("The GTIN contains non-compatiable characters for a URI.");
                }
                else if (gtinStr.Contains(" "))
                {
                    return ("GTIN cannot contain spaces.");
                }
                else if (gtinStr.Length == 14 && gtinStr.IsOnlyDigits())
                {
                    // validate the checksum
                    char checksum = GS1Util.CalculateGTIN14CheckSum(gtinStr);
                    if (checksum != gtinStr.ToCharArray().Last())
                    {
                        return string.Format("The check sum did not calculate correctly. The expected check sum was {0}. " +
                            "Please make sure to validate that you typed the GTIN correctly. It's possible the check sum " +
                            "was typed correctly but another number was entered wrong.", checksum);
                    }

                    return (null);
                }
                if (gtinStr.StartsWith("urn:") && gtinStr.Contains(":product:class:"))
                {
                    return (null);
                }
                else if (gtinStr.StartsWith("urn:") && gtinStr.Contains(":idpat:sgtin:"))
                {
                    string lastPiece = gtinStr.Split(':').Last().Replace(".", "");
                    if (!lastPiece.IsOnlyDigits())
                    {
                        return ("This is supposed to be a GS1 GTIN based on the System Prefix and " +
                            "Data Type Prefix. That means the Company Prefix and Serial Numbers " +
                            "should only be digits. Found non-digit characters in the Company Prefix " +
                            "or Serial Number.");
                    }
                    else if (lastPiece.Length != 13)
                    {
                        return ("This is supposed to be a GS1 GTIN based on the System Prefix and Data Type " +
                            "Prefix. That means the Company Prefix and Serial Numbers should contain a maximum " +
                            "total of 13 digits between the two. The total number of digits when combined " +
                            "is " + lastPiece.Length + ".");
                    }

                    return (null);
                }
                else
                {
                    return ("The GTIN is not in a valid EPCIS URI format or in GS1 GTIN-14 format.");
                }
            }
            catch (Exception Ex)
            {
                Exception exception = new Exception("Failed to detect GTIN Issues. GTIN=" + gtinStr, Ex);
                OTLogger.Error(exception);
                throw;
            }
        }

        /// <summary>
        /// This function detects if the GTIN is a valid GTIN str or not.
        /// </summary>
        /// <param name="gtinStr"></param>
        /// <returns></returns>
        public static bool IsGTIN(string gtinStr)
        {
            try
            {
                if (DetectGTINIssue(gtinStr) == null)
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

        public object Clone()
        {
            GTIN gtin = new GTIN(this.ToString());
            return gtin;
        }

        #region Overrides

        public static bool operator ==(GTIN obj1, GTIN obj2)
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

                return obj1.Equals(obj2);
            }
            catch (Exception Ex)
            {
                OTLogger.Error(Ex);
                throw;
            }
        }

        public static bool operator !=(GTIN obj1, GTIN obj2)
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

                return !obj1.Equals(obj2);
            }
            catch (Exception Ex)
            {
                OTLogger.Error(Ex);
                throw;
            }
        }

        public override bool Equals(object obj)
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

                return this.IsEquals((GTIN)obj);
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
                return this._gtinStr.ToLower();
            }
            catch (Exception Ex)
            {
                OTLogger.Error(Ex);
                throw;
            }
        }

        #endregion Overrides

        #region IEquatable<GTIN>

        public bool Equals(GTIN? gtin)
        {
            try
            {
                if (Object.ReferenceEquals(null, gtin))
                {
                    return false;
                }

                if (Object.ReferenceEquals(this, gtin))
                {
                    return true;
                }

                return this.IsEquals(gtin);
            }
            catch (Exception Ex)
            {
                OTLogger.Error(Ex);
                throw;
            }
        }

        private bool IsEquals(GTIN? gtin)
        {
            try
            {
                if (Object.ReferenceEquals(null, gtin))
                {
                    return false;
                }

                if (this.ToString().ToLower() == gtin.ToString().ToLower())
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

        #endregion IEquatable<GTIN>

        #region IComparable

        public int CompareTo(GTIN? gtin)
        {
            try
            {
                if (Object.ReferenceEquals(null, gtin))
                {
                    throw new ArgumentNullException(nameof(gtin));
                }

                long myInt64Hash = this.ToString().GetInt64HashCode();
                long otherInt64Hash = gtin.ToString().GetInt64HashCode();

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
    }
}