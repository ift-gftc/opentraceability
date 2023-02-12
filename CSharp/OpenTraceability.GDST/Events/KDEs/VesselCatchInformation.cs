using OpenTraceability.Interfaces;
using OpenTraceability.Utility;
using System.Xml.Linq;

namespace OpenTraceability.GDST.Events.KDEs
{
    /// <summary>
    /// Represents vessel catch information found in the ILMD of a fishing event.
    /// </summary>
    public class VesselCatchInformation : IEquatable<VesselCatchInformation>
    {
        public string? CatchArea { get; set; }
        public string? VesselName { get; set; }
        public string? VesselID { get; set; }
        public string? IMONumber { get; set; }
        public Country? VesselFlagState { get; set; }
        public string? RFMO { get; set; }
        public string? EconomicZone { get; set; }
        public string? SubNationalPermitArea { get; set; }
        public string? MSCUnitCertificateArea { get; set; }
        public string? VesselPublicRegistry { get; set; }
        public bool GPSAvailability { get; set; }
        public string? SatelliteTrackingAuthority { get; set; }
        public string? FIP { get; set; }
        public string? GearType { get; set; }
        public DateTime? VesselTripDate { get; set; }

        public List<IEventKDE> KDEs { get; set; } = new List<IEventKDE>();

        public XElement ToXml()
        {
            XElement xml = new XElement("cbvmda:vesselCatchInformation");

            if (!string.IsNullOrEmpty(VesselName))
            {
                xml.Add(new XElement("cbvmda:vesselName", this.VesselName));
            }

            if (!string.IsNullOrEmpty(VesselID))
            {
                xml.Add(new XElement("cbvmda:vesselID", this.VesselID));
            }

            if (!string.IsNullOrEmpty(IMONumber))
            {
                xml.Add(new XElement("gdst:imoNumber", this.IMONumber));
            }

            if (VesselFlagState != null)
            {
                xml.Add(new XElement("cbvmda:vesselFlagState", this.VesselFlagState.Abbreviation));
            }

            if (!string.IsNullOrEmpty(VesselPublicRegistry))
            {
                xml.Add(new XElement("gdst:vesselPublicRegistry", this.VesselPublicRegistry));
            }

            xml.Add(new XElement("gdst:gpsAvailability", this.GPSAvailability.ToString().ToLower()));

            if (!string.IsNullOrEmpty(SatelliteTrackingAuthority))
            {
                xml.Add(new XElement("gdst:satelliteTrackingAuthority", this.SatelliteTrackingAuthority));
            }

            if (!string.IsNullOrEmpty(EconomicZone))
            {
                xml.Add(new XElement("cbvmda:economicZone", this.EconomicZone));
            }

            if (!string.IsNullOrEmpty(FIP))
            {
                xml.Add(new XElement("gdst:fisheryImprovementProject", this.FIP));
            }

            if (!string.IsNullOrEmpty(RFMO))
            {
                xml.Add(new XElement("gdst:rfmoArea", this.RFMO));
            }

            if (!string.IsNullOrEmpty(SubNationalPermitArea))
            {
                xml.Add(new XElement("gdst:subnationalPermitArea", this.SubNationalPermitArea));
            }

            if (!string.IsNullOrWhiteSpace(MSCUnitCertificateArea))
            {
                xml.Add(new XElement("gdst:mscUnitCertificateArea", this.MSCUnitCertificateArea));
            }

            if (!string.IsNullOrEmpty(CatchArea))
            {
                xml.Add(new XElement("cbvmda:catchArea", this.CatchArea));
            }

            if (!string.IsNullOrEmpty(GearType))
            {
                xml.Add(new XElement("cbvmda:fishingGearTypeCode", this.GearType));
            }

            if (VesselTripDate != null)
            {
                xml.Add(new XElement("gdst:vesselTripDate", this.VesselTripDate?.ToUniversalTime().ToString("yyyy-MM-ddTHH:mm:ss.fffZ")));
            }

            return xml;
        }

        public void FromXml(XElement xml)
        {
            foreach (var child in xml.Elements())
            {
                if (child.Name == "cbvmda:catchArea") this.CatchArea = child.Value;
                if (child.Name == "cbvmda:vesselName") this.VesselName = child.Value;
                if (child.Name == "cbvmda:vesselID") this.VesselID = child.Value;
                if (child.Name == "gdst:imoNumber") this.IMONumber = child.Value;
                if (child.Name == "cbvmda:vesselFlagState") this.VesselFlagState = Countries.FromAbbreviation(child.Value);
                if (child.Name == "gdst:rfmoArea") this.RFMO = child.Value;
                if (child.Name == "cbvmda:economicZone") this.EconomicZone = child.Value;
                if (child.Name == "gdst:subnationalPermitArea") this.SubNationalPermitArea = child.Value;
                if (child.Name == "gdst:mscUnitCertificateArea") this.MSCUnitCertificateArea = child.Value;
                if (child.Name == "gdst:vesselPublicRegistry") this.VesselPublicRegistry = child.Value;
                if (child.Name == "gdst:gpsAvailability") this.GPSAvailability = bool.Parse(child.Value);
                if (child.Name == "gdst:satelliteTrackingAuthority") this.SatelliteTrackingAuthority = child.Value;
                if (child.Name == "gdst:fisheryImprovementProject") this.FIP = child.Value;
                if (child.Name == "cbvmda:fishingGearTypeCode") this.GearType = child.Value;
                if (child.Name == "gdst:vesselTripDate")
                {
                    if (DateTime.TryParse(child.Value, out DateTime dt))
                    {
                        this.VesselTripDate = dt;
                    }
                }
            }
        }

        public override bool Equals(object? obj)
        {
            if (Object.ReferenceEquals(null, obj))
            {
                return false;
            }

            if (Object.ReferenceEquals(this, obj))
            {
                return true;
            }

            if (obj is VesselCatchInformation)
            {
                return this.Equals(obj as VesselCatchInformation);
            }
            else
            {
                return false;
            }
        }

        public override int GetHashCode()
        {
            return base.GetHashCode();
        }

        public bool Equals(VesselCatchInformation? other)
        {
            if (other == null)
            {
                return false;
            }

            if (this.CatchArea?.ToLower() != other.CatchArea?.ToLower())
            {
                return false;
            }

            if (this.VesselName?.ToLower() != other.VesselName?.ToLower())
            {
                return false;
            }

            if (this.VesselID?.ToLower() != other.VesselID?.ToLower())
            {
                return false;
            }

            if (this.IMONumber?.ToLower() != other.IMONumber?.ToLower())
            {
                return false;
            }

            if (this.RFMO?.ToLower() != other.RFMO?.ToLower())
            {
                return false;
            }

            if (this.EconomicZone?.ToLower() != other.EconomicZone?.ToLower())
            {
                return false;
            }

            if (this.SubNationalPermitArea?.ToLower() != other.SubNationalPermitArea?.ToLower())
            {
                return false;
            }

            if (this.SatelliteTrackingAuthority?.ToLower() != other.SatelliteTrackingAuthority?.ToLower())
            {
                return false;
            }

            if (this.VesselPublicRegistry?.ToLower() != other.VesselPublicRegistry?.ToLower())
            {
                return false;
            }

            if (this.MSCUnitCertificateArea?.ToLower() != other.MSCUnitCertificateArea?.ToLower())
            {
                return false;
            }

            if (this.FIP?.ToLower() != other.FIP?.ToLower())
            {
                return false;
            }

            if (this.GearType?.ToLower() != other.GearType?.ToLower())
            {
                return false;
            }

            if (this.VesselFlagState != other.VesselFlagState)
            {
                return false;
            }

            if (this.GPSAvailability != other.GPSAvailability)
            {
                return false;
            }

            return true;
        }
    }
}