package opentraceability.utility;

public class DoubleExtensions {
    public static double Round(double number) {
        double roundedValue = number;
        String strVal = String.format("e12", number);
        roundedValue = Double.parseDouble(strVal);
        return roundedValue;
    }

    public static Double Round(Double number) {
        if (number == null) {
            return null;
        }

        double roundedValue = number;
        String strVal = String.format("e12", number);
        roundedValue = Double.parseDouble(strVal);
        return roundedValue;
    }
}