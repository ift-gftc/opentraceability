package models.events
enum class EventProductType(val value: Int) {
    Reference(1),
    Input(2),
    Output(3),
    Parent(4),
    Child(5),
}
