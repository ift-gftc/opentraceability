package opentraceability.models.identifiers;

import opentraceability.utility.*;
import opentraceability.OTLogger;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.MalformedURLException;
import java.util.*;
import java.lang.Exception;

public class EPC {
    private String epcStr = "";

    public EPCType Type;
    public GTIN GTIN = null;
    public String SerialLotNumber = null;

    public EPC() { }

    public EPC(String str) throws Exception {
        String error = EPC.DetectEPCIssue(str);

        if (error == null || error.trim().isEmpty()) {
            throw new Exception(String.format("The EPC %s is invalid. %s", str, error));
        } else if (str == null) {
            throw new IllegalArgumentException("str is null");
        }

        this.epcStr = str;

        // if this is a GS1 class level epc (GS1 GTIN + Lot Number)
        if (str.startsWith("urn:epc:id:sscc:")) {
            this.Type = EPCType.SSCC;
            String[] parts = str.split(":");
            this.SerialLotNumber = parts[parts.length - 1];
        }
        else if (str.startsWith("urn:epc:class:lgtin:") || str.startsWith("urn:epc:id:sgtin:")
                || str.startsWith("urn:") && str.contains(":product:lot:class:")
                || str.startsWith("urn:") && str.contains(":product:serial:obj:")) {

            List<String> parts = new ArrayList<>(Arrays.asList(str.split(":")));
            List<String> parts2 = new ArrayList<>(Arrays.asList(parts.get(parts.size() - 1).split("\\.")));
            parts.remove(parts.size() - 1);

            String gtinStr = String.join(":", parts) + ":" + parts2.get(0) + "." + parts2.get(1);

            if(str.startsWith("urn:epc:class:lgtin:") || str.startsWith("urn:epc:id:sgtin:")){
                gtinStr = gtinStr.replace(":class:lgtin:", ":idpat:sgtin:");
                gtinStr = gtinStr.replace(":id:sgtin:", ":idpat:sgtin:");
                this.Type = EPCType.Class;
            } else {
                gtinStr = gtinStr.replace(":product:lot:class:", ":product:class:");
                gtinStr = gtinStr.replace(":product:serial:obj:", ":product:class:");
                this.Type = EPCType.Instance;
            }

            this.SerialLotNumber = parts2.get(2);
            this.GTIN = new GTIN(gtinStr);
        }
        else if (str.startsWith("urn:sscc:") || (str.startsWith("urn:") && str.contains(":lpn:obj:"))
                || str.startsWith("urn:epc:id:bic:")) {
            this.Type = EPCType.SSCC;
        }
        else if (StringExtensions.isURICompatibleChars(str) && str.startsWith("http") && str.contains("/obj/")) {
            this.Type = EPCType.Instance;
            String[] parts = str.split("/");
            this.SerialLotNumber = parts[parts.length - 1];
        }
        else if (StringExtensions.isURICompatibleChars(str) && str.startsWith("http") && str.contains("/class/")) {
            this.Type = EPCType.Class;
            String[] parts = str.split("/");
            this.SerialLotNumber = parts[parts.length - 1];
        }
        else if (StringExtensions.isURICompatibleChars(str)) {
            this.Type = EPCType.URI;
        }
    }

    public EPC(EPCType type, GTIN gtin, String lotOrSerial) throws Exception {
        String epc = (gtin.toString().toLowerCase() + "." + lotOrSerial);

        if (type == EPCType.Class) {
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
            this.epcStr = epc;
        } else if (type == EPCType.Instance) {
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
            this.epcStr = epc;
        } else {
            throw new Exception("Cannot build EPC of type " + type + " with a GTIN and Lot/Serial number.");
        }
    }

    public static String DetectEPCIssue(String epcStr) {
        if (epcStr == null || epcStr.trim().isBlank()) {
            return "The EPC is a NULL or White Space string.";
        }

        if (!StringExtensions.isURICompatibleChars(epcStr)) {
            return "The EPC contains non-compatiable characters for a URN format.";
        }

        String[] parts = epcStr.split(":");
        String[] parts2 = parts[parts.length - 1].split("\\.");

        if (!epcStr.startsWith("urn:epc:class:lgtin:")
                && !epcStr.startsWith("urn:epc:id:sgtin:")
                && (!epcStr.startsWith("urn:") && !epcStr.contains(":product:lot:class:"))
                && (!epcStr.startsWith("urn:") && !epcStr.contains(":product:serial:obj:"))
                && !epcStr.startsWith("urn:epc:id:sscc:")
                && !epcStr.startsWith("urn:epc:id:bic:")
                && (!epcStr.startsWith("urn:") && !epcStr.contains(":lpn:obj:"))
                && !(StringExtensions.isURICompatibleChars(epcStr) && epcStr.startsWith("http") && epcStr.contains("/obj/"))
                && !(StringExtensions.isURICompatibleChars(epcStr) && epcStr.startsWith("http") && epcStr.contains("/class/"))
                && !StringExtensions.isURICompatibleChars(epcStr)) {
            return "This EPC does not fit any of the allowed formats.";
        }

        if (parts2.length < 3) {
            return String.format("The EPC %s is not in the right format. It doesn't contain a company prefix, item code, and lot number.", epcStr);
        } else {
            return null;
        }
    }

    public EPC Clone() throws Exception {
        EPC epc = new EPC(this.toString());
        return epc;
    }

    // Define getters and setters for each attribute here
    public Boolean matches(EPC targetEPC)
    {
        if (this.equals(targetEPC))
        {
            return true;
        }
        else if (this.SerialLotNumber == "*" && this.GTIN == targetEPC.GTIN)
        {
            return true;
        }
        return false;
    }

    public static boolean equals(EPC obj1, EPC obj2) {
        if (Objects.equals(obj1, null) && Objects.equals(obj2, null)) {
            return true;
        }

        if (!Objects.equals(obj1, null) && Objects.equals(obj2, null)) {
            return false;
        }

        if (Objects.equals(obj1, null) && !Objects.equals(obj2, null)) {
            return false;
        }

        if (obj1 == null) {
            return false;
        }

        return obj1.equals(obj2);
    }

    public static boolean notEquals(EPC obj1, EPC obj2) {
        try {
            if (Objects.equals(obj1, null) && Objects.equals(obj2, null)) {
                return false;
            }

            if (!Objects.equals(obj1, null) && Objects.equals(obj2, null)) {
                return true;
            }

            if (Objects.equals(obj1, null) && !Objects.equals(obj2, null)) {
                return true;
            }

            if (obj1 == null) {
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
            if (Objects.equals(obj, null)) {
                return false;
            }

            if (Objects.equals(this, obj)) {
                return true;
            }

            if (obj.getClass() != this.getClass()) {
                return false;
            }

            return EPC.equals(this, (EPC) obj);
        }
        catch (Exception ex)
        {
            OTLogger.error(ex);
            throw ex;
        }
    }

    @Override
    public int hashCode() {
        try {
            int hash = this.toString().hashCode();
            return hash;
        } catch (Exception ex) {
            OTLogger.error(ex);
            throw ex;
        }
    }

    @Override
    public String toString() {
        try {
            return epcStr.toLowerCase();
        } catch (Exception ex) {
            OTLogger.error(ex);
            throw ex;
        }
    }
}

