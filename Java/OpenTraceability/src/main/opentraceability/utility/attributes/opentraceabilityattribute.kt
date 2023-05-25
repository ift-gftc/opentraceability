package utility.attributes

import models.identifiers.*
import models.events.*
import java.lang.reflect.Type

//[AttributeUsage(AttributeTargets.Property, AllowMultiple = true)]
class OpenTraceabilityAttribute /*:Attribute*/ {

    var Name: String = ""
    var SequenceOrder: Int? = null
    var Version: EPCISVersion? = null

    constructor(name: String) {
        Name = name
    }
    constructor(ns: String, name: String) {
        //Name = (((XNamespace)ns) + name).ToString()
    }
    constructor(name: String, sequenceOrder:Int) {
        Name = name
        SequenceOrder = sequenceOrder
    }
    constructor(ns: String, name: String, sequenceOrder:Int) {
        //Name = (((XNamespace)ns) + name).ToString()
        SequenceOrder = sequenceOrder
    }
    constructor(name: String, version: EPCISVersion) {
        Name = name
        Version = version
    }
    constructor(ns: String, name: String, version: EPCISVersion) {
        //Name = (((XNamespace)ns) + name).ToString()
        Version = version
    }

    constructor(name: String, sequenceOrder:Int, version: EPCISVersion) {
        Name = name
        SequenceOrder = sequenceOrder
        Version = version
    }
    constructor(ns: String, name: String, sequenceOrder:Int, version: EPCISVersion) {
        //Name = (((XNamespace)ns) + name).ToString()
        SequenceOrder = sequenceOrder
        Version = version
    }
}
