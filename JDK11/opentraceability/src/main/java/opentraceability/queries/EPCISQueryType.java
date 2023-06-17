package opentraceability.queries;

public enum EPCISQueryType {
    Unknown(0),
    events(1);

    private final int value;

    EPCISQueryType(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }
}