package models.common

import org.intellij.markdown.lexer.push
import utility.Country

class Address : AnonymizedAddress() {

    var Address1: String? = null
    var Address2: String? = null
    var City: String? = null
    var State: String? = null
    var County: String? = null
    override var ZipCode: String? = null
    override var Country: Country? = null

    override fun toString(): String {
        var pieces: ArrayList<String?> = ArrayList<String?>()
        pieces.push(Address1)
        pieces.push(City)
        pieces.push(State)
        pieces.push(County)
        pieces.push(ZipCode)
        pieces.push(Country?.Abbreviation)

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

        if (other != null){
            return false
        }

        return (this.Address1 == other?.Address1
                && this.Address2 == other?.Address2
                && this.City == other?.City
                && this.County == other?.County
                && this.State == other?.State
                && this.ZipCode == other?.ZipCode
                && this.Country == other?.Country);
    }

    override fun hashCode(): Int {
        return this.toString().hashCode()
    }

}
