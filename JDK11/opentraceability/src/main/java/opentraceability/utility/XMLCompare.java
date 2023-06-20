package opentraceability.utility;

import tangible.ListHelper;
import tangible.StringHelper;

import javax.xml.xpath.XPathExpressionException;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class XMLCompare {
    public static void CompareXML(String xml, String xmlAfter) throws Exception {
        XElement x1 = XElement.Parse(xml);
        XElement x2 = XElement.Parse(xmlAfter);
        XMLCompare(x1, x2, false);
    }

    private static void XMLCompare(XElement primary, XElement secondary, Boolean noAssertions) throws Exception
    {
        if (!primary.getTagName().equals(secondary.getTagName()))
        {
            throw new Exception(String.format("The XML element name does not match where name=%s.\nprimary xml:\n%s\n\nsecondary xml:\n%s", primary.getTagName(), primary.toString(), secondary.toString()));
        }

        if (!primary.Attributes().isEmpty())
        {
            if (primary.Attributes().size() != secondary.Attributes().size())
            {
                throw new RuntimeException(String.format("The XML attribute counts do not match.%nprimary xml:%n%s%n%nsecondary xml:%n%s%n",
                        primary.toString(), secondary.toString()));
            }

            for (XAttribute attr: primary.Attributes())
            {
                if (secondary.Attribute(attr.Name) == null)
                {
                    throw new Exception(String.format("The XML attribute %s was not found on the secondary xml.%nprimary xml:%n%s%n%nsecondary xml:%n%s%n",
                            attr.Name, primary.toString(), secondary.toString()));
                }

                String val1 = attr.Value;
                String val2 = secondary.Attribute(attr.Name);
                if (!val1.toLowerCase().equals(val2.toLowerCase()))
                {
                    if (!TryAdvancedValueCompare(val1, val2))
                    {
                        String errorMessage = String.format("The XML attribute %s value does not match where the original is %s and the after is %s.%nprimary xml:%n%s%n%nsecondary xml:%n%s%n",
                                attr.Name, val1, val2, primary.toString(), secondary.toString());
                        throw new Exception(errorMessage);
                    }
                }
            }
        }

        if (primary.HasElements() || secondary.HasElements())
        {
            if (primary.Elements().size() != secondary.Elements().size())
            {
                List<String> eles1 = primary.Elements().stream().map(XElement::getTagName).collect(Collectors.toList());
                List<String> eles2 = secondary.Elements().stream().map(XElement::getTagName).collect(Collectors.toList());

                List<String> missing1 = eles1.stream().filter(e -> !eles2.contains(e)).collect(Collectors.toList());
                List<String> missing2 = eles2.stream().filter(e -> !eles1.contains(e)).collect(Collectors.toList());

                String errorMessage = String.format("The XML child elements count does not match.%nElements only in primary xml: %s%nElements only in secondary xml: %s%nprimary xml:%n%s%n%nsecondary xml:%n%s%n",
                        String.join(", ", missing1), String.join(", ", missing2), primary.toString(), secondary.toString());

                throw new RuntimeException(errorMessage);
            }

            for (var i = 0; i <= primary.Elements().size() - 1; i++)
            {
                XElement child1 = ListExtensions.FirstOrDefault(primary.Elements().stream().skip(i));

                // we will try and find the matching node...
                XElement xchild2 = FindMatchingNode(child1, secondary, i);
                if (xchild2 == null || xchild2.IsNull)
                {
                    String errorMessage = String.format("Failed to find matching node for comparison in the secondary xml.%nchild1=%s%nprimary xml:%n%s%n%nsecondary xml:%n%s%n",
                            child1, primary.toString(), secondary.toString());

                    throw new RuntimeException(errorMessage);
                }
                else
                {
                    XMLCompare(child1, xchild2, false);
                }
            }
        }
        else if (!primary.getValue().toLowerCase().equals(secondary.getValue().toLowerCase()))
        {
            if (!TryAdvancedValueCompare(primary.getValue(), secondary.getValue()))
            {
                String errorMessage = String.format("The XML element value does not match where name=%s and value=%s with the after value=%s.%nprimary xml:%n%s%n%nsecondary xml:%n%s%n",
                        primary.getTagName(), primary.getValue(), secondary.getValue(), primary.toString(), secondary.toString());

                throw new RuntimeException(errorMessage);
            }
        }
    }

    private static XElement FindMatchingNode(XElement xchild1, XElement x2, int i) throws Exception
    {
        // let's see if there is more than one node with the same element name...
        String tagName = xchild1.getTagName();
        if (x2.Elements(xchild1.getTagName()).size() == 0)
        {
            return null;
        }
        else if (x2.Elements(xchild1.getTagName()).size() == 1)
        {
            return x2.Element(xchild1.getTagName());
        }
        else
        {
            XElement xchild2 = null;
            if (x2.getTagName().equals("EventList"))
            {
                String[] eventxpaths = new String[] { "eventID", "baseExtension/eventID", "TransformationEvent/baseExtension/eventID" };
                for (String xp: eventxpaths)
                {
                    if (!xchild1.Element(xp).IsNull)
                    {
                        String eventid = xchild1.Element(xp).getValue();
                        xchild2 = ListExtensions.FirstOrDefault(x2.Elements().stream().filter(x -> {
                            try {
                                return x.Element(xp).getValue().equals(eventid);
                            } catch (XPathExpressionException e) {
                                throw new RuntimeException(e);
                            }
                        }));
                        return xchild2;
                    }
                }
            }

            String id = xchild1.Attribute("id");
            if (!StringHelper.isNullOrEmpty(id))
            {
                xchild2 = ListExtensions.FirstOrDefault(x2.Elements().stream().filter(x -> x.Attribute("id").equals(id)));
            }
            if (xchild2 == null)
            {
                // try and find by internal value...
                String value = xchild1.getValue();
                if (!StringHelper.isNullOrEmpty(value) && x2.Elements().stream().anyMatch(x -> x.getTagName().equals(xchild1.getTagName()) && x.getValue().equals(value)))
                {
                    xchild2 = ListExtensions.FirstOrDefault(x2.Elements().stream().filter(x -> x.getTagName().equals(xchild1.getTagName()) && x.getValue().equals(x.getValue())));
                }
                if (xchild2 == null)
                {
                    xchild2 = ListExtensions.FirstOrDefault(x2.Elements().stream().skip(i));
                    return xchild2;
                }
                else
                {
                    return xchild2;
                }
            }
            else
            {
                return xchild2;
            }
        }
    }

    private static Boolean TryAdvancedValueCompare(String str1, String str2)
    {
        return (TryCompareDouble(str1, str2) || TryCompareXMLDateTime(str1, str2));
    }

    private static Boolean TryCompareXMLDateTime(String str1, String str2)
    {
        if (str1 != null && str2 != null)
        {
            OffsetDateTime dt1 = StringExtensions.tryConvertToDateTimeOffset(str1);
            OffsetDateTime dt2 = StringExtensions.tryConvertToDateTimeOffset(str2);
            if (dt1 != null && dt2 != null)
            {
                return dt1.equals(dt2);
            }
        }
        return false;
    }

    private static Boolean TryCompareDouble(String str1, String str2)
    {
        if (str1 != null && str2 != null)
        {
            try {
                Double d1 = Double.parseDouble(str1);
                Double d2 = Double.parseDouble(str2);
                return d1.equals(d2);
            }
            catch (Exception e)
            {
                return false;
            }
        }
        return false;
    }
}
