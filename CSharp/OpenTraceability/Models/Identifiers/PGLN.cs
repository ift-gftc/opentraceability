using DSUtil;
using System;
using System.Collections.Generic;
using System.Linq;
using System.Runtime.Serialization;
using System.Text;
using GS1.Interfaces.Models.Identifiers;
using DSUtil.Extensions;

namespace OpenTraceability.Models.Identifiers
{
    /// <summary>
    /// Party Global Location Number - Used for identifying Trading Partners, Business Departments, 
    /// Business Regions, Business Groups, and Private Trading Partners in Full Chain Traceability.
    /// </summary>
    [DataContract]
    public class PGLN : IEquatable<PGLN>, IComparable<PGLN>, PGLN
    {
        private string _pglnStr;

        public PGLN()
        {

        }

        public PGLN(string pglnStr)
        {
            string error = DetectPGLNIssue(pglnStr);
            if (!string.IsNullOrWhiteSpace(error))
            {
                throw new Exception($"The PGLN {pglnStr} is not valid. {error}");
            }
            this._pglnStr = pglnStr;
        }

        public string ToDigitalLinkURL(string baseURL)
        {
            try
            {
                return $"{baseURL}/pgln/{this._pglnStr}";
            }
            catch (Exception Ex)
            {
                OTLogger.Error(Ex);
                throw;
            }
        }

        public static bool IsPGLN(string pglnStr)
        {
            try
            {
                if (PGLN.DetectPGLNIssue(pglnStr) == null)
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
        public static string DetectPGLNIssue(string pglnStr)
        {
            try
            {
                if (string.IsNullOrEmpty(pglnStr))
                {
                    return ("PGLN is NULL or EMPTY.");
                }
                else if (pglnStr.Contains(" "))
                {
                    return ("PGLN cannot contain spaces.");
                }
                else if (pglnStr.IsURICompatibleChars() == false)
                {
                    return ("The PGLN contains non-compatiable characters for a URI.");
                }
                else if (pglnStr.Length == 13 && pglnStr.IsOnlyDigits())
                {
                    // validate the checksum
                    char checksum = GS1Util.CalculateGLN13CheckSum(pglnStr);
                    if (checksum != pglnStr.ToCharArray().Last())
                    {
                        return string.Format("The check sum did not calculate correctly. The expected check sum was {0}. " +
                            "Please make sure to validate that you typed the PGLN correctly. It's possible the check sum " +
                            "was typed correctly but another number was entered wrong.", checksum);
                    }

                    return (null);
                }
                else if (pglnStr.StartsWith("urn:") && pglnStr.Contains(":party:"))
                {
                    return (null);
                }
                else if (pglnStr.StartsWith("urn:") && (pglnStr.Contains(":id:pgln:") || pglnStr.Contains(":id:sgln:")))
                {
                    string[] pieces = pglnStr.Split(':').Last().Split(".");
                    if (pieces.Count() < 2)
                    {
                        return ("This is supposed to contain the company prefix and the location code. Did not find these two pieces.");
                    }
                    string lastPiece = pieces[0] + pieces[1];
                    if (!lastPiece.IsOnlyDigits())
                    {
                        return ("This is supposed to be a GS1 PGLN based on the System Prefix and " +
                            "Data Type Prefix. That means the Company Prefix and Serial Numbers " +
                            "should only be digits. Found non-digit characters in the Company Prefix " +
                            "or Serial Number.");
                    }
                    else if (lastPiece.Length != 12)
                    {
                        return ("This is supposed to be a GS1 PGLN based on the System Prefix and Data Type " +
                            "Prefix. That means the Company Prefix and Serial Numbers should contain a maximum " +
                            "total of 12 digits between the two. The total number of digits when combined " +
                            "is " + lastPiece.Length + ".");
                    }

                    return (null);
                }
                else
                {
                    return ("The PGLN is not in a valid EPCIS URI format or in GS1 (P)GLN-13 format.");
                }
            }
            catch (Exception Ex)
            {
                OTLogger.Error(Ex);
                throw;
            }
        }
        public static bool TryParse(string pglnStr, out PGLN pgln, out string error)
        {
            try
            {
                error = PGLN.DetectPGLNIssue(pglnStr);
                if (string.IsNullOrWhiteSpace(error))
                {
                    pgln = new PGLN(pglnStr);
                    return true;
                }
                else
                {
                    pgln = null;
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
            PGLN pgln = new PGLN(this.ToString());
            return pgln;
        }

        #region Overrides
        public static bool operator ==(PGLN obj1, PGLN obj2)
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
        public static bool operator !=(PGLN obj1, PGLN obj2)
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

                return this.IsEquals((PGLN)obj);
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
                return this._pglnStr.ToLower();
            }
            catch (Exception Ex)
            {
                OTLogger.Error(Ex);
                throw;
            }
        }
        #endregion

        #region IEquatable
        public bool Equals(PGLN pgln)
        {
            try
            {
                if (Object.ReferenceEquals(null, pgln))
                {
                    return false;
                }

                if (Object.ReferenceEquals(this, pgln))
                {
                    return true;
                }

                return this.IsEquals(pgln);
            }
            catch (Exception Ex)
            {
                OTLogger.Error(Ex);
                throw;
            }
        }
        private bool IsEquals(PGLN pgln)
        {
            try
            {
                if (pgln == null) throw new ArgumentNullException(nameof(pgln));

                if (this.ToString().ToLower() == pgln.ToString().ToLower())
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

        public bool Equals(PGLN pgln)
        {
            try
            {
                if (Object.ReferenceEquals(null, pgln))
                {
                    return false;
                }

                if (Object.ReferenceEquals(this, pgln))
                {
                    return true;
                }

                return this.IsEquals(pgln);
            }
            catch (Exception Ex)
            {
                OTLogger.Error(Ex);
                throw;
            }
        }
        private bool IsEquals(PGLN pgln)
        {
            try
            {
                if (pgln == null) throw new ArgumentNullException(nameof(pgln));

                if (this.ToString() == pgln.ToString())
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
        #endregion

        #region IComparable 
        public int CompareTo(PGLN pgln)
        {
            try
            {
                if (pgln == null) throw new ArgumentNullException(nameof(pgln));

                long myInt64Hash = this.ToString().ToLower().GetInt64HashCode();
                long otherInt64Hash = pgln.ToString().ToLower().GetInt64HashCode();

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
        #endregion
    }
}
