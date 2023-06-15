package opentraceability.models.events;

public enum EPCISVersion {
    V1(1),
    V2(2);

    private int value;

    EPCISVersion(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }
}