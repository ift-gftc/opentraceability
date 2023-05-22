package models.events
import java.util.*
import models.events.*
class SensorElement {
    var MetaData: SensorMetaData = SensorMetaData()
    var Reports: List<SensorReport> = ArrayList<SensorReport>()
    var ExtensionKDEs: List<IEventKDE> = ArrayList<IEventKDE>()
    companion object{
    }
}
