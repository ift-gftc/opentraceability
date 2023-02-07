using System;
using System.Collections.Generic;
using System.Runtime.Serialization;
using System.Text;

namespace OpenTraceability.Models.Identifiers
{
    [DataContract]
    public class GS1Prefixes
    {
        [DataMember]
        public string UPCPrefix { get; set; }

        [DataMember]
        public string GS1Prefix { get; set; }

        [DataMember]
        public string PrefixStatus { get; set; }

        [DataMember]
        public DateTime ModifiedDate { get; set; }
    }

    [DataContract]
    public class GS1PrefixInfo
    {
        public GS1PrefixInfo()
        {
            Prefixes = new GS1Prefixes();
        }

        [DataMember]
        public string AccountID { get; set; }

        [DataMember]
        public string Source { get; set; }

        [DataMember]
        public string EntityGLN { get; set; }

        [DataMember]
        public string CompanyName { get; set; }

        [DataMember]
        public string StreetAddress1 { get; set; }

        [DataMember]
        public string StreetAddress2 { get; set; }

        [DataMember]
        public string StreetAddress3 { get; set; }

        [DataMember]
        public string City { get; set; }

        [DataMember]
        public string StateProvince { get; set; }

        [DataMember]
        public string ZipCode { get; set; }

        [DataMember]
        public string Country { get; set; }

        [DataMember]
        public string GSRN { get; set; }

        [DataMember]
        public string ModifiedDate { get; set; }

        [DataMember]
        public GS1Prefixes Prefixes { get; set; }
    }

}
