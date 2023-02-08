using OpenTraceability;
using OpenTraceability.Interfaces;
using OpenTraceability.Mappers.EPCIS.XML;
using OpenTraceability.Models.Events;
using OpenTraceability.Utility;
using System.Globalization;
using System.Xml.Linq;

namespace GS1.Mappers.EPCIS
{
    public class EPCISDocumentXMLMapper : IEPCISDocumentMapper
    {
        public EPCISDocument Map(string strValue)
        {
            try
            {
                // convert into XDocument
                XDocument xDoc = XDocument.Parse(strValue);
                if (xDoc.Root == null)
                {
                    throw new Exception("Failed to parse EPCISDocument from xml string because after parsing the XDocument the Root property was null.");
                }

                // read all of the namespaces
                EPCISDocument document = new EPCISDocument();
                document.Namespaces = xDoc.GetDocumentNamespaces();

                // reverse the dictionary for looking up the namespace prefix
                Dictionary<string, string> prefixLookup = document.Namespaces.Reverse();

                // determine epcis version
                if (document.Namespaces.Values.Contains(Constants.EPCIS_2_NAMESPACE))
                {
                    document.EPCISVersion = EPCISVersion.Version_2_0;
                }
                else if (document.Namespaces.Values.Contains(Constants.EPCIS_1_NAMESPACE))
                {
                    document.EPCISVersion = EPCISVersion.Version_1_2;
                }

                if (document.EPCISVersion == null)
                {
                    throw new Exception($"Failed to determine the EPCIS version of the XML document. Must contain a namespace with either '{Constants.EPCIS_2_NAMESPACE}' or '{Constants.EPCIS_1_NAMESPACE}'");
                }

                // TODO: validate the schema depending on the version in the document

                // read the creation date
                string? creationDateAttributeStr = xDoc.Root.Attribute("creationDate")?.Value;
                if (!string.IsNullOrWhiteSpace(creationDateAttributeStr))
                {
                    document.CreationDate = DateTime.Parse(creationDateAttributeStr);
                }

                // read the standard business document header
                XElement? xHeader = xDoc.Root.Element(Constants.SBDH_XNAMESPACE + "StandardBusinessDocumentHeader");
                if (xHeader != null)
                {
                    document.Header = EPCISXmlHeaderReader.ReadHeader(xHeader);
                }

                // TODO: read the master data

                // read the events
                XElement? xEventList = xDoc.Root?.Element("EPCISBody")?.Element("EventList");
                if (xEventList != null)
                {
                    foreach (XElement xEvent in xEventList.Elements())
                    {
                        IEvent e = EPCISXmlEventReader.ReadEvent(xEvent, document.EPCISVersion.Value);
                        document.Events.Add(e);
                    }
                }

                return document;
            }
            catch (Exception Ex)
            {
                System.Exception exception = new Exception("Failed to parse the EPCIS document from the XML. xml=" + strValue, Ex);
                OTLogger.Error(exception);
                throw;
            }
        }

        public string Map(EPCISDocument doc)
        {
            if (doc.EPCISVersion == null)
            {
                throw new Exception("doc.EPCISVersion is NULL. This must be set to a version.");
            }

            // build namespaces 
            Dictionary<string, string> namespaces = doc.GetNamespaces();

            XNamespace epcisNS = (doc.EPCISVersion == EPCISVersion.Version_2_0) ? Constants.EPCIS_2_NAMESPACE : Constants.EPCIS_1_NAMESPACE;

            // create a new xdocument with all of the namespaces...
            XDocument xDoc = new XDocument(new XElement(epcisNS + "EPCISDocument", namespaces.Select(ns => new XAttribute(XNamespace.Xmlns + ns.Key, ns.Value))));
            if (xDoc.Root == null)
            {
                throw new Exception("Failed to convert EPCIS Document into XML because the XDoc.Root is NULL. This should not happen.");
            }

            // set the creation date
            if (doc.CreationDate != null)
            {
                xDoc.Root.Add(new XAttribute("creationDate", doc.CreationDate.Value.ToString("yyyy-MM-ddTHH:mm:ss.ffffffZ")));
            }

            // write the standard business document header
            if (doc.Header != null)
            {
                XElement xHeader = EPCISXmlHeaderWriter.WriteHeader(doc.Header);
                xDoc.Root.Add(xHeader);
            }

            // TODO: write the master data

            // write the events
            xDoc.Root.Add(new XElement("EPCISBody", new XElement("EventList")));
            XElement xEventList = xDoc.Root?.Element("EPCISBody")?.Element("EventList") ?? throw new Exception("Failed to get EPCISBody/EventList after adding it to the XDoc.Root");
            foreach (IEvent e in doc.Events)
            {
                XElement xEvent = EPCISXmlEventWriter.WriteEvent(e, doc.EPCISVersion.Value);
                xEventList.Add(xEvent);
            }

            // TODO: validate the schema depending on the version in the document

            return xDoc.ToString();
        }
    }
}