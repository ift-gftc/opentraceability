using OpenTraceability.Interfaces;
using OpenTraceability.Models.Events;
using OpenTraceability.Models.Events.KDEs;
using OpenTraceability.Models.Identifiers;
using OpenTraceability.Utility;
using System;
using System.Collections.Generic;
using System.Globalization;
using System.Linq;
using System.Security.Cryptography.X509Certificates;
using System.Text;
using System.Threading.Tasks;
using System.Xml.Linq;
using System.Xml.XPath;

namespace OpenTraceability.Mappers.EPCIS.XML
{
    /// <summary>
    /// This class is used for reading an event from XML and converting it into an IEvent.
    /// </summary>
    public static class EPCISXmlEventReader
    {
        // https://ref.gs1.org/standards/epcis/epcglobal-epcis-2_0.xsd

        private static List<EPCISXmlKDE> baseKDEs = new List<EPCISXmlKDE>()
        {
            new EPCISXmlKDE("eventTime", ReadEventTime),
            new EPCISXmlKDE("recordTime", ReadRecordTime),
            new EPCISXmlKDE("eventTimeZoneOffset", ReadEventTimeZoneOffset),
            new EPCISXmlKDE("eventID", ReadEventID, EPCISVersion.Version_2_0),
            new EPCISXmlKDE("errorDeclaration", ReadErrorDeclaration, EPCISVersion.Version_2_0),
            new EPCISXmlKDE("certificationInfo", ReadCertificationInfo, EPCISVersion.Version_2_0),
            new EPCISXmlKDE("baseExtension/eventID", ReadEventID, EPCISVersion.Version_1_2),
            new EPCISXmlKDE("baseExtension/errorDeclaration", ReadErrorDeclaration, EPCISVersion.Version_1_2),
        };

        private static List<EPCISXmlKDE> objectKDEs = new List<EPCISXmlKDE>()
        {
            new EPCISXmlKDE("epcList", ReadEPCList),
            new EPCISXmlKDE("action", ReadAction),
            new EPCISXmlKDE("bizStep", ReadBizStep),
            new EPCISXmlKDE("disposition", ReadDisposition),
            new EPCISXmlKDE("readPoint", ReadReadPoint),
            new EPCISXmlKDE("bizLocation", ReadBizLocation),
            new EPCISXmlKDE("bizTransactionList", ReadBizTransactionList),
            new EPCISXmlKDE("quantityList", ReadQuantityList, EPCISVersion.Version_2_0),
            new EPCISXmlKDE("sourceList", ReadSourceList, EPCISVersion.Version_2_0),
            new EPCISXmlKDE("destinationList", ReadDestinationList, EPCISVersion.Version_2_0),
            new EPCISXmlKDE("sensorElementList", ReadSensorElementList, EPCISVersion.Version_2_0),
            new EPCISXmlKDE("persistentDisposition", ReadPersistentDisposition, EPCISVersion.Version_2_0),
            new EPCISXmlKDE("ilmd", ReadILMD, EPCISVersion.Version_2_0),
            new EPCISXmlKDE("extension/quantityList", ReadQuantityList, EPCISVersion.Version_1_2),
            new EPCISXmlKDE("extension/sourceList", ReadSourceList, EPCISVersion.Version_1_2),
            new EPCISXmlKDE("extension/destinationList", ReadDestinationList, EPCISVersion.Version_1_2),
            new EPCISXmlKDE("extension/ilmd", ReadILMD, EPCISVersion.Version_1_2)
        };

        private static List<EPCISXmlKDE> transformationKDEs = new List<EPCISXmlKDE>()
        {
            new EPCISXmlKDE("inputEPCList", ReadInputEPCList),
            new EPCISXmlKDE("inputQuantityList", ReadInputQuantityList),
            new EPCISXmlKDE("outputEPCList", ReadOutputEPCList),
            new EPCISXmlKDE("outputQuantityList", ReadOutputQuantityList),
            new EPCISXmlKDE("transformationID", ReadTransformationID),
            new EPCISXmlKDE("bizStep", ReadBizStep),
            new EPCISXmlKDE("disposition", ReadDisposition),
            new EPCISXmlKDE("readPoint", ReadReadPoint),
            new EPCISXmlKDE("bizLocation", ReadBizLocation),
            new EPCISXmlKDE("bizTransactionList", ReadBizTransactionList),
            new EPCISXmlKDE("sourceList", ReadSourceList, EPCISVersion.Version_2_0),
            new EPCISXmlKDE("destinationList", ReadDestinationList, EPCISVersion.Version_2_0),
            new EPCISXmlKDE("sensorElementList", ReadSensorElementList, EPCISVersion.Version_2_0),
            new EPCISXmlKDE("persistentDisposition", ReadPersistentDisposition, EPCISVersion.Version_2_0),
            new EPCISXmlKDE("ilmd", ReadILMD, EPCISVersion.Version_2_0),
            new EPCISXmlKDE("extension/quantityList", ReadQuantityList, EPCISVersion.Version_1_2),
            new EPCISXmlKDE("extension/sourceList", ReadSourceList, EPCISVersion.Version_1_2),
            new EPCISXmlKDE("extension/destinationList", ReadDestinationList, EPCISVersion.Version_1_2),
            new EPCISXmlKDE("extension/ilmd", ReadILMD, EPCISVersion.Version_1_2)
        };

        private static List<EPCISXmlKDE> transactionKDEs = new List<EPCISXmlKDE>()
        {
            new EPCISXmlKDE("bizTransactionList", ReadBizTransactionList),
            new EPCISXmlKDE("parentID", ReadParentID),
            new EPCISXmlKDE("epcList", ReadEPCList),
            new EPCISXmlKDE("action", ReadAction),
            new EPCISXmlKDE("bizStep", ReadBizStep),
            new EPCISXmlKDE("disposition", ReadDisposition),
            new EPCISXmlKDE("readPoint", ReadReadPoint),
            new EPCISXmlKDE("bizLocation", ReadBizLocation),
            new EPCISXmlKDE("quantityList", ReadQuantityList, EPCISVersion.Version_2_0),
            new EPCISXmlKDE("sourceList", ReadSourceList, EPCISVersion.Version_2_0),
            new EPCISXmlKDE("destinationList", ReadDestinationList, EPCISVersion.Version_2_0),
            new EPCISXmlKDE("sensorElementList", ReadSensorElementList, EPCISVersion.Version_2_0),
            new EPCISXmlKDE("persistentDisposition", ReadPersistentDisposition, EPCISVersion.Version_2_0),
            new EPCISXmlKDE("extension/quantityList", ReadQuantityList, EPCISVersion.Version_1_2),
            new EPCISXmlKDE("extension/sourceList", ReadSourceList, EPCISVersion.Version_1_2),
            new EPCISXmlKDE("extension/destinationList", ReadDestinationList, EPCISVersion.Version_1_2),
        };

        private static List<EPCISXmlKDE> aggregationKDEs = new List<EPCISXmlKDE>()
        {
            new EPCISXmlKDE("parentID", ReadParentID),
            new EPCISXmlKDE("childEPCs", ReadChildEPCList),
            new EPCISXmlKDE("action", ReadAction),
            new EPCISXmlKDE("bizStep", ReadBizStep),
            new EPCISXmlKDE("disposition", ReadDisposition),
            new EPCISXmlKDE("readPoint", ReadReadPoint),
            new EPCISXmlKDE("bizLocation", ReadBizLocation),
            new EPCISXmlKDE("bizTransactionList", ReadBizTransactionList),
            new EPCISXmlKDE("childQuantityList", ReadChildQuantityList, EPCISVersion.Version_2_0),
            new EPCISXmlKDE("sourceList", ReadSourceList, EPCISVersion.Version_2_0),
            new EPCISXmlKDE("destinationList", ReadDestinationList, EPCISVersion.Version_2_0),
            new EPCISXmlKDE("sensorElementList", ReadSensorElementList, EPCISVersion.Version_2_0),
            new EPCISXmlKDE("persistentDisposition", ReadPersistentDisposition, EPCISVersion.Version_2_0),
            new EPCISXmlKDE("ilmd", ReadILMD, EPCISVersion.Version_2_0),
            new EPCISXmlKDE("extension/childQuantityList", ReadChildQuantityList, EPCISVersion.Version_1_2),
            new EPCISXmlKDE("extension/sourceList", ReadSourceList, EPCISVersion.Version_1_2),
            new EPCISXmlKDE("extension/destinationList", ReadDestinationList, EPCISVersion.Version_1_2)
        };

        private static List<EPCISXmlKDE> associationKDEs = new List<EPCISXmlKDE>()
        {
            new EPCISXmlKDE("parentID", ReadParentID),
            new EPCISXmlKDE("childEPCs", ReadChildEPCList),
            new EPCISXmlKDE("childQuantityList", ReadChildQuantityList),
            new EPCISXmlKDE("action", ReadAction),
            new EPCISXmlKDE("bizStep", ReadBizStep),
            new EPCISXmlKDE("disposition", ReadDisposition),
            new EPCISXmlKDE("readPoint", ReadReadPoint),
            new EPCISXmlKDE("bizLocation", ReadBizLocation),
            new EPCISXmlKDE("bizTransactionList", ReadBizTransactionList),
            new EPCISXmlKDE("sourceList", ReadSourceList),
            new EPCISXmlKDE("destinationList", ReadDestinationList),
            new EPCISXmlKDE("sensorElementList", ReadSensorElementList),
            new EPCISXmlKDE("persistentDisposition", ReadPersistentDisposition)
        };

        private static List<string> sensorMetaDataAttributes = new List<string>() { "time", "deviceID", "deviceMetadata", "rawData", "startTime", "endTime", "bizRules", "dataProcessingMethod" };
        private static List<string> sensorReportAttributes = new List<string>() { "type", "value", "component", "stringValue", "booleanValue", "hexBinaryValue", "uriValue", "uom", "minValue", "maxValue", "sDev", "chemicalSubstance", "microorganism", "deviceID", "deviceMetadata", "rawData", "time", "meanValue", "percRank", "percValue", "dataProcessingMethod", "coordinateReferenceSystem", "exception" };

        public static IEvent ReadEvent(XElement xEvent, EPCISVersion epcisVersion)
        {
            IEvent? e = null;
            List<EPCISXmlKDE> kdes = new List<EPCISXmlKDE>(baseKDEs);

            if (xEvent.Name == "ObjectEvent")
            {
                e = new ObjectEvent();
                kdes.AddRange(objectKDEs);
            }
            else if (xEvent.Name == "TransformationEvent")
            {
                e = new TransformationEvent();
                kdes.AddRange(transformationKDEs);
            }
            //else if (xEvent.Name == "")
            //{
            //    e = new TransactionEvent();
            //    kdes.AddRange(transactionKDEs);
            //}
            else if (xEvent.Name == "AggregationEvent")
            {
                e = new AggregationEvent();
                kdes.AddRange(aggregationKDEs);
            }
            else if (xEvent.Name == "AssociationEvent")
            {
                e = new AssociationEvent();
                kdes.AddRange(associationKDEs);
            }
            else
            {
                throw new Exception("Unrecognized event type = " + xEvent.Name);
            }

            kdes = kdes.Where(k => k.RequiredVersion == null || k.RequiredVersion == epcisVersion).ToList();


            // go through each KDE in our list...
            int i = 0;
            foreach (XElement x in xEvent.Elements())
            {
                // move through our list of KDEs until we find that the current element matches it...
                while (i < kdes.Count)
                {
                    // here we need to handle if we are processing a child element like baseExtension or extension
                    if (kdes[i].Name.Contains("/"))
                    {
                        string[] parts = kdes[i].Name.Split("/");
                        while (x.Name == parts[0])
                        {
                            foreach (XElement xChild in x.Elements())
                            {
                                if (xChild.Name == parts[2])
                                {
                                    // process the KDe and move to next KDE...
                                    kdes[i].Action(kdes[i].Name, e, xChild);

                                    i++;
                                    if (kdes[i].Name.Contains("/"))
                                    { 
                                        parts = kdes[i].Name.Split("/");
                                    }
                                    else
                                    {
                                        break;
                                    }
                                }
                            }
                            break;
                        }
                    }
                    // processing a standard root KDE...
                    else if (x.Name == kdes[i].Name)
                    {
                        // process the KDEs and break...
                        kdes[i].Action(kdes[i].Name, e, x);
                        i++;
                        break;
                    }
                    // did not find a match, so it might not exist in the XML, so lets move to the next KDE...
                    else
                    {
                        i++;
                    }
                }

                // if we reached the end of our list of kdes... assume it is an extension KDE...
                if (i >= kdes.Count)
                {
                    IEventKDE kde = ReadKDE(x);
                    e.AddKDE(kde);
                }
            }

            return e;
        }

        private static void ReadEventTime(string xName, IEvent e, XElement x)
        {
            string strValue = x.Value;

            for (int i = 1; i <= 6; i++)
            {
                string f = "".PadLeft(i,'f');
                if (DateTimeOffset.TryParseExact(strValue, $"yyyy-MM-ddTHH:mm:ss.{f}Z", CultureInfo.InvariantCulture, DateTimeStyles.None, out DateTimeOffset dt))
                {
                    e.EventTime = dt;
                    return;
                }
                else if (DateTimeOffset.TryParseExact(strValue, $"yyyy-MM-ddTHH:mm:ss.{f}K", CultureInfo.InvariantCulture, DateTimeStyles.None, out dt))
                {
                    e.EventTime = dt;
                    return;
                }
            }

            throw new Exception($"The event time {strValue} is not in a recognized format.");
        }

        private static void ReadRecordTime(string xName, IEvent e, XElement x)
        {
            string strValue = x.Value;

            for (int i = 1; i <= 6; i++)
            {
                string f = "".PadLeft(i, 'f');
                if (DateTime.TryParseExact(strValue, $"yyyy-MM-ddTHH:mm:ss.{f}Z", CultureInfo.InvariantCulture, DateTimeStyles.None, out DateTime dt))
                {
                    e.Recorded = dt;
                    return;
                }
                else if (DateTime.TryParseExact(strValue, $"yyyy-MM-ddTHH:mm:ss.{f}K", CultureInfo.InvariantCulture, DateTimeStyles.None, out dt))
                {
                    e.Recorded = dt;
                    return;
                }
            }

            throw new Exception($"The recorded time {strValue} is not in a recognized format.");
        }

        private static void ReadEventTimeZoneOffset(string xName, IEvent e, XElement x)
        {
            string hours = x.Value.Substring(1);
            TimeSpan ts = TimeSpan.Parse(hours);
            double dbl = ts.TotalHours;
            if (x.Value[0] == '-')
            {
                dbl = -dbl;
            }
            e.EventTimeOffset = dbl;
        }

        private static void ReadEventID(string xName, IEvent e, XElement x)
        {
            e.EventID = x.Value;
        }

        private static void ReadCertificationInfo(string xName, IEvent e, XElement x)
        {
            e.CertificationInfo = x.Value;
        }

        private static void ReadTransformationID(string xName, IEvent e, XElement x)
        {
            ((TransformationEvent)e).TransformationID = x.Value;   
        }

        private static void ReadErrorDeclaration(string xName, IEvent e, XElement x)
        {
            //<errorDeclaration>
            //    <declarationTime>2022-02-08T19:41:23</declarationTime>
            //    <correctiveEventIDs>
            //        <correctiveEventID>0038bdc2-cad3-43eb-88f5-c93eda34ec43</correctiveEventID>
            //    </correctiveEventIDs>
            //    <extension />
            //</errorDeclaration>

            ErrorDeclaration err = new ErrorDeclaration();
            err.DeclarationTime = DateTime.Parse(x.Element("declarationTime")?.Value ?? throw new Exception("No declarationTime on the error declaration."));

            XElement? xReason = x.Element("reason");
            if (xReason != null)
            {
                err.RawReason = new Uri(xReason.Value);
            }

            XElement? xCorrectiveEventIDs = x.Element("correctiveEventIDs");
            if (xCorrectiveEventIDs != null)
            {
                err.CorrectingEventIDs = new List<string>();
                foreach (XElement xCorrEventID in xCorrectiveEventIDs.Elements("correctiveEventID"))
                {
                    err.CorrectingEventIDs.Add(xCorrEventID.Value);
                }
            }

            // TODO: support extension kdes

            e.ErrorDeclaration = err;
        }

        private static void ReadEPCList(string xName, IEvent e, XElement x)
        {
            foreach (var xEPC in x.Elements("epc"))
            {
                EventProduct product = new EventProduct();
                product.Type = EventProductType.Reference;
                product.EPC = new EPC(xEPC.Value);
                e.AddProduct(product);
            }
        }

        private static void ReadChildEPCList(string xName, IEvent e, XElement x)
        {
            foreach (var xEPC in x.Elements("epc"))
            {
                EventProduct product = new EventProduct();
                product.Type = EventProductType.Child;
                product.EPC = new EPC(xEPC.Value);
                e.AddProduct(product);
            }
        }

        private static void ReadInputEPCList(string xName, IEvent e, XElement x)
        {
            foreach (var xEPC in x.Elements("epc"))
            {
                EventProduct product = new EventProduct();
                product.Type = EventProductType.Input;
                product.EPC = new EPC(xEPC.Value);
                e.AddProduct(product);
            }
        }

        private static void ReadOutputEPCList(string xName, IEvent e, XElement x)
        {
            foreach (var xEPC in x.Elements("epc"))
            {
                EventProduct product = new EventProduct();
                product.Type = EventProductType.Output;
                product.EPC = new EPC(xEPC.Value);
                e.AddProduct(product);
            }
        }

        private static void ReadAction(string xName, IEvent e, XElement x)
        {
            if (Enum.TryParse<EventAction>(x.Value, out EventAction action))
            {
                e.Action = action;
            }
        }

        private static void ReadBizStep(string xName, IEvent e, XElement x)
        {
            e.BusinessStep = x.Value;
        }

        private static void ReadDisposition(string xName, IEvent e, XElement x)
        {
            e.Disposition = x.Value;
        }

        private static void ReadReadPoint(string xName, IEvent e, XElement x)
        {
            XElement? xID = x.Element("id");
            if (xID != null)
            {
                e.ReadPoint = new EventReadPoint();
                e.ReadPoint.ID = xID.Value;
            }
        }

        private static void ReadBizLocation(string xName, IEvent e, XElement x)
        {
            XElement? xID = x.Element("id");
            if (xID != null)
            {
                e.Location = new EventLocation();
                e.Location.GLN = new GLN(xID.Value);
            }
        }

        private static void ReadBizTransactionList(string xName, IEvent e, XElement x)
        {
            foreach (var xBizTransaction in x.Elements("bizTransaction"))
            {
                EventBusinessTransaction bizTransaction = new EventBusinessTransaction();
                bizTransaction.RawType = xBizTransaction.Attribute("type")?.Value;
                bizTransaction.Value = xBizTransaction.Value;
                e.BusinessTransactions.Add(bizTransaction);
            }
        }

        private static void ReadQuantityList(string xName, IEvent e, XElement x)
        {
            foreach (var xQuantity in x.Elements("quantityElement"))
            {
                EventProduct product = new EventProduct();
                product.Type = EventProductType.Reference;
                product.EPC = new EPC(xQuantity.Element("epcClass")?.Value ?? string.Empty);

                double quantity = double.Parse(xQuantity.Element("quantity")?.Value ?? string.Empty);
                string uom = xQuantity.Element("uom")?.Value ?? "EA";
                product.Quantity = new Measurement(quantity, uom);

                e.AddProduct(product);
            }
        }

        private static void ReadChildQuantityList(string xName, IEvent e, XElement x)
        {
            foreach (var xQuantity in x.Elements("quantityElement"))
            {
                EventProduct product = new EventProduct();
                product.Type = EventProductType.Child;
                product.EPC = new EPC(xQuantity.Element("epcClass")?.Value ?? string.Empty);

                double quantity = double.Parse(xQuantity.Element("quantity")?.Value ?? string.Empty);
                string uom = xQuantity.Element("uom")?.Value ?? "EA";
                product.Quantity = new Measurement(quantity, uom);

                e.AddProduct(product);
            }
        }

        private static void ReadInputQuantityList(string xName, IEvent e, XElement x)
        {
            foreach (var xQuantity in x.Elements("quantityElement"))
            {
                EventProduct product = new EventProduct();
                product.Type = EventProductType.Input;
                product.EPC = new EPC(xQuantity.Element("epcClass")?.Value ?? string.Empty);

                double quantity = double.Parse(xQuantity.Element("quantity")?.Value ?? string.Empty);
                string uom = xQuantity.Element("uom")?.Value ?? "EA";
                product.Quantity = new Measurement(quantity, uom);

                e.AddProduct(product);
            }
        }

        private static void ReadOutputQuantityList(string xName, IEvent e, XElement x)
        {
            foreach (var xQuantity in x.Elements("quantityElement"))
            {
                EventProduct product = new EventProduct();
                product.Type = EventProductType.Output;
                product.EPC = new EPC(xQuantity.Element("epcClass")?.Value ?? string.Empty);

                double quantity = double.Parse(xQuantity.Element("quantity")?.Value ?? string.Empty);
                string uom = xQuantity.Element("uom")?.Value ?? "EA";
                product.Quantity = new Measurement(quantity, uom);

                e.AddProduct(product);
            }
        }

        private static void ReadSourceList(string xName, IEvent e, XElement x)
        {
            foreach (var xSource in x.Elements("source"))
            {
                EventSource source = new EventSource();
                source.RawType = xSource.Attribute("type")?.Value;
                source.Value = xSource.Value;
                e.SourceList.Add(source);
            }
        }

        private static void ReadDestinationList(string xName, IEvent e, XElement x)
        {
            foreach (var xDest in x.Elements("destination"))
            {
                EventDestination dest = new EventDestination();
                dest.RawType = xDest.Attribute("type")?.Value;
                dest.Value = xDest.Value;
                e.DestinationList.Add(dest);
            }
        }

        private static void ReadSensorElementList(string xName, IEvent e, XElement x)
        {
            foreach (XElement xSensor in x.Elements())
            {
                SensorElement s = new SensorElement();

                // sensor metadata
                XElement? xSensorMetaData = xSensor.Element("sensorMetadata");
                if (xSensorMetaData != null)
                {
                    s.MetaData = new SensorMetaData();
                    s.MetaData.TimeStamp = xSensorMetaData.AttributeISODateTime("time");
                    s.MetaData.DeviceID = xSensorMetaData.AttributeURI("deviceID");
                    s.MetaData.StartTime = xSensorMetaData.AttributeISODateTime("startTime");
                    s.MetaData.EndTime = xSensorMetaData.AttributeISODateTime("endTime");
                    s.MetaData.DeviceMetaData = xSensorMetaData.AttributeURI("deviceMetadata");
                    s.MetaData.RawData = xSensorMetaData.AttributeURI("rawData");
                    s.MetaData.BizRules = xSensorMetaData.AttributeURI("bizRules");
                    s.MetaData.DataProcessingMethod = xSensorMetaData.AttributeURI("dataProcessingMethod");

                    // TODO: extension attributes...
                    foreach (XAttribute xa in xSensorMetaData.Attributes().Where(a => !sensorMetaDataAttributes.Contains(a.Name.LocalName)))
                    {
                        IEventKDE kde = ReadAttributeKDE(xa);
                        s.MetaData.ExtensionAttributes.Add(kde);
                    }
                }                

                // sensor report(s)
                foreach (XElement xSensorReport in xSensor.Elements("sensorReport"))
                {
                    SensorReport sReport = new SensorReport();
                    sReport.Type = xSensorReport.AttributeURI("type");
                    sReport.Value = xSensorReport.AttributeDouble("value");
                    sReport.Component = xSensorReport.AttributeURI("component");
                    sReport.StringValue = xSensorReport.Attribute("stringValue")?.Value;
                    sReport.BooleanValue = xSensorReport.AttributeBoolean("booleanValue");
                    sReport.HexBinaryValue = xSensorReport.Attribute("hexBinaryValue")?.Value;
                    sReport.URIValue = xSensorReport.AttributeURI("uriValue");
                    sReport.UOM = xSensorReport.AttributeUOM("uom");
                    sReport.MinValue = xSensorReport.AttributeDouble("minValue");
                    sReport.MaxValue = xSensorReport.AttributeDouble("maxValue");
                    sReport.SDev = xSensorReport.AttributeDouble("sDev");
                    sReport.ChemicalSubstance = xSensorReport.AttributeURI("chemicalSubstance");
                    sReport.MicroOrganism = xSensorReport.AttributeURI("microorganism");
                    sReport.DeviceID = xSensorReport.AttributeURI("deviceID");
                    sReport.DeviceMetadata = xSensorReport.AttributeURI("deviceMetadata");
                    sReport.RawData = xSensorReport.AttributeURI("rawData");
                    sReport.TimeStamp = xSensorReport.AttributeISODateTime("time");
                    sReport.MeanValue = xSensorReport.AttributeDouble("meanValue");
                    sReport.PercentageValue = xSensorReport.AttributeDouble("percValue");
                    sReport.PercentageRank = xSensorReport.AttributeDouble("percRank");
                    sReport.DataProcessingMethod = xSensorReport.AttributeURI("dataProcessingMethod");
                    sReport.CoordinateReferenceSystem = xSensorReport.AttributeURI("coordinateReferenceSystem");
                    sReport.Exception = xSensorReport.AttributeURI("exception");

                    // TODO: extension attributes...
                    foreach (XAttribute xa in xSensorReport.Attributes().Where(a => !sensorReportAttributes.Contains(a.Name.LocalName)))
                    {
                        IEventKDE kde = ReadAttributeKDE(xa);
                        sReport.ExtensionAttributes.Add(kde);
                    }

                    s.Reports.Add(sReport);
                }

                // TODO: extension elements...
                foreach (XElement xKDE in xSensor.XPathSelectElements("*[not(self::sensorReport) and not(self::sensorMetadata)]"))
                {
                    IEventKDE kde = ReadKDE(xKDE);
                    s.ExtensionKDEs.Add(kde);
                }
                

                e.SensorElementList.Add(s);
            }
        }

        private static void ReadPersistentDisposition(string xName, IEvent e, XElement x)
        {
            e.PersistentDisposition = new PersistentDisposition();

            if (x.Element("set") != null)
            {
                e.PersistentDisposition.Set = new List<string>();
                foreach (var xSet in x.Elements("set"))
                {
                    e.PersistentDisposition.Set.Add(xSet.Value);
                }
            }

            if (x.Element("unset") != null)
            {
                e.PersistentDisposition.Unset = new List<string>();
                foreach (var xUnset in x.Elements("unset"))
                {
                    e.PersistentDisposition.Unset.Add(xUnset.Value);
                }
            }
        }

        private static void ReadILMD(string xName, IEvent e, XElement x)
        {
            e.ILMD = new EventILMD();

            // the ILMD are just a series of extension KDEs
            foreach (XElement xKDE in x.Elements())
            {
                // read the kde...
                IEventKDE kde = ReadKDE(xKDE);
                e.ILMD.AddKDE(kde);
            }
        }

        private static void ReadParentID(string xName, IEvent e, XElement x)
        {
            EventProduct product = new EventProduct();
            product.EPC = new EPC(x.Value);
            product.Type = EventProductType.Parent;
            e.AddProduct(product);
        }

        private static IEventKDE ReadKDE(XElement x)
        {
            // we need to parse the xml into an event KDE here...

            // check if it is a registered KDE...
            IEventKDE? kde = IEventKDE.InitializeKDE(x.Name.NamespaceName, x.Name.LocalName);

            // if not, then check if the data type is specified and we recognize it
            if (kde == null)
            {
                XAttribute? xsiType = x.Attribute((XNamespace)Constants.XSI_NAMESPACE + "type");
                if (xsiType != null)
                {
                    switch (xsiType.Value)
                    {
                        case "string": kde = new EventKDEString(x.Name.NamespaceName, x.Name.LocalName); break;
                        case "boolean": kde = new EventKDEBoolean(x.Name.NamespaceName, x.Name.LocalName); break;
                        case "number": kde = new EventKDEDouble(x.Name.NamespaceName, x.Name.LocalName); break;
                    }
                }
            }

            // if not, check if it is a simple value or an object
            if (kde == null)
            {
                if (x.Elements().Count() > 0)
                {
                    kde = new EventKDEObject(x.Name.NamespaceName, x.Name.LocalName);
                }
                // else if simple value, then we will consume it as a string
                else
                {
                    kde = new EventKDEString(x.Name.NamespaceName, x.Name.LocalName);
                }
            }

            if (kde != null)
            {
                kde.SetFromXml(x);
            }
            else
            {
                throw new Exception("Failed to initialize KDE from XML = " + x.ToString());
            }

            return kde;
        }

        private static IEventKDE ReadAttributeKDE(XAttribute x)
        {
            // we need to parse the xml into an event KDE here...

            // check if it is a registered KDE...
            IEventKDE? kde = IEventKDE.InitializeKDE(x.Name.NamespaceName, x.Name.LocalName);

            // if not, check if it is a simple value or an object
            if (kde == null)
            {
                kde = new EventKDEString(x.Name.NamespaceName, x.Name.LocalName);
            }

            if (kde != null)
            {
                // here we will convert the attribute into a simple element for fitting into the interface
                XElement xE = new XElement(x.Name, x.Value);
                kde.SetFromXml(xE);
            }
            else
            {
                throw new Exception("Failed to initialize KDE from XML = " + x.ToString());
            }

            return kde;
        }
    }
}
