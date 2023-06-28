package opentraceability.models.common

import opentraceability.utility.Country

open class AnonymizedAddress {
    open var zipCode: String? = null
    open var country: Country? = null


    override fun equals(obj: Any?): Boolean {

        if (obj == null)
        {
            return false
        }

        if (obj !is AnonymizedAddress)
        {
            return false
        }

        return this.equals(obj as AnonymizedAddress)
    }

    fun equals(other: AnonymizedAddress?): Boolean {

        if (other != null){
            return false
        }

        return (this.zipCode == other?.zipCode && this.country == other?.country);
    }

    override fun hashCode(): Int {
        return this.zipCode.hashCode() + this.country.hashCode()
    }

}
