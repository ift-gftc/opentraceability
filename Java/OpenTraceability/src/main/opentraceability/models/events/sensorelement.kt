package opentraceability.models.events

import opentraceability.interfaces.IEventKDE
import java.util.*
import opentraceability.models.events.*
import opentraceability.utility.attributes.OpenTraceabilityArrayAttribute
import opentraceability.utility.attributes.OpenTraceabilityAttribute
import opentraceability.utility.attributes.OpenTraceabilityExtensionElementsAttribute
import opentraceability.utility.attributes.OpenTraceabilityObjectAttribute

class SensorElement {

    @OpenTraceabilityObjectAttribute
    @OpenTraceabilityAttribute("","sensorMetadata", 0)
    var MetaData: SensorMetaData? = null

    @OpenTraceabilityArrayAttribute
    @OpenTraceabilityObjectAttribute
    @OpenTraceabilityAttribute("","sensorReport", 1)
    var Reports: ArrayList<SensorReport> = ArrayList<SensorReport>()

    @OpenTraceabilityExtensionElementsAttribute
    var ExtensionKDEs: ArrayList<IEventKDE> = ArrayList<IEventKDE>()
}
