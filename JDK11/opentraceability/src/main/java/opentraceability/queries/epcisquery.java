package opentraceability.queries;

import java.net.URI;
import java.time.OffsetDateTime;
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
    public List<URI> EQ_bizLocation = new ArrayList<>();
    public List<String> EQ_action = new ArrayList<>();
}