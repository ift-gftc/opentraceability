using OpenTraceability.Interfaces;
using OpenTraceability.Models.Events;
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

        public static IEvent ReadEvent(XElement xEvent, EPCISVersion epcisVersion)
        {
            IEvent? e = null;
            List<EPCISXmlKDE> kdes = new List<EPCISXmlKDE>(baseKDEs);

            if (xEvent.Name == "ObjectEvent")
            {
                e = new ObjectEvent();
                kdes.AddRange(objectKDEs);
            }
            else if (xEvent.Name == "")
            {
                e = new TransformationEvent();
                kdes.AddRange(transformationKDEs);
            }
            else if (xEvent.Name == "")
            {
                e = new TransactionEvent();
                kdes.AddRange(transactionKDEs);
            }
            else if (xEvent.Name == "")
            {
                e = new AggregationEvent();
                kdes.AddRange(aggregationKDEs);
            }
            else if (xEvent.Name == "")
            {
                e = new AssociationEvent();
                kdes.AddRange(associationKDEs);
            }
            else
            {
                throw new Exception("Unrecognized event type = " + xEvent.Name);
            }


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
                                    kdes[i].Action(e, xChild);

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
                        }
                        break;
                    }
                    // processing a standard root KDE...
                    else if (x.Name == kdes[i].Name)
                    {
                        // process the KDEs and break...
                        kdes[i].Action(e, x);
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
                IEventKDE kde = ReadKDE(x);
                e.KDEs.Add(kde);
            }

            return e;
        }

        private static void ReadEventTime(IEvent e, XElement x)
        {
            string strValue = x.Value;

            if (DateTimeOffset.TryParseExact(strValue, "yyyy-MM-ddTHH:mm:ss.fffZ", CultureInfo.InvariantCulture, DateTimeStyles.None, out DateTimeOffset dt))
            {
                e.EventTime = dt;
            }

            if (DateTimeOffset.TryParseExact(strValue, "yyyy-MM-ddTHH:mm:ss.ffZ", CultureInfo.InvariantCulture, DateTimeStyles.None, out dt))
            {
                e.EventTime = dt;
            }

            if (DateTimeOffset.TryParseExact(strValue, "yyyy-MM-ddTHH:mm:ss.fZ", CultureInfo.InvariantCulture, DateTimeStyles.None, out dt))
            {
                e.EventTime = dt;
            }

            if (DateTimeOffset.TryParseExact(strValue, "yyyy-MM-ddTHH:mm:ssZ", CultureInfo.InvariantCulture, DateTimeStyles.None, out dt))
            {
                e.EventTime = dt;
            }

            throw new Exception("The event time {strValue} is not in a recognized format.");
        }

        private static void ReadRecordTime(IEvent e, XElement x)
        {
            string strValue = x.Value;

            if (DateTime.TryParseExact(strValue, "yyyy-MM-ddTHH:mm:ss.fffZ", CultureInfo.InvariantCulture, DateTimeStyles.None, out DateTime dt))
            {
                e.Recorded = dt;
            }

            if (DateTime.TryParseExact(strValue, "yyyy-MM-ddTHH:mm:ss.ffZ", CultureInfo.InvariantCulture, DateTimeStyles.None, out dt))
            {
                e.Recorded = dt;
            }

            if (DateTime.TryParseExact(strValue, "yyyy-MM-ddTHH:mm:ss.fZ", CultureInfo.InvariantCulture, DateTimeStyles.None, out dt))
            {
                e.Recorded = dt;
            }

            if (DateTime.TryParseExact(strValue, "yyyy-MM-ddTHH:mm:ssZ", CultureInfo.InvariantCulture, DateTimeStyles.None, out dt))
            {
                e.Recorded = dt;
            }

            throw new Exception("The event time {strValue} is not in a recognized format.");
        }

        private static void ReadEventTimeZoneOffset(IEvent e, XElement x)
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

        private static void ReadEventID(IEvent e, XElement x)
        {
            e.EventID = x.Value;
        }

        private static void ReadCertificationInfo(IEvent e, XElement x)
        {
            e.CertificationInfo = x.Value;
        }

        private static void ReadTransformationID(IEvent e, XElement x)
        {
            ((TransformationEvent)e).TransformationID = x.Value;   
        }

        private static void ReadErrorDeclaration(IEvent e, XElement x)
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

            XElement? xCorrectiveEventIDs = x.Element("correctiveEventIDs");
            if (xCorrectiveEventIDs != null)
            {
                err.CorrectingEventIDs = new List<string>();
                foreach (XElement xCorrEventID in xCorrectiveEventIDs.Elements("correctiveEventID"))
                {
                    err.CorrectingEventIDs.Add(xCorrEventID.Value);
                }
            }

            e.ErrorDeclaration = err;
        }

        private static void ReadEPCList(IEvent e, XElement x)
        {
            foreach (var xEPC in x.Elements("epc"))
            {
                EventProduct product = new EventProduct();
                product.Type = EventProductType.Reference;
                product.EPC = new EPC(xEPC.Value);
                e.AddProduct(product);
            }
        }

        private static void ReadChildEPCList(IEvent e, XElement x)
        {
            foreach (var xEPC in x.Elements("epc"))
            {
                EventProduct product = new EventProduct();
                product.Type = EventProductType.Child;
                product.EPC = new EPC(xEPC.Value);
                e.AddProduct(product);
            }
        }

        private static void ReadInputEPCList(IEvent e, XElement x)
        {
            foreach (var xEPC in x.Elements("epc"))
            {
                EventProduct product = new EventProduct();
                product.Type = EventProductType.Input;
                product.EPC = new EPC(xEPC.Value);
                e.AddProduct(product);
            }
        }

        private static void ReadOutputEPCList(IEvent e, XElement x)
        {
            foreach (var xEPC in x.Elements("epc"))
            {
                EventProduct product = new EventProduct();
                product.Type = EventProductType.Output;
                product.EPC = new EPC(xEPC.Value);
                e.AddProduct(product);
            }
        }

        private static void ReadAction(IEvent e, XElement x)
        {
            if (Enum.TryParse<EventAction>(x.Value, out EventAction action))
            {
                e.Action = action;
            }
        }

        private static void ReadBizStep(IEvent e, XElement x)
        {
            e.BusinessStep = x.Value;
        }

        private static void ReadDisposition(IEvent e, XElement x)
        {
            e.Disposition = x.Value;
        }

        private static void ReadReadPoint(IEvent e, XElement x)
        {
            XElement? xID = x.Element("id");
            if (xID != null)
            {
                e.ReadPoint = new EventReadPoint();
                e.ReadPoint.ID = xID.Value;
            }
        }

        private static void ReadBizLocation(IEvent e, XElement x)
        {
            XElement? xID = x.Element("id");
            if (xID != null)
            {
                e.Location = new EventLocation();
                e.Location.GLN = new GLN(xID.Value);
            }
        }

        private static void ReadBizTransactionList(IEvent e, XElement x)
        {
            foreach (var xBizTransaction in x.Elements("bizTransaction"))
            {
                EventBusinessTransaction bizTransaction = new EventBusinessTransaction();
                bizTransaction.RawType = xBizTransaction.Attribute("type")?.Value;
                bizTransaction.Value = xBizTransaction.Value;
                e.BusinessTransactions.Add(bizTransaction);
            }
        }

        private static void ReadQuantityList(IEvent e, XElement x)
        {
            foreach (var xQuantity in x.Elements("quantityElement"))
            {
                EventProduct product = new EventProduct();
                product.Type = EventProductType.Reference;
                product.EPC = new EPC(xQuantity.Element("epcClass")?.Value);

                double quantity = double.Parse(xQuantity.Element("quantity")?.Value);
                string uom = xQuantity.Element("uom")?.Value;
                product.Quantity = new Measurement(quantity, uom);

                e.AddProduct(product);
            }
        }

        private static void ReadChildQuantityList(IEvent e, XElement x)
        {
            foreach (var xQuantity in x.Elements("quantityElement"))
            {
                EventProduct product = new EventProduct();
                product.Type = EventProductType.Child;
                product.EPC = new EPC(xQuantity.Element("epcClass")?.Value);

                double quantity = double.Parse(xQuantity.Element("quantity")?.Value);
                string uom = xQuantity.Element("uom")?.Value;
                product.Quantity = new Measurement(quantity, uom);

                e.AddProduct(product);
            }
        }

        private static void ReadInputQuantityList(IEvent e, XElement x)
        {
            foreach (var xQuantity in x.Elements("quantityElement"))
            {
                EventProduct product = new EventProduct();
                product.Type = EventProductType.Input;
                product.EPC = new EPC(xQuantity.Element("epcClass")?.Value);

                double quantity = double.Parse(xQuantity.Element("quantity")?.Value);
                string uom = xQuantity.Element("uom")?.Value;
                product.Quantity = new Measurement(quantity, uom);

                e.AddProduct(product);
            }
        }

        private static void ReadOutputQuantityList(IEvent e, XElement x)
        {
            foreach (var xQuantity in x.Elements("quantityElement"))
            {
                EventProduct product = new EventProduct();
                product.Type = EventProductType.Output;
                product.EPC = new EPC(xQuantity.Element("epcClass")?.Value);

                double quantity = double.Parse(xQuantity.Element("quantity")?.Value);
                string uom = xQuantity.Element("uom")?.Value;
                product.Quantity = new Measurement(quantity, uom);

                e.AddProduct(product);
            }
        }

        private static void ReadSourceList(IEvent e, XElement x)
        {
            foreach (var xSource in x.Elements("source"))
            {
                EventSource source = new EventSource();
                source.RawType = xSource.Attribute("type")?.Value;
                source.Value = xSource.Value;
                e.SourceList.Add(source);
            }
        }

        private static void ReadDestinationList(IEvent e, XElement x)
        {
            foreach (var xDest in x.Elements("destination"))
            {
                EventDestination dest = new EventDestination();
                dest.RawType = xDest.Attribute("type")?.Value;
                dest.Value = xDest.Value;
                e.DestinationList.Add(dest);
            }
        }

        private static void ReadSensorElementList(IEvent e, XElement x)
        {
            throw new NotImplementedException();
        }

        private static void ReadPersistentDisposition(IEvent e, XElement x)
        {
            e.PersistentDisposition = new PersistentDisposition();
            foreach (var xSet in x.Elements("set"))
            {
                e.PersistentDisposition.Set.Add(xSet.Value);
            }
            foreach (var xUnset in x.Elements("unset"))
            {
                e.PersistentDisposition.Unset.Add(xUnset.Value);
            }
        }

        private static void ReadILMD(IEvent e, XElement x)
        {
            e.ILMD = new EventILMD();

            // the ILMD are just a series of extension KDEs
            foreach (XElement xKDE in x.Elements())
            {
                // read the kde...
                IEventKDE kde = ReadKDE(xKDE);
                e.ILMD.KDEs.Add(kde);
            }
        }

        private static void ReadParentID(IEvent e, XElement x)
        {
            EventProduct product = new EventProduct();
            product.EPC = new EPC(x.Value);
            product.Type = EventProductType.Parent;
            e.AddProduct(product);
        }

        private static IEventKDE ReadKDE(XElement x)
        {
            throw new NotImplementedException();
        }
    }
}
