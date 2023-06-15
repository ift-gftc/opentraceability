package opentraceability.queries;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.net.URI;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;

import opentraceability.models.identifiers.*;

class EPCISQueryStackTraceItem {
    public String ID = UUID.randomUUID().toString();
    public LocalDateTime Created = LocalDateTime.now();
    //public HttpStatusCode ResponseStatusCode = null;
    public Integer ResponseStatusCode = null;
    public URI RelativeURL = null;
    public List<Map<String, List<String>>> RequestHeaders = null;
    public List<Map<String, List<String>>> ResponseHeaders = null;
    public String RequestBody = null;
    public String ResponseBody = null;
}