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
                XNamespace? epcisNS = null;
                if (document.Namespaces.Values.Contains(Constants.EPCIS_2_NAMESPACE))
                {
                    document.EPCISVersion = EPCISVersion.Version_2_0;
                    epcisNS = Constants.EPCIS_2_NAMESPACE;
                }
                else if (document.Namespaces.Values.Contains(Constants.EPCIS_1_NAMESPACE))
                {
                    document.EPCISVersion = EPCISVersion.Version_1_2;
                    epcisNS = Constants.EPCIS_1_NAMESPACE;
                }

                if (document.EPCISVersion == null || epcisNS == null)
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
                if (prefixLookup.ContainsKey(Constants.SBDH_NAMESPACE))
                {
                    string headerNS = prefixLookup[Constants.SBDH_NAMESPACE];
                    XElement? xHeader = xDoc.Root.Element($"{headerNS}:StandardBusinessDocumentHeader");
                    if (xHeader != null)
                    {
                        document.Header = EPCISXmlHeaderReader.ReadHeader(xHeader, headerNS);
                    }
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
            throw new NotImplementedException();
        }
    }
}