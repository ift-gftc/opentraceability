package opentraceability.utility;

public class XAttribute {
    public String Name = null;
    public String LocalName = null;
    public String Namespace = null;
    public String Value = null;

    public XAttribute(String ns, String name, String localName, String value)
    {
        this.Name = name;
        this.LocalName = localName;
        this.Namespace = ns;
        this.Value = value;
    }

    public XAttribute(String ns, String name, String value)
    {
        this.Name = name;
        this.Namespace = ns;
        this.Value = value;
    }

    public XAttribute(String name, String value)
    {
        this.Name = name;
        this.Value = value;
    }

    public XAttribute()
    {

    }
}
