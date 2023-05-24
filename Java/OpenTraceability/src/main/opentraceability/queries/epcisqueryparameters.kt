package queries

import models.identifiers.*
import java.lang.reflect.Type
import java.net.URL

class EPCISQueryParameters {
    var queryType: EPCISQueryType = EPCISQueryType.events
    var query: EPCISQuery = EPCISQuery()

    companion object {
        fun EPCISQueryParameters(){
            TODO("Not yet implemented")
        }
    }


    constructor(epcs: ArrayList<EPC>) {
        TODO("Not yet implemented")
    }
    constructor(uri: URL) {
        TODO("Not yet implemented")
    }

    fun IsValid(error: String?): Boolean{
        TODO("Not yet implemented")
    }

    fun ToJSON(): String{
        TODO("Not yet implemented")
    }

    fun ToQueryParameters(): String{
        TODO("Not yet implemented")
    }
}
