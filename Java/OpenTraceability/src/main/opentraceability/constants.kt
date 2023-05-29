package opentraceability

import opentraceability.models.identifiers.*

class Constants {
    companion object {
        const val SBDH_NAMESPACE: String = "http://www.unece.org/cefact/namespaces/StandardBusinessDocumentHeader"
        const val EPCIS_2_NAMESPACE: String = "urn:epcglobal:epcis:xsd:2"
        const val EPCIS_1_NAMESPACE: String = "urn:epcglobal:epcis:xsd:1"
        const val EPCISQUERY_1_NAMESPACE: String = "urn:epcglobal:epcis-query:xsd:1"
        const val EPCISQUERY_2_NAMESPACE: String = "urn:epcglobal:epcis-query:xsd:2"
        const val XSI_NAMESPACE: String = "http://www.w3.org/2001/XMLSchema-instance"
        const val CBVMDA_NAMESPACE: String = "urn:epcglobal:cbv:mda"
        const val GDST_NAMESPACE: String = "https://traceability-dialogue.org/epcis"
        const val XMLNS_NAMEPSACE: String = "http://www.w3.org/2000/xmlns/"

        var SBDH_XNAMESPACE: String = SBDH_NAMESPACE
        var EPCIS_2_XNAMESPACE: String = EPCIS_2_NAMESPACE
        var EPCIS_1_XNAMESPACE: String = EPCIS_1_NAMESPACE
        var EPCISQUERY_1_XNAMESPACE: String = EPCISQUERY_1_NAMESPACE
        var EPCISQUERY_2_XNAMESPACE: String = EPCISQUERY_2_NAMESPACE
        var XSI_XNAMESPACE: String = XSI_NAMESPACE
        var CBVMDA_XNAMESPACE: String = CBVMDA_NAMESPACE
        var GDST_XNAMESPACE: String = GDST_NAMESPACE
        var XMLNS_XNAMESPACE: String = XMLNS_NAMEPSACE

        var EPCIS: EPCISConstants = EPCISConstants()
    }
}
