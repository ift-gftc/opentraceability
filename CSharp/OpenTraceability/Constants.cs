using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;
using System.Xml.Linq;

namespace OpenTraceability
{
    public static class Constants
    {
        public const string SBDH_NAMESPACE = "http://www.unece.org/cefact/namespaces/StandardBusinessDocumentHeader";
        public const string EPCIS_2_NAMESPACE = "urn:epcglobal:epcis:xsd:2";
        public const string EPCIS_1_NAMESPACE = "urn:epcglobal:epcis:xsd:1";
        public const string EPCISQUERY_1_NAMESPACE = "urn:epcglobal:epcis-query:xsd:1";
        public const string EPCISQUERY_2_NAMESPACE = "urn:epcglobal:epcis-query:xsd:2";
        public const string XSI_NAMESPACE = "http://www.w3.org/2001/XMLSchema-instance";
        public const string CBVMDA_NAMESPACE = "urn:epcglobal:cbv:mda";
        public const string GDST_NAMESPACE = "https://traceability-dialogue.org/epcis";

        public static readonly XNamespace SBDH_XNAMESPACE = SBDH_NAMESPACE;
        public static readonly XNamespace EPCIS_2_XNAMESPACE = EPCIS_2_NAMESPACE;
        public static readonly XNamespace EPCIS_1_XNAMESPACE = EPCIS_1_NAMESPACE;
        public static readonly XNamespace EPCISQUERY_1_XNAMESPACE = EPCISQUERY_1_NAMESPACE;
        public static readonly XNamespace EPCISQUERY_2_XNAMESPACE = EPCISQUERY_2_NAMESPACE;
        public static readonly XNamespace XSI_XNAMESPACE = XSI_NAMESPACE;
        public static readonly XNamespace CBVMDA_XNAMESPACE = CBVMDA_NAMESPACE;
        public static readonly XNamespace GDST_XNAMESPACE = GDST_NAMESPACE;
    }
}
