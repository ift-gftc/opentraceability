package opentraceability.models.identifiers;

import opentraceability.OTLogger;
import opentraceability.utility.GS1Util;
import opentraceability.utility.StringExtensions;

import java.lang.Exception;

public class GTIN implements Comparable<GTIN> {

    public String _gtinStr = "";

    public GTIN() {
    }

    public GTIN(String gtinStr) {
        try {
            String error = DetectGTINIssue(gtinStr);
            if (!(error == null || error.isEmpty())) {
                throw new Exception("The GTIN " + gtinStr + " is invalid. " + error);
            }
            this._gtinStr = gtinStr;
        } catch (Exception ex) {
            OTLogger.error(ex);
            throw ex;
        }
    }

    public static Pair<GTIN, String> TryParse(String gtinStr) {
        try {
            String error = DetectGTINIssue(gtinStr);
            if (error == null || error.isEmpty()) {
                return new Pair<>(new GTIN(gtinStr), null);
            } else {
                return new Pair<>(null, error);
            }
        } catch (Exception ex) {
            OTLogger.error(ex);
            throw ex;
        }
    }

    public static boolean IsGTIN(String gtinStr) {
        try {
            return DetectGTINIssue(gtinStr) == null;
        } catch (Exception ex) {
            OTLogger.error(ex);
            throw ex;
        }
    }

    public static String DetectGTINIssue(String gtinStr) {
        try {
            if (gtinStr == null || gtinStr.isEmpty()) {
                return "GTIN is NULL or EMPTY.";
            } else if (!StringExtensions.IsURICompatibleChars(gtinStr)) {
                return "The GTIN contains non-compatible characters for a URI.";
            } else if (gtinStr.contains(" ")) {
                return "GTIN cannot contain spaces.";
            } else if (gtinStr.length() == 14 && StringExtensions.IsOnlyDigits(gtinStr)) {
                char checksum = GS1Util.CalculateGTIN14CheckSum(gtinStr);
                if (checksum != gtinStr.charAt(gtinStr.length() - 1)) {
                    return "The check sum did not calculate correctly. The expected check sum was " + checksum +
                            ". Please make sure to validate that you typed the GTIN correctly. It's possible the check sum " +
                            "was typed correctly but another number was entered wrong.";
                }
                return null;
            } else if (gtinStr.startsWith("urn:") && gtinStr.contains(":product:class:")) {
                return null;
            } else if (gtinStr.startsWith("urn:") && gtinStr.contains(":idpat:sgtin:")) {
                String lastPiece = gtinStr.split(":")[gtinStr.split(":").length - 1].replace(".", "");
                if (!StringExtensions.IsOnlyDigits(lastPiece)) {
                    return "This is supposed to be a GS1 GTIN based on the System Prefix and " +
                            "Data Type Prefix. That means the Company Prefix and Serial Numbers " +
                            "should only be digits. Found non-digit characters in the Company Prefix " +
                            "or Serial Number.";
                } else if (lastPiece.length() != 13) {
                    return "This is supposed to be a GS1 GTIN based on the System Prefix and Data Type " +
                            "Prefix. That means the Company Prefix and Serial Numbers should contain a maximum " +
                            "total of 13 digits between the two. The total number of digits when combined " +
                            "is " + lastPiece.length() + ".";
                }
                return null;
            } else {
                return "The GTIN is not in a valid EPCIS URI format or in GS1 GTIN-14 format.";
            }
        } catch (Exception ex) {
            Exception exception = new Exception("Failed to detect GTIN Issues. GTIN=" + gtinStr, ex);
            OTLogger.error(exception);
            throw exception;
        }
    }

    public boolean IsGS1GTIN() {
        return _gtinStr != null && _gtinStr.contains(":idpat:sgtin:");
    }

    public String ToDigitalLinkURL() {
        try {
            if (_gtinStr == null || _gtinStr.isEmpty()) {
                return "";
            } else if (IsGS1GTIN()) {
                String[] gtinParts = _gtinStr.split(":")[_gtinStr.split(":").length - 1].split("\\.");
                String gtin14 = gtinParts[1].charAt(0) + gtinParts[0] + gtinParts[1].substring(1);
                char checksum = GS1Util.CalculateGTIN14CheckSum(gtin14);
                String gtinWithChecksum = gtin14 + checksum;
                return "01/" + gtinWithChecksum;
            } else {
                return "01/" + _gtinStr;
            }
        } catch (Exception ex) {
            OTLogger.error(ex);
            throw ex;
        }
    }

    @Override
    public boolean equals(Object other) {
        try {
            if (this == other) {
                return true;
            }

            if (!(other instanceof GTIN)) {
                return false;
            }

            return toString().toLowerCase().equals(other.toString().toLowerCase());
        } catch (Exception ex) {
            OTLogger.error(ex);
            throw ex;
        }
    }

    @Override
    public int hashCode() {
        try {
            return toString().toLowerCase().hashCode();
        } catch (Exception ex) {
            OTLogger.error(ex);
            throw ex;
        }
    }

    @Override
    public String toString() {
        try {
            return _gtinStr != null ? _gtinStr.toLowerCase() : "";
        } catch (Exception ex) {
            OTLogger.error(ex);
            throw ex;
        }
    }

    @Override
    public int compareTo(GTIN other) {
        try {
            long myInt64Hash = toString().getInt64HashCode();
            long otherInt64Hash = other.toString().getInt64HashCode();
            return Long.compare(myInt64Hash, otherInt64Hash);
        } catch (Exception ex) {
            OTLogger.error(ex);
            throw ex;
        }
    }
}