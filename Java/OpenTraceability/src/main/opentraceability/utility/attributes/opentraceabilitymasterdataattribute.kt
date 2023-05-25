package utility.attributes

import java.lang.reflect.Type

class OpenTraceabilityMasterDataAttribute /*: Attribute*/ {
    var Name: String = ""

    constructor() {
        Name = ""
    }
    constructor(name: String) {
        Name = name
    }
    constructor(ns: String, name: String) {
        //Name = (((XNamespace)ns) + name).ToString();
        Name = name
    }
}
