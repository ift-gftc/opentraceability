using OpenTraceability.Interfaces;
using OpenTraceability.Models.Events;
using OpenTraceability.Models.Identifiers;
using OpenTraceability.Utility;
using OpenTraceability.Utility.Attributes;
using System;
using System.Reflection;
using System.Runtime.Intrinsics.X86;
using System.Xml.Linq;
using static System.Runtime.InteropServices.JavaScript.JSType;

namespace OpenTraceability.Mappers.EPCIS.XML
{
    internal class EPCISXmlEventWriter
    {
        private static List<EPCISXmlKDE> baseKDEs = new List<EPCISXmlKDE>()
        {
            new EPCISXmlKDE("eventTime", WriteEventTime),
            new EPCISXmlKDE("recordTime", WriteRecordTime),
            new EPCISXmlKDE("eventTimeZoneOffset", WriteEventTimeZoneOffset),
            new EPCISXmlKDE("eventID", WriteEventID, EPCISVersion.Version_2_0),
            new EPCISXmlKDE("errorDeclaration", WriteErrorDeclaration, EPCISVersion.Version_2_0),
            new EPCISXmlKDE("certificationInfo", WriteCertificationInfo, EPCISVersion.Version_2_0),
            new EPCISXmlKDE("baseExtension/eventID", WriteEventID, EPCISVersion.Version_1_2),
            new EPCISXmlKDE("baseExtension/errorDeclaration", WriteErrorDeclaration, EPCISVersion.Version_1_2),
        };

        private static List<EPCISXmlKDE> objectKDEs = new List<EPCISXmlKDE>()
        {
            new EPCISXmlKDE("epcList", WriteEPCList),
            new EPCISXmlKDE("action", WriteAction),
            new EPCISXmlKDE("bizStep", WriteBizStep),
            new EPCISXmlKDE("disposition", WriteDisposition),
            new EPCISXmlKDE("readPoint", WriteReadPoint),
            new EPCISXmlKDE("bizLocation", WriteBizLocation),
            new EPCISXmlKDE("bizTransactionList", WriteBizTransactionList),
            new EPCISXmlKDE("quantityList", WriteQuantityList, EPCISVersion.Version_2_0),
            new EPCISXmlKDE("sourceList", WriteSourceList, EPCISVersion.Version_2_0),
            new EPCISXmlKDE("destinationList", WriteDestinationList, EPCISVersion.Version_2_0),
            new EPCISXmlKDE("sensorElementList", WriteSensorElementList, EPCISVersion.Version_2_0),
            new EPCISXmlKDE("persistentDisposition", WritePersistentDisposition, EPCISVersion.Version_2_0),
            new EPCISXmlKDE("ilmd", WriteILMD, EPCISVersion.Version_2_0),
            new EPCISXmlKDE("extension/quantityList", WriteQuantityList, EPCISVersion.Version_1_2),
            new EPCISXmlKDE("extension/sourceList", WriteSourceList, EPCISVersion.Version_1_2),
            new EPCISXmlKDE("extension/destinationList", WriteDestinationList, EPCISVersion.Version_1_2),
            new EPCISXmlKDE("extension/ilmd", WriteILMD, EPCISVersion.Version_1_2)
        };

        private static List<EPCISXmlKDE> transformationKDEs = new List<EPCISXmlKDE>()
        {
            new EPCISXmlKDE("inputEPCList", WriteInputEPCList),
            new EPCISXmlKDE("inputQuantityList", WriteInputQuantityList),
            new EPCISXmlKDE("outputEPCList", WriteOutputEPCList),
            new EPCISXmlKDE("outputQuantityList", WriteOutputQuantityList),
            new EPCISXmlKDE("transformationID", WriteTransformationID),
            new EPCISXmlKDE("bizStep", WriteBizStep),
            new EPCISXmlKDE("disposition", WriteDisposition),
            new EPCISXmlKDE("readPoint", WriteReadPoint),
            new EPCISXmlKDE("bizLocation", WriteBizLocation),
            new EPCISXmlKDE("bizTransactionList", WriteBizTransactionList),
            new EPCISXmlKDE("sourceList", WriteSourceList, EPCISVersion.Version_2_0),
            new EPCISXmlKDE("destinationList", WriteDestinationList, EPCISVersion.Version_2_0),
            new EPCISXmlKDE("sensorElementList", WriteSensorElementList, EPCISVersion.Version_2_0),
            new EPCISXmlKDE("persistentDisposition", WritePersistentDisposition, EPCISVersion.Version_2_0),
            new EPCISXmlKDE("ilmd", WriteILMD, EPCISVersion.Version_2_0),
            new EPCISXmlKDE("extension/quantityList", WriteQuantityList, EPCISVersion.Version_1_2),
            new EPCISXmlKDE("extension/sourceList", WriteSourceList, EPCISVersion.Version_1_2),
            new EPCISXmlKDE("extension/destinationList", WriteDestinationList, EPCISVersion.Version_1_2),
            new EPCISXmlKDE("extension/ilmd", WriteILMD, EPCISVersion.Version_1_2)
        };

        private static List<EPCISXmlKDE> transactionKDEs = new List<EPCISXmlKDE>()
        {
            new EPCISXmlKDE("bizTransactionList", WriteBizTransactionList),
            new EPCISXmlKDE("parentID", WriteParentID),
            new EPCISXmlKDE("epcList", WriteEPCList),
            new EPCISXmlKDE("action", WriteAction),
            new EPCISXmlKDE("bizStep", WriteBizStep),
            new EPCISXmlKDE("disposition", WriteDisposition),
            new EPCISXmlKDE("readPoint", WriteReadPoint),
            new EPCISXmlKDE("bizLocation", WriteBizLocation),
            new EPCISXmlKDE("quantityList", WriteQuantityList, EPCISVersion.Version_2_0),
            new EPCISXmlKDE("sourceList", WriteSourceList, EPCISVersion.Version_2_0),
            new EPCISXmlKDE("destinationList", WriteDestinationList, EPCISVersion.Version_2_0),
            new EPCISXmlKDE("sensorElementList", WriteSensorElementList, EPCISVersion.Version_2_0),
            new EPCISXmlKDE("persistentDisposition", WritePersistentDisposition, EPCISVersion.Version_2_0),
            new EPCISXmlKDE("extension/quantityList", WriteQuantityList, EPCISVersion.Version_1_2),
            new EPCISXmlKDE("extension/sourceList", WriteSourceList, EPCISVersion.Version_1_2),
            new EPCISXmlKDE("extension/destinationList", WriteDestinationList, EPCISVersion.Version_1_2),
        };

        private static List<EPCISXmlKDE> aggregationKDEs = new List<EPCISXmlKDE>()
        {
            new EPCISXmlKDE("parentID", WriteParentID),
            new EPCISXmlKDE("childEPCs", WriteChildEPCList),
            new EPCISXmlKDE("action", WriteAction),
            new EPCISXmlKDE("bizStep", WriteBizStep),
            new EPCISXmlKDE("disposition", WriteDisposition),
            new EPCISXmlKDE("readPoint", WriteReadPoint),
            new EPCISXmlKDE("bizLocation", WriteBizLocation),
            new EPCISXmlKDE("bizTransactionList", WriteBizTransactionList),
            new EPCISXmlKDE("childQuantityList", WriteChildQuantityList, EPCISVersion.Version_2_0),
            new EPCISXmlKDE("sourceList", WriteSourceList, EPCISVersion.Version_2_0),
            new EPCISXmlKDE("destinationList", WriteDestinationList, EPCISVersion.Version_2_0),
            new EPCISXmlKDE("sensorElementList", WriteSensorElementList, EPCISVersion.Version_2_0),
            new EPCISXmlKDE("persistentDisposition", WritePersistentDisposition, EPCISVersion.Version_2_0),
            new EPCISXmlKDE("ilmd", WriteILMD, EPCISVersion.Version_2_0),
            new EPCISXmlKDE("extension/childQuantityList", WriteChildQuantityList, EPCISVersion.Version_1_2),
            new EPCISXmlKDE("extension/sourceList", WriteSourceList, EPCISVersion.Version_1_2),
            new EPCISXmlKDE("extension/destinationList", WriteDestinationList, EPCISVersion.Version_1_2)
        };

        private static List<EPCISXmlKDE> associationKDEs = new List<EPCISXmlKDE>()
        {
            new EPCISXmlKDE("parentID", WriteParentID),
            new EPCISXmlKDE("childEPCs", WriteChildEPCList),
            new EPCISXmlKDE("childQuantityList", WriteChildQuantityList),
            new EPCISXmlKDE("action", WriteAction),
            new EPCISXmlKDE("bizStep", WriteBizStep),
            new EPCISXmlKDE("disposition", WriteDisposition),
            new EPCISXmlKDE("readPoint", WriteReadPoint),
            new EPCISXmlKDE("bizLocation", WriteBizLocation),
            new EPCISXmlKDE("bizTransactionList", WriteBizTransactionList),
            new EPCISXmlKDE("sourceList", WriteSourceList),
            new EPCISXmlKDE("destinationList", WriteDestinationList),
            new EPCISXmlKDE("sensorElementList", WriteSensorElementList),
            new EPCISXmlKDE("persistentDisposition", WritePersistentDisposition)
        };

        public static XElement WriteEvent(IEvent e, EPCISVersion epcisVersion)
        {
            List<EPCISXmlKDE> kdes = new List<EPCISXmlKDE>(baseKDEs);

            XElement? xEvent = null;
            if (e is ObjectEvent)
            {
                xEvent = new XElement("ObjectEvent");
                kdes.AddRange(objectKDEs);
            }
            else if (e is AggregationEvent)
            {
                xEvent = new XElement("AggregationEvent");
                kdes.AddRange(aggregationKDEs);
            }
            else if (e is TransformationEvent)
            {
                xEvent = new XElement("TransformationEvent");
                kdes.AddRange(transformationKDEs);
            }
            //else if (xEvent.Name == "")
            //{
            //    e = new TransactionEvent();
            //    kdes.AddRange(transactionKDEs);
            //}
            //else if (xEvent.Name == "")
            //{
            //    e = new AggregationEvent();
            //    kdes.AddRange(aggregationKDEs);
            //}
            else if (e is AssociationEvent)
            {
                xEvent = new XElement("AssociationEvent");
                kdes.AddRange(associationKDEs);
            }
            else
            {
                throw new Exception("Unrecognized event type = " + e.GetType().FullName);
            }

            kdes = kdes.Where(k => k.RequiredVersion == null || k.RequiredVersion == epcisVersion).ToList();

            foreach (var kde in kdes)
            {
                kde.Action(kde.Name, e, xEvent);
            }

            foreach (var kde in e.KDEs)
            {
                XElement? xKDE = kde.GetXml();
                if (xKDE != null)
                {
                    xEvent.Add(xKDE);
                }
            }

            return xEvent;
        }

        private static void WriteCertificationInfo(string xName, IEvent e, XElement x)
        {
            if (!string.IsNullOrEmpty(e.CertificationInfo))
            {
                XElement xKDE = new XElement(xName, e.CertificationInfo);
                x.Add(xKDE);
            }
        }

        private static void WriteErrorDeclaration(string xName, IEvent e, XElement x)
        {
            //<errorDeclaration>
            //    <declarationTime>2022-02-08T19:41:23</declarationTime>
            //    <correctiveEventIDs>
            //        <correctiveEventID>0038bdc2-cad3-43eb-88f5-c93eda34ec43</correctiveEventID>
            //    </correctiveEventIDs>
            //    <extension />
            //</errorDeclaration>

            if (e.ErrorDeclaration != null)
            {
                XElement xe = new XElement(xName);
                xe.AddDateTimeOffsetISOElement("declarationTime", e.ErrorDeclaration.DeclarationTime);
                xe.AddStringElement("reason", e.ErrorDeclaration.RawReason?.ToString());
                if (e.ErrorDeclaration.CorrectingEventIDs != null && e.ErrorDeclaration.CorrectingEventIDs.Count > 0)
                {
                    XElement xIDs = new XElement("correctiveEventIDs");
                    foreach (var id in e.ErrorDeclaration.CorrectingEventIDs)
                    {
                        xIDs.AddStringElement("correctiveEventID", id);
                    }
                    xe.Add(xIDs);
                }

                // TODO: support extension kdes

                x.Add(xe);
            }
        }

        private static void WriteEventID(string xName, IEvent e, XElement x)
        {
            if (!string.IsNullOrWhiteSpace(e.EventID))
            {
                XElement xKDE = new XElement(xName, e.EventID);
                x.Add(xKDE);
            }
        }

        private static void WriteEventTimeZoneOffset(string xName, IEvent e, XElement x)
        {
            if (e.EventTimeOffset != null)
            {
                TimeSpan ts = TimeSpan.FromHours(Math.Abs(e.EventTimeOffset.Value));
                string offset = $"{ts.Hours.ToString().PadLeft(2, '0')}:{ts.Minutes.ToString().PadLeft(2, '0')}";
                if (e.EventTimeOffset.Value >= 0) offset = "+" + offset;
                else offset = "-" + offset;

                x.Add(new XElement(xName, offset));
            }
        }

        private static void WriteRecordTime(string xName, IEvent e, XElement x)
        {
            if (e.Recorded != null)
            {
                XElement xKDE = new XElement(xName, e.Recorded.Value.ToString("o"));
                x.Add(xKDE);
            }
        }

        private static void WriteEventTime(string xName, IEvent e, XElement x)
        {
            if (e.EventTime != null)
            {
                XElement xKDE = new XElement(xName, e.EventTime.Value.ToString("o"));
                x.Add(xKDE);
            }
        }

        private static void WriteILMD(string xName, IEvent e, XElement x)
        {
            if (e.ILMD != null)
            {
                XElement xILMD = new XElement("ilmd");
                foreach (IEventKDE kde in e.ILMD.KDEs)
                {
                    XElement? xKDE = kde.GetXml();
                    if (xKDE != null)
                    {
                        xILMD.Add(xKDE);
                    }
                }
                x.Add(xILMD);
            }
        }

        private static void WritePersistentDisposition(string xName, IEvent e, XElement x)
        {
            if (e.PersistentDisposition != null)
            {
                XElement xKDE = new XElement(xName);
                if (e.PersistentDisposition.Unset != null)
                {
                    foreach (string unset in e.PersistentDisposition.Unset)
                    {
                        XElement xUnset = new XElement("unset", unset);
                        xKDE.Add(xUnset);
                    }
                }
                if (e.PersistentDisposition.Set != null)
                {
                    foreach (string set in e.PersistentDisposition.Set)
                    {
                        XElement xSet = new XElement("set", set);
                        xKDE.Add(xSet);
                    }
                }
                x.Add(xKDE);
            }
        }

        private static void WriteSensorElementList(string xName, IEvent e, XElement x)
        {
            if (e.SensorElementList.Count > 0)
            {
                XElement xList = new XElement(xName);
                foreach (SensorElement se in e.SensorElementList)
                {
                    XElement xSE = new XElement("sensorElement");

                    if (se.MetaData != null)
                    {
                        XElement xMetadata = new XElement("sensorMetadata");

                        WriteXmlAttributes(xMetadata, se.MetaData);
                        WriteXmlExtensionAttributes(xMetadata, se.MetaData.ExtensionAttributes);

                        xSE.Add(xMetadata);
                    }

                    foreach (SensorReport report in se.Reports)
                    {
                        XElement xReport = new XElement("sensorReport");

                        WriteXmlAttributes(xReport, report);
                        WriteXmlExtensionAttributes(xReport, report.ExtensionAttributes);

                        xSE.Add(xReport);
                    }

                    WriteXmlExtensionKDEs(xSE, se.ExtensionKDEs);

                    xList.Add(xSE);
                }
                x.Add(xList);
            }
        }

        private static void WriteDestinationList(string xName, IEvent e, XElement x)
        {
            if (e.DestinationList.Count > 0)
            {
                XElement xDL = new XElement(xName);
                foreach (EventDestination d in e.DestinationList)
                {
                    XElement xD = new XElement("destination", new XAttribute("type", d.RawType), d.Value);
                    xDL.Add(xD);
                }
                x.Add(xDL);
            }
        }

        private static void WriteSourceList(string xName, IEvent e, XElement x)
        {
            if (e.SourceList.Count > 0)
            {
                XElement xSL = new XElement(xName);
                foreach (EventSource s in e.SourceList)
                {
                    XElement xS = new XElement("source", new XAttribute("type", s.RawType), s.Value);
                    xSL.Add(xS);
                }
                x.Add(xSL);
            }
        }

        private static void WriteQuantityList(string xName, IEvent e, XElement x)
        {
            List<EventProduct> products = e.Products.Where(p => p.Type == EventProductType.Reference && p.Quantity != null).ToList();
            if (products.Count > 0)
            {
                XElement xQuantityList = new XElement(xName);
                foreach (EventProduct product in products)
                {
                    if (product.EPC != null && product.Quantity != null)
                    {
                        XElement xQuantity = new XElement("quantityElement",
                            new XElement("epcClass", product.EPC.ToString()),
                            new XElement("quantity", product.Quantity.Value));

                        if (product.Quantity.UoM.UNCode != "EA")
                        {
                            xQuantity.Add(new XElement("uom", product.Quantity.UoM.UNCode));
                        }

                        xQuantityList.Add(xQuantity);
                    }
                }
                x.Add(xQuantityList);
            }
        }

        private static void WriteBizTransactionList(string xName, IEvent e, XElement x)
        {
            if (e.BusinessTransactions.Count > 0)
            {
                XElement xBTs = new XElement(xName);
                foreach (EventBusinessTransaction bt in e.BusinessTransactions)
                {
                    XElement xBT = new XElement("bizTransaction", new XAttribute("type", bt.RawType), bt.Value);
                    xBTs.Add(xBT);
                }
                x.Add(xBTs);
            }
        }

        private static void WriteBizLocation(string xName, IEvent e, XElement x)
        {
            if (!string.IsNullOrWhiteSpace(e.Location?.GLN?.ToString()))
            {
                x.Add(new XElement(xName, new XElement("id", e.Location.GLN.ToString())));
            }
        }

        private static void WriteReadPoint(string xName, IEvent e, XElement x)
        {
            if (!string.IsNullOrWhiteSpace(e.ReadPoint?.ID))
            {
                x.Add(new XElement(xName, new XElement("id", e.ReadPoint.ID)));
            }
        }

        private static void WriteDisposition(string xName, IEvent e, XElement x)
        {
            if (!string.IsNullOrWhiteSpace(e.Disposition))
            {
                x.Add(new XElement(xName, e.Disposition));
            }
        }

        private static void WriteBizStep(string xName, IEvent e, XElement x)
        {
            if (!string.IsNullOrWhiteSpace(e.BusinessStep))
            {
                x.Add(new XElement(xName, e.BusinessStep));
            }
        }

        private static void WriteAction(string xName, IEvent e, XElement x)
        {
            if (e.Action != null)
            {
                x.Add(new XElement(xName, e.Action.ToString()));
            }
        }

        private static void WriteEPCList(string xName, IEvent e, XElement x)
        {
            List<EventProduct> products = e.Products.Where(p => p.Type == EventProductType.Reference && p.Quantity == null).ToList();
            if (products.Count > 0)
            {
                XElement xEPCList = new XElement(xName);
                foreach (EventProduct prod in products)
                {
                    if (prod.EPC != null)
                    {
                        xEPCList.Add(new XElement("epc", prod.EPC.ToString()));
                    }
                }
                x.Add(xEPCList);
            }
        }

        private static void WriteTransformationID(string xName, IEvent e, XElement x)
        {
            TransformationEvent tEvent = (TransformationEvent)e;
            if (!string.IsNullOrWhiteSpace(tEvent.TransformationID))
            {
                x.Add(new XElement(xName, tEvent.TransformationID));
            }
        }

        private static void WriteOutputQuantityList(string xName, IEvent e, XElement x)
        {
            List<EventProduct> products = e.Products.Where(p => p.Type == EventProductType.Output && p.Quantity != null).ToList();
            if (products.Count > 0)
            {
                XElement xQuantityList = new XElement(xName);
                foreach (EventProduct product in products)
                {
                    if (product.EPC != null && product.Quantity != null)
                    {
                        XElement xQuantity = new XElement("quantityElement",
                            new XElement("epcClass", product.EPC.ToString()),
                            new XElement("quantity", product.Quantity.Value));

                        if (product.Quantity.UoM.UNCode != "EA")
                        {
                            xQuantity.Add(new XElement("uom", product.Quantity.UoM.UNCode));
                        }

                        xQuantityList.Add(xQuantity);
                    }
                }
                x.Add(xQuantityList);
            }
        }

        private static void WriteOutputEPCList(string xName, IEvent e, XElement x)
        {
            List<EventProduct> products = e.Products.Where(p => p.Type == EventProductType.Output && p.Quantity == null).ToList();
            if (products.Count > 0)
            {
                XElement xEPCList = new XElement(xName);
                foreach (EventProduct prod in products)
                {
                    if (prod.EPC != null)
                    {
                        xEPCList.Add(new XElement("epc", prod.EPC.ToString()));
                    }
                }
                x.Add(xEPCList);
            }
        }

        private static void WriteInputQuantityList(string xName, IEvent e, XElement x)
        {
            List<EventProduct> products = e.Products.Where(p => p.Type == EventProductType.Input && p.Quantity != null).ToList();
            if (products.Count > 0)
            {
                XElement xQuantityList = new XElement(xName);
                foreach (EventProduct product in products)
                {
                    if (product.EPC != null && product.Quantity != null)
                    {
                        XElement xQuantity = new XElement("quantityElement",
                            new XElement("epcClass", product.EPC.ToString()),
                            new XElement("quantity", product.Quantity.Value));

                        if (product.Quantity.UoM.UNCode != "EA")
                        {
                            xQuantity.Add(new XElement("uom", product.Quantity.UoM.UNCode));
                        }

                        xQuantityList.Add(xQuantity);
                    }
                }
                x.Add(xQuantityList);
            }
        }

        private static void WriteInputEPCList(string xName, IEvent e, XElement x)
        {
            List<EventProduct> products = e.Products.Where(p => p.Type == EventProductType.Input && p.Quantity == null).ToList();
            if (products.Count > 0)
            {
                XElement xEPCList = new XElement(xName);
                foreach (EventProduct prod in products)
                {
                    if (prod.EPC != null)
                    {
                        xEPCList.Add(new XElement("epc", prod.EPC.ToString()));
                    }
                }
                x.Add(xEPCList);
            }
        }

        private static void WriteParentID(string xName, IEvent e, XElement x)
        {
            EventProduct? parent = e.Products.FirstOrDefault(p => p.Type == EventProductType.Parent);
            if (parent?.EPC != null)
            {
                x.Add(new XElement(xName, parent.EPC.ToString()));
            }
        }

        private static void WriteChildQuantityList(string xName, IEvent e, XElement x)
        {
            List<EventProduct> products = e.Products.Where(p => p.Type == EventProductType.Child && p.Quantity != null).ToList();
            if (products.Count > 0)
            {
                XElement xQuantityList = new XElement(xName);
                foreach (EventProduct product in products)
                {
                    if (product.EPC != null && product.Quantity != null)
                    {
                        XElement xQuantity = new XElement("quantityElement",
                            new XElement("epcClass", product.EPC.ToString()),
                            new XElement("quantity", product.Quantity.Value));

                        if (product.Quantity.UoM.UNCode != "EA")
                        {
                            xQuantity.Add(new XElement("uom", product.Quantity.UoM.UNCode));
                        }

                        xQuantityList.Add(xQuantity);
                    }
                }
                x.Add(xQuantityList);
            }
        }

        private static void WriteChildEPCList(string xName, IEvent e, XElement x)
        {
            List<EventProduct> products = e.Products.Where(p => p.Type == EventProductType.Child && p.Quantity == null).ToList();
            if (products.Count > 0)
            {
                XElement xEPCList = new XElement(xName);
                foreach (EventProduct prod in products)
                {
                    if (prod.EPC != null)
                    {
                        xEPCList.Add(new XElement("epc", prod.EPC.ToString()));
                    }
                }
                x.Add(xEPCList);
            }
        }

        private static void WriteXmlAttributes(XElement x, object o)
        {
            foreach (PropertyInfo prop in o.GetType().GetProperties())
            {
                OpenTraceabilityXmlAttribute? att = prop.GetCustomAttribute<OpenTraceabilityXmlAttribute>();
                if (att != null)
                {
                    if (prop.PropertyType == typeof(DateTimeOffset?))
                    {
                        DateTimeOffset? dt = (DateTimeOffset?)prop.GetValue(o);
                        if (dt != null)
                        {
                            x.Add(new XAttribute(att.Name, dt.Value.ToString("o")));
                        }
                    }
                    else if (prop.PropertyType == typeof(UOM))
                    {
                        UOM? uom = (UOM?)prop.GetValue(o);
                        if (uom != null)
                        {
                            x.Add(new XAttribute(att.Name, uom.UNCode));
                        }
                    }
                    else if (prop.PropertyType == typeof(bool?))
                    {
                        bool? b = (bool?)prop.GetValue(o);
                        if (b != null)
                        {
                            x.Add(new XAttribute(att.Name, Convert.ToString(b)?.ToLower() ?? string.Empty));
                        }
                    }
                    else
                    {
                        string? str = prop.GetValue(o)?.ToString();
                        if (str != null)
                        {
                            x.Add(new XAttribute(att.Name, str));
                        }
                    }
                }
            }
        }

        private static void WriteXmlExtensionAttributes(XElement x, List<IEventKDE> kdes)
        {
            foreach (IEventKDE kde in kdes)
            {
                XElement? xKDE = kde.GetXml();
                if (xKDE != null)
                {
                    x.Add(new XAttribute(xKDE.Name, xKDE.Value));
                }
            }
        }

        private static void WriteXmlExtensionKDEs(XElement x, List<IEventKDE> kdes)
        {
            foreach (IEventKDE kde in kdes)
            {
                XElement? xKDE = kde.GetXml();
                if (xKDE != null)
                {
                    x.Add(xKDE);
                }
            }
        }
    }
}