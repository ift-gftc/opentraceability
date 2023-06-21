package opentraceability.utility;

import javax.xml.namespace.NamespaceContext;
import java.util.*;

public class SimpleNamespaceContext implements NamespaceContext {

    private final Map<String, String> PREF_MAP = new HashMap<String, String>();
    private List<String> PREF_LIST = new ArrayList<>();

    public SimpleNamespaceContext(final Map<String, String> prefMap, List<String> prefixes) {
        PREF_MAP.putAll(prefMap);
        PREF_LIST = prefixes;
    }

    public String getNamespaceURI(String prefix) {
        return PREF_MAP.get(prefix);
    }

    public String getPrefix(String uri) {
        return PREF_MAP.get(uri);
    }

    public Iterator getPrefixes(String uri) {
        return PREF_LIST.iterator();
    }
}
