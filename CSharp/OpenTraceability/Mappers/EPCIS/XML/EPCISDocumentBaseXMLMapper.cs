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
                    document.Attributes.Add(xatt.Name.ToString(), xatt.Value);
                }
            }

            // determine epcis version
            if (document.Attributes.Values.ToList().Contains(Constants.EPCIS_2_NAMESPACE))
            {
                document.EPCISVersion = EPCISVersion.V2;
            }
            else if (document.Attributes.Values.ToList().Contains(Constants.EPCIS_1_NAMESPACE))
            {
                document.EPCISVersion = EPCISVersion.V1;
            }

            if (document.EPCISVersion == null)
            {
                throw new Exception($"Failed to determine the EPCIS version of the XML document. Must contain a namespace with either '{Constants.EPCIS_2_NAMESPACE}' or '{Constants.EPCIS_1_NAMESPACE}'");
            }

            // validate the schema depending on the version in the document
            ValidateSchema(xDoc, document.EPCISVersion.Value);

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

            ValidateSchema(xDoc, doc.EPCISVersion.Value);

            // set the creation date
            if (doc.CreationDate != null)
            {
                xDoc.Root.Add(new XAttribute("creationDate", doc.CreationDate.Value.ToString("o")));
            }

            if (doc.EPCISVersion == EPCISVersion.V2)
            {
                xDoc.Root.Add(new XAttribute("schemaVersion", "2.0"));
            }
            else if (doc.EPCISVersion == EPCISVersion.V1)
            {
                xDoc.Root.Add(new XAttribute("schemaVersion", "1.2"));
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

            OpenTraceabilityEventProfile? profile = OpenTraceability.Profiles.Where(p => p.EventType == eventType && (p.Action == null || p.Action == action) && (p.BusinessStep == null || p.BusinessStep == bizStep)).OrderByDescending(p => p.SpecificityScore).FirstOrDefault();
            if (profile == null)
            {
                throw new Exception("Failed to create event from profile. Type=" + eventType + " and BizStep=" + bizStep + " and Action=" + action);
            }
            else
            {
                return profile.EventClassType;
            }
        }

        internal static string GetEventXName(IEvent e)
        {
            if (e.EventType == EventType.Object)
            {
                return "ObjectEvent";
            }
            else if (e is TransformationEvent)
            {
                return "TransformationEvent";
            }
            else if (e is TransactionEvent)
            {
                return "TransactionEvent";
            }
            else if (e is AggregationEvent)
            {
                return "AggregationEvent";
            }
            else if (e is AssociationEvent)
            {
                return "AssociationEvent";
            }
            else
            {
                throw new Exception("Failed to determine the event xname. Event C# type is " + e.GetType().FullName);
            }
        }

        internal static void ValidateSchema(XDocument xdoc, EPCISVersion version)
        {
            if (version == EPCISVersion.V1)
            {
                // validate the schema depending on the version in the document
                if (!EPCISDocumentBaseXMLMapper._schemaChecker.Validate(xdoc, "https://raw.githubusercontent.com/ift-gftc/doc.gdst/master/schemas/xml/epcis_1_2/EPCglobal-epcis-1_2.xsd", out string? error))
                {
                    throw new Exception($"Failed to validate the XML schema for the EPCIS XML.\n" + error);
                }
            }
            else
            {
                throw new NotImplementedException("Have not added schema checking for EPCIS 2.0 yet.");
            }
        }
    }
}
