using OpenTraceability.Interfaces;
using System.ComponentModel.DataAnnotations.Schema;
using System.Xml.Linq;

namespace OpenTraceability.Models.Events.KDEs
{
    public class CertificationList : IEventKDE
    {
        public List<ICertificate> Certificates { get; set; } = new List<ICertificate>();

        public DSXML ToXml()
        {
            DSXML xml = new DSXML("cbvmda:certificationList");
            foreach (var c in Certificates)
            {
                DSXML xCertificate = new DSXML("certification");

                if (c.CertificateType != null)
                {
                    xCertificate.AddChild("gdst:certificationType", c.CertificateType);
                }

                if (!string.IsNullOrWhiteSpace(c.Standard))
                {
                    xCertificate.AddChild("certificationStandard", c.Standard);
                }

                if (!string.IsNullOrWhiteSpace(c.Agency))
                {
                    xCertificate.AddChild("certificationAgency", c.Agency);
                }

                if (!string.IsNullOrWhiteSpace(c.Value))
                {
                    xCertificate.AddChild("certificationValue", c.Value);
                }

                if (!string.IsNullOrWhiteSpace(c.Identification))
                {
                    xCertificate.AddChild("certificationIdentification", c.Identification);
                }

                xml.AddChild(xCertificate);
            }
            return xml;
        }

        public void FromXml(DSXML xml)
        {
            this.Certificates = new List<ICertificate>();
            foreach (DSXML xCertificate in xml.Elements("certification"))
            {
                ICertificate cert = new Certificate();
                cert.CertificateType = xCertificate["gdst:certificationType"].Value;
                cert.Standard = xCertificate["certificationStandard"].Value;
                cert.Agency = xCertificate["certificationAgency"].Value;
                cert.Value = xCertificate["certificationValue"].Value;
                cert.Identification = xCertificate["certificationIdentification"].Value;
                this.Certificates.Add(cert);
            }
        }

        #region IEventKDE

        public string Key { get; set; } = "cbvmda:certificationList";

        [NotMapped]
        public DSXML XmlValue { get => ToXml(); set => FromXml(value); }

        [NotMapped]
        public JToken JsonValue { get => throw new NotImplementedException(); set => throw new NotImplementedException(); }

        public Type ValueType => typeof(CertificationList);

        #endregion IEventKDE
    }
}