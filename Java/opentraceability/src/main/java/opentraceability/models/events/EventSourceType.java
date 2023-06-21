package opentraceability.models.events;

import java.util.Arrays;
import java.util.List;

import opentraceability.utility.CBVAttribute;

public enum EventSourceType {
    Unknown(0),
    Owner(1,
    		"owning_party",
    		"https://ref.gs1.org/cbv/SDT-owning_party",
    		"urn:epcglobal:cbv:sdt:owning_party"),
    Possessor(2,
    		"possessing_party",
    		"https://ref.gs1.org/cbv/SDT-possessing_party",
    		"urn:epcglobal:cbv:sdt:possessing_party"),
    Location(3,
    		"location",
    		"https://ref.gs1.org/cbv/SDT-location",
    		"urn:epcglobal:cbv:sdt:location");

    private final int value;

    private final List<String> attributes;

    EventSourceType(int value, String...attributes) {
        this.value = value;
        this.attributes = Arrays.asList(attributes);
    }

    public int getValue() {
        return value;
    }

    public List<String> getCBVAttributes() {
        return attributes;
    }
}