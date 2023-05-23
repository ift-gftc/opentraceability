
class OpenTraceabilityEventKDEProfile {
    var XPath_V1: String = String()
    var XPath_V2: String = String()
    var JPath: String = String()

    constructor(xPath_V1: String,xPath_V2: String,jPath: String) {
        XPath_V1 = xPath_V1
        XPath_V2 = xPath_V2
        JPath = jPath
    }

    override fun toString(): String {

        return "$XPath_V1:$XPath_V2:$JPath"
    }
}
