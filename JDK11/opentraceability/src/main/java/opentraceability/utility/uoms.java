package opentraceability.utility;

import opentraceability.OTLogger;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class UOMS {
    public UOMS() {
        load();
    }

    private static final Object _locker = new Object();

    public static Map<String, UOM> uomsAbbrevDict = new HashMap<>();
    public static Map<String, UOM> uomsUNCodeDict = new HashMap<>();

    public static void load() {
        try {
            // Load the subscriptions JSON
            EmbeddedResourceLoader loader = new EmbeddedResourceLoader();
            String jsonText = loader.readString("OpenTraceability", "OpenTraceability.Utility.Data.uoms.json");
            JSONArray jarr = new JSONArray(jsonText);

            for (int i = 0; i < jarr.length(); i++) {
                JSONObject juom = jarr.getJSONObject(i);
                UOM uom = new UOM(juom);
                String lowerCaseAbbreviation = uom.Abbreviation.toLowerCase();
                String upperCaseUNCode = uom.UNCode.toUpperCase();

                if (!uomsAbbrevDict.containsKey(lowerCaseAbbreviation)) {
                    uomsAbbrevDict.put(lowerCaseAbbreviation, uom);
                } else {
                    System.out.println("Duplicate Unit abbreviation detected: " + uom.Abbreviation);
                }

                if (!uomsUNCodeDict.containsKey(upperCaseUNCode)) {
                    uomsUNCodeDict.put(upperCaseUNCode, uom);
                } else {
                    System.out.println("Duplicate Unit UNCode detected: " + uom.UNCode);
                }
            }
        } catch (Exception ex) {
            OTLogger.error(ex);
            throw ex;
        }
    }

    public static UOM getBase(UOM uom) {
        return getBase(uom.UnitDimension);
    }

    public static UOM getBase(String dimension) {
        for (UOM entry : uomsAbbrevDict.values()) {
            if (entry.UnitDimension.equals(dimension) && entry.isBase()) {
                return entry;
            }
        }
        throw new Exception("Failed to get base for dimension = " + dimension);
    }

    public static UOM getUOMFromName(String name) {
        UOM uom = null;
        String formattedName = name.toLowerCase();
        if (formattedName.equals("count")) {
            formattedName = "ea";
        }
        if (formattedName.equals("pound") || formattedName.equals("pounds") || formattedName.equals("ib")) {
            formattedName = "lb";
        }
        if (formattedName.charAt(formattedName.length() - 1) == '.') {
            formattedName = formattedName.substring(0, formattedName.length() - 1);
        }
        if (uomsAbbrevDict.containsKey(formattedName)) {
            uom = uomsAbbrevDict.get(formattedName);
        } else {
            for (UOM entry : uomsAbbrevDict.values()) {
                if (entry.Name.toLowerCase().equals(formattedName)) {
                    uom = entry;
                    break;
                }
            }
            if (uom == null) {
                if (formattedName.charAt(formattedName.length() - 1) == 's') {
                    formattedName = formattedName.substring(0, formattedName.length() - 1);
                    for (UOM entry : uomsAbbrevDict.values()) {
                        if (entry.Name.toLowerCase().equals(formattedName)) {
                            uom = entry;
                            break;
                        }
                    }
                }
            }
        }
        return uom;
    }

    public static UOM getUOMFromUNCode(String name) {
        String formattedName = name.toUpperCase();
        UOM uom = uomsUNCodeDict.get(formattedName);
        if (uom == null) {
            for (UOM entry : uomsAbbrevDict.values()) {
                if (entry.UNCode.equalsIgnoreCase(formattedName)) {
                    uom = entry;
                    break;
                }
            }
        }
        return uom;
    }

    public static List<UOM> getList() {
        synchronized (_locker) {
            return new ArrayList<>(uomsAbbrevDict.values());
        }
    }
}