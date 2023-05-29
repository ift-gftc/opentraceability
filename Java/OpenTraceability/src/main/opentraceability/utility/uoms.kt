package utility

import OTLogger
import org.intellij.markdown.lexer.push
import java.util.*

//[System.Xml.Serialization.XmlTypeAttribute(AnonymousType = true)
//[System.Xml.Serialization.XmlRootAttribute(Namespace = "", IsNullable = false)
class UOMS {

    //TODO: Not yet implemented

    companion object {
        var uomsAbbrevDict: MutableMap<String, UOM> = mutableMapOf()
        var uomsUNCodeDict: MutableMap<String, UOM> = mutableMapOf()
        var _locker: Object = Object()

        fun Load() {
            try {
                /*
                // load the subscriptions xml
                EmbeddedResourceLoader loader = new EmbeddedResourceLoader();
                JArray jarr = JArray.Parse(loader.ReadString("OpenTraceability", "OpenTraceability.Utility.Data.uoms.json"));
                foreach (JObject juom in jarr)
                {
                    UOM uom = new UOM(juom);
                    if (!uomsAbbrevDict.ContainsKey(uom.Abbreviation.ToLower()))
                    {
                        uomsAbbrevDict.TryAdd(uom.Abbreviation.ToLower(), uom);
                    }
                    else
                    {
                        System.Diagnostics.Trace.WriteLine("Duplicate Unit abbreviation detected:" + uom.Abbreviation);
                    }
                    if (!uomsUNCodeDict.ContainsKey(uom.UNCode.ToUpper()))
                    {
                        uomsUNCodeDict.TryAdd(uom.UNCode.ToUpper(), uom);
                    }
                    else
                    {
                        System.Diagnostics.Trace.WriteLine("Duplicate Unit UNCode detected:" + uom.UNCode);
                    }
                }
                */
            } catch (ex: Exception) {
                OTLogger.Error(ex)
                throw ex
            }
        }

        fun GetBase(uom: UOM): UOM {
            return (GetBase(uom.UnitDimension));
        }

        fun GetBase(dimension: String): UOM {

            uomsAbbrevDict.forEach { kvp ->
                if (kvp.value.UnitDimension == dimension) {
                    if (kvp.value.IsBase()) {
                        return (kvp.value);
                    }
                }
            }

            throw Exception("Failed to get base for dimension = " + dimension);
        }

        fun GetUOMFromName(Name: String): UOM? {
            var uom: UOM? = null;
            var Name = Name.toLowerCase();
            if (Name == "count") {
                Name = "ea";
            }
            if (Name == "pound" || Name == "pounds" || Name == "ib") {
                Name = "lb";
            }
            if (Name[Name.length - 1] == '.') {
                Name = Name.substring(0, Name.length - 1);
            }
            if (uomsAbbrevDict.containsKey(Name)) {
                uom = uomsAbbrevDict[Name];
            } else {
                run loop@{
                    uomsAbbrevDict.forEach { kvp ->
                        if (kvp.value.Name.toLowerCase() == Name) {
                            uom = kvp.value;
                            return@loop
                        }
                    }
                }

                if (uom == null) {
                    if (Name[Name.length - 1] == 's') {
                        Name = Name.substring(0, Name.length - 1);

                        run loop@{
                            uomsAbbrevDict.forEach { kvp ->
                                if (kvp.value.Name.toLowerCase() == Name) {
                                    uom = kvp.value;
                                    return@loop
                                }
                            }
                        }

                    }
                }
            }

            return (uom);
        }

        fun GetUOMFromUNCode(Name: String): UOM? {
            var uom: UOM? = null;

            var Name = Name.uppercase()

            if (uomsUNCodeDict.containsKey(Name)) {
                uom = uomsUNCodeDict[Name];
            } else {
                run loop@{
                    uomsAbbrevDict.forEach { kvp ->
                        if (kvp.value.UNCode.toLowerCase() == Name) {
                            uom = kvp.value;
                            return@loop
                        }
                    }
                }
            }

            return uom
        }

        var List: ArrayList<UOM> = ArrayList<UOM>()
            get() {
                var lst: ArrayList<UOM> = ArrayList<UOM>();
                //lock(_locker) {
                    uomsAbbrevDict.forEach { kvp ->
                        lst.push(kvp.value);
                    }

                    return lst;
                //}
            }
    }

    constructor() {
        _locker = Object()
        uomsAbbrevDict = mutableMapOf()
        uomsUNCodeDict = mutableMapOf()
        Load()
    }

}
