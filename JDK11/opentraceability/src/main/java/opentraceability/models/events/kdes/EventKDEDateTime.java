package opentraceability.models.events.kdes;

import opentraceability.interfaces.IEventKDE;
import opentraceability.utility.XElement;
import org.json.JSONObject;

import java.time.OffsetDateTime;
import org.w3c.dom.Element;

public class EventKDEDateTime extends IEventKDE {
    public Class valueType = OffsetDateTime.class;

    public OffsetDateTime value = null;

    public EventKDEDateTime() {
    }

    public EventKDEDateTime(String ns, String name) {
        this.namespace = ns;
        this.name = name;
    }

    @Override
    public Object getJson() {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public XElement getXml() {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public void setFromJson(Object json) {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public void setFromXml(XElement xml) {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public String toString() {
        if (value != null) {
            return value.toString();
        } else {
            return "";
        }
    }
}