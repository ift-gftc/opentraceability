package opentraceability.models.events

enum class EventErrorType(val value: Int) {
    Unknown(0),
    IncorrectData(1),
    DidNotOccur(0),
}
