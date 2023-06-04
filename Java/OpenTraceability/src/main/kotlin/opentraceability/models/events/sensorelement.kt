package opentraceability.models.events

import opentraceability.interfaces.IEventKDE
import opentraceability.utility.attributes.*
import java.util.*

class SensorElement {

    @OpenTraceabilityObjectAttribute
    @OpenTraceabilityAttribute("","sensorMetadata", 0)
    var metaData: SensorMetaData? = null

    @OpenTraceabilityArrayAttribute
    @OpenTraceabilityObjectAttribute
    @OpenTraceabilityAttribute("","sensorReport", 1)
    var reports: MutableList<SensorReport> = mutableListOf()

    @OpenTraceabilityExtensionElementsAttribute
    var extensionKDEs: MutableList<IEventKDE> = mutableListOf()
}
