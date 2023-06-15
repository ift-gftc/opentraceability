package opentraceability.models.identifiers;

import opentraceability.OTLogger;
import opentraceability.utility.GS1Util;
import java.lang.Exception;
import java.lang.IllegalArgumentException;
import java.lang.String;
import java.util.regex.Pattern;

public class PGLN {
    private String _pglnStr = "";

    public PGLN() {}

    public PGLN(String pglnStr) throws Exception {
        String error = DetectPGLNIssue(pglnStr);
        if (error != null && !error.isBlank()) {
            throw new Exception("The PGLN " + pglnStr + " is not valid. " + error);
        } else if (pglnStr != null) {
            this._pglnStr = pglnStr;
        } else {
            this._pglnStr = "";
        }
    }

    public Boolean IsGS1PGLN() {
        return _pglnStr.contains(":id:pgln:") || _pglnStr.contains(":id:sgln:");
    }

    public String ToDigitalLinkURL() throws Exception {
        try {
            if (IsGS1PGLN()) {
                String[] gtinParts = _pglnStr.split(":").last().split("\\.");
                String pgln = gtinParts[0] + gtinParts[1];
                String calculatedCheckSum = GS1Util.CalculateGLN13CheckSum(pgln);
                String digitalLinkURL = "417/" + pgln + calculatedCheckSum;
                return digitalLinkURL;
            } else {
                String digitalLinkURL = "417/" + _pglnStr;
                return digitalLinkURL;
            }
        } catch (Exception ex) {
            OTLogger.error(ex);
            throw ex;
        }
    }

    public PGLN clone() throws Exception {
        return new PGLN(this.toString());
    }

    public Boolean notEquals(Object obj) {
        return !(this.equals(obj));
    }

    @Override
    public Boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (!(other instanceof PGLN)) {
            return false;
        }
        return this.isEquals((PGLN) other);
    }

    @Override
    public int hashCode() {
        return this.toString().toLowerCase().hashCode();
    }

    @Override
    public String toString() {
        return this._pglnStr != null ? this._pglnStr.toLowerCase() : "";
    }

    public Boolean equals(PGLN pgln) {
        if (pgln == null) {
            return false;
        }
        if (this == pgln) {
            return true;
        }
        return isEquals(pgln);
    }

    public Boolean isEquals(PGLN pgln) {
        if (pgln == null) {
            return false;
        }
        return toString().equalsIgnoreCase(pgln.toString());
    }

    public int compareTo(PGLN pgln) {
        if (pgln == null) {
            throw new IllegalArgumentException("pgln");
        }
        long myInt64Hash = toString().getInt64HashCode();
        long otherInt64Hash = pgln.toString().getInt64HashCode();
        if (myInt64Hash > otherInt64Hash) {
            return -1;
        } else if (myInt64Hash == otherInt64Hash) {
            return 0;
        } else {
            return 1;
        }
    }

    public static Boolean IsPGLN(String pglnStr) {
        try {
            String error = DetectPGLNIssue(pglnStr);
            return error == null;
        } catch (Exception ex) {
            OTLogger.error(ex);
            throw ex;
        }
    }

    public static String DetectPGLNIssue(String pglnStr) {
        try {
            if (pglnStr == null || pglnStr.isEmpty()) {
                return "PGLN is NULL or EMPTY.";
            } else if (pglnStr.contains(" ")) {
                return "PGLN cannot contain spaces.";
            } else if (!IsURICompatibleChars(pglnStr)) {
                return "The PGLN contains non-compatible characters for a URI.";
            } else if (pglnStr.length() == 13 && IsOnlyDigits(pglnStr)) {
                String checksum = GS1Util.CalculateGLN13CheckSum(pglnStr);
                if (checksum.charAt(0) != pglnStr.charAt(pglnStr.length() - 1)) {
                    return "The check sum did not calculate correctly. The expected check sum was " + checksum +
                            ". Please make sure to validate that you typed the PGLN correctly. It's possible the check sum " +
                            "was typed correctly but another number was entered wrong.";
                } else {
                    return null;
                }
            } else if (pglnStr.startsWith("urn:") && (pglnStr.contains(":id:pgln:") || pglnStr.contains(":id:sgln:"))) {
                String[] pieces = pglnStr.split(":").last().split("\\.");
                if (pieces.length < 2) {
                    return "This is supposed to contain the company prefix and the location code. Did not find these two pieces.";
                }
                String lastPiece = pieces[0] + pieces[1];
                if (!IsOnlyDigits(lastPiece)) {
                    return "This is supposed to be a GS1 PGLN based on the System Prefix and Data Type Prefix. That means the Company Prefix and Serial Numbers should only be digits. Found non-digit characters in the Company Prefix or Serial Number.";
                } else if (lastPiece.length() != 12) {
                    return "This is supposed to be a GS1 PGLN based on the System Prefix and Data Type Prefix. That means the Company Prefix and Serial Numbers should contain a maximum total of 12 digits between the two. The total number of digits when combined is " + lastPiece.length() + ".";
                } else {
                    return null;
                }
            } else {
                return "The PGLN is not in a valid EPCIS URI format or in GS1 (P)GLN-13 format. PGLN = " + pglnStr;
            }
        } catch (Exception ex) {
            OTLogger.error(ex);
            throw ex;
        }
    }

    public static Boolean IsURICompatibleChars(String input) {
        String reservedChars = ":/?#[]@!$&'()*+,;=";
        String unreservedChars = "-._~";
        String allowedChars = reservedChars + unreservedChars + "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
        Pattern pattern = Pattern.compile("["+allowedChars+"]+");
        return pattern.matcher(input).matches();
    }

    public static Boolean IsOnlyDigits(String input) {
        String regex = "\\d+";
        Pattern pattern = Pattern.compile(regex);
        return pattern.matcher(input).matches();
    }

    public static Pair<Boolean, PGLN> TryParsePGLN(String pglnStr, PGLN pgln) throws Exception {
        try {
            String error = DetectPGLNIssue(pglnStr);
            if (error == null) {
                PGLN parsedPGLN = new PGLN(pglnStr);
                return new Pair<Boolean, PGLN>(true, parsedPGLN);
            } else {
                return new Pair<Boolean, PGLN>(false, null);
            }
        } catch (Exception ex) {
            OTLogger.error(ex);
            throw ex;
        }
    }
}