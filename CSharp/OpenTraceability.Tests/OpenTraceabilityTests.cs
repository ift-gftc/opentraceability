using OpenTraceability.Utility;
using System;
using System.Collections.Generic;
using System.Linq;
using System.Reflection;
using System.Text;
using System.Threading.Tasks;
using System.Xml.Linq;
using System.Xml.XPath;

namespace OpenTraceability.Tests
{
    /// <summary>
    /// Utility class for the open traceability test cases.
    /// </summary>
    public static class OpenTraceabilityTests
    {
        internal static void CompareXML(string xml, string xmlAfter)
        {
            XElement x1 = XElement.Parse(xml);
            XElement x2 = XElement.Parse(xmlAfter);
            XMLCompare(x1, x2);
        }

        private static void XMLCompare(XElement primary, XElement secondary, bool noAssertions=false)
        {
            if (primary.Name != secondary.Name)
            {
                Assert.Fail($"The XML element name does not match where name={primary.Name}.\nprimary xml:\n{primary.ToString()}\n\nsecondary xml:\n{secondary.ToString()}");
            }

            if (primary.HasAttributes)
            {
                if (primary.Attributes().Count() != secondary.Attributes().Count())
                {
                    Assert.Fail($"The XML attribute counts to not match.\nprimary xml:\n{primary.ToString()}\n\nsecondary xml:\n{secondary.ToString()}");
                }

                foreach (XAttribute attr in primary.Attributes())
                {
                    if (secondary.Attribute(attr.Name) == null)
                    {
                        Assert.Fail($"The XML attribute {attr.Name} was not found on the secondary xml.\nprimary xml:\n{primary.ToString()}\n\nsecondary xml:\n{secondary.ToString()}");
                    }

                    string? val1 = attr.Value;
                    string? val2 = secondary.Attribute(attr.Name)?.Value;
                    if (val1?.ToLower() != val2?.ToLower())
                    {
                        if (!TryAdvancedValueCompare(val1, val2))
                        {
                            Assert.Fail($"The XML attribute {attr.Name} value does not match where the original is {val1} and the after is {val2}.\nprimary xml:\n{primary.ToString()}\n\nsecondary xml:\n{secondary.ToString()}");
                        }
                    }
                }
            }

            if (primary.HasElements || secondary.HasElements)
            {
                if (primary.Elements().Count() != secondary.Elements().Count())
                {
                    List<string> eles1 = primary.Elements().Select(s => s.Name.ToString()).ToList();
                    List<string> eles2 = secondary.Elements().Select(s => s.Name.ToString()).ToList();

                    List<string> missing1 = eles1.Where(e => !eles2.Contains(e)).ToList();
                    List<string> missing2 = eles2.Where(e => !eles1.Contains(e)).ToList();

                    Assert.Fail($"The XML child elements count does not match.\nElements only in primary xml: {string.Join(", ", missing1)}\nElements only in secondary xml: {string.Join(", ", missing2)}\nprimary xml:\n{primary.ToString()}\n\nsecondary xml:\n{secondary.ToString()}");
                }
                for (var i = 0; i <= primary.Elements().Count() - 1; i++)
                {
                    XElement child1 = primary.Elements().Skip(i).Take(1).Single();

                    // we will try and find the matching node...
                    XElement? xchild2 = FindMatchingNode(child1, secondary, i);
                    if (xchild2  == null)
                    {
                        Assert.Fail($"Failed to find matching node for comparison in the secondary xml.\nchild1={child1}\nprimary xml:\n{primary.ToString()}\n\nsecondary xml:\n{secondary.ToString()}");
                    }

                    XMLCompare(child1, xchild2);
                }
            }
            else if (primary.Value.ToLower() != secondary.Value.ToLower())
            {
                if (!TryAdvancedValueCompare(primary.Value, secondary.Value))
                {
                    Assert.Fail($"The XML element value does not match where name={primary.Name} and value={primary.Value} with the after value={secondary.Value}.\nprimary xml:\n{primary.ToString()}\n\nsecondary xml:\n{secondary.ToString()}");
                }
            }
        }

        static XElement? FindMatchingNode(XElement xchild1, XElement x2, int i)
        {
            // lets see if there is more than one node with the same element name...
            if (x2.Elements(xchild1.Name).Count() == 0)
            {
                return null;
            }
            else if (x2.Elements(xchild1.Name).Count() == 1)
            {
                return x2.Element(xchild1.Name);
            }
            else
            {
                XElement? xchild2 = null;
                if (x2.Name == "EventList")
                {
                    List<string> eventxpaths = new List<string>() { "eventID", "baseExtension/eventID", "TransformationEvent/baseExtension/eventID" };
                    foreach (string xp in eventxpaths)
                    {
                        if (xchild1.XPathSelectElement(xp) != null)
                        {
                            string? eventid = xchild1.XPathSelectElement(xp)?.Value;
                            xchild2 = x2.Elements().FirstOrDefault(x => x.XPathSelectElement(xp)?.Value == eventid);
                            return xchild2;
                        }
                    }
                }
                
                string id = xchild1.Attribute("id")?.Value ?? string.Empty;
                if (!string.IsNullOrEmpty(id))
                {
                    xchild2 = x2.Elements().FirstOrDefault(x => x.Attribute("id")?.Value == id);
                }
                if (xchild2 == null)
                {
                    // try and find by internal value...
                    string value = xchild1.Value;
                    if (!string.IsNullOrEmpty(value) && x2.Elements().Where(x => x.Name == xchild1.Name && x.Value == value).Count() == 1)
                    {
                        xchild2 = x2.Elements().Where(x => x.Name == xchild1.Name && x.Value == value).FirstOrDefault();
                    }
                    if (xchild2 == null)
                    {
                        xchild2 = x2.Elements().Skip(i).Take(1).Single();
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

        internal static bool TryAdvancedValueCompare(string? str1, string? str2)
        {
            return (TryCompareDouble(str1, str2) 
                || TryCompareXMLDateTime(str1, str2));
        }

        internal static bool TryCompareXMLDateTime(string? str1, string? str2)
        {
            if (str1 != null && str2 != null)
            {
                DateTimeOffset? dt1 = str1.TryConvertToDateTimeOffset();
                DateTimeOffset? dt2 = str2.TryConvertToDateTimeOffset();
                if (dt1 != null && dt2 != null)
                {
                    return dt1.Value.Equals(dt2);
                }
            }
            return false;
        }

        internal static bool TryCompareDouble(string? str1, string? str2)
        {
            if (str1 != null && str2 != null)
            {
                if (double.TryParse(str1, out double d1) && double.TryParse(str2, out double d2))
                {
                    return d1.Equals(d2);
                }
            }
            return false;
        }

        internal static string ReadTestData(string v)
        {
            EmbeddedResourceLoader loader = new EmbeddedResourceLoader();
            string str = loader.ReadString("OpenTraceability.Tests", $"OpenTraceability.Tests.Data.{v}");
            return str;
        }
    }
}
