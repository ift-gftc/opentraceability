package utility
class JsonSchemaChecker {
    companion object{

        var _lock: Object = Object()
        lateinit var _schemaCache: MutableMap<String, String>

        //TODO: errors: ArrayList<String> is a output parameter
        fun IsValid(jsonStr: String, schemaURL: String,  errors: ArrayList<String>): Boolean {

            TODO("Not yet implemented")
        }
    }


}
