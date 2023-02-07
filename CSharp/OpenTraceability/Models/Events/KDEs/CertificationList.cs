using Newtonsoft.Json.Linq;
using OpenTraceability.Interfaces;
using OpenTraceability.Models.Common;
using System.ComponentModel.DataAnnotations.Schema;
using System.Xml.Linq;

namespace OpenTraceability.Models.Events.KDEs
{
    public class CertificationList : IEventKDE
    {
        public List<Certificate> Certificates { get; set; } = new List<Certificate>();

        public string Key => throw new NotImplementedException();

        public Type ValueType => typeof(List<Certificate>);

        public JToken? GetJson()
        {
            throw new NotImplementedException();
        }

        public XElement? GetXml()
        {
            throw new NotImplementedException();
        }

        public void SetFromJson(JToken json)
        {
            throw new NotImplementedException();
        }

        public void SetFromXml(XElement xml)
        {
            throw new NotImplementedException();
        }

        //public DSXML ToXml()
        //{
        //    DSXML xml = new DSXML("cbvmda:certificationList");
        //    foreach (var c in Certificates)
        //    {
        //        DSXML xCertificate = new DSXML("certification");

        //        if (c.CertificateType != null)
        //        {
        //            xCertificate.AddChild("gdst:certificationType", c.CertificateType);
        //        }

        //        if (!string.IsNullOrWhiteSpace(c.Standard))
        //        {
        //            xCertificate.AddChild("certificationStandard", c.Standard);
        //        }

        //        if (!string.IsNullOrWhiteSpace(c.Agency))
        //        {
        //            xCertificate.AddChild("certificationAgency", c.Agency);
        //        }

        //        if (!string.IsNullOrWhiteSpace(c.Value))
        //        {
        //            xCertificate.AddChild("certificationValue", c.Value);
        //        }

        //        if (!string.IsNullOrWhiteSpace(c.Identification))
        //        {
        //            xCertificate.AddChild("certificationIdentification", c.Identification);
        //        }

        //        xml.AddChild(xCertificate);
        //    }
        //    return xml;
        //}

        //public void FromXml(DSXML xml)
        //{
        //    this.Certificates = new List<ICertificate>();
        //    foreach (DSXML xCertificate in xml.Elements("certification"))
        //    {
        //        ICertificate cert = new Certificate();
        //        cert.CertificateType = xCertificate["gdst:certificationType"].Value;
        //        cert.Standard = xCertificate["certificationStandard"].Value;
        //        cert.Agency = xCertificate["certificationAgency"].Value;
        //        cert.Value = xCertificate["certificationValue"].Value;
        //        cert.Identification = xCertificate["certificationIdentification"].Value;
        //        this.Certificates.Add(cert);
        //    }
        //}
    }
}