package models.events
enum class EventDestinationType(val value: Int) {
    //TODO: review this

    Unknown(0),

    //[CBV("owning_party")
    //[CBV("https://ref.gs1.org/cbv/SDT-owning_party")
    //[CBV("urn:epcglobal:cbv:sdt:owning_party")
    Owner(1),

    //[CBV("possessing_party")
    //[CBV("https://ref.gs1.org/cbv/SDT-possessing_party")
    //[CBV("urn:epcglobal:cbv:sdt:possessing_party")
    Possessor(2),

    //[CBV("location")
    //[CBV("https://ref.gs1.org/cbv/SDT-location")
    //[CBV("urn:epcglobal:cbv:sdt:location")
    Location(3),
}
