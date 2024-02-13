using Newtonsoft.Json.Linq;
using OpenTraceability.Interfaces;
using OpenTraceability.Models.Common;
using OpenTraceability.Models.Events;
using OpenTraceability.Models.Events.KDEs;
using OpenTraceability.Utility;
using System;
using System.Collections.Generic;
using System.Linq;
using System.Reflection;
using System.Runtime.CompilerServices;
using System.Text;
using System.Threading.Tasks;
using System.Xml.Linq;
using System.Xml.XPath;

namespace OpenTraceability.Mappers.EPCIS.XML
{
    public class EPCISDocumentBaseXMLMapper
    {
        internal static XmlSchemaChecker _schemaChecker = new XmlSchemaChecker();

        public static T ReadXml<T>(string strValue, out XDocument xDoc) where T : EPCISBaseDocument, new()
        {
            // convert into XDocument
            xDoc = XDocument.Parse(strValue);
            if (xDoc.Root == null)
            {
                throw new Exception("Failed to parse EPCISBaseDocument from xml string because after parsing the XDocument the Root property was null.");
            }

            // read all of the attributes
            T document = Activator.CreateInstance<T>();
            foreach (XAttribute xatt in xDoc.Root.Attributes())
            {
                if (xatt.Name == "creationDate" || xatt.Name == "schemaVersion")
                {
                    continue;
                }
                else
                {
                    if (xatt.Name.Namespace == Constants.XMLNS_NAMEPSACE)
                    {
                        document.Namespaces.Add(xatt.Name.LocalName, xatt.Value);
                    }
                    else
                    {
                        document.Attributes.Add(xatt.Name.ToString(), xatt.Value);
                    }
                }
            }

            // determine epcis version
            if (document.Namespaces.Values.ToList().Contains(Constants.EPCIS_2_NAMESPACE) || document.Namespaces.Values.ToList().Contains(Constants.EPCISQUERY_2_NAMESPACE))
            {
                document.EPCISVersion = EPCISVersion.V2;
            }
            else if (document.Namespaces.Values.ToList().Contains(Constants.EPCIS_1_NAMESPACE) || document.Namespaces.Values.ToList().Contains(Constants.EPCISQUERY_1_NAMESPACE))
            {
                document.EPCISVersion = EPCISVersion.V1;
            }

            if (document.EPCISVersion == null)
            {
                throw new Exception($"Failed to determine the EPCIS version of the XML document. Must contain a namespace with either '{Constants.EPCIS_2_NAMESPACE}' or '{Constants.EPCIS_1_NAMESPACE}' or '{Constants.EPCISQUERY_2_NAMESPACE}' or '{Constants.EPCISQUERY_1_NAMESPACE}'");
            }

            // read the creation date
            string? creationDateAttributeStr = xDoc.Root.Attribute("creationDate")?.Value;
            if (!string.IsNullOrWhiteSpace(creationDateAttributeStr))
            {
                document.CreationDate = creationDateAttributeStr.TryConvertToDateTimeOffset();
            }

            // read the standard business document header
            XElement? xHeader = xDoc.Root.Element("EPCISHeader")?.Element(Constants.SBDH_XNAMESPACE + "StandardBusinessDocumentHeader");
            if (xHeader != null)
            {
                document.Header = OpenTraceabilityXmlMapper.FromXml<StandardBusinessDocumentHeader>(xHeader, document.EPCISVersion.Value);
            }

            // read the master data
            XElement? xMasterData = xDoc.Root.Element("EPCISHeader")?.Element("extension")?.Element("EPCISMasterData");
            if (xMasterData != null)
            {
                EPCISXmlMasterDataReader.ReadMasterData(document, xMasterData);
            }

            return document;
        }

        public static XDocument WriteXml(EPCISBaseDocument doc, XNamespace epcisNS, string rootEleName)
        {
            if (doc.EPCISVersion == null)
            {
                throw new Exception("doc.EPCISVersion is NULL. This must be set to a version.");
            }

            // create a new xdocument with all of the namespaces...
            XDocument xDoc = new XDocument(new XElement(epcisNS + rootEleName, doc.Attributes.Select(a => new XAttribute(a.Key, a.Value))));
            if (xDoc.Root == null)
            {
                throw new Exception("Failed to convert EPCIS Document into XML because the XDoc.Root is NULL. This should not happen.");
            }

            foreach (var ns in doc.Namespaces)
            {
                if (ns.Value == Constants.CBVMDA_NAMESPACE
                 || ns.Value == Constants.EPCISQUERY_1_NAMESPACE
                 || ns.Value == Constants.EPCISQUERY_2_NAMESPACE
                 || ns.Value == Constants.EPCIS_1_NAMESPACE
                 || ns.Value == Constants.EPCIS_2_NAMESPACE)
                {
                    continue;
                }
                else
                {
                    xDoc.Root.TryAddAttribute(Constants.XMLNS_XNAMESPACE, ns.Key, ns.Value);
                }
            }

            // set the creation date
            if (doc.CreationDate != null)
            {
                xDoc.Root.Add(new XAttribute("creationDate", doc.CreationDate.Value.ToString("o")));
            }

            xDoc.Root.SetAttributeValue(Constants.XMLNS_XNAMESPACE + "epcis", null);
            xDoc.Root.SetAttributeValue(Constants.XMLNS_XNAMESPACE + "cbvmda", null);

            if (doc.EPCISVersion == EPCISVersion.V2)
            {
                xDoc.Root.Add(new XAttribute("schemaVersion", "2.0"));
                if (doc is EPCISQueryDocument)
                {
                    xDoc.Root.Add(new XAttribute(Constants.XMLNS_XNAMESPACE + "epcisq", Constants.EPCISQUERY_2_NAMESPACE));
                    xDoc.Root.Add(new XAttribute(Constants.XMLNS_XNAMESPACE + "cbvmda", Constants.CBVMDA_NAMESPACE));
                }
                else
                {
                    xDoc.Root.Add(new XAttribute(Constants.XMLNS_XNAMESPACE + "epcis", Constants.EPCIS_2_NAMESPACE));
                    xDoc.Root.Add(new XAttribute(Constants.XMLNS_XNAMESPACE + "cbvmda", Constants.CBVMDA_NAMESPACE));
                }
            }
            else if (doc.EPCISVersion == EPCISVersion.V1)
            {
                xDoc.Root.Add(new XAttribute("schemaVersion", "1.2"));
                if (doc is EPCISQueryDocument)
                {
                    xDoc.Root.Add(new XAttribute(Constants.XMLNS_XNAMESPACE + "epcisq", Constants.EPCISQUERY_1_NAMESPACE));
                    xDoc.Root.Add(new XAttribute(Constants.XMLNS_XNAMESPACE + "cbvmda", Constants.CBVMDA_NAMESPACE));
                }
                else
                {
                    xDoc.Root.Add(new XAttribute(Constants.XMLNS_XNAMESPACE + "epcis", Constants.EPCIS_1_NAMESPACE));
                    xDoc.Root.Add(new XAttribute(Constants.XMLNS_XNAMESPACE + "cbvmda", Constants.CBVMDA_NAMESPACE));
                }
            }

            // write the standard business document header
            if (doc.Header != null)
            {
                string xname = ((Constants.SBDH_XNAMESPACE) + "StandardBusinessDocumentHeader").ToString();
                XElement? xHeader = OpenTraceabilityXmlMapper.ToXml(xname, doc.Header, doc.EPCISVersion.Value);
                if (xHeader != null)
                {
                    xDoc.Root.Add(new XElement("EPCISHeader", xHeader));
                }
            }

            // write the master data
            EPCISXmlMasterDataWriter.WriteMasterData(xDoc.Root, doc);

            return xDoc;
        }

        internal static Type GetEventTypeFromProfile(XElement xEvent)
        {
            Enum.TryParse<EventAction>(xEvent.Element("action")?.Value, out var action);
            string? bizStep = xEvent.Element("bizStep")?.Value;
            string eventType = xEvent.Name.LocalName;

            if (eventType == "extension")
            {
                eventType = xEvent.Elements().First().Name.LocalName;
            }

            var profiles = Setup.Profiles.Where(p => p.EventType.ToString() == eventType && (p.Action == null || p.Action == action) && (p.BusinessStep == null || p.BusinessStep.ToLower() == bizStep?.ToLower())).OrderByDescending(p => p.SpecificityScore).ToList();
            if (profiles.Count() == 0)
            {
                throw new Exception("Failed to create event from profile. Type=" + eventType + " and BizStep=" + bizStep + " and Action=" + action);
            }
            else
            {
                foreach (var profile in profiles.Where(p => p.KDEProfiles != null).ToList())
                {
                    foreach (var kdeProfile in profile.KDEProfiles)
                    {
                        if (xEvent.QueryXPath(kdeProfile.XPath_V1) == null)
                        {
                            profiles.Remove(profile);
                        }
                    }
                }

                if (profiles.Count() == 0)
                {
                    throw new Exception("Failed to create event from profile. Type=" + eventType + " and BizStep=" + bizStep + " and Action=" + action);
                }

                return profiles.First().EventClassType;
            }
        }

        internal static string GetEventXName(IEvent e)
        {
            if (e.EventType == EventType.ObjectEvent)
            {
                return "ObjectEvent";
            }
            else if (e.EventType == EventType.TransformationEvent)
            {
                return "TransformationEvent";
            }
            else if (e.EventType == EventType.TransactionEvent)
            {
                return "TransactionEvent";
            }
            else if (e.EventType == EventType.AggregationEvent)
            {
                return "AggregationEvent";
            }
            else if (e.EventType == EventType.AssociationEvent)
            {
                return "AssociationEvent";
            }
            else
            {
                throw new Exception("Failed to determine the event xname. Event C# type is " + e.GetType().FullName);
            }
        }

        public static void ValidateEPCISDocumentSchema(XDocument xdoc, EPCISVersion version)
        {
            if (version == EPCISVersion.V1)
            {
                // validate the schema depending on the version in the document
                if (!EPCISDocumentBaseXMLMapper._schemaChecker.Validate(xdoc, "https://raw.githubusercontent.com/ift-gftc/doc.gdst/master/schemas/xml/epcis_1_2/EPCglobal-epcis-1_2.xsd", out string? error))
                {
                    throw new OpenTraceabilitySchemaException($"Failed to validate the XML schema for the EPCIS XML.\n" + error);
                }
            }
            else
            {
                // https://ref.gs1.org/standards/epcis/epcglobal-epcis-2_0.xsd
                if (!EPCISDocumentBaseXMLMapper._schemaChecker.Validate(xdoc, "https://ref.gs1.org/standards/epcis/epcglobal-epcis-2_0.xsd", out string? error))
                {
                    throw new OpenTraceabilitySchemaException($"Failed to validate the XML schema for the EPCIS XML.\n" + error);
                }
            }
        }

        public static void ValidateEPCISQueryDocumentSchema(XDocument xdoc, EPCISVersion version)
        {
            if (version == EPCISVersion.V1)
            {
                // validate the schema depending on the version in the document
                if (!EPCISDocumentBaseXMLMapper._schemaChecker.Validate(xdoc, "https://raw.githubusercontent.com/ift-gftc/doc.gdst/master/schemas/xml/epcis_1_2/EPCglobal-epcis-query-1_2.xsd", out string? error))
                {
                    throw new Exception($"Failed to validate the XML schema for the EPCIS XML.\n" + error);
                }
            }
            else
            {
                // https://ref.gs1.org/standards/epcis/epcglobal-epcis-query-2_0.xsd
                if (!EPCISDocumentBaseXMLMapper._schemaChecker.Validate(xdoc, "https://ref.gs1.org/standards/epcis/epcglobal-epcis-query-2_0.xsd", out string? error))
                {
                    throw new Exception($"Failed to validate the XML schema for the EPCIS XML.\n" + error);
                }
            }
        }
    }
}