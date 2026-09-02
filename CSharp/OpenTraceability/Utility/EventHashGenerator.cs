using OpenTraceability.Interfaces;
using OpenTraceability.Mappers;
using OpenTraceability.Mappers.EPCIS.XML;
using OpenTraceability.Models.Events;
using System;
using System.Collections.Generic;
using System.Globalization;
using System.Linq;
using System.Security.Cryptography;
using System.Text;
using System.Xml.Linq;

namespace OpenTraceability.Utility
{
    /// <summary>
    /// Generates the canonical EPCIS Event Hash ID for an EPCIS event as specified in the
    /// GS1 Core Business Vocabulary (CBV) 2.0 standard.
    ///
    /// The algorithm derives a deterministic, data-binding-independent "pre-hash" string by
    /// extracting the event's intrinsic values in a fixed canonical property order, normalizing
    /// them (UTC timestamps with millisecond precision, trailing-zero stripping, CBV URN to GS1
    /// web-vocabulary conversion, EPC URN to canonical GS1 Digital Link conversion, lexical
    /// sorting of list members and user extensions), and then hashing the UTF-8 bytes with
    /// SHA-256. The result is returned in the RFC 6920 "ni" URI form:
    ///     ni:///sha-256;{lowercase-hex-digest}?ver=CBV2.0
    ///
    /// This is a port of the reference implementations:
    ///   - https://github.com/RalphTro/epcis-event-hash-generator (Python)
    ///   - https://github.com/openepcis/openepcis-event-hash-generator (Java)
    /// </summary>
    public static class EventHashGenerator
    {
        private const string NiPrefix = "ni:///sha-256;";
        private const string VerSuffix = "?ver=CBV2.0";
        private const string DLDomain = "https://id.gs1.org";

        /// <summary>
        /// Generates the EPCIS Event Hash ID for the given event in the form
        /// "ni:///sha-256;{hex}?ver=CBV2.0".
        /// </summary>
        public static string GenerateHash(IEvent e)
        {
            if (e == null) throw new ArgumentNullException(nameof(e));

            try
            {
                string preHash = GeneratePreHashString(e);
                using (SHA256 sha = SHA256.Create())
                {
                    byte[] hash = sha.ComputeHash(Encoding.UTF8.GetBytes(preHash));
                    StringBuilder hex = new StringBuilder(hash.Length * 2);
                    foreach (byte b in hash)
                    {
                        hex.Append(b.ToString("x2", CultureInfo.InvariantCulture));
                    }
                    return NiPrefix + hex + VerSuffix;
                }
            }
            catch (Exception ex)
            {
                OTLogger.Error(ex);
                throw;
            }
        }

        /// <summary>
        /// Serializes the event into its canonical EPCIS 2.0 XML representation and builds the
        /// pre-hash string by walking the element tree. Exposed internally for white-box testing.
        /// </summary>
        internal static string GeneratePreHashString(IEvent e)
        {
            string xname = EPCISDocumentBaseXMLMapper.GetEventXName(e);
            XElement xEvent = OpenTraceabilityXmlMapper.ToXml(xname, e, EPCISVersion.V2)
                ?? throw new Exception("Failed to serialize event to XML for hashing.");
            return GeneratePreHashString(xEvent);
        }

        /// <summary>
        /// Builds the canonical pre-hash string from an EPCIS event XElement.
        /// </summary>
        internal static string GeneratePreHashString(XElement xEvent)
        {
            StringBuilder sb = new StringBuilder();

            // 1. eventType
            sb.Append("eventType=").Append(xEvent.Name.LocalName);

            // 2. ordered scalar / structured fields (the "PROP_ORDER" main pass)
            AppendLeaf(sb, xEvent, "eventTime", isTime: true);
            AppendLeaf(sb, xEvent, "eventTimeZoneOffset", isTime: false);
            AppendLeaf(sb, xEvent, "certificationInfo", isTime: false);
            AppendLeaf(sb, xEvent, "parentID", isTime: false);
            AppendEpcList(sb, xEvent, "epcList");
            AppendEpcList(sb, xEvent, "inputEPCList");
            AppendEpcList(sb, xEvent, "childEPCs");
            AppendQuantityList(sb, xEvent, "quantityList");
            AppendQuantityList(sb, xEvent, "childQuantityList");
            AppendQuantityList(sb, xEvent, "inputQuantityList");
            AppendEpcList(sb, xEvent, "outputEPCList");
            AppendQuantityList(sb, xEvent, "outputQuantityList");
            AppendLeaf(sb, xEvent, "action", isTime: false);
            AppendLeaf(sb, xEvent, "transformationID", isTime: false);
            AppendLeaf(sb, xEvent, "bizStep", isTime: false);
            AppendLeaf(sb, xEvent, "disposition", isTime: false);
            AppendPersistentDisposition(sb, xEvent);
            AppendLocation(sb, xEvent, "readPoint");
            AppendLocation(sb, xEvent, "bizLocation");
            AppendSensorElementList(sb, xEvent);

            // 3. the "remaining" pass: bizTransactionList, sourceList, destinationList, ilmd, and
            //    top-level user extensions. These are sorted lexically against one another.
            List<string> remaining = new List<string>();

            string bizTxn = FormatTypedValueList(xEvent, "bizTransactionList", "bizTransaction");
            if (!string.IsNullOrEmpty(bizTxn)) remaining.Add(bizTxn);

            string sources = FormatTypedValueList(xEvent, "sourceList", "source");
            if (!string.IsNullOrEmpty(sources)) remaining.Add(sources);

            string destinations = FormatTypedValueList(xEvent, "destinationList", "destination");
            if (!string.IsNullOrEmpty(destinations)) remaining.Add(destinations);

            string ilmd = FormatIlmd(xEvent);
            if (!string.IsNullOrEmpty(ilmd)) remaining.Add(ilmd);

            foreach (XElement ext in UserExtensions(xEvent))
            {
                string s = FormatExtensionElement(ext);
                if (!string.IsNullOrEmpty(s)) remaining.Add(s);
            }

            remaining.Sort(StringComparer.Ordinal);
            foreach (string s in remaining)
            {
                sb.Append(s);
            }

            return sb.ToString();
        }

        #region Field appenders

        private static void AppendLeaf(StringBuilder sb, XElement parent, string name, bool isTime)
        {
            XElement? x = StdElement(parent, name);
            if (x == null) return;
            string raw = (x.Value ?? string.Empty).Trim();
            if (raw.Length == 0) return;
            string value = isTime ? FixTimestamp(raw) : CanonizeValue(raw);
            if (string.IsNullOrEmpty(value)) return;
            sb.Append(name).Append('=').Append(value);
        }

        private static void AppendEpcList(StringBuilder sb, XElement parent, string listName)
        {
            XElement? list = StdElement(parent, listName);
            if (list == null) return;

            List<string> values = new List<string>();
            foreach (XElement epc in StdElements(list, "epc"))
            {
                string v = (epc.Value ?? string.Empty).Trim();
                if (v.Length == 0) continue;
                values.Add("epc=" + CanonizeValue(v));
            }
            if (values.Count == 0) return;

            values.Sort(StringComparer.Ordinal);
            sb.Append(listName);
            foreach (string v in values) sb.Append(v);
        }

        private static void AppendQuantityList(StringBuilder sb, XElement parent, string listName)
        {
            XElement? list = StdElement(parent, listName);
            if (list == null) return;

            List<string> items = new List<string>();
            foreach (XElement qe in StdElements(list, "quantityElement"))
            {
                StringBuilder local = new StringBuilder("quantityElement");
                AppendLeaf(local, qe, "epcClass", isTime: false);
                AppendLeaf(local, qe, "quantity", isTime: false);
                AppendLeaf(local, qe, "uom", isTime: false);
                if (local.Length > "quantityElement".Length)
                {
                    items.Add(local.ToString());
                }
            }
            if (items.Count == 0) return;

            items.Sort(StringComparer.Ordinal);
            sb.Append(listName);
            foreach (string item in items) sb.Append(item);
        }

        private static void AppendPersistentDisposition(StringBuilder sb, XElement parent)
        {
            XElement? pd = StdElement(parent, "persistentDisposition");
            if (pd == null) return;

            List<string> setValues = StdElements(pd, "set")
                .Select(x => (x.Value ?? string.Empty).Trim())
                .Where(v => v.Length > 0)
                .Select(v => "set=" + CanonizeValue(v))
                .OrderBy(v => v, StringComparer.Ordinal)
                .ToList();

            List<string> unsetValues = StdElements(pd, "unset")
                .Select(x => (x.Value ?? string.Empty).Trim())
                .Where(v => v.Length > 0)
                .Select(v => "unset=" + CanonizeValue(v))
                .OrderBy(v => v, StringComparer.Ordinal)
                .ToList();

            if (setValues.Count == 0 && unsetValues.Count == 0) return;

            sb.Append("persistentDisposition");
            foreach (string v in setValues) sb.Append(v);
            foreach (string v in unsetValues) sb.Append(v);
        }

        private static void AppendLocation(StringBuilder sb, XElement parent, string name)
        {
            XElement? loc = StdElement(parent, name);
            if (loc == null) return;

            StringBuilder local = new StringBuilder();
            XElement? id = StdElement(loc, "id");
            if (id != null)
            {
                string v = (id.Value ?? string.Empty).Trim();
                if (v.Length > 0) local.Append("id=").Append(CanonizeValue(v));
            }

            string ext = FormatExtensionChildren(loc);
            if (local.Length == 0 && ext.Length == 0) return;

            sb.Append(name).Append(local).Append(ext);
        }

        // Canonical order of the sensorMetadata attributes.
        private static readonly string[] SensorMetadataOrder =
        {
            "time", "startTime", "endTime", "deviceID", "deviceMetadata", "rawData",
            "dataProcessingMethod", "bizRules"
        };

        // Canonical order of the sensorReport attributes.
        private static readonly string[] SensorReportOrder =
        {
            "type", "exception", "deviceID", "deviceMetadata", "rawData", "dataProcessingMethod",
            "time", "microorganism", "chemicalSubstance", "value", "component", "stringValue",
            "booleanValue", "hexBinaryValue", "uriValue", "minValue", "maxValue", "meanValue",
            "sDev", "percRank", "percValue", "uom", "coordinateReferenceSystem"
        };

        private static void AppendSensorElementList(StringBuilder sb, XElement parent)
        {
            XElement? list = StdElement(parent, "sensorElementList");
            if (list == null) return;

            List<string> sensorElements = new List<string>();
            foreach (XElement se in StdElements(list, "sensorElement"))
            {
                StringBuilder local = new StringBuilder("sensorElement");

                XElement? meta = StdElement(se, "sensorMetadata");
                if (meta != null)
                {
                    StringBuilder ml = new StringBuilder("sensorMetadata");
                    AppendAttributesInOrder(ml, meta, SensorMetadataOrder);
                    ml.Append(FormatExtensionAttributes(meta));
                    if (ml.Length > "sensorMetadata".Length) local.Append(ml);
                }

                List<string> reports = new List<string>();
                foreach (XElement report in StdElements(se, "sensorReport"))
                {
                    StringBuilder rl = new StringBuilder("sensorReport");
                    AppendAttributesInOrder(rl, report, SensorReportOrder);
                    rl.Append(FormatExtensionAttributes(report));
                    rl.Append(FormatExtensionChildren(report));
                    if (rl.Length > "sensorReport".Length) reports.Add(rl.ToString());
                }
                reports.Sort(StringComparer.Ordinal);
                foreach (string r in reports) local.Append(r);

                // user extensions directly on the sensorElement
                local.Append(FormatExtensionChildren(se));

                if (local.Length > "sensorElement".Length)
                {
                    sensorElements.Add(local.ToString());
                }
            }
            if (sensorElements.Count == 0) return;

            sensorElements.Sort(StringComparer.Ordinal);
            sb.Append("sensorElementList");
            foreach (string s in sensorElements) sb.Append(s);
        }

        private static void AppendAttributesInOrder(StringBuilder sb, XElement element, string[] order)
        {
            foreach (string name in order)
            {
                XAttribute attr = element.Attribute(name);
                if (attr == null) continue;
                string raw = (attr.Value ?? string.Empty).Trim();
                if (raw.Length == 0) continue;
                bool isTime = IsTimeField(name);
                string value = isTime ? FixTimestamp(raw) : CanonizeValue(raw);
                if (string.IsNullOrEmpty(value)) continue;
                sb.Append(name).Append('=').Append(value);
            }
        }

        /// <summary>
        /// Formats a list of typed values (bizTransactionList/bizTransaction, sourceList/source,
        /// destinationList/destination). Each member is rendered as "type={cbv}{itemName}={value}"
        /// and the members are sorted lexically against one another.
        /// </summary>
        private static string FormatTypedValueList(XElement parent, string listName, string itemName)
        {
            XElement? list = StdElement(parent, listName);
            if (list == null) return string.Empty;

            List<string> items = new List<string>();
            foreach (XElement item in StdElements(list, itemName))
            {
                StringBuilder local = new StringBuilder();
                XAttribute? type = item.Attribute("type");
                if (type != null)
                {
                    string t = (type.Value ?? string.Empty).Trim();
                    if (t.Length > 0) local.Append("type=").Append(CanonizeValue(t));
                }
                string v = (item.Value ?? string.Empty).Trim();
                if (v.Length > 0) local.Append(itemName).Append('=').Append(CanonizeValue(v));
                if (local.Length > 0) items.Add(local.ToString());
            }
            if (items.Count == 0) return string.Empty;

            items.Sort(StringComparer.Ordinal);
            return listName + string.Concat(items);
        }

        private static string FormatIlmd(XElement parent)
        {
            XElement? ilmd = StdElement(parent, "ilmd");
            if (ilmd == null) return string.Empty;
            string children = FormatExtensionChildren(ilmd);
            if (children.Length == 0) return string.Empty;
            return "ilmd" + children;
        }

        #endregion

        #region User extension handling

        /// <summary>
        /// Renders the namespaced child elements of <paramref name="parent"/> (user extensions),
        /// sorted lexically, each as "{namespace}localName=value" (recursing for nested objects).
        /// </summary>
        private static string FormatExtensionChildren(XElement parent)
        {
            List<string> parts = new List<string>();
            foreach (XElement child in parent.Elements())
            {
                if (child.Name.Namespace == XNamespace.None) continue;
                string s = FormatExtensionElement(child);
                if (!string.IsNullOrEmpty(s)) parts.Add(s);
            }
            if (parts.Count == 0) return string.Empty;
            parts.Sort(StringComparer.Ordinal);
            return string.Concat(parts);
        }

        /// <summary>
        /// Renders the namespaced attributes of <paramref name="element"/> (used for sensor
        /// metadata/report user extensions), sorted lexically, each as "{namespace}localName=value".
        /// </summary>
        private static string FormatExtensionAttributes(XElement element)
        {
            List<string> parts = new List<string>();
            foreach (XAttribute attr in element.Attributes())
            {
                if (attr.IsNamespaceDeclaration) continue;
                if (attr.Name.Namespace == XNamespace.None) continue;
                string v = (attr.Value ?? string.Empty).Trim();
                string key = attr.Name.ToString();
                parts.Add(v.Length == 0 ? key : key + "=" + CanonizeValue(v));
            }
            if (parts.Count == 0) return string.Empty;
            parts.Sort(StringComparer.Ordinal);
            return string.Concat(parts);
        }

        private static string FormatExtensionElement(XElement x)
        {
            string key = x.Name.ToString(); // "{namespace}localName"

            List<XElement> childElements = x.Elements().ToList();
            if (childElements.Count > 0)
            {
                List<string> parts = new List<string>();
                foreach (XElement child in childElements)
                {
                    string s = FormatExtensionElement(child);
                    if (!string.IsNullOrEmpty(s)) parts.Add(s);
                }
                parts.Sort(StringComparer.Ordinal);
                return key + string.Concat(parts);
            }

            string value = (x.Value ?? string.Empty).Trim();
            return value.Length == 0 ? key : key + "=" + CanonizeValue(value);
        }

        private static IEnumerable<XElement> UserExtensions(XElement xEvent)
        {
            foreach (XElement child in xEvent.Elements())
            {
                if (child.Name.Namespace != XNamespace.None)
                {
                    yield return child;
                }
            }
        }

        #endregion

        #region Value canonization

        private static bool IsTimeField(string name)
        {
            return name.IndexOf("time", StringComparison.OrdinalIgnoreCase) >= 0
                && name.IndexOf("offset", StringComparison.OrdinalIgnoreCase) < 0;
        }

        private static string CanonizeValue(string text)
        {
            text = ConvertCbvWebVocabulary(text);
            text = FormatNumeric(text);
            string? dl = NormalizeDigitalLink(text);
            return dl == null || string.IsNullOrEmpty(dl) ? text : dl;
        }

        private static string ConvertCbvWebVocabulary(string text)
        {
            return text
                .Replace("urn:epcglobal:cbv:bizstep:", "https://ref.gs1.org/cbv/BizStep-")
                .Replace("urn:epcglobal:cbv:disp:", "https://ref.gs1.org/cbv/Disp-")
                .Replace("urn:epcglobal:cbv:btt:", "https://ref.gs1.org/cbv/BTT-")
                .Replace("urn:epcglobal:cbv:sdt:", "https://ref.gs1.org/cbv/SDT-")
                .Replace("urn:epcglobal:cbv:er:", "https://ref.gs1.org/cbv/ER-");
        }

        private static string FormatNumeric(string text)
        {
            if (double.TryParse(text, NumberStyles.Float, CultureInfo.InvariantCulture, out double d)
                && !double.IsInfinity(d) && !double.IsNaN(d))
            {
                if (Math.Floor(d) == d && Math.Abs(d) < 9.2e18)
                {
                    return ((long)d).ToString(CultureInfo.InvariantCulture);
                }
                return d.ToString("R", CultureInfo.InvariantCulture);
            }
            return text;
        }

        private static string FixTimestamp(string text)
        {
            if (DateTimeOffset.TryParse(text, CultureInfo.InvariantCulture, DateTimeStyles.None, out DateTimeOffset dto))
            {
                return dto.ToUniversalTime().ToString("yyyy-MM-ddTHH:mm:ss.fffZ", CultureInfo.InvariantCulture);
            }
            return text;
        }

        #endregion

        #region GS1 Digital Link normalization (port of dl_normaliser.py)

        private static string PercentEncode(string s)
        {
            return s
                .Replace("!", "%21")
                .Replace("(", "%28")
                .Replace(")", "%29")
                .Replace("*", "%2A")
                .Replace("+", "%2B")
                .Replace(",", "%2C")
                .Replace(":", "%3A");
        }

        private static int CheckDigit(string keyWithoutCheckDigit)
        {
            char[] arr = keyWithoutCheckDigit.ToCharArray();
            Array.Reverse(arr);
            int sum = 0;
            for (int i = 0; i < arr.Length; i++)
            {
                int c = arr[i] - '0';
                if (c == 0) continue;
                sum += (i % 2 == 0) ? c * 3 : c * 1;
            }
            return ((sum + 9) / 10) * 10 - sum;
        }

        /// <summary>
        /// Converts an EPC/EPC-Class/EPC-Pattern URI conveying a GS1 key into its constrained,
        /// canonical GS1 Digital Link URI. Returns null when the value is not a normalizable GS1 id.
        /// </summary>
        private static string? NormalizeDigitalLink(string uri)
        {
            if (string.IsNullOrEmpty(uri)) return null;

            int partition = uri.IndexOf('.');
            if (partition < 0)
            {
                // Already-canonical GS1 Digital Link URIs have no '.' in the key portion but do in
                // the domain; handle the explicit id.gs1.org passthrough below.
                if (uri.StartsWith(DLDomain + "/", StringComparison.Ordinal))
                {
                    int q = uri.IndexOf('?');
                    return q >= 0 ? uri.Substring(0, q) : uri;
                }
                return null;
            }

            try
            {
                // ---- EPC URIs ----
                if (uri.StartsWith("urn:epc:id:sgtin:", StringComparison.Ordinal))
                {
                    string cp = uri.Substring(17, partition - 17);
                    string itemref = uri.Substring(partition + 1, 13 - cp.Length);
                    string gtin = itemref.Substring(0, 1) + cp + itemref.Substring(1);
                    string serial = uri.Substring(32);
                    return DLDomain + "/01/" + gtin + CheckDigit(gtin) + "/21/" + PercentEncode(serial);
                }
                if (uri.StartsWith("urn:epc:id:sscc:", StringComparison.Ordinal))
                {
                    string cp = uri.Substring(16, partition - 16);
                    string serialref = uri.Substring(partition + 2);
                    string sscc = uri.Substring(partition + 1, 1) + cp + serialref;
                    return DLDomain + "/00/" + sscc + CheckDigit(sscc);
                }
                if (uri.StartsWith("urn:epc:id:sgln:", StringComparison.Ordinal))
                {
                    string cp = uri.Substring(16, partition - 16);
                    string locationref = uri.Substring(partition + 1, 12 - cp.Length);
                    string gln = cp + locationref;
                    string extension = uri.Substring(30);
                    if (extension == "0")
                    {
                        return DLDomain + "/414/" + gln + CheckDigit(gln);
                    }
                    return DLDomain + "/414/" + gln + CheckDigit(gln) + "/254/" + PercentEncode(extension);
                }
                if (uri.StartsWith("urn:epc:id:grai:", StringComparison.Ordinal))
                {
                    string cp = uri.Substring(16, partition - 16);
                    string assetref = uri.Substring(partition + 1, 12 - cp.Length);
                    string grai = "0" + cp + assetref;
                    string serial = uri.Substring(30);
                    return DLDomain + "/8003/" + grai + CheckDigit(grai) + PercentEncode(serial);
                }
                if (uri.StartsWith("urn:epc:id:giai:", StringComparison.Ordinal))
                {
                    string cp = uri.Substring(16, partition - 16);
                    string assetref = uri.Substring(partition + 1);
                    return DLDomain + "/8004/" + cp + PercentEncode(assetref);
                }
                if (uri.StartsWith("urn:epc:id:gsrnp:", StringComparison.Ordinal))
                {
                    string cp = uri.Substring(17, partition - 17);
                    string serviceref = uri.Substring(partition + 1);
                    string gsrnp = cp + serviceref;
                    return DLDomain + "/8017/" + gsrnp + CheckDigit(gsrnp);
                }
                if (uri.StartsWith("urn:epc:id:gsrn:", StringComparison.Ordinal))
                {
                    string cp = uri.Substring(16, partition - 16);
                    string serviceref = uri.Substring(partition + 1);
                    string gsrn = cp + serviceref;
                    return DLDomain + "/8018/" + gsrn + CheckDigit(gsrn);
                }
                if (uri.StartsWith("urn:epc:id:gdti:", StringComparison.Ordinal))
                {
                    string cp = uri.Substring(16, partition - 16);
                    string documenttype = uri.Substring(partition + 1, 12 - cp.Length);
                    string gdti = cp + documenttype;
                    string serial = uri.Substring(30);
                    return DLDomain + "/253/" + gdti + CheckDigit(gdti) + PercentEncode(serial);
                }
                if (uri.StartsWith("urn:epc:id:cpi:", StringComparison.Ordinal))
                {
                    string cp = uri.Substring(15, partition - 15);
                    int separator = uri.LastIndexOf('.');
                    string cpref = uri.Substring(partition + 1, separator - (partition + 1));
                    string serial = uri.Substring(separator + 1);
                    return DLDomain + "/8010/" + PercentEncode(cp + cpref) + "/8011/" + serial;
                }
                if (uri.StartsWith("urn:epc:id:sgcn:", StringComparison.Ordinal))
                {
                    string cp = uri.Substring(16, partition - 16);
                    string couponref = uri.Substring(partition + 1, 12 - cp.Length);
                    string sgcn = cp + couponref;
                    string serial = uri.Substring(30);
                    return DLDomain + "/255/" + sgcn + CheckDigit(sgcn) + serial;
                }
                if (uri.StartsWith("urn:epc:id:ginc:", StringComparison.Ordinal))
                {
                    string cp = uri.Substring(16, partition - 16);
                    string consignmentref = uri.Substring(partition + 1);
                    return DLDomain + "/401/" + cp + PercentEncode(consignmentref);
                }
                if (uri.StartsWith("urn:epc:id:gsin:", StringComparison.Ordinal))
                {
                    string cp = uri.Substring(16, partition - 16);
                    string shipperref = uri.Substring(partition + 1);
                    string gsin = cp + shipperref;
                    return DLDomain + "/402/" + gsin + CheckDigit(gsin);
                }
                if (uri.StartsWith("urn:epc:id:itip:", StringComparison.Ordinal))
                {
                    string cp = uri.Substring(16, partition - 16);
                    string itemref = uri.Substring(partition + 1, 13 - cp.Length);
                    string gtin = itemref.Substring(0, 1) + cp + itemref.Substring(1);
                    string piece = uri.Substring(31, 2);
                    string total = uri.Substring(34, 2);
                    string serial = uri.Substring(37);
                    return DLDomain + "/8006/" + gtin + CheckDigit(gtin) + piece + total + "/21/" + PercentEncode(serial);
                }
                if (uri.StartsWith("urn:epc:id:upui:", StringComparison.Ordinal))
                {
                    string cp = uri.Substring(16, partition - 16);
                    string itemref = uri.Substring(partition + 1, 13 - cp.Length);
                    string gtin = itemref.Substring(0, 1) + cp + itemref.Substring(1);
                    string serial = uri.Substring(31);
                    return DLDomain + "/01/" + gtin + CheckDigit(gtin) + "/235/" + PercentEncode(serial);
                }
                if (uri.StartsWith("urn:epc:id:pgln:", StringComparison.Ordinal))
                {
                    string cp = uri.Substring(16, partition - 16);
                    string partyref = uri.Substring(partition + 1, 12 - cp.Length);
                    string gln = cp + partyref;
                    return DLDomain + "/417/" + gln + CheckDigit(gln);
                }

                // ---- EPC Class URIs ----
                if (uri.StartsWith("urn:epc:class:lgtin:", StringComparison.Ordinal))
                {
                    string cp = uri.Substring(20, partition - 20);
                    string itemref = uri.Substring(partition + 1, 13 - cp.Length);
                    string gtin = itemref.Substring(0, 1) + cp + itemref.Substring(1);
                    string lot = uri.Substring(35);
                    return DLDomain + "/01/" + gtin + CheckDigit(gtin) + "/10/" + PercentEncode(lot);
                }

                // ---- EPC ID Pattern URIs ----
                if (uri.StartsWith("urn:epc:idpat:sgtin:", StringComparison.Ordinal))
                {
                    string cp = uri.Substring(20, partition - 20);
                    string itemref = uri.Substring(partition + 1, 13 - cp.Length);
                    string gtin = itemref.Substring(0, 1) + cp + itemref.Substring(1);
                    return DLDomain + "/01/" + gtin + CheckDigit(gtin);
                }

                // ---- Already a GS1 Digital Link URI ----
                if (uri.StartsWith(DLDomain + "/", StringComparison.Ordinal))
                {
                    int q = uri.IndexOf('?');
                    return q >= 0 ? uri.Substring(0, q) : uri;
                }
            }
            catch (Exception ex)
            {
                // A malformed identifier that slipped through should not break hashing;
                // leave the value untouched.
                OTLogger.Error(ex);
                return null;
            }

            return null;
        }

        #endregion

        #region XElement helpers (standard, non-namespaced fields)

        private static XElement? StdElement(XElement parent, string localName)
        {
            return parent.Elements()
                .FirstOrDefault(x => x.Name.Namespace == XNamespace.None && x.Name.LocalName == localName);
        }

        private static IEnumerable<XElement> StdElements(XElement parent, string localName)
        {
            return parent.Elements()
                .Where(x => x.Name.Namespace == XNamespace.None && x.Name.LocalName == localName);
        }

        #endregion
    }
}
