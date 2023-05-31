package models.events

import interfaces.IEventKDE
import java.util.*
import utility.attributes.*

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
