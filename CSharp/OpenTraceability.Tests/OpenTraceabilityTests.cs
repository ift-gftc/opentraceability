using OpenTraceability.Utility;
using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;
using System.Xml.Linq;

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

        private static void XMLCompare(XElement primary, XElement secondary)
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
                    if (val1 != val2)
                    {
                        if (!TryAdvancedValueCompare(val1, val2))
                        {
                            Assert.Fail($"The XML attribute {attr.Name} value does not match where the original is {val1} and the after is {val2}.\nprimary xml:\n{primary.ToString()}\n\nsecondary xml:\n{secondary.ToString()}");
                        }
                    }
                }
            }
            if (primary.HasElements)
            {
                if (primary.Elements().Count() != secondary.Elements().Count())
                {
                    Assert.Fail($"The XML child elements count does not match.\nprimary xml:\n{primary.ToString()}\n\nsecondary xml:\n{secondary.ToString()}");
                }
                for (var i = 0; i <= primary.Elements().Count() - 1; i++)
                {
                    XElement child1 = primary.Elements().Skip(i).Take(1).Single();
                    XElement child2 = secondary.Elements().Skip(i).Take(1).Single();
                    XMLCompare(child1, child2);
                }
            }
            else if (primary.Value != secondary.Value)
            {
                if (!TryAdvancedValueCompare(primary.Value, secondary.Value))
                {
                    Assert.Fail($"The XML element value does not match where name={primary.Name} and value={primary.Value} with the after value={secondary.Value}.\nprimary xml:\n{primary.ToString()}\n\nsecondary xml:\n{secondary.ToString()}");
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
