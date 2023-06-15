package opentraceability.models.events;

public enum EventErrorType {
    Unknown(0),
    IncorrectData(1),
    DidNotOccur(0);

    private int value;

    EventErrorType(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }
}