using Microsoft.Extensions.Configuration;
using Newtonsoft.Json;
using Newtonsoft.Json.Linq;
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

        private static XElement? FindMatchingNode(XElement xchild1, XElement x2, int i)
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

        private static bool TryAdvancedValueCompare(string? str1, string? str2)
        {
            return (TryCompareDouble(str1, str2) 
                || TryCompareXMLDateTime(str1, str2));
        }

        private static bool TryCompareXMLDateTime(string? str1, string? str2)
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

        private static bool TryCompareDouble(string? str1, string? str2)
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


        internal static void CompareJSON(string json, string jsonAfter)
        {
            var settings = new JsonSerializerSettings { DateParseHandling = DateParseHandling.None };
            JObject j1 = JsonConvert.DeserializeObject<JObject>(json, settings) ?? throw new Exception("Failed to parse json from string. " + json);
            JObject j2 = JsonConvert.DeserializeObject<JObject>(jsonAfter, settings) ?? throw new Exception("Failed to parse json from string. " + jsonAfter);
            JSONCompare(j1, j2);
        }

        private static void JSONCompare(JObject j1, JObject j2)
        {
            List<JProperty> j1props = j1.Properties().ToList();
            List<JProperty> j2props = j2.Properties().ToList();

            // go through each property
            foreach (var prop in j1props)
            {
                if (j2[prop.Name] == null)
                {
                    Assert.Fail($"j1 has property {prop.Name}, but as not found on j2.\nh1={j1.ToString(Newtonsoft.Json.Formatting.Indented)}\nj2={j2.ToString(Newtonsoft.Json.Formatting.Indented)}");
                }

                if (j1[prop.Name]?.GetType() != j2[prop.Name]?.GetType())
                {
                    Assert.Fail($"j1 property value type for {prop.Name} is {j1[prop.Name]?.GetType()}, but on j2 it is {j2[prop.Name]?.GetType()}.\nh1={j1.ToString(Newtonsoft.Json.Formatting.Indented)}\nj2={j2.ToString(Newtonsoft.Json.Formatting.Indented)}");
                }

                JArray? jarr1 = j1[prop.Name] as JArray;
                if (jarr1 != null)
                {
#pragma warning disable CS8600 // Converting null literal or possible null value to non-nullable type.
                    JArray jarr2 = (JArray)j2[prop.Name];
#pragma warning restore CS8600 // Converting null literal or possible null value to non-nullable type.

                    if (jarr1.Count() != jarr2.Count())
                    {
                        Assert.Fail($"j1 property value type for {prop.Name} is an array with {jarr1.Count()} items, but the same property on j2 has only {jarr2.Count()} items.\nh1={j1.ToString(Newtonsoft.Json.Formatting.Indented)}\nj2={j2.ToString(Newtonsoft.Json.Formatting.Indented)}");
                    }

                    for(int i = 0; i < jarr1.Count(); i++)
                    {
                        JToken jt1 = jarr1[i];
                        JToken jt2 = jarr2[i];
                        if (jt1.GetType() != jt2.GetType())
                        {
                            Assert.Fail($"j1 property array {prop.Name} has item[{i}] with type {jt1.GetType()}, but on j2 it is {jt2.GetType()}.\nh1={j1.ToString(Newtonsoft.Json.Formatting.Indented)}\nj2={j2.ToString(Newtonsoft.Json.Formatting.Indented)}");
                        }

                        if (jt1 is JObject && jt2 is JObject)
                        {
                            JSONCompare((JObject)jt1, (JObject)jt2);
                        }
                        else
                        {
                            string? str1 = jt1.ToString();
                            string? str2 = jt2.ToString();
                            if (!TryAdvancedValueCompare(str1, str2))
                            {
                                if (str1?.ToLower() != str2?.ToLower())
                                {
                                    Assert.Fail($"j1 property array {prop.Name} has item[{i}] with value {str1}, but on j2 it the value is {str2}.\nh1={j1.ToString(Newtonsoft.Json.Formatting.Indented)}\nj2={j2.ToString(Newtonsoft.Json.Formatting.Indented)}");
                                }
                            }
                        }
                    }
                }
                else if (j1[prop.Name] is JObject)
                {
#pragma warning disable CS8600 // Converting null literal or possible null value to non-nullable type.
                    JObject jobj1 = (JObject)j1[prop.Name];
                    JObject jobj2 = (JObject)j2[prop.Name];

#pragma warning disable CS8604 // Possible null reference argument.
                    JSONCompare(jobj1, jobj2);
#pragma warning restore CS8604 // Possible null reference argument.
#pragma warning restore CS8600 // Converting null literal or possible null value to non-nullable type.
                }
                else
                {
                    string? str1 = j1[prop.Name]?.ToString();
                    string? str2 = j2[prop.Name]?.ToString();
                    if (!TryAdvancedValueCompare(str1, str2))
                    {
                        if (str1?.ToLower() != str2?.ToLower())
                        {
                            Assert.Fail($"j1 property {prop.Name} has string value {str1}, but on j2 it the value is {str2}.\nh1={j1.ToString(Newtonsoft.Json.Formatting.Indented)}\nj2={j2.ToString(Newtonsoft.Json.Formatting.Indented)}");
                        }
                    }
                }
            }
        }


        internal static string ReadTestData(string v)
        {
            EmbeddedResourceLoader loader = new EmbeddedResourceLoader();
            string str = loader.ReadString("OpenTraceability.Tests", $"OpenTraceability.Tests.Data.{v}");
            return str;
        }

        public static IConfiguration GetConfiguration(string appsettingsName)
        {
            // first we are going to remove any of the appsettings.json from our directory so that it 
            // does not interfere with anything...
            // this is because by referencing the web service / web application projects, it copies these files into
            // there because they are included in copy always / content
            string currentDirectory = Directory.GetCurrentDirectory();
            foreach (var file in (new DirectoryInfo(currentDirectory)).GetFiles("appsettings*"))
            {
                File.Delete(file.FullName);
            }

            var configBuilder = new ConfigurationBuilder();

            EmbeddedResourceLoader loader = new EmbeddedResourceLoader();
            var jsonString = loader.ReadString("OpenTraceability.Tests", $"OpenTraceability.Tests.Configurations.{appsettingsName}.json");
            var appsettings = JObject.Parse(jsonString);
            configBuilder.AddJsonStream(new MemoryStream(Encoding.UTF8.GetBytes(appsettings.ToString())));

            // try and load any machine dependent app settings for the unit test
            try
            {
                var maschineJsonStr = loader.ReadString("OpenTraceability.Tests", $"OpenTraceability.Tests.Configurations.AppSettings.{appsettingsName}.{Environment.MachineName}.json");
                var machineAppSettings = JObject.Parse(maschineJsonStr);
                configBuilder.AddJsonStream(new MemoryStream(Encoding.UTF8.GetBytes(machineAppSettings.ToString())));
            }
            catch (Exception ex)
            {
                if (!ex.Message.Contains("Failed to find the resource in the assembly"))
                {
                    throw;
                }
            }

            var config = configBuilder.Build() as IConfiguration;
            return config;
        }
    }
}
