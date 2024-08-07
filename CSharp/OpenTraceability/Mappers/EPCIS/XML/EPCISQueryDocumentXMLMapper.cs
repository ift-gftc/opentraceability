﻿using OpenTraceability;
using OpenTraceability.Interfaces;
using OpenTraceability.Mappers;
using OpenTraceability.Mappers.EPCIS.XML;
using OpenTraceability.Models.Events;
using OpenTraceability.Utility;
using System;
using System.Globalization;
using System.Linq;
using System.Threading.Tasks;


//using System.Reflection.Metadata;
using System.Xml.Linq;
//using static System.Runtime.InteropServices.JavaScript.JSType;

namespace GS1.Mappers.EPCIS
{
    public class EPCISQueryDocumentXMLMapper : IEPCISQueryDocumentMapper
    {
        public EPCISQueryDocument Map(string strValue, bool checkSchema = true)
        {
            try
            {
                // TODO: validate the schema depending on the version in the document

                EPCISQueryDocument document = EPCISDocumentBaseXMLMapper.ReadXml<EPCISQueryDocument>(strValue, out XDocument xDoc);
                if (xDoc.Root == null)
                {
                    throw new Exception("Failed to parse EPCISQueryDocument from xml string because after parsing the XDocument the Root property was null.");
                }
                if (document.EPCISVersion == null)
                {
                    throw new Exception("doc.EPCISVersion is NULL. This must be set to a version.");
                }

                if (checkSchema)
                {
                    EPCISDocumentBaseXMLMapper.ValidateEPCISQueryDocumentSchema(xDoc, document.EPCISVersion.Value);
                }

                XNamespace epcisQueryXName = (document.EPCISVersion == EPCISVersion.V1) ? Constants.EPCISQUERY_1_XNAMESPACE : Constants.EPCISQUERY_2_XNAMESPACE;

                // read the query name
                XElement xQueryName = xDoc.Root?.Element("EPCISBody")?.Element(epcisQueryXName + "QueryResults")?.Element("queryName");
                if (xQueryName != null)
                {
                    document.QueryName = xQueryName.Value;
                }

                // read the events
                XElement xEventList = xDoc.Root?.Element("EPCISBody")?.Element(epcisQueryXName + "QueryResults")?.Element("resultsBody")?.Element("EventList")
                    ?? throw new Exception("Failed to get EPCISBody/EventList after adding it to the XDoc.Root");
                if (xEventList != null)
                {
                    foreach (XElement xe in xEventList.Elements())
                    {
                        XElement xEvent = xe;
                        if (xEvent.Name == "extension")
                        {
                            xEvent = xEvent.Elements().First();
                        }

                        Type eventType = EPCISDocumentBaseXMLMapper.GetEventTypeFromProfile(xEvent);
                        IEvent e = (IEvent)OpenTraceabilityXmlMapper.FromXml(xEvent, eventType, document.EPCISVersion.Value);
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

        public string Map(EPCISQueryDocument doc, bool checkSchema = true)
        {
            if (doc.EPCISVersion == null)
            {
                throw new Exception("doc.EPCISVersion is NULL. This must be set to a version.");
            }

            XNamespace epcisNS = (doc.EPCISVersion == EPCISVersion.V2) ? Constants.EPCISQUERY_2_NAMESPACE : Constants.EPCISQUERY_1_NAMESPACE;

            XDocument xDoc = EPCISDocumentBaseXMLMapper.WriteXml(doc, epcisNS, "EPCISQueryDocument");
            if (xDoc.Root == null)
            {
                throw new Exception("Failed to parse EPCISQueryDocument from xml string because after parsing the XDocument the Root property was null.");
            }

            XNamespace epcisQueryXName = (doc.EPCISVersion == EPCISVersion.V1) ? Constants.EPCISQUERY_1_XNAMESPACE : Constants.EPCISQUERY_2_XNAMESPACE;

            // write the query name
            xDoc.Root.Add(new XElement("EPCISBody",
                              new XElement(epcisQueryXName + "QueryResults",
                                  new XElement("queryName"),
                                  new XElement("resultsBody",
                                      new XElement("EventList")))));

            XElement xQueryName = xDoc.Root?.Element("EPCISBody")?.Element(epcisQueryXName + "QueryResults")?.Element("queryName");
            if (xQueryName != null)
            {
                xQueryName.Value = doc.QueryName;
            }

            // write the events
            XElement xEventList = xDoc.Root?.Element("EPCISBody")?.Element(epcisQueryXName + "QueryResults")?.Element("resultsBody")?.Element("EventList")
                ?? throw new Exception("Failed to get EPCISBody/EventList after adding it to the XDoc.Root");
            foreach (IEvent e in doc.Events)
            {
                string xname = EPCISDocumentBaseXMLMapper.GetEventXName(e);
                XElement xEvent = OpenTraceabilityXmlMapper.ToXml(xname, e, doc.EPCISVersion.Value);
                if (e.EventType == EventType.TransformationEvent && doc.EPCISVersion.Value == EPCISVersion.V1)
                {
                    xEvent = new XElement("extension", xEvent);
                }
                if (xEvent != null)
                {
                    xEventList.Add(xEvent);
                }
            }

            if (checkSchema)
            {
                EPCISDocumentBaseXMLMapper.ValidateEPCISQueryDocumentSchema(xDoc, doc.EPCISVersion.Value); 
            }

            return xDoc.ToString();
        }

        public Task<EPCISQueryDocument> MapAsync(string strValue, bool checkSchema = true)
        {
            return Task.FromResult(Map(strValue, checkSchema));
        }

        public Task<string> MapAsync(EPCISQueryDocument doc, bool checkSchema = true)
        {
            return Task.FromResult(Map(doc, checkSchema));
        }
    }
}