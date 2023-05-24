package utility
class JsonSchemaChecker {
    companion object{

        var _lock: Object = Object()
        lateinit var _schemaCache: MutableMap<String, String>

        //TODO: errors: List<String> is a output parameter
        fun IsValid(jsonStr: String, schemaURL: String,  errors: List<String>): Boolean {

            TODO("Not yet implemented")
        }
    }


}
