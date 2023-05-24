package models.events

//TODO: review this

enum class EventSourceType(val value: Int) {
    Unknown(0),

    //[CBV("owning_party")]
    //[CBV("https://ref.gs1.org/cbv/SDT-owning_party")]
    //[CBV("urn:epcglobal:cbv:sdt:owning_party")]
    Owner(1),

    //[CBV("possessing_party")]
    //[CBV("https://ref.gs1.org/cbv/SDT-possessing_party")]
    //[CBV("urn:epcglobal:cbv:sdt:possessing_party")]
    Possessor(2),

    //[CBV("location")]
    //[CBV("https://ref.gs1.org/cbv/SDT-location")]
    //[CBV("urn:epcglobal:cbv:sdt:location")]
    Location(3),
}
