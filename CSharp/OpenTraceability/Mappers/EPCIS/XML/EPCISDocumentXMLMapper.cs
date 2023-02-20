using OpenTraceability;
using OpenTraceability.Interfaces;
using OpenTraceability.Mappers;
using OpenTraceability.Mappers.EPCIS.XML;
using OpenTraceability.Models.Events;
using OpenTraceability.Utility;
using System.Globalization;
using System.Reflection.Metadata;
using System.Xml.Linq;

namespace GS1.Mappers.EPCIS
{
    public class EPCISDocumentXMLMapper : IEPCISDocumentMapper
    {
        public EPCISDocument Map(string strValue)
        {
            try
            {
                EPCISDocument doc = EPCISDocumentBaseXMLMapper.ReadXml<EPCISDocument>(strValue, out XDocument xDoc);

                if (doc.EPCISVersion == null)
                {
                    throw new Exception("doc.EPCISVersion is NULL. This must be set to a version.");
                }

                EPCISDocumentBaseXMLMapper.ValidateEPCISDocumentSchema(xDoc, doc.EPCISVersion.Value);

                // read the events
                XElement? xEventList = xDoc.Root?.Element("EPCISBody")?.Element("EventList");
                if (xEventList != null)
                {
                    foreach (XElement xEvent in xEventList.Elements())
                    {
                        Type eventType = EPCISDocumentBaseXMLMapper.GetEventTypeFromProfile(xEvent);
                        IEvent e = (IEvent)OpenTraceabilityXmlMapper.FromXml(xEvent, eventType, doc.EPCISVersion.Value);
                        doc.Events.Add(e);
                    }
                }

                return doc;
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

            XNamespace epcisNS = (doc.EPCISVersion == EPCISVersion.V2) ? Constants.EPCIS_2_NAMESPACE : Constants.EPCIS_1_NAMESPACE;

            XDocument xDoc = EPCISDocumentBaseXMLMapper.WriteXml(doc, epcisNS, "EPCISDocument");
            if (xDoc.Root == null)
            {
                throw new Exception("Failed to parse EPCISQueryDocument from xml string because after parsing the XDocument the Root property was null.");
            }

            // write the events
            xDoc.Root.Add(new XElement("EPCISBody", new XElement("EventList")));
            XElement xEventList = xDoc.Root?.Element("EPCISBody")?.Element("EventList") ?? throw new Exception("Failed to get EPCISBody/EventList after adding it to the XDoc.Root");
            foreach (IEvent e in doc.Events)
            {
                string xname = EPCISDocumentBaseXMLMapper.GetEventXName(e);
                XElement? xEvent = OpenTraceabilityXmlMapper.ToXml(xname, e, doc.EPCISVersion.Value);
                if (e.EventType == EventType.Transformation && doc.EPCISVersion.Value == EPCISVersion.V1)
                {
                    xEvent = new XElement("extension", xEvent);
                }
                if (xEvent != null)
                {
                    xEventList.Add(xEvent);
                }
            }

            EPCISDocumentBaseXMLMapper.ValidateEPCISDocumentSchema(xDoc, doc.EPCISVersion.Value);

            return xDoc.ToString();
        }
    }
}