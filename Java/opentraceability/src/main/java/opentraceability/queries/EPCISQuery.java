package opentraceability.queries;

import org.json.JSONArray;
import org.json.JSONObject;

import java.net.URI;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class EPCISQuery {
    public OffsetDateTime GE_recordTime = null;
    public OffsetDateTime LE_recordTime = null;
    public OffsetDateTime GE_eventTime = null;
    public OffsetDateTime LE_eventTime = null;
    public List<String> eventTypes = new ArrayList<>();
    public List<String> MATCH_epc = new ArrayList<>();
    public List<String> MATCH_epcClass = new ArrayList<>();
    public List<String> MATCH_anyEPC = new ArrayList<>();
    public List<String> MATCH_anyEPCClass = new ArrayList<>();
    public List<String> EQ_bizStep = new ArrayList<>();
    public List<String> EQ_bizLocation = new ArrayList<>();
    public List<String> EQ_action = new ArrayList<>();

    public JSONObject toJSON()
    {
        JSONObject json = new JSONObject();

        AddOffSetDateTime(json, "GE_eventTime", GE_eventTime);
        AddOffSetDateTime(json, "LE_eventTime", LE_eventTime);
        AddOffSetDateTime(json, "GE_recordTime", GE_recordTime);
        AddOffSetDateTime(json, "LE_recordTime", LE_recordTime);

        AddListString(json, "eventTypes", eventTypes);
        AddListString(json, "MATCH_epc", MATCH_epc);
        AddListString(json, "MATCH_epcClass", MATCH_epcClass);
        AddListString(json, "MATCH_anyEPC", MATCH_anyEPC);
        AddListString(json, "MATCH_anyEPCClass", MATCH_anyEPCClass);
        AddListString(json, "EQ_bizStep", EQ_bizStep);
        AddListString(json, "EQ_action", EQ_action);

        AddListString(json, "EQ_bizLocation", EQ_bizLocation);

        return json;
    }

    private void AddOffSetDateTime(JSONObject json, String name, OffsetDateTime dt)
    {
        if (dt != null)
        {
            json.put(name, dt.format(DateTimeFormatter.ISO_DATE_TIME));
        }
    }

    private void AddListString(JSONObject json, String name, List<String> list) {
        if (list != null && !list.isEmpty()) {
            JSONArray jarray = new JSONArray();
            for (var s : list) {
                jarray.put(s);
            }
            json.put(name, jarray);
        }
    }
}