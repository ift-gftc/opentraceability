package opentraceability.models.events;

import opentraceability.utility.CBVAttribute;

public enum EventDestinationType {
    Unknown(0),

    @CBVAttribute("owning_party")
    @CBVAttribute("https://ref.gs1.org/cbv/SDT-owning_party")
    @CBVAttribute("urn:epcglobal:cbv:sdt:owning_party")
    Owner(1),

    @CBVAttribute("possessing_party")
    @CBVAttribute("https://ref.gs1.org/cbv/SDT-possessing_party")
    @CBVAttribute("urn:epcglobal:cbv:sdt:possessing_party")
    Possessor(2),

    @CBVAttribute("location")
    @CBVAttribute("https://ref.gs1.org/cbv/SDT-location")
    @CBVAttribute("urn:epcglobal:cbv:sdt:location")
    Location(3);

    private int value;

    EventDestinationType(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }
}