package opentraceability.utility;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.Arrays;
import java.util.stream.Collectors;

public class JSONExtensions {
    public static Object query(JSONObject json, String jpath)
    {
        if (json == null)
        {
            return null;
        }

        if (jpath == null)
        {
            return json;
        }

        if (jpath.contains("."))
        {
            String[] parts = jpath.split("[.]");
            String newJPath = String.join(".", Arrays.stream(parts).skip(1).collect(Collectors.toList()));
            JSONObject newJSON = json.optJSONObject(parts[0]);
            return query(newJSON, newJPath);
        }
        else
        {
            return (json.has(jpath) ? json.get(jpath) : null);
        }
    }

    public static JSONArray queryForArray(JSONObject json, String jpath)
    {
        Object o = query(json, jpath);
        if (o != null && o instanceof  JSONArray)
        {
            return (JSONArray) o;
        }
        else {
            return null;
        }
    }

    public static void put(JSONObject jobj, String name, Object value)
    {
        if (value instanceof JSONObject)
        {
            jobj.put(name, (JSONObject)value);
        }
        else if (value instanceof JSONArray)
        {
            jobj.put(name, (JSONArray)value);
        }
        else if (value instanceof Integer)
        {
            jobj.put(name, (Integer) value);
        }
        else if (value instanceof Double)
        {
            jobj.put(name, (Double)value);
        }
        else if (value instanceof Boolean)
        {
            jobj.put(name, (Boolean)value);
        }
        else
        {
            jobj.put(name, value.toString());
        }

        String debug = jobj.toString();
    }

    public static void put(JSONArray jarr, Object value)
    {
        jarr.put(value);
    }

    public static JSONObject queryForObject(JSONObject json, String jpath)
    {
        Object o = query(json, jpath);
        if (o != null && o instanceof JSONObject)
        {
            return (JSONObject) o;
        }
        else {
            return null;
        }
    }

    public static Measurement readMeasurement(JSONObject json)
    {
        double quantity = json.getDouble("quantity");
        String uom = "EA";
        if (json.has("uom"))
        {
            uom = json.getString("uom");
        }
        else
        {
            uom = "EA";
        }
        Measurement m = new Measurement(quantity, uom);
        return m;
    }

    public static String queryString(JSONObject json, String jpath) {
        Object o = query(json, jpath);
        if (o != null)
        {
            return o.toString();
        }
        else {
            return null;
        }
    }
}
