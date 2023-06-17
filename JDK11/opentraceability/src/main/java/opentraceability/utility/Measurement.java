package opentraceability.utility;

import org.w3c.dom.Element;

import java.util.ArrayList;

public class Measurement implements Comparable<Measurement> {
    public double value = 0.0;
    public UOM uom = new UOM();

    public Measurement() {

    }

    public Measurement(Element xmlElement) {
        value = xmlElement.getAttribute("Value") != null ? Double.parseDouble(xmlElement.getAttribute("Value")) : 0.0;
        uom =  UOMS.getUOMFromName(xmlElement.getAttribute("UoM") != null ? xmlElement.getAttribute("UoM") : "");
    }

    public Measurement(Measurement copyFrom) {
        value = copyFrom.value;
        uom = new UOM(copyFrom.uom);
    }

    public Measurement(double value, UOM unitCode) {
        this.value = value;
        uom = unitCode;
    }

    public Measurement(double value, String unitCode) {
        this.value = value;
        uom = UOMS.getUOMFromName(unitCode);
    }

    public void add(Measurement measurement) throws Exception {
        try {
            if (measurement != null) {
                value += measurement.value;
            }
        } catch (Exception ex) {
            opentraceability.OTLogger.error(ex);
            throw ex;
        }
    }

    public Measurement plus(Measurement right) throws Exception {
        if (uom.UnitDimension != right.uom.UnitDimension) {
            throw new Exception("All operands must be of the same unit dimension. Left UoM = " + uom.UNCode + " | Right UoM = " + right.uom.UNCode + ".");
        }

        double rightValue = right.uom.convert(right.value, uom);
        double sum = value + rightValue;

        return new Measurement(sum, uom);
    }

    public Measurement minus(Measurement right) throws Exception {
        if (uom.UnitDimension != right.uom.UnitDimension) {
            throw new Exception("All operands must be of the same unit dimension.");
        }

        double rightValue = right.uom.convert(right.value, uom);
        double diff = value - rightValue;
        return new Measurement(diff, uom);
    }

    public Measurement times(double factor) throws Exception {
        double newValue = value * factor;
        return new Measurement(newValue, uom);
    }

    public Measurement toBase() throws Exception {
        if (uom.UNCode == null || uom.UNCode.trim().equals("")) return this;

        Measurement trBase = new Measurement();
        trBase.uom = UOMS.getBase(uom);

        if (trBase.uom.UNCode == null || trBase.uom.UNCode.trim().equals("")) throw new NullPointerException("Failed to look up base UoM. UNCode=" + uom.UNCode);

        trBase.value = uom.convert(value, trBase.uom);
        trBase.value = Math.round(trBase.value);
        return trBase;
    }

    public Measurement convertTo(String uomStr) throws Exception {
        if (uomStr == null || uomStr.trim().equals("")) {
            return this;
        }
        Measurement trBase = new Measurement();
        trBase.uom = UOMS.getUOMFromUNCode(uomStr);
        if (trBase.uom == null) {
            return this;
        }
        trBase.value = uom.convert(value, trBase.uom);
        trBase.value = Math.round(trBase.value);
        return trBase;
    }

    public Measurement convertTo(UOM uom) throws Exception {
        if (uom == null) {
            return this;
        }
        Measurement trBase = new Measurement();
        trBase.uom = uom;
        trBase.value = uom.convert(value, trBase.uom);
        trBase.value = Math.round(trBase.value);
        return trBase;
    }

    @Override
    public String toString() {
        try {
            String str = String.valueOf(value);
            str += " " + uom.UNCode;
            return str;
        } catch (Exception ex) {
            opentraceability.OTLogger.error(ex);
            throw ex;
        }
    }

    public String toStringEx() {
        try {
            String str = String.valueOf(value);
            str += " " + uom.Abbreviation;
            return str;
        } catch (Exception ex) {
            opentraceability.OTLogger.error(ex);
            throw ex;
        }
    }

    public String getUniquenessKey(int iVersion) throws Exception {
        try {
            return value == 0.0 && uom == null ? "" : toBase().toString().trim();
        } catch (Exception ex) {
            opentraceability.OTLogger.error(ex);
            throw ex;
        }
    }

    public static Measurement parse(String strValue) throws Exception {
        try {
            if (strValue == null || strValue.trim().equals("")) {
                Measurement emptyMeasurement = new Measurement();
                return emptyMeasurement;
            }

            String numberStr = "";
            String uomStr = "";

            String[] strParts = strValue.split(" ");
            if (strParts.length != 2) {
                throw new Exception("Invalid Measurment string encountered, value=" + strValue + ". String must have a value and the UOM UN Code.");
            }
            numberStr = strParts[0].trim();
            uomStr = strParts[1].trim();

            ArrayList<UOM> uoms = UOMS.getList();

            double dblValue = Double.parseDouble(numberStr);
            UOM uom = UOMS.getUOMFromUNCode(uomStr);
            if (uom == null) {
                for (UOM tempUOM : uoms) {
                    if (tempUOM.Abbreviation.equalsIgnoreCase(uomStr) || uomStr.equalsIgnoreCase(tempUOM.Name)) {
                        uom = tempUOM;
                        break;
                    }
                }
            }

            if (uom == null) {
                throw new Exception("Failed to recognize UoM while parsing a TRMeasurement from a string. String=" + strValue + ", Value=" + numberStr + ", UoM=" + uomStr);
            }

            Measurement measurement = new Measurement();
            measurement.value = dblValue;
            measurement.uom = uom;
            return measurement;
        } catch (Exception ex) {
            opentraceability.OTLogger.error(ex);
            throw ex;
        }
    }

    public static Measurement TryParse(String strValue) {
        Measurement measure = null;
        try {
            if (strValue != null && !strValue.trim().equals("")) {
                measure = parse(strValue);
            }
        } catch (Exception ex) {

        }
        return measure;
    }

    @Override
    public boolean equals(Object other) {
        if (other != null && other instanceof Measurement) {
            Measurement otherMeasurement = (Measurement) other;
            return value == otherMeasurement.value && uom == otherMeasurement.uom;
        }
        return false;
    }

    @Override
    public int compareTo(Measurement other) {
        if (other == null) {
            return 1;
        }

        Measurement thisBase = null;
        try {
            thisBase = toBase();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        Measurement otherBase = null;
        try {
            otherBase = other.toBase();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        if (thisBase.value == otherBase.value) {
            return 0;
        } else if (thisBase.value < otherBase.value) {
            return -1;
        } else {
            return 1;
        }
    }

    @Override
    public int hashCode() {
        return Double.hashCode(value) + (uom != null ? uom.hashCode() : 0);
    }
}