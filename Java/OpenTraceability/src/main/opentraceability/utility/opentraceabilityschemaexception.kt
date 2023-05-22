package utility
class OpenTraceabilitySchemaException {
    var TargetSite: MethodBase = MethodBase()
    var Message: String = String()
    var Data: IDictionary = IDictionary()
    var InnerException: Exception = Exception()
    var HelpLink: String = String()
    var Source: String = String()
    var HResult: Int = Int()
    var StackTrace: String = String()
    companion object{
    }
}
