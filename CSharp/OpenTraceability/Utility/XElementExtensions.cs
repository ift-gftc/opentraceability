using System;
using System.Collections.Generic;
using System.Globalization;
using System.Linq;
using System.Text;
using System.Threading.Tasks;
using System.Xml.Linq;

namespace OpenTraceability.Utility
{
    public static class XElementExtensions
    {
        public static Dictionary<string, string> GetDocumentNamespaces(this XDocument x)
        {
            if (x.Root == null) throw new Exception("Root on XDocument is null.");

            var result = x.Root.Attributes().
                         Where(a => a.IsNamespaceDeclaration).
                         GroupBy(a => a.Name.Namespace == XNamespace.None ? String.Empty : a.Name.LocalName,
                                a => XNamespace.Get(a.Value)).
                         ToDictionary(g => g.Key,
                                     g => g.First().NamespaceName);

            return result;
        }

        public static DateTime? AttributeISODateTime(this XElement x, string attName)
        {
            string? strValue = x.Attribute(attName)?.Value;

            if (!string.IsNullOrEmpty(strValue))
            {
                for (int i = 1; i <= 6; i++)
                {
                    string f = "".PadLeft(i, 'f');
                    if (DateTime.TryParseExact(strValue, $"yyyy-MM-ddTHH:mm:ss.{f}Z", CultureInfo.InvariantCulture, DateTimeStyles.None, out DateTime dt))
                    {
                        return dt;
                    }
                    else if (DateTime.TryParseExact(strValue, $"yyyy-MM-ddTHH:mm:ss.{f}K", CultureInfo.InvariantCulture, DateTimeStyles.None, out dt))
                    {
                        return dt;
                    }
                }
            }

            return null;
        }

        public static Uri? AttributeURI(this XElement x, string attName)
        {
            string? strValue = x.Attribute(attName)?.Value;

            if (!string.IsNullOrEmpty(strValue))
            {
                try
                {
                    Uri uri = new Uri(strValue);
                    return uri;
                }
                catch (Exception ex)
                {
                    Exception exception = new Exception("Failed to create URI from string = " + strValue, ex);
                    OTLogger.Error(exception);
                    throw exception;
                }
            }

            return null;
        }

        public static bool? AttributeBoolean(this XElement x, string attName)
        {
            string? strValue = x.Attribute(attName)?.Value;

            if (!string.IsNullOrEmpty(strValue))
            {
                try
                {
                    bool b = bool.Parse(strValue);
                    return b;
                }
                catch (Exception ex)
                {
                    Exception exception = new Exception("Failed to create Boolean from string = " + strValue, ex);
                    OTLogger.Error(exception);
                    throw exception;
                }
            }

            return null;
        }

        public static double? AttributeDouble(this XElement x, string attName)
        {
            string? strValue = x.Attribute(attName)?.Value;

            if (!string.IsNullOrEmpty(strValue))
            {
                try
                {
                    double d = double.Parse(strValue);
                    return d;
                }
                catch (Exception ex)
                {
                    Exception exception = new Exception("Failed to create Double from string = " + strValue, ex);
                    OTLogger.Error(exception);
                    throw exception;
                }
            }

            return null;
        }

        public static UOM? AttributeUOM(this XElement x, string attName)
        {
            string? strValue = x.Attribute(attName)?.Value;

            if (!string.IsNullOrEmpty(strValue))
            {
                try
                {
                    UOM uom = UOM.ParseFromName(strValue);
                    return uom;
                }
                catch (Exception ex)
                {
                    Exception exception = new Exception("Failed to create UOM from string = " + strValue, ex);
                    OTLogger.Error(exception);
                    throw exception;
                }
            }

            return null;
        }

        public static void AddStringElement(this XElement x, XName xname, string? value)
        {
            if (!string.IsNullOrWhiteSpace(value))
            {
                x.Add(new XElement(xname, value));
            }
        }

        public static void AddDateTimeISOElement(this XElement x, XName xname, DateTime? value)
        {
            if (value != null)
            {
                x.Add(new XElement(xname, value.Value.ToString("yyyy-MM-ddTHH:mm:ss.ffffffZ")));
            }
        }

        public static void AddDateTimeISOElement(this XElement x, XName xname, DateTimeOffset? value)
        {
            if (value != null)
            {
                x.Add(new XElement(xname, value.Value.ToString("yyyy-MM-ddTHH:mm:ss.ffffffZ")));
            }
        }
    }
}
