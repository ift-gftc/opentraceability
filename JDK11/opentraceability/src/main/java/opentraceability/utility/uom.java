package opentraceability.utility;

import opentraceability.OTLogger;
import org.json.JSONObject;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.locks.ReentrantLock;

public class UOM {
    Object _locker = new Object();
    public String Name = "";
    public String Abbreviation = "";
    public String UnitDimension = "";
    public String SubGroup = "";
    public String UNCode = "";
    public double A = 0.0;
    public double B = 0.0;
    public double C = 0.0;
    public double D = 0.0;
    public double Offset = 0.0;

    public UOM() {}

    public UOM(UOM uom) {
        this.Abbreviation = uom.Abbreviation;
        this.Name = uom.Name;
        this.UnitDimension = uom.UnitDimension;
        this.UNCode = uom.UNCode;
        this.SubGroup = uom.SubGroup;
        this.Offset = uom.Offset;
        this.A = uom.A;
        this.B = uom.B;
        this.C = uom.C;
        this.D = uom.D;
    }

    public UOM(JSONObject juom) throws Exception {
        this.A = 0.0;
        this.B = 1.0;
        this.C = 1.0;
        this.D = 0.0;

        this.Name = juom.has("name") ? juom.getString("name") : throw new Exception("name not set on uom json. " + juom.toString());
        this.UNCode = juom.has("UNCode") ? juom.getString("UNCode") : throw new Exception("UNCode not set on uom json. " + juom.toString());
        this.Abbreviation = juom.has("symbol") ? juom.getString("symbol") : throw new Exception("symbol not set on uom json. " + juom.toString());
        this.UnitDimension = juom.has("type") ? juom.getString("type") : throw new Exception("type not set on uom json. " + juom.toString());

        String offsetString = juom.has("offset") ? juom.getString("offset") : null;
        if (offsetString != null) {
            Offset = Double.valueOf(offsetString);
        }

        String multiplierString = juom.has("multiplier") ? juom.getString("multiplier") : throw new Exception("multiplier not set on uom json. " + juom.toString());
        if (multiplierString.contains("/")) {
            int numerator = Integer.parseInt(multiplierString.split("/")[0]);
            int denominator = Integer.parseInt(multiplierString.split("/")[1]);
            B = numerator;
            C = denominator;
        } else {
            String multiplier = juom.has("multiplier") ? juom.getString("multiplier") : throw new Exception("multiplier not set on uom json. " + juom.toString());
            B = Double.valueOf(multiplier);
        }
    }

    public static UOM lookUpFromUNCode(String unCode) {
        Object[] lockObjs = new Object[]{new Object()};
        return uomListLock.withLock(new Function0<UOM>() {
            public UOM invoke() {
                for (UOM uom : getUOMList()) {
                    if (uom.UNCode == unCode) {
                        return uom;
                    }
                }              
                return new UOM();
            }
        });
    }

    public static boolean isNullOrEmpty(UOM uom) {
        return uom == null || uom.Abbreviation.isEmpty();
    }

    public static UOM parseFromName(String name) {
        try {
            if (name == null || name.isEmpty()) {
                throw new IllegalArgumentException("name");
            }

            UOM uom = getUOMList().stream().filter(u -> u.Name == name).findFirst().orElse(null);
            if (uom == null) {
                uom = getUOMList().stream().filter(u -> u.UNCode.equalsIgnoreCase(name)).findFirst().orElse(null);
            }
            if (uom == null) {
                throw new Exception("Failed to parse UOM");
            }

            return new UOM(uom);
        }
        catch (Exception ex)
        {
            OTLogger.error(ex);
            throw ex; 
        }
    }

    private static List<UOM> uomList = null;
    private static ReentrantLock uomListLock = new ReentrantLock();

    public static List<UOM> getUOMList() {
        if (uomList == null) {
            uomList = loadUOMList();
        }
        return uomList;
    }

    public static List<UOM> loadUOMList() {
        return new ArrayList<>();
    }

    public void CopyFrom(UOM uom) {
        this.Abbreviation = uom.Abbreviation;
        this.Name = uom.Name;
        this.UnitDimension = uom.UnitDimension;
        this.UNCode = uom.UNCode;
        this.SubGroup = uom.SubGroup;
        this.Offset = uom.Offset;
        this.A = uom.A;
        this.B = uom.B;
        this.C = uom.C;
        this.D = uom.D;
    }

    public boolean isBase() {
        return A == 0.0 && B == 1.0 && C == 1.0 && D == 0.0;
    }

    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (!(other instanceof UOM)) {
            return false;
        }
        UOM otherUOM = (UOM) other;
        return UNCode.equals(otherUOM.UNCode);
    }

    public int hashCode() {
        return UNCode.hashCode();
    }

    public String key() {
        return Abbreviation;
    }

    public double convert(double value, UOM to) {
        double valueBase = toBase(value);
        double valueNew = to.fromBase(valueBase);
        return valueNew;
    }

    public double toBase(double value) {
        double baseValue = ((A + B * value) / (C + D * value)) - Offset;
        return baseValue;
    }

    public double fromBase(double baseValue) {
        double value = ((A - C * baseValue) / (D * baseValue - B)) + Offset;
        return value;
    }

    public String toString() {
        return Name + " [" + Abbreviation + "]";
    }
}