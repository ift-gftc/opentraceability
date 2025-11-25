using Newtonsoft.Json;
using OpenTraceability.Utility;
using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;

namespace OpenTraceability.Models.Identifiers
{
    public enum EPCType
    {
        Class = 0,
        Instance = 1,
        SSCC = 2,
        URI = 3
    }

    [JsonConverter(typeof(EPCConverter))]
    public class EPC
    {
        private string _epcStr = string.Empty;

        public EPCType Type { get; private set; }
        public GTIN GTIN { get; private set; }
        public string SerialLotNumber { get; private set; }

        public EPC(string epcStr)
        {
            try
            {
                string error = EPC.DetectEPCIssue(epcStr);

                if (!string.IsNullOrWhiteSpace(error))
                {
                    throw new Exception($"The EPC {epcStr} is invalid. {error}");
                }
                else if (epcStr == null)
                {
                    throw new ArgumentNullException(nameof(epcStr));
                }

                this._epcStr = epcStr;

                // if this is a GS1 class level epc (GS1 GTIN + Lot Number)
                if (epcStr.StartsWith("urn:epc:id:sscc:"))
                {
                    this.Type = EPCType.SSCC;
                    this.SerialLotNumber = epcStr.Split(':').LastOrDefault();
                }
                else if (epcStr.StartsWith("urn:epc:class:lgtin:"))
                {
                    this.Type = EPCType.Class;

                    List<string> parts = epcStr.Split(':').ToList();
                    List<string> parts2 = parts.Last().Split('.').ToList();
                    parts.RemoveAt(parts.Count - 1);

                    string gtinStr = String.Join(":", parts) + ":" + parts2[0] + "." + parts2[1];
                    gtinStr = gtinStr.Replace(":class:lgtin:", ":idpat:sgtin:");
                    this.SerialLotNumber = parts2[2];
                    this.GTIN = new GTIN(gtinStr);
                }
                // else if this is a GS1 instance level epc (GS1 GTIN + Serial Number)
                else if (epcStr.StartsWith("urn:epc:id:sgtin:"))
                {
                    this.Type = EPCType.Instance;

                    List<string> parts = epcStr.Split(':').ToList();
                    List<string> parts2 = parts.Last().Split('.').ToList();
                    parts.RemoveAt(parts.Count - 1);

                    string gtinStr = String.Join(":", parts) + ":" + parts2[0] + "." + parts2[1];
                    gtinStr = gtinStr.Replace(":id:sgtin:", ":idpat:sgtin:");
                    this.SerialLotNumber = parts2[2];
                    this.GTIN = new GTIN(gtinStr);
                }
                // else if this is a GDST / IBM private class level identifier (GTIN + Lot Number)
                else if (epcStr.StartsWith("urn:") && epcStr.Contains(":product:lot:class:"))
                {
                    this.Type = EPCType.Class;

                    List<string> parts = epcStr.Split(':').ToList();
                    List<string> parts2 = parts.Last().Split('.').ToList();
                    parts.RemoveAt(parts.Count - 1);

                    string gtinStr = String.Join(":", parts) + ":" + parts2[0] + "." + parts2[1];
                    gtinStr = gtinStr.Replace(":product:lot:class:", ":product:class:");
                    this.SerialLotNumber = parts2[2];
                    this.GTIN = new GTIN(gtinStr);
                }
                // else if this is a GDST / IBM private instance level identifier (GTIN + Serial Number)
                else if (epcStr.StartsWith("urn:") && epcStr.Contains(":product:serial:obj:"))
                {
                    this.Type = EPCType.Instance;

                    List<string> parts = epcStr.Split(':').ToList();
                    List<string> parts2 = parts.Last().Split('.').ToList();
                    parts.RemoveAt(parts.Count - 1);

                    string gtinStr = String.Join(":", parts) + ":" + parts2[0] + "." + parts2[1];
                    gtinStr = gtinStr.Replace(":product:serial:obj:", ":product:class:");
                    this.SerialLotNumber = parts2[2];
                    this.GTIN = new GTIN(gtinStr);
                }
                else if (epcStr.StartsWith("urn:sscc:"))
                {
                    this.Type = EPCType.SSCC;
                }
                else if (epcStr.StartsWith("urn:") && epcStr.Contains(":lpn:obj:"))
                {
                    this.Type = EPCType.SSCC;
                }
                else if (epcStr.StartsWith("urn:epc:id:bic:"))
                {
                    this.Type = EPCType.SSCC;
                }
                // DIGITAL LINK URI CHECK
                else if (epcStr.StartsWith("https://id.gs1.org"))
                {
                    List<string> parts = epcStr.Split('?').First().Substring("https://id.gs1.org".Length).Split('/').Where(s => !string.IsNullOrWhiteSpace(s)).ToList();

                    Dictionary<string, string> applicationIdentifiers = new();
                    if (parts.Count >= 2)
                    {
                        for (int i = 0; i < parts.Count; i += 2)
                        {
                            string key = parts[i];
                            string value = parts[i + 1];
                            applicationIdentifiers[key] = value;
                        }
                    }

                    // CHECK IF IS SSCC (00)
                    if (applicationIdentifiers.ContainsKey("00"))
                    {
                        this.Type = EPCType.SSCC;
                        this.SerialLotNumber = applicationIdentifiers["00"];
                    }
                    // CHECK IF IS CLASS (01 + 10)
                    else if (applicationIdentifiers.ContainsKey("01") && applicationIdentifiers.ContainsKey("10"))
                    {
                        this.Type = EPCType.Class;
                        this.GTIN = new GTIN(applicationIdentifiers["01"]);
                        this.SerialLotNumber = applicationIdentifiers["10"];
                    }
                    // CHECK IF IS INSTANCE (01 + 21)
                    else if (applicationIdentifiers.ContainsKey("01") && applicationIdentifiers.ContainsKey("21"))
                    {
                        this.Type = EPCType.Instance;
                        this.GTIN = new GTIN(applicationIdentifiers["01"]);
                        this.SerialLotNumber = applicationIdentifiers["21"];
                    }
                }
                else if (Uri.IsWellFormedUriString(epcStr, UriKind.Absolute) && epcStr.StartsWith("http") && epcStr.Contains("/obj/"))
                {
                    this.Type = EPCType.Instance;
                    this.SerialLotNumber = epcStr.Split('/').LastOrDefault();
                }
                else if (Uri.IsWellFormedUriString(epcStr, UriKind.Absolute) && epcStr.StartsWith("http") && epcStr.Contains("/class/"))
                {
                    this.Type = EPCType.Class;
                    this.SerialLotNumber = epcStr.Split('/').LastOrDefault();
                }
                else if (Uri.IsWellFormedUriString(epcStr, UriKind.Absolute))
                {
                    this.Type = EPCType.URI;
                }
            }
            catch (Exception Ex)
            {
                Exception exception = new Exception("The EPC is not in a valid format and could not be parsed. EPC=" + epcStr, Ex);
                OTLogger.Error(Ex);
                throw exception;
            }
        }

        public EPC(EPCType type, GTIN gtin, string lotOrSerial)
        {
            if (type == EPCType.Class)
            {
                string epc = gtin.ToString().ToLower() + "." + lotOrSerial;
                if (epc.Contains(":product:class:"))
                {
                    epc = epc.Replace(":product:class:", ":product:lot:class:");
                }
                else if (epc.Contains(":idpat:sgtin:"))
                {
                    epc = epc.Replace(":idpat:sgtin:", ":class:lgtin:");
                }
                else
                {
                    throw new Exception("Unrecognized GTIN pattern. " + gtin.ToString());
                }
                this.Type = type;
                this.GTIN = gtin;
                this.SerialLotNumber = lotOrSerial;
                this._epcStr = epc;
            }
            else if (type == EPCType.Instance)
            {
                string epc = gtin.ToString().ToLower() + "." + lotOrSerial;
                if (epc.Contains(":product:class:"))
                {
                    epc = epc.Replace(":product:class:", ":product:serial:obj:");
                }
                else if (epc.Contains(":idpat:sgtin:"))
                {
                    epc = epc.Replace(":idpat:sgtin:", ":id:sgtin:");
                }
                else
                {
                    throw new Exception("Unrecognized GTIN pattern. " + gtin.ToString());
                }
                this.Type = type;
                this.GTIN = gtin;
                this.SerialLotNumber = lotOrSerial;
                this._epcStr = epc;
            }
            else
            {
                throw new Exception("Cannot build EPC of type {type} with a GTIN and Lot/Serial number.");
            }
        }

        public static string DetectEPCIssue(string epcStr)
        {
            try
            {
                if (string.IsNullOrWhiteSpace(epcStr))
                {
                    return ("The EPC is a NULL or White Space string.");
                }

                // if this is a GS1 class level epc (GS1 GTIN + Lot Number)
                if (epcStr.StartsWith("urn:epc:class:lgtin:"))
                {
                    if (!epcStr.IsURICompatibleChars())
                    {
                        return ("The EPC contains non-compatiable characters for a URN format.");
                    }

                    string[] parts = epcStr.Split(':');
                    string[] parts2 = parts.Last().Split('.');

                    if (parts2.Count() < 3)
                    {
                        return $"The EPC {epcStr} is not in the right format. It doesn't contain a company prefix, item code, and lot number.";
                    }
                    else
                    {
                        return string.Empty;
                    }
                }
                // else if this is a GS1 instance level epc (GS1 GTIN + Serial Number)
                else if (epcStr.StartsWith("urn:epc:id:sgtin:"))
                {
                    if (!epcStr.IsURICompatibleChars())
                    {
                        return ("The EPC contains non-compatiable characters for a URN format.");
                    }

                    string[] parts = epcStr.Split(':');
                    string[] parts2 = parts.Last().Split('.');

                    if (parts2.Count() < 3)
                    {
                        return $"The EPC {epcStr} is not in the right format. It doesn't contain a company prefix, item code, and lot number.";
                    }
                    else
                    {
                        return null;
                    }
                }
                // else if this is a GDST / IBM private class level identifier (GTIN + Lot Number)
                else if (epcStr.StartsWith("urn:") && epcStr.Contains(":product:lot:class:"))
                {
                    if (!epcStr.IsURICompatibleChars())
                    {
                        return ("The EPC contains non-compatiable characters for a URN format.");
                    }

                    string[] parts = epcStr.Split(':');
                    string[] parts2 = parts.Last().Split('.');

                    if (parts2.Count() < 3)
                    {
                        return $"The EPC {epcStr} is not in the right format. It doesn't contain a company prefix, item code, and serial number.";
                    }
                    else
                    {
                        return null;
                    }
                }
                // else if this is a GDST / IBM private instance level identifier (GTIN + Serial Number)
                else if (epcStr.StartsWith("urn:") && epcStr.Contains(":product:serial:obj:"))
                {
                    if (!epcStr.IsURICompatibleChars())
                    {
                        return ("The EPC contains non-compatiable characters for a URN format.");
                    }

                    string[] parts = epcStr.Split(':');
                    string[] parts2 = parts.Last().Split('.');

                    if (parts2.Count() < 3)
                    {
                        return $"The EPC {epcStr} is not in the right format. It doesn't contain a company prefix, item code, and a serial number.";
                    }
                    else
                    {
                        return null;
                    }
                }
                else if (epcStr.StartsWith("urn:epc:id:sscc:"))
                {
                    if (!epcStr.IsURICompatibleChars())
                    {
                        return ("The EPC contains non-compatiable characters for a URN format.");
                    }

                    return null;
                }
                else if (epcStr.StartsWith("urn:epc:id:bic:"))
                {
                    if (!epcStr.IsURICompatibleChars())
                    {
                        return ("The EPC contains non-compatiable characters for a URN format.");
                    }

                    return null;
                }
                else if (epcStr.StartsWith("urn:") && epcStr.Contains(":lpn:obj:"))
                {
                    if (!epcStr.IsURICompatibleChars())
                    {
                        return ("The EPC contains non-compatiable characters for a URN format.");
                    }

                    return null;
                }
                else if (Uri.IsWellFormedUriString(epcStr, UriKind.Absolute) && epcStr.StartsWith("http") && epcStr.Contains("/obj/"))
                {
                    return null;
                }
                else if (Uri.IsWellFormedUriString(epcStr, UriKind.Absolute) && epcStr.StartsWith("http") && epcStr.Contains("/class/"))
                {
                    return null;
                }
                else if (Uri.IsWellFormedUriString(epcStr, UriKind.Absolute))
                {
                    return null;
                }
                else
                {
                    return "This EPC does not fit any of the allowed formats.";
                }
            }
            catch (Exception Ex)
            {
                OTLogger.Error(Ex);
                throw;
            }
        }

        public static bool TryParse(string epcStr, out EPC epc, out string error)
        {
            try
            {
                error = EPC.DetectEPCIssue(epcStr);
                if (string.IsNullOrWhiteSpace(error))
                {
                    epc = new EPC(epcStr);
                    return true;
                }
                else
                {
                    epc = null;
                    return false;
                }
            }
            catch (Exception Ex)
            {
                OTLogger.Error(Ex);
                throw;
            }
        }

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

        public string ToDigitalLinkURI()
        {
            if (this._epcStr.StartsWith("https://id.gs1.org"))
            {
                return this._epcStr.Substring("https://id.gs1.org".Length);
            }

            string? relativeUrl = null;
            switch (this.Type)
            {
                case EPCType.Class: relativeUrl = this.GTIN?.ToDigitalLinkURL() + "/10/" + this.SerialLotNumber; break;
                case EPCType.Instance: relativeUrl = this.GTIN?.ToDigitalLinkURL() + "/21/" + this.SerialLotNumber; break;
                case EPCType.SSCC: relativeUrl = "00/" + this.ToString(); break;
                default: throw new Exception($"Cannot build Digital Link URL with EPC {this}. We need either GTIN+LOT, GTIN+SERIAL, or SSCC.");
            }
            return relativeUrl;
        }

        #region Overrides

        public static bool operator ==(EPC obj1, EPC obj2)
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

        public static bool operator !=(EPC obj1, EPC obj2)
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

        public bool Equals(EPC epc)
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

        private bool IsEquals(EPC epc)
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
    }
}