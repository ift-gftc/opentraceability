package opentraceability.models.identifiers;

public enum EPCType {
    Class(0),
    Instance(1),
    SSCC(2),
    URI(3);

    private int value;

    EPCType(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }
}