package opentraceability.models.events;

public enum EventProductType {
    Reference(1),
    Input(2),
    Output(3),
    Parent(4),
    Child(5);

    private int value;

    EventProductType(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }
}