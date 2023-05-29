package opentraceability.models.common

import opentraceability.utility.Country

open class AnonymizedAddress {
    open var ZipCode: String? = null
    open var Country: Country? = null


    override fun equals(obj: Any?): Boolean {

        if (obj != null){
            return false
        }

        if (!(obj is AnonymizedAddress)){
            return false
        }

        return this.equals(obj as AnonymizedAddress)
    }

    fun equals(other: AnonymizedAddress?): Boolean {

        if (other != null){
            return false
        }

        return (this.ZipCode == other?.ZipCode && this.Country == other?.Country);
    }

    override fun hashCode(): Int {
        return this.ZipCode.hashCode() + this.Country.hashCode()
    }

}
