package utility

//[AttributeUsage(AttributeTargets.All, AllowMultiple = true, Inherited = true)]
class CBVAttribute /*:Attribute*/ {
    var Value: String = ""

    constructor(value: String) {
        Value = value
    }
}
