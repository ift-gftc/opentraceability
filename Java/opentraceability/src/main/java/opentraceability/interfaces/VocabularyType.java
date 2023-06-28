package opentraceability.interfaces;

public enum VocabularyType {
    Unknown(0),
    TradeItem(1),
    Location(2),
    TradingParty(3);

    private final int value;

    VocabularyType(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }
}