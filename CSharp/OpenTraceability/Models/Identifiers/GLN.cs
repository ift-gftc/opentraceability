using Newtonsoft.Json;
using OpenTraceability.Utility;
using System.Runtime.Serialization;

namespace OpenTraceability.Models.Identifiers
{
    /// <summary>
    /// Global Location Number - used for identifying SCE's in Full Chain Traceability.
    /// </summary>
    [DataContract]
    [JsonConverter(typeof(GLNConverter))]
    public class GLN : IEquatable<GLN>, IComparable<GLN>
    {
        private string _glnStr = string.Empty;

        public GLN()
        {

        }

        public GLN(string glnStr)
        {
            try
            {
                string error = GLN.DetectGLNIssue(glnStr);
                if (!string.IsNullOrWhiteSpace(error))
                {
                    throw new Exception($"The GLN {glnStr} is invalid. {error}");
                }
                this._glnStr = glnStr;
            }
            catch (Exception Ex)
            {
                OTLogger.Error(Ex);
                throw;
            }
        }

        public bool IsGS1PGLN()
        {
            return (_glnStr.Contains(":id:sgln:"));
        }

        public string ToDigitalLinkURL()
        {
            try
            {
                if (IsGS1PGLN())
                {
                    string[] gtinParts = _glnStr.Split(':').Last().Split('.');
                    string pgln = gtinParts[0] + gtinParts[1];
                    pgln = pgln + GS1Util.CalculateGLN13CheckSum(pgln);
                    return $"414/{pgln}";
                }
                else
                {
                    return $"414/{this._glnStr}";
                }
            }
            catch (Exception Ex)
            {
                OTLogger.Error(Ex);
                throw;
            }
        }

        public static bool IsGLN(string glnStr)
        {
            try
            {
                if (DetectGLNIssue(glnStr) == null)
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

        public static string? DetectGLNIssue(string glnStr)
        {
            try
            {
                if (string.IsNullOrEmpty(glnStr))
                {
                    return ("The GLN is NULL or EMPTY.");
                }
                else if (glnStr.IsURICompatibleChars() == false)
                {
                    return ("The GLN contains non-compatiable characters for a URI.");
                }
                else if (glnStr.Contains(" "))
                {
                    return ("GLN cannot contain spaces.");
                }
                else if (glnStr.Length == 13 && glnStr.IsOnlyDigits())
                {
                    // we don't care about validating the company prefix anymore
                    // string cp = GCPLookUp.DetermineCompanyPrefix(glnStr);
                    // if (string.IsNullOrWhiteSpace(cp))
                    // {
                    //     return "Invalid Company Prefix.";
                    // }

                    // validate the checksum
                    char checksum = GS1Util.CalculateGLN13CheckSum(glnStr);
                    if (checksum != glnStr.ToCharArray().Last())
                    {
                        return string.Format("The check sum did not calculate correctly. The expected check sum was {0}. " +
                            "Please make sure to validate that you typed the GLN correctly. It's possible the check sum " +
                            "was typed correctly but another number was entered wrong.", checksum);
                    }

                    return (null);
                }
                else if (glnStr.StartsWith("urn:") && glnStr.Contains(":location:loc:"))
                {
                    return (null);
                }
                else if (glnStr.StartsWith("urn:") && glnStr.Contains(":location:extension:loc:"))
                {
                    return (null);
                }
                else if (glnStr.Contains(":id:sgln:"))
                {
                    string[] pieces = glnStr.Split(':').Last().Split('.');
                    if (pieces.Length < 2)
                    {
                        throw new Exception($"The GLN {glnStr} is not valid.");
                    }

                    string lastPiece = pieces[0] + pieces[1];
                    if (!lastPiece.IsOnlyDigits())
                    {
                        return ("This is supposed to be a GS1 GLN based on the System Prefix and " +
                            "Data Type Prefix. That means the Company Prefix and Serial Numbers " +
                            "should only be digits. Found non-digit characters in the Company Prefix " +
                            "or Serial Number.");
                    }
                    else if (lastPiece.Length != 12)
                    {
                        return ("This is supposed to be a GS1 GLN based on the System Prefix and Data Type " +
                            "Prefix. That means the Company Prefix and Serial Numbers should contain a maximum " +
                            "total of 12 digits between the two. The total number of digits when combined " +
                            "is " + lastPiece.Length + ".");
                    }

                    return (null);
                }
                else
                {
                    return ("The GLN is not in a valid EPCIS URI format or in GS1 GLN-13 format. Value = " + glnStr);
                }
            }
            catch (Exception Ex)
            {
                OTLogger.Error(Ex);
                throw;
            }
        }

        public static bool TryParse(string glnStr, out GLN? gln, out string? error)
        {
            try
            {
                error = GLN.DetectGLNIssue(glnStr);
                if (string.IsNullOrWhiteSpace(error))
                {
                    gln = new GLN(glnStr);
                    return true;
                }
                else
                {
                    gln = null;
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
            GLN gln = new GLN(this.ToString());
            return gln;
        }

        #region Overrides

        public static bool operator ==(GLN? obj1, GLN? obj2)
        {
            try
            {
                if (Object.ReferenceEquals(null, obj1) && Object.ReferenceEquals(null, obj2))
                {
                    return true;
                }

                if (Object.ReferenceEquals(null, obj1) && Object.ReferenceEquals(null, obj2))
                {
                    return false;
                }

                if (Object.ReferenceEquals(null, obj1) && !Object.ReferenceEquals(null, obj2))
                {
                    return false;
                }

#pragma warning disable CS8602 // Dereference of a possibly null reference.
                return obj1.Equals(obj2);
#pragma warning restore CS8602 // Dereference of a possibly null reference.
            }
            catch (Exception Ex)
            {
                OTLogger.Error(Ex);
                throw;
            }
        }

        public static bool operator !=(GLN? obj1, GLN? obj2)
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

                return this.IsEquals((GLN)obj);
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
                return _glnStr.ToLower();
            }
            catch (Exception Ex)
            {
                OTLogger.Error(Ex);
                throw;
            }
        }

        #endregion Overrides

        #region IEquatable<GLN>

        public bool Equals(GLN? gln)
        {
            try
            {
                if (Object.ReferenceEquals(null, gln))
                {
                    return false;
                }

                if (Object.ReferenceEquals(this, gln))
                {
                    return true;
                }

                if (gln is null)
                {
                    return false;
                }
                else
                {
                    return this.ToString() == gln.ToString();
                }
            }
            catch (Exception Ex)
            {
                OTLogger.Error(Ex);
                throw;
            }
        }

        private bool IsEquals(GLN gln)
        {
            try
            {
                if (Object.ReferenceEquals(null, gln))
                {
                    return false;
                }

                if (this.ToString().ToLower() == gln.ToString().ToLower())
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

        #endregion IEquatable<GLN>

        #region IComparable

        public int CompareTo(GLN? gln)
        {
            try
            {
                if (Object.ReferenceEquals(null, gln))
                {
                    throw new ArgumentNullException(nameof(gln));
                }

                long myInt64Hash = this.ToString().GetInt64HashCode();
                long otherInt64Hash = gln.ToString().GetInt64HashCode();

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