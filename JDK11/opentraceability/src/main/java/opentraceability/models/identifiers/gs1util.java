package opentraceability.models.identifiers;

import java.util.ArrayList;
import java.util.List;

public class GS1Util {
    public static boolean IsEven(int i) {
        return i % 2 == 0;
    }

    public static int CharToInt32(char charInt) {
        switch (charInt) {
            case '0': return 0;
            case '1': return 1;
            case '2': return 2;
            case '3': return 3;
            case '4': return 4;
            case '5': return 5;
            case '6': return 6;
            case '7': return 7;
            case '8': return 8;
            case '9': return 9;
            default: throw new IllegalArgumentException("Must give a single digit numeral string.");
        }
    }

    public static char Int32ToChar(int charInt) {
        switch (charInt) {
            case 0: return '0';
            case 1: return '1';
            case 2: return '2';
            case 3: return '3';
            case 4: return '4';
            case 5: return '5';
            case 6: return '6';
            case 7: return '7';
            case 8: return '8';
            case 9: return '9';
            default: throw new IllegalArgumentException("Must give a single digit numeral string.");
        }
    }

    public static int[] BreakIntoDigits(String strInt) {
        List<Integer> rtnInts = new ArrayList<>();

        for (int i = 0; i < strInt.length(); i++) {
            rtnInts.add(CharToInt32(strInt.charAt(i)));
        }

        return rtnInts.stream().mapToInt(i -> i).toArray();
    }

    public static char CalculateGTIN14CheckSum(String strGS) {
        if (strGS == null) throw new IllegalArgumentException("strGS cannot be null.");

        int[] gsDigits = BreakIntoDigits(strGS);
        int sum = 0;

        for (int i = 0; i < gsDigits.length; i++) {
            if (IsEven(i)) {
                sum += gsDigits[i] * 3;
            } else {
                sum += gsDigits[i];
            }
        }

        int higherMultipleOfTen = 10;
        while (higherMultipleOfTen < sum) {
            higherMultipleOfTen += 10;
        }
        if (sum == 0) {
            higherMultipleOfTen = 0;
        }

        int determinedCheckSum = higherMultipleOfTen - sum;
        char charCheckSum = Int32ToChar(determinedCheckSum);
        return charCheckSum;
    }

    public static char CalculateGLN13CheckSum(String strGS) {
        if (strGS == null) throw new IllegalArgumentException("strGS cannot be null.");

        int[] gsDigits = BreakIntoDigits(strGS);
        int sum = 0;

        for (int i = 0; i < gsDigits.length; i++) {
            if (IsEven(i)) {
                sum += gsDigits[i];
            } else {
                sum += gsDigits[i] * 3;
            }
        }

        int higherMultipleOfTen = 10;
        while (higherMultipleOfTen < sum) {
            higherMultipleOfTen += 10;
        }

        int determinedCheckSum = higherMultipleOfTen - sum;
        char charCheckSum = Int32ToChar(determinedCheckSum);
        return charCheckSum;
    }
}