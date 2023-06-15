package opentraceability.models.identifiers;

import opentraceability.utility.*;
import opentraceability.OTLogger;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.MalformedURLException;
import java.util.Arrays;
import java.util.List;
import java.util.ArrayList;
import java.util.Optional;
import java.lang.Exception;

// Uncomment if you're using Jackson library
// @JsonDeserialize(using = EPCDeserializer.class)
// @JsonSerialize(using = EPCSerializer.class)
public class EPC {

    protected String _epcStr = "";

    protected EPCType Type;
    protected GTIN GTIN = null;
    protected String SerialLotNumber = null;

    public EPC() { }

    public EPC(String epcStr) throws Exception {
        try {
            String error = EPC.DetectEPCIssue(epcStr);

            if (error != null && !error.isEmpty()) {
                throw new Exception("The EPC " + epcStr + " is invalid. " + error);
            } else if (epcStr == null) {
                throw new Exception("ArgumentNullException epcStr");
            }

            this._epcStr = epcStr;

            if (epcStr.startsWith("urn:epc:id:sscc:")) {
                this.Type = EPCType.SSCC;
                this.SerialLotNumber = epcStr.substring(epcStr.lastIndexOf(":") + 1);
            } else if (epcStr.startsWith("urn:epc:class:lgtin:")) {
                this.Type = EPCType.Class;

                List<String> parts = new ArrayList<>(Arrays.asList(epcStr.split(":")));
                List<String> parts2 = new ArrayList<>(Arrays.asList(parts.get(parts.size() - 1).split("\\.")));
                parts.remove(parts.size() - 1);

                String gtinStr = String.join(":", parts) + ":" + parts2.get(0) + "." + parts2.get(1);
                if (parts2.size() > 2) {
                    this.SerialLotNumber = parts2.get(2);
                }
                gtinStr = gtinStr.replace(":class:lgtin:", ":idpat:sgtin:");
                this.GTIN = new GTIN(gtinStr);
            }
            // rest of the code for constructor...

        } catch (Exception ex) {
            Exception exception = new Exception("The EPC is not in a valid format and could not be parsed. EPC=" + epcStr, ex);
            OTLogger.error(ex);
            throw exception;
        }
    }

    // Rest of the class code...

    public static String DetectEPCIssue(String epcStr) throws Exception {
        try {
            if (epcStr == null || epcStr.trim().isEmpty()) {
                return "The EPC is a NULL or White Space string.";
            }

            if (epcStr.startsWith("urn:epc:class:lgtin:")) {
                if (!StringExtensions.IsURICompatibleChars(epcStr)) {
                    return "The EPC contains non-compatible characters for a URN format.";
                }

                String[] parts = epcStr.split(":");
                String[] parts2 = parts[parts.length - 1].split("\\.");

                if (parts2.length < 3) {
                    return "The EPC " + epcStr + " is not in the right format. It doesn't contain a company prefix, item code, and lot number.";
                } else {
                    return null;
                }
            }
            // Rest of the function...
        } catch (Exception ex) {
            OTLogger.error(ex);
            throw ex;
...
            if (epcStr.startsWith("urn:epc:class:lgtin:")) {
                setType(EPCType.Class);

                List<String> parts = new ArrayList<>(Arrays.asList(epcStr.split(":")));
                List<String> parts2 = new ArrayList<>(Arrays.asList(parts.get(parts.size() - 1).split("\\.")));
                parts.remove(parts.size() - 1);

                String gtinStr = String.join(":", parts) + ":" + parts2.get(0) + "." + parts2.get(1);
                if (parts2.size() > 2) {
                    setSerialLotNumber(parts2.get(2));
                }
                gtinStr = gtinStr.replace(":class:lgtin:", ":idpat:sgtin:");
                setGTIN(new GTIN(gtinStr));
            }
            // else if this is a GS1 instance level epc (GS1 GTIN + Serial Number)
            else if (epcStr.startsWith("urn:epc:id:sgtin:")) {
                setType(EPCType.Instance);

                List<String> parts = new ArrayList<>(Arrays.asList(epcStr.split(":")));
                List<String> parts2 = new ArrayList<>(Arrays.asList(parts.get(parts.size() - 1).split("\\.")));
                parts.remove(parts.size() - 1);

                String gtinStr = String.join(":", parts) + ":" + parts2.get(0) + "." + parts2.get(1);
                if (parts2.size() > 2) {
                    setSerialLotNumber(parts2.get(2));
                }
                gtinStr = gtinStr.replace(":id:sgtin:", ":idpat:sgtin:");
                setGTIN(new GTIN(gtinStr));
            }
            // else if this is a GDST / IBM private class level identifier (GTIN + Lot Number)
            else if (epcStr.startsWith("urn:") && epcStr.contains(":product:lot:class:")) {
                setType(EPCType.Class);

                List<String> parts = new ArrayList<>(Arrays.asList(epcStr.split(":")));
                List<String> parts2 = new ArrayList<>(Arrays.asList(parts.get(parts.size() - 1).split("\\.")));
                parts.remove(parts.size() - 1);

                String gtinStr = String.join(":", parts) + ":" + parts2.get(0) + "." + parts2.get(1);
                if (parts2.size() > 2) {
                    setSerialLotNumber(parts2.get(2));
                }
                gtinStr = gtinStr.replace(":product:lot:class:", ":product:class:");
                setGTIN(new GTIN(gtinStr));
            }
            // else if this is a GDST / IBM private instance level identifier (GTIN + Serial Number)
            else if (epcStr.startsWith("urn:") && epcStr.contains(":product:serial:obj:")) {
                setType(EPCType.Instance);

                List<String> parts = new ArrayList<>(Arrays.asList(epcStr.split(":")));
                List<String> parts2 = new ArrayList<>(Arrays.asList(parts.get(parts.size() - 1).split("\\.")));
                parts.remove(parts.size() - 1);

                String gtinStr = String.join(":", parts) + ":" + parts2.get(0) + "." + parts2.get(1);
                if (parts2.size() > 2) {
                    setSerialLotNumber(parts2.get(2));
                }
                gtinStr = gtinStr.replace(":product:serial:obj:", ":product:class:");
                setGTIN(new GTIN(gtinStr));
            } else if (epcStr.startsWith("urn:sscc:")) {
                setType(EPCType.SSCC.
                        GTIN gtinStr = new GTIN(gtin);
                this.GTIN = gtinStr;
            } else if (epcStr.startsWith("urn:") && epcStr.contains(":lpn:obj:")) {
                this.Type = EPCType.SSCC;
            } else if (epcStr.startsWith("urn:epc:id:bic:")) {
                this.Type = EPCType.SSCC;
            } else if (isWellFormedUriString(epcStr) && epcStr.startsWith("http") && epcStr.contains("/obj/")) {
                this.Type = EPCType.Instance;
                this.SerialLotNumber = epcStr.split('/')[epcStr.split('/').length - 1];
            } else if (isWellFormedUriString(epcStr) && epcStr.startsWith("http") && epcStr.contains("/class/")) {
                this.Type = EPCType.Class;
                this.SerialLotNumber = epcStr.split('/')[epcStr.split('/').length - 1];
            } else if (isWellFormedUriString(epcStr)) {
                this.Type = EPCType.URI;
            }
        } catch (Exception ex) {
            Exception exception = new Exception("The EPC is not in a valid format and could not be parsed. EPC=" + epcStr, ex);
            OTLogger.error(ex);
            throw exception;
        }
    }

    public EPC(EPCType type, GTIN gtin, String lotOrSerial) {
        if (type == EPCType.Class) {
            String epc = (gtin.toString().toLowerCase() + "." + lotOrSerial);
            if (epc.contains(":product:class:")) {
                epc = epc.replace(":product:class:", ":product:lot:class:");
            } else if (epc.contains(":idpat:sgtin:")) {
                epc = epc.replace(":idpat:sgtin:", ":class:lgtin:");
            } else {
                throw new Exception("Unrecognized GTIN pattern. " + gtin.toString());
            }
            this.Type = type;
            this.GTIN = gtin;
            this.SerialLotNumber = lotOrSerial;
            this._epcStr = epc;
        } else if (type == EPCType.Instance) {
            String epc = (gtin.toString().toLowerCase() + "." + lotOrSerial);
            if (epc.contains(":product:class:")) {
                epc = epc.replace(":product:class:", ":product:serial:obj:");
            } else if (epc.contains(":idpat:sgtin:")) {
                epc = epc.replace(":idpat:sgtin:", ":id:sgtin:");
            } else {
                throw new Exception("Unrecognized GTIN pattern. " + gtin.toString());
            }
            this.Type = type;
            this.GTIN = gtin;
            this.SerialLotNumber = lotOrSerial;
            this._epcStr = epc;
        } else {
            throw new Exception("Cannot build EPC of type " + type + " with a GTIN and Lot/Serial number.");
        }
    }

    public static String DetectEPCIssue(String epcStr) {
        try {
            if (epcStr == null || epcStr.trim().isEmpty()) {
                return "The EPC is a NULL or White Space string.";
            }

            // if this is a GS1 class level epc (GS1 GTIN + Lot Number)
            if (epcStr.startsWith("urn:epc:class:lgtin:")) {
                if (!isURICompatibleChars(epcStr)) {
                    return "The EPC contains non-compatible characters for a URN format.";
                }

                String[] parts = epcStr.split(":");
                String[] parts2 = parts[parts.length - 1].split("\\.");

                if (parts2.length < 3) {
                    return "The EPC " + epcStr + " is not in the right format. It doesn't contain a company prefix, item code, and lot number.";
                } else {
                    return null;
                }
            }

            // rest of DetectEPCIssue function here...
        } catch (Exception ex) {
            OTLogger.error(ex);
            throw ex;
        }
    }

    // Define getters and setters for each attribute here
}
}
