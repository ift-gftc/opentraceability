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

        this.Name = juom.has("name") ? juom.getString("name") : null;
        this.UNCode = juom.has("UNCode") ? juom.getString("UNCode") : null;
        this.Abbreviation = juom.has("symbol") ? juom.getString("symbol") : null;
        this.UnitDimension = juom.has("type") ? juom.getString("type") : null;

        Offset = juom.has("offset") ? juom.getDouble("offset") : 0.0;

        Object multiplier = juom.has("multiplier") ? juom.get("multiplier") : null;
        if (multiplier instanceof String) {
            String multiplierString = (String)multiplier;
            int numerator = Integer.parseInt(multiplierString.split("/")[0]);
            int denominator = Integer.parseInt(multiplierString.split("/")[1]);
            B = numerator;
            C = denominator;
        } else {
            B = Double.parseDouble(multiplier.toString());
        }
    }

    public static boolean isNullOrEmpty(UOM uom) {
        return uom == null || uom.Abbreviation.isEmpty();
    }

    private static List<UOM> uomList = null;
    private static final ReentrantLock uomListLock = new ReentrantLock();

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