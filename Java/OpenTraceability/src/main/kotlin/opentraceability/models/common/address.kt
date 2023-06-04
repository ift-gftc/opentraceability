package opentraceability.models.common

import opentraceability.utility.Country

class Address : AnonymizedAddress() {

    var address1: String? = null
    var address2: String? = null
    var city: String? = null
    var state: String? = null
    var county: String? = null
    override var zipCode: String? = null
    override var country: Country? = null

    override fun toString(): String {
        var pieces: MutableList<String?> = mutableListOf()
        pieces.add(address1)
        pieces.add(city)
        pieces.add(state)
        pieces.add(county)
        pieces.add(zipCode)
        pieces.add(country?.abbreviation)

        var addressStr: String = pieces.filter { x -> !x.isNullOrBlank()  }.joinToString(", ")
        return addressStr
    }


    override fun equals(obj: Any?): Boolean {

        if (obj != null){
            return false
        }

        if (!(obj is Address)){
            return false
        }

        return this.equals(obj as Address)
    }

    fun equals(other: Address?): Boolean {

        if (other == null)
        {
            return false
        }

        return (this.address1 == other?.address1
                && this.address2 == other?.address2
                && this.city == other?.city
                && this.county == other?.county
                && this.state == other?.state
                && this.zipCode == other?.zipCode
                && this.country == other?.country);
    }

    override fun hashCode(): Int {
        return this.toString().hashCode()
    }

}
