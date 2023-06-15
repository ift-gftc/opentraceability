package opentraceability.models.identifiers;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import opentraceability.OTLogger;
import opentraceability.utility.GS1Util;

// [DataContract]
// [JsonConverter(typeof(GLNConverter))]
class GLN {

    String _glnStr = "";

    public GLN(){
    }

    public GLN(String glnStr) {
        try {
            String error = GLN.DetectGLNIssue(glnStr);
            if (error != null && !error.trim().equals("")) {
                throw new Exception("The GLN " + glnStr + " is invalid. " + error);
            }
            this._glnStr = glnStr;
        } catch (Exception ex) {
            OTLogger.error(ex);
            throw ex;
        }

    }

    public String ToDigitalLinkURL() {
        try {
            if (IsGS1PGLN()) {
                String[] gtinParts = _glnStr.split(":")[1].split("\\.");
                String pgln = gtinParts[0] + gtinParts[1] + GS1Util.CalculateGLN13CheckSum(gtinParts[0] + gtinParts[1]);
                return "414/" + pgln;
            } else {
                return "414/" + _glnStr;
            }
        } catch (Exception ex) {
            OTLogger.error(ex);
            throw ex;
        }
    }

    public Boolean IsGS1PGLN() {
        return _glnStr.contains(":id:sgln:");
    }

    public Object Clone() {
        GLN gln = new GLN(toString());
        return gln;
    }

    public Boolean equals(GLN obj1, GLN obj2) {
        try {
            if (obj1 == null && obj2 == null) {
                return true;
            }

            if (obj1 == null || obj2 == null) {
                return false;
            }

            return obj1.equals(obj2);
        } catch (Exception ex) {
            OTLogger.error(ex);
            throw ex;
        }
    }

    public Boolean notEquals(GLN obj1, GLN obj2) {
        try {
            if (obj1 == null && obj2 == null) {
                return false;
            }

            if (obj1 == null || obj2 == null) {
                return true;
            }

            return !obj1.equals(obj2);
        } catch (Exception ex) {
            OTLogger.error(ex);
            throw ex;
        }
    }

    @Override
    public boolean equals(Object obj) {
        try {
            if (obj == null) {
                return false;
            }

            if (this == obj) {
                return true;
            }

            if (getClass() != obj.getClass()) {
                return false;
            }

            return isEquals((GLN) obj);
        } catch (Exception ex) {
            OTLogger.error(ex);
            throw ex;
        }
    }

    @Override
    public int hashCode() {
        try {
            int hash = toString().hashCode();
            return hash;
        } catch (Exception ex) {
            OTLogger.error(ex);
            throw ex;
        }
    }

    @Override
    public String toString() {
        try {
            return _glnStr.toLowerCase();
        } catch (Exception ex) {
            OTLogger.error(ex);
            throw ex;
        }
    }

    public Boolean equals(GLN gln) {
        try {
            if (gln == null) {
                return false;
            }

            if (this == gln) {
                return true;
            }

            return toString().equals(gln.toString());
        } catch (Exception ex) {
            OTLogger.error(ex);
            throw ex;
        }
    }

    public Boolean isEquals(GLN gln) {
        try {
            if (gln == null) {
                return false;
            }

            return toString().toLowerCase().equals(gln.toString().toLowerCase());
        } catch (Exception ex) {
            OTLogger.error(ex);
            throw ex;
        }
    }

    public int compareTo(GLN gln) {
        try {
            if (gln == null) {
                throw new NullPointerException("gln");
            }

            long myInt64Hash = toString().hashCode();
            long otherInt64Hash = gln.toString().hashCode();

            if (myInt64Hash > otherInt64Hash) {
                return -1;
            } else if (myInt64Hash < otherInt64Hash) {
                return 1;
            } else {
                return 0;
            }
        } catch (Exception ex) {
            OTLogger.error(ex);
            throw ex;
        }
    }

    public static String DetectGLNIssue(String gln) {
        try {
            if (gln == null || gln.isEmpty()) {
                return "The GLN is NULL or EMPTY.";
            } else if (!IsURICompatibleChars(gln)) {
                return "The GLN contains non-compatiable characters for a URI.";
            } else if (gln.contains(" ")) {
                return "GLN cannot contain spaces.";
            } else if (gln.length() == 13 && IsOnlyDigits(gln)) {
                String checksum = GS1Util.CalculateGLN13CheckSum(gln);
                if (checksum.charAt(0) != gln.charAt(12)) {
                    return "The check sum did not calculate correctly. The expected check sum was " + checksum
                            + ".  Please make sure to validate that you typed the GLN correctly. It's possible the check sum "
                            + "was typed correctly but another number was entered wrong.";
                }
                return null;
            } else if (gln.startsWith("urn:") && gln.contains(":location:loc:")) {
                return null;
            } else if (gln.startsWith("urn:") && gln.contains(":location:extension:loc:")) {
                return null;
            } else if (gln.contains(":id:sgln:")) {
                String[] pieces = gln.split(":")[gln.split(":").length - 1].split("\\.");
                if (pieces.length < 2) {
                    throw new Exception("The GLN " + gln + " is not valid.");
                }
                String lastPiece = pieces[0] + pieces[1];
                if (!IsOnlyDigits(lastPiece)) {
                    return "This is supposed to be a GS1 GLN based on the System Prefix and "
                            + "Data Type Prefix. That means the Company Prefix and Serial Numbers "
                            + "should only be digits. Found non-digit characters in the Company Prefix "
                            + "or Serial Number.";
                } else if (lastPiece.length() != 12) {
                    return "This is supposed to be a GS1 GLN based on the System Prefix and Data Type "
                            + "Prefix. That means the Company Prefix and Serial Numbers should contain a maximum "
                            + "total of 12 digits between the two. The total number of digits when combined "
                            + "is " + lastPiece.length() + ".";
                }
                return null;
            } else {
                return "The GLN is not in a valid EPCIS URI format or in GS1 GLN-13 format. Value = " + gln;
            }
        } catch (Exception ex) {
            OTLogger.error(ex);
            throw ex;
        }
    }

    public static Boolean isGLN(String gln) {
        try {
            String error = DetectGLNIssue(gln);
            return error == null || error.trim().equals("");
        } catch (Exception ex) {
            OTLogger.error(ex);
            throw ex;
        }
    }

    public static Boolean IsURICompatibleChars(String input) {
        String reservedChars = ":