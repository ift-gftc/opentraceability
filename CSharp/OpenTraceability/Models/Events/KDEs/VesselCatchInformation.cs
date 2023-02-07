using DSUtil;
using DSUtil.StaticData;
using GS1.Interfaces.Models.Events;
using System;
using System.Collections.Generic;
using System.Diagnostics.CodeAnalysis;
using System.Linq;
using System.Text;
using System.Threading.Tasks;
using DSUtil.Meta;

namespace OpenTraceability.Models.Events.KDEs
{
    public class VesselCatchInformation : IEquatable<VesselCatchInformation>
    {
        public string CatchArea { get; set; }
        public string VesselName { get; set; }
        public string VesselID { get; set; }
        public string IMONumber { get; set; }
        public Country VesselFlagState { get; set; }
        public string RFMO { get; set; }
        public string EconomicZone { get; set; }
        public string SubNationalPermitArea { get; set; }
        public string MSCUnitCertificateArea { get; set; }
        public string VesselPublicRegistry { get; set; }
        public bool GPSAvailability { get; set; }
        public string SatelliteTrackingAuthority { get; set; }
        public string FIP { get; set; }
        public string GearType { get; set; }
        public DateTime? VesselTripDate { get; set; }

        public List<IEventKDE> KDEs { get; set; }

        public DSXML ToXml()
        {
            DSXML xml = new DSXML("cbvmda:vesselCatchInformation");

            if (!string.IsNullOrEmpty(VesselName)) xml.AddChild("cbvmda:vesselName", this.VesselName);
            if (!string.IsNullOrEmpty(VesselID)) xml.AddChild("cbvmda:vesselID", this.VesselID);
            if (!string.IsNullOrEmpty(IMONumber)) xml.AddChild("gdst:imoNumber", this.IMONumber);
            if (VesselFlagState != null) xml.AddChild("cbvmda:vesselFlagState", this.VesselFlagState.Abbreviation);
            if (!string.IsNullOrEmpty(VesselPublicRegistry)) xml.AddChild("gdst:vesselPublicRegistry", this.VesselPublicRegistry);
            xml.AddChild("gdst:gpsAvailability", this.GPSAvailability.ToString().ToLower());
            if (!string.IsNullOrEmpty(SatelliteTrackingAuthority)) xml.AddChild("gdst:satelliteTrackingAuthority", this.SatelliteTrackingAuthority);
            if (!string.IsNullOrEmpty(EconomicZone)) xml.AddChild("cbvmda:economicZone", this.EconomicZone);
            if (!string.IsNullOrEmpty(FIP)) xml.AddChild("gdst:fisheryImprovementProject", this.FIP);
            if (!string.IsNullOrEmpty(RFMO)) xml.AddChild("gdst:rfmoArea", this.RFMO);
            if (!string.IsNullOrEmpty(SubNationalPermitArea)) xml.AddChild("gdst:subnationalPermitArea", this.SubNationalPermitArea);
            if (!string.IsNullOrWhiteSpace(MSCUnitCertificateArea)) xml.AddChild("gdst:mscUnitCertificateArea", this.MSCUnitCertificateArea);
            if (!string.IsNullOrEmpty(CatchArea)) xml.AddChild("cbvmda:catchArea", this.CatchArea);
            if (!string.IsNullOrEmpty(GearType)) xml.AddChild("cbvmda:fishingGearTypeCode", this.GearType);
            if (VesselTripDate != null) xml.AddChild("gdst:vesselTripDate", this.VesselTripDate?.ToUniversalTime().ToString("yyyy-MM-ddTHH:mm:ss.fffZ"));

            return xml;
        }

        public void FromXml(DSXML xml)
        {
            foreach (var child in xml.ChildElements)
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
                if (child.Name == "gdst:gpsAvailability") this.GPSAvailability = child.GetValueBool();
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

        public override bool Equals(object obj)
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

        public bool Equals(VesselCatchInformation other)
        {
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
