package opentraceability.models.events;

import opentraceability.utility.CBVAttribute;

public enum EventDestinationType {
    Unknown(0),

    @CBVAttribute(value = "owning_party")
    @CBVAttribute(value = "https://ref.gs1.org/cbv/SDT-owning_party")
    @CBVAttribute(value = "urn:epcglobal:cbv:sdt:owning_party")
    Owner(1),

    @CBVAttribute(value = "possessing_party")
    @CBVAttribute(value = "https://ref.gs1.org/cbv/SDT-possessing_party")
    @CBVAttribute(value = "urn:epcglobal:cbv:sdt:possessing_party")
    Possessor(2),

    @CBVAttribute(value = "location")
    @CBVAttribute(value = "https://ref.gs1.org/cbv/SDT-location")
    @CBVAttribute(value = "urn:epcglobal:cbv:sdt:location")
    Location(3);

    private int value;

    EventDestinationType(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }
}