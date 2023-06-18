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

        if (jpath.contains("[.]"))
        {
            String[] parts = jpath.split("[.]");
            String newJPath = String.join(".", Arrays.stream(parts).skip(1).collect(Collectors.toList()));
            JSONObject newJSON = json.getJSONObject(parts[0]);
            return query(newJSON, newJPath);
        }
        else
        {
            return json.get(jpath);
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
}
