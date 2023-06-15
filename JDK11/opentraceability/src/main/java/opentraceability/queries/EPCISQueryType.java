package opentraceability.queries;

public enum EPCISQueryType {
    Unknown(0),
    events(1);

    private int value;

    EPCISQueryType(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }
}