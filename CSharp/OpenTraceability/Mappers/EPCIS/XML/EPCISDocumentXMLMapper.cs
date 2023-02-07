using OpenTraceability.Interfaces;
using OpenTraceability.Models.Events;
using System.Globalization;

namespace GS1.Mappers.EPCIS
{
    public class EPCISDocumentXMLMapper : IEPCISDocumentMapper
    {
        static EPCISXmlMapper_1_2()
        {
            IEventKDE.Register<VesselCatchInformationList>();
            IEventKDE.Register<CertificationList>();

            IEventKDE.Register<EventKDEDateTime>("cbvmda:harvestStartDate");
            IEventKDE.Register<EventKDEDateTime>("cbvmda:harvestEndDate");
            IEventKDE.Register<EventKDEDateTime>("cbvmda:transshipStartDate");
            IEventKDE.Register<EventKDEDateTime>("cbvmda:transshipEndDate");
            IEventKDE.Register<EventKDEDateTime>("cbvmda:landingStartDate");
            IEventKDE.Register<EventKDEDateTime>("cbvmda:landingEndDate");
            IEventKDE.Register<EventKDEDateTime>("cbvmda:unloadingPort");
            IEventKDE.Register<EventKDEDateTime>("cbvmda:productionDate");
            IEventKDE.Register<EventKDEDateTime>("cbvmda:itemExpirationDate");

            IEventKDE.Register<EventKDEString>("gdst:humanWelfarePolicy");
            IEventKDE.Register<EventKDEString>("gdst:productOwner");
            IEventKDE.Register<EventKDEString>("cbvmda:lotNumber");
            IEventKDE.Register<EventKDEString>("cbvmda:informationProvider");
            IEventKDE.Register<EventKDEString>("gdst:proteinSource");
            IEventKDE.Register<EventKDEString>("gdst:broodstockSource");
            IEventKDE.Register<EventKDEString>("cbvmda:unloadingPort");
            IEventKDE.Register<EventKDEString>("gdst:aquacultureMethod");
            IEventKDE.Register<EventKDEString>("cbvmda:productionMethodForFishAndSeafoodCode");

            IEventKDE.Register<EventKDECountry>("cbvmda:countryOfOrigin");
        }

        public EPCISDocument Map(string strValue)
        {
            // TODO: convert into XDocument

            // TODO: validate the schema depending on the version in the document

            // TODO: read all of the namespaces

            // TODO: read the creation date

            // TODO: read the standard business document header

            // TODO: read the master data

            // TODO: read the object events

            // TODO: read the transformation events

            // TODO: read the aggregation events

            // TODO: read the transaction events

            // TODO: read the association events

            throw new NotImplementedException();
        }

        public string Map(EPCISDocument doc)
        {
            throw new NotImplementedException();
        }

        private void ValidateSchema(DSXML xml)
        {
            try
            {
                if (!_schemaChecker.Validate(xml, "https://raw.githubusercontent.com/ift-gftc/doc.gdst/master/schemas/xml/epcis_1_2/EPCglobal-epcis-1_2.xsd", out string error))
                {
                    throw new MappingException($"Failed to validate the XML schema for the EPCIS XML.\n" + error);
                }
            }
            catch (Exception Ex)
            {
                DSLogger.Log(0, Ex);
                throw;
            }
        }

        private void ValidateQuerySchema(DSXML xml)
        {
            try
            {
                if (!_schemaChecker.Validate(xml, "https://raw.githubusercontent.com/ift-gftc/doc.gdst/master/schemas/xml/epcis_1_2/EPCglobal-epcis-query-1_2.xsd", out string error))
                {
                    throw new MappingException($"Failed to validate the XML schema for the EPCIS XML.\n" + error);
                }
            }
            catch (Exception Ex)
            {
                DSLogger.Log(0, Ex);
                throw;
            }
        }

        public string WriteEPCISData(IEPCISDocument data)
        {
            try
            {
                DSXML xml = new DSXML("epcis:EPCISDocument");
                xml.Attribute("xmlns:cbvmda", "urn:epcglobal:cbv:mda");
                xml.Attribute("xmlns:gdst", "https://traceability-dialogue.org/epcis");
                xml.Attribute("xmlns:sbdh", "http://www.unece.org/cefact/namespaces/StandardBusinessDocumentHeader");
                xml.Attribute("schemaVersion", "1.2");
                xml.Attribute("creationDate", DateTime.UtcNow.ToString("yyyy-MM-ddTHH:mm:ss.fffK"));

                // add in the header information
                WriteHeader(data.Header, xml);

                // if there are any master data..
                if (data.ProductDefinitions.Count > 0 || data.Locations.Count > 0 || data.TradingParties.Count > 0)
                {
                    if (xml["EPCISHeader"].IsNull) xml.AddChild("EPCISHeader");
                    DSXML xVocabList = xml["EPCISHeader"].AddChild("extension").AddChild("EPCISMasterData").AddChild("VocabularyList");
                    WriteProducts(data.ProductDefinitions, xVocabList);
                    WriteLocations(data.Locations, xVocabList);
                    WriteTradingParties(data.TradingParties, xVocabList);
                }

                // add in the events
                DSXML xEventList = xml.AddChild("EPCISBody").AddChild("EventList");
                foreach (var e in data.Events)
                {
                    DSXML xEvent = null;

                    if (e is IObjectEvent) xEvent = WriteObjectEvent(e as IObjectEvent);
                    if (e is ITransformationEvent) xEvent = WriteTransformationEvent(e as ITransformationEvent);
                    if (e is ITransactionEvent) xEvent = WriteTransactionEvent(e as ITransactionEvent);
                    if (e is IAggregationEvent) xEvent = WriteAggregationEvent(e as IAggregationEvent);
                    if (e is IAssociationEvent) xEvent = WriteAssociationEvent(e as IAssociationEvent);

                    if (xEvent != null)
                    {
                        xEventList.AddChild(xEvent);
                    }
                }

                // validate the schema
                ValidateSchema(xml);

                return xml.PrintXmlString;
            }
            catch (Exception Ex)
            {
                DSLogger.Log(0, Ex);
                throw;
            }
        }

        public string WriteEPCISQueryData(IEPCISQueryDocument data)
        {
            try
            {
                DSXML xml = new DSXML("epcisq:EPCISQueryDocument");
                xml.Attribute("xmlns:cbvmda", "urn:epcglobal:cbv:mda");
                xml.Attribute("xmlns:gdst", "https://traceability-dialogue.org/epcis");
                xml.Attribute("xmlns:sbdh", "http://www.unece.org/cefact/namespaces/StandardBusinessDocumentHeader");
                xml.Attribute("xmlns:epcis", "urn:epcglobal:epcis:xsd:1");
                xml.Attribute("xmlns:epcisq", "urn:epcglobal:epcis-query:xsd:1");
                xml.Attribute("schemaVersion", "1.2");
                xml.Attribute("creationDate", DateTime.UtcNow.ToString("yyyy-MM-ddTHH:mm:ss.fffK"));

                // add in the header information
                WriteHeader(data.Header, xml);

                // if there are any master data..
                if (data.ProductDefinitions.Count > 0 || data.Locations.Count > 0 || data.TradingParties.Count > 0)
                {
                    if (xml["EPCISHeader"].IsNull) xml.AddChild("EPCISHeader");
                    DSXML xVocabList = xml["EPCISHeader"].AddChild("extension").AddChild("EPCISMasterData").AddChild("VocabularyList");
                    WriteProducts(data.ProductDefinitions, xVocabList);
                    WriteLocations(data.Locations, xVocabList);
                    WriteTradingParties(data.TradingParties, xVocabList);
                }

                // add in the events
                var xQueryResults = xml.AddChild("EPCISBody").AddChild("epcisq:QueryResults");
                xQueryResults.AddChild("queryName", data.QueryName);
                var xEventList = xQueryResults.AddChild("resultsBody").AddChild("EventList");
                foreach (var e in data.Events)
                {
                    DSXML xEvent = null;

                    if (e is IObjectEvent) xEvent = WriteObjectEvent(e as IObjectEvent);
                    if (e is ITransformationEvent) xEvent = WriteTransformationEvent(e as ITransformationEvent);
                    if (e is ITransactionEvent) xEvent = WriteTransactionEvent(e as ITransactionEvent);
                    if (e is IAggregationEvent) xEvent = WriteAggregationEvent(e as IAggregationEvent);
                    if (e is IAssociationEvent) xEvent = WriteAssociationEvent(e as IAssociationEvent);

                    if (xEvent != null)
                    {
                        xEventList.AddChild(xEvent);
                    }
                }

                // validate the schema
                ValidateQuerySchema(xml);

                return xml.PrintXmlString;
            }
            catch (Exception Ex)
            {
                DSLogger.Log(0, Ex);
                throw;
            }
        }

        private void WriteHeader(IStandardBusinessDocumentHeader header, DSXML xml)
        {
            // add in the header information
            if (header != null)
            {
                DSXML xHeader = xml.AddChild("EPCISHeader").AddChild("sbdh:StandardBusinessDocumentHeader");

                if (!string.IsNullOrWhiteSpace(header.HeaderVersion))
                {
                    xHeader.AddChild("sbdh:HeaderVersion", header.HeaderVersion);
                }

                if (header.Sender != null)
                {
                    DSXML xSender = xHeader.AddChild("sbdh:Sender");

                    if (!string.IsNullOrWhiteSpace(header.Sender.Identifier))
                    {
                        xSender.AddChild("sbdh:Identifier", header.Sender.Identifier);
                    }

                    if (!string.IsNullOrWhiteSpace(header.Sender.EmailAddress) || !string.IsNullOrWhiteSpace(header.Sender.ContactName))
                    {
                        DSXML xContact = xSender.AddChild("sbdh:ContactInformation");
                        if (!string.IsNullOrWhiteSpace(header.Sender.ContactName))
                        {
                            xContact.AddChild("sbdh:Contact", header.Sender.ContactName);
                        }
                        if (!string.IsNullOrWhiteSpace(header.Sender.EmailAddress))
                        {
                            xContact.AddChild("sbdh:EmailAddress", header.Sender.EmailAddress);
                        }
                    }
                }

                if (header.Receiver != null)
                {
                    DSXML xReceiver = xHeader.AddChild("sbdh:Receiver");

                    if (!string.IsNullOrWhiteSpace(header.Receiver.Identifier))
                    {
                        xReceiver.AddChild("sbdh:Identifier", header.Receiver.Identifier);
                    }

                    if (!string.IsNullOrWhiteSpace(header.Receiver.EmailAddress) || !string.IsNullOrWhiteSpace(header.Receiver.ContactName))
                    {
                        DSXML xContact = xReceiver.AddChild("sbdh:ContactInformation");
                        if (!string.IsNullOrWhiteSpace(header.Receiver.ContactName))
                        {
                            xContact.AddChild("sbdh:Contact", header.Receiver.ContactName);
                        }
                        if (!string.IsNullOrWhiteSpace(header.Receiver.EmailAddress))
                        {
                            xContact.AddChild("sbdh:EmailAddress", header.Receiver.EmailAddress);
                        }
                    }
                }

                if (header.DocumentIdentification != null)
                {
                    DSXML xDocIdentification = xHeader.AddChild("sbdh:DocumentIdentification");

                    if (!string.IsNullOrWhiteSpace(header.DocumentIdentification.Standard))
                    {
                        xDocIdentification.AddChild("sbdh:Standard", header.DocumentIdentification.Standard);
                    }

                    if (!string.IsNullOrWhiteSpace(header.DocumentIdentification.TypeVersion))
                    {
                        xDocIdentification.AddChild("sbdh:TypeVersion", header.DocumentIdentification.TypeVersion);
                    }

                    if (!string.IsNullOrWhiteSpace(header.DocumentIdentification.InstanceIdentifier))
                    {
                        xDocIdentification.AddChild("sbdh:InstanceIdentifier", header.DocumentIdentification.InstanceIdentifier);
                    }

                    if (!string.IsNullOrWhiteSpace(header.DocumentIdentification.Type))
                    {
                        xDocIdentification.AddChild("sbdh:Type", header.DocumentIdentification.Type);
                    }

                    if (!string.IsNullOrWhiteSpace(header.DocumentIdentification.MultipleType))
                    {
                        xDocIdentification.AddChild("sbdh:MultipleType", header.DocumentIdentification.MultipleType);
                    }

                    if (header.DocumentIdentification.CreationDateAndTime != null)
                    {
                        xDocIdentification.AddChild("sbdh:CreationDateAndTime", header.DocumentIdentification.CreationDateAndTime?.ToString("yyyy-MM-ddTHH:mm:ss.fffK"));
                    }
                }
            }
        }

        private void WriteProducts(List<IProduct> products, DSXML xVocabList)
        {
            if (products.Count > 0)
            {
                DSXML xVocab = xVocabList.AddChild("Vocabulary");
                xVocab.Attribute("type", "urn:epcglobal:epcis:vtype:EPCClass");

                DSXML xEleList = xVocab.AddChild("VocabularyElementList");

                foreach (var p in products)
                {
                    DSXML xVocabEle = xEleList.AddChild("VocabularyElement");
                    xVocabEle.Attribute("id", p.GTIN.ToString());
                    xVocabEle.AddChild("attribute", p.Name).Attribute("id", "urn:epcglobal:cbv:mda#descriptionShort");
                    xVocabEle.AddChild("attribute", p.Seafood?.ProductForm?.Key).Attribute("id", "urn:epcglobal:cbv:mda#tradeItemConditionCode");
                    if (p.Owner != null) xVocabEle.AddChild("attribute", p.Owner).Attribute("id", "urn:epcglobal:cbv:owning_party");
                    if (p.InformationProvider != null) xVocabEle.AddChild("attribute", p.InformationProvider).Attribute("id", "urn:epcglobal:cbv:mda#informationProvider");

                    if (p.Species?.FirstOrDefault() != null)
                    {
                        xVocabEle.AddChild("attribute", p.Species.FirstOrDefault()?.ScientificName).Attribute("id", "urn:epcglobal:cbv:mda#speciesForFisheryStatisticsPurposesName");
                        xVocabEle.AddChild("attribute", p.Species.FirstOrDefault()?.Alpha3Code).Attribute("id", "urn:epcglobal:cbv:mda#speciesForFisheryStatisticsPurposesCode");
                    }
                }
            }
        }

        private void WriteLocations(List<ILocation> locations, DSXML xVocabList)
        {
            if (locations.Count > 0)
            {
                DSXML xVocab = xVocabList.AddChild("Vocabulary");
                xVocab.Attribute("type", "urn:epcglobal:epcis:vtype:Location");

                DSXML xEleList = xVocab.AddChild("VocabularyElementList");

                foreach (var l in locations)
                {
                    DSXML xVocabEle = xEleList.AddChild("VocabularyElement");
                    xVocabEle.Attribute("id", l.GLN.ToString());
                    xVocabEle.AddChild("attribute", l.Name).Attribute("id", "urn:epcglobal:cbv:mda#name");
                    if (l.Address != null)
                    {
                        if (l.Owner != null) xVocabEle.AddChild("attribute", l.Owner).Attribute("id", "urn:epcglobal:cbv:owning_party");
                        if (l.InformationProvider != null) xVocabEle.AddChild("attribute", l.InformationProvider).Attribute("id", "urn:epcglobal:cbv:mda#informationProvider");
                        if (!string.IsNullOrWhiteSpace(l.Address.Address1)) xVocabEle.AddChild("attribute", l.Address.Address1).Attribute("id", "urn:epcglobal:cbv:mda#streetAddressOne");
                        if (!string.IsNullOrWhiteSpace(l.Address.Address2)) xVocabEle.AddChild("attribute", l.Address.Address2).Attribute("id", "urn:epcglobal:cbv:mda#streetAddressTwo");
                        if (!string.IsNullOrWhiteSpace(l.Address.City)) xVocabEle.AddChild("attribute", l.Address.City).Attribute("id", "urn:epcglobal:cbv:mda#city");
                        if (!string.IsNullOrWhiteSpace(l.Address.State)) xVocabEle.AddChild("attribute", l.Address.State).Attribute("id", "urn:epcglobal:cbv:mda#state");
                        if (!string.IsNullOrWhiteSpace(l.Address.ZipCode)) xVocabEle.AddChild("attribute", l.Address.ZipCode).Attribute("id", "urn:epcglobal:cbv:mda#postalCode");
                        if (l.Address.Country != null)
                        {
                            xVocabEle.AddChild("attribute", l.Address.Country.Abbreviation).Attribute("id", "urn:epcglobal:cbv:mda#countryCode");
                        }
                    }
                    if (l.Vessel != null)
                    {
                        if (!string.IsNullOrWhiteSpace(l.Vessel.VesselName)) xVocabEle.AddChild("attribute", l.Vessel.VesselName).Attribute("id", "urn:epcglobal:cbv:mda#vesselName");
                        if (!string.IsNullOrWhiteSpace(l.Vessel.VesselID)) xVocabEle.AddChild("attribute", l.Vessel.VesselID).Attribute("id", "urn:epcglobal:cbv:mda#vesselID");
                        if (!string.IsNullOrWhiteSpace(l.Vessel.IMONumber)) xVocabEle.AddChild("attribute", l.Vessel.IMONumber).Attribute("id", "urn:gdst:kde#imoNumber");
                        if (l.Vessel.VesselFlag != null) xVocabEle.AddChild("attribute", l.Vessel.VesselFlag.Abbreviation).Attribute("id", "urn:epcglobal:cbv:mda#vesselFlagState");
                    }
                }
            }
        }

        private void WriteTradingParties(List<ITradingParty> parties, DSXML xVocabList)
        {
            if (parties.Count > 0)
            {
                DSXML xVocab = xVocabList.AddChild("Vocabulary");
                xVocab.Attribute("type", "urn:epcglobal:epcis:vtype:Party");

                DSXML xEleList = xVocab.AddChild("VocabularyElementList");

                foreach (var tp in parties)
                {
                    DSXML xVocabEle = xEleList.AddChild("VocabularyElement");
                    xVocabEle.Attribute("id", tp.PGLN.ToString());
                    xVocabEle.AddChild("attribute", tp.Name).Attribute("id", "urn:epcglobal:cbv:mda#name");
                    if (tp.Owner != null) xVocabEle.AddChild("attribute", tp.Owner).Attribute("id", "urn:epcglobal:cbv:owning_party");
                    if (tp.InformationProvider != null) xVocabEle.AddChild("attribute", tp.InformationProvider).Attribute("id", "urn:epcglobal:cbv:mda#informationProvider");
                }
            }
        }

        private DSXML WriteObjectEvent(IObjectEvent e)
        {
            DSXML xObject = new DSXML("ObjectEvent");

            // write the base kdes on the object event
            WriteEPCISBase_Begginning(e, xObject);

            // epcList
            xObject.AddChild("epcList");
            foreach (var p in e.Products.Where(p => p.EPC.Type != EPCType.Class))
            {
                xObject["epcList"].AddChild("epc", p.EPC.ToString());
            }

            // action, bizStep, disposition, readPoint, bizLocation, bizTransactionList
            WriteEPCISBase_Middle(e, xObject);

            // extension
            xObject.AddChild("extension");

            // extension.quantityList
            if (e.Products.Where(p => p.EPC.Type == EPCType.Class).Count() > 0)
            {
                xObject["extension"].AddChild("quantityList");
                foreach (var p in e.Products.Where(p => p.EPC.Type == EPCType.Class))
                {
                    DSXML xQuantity = new DSXML("quantityElement");
                    xQuantity.AddChild("epcClass", p.EPC.ToString());
                    xQuantity.AddChild("quantity", p.Quantity.Value);
                    if (p.Quantity.UoM != null && p.Quantity.UoM?.UNCode != "EA")
                    {
                        xQuantity.AddChild("uom", p.Quantity.UoM.UNCode);
                    }
                    xObject["extension/quantityList"].AddChild(xQuantity);
                }
            }

            // sourceList, destinationList
            WriteSourceDestinationList(e, xObject["extension"]);

            // extension.ilmd
            if (e.ILMD != null)
            {
                var xILMD = xObject["extension"].AddChild("ilmd");
                foreach (var kde in e.ILMD.KDEs)
                {
                    xILMD.AddChild(kde.XmlValue);
                }
            }

            // common data elements
            WriteKDEs(e, xObject);

            return xObject;
        }

        private DSXML WriteTransactionEvent(ITransactionEvent e)
        {
            DSXML xTransaction = new DSXML("TransactionEvent");

            // write the base kdes on the object event
            WriteEPCISBase_Begginning(e, xTransaction);

            // parentID
            if (!IEPC.IsNullOrEmpty(e.ParentID))
            {
                xTransaction.AddChild("parentID", e.ParentID);
            }

            // epcList
            xTransaction.AddChild("epcList");
            foreach (var p in e.Products.Where(p => p.EPC.Type == EPCType.Instance || p.EPC.Type == EPCType.SSCC))
            {
                xTransaction["epcList"].AddChild("epc", p.EPC.ToString());
            }

            // action, bizStep, disposition, readPoint, bizLocation, bizTransactionList
            WriteEPCISBase_Middle(e, xTransaction);

            // extension
            xTransaction.AddChild("extension");

            // extension.quantityList
            if (e.Products.Where(p => p.EPC.Type == EPCType.Class).Count() > 0)
            {
                xTransaction["extension"].AddChild("quantityList");
                foreach (var p in e.Products.Where(p => p.EPC.Type == EPCType.Class))
                {
                    DSXML xQuantity = new DSXML("quantityElement");
                    xQuantity.AddChild("epcClass", p.EPC.ToString());
                    xQuantity.AddChild("quantity", p.Quantity.Value);
                    if (p.Quantity.UoM != null && p.Quantity.UoM?.UNCode != "EA")
                    {
                        xQuantity.AddChild("uom", p.Quantity.UoM.UNCode);
                    }
                    xTransaction["extension/quantityList"].AddChild(xQuantity);
                }
            }

            // sourceList, destinationList
            WriteSourceDestinationList(e, xTransaction["extension"]);

            // extension.ilmd
            if (e.ILMD != null)
            {
                var xILMD = xTransaction["extension"].AddChild("ilmd");
                foreach (var kde in e.ILMD.KDEs)
                {
                    xILMD.AddChild(kde.XmlValue);
                }
            }

            // common data elements
            WriteKDEs(e, xTransaction);

            return xTransaction;
        }

        private DSXML WriteTransformationEvent(ITransformationEvent e)
        {
            DSXML xExtension = new DSXML("extension");
            DSXML xTransform = xExtension.AddChild("TransformationEvent");

            // eventID, recordTime, eventTimeZoneOffset, eventID, errorDeclaration
            WriteEPCISBase_Begginning(e, xTransform);

            // inputEPCList
            if (e.Products.Exists(p => p.Type == EventProductType.Input && p.EPC.Type != EPCType.Class))
            {
                DSXML xInputs = xTransform.AddChild("inputEPCList");
                foreach (var p in e.Products.Where(p => p.Type == EventProductType.Input && p.EPC.Type != EPCType.Class))
                {
                    xInputs.AddChild("epc", p.EPC?.ToString());
                }
            }

            // inputQuantityList
            if (e.Products.Exists(p => p.EPC.Type == EPCType.Class && p.Type == EventProductType.Input))
            {
                DSXML xInputQuantities = xTransform.AddChild("inputQuantityList");
                foreach (var p in e.Products.Where(p => p.EPC.Type == EPCType.Class && p.Type == EventProductType.Input))
                {
                    DSXML xQuantity = new DSXML("quantityElement");
                    xQuantity.AddChild("epcClass", p.EPC.ToString());
                    xQuantity.AddChild("quantity", p.Quantity.Value);
                    if (p.Quantity.UoM != null && p.Quantity.UoM?.UNCode != "EA")
                    {
                        xQuantity.AddChild("uom", p.Quantity.UoM.UNCode);
                    }
                    xInputQuantities.AddChild(xQuantity);
                }
            }

            // outputEPCList
            if (e.Products.Exists(p => p.Type == EventProductType.Output && p.EPC.Type != EPCType.Class))
            {
                DSXML xOutputs = xTransform.AddChild("outputEPCList");
                foreach (var p in e.Products.Where(p => p.Type == EventProductType.Output && p.EPC.Type != EPCType.Class))
                {
                    xOutputs.AddChild("epc", p.EPC?.ToString());
                }
            }

            // outputQuantityList
            if (e.Products.Exists(p => p.EPC.Type == EPCType.Class && p.Type == EventProductType.Output))
            {
                DSXML xOutputQuantities = xTransform.AddChild("outputQuantityList");
                foreach (var p in e.Products.Where(p => p.EPC.Type == EPCType.Class && p.Type == EventProductType.Output))
                {
                    DSXML xQuantity = new DSXML("quantityElement");
                    xQuantity.AddChild("epcClass", p.EPC.ToString());
                    xQuantity.AddChild("quantity", p.Quantity.Value);
                    if (p.Quantity.UoM != null && p.Quantity.UoM?.UNCode != "EA")
                    {
                        xQuantity.AddChild("uom", p.Quantity.UoM.UNCode);
                    }
                    xOutputQuantities.AddChild(xQuantity);
                }
            }

            // transformationID
            if (!string.IsNullOrWhiteSpace(e.TransformationID))
            {
                xTransform.AddChild("transformationID", e.TransformationID);
            }

            // bizStep, disposition, readPoint, bizLocation, bizTransactionList
            WriteEPCISBase_Middle(e, xTransform);

            // sourceList, destinationList
            WriteSourceDestinationList(e, xTransform);

            // ilmd
            if (e.ILMD != null)
            {
                var xILMD = xTransform.AddChild("ilmd");
                foreach (var kde in e.ILMD.KDEs)
                {
                    xILMD.AddChild(kde.XmlValue);
                }
            }

            // kdes
            WriteKDEs(e, xTransform);

            return xExtension;
        }

        private DSXML WriteAssociationEvent(IAssociationEvent e)
        {
            throw new Exception("EPCIS 1.2 does not support the association type event.");
        }

        private DSXML WriteAggregationEvent(IAggregationEvent e)
        {
            DSXML xAgg = new DSXML("AggregationEvent");

            // eventID, recordTime, eventTimeZoneOffset, eventID, errorDeclaration
            WriteEPCISBase_Begginning(e, xAgg);

            // parentID
            if (!IEPC.IsNullOrEmpty(e.ParentID))
            {
                xAgg.AddChild("parentID", e.ParentID);
            }

            // childEPCs
            xAgg.AddChild("childEPCs");
            foreach (var p in e.Products.Where(p => p.Type == EventProductType.Child && p.EPC.Type != EPCType.Class))
            {
                xAgg["childEPCs"].AddChild("epc", p.EPC.ToString());
            }

            // action, bizStep, disposition, readPoint, bizLocation, bizTransactionList
            WriteEPCISBase_Middle(e, xAgg);

            // extension
            xAgg.AddChild("extension");

            // extension.childQuantityList
            if (e.Products.Exists(p => p.Type == EventProductType.Child && p.EPC.Type == EPCType.Class))
            {
                xAgg["extension"].AddChild("childQuantityList");
                foreach (var p in e.Products.Where(p => p.Type == EventProductType.Child && p.EPC.Type == EPCType.Class))
                {
                    DSXML xQuantity = new DSXML("quantityElement");
                    xQuantity.AddChild("epcClass", p.EPC.ToString());
                    xQuantity.AddChild("quantity", p.Quantity.Value);
                    if (p.Quantity.UoM != null && p.Quantity.UoM?.UNCode != "EA")
                    {
                        xQuantity.AddChild("uom", p.Quantity.UoM.UNCode);
                    }
                    xAgg["extension/childQuantityList"].AddChild(xQuantity);
                }
            }

            // sourceList, destinationList
            WriteSourceDestinationList(e, xAgg["extension"]);

            // ilmd
            if (e.ILMD != null)
            {
                throw new Exception("The aggregate event does not support ILMD in EPCIS 1.2");
            }

            // common data elements
            WriteKDEs(e, xAgg);

            return xAgg;
        }

        /// <summary>
        /// Writes the eventID, recordTime, eventTimeZoneOffset, eventID, and error declaration
        /// </summary>
        /// <param name="e"></param>
        /// <param name="xEvent"></param>
        private void WriteEPCISBase_Begginning(IEvent e, DSXML xEvent)
        {
            // eventTime
            xEvent.AddChild("eventTime", e.EventTime.ToUniversalTime().ToString("yyyy-MM-ddTHH:mm:ss.fffZ"));

            // recordTime
            if (e.Recorded != null)
            {
                xEvent.AddChild("recordTime", e.Recorded?.ToUniversalTime().ToString("yyyy-MM-ddTHH:mm:ss.fffZ"));
            }

            // eventTimeZoneOffset
            xEvent.AddChild("eventTimeZoneOffset", ConvertToTimeZoneOffset(e.EventTimeOffset));

            // baseExtension (eventID, errorDeclaration)
            if (!string.IsNullOrWhiteSpace(e.EventID))
            {
                if (xEvent["baseExtension"].IsNull)
                {
                    xEvent.AddChild("baseExtension");
                }
                xEvent["baseExtension"].AddChild("eventID", e.EventID);
            }

            if (e.ErrorDeclaration != null)
            {
                //<errorDeclaration>
                //    <declarationTime>2022-02-08T19:41:23</declarationTime>
                //    <correctiveEventIDs>
                //        <correctiveEventID>0038bdc2-cad3-43eb-88f5-c93eda34ec43</correctiveEventID>
                //    </correctiveEventIDs>
                //    <extension />
                //</errorDeclaration>

                if (xEvent["baseExtension"].IsNull)
                {
                    xEvent.AddChild("baseExtension");
                }

                DSXML xErrDec = xEvent["baseExtension"].AddChild("errorDeclaration");
                xErrDec.AddChild("declarationTime", e.ErrorDeclaration.DeclarationTime?.ToUniversalTime().ToString("yyyy-MM-ddTHH:mm:ssZ"));
                if (e.ErrorDeclaration.CorrectingEventIDs != null && e.ErrorDeclaration.CorrectingEventIDs.Count > 0)
                {
                    DSXML xIDs = xErrDec.AddChild("correctiveEventIDs");
                    foreach (var id in e.ErrorDeclaration.CorrectingEventIDs)
                    {
                        xIDs.AddChild("correctiveEventID", id);
                    }
                }
            }
        }

        /// <summary>
        /// Writes the action, bizStep, disposition, readPoint, bizLocation, and bizTransactions
        /// </summary>
        /// <param name="e"></param>
        /// <param name="xEvent"></param>
        private void WriteEPCISBase_Middle(IEvent e, DSXML xEvent)
        {
            // action
            if (!(e is ITransformationEvent))
            {
                xEvent.AddChild("action", e.Action.ToString());
            }

            // bizStep
            if (!string.IsNullOrWhiteSpace(e.BusinessStep))
            {
                xEvent.AddChild("bizStep", e.BusinessStep);
            }

            // disposition
            if (!string.IsNullOrWhiteSpace(e.Disposition))
            {
                xEvent.AddChild("disposition", e.Disposition);
            }

            // read point
            if (!string.IsNullOrWhiteSpace(e.ReadPoint?.ID))
            {
                xEvent.AddChild("readPoint").AddChild("id").Value = e.ReadPoint.ID;
            }

            // bizLocation
            if (e.Location?.GLN != null)
            {
                xEvent.AddChild("bizLocation");
                xEvent["bizLocation"].AddChild("id", e.Location.GLN.ToString());
            }

            // bizTransactionList
            if (e.BusinessTransactions != null && e.BusinessTransactions.Count > 0)
            {
                var xBizTransactionList = xEvent.AddChild("bizTransactionList");
                foreach (var bt in e.BusinessTransactions)
                {
                    var xBizTransaction = xBizTransactionList.AddChild("bizTransaction");
                    xBizTransaction.Attribute("type", bt.RawType);
                    xBizTransaction.Value = bt.Value;
                }
            }
        }

        /// <summary>
        /// Writes the sourceList and destinationList
        /// </summary>
        /// <param name="e"></param>
        /// <param name="xEvent"></param>
        private void WriteSourceDestinationList(IEvent e, DSXML xml)
        {
            // source list
            if (e.SourceList != null && e.SourceList.Count > 0)
            {
                DSXML xSources = xml.AddChild("sourceList");
                foreach (var sl in e.SourceList)
                {
                    DSXML xSource = xSources.AddChild("source");
                    xSource.Attribute("type", sl.RawType);
                    xSource.Value = sl.Value;
                }
            }

            // destination list
            if (e.SourceList != null && e.DestinationList.Count > 0)
            {
                DSXML xDestinations = xml.AddChild("destinationList");
                foreach (var sl in e.DestinationList)
                {
                    DSXML xDest = xDestinations.AddChild("destination");
                    xDest.Attribute("type", sl.RawType);
                    xDest.Value = sl.Value;
                }
            }
        }

        private void WriteKDEs(IEvent e, DSXML xEvent)
        {
            try
            {
                // kdes
                foreach (var kde in e.KDEs)
                {
                    xEvent.AddChild(kde.XmlValue);
                }

                // extension kdes
                foreach (var kde in e.ExtensionKDEs)
                {
                    if (xEvent["extension"].IsNull) xEvent.AddChild("extension");
                    xEvent["extension"].AddChild(kde.XmlValue);
                }
            }
            catch (Exception Ex)
            {
                DSLogger.Log(0, Ex);
                throw;
            }
        }

        #region Convert from XML to C# Models

        public IEPCISDocument ReadEPCISData(string value)
        {
            try
            {
                IEPCISDocument data = new EPCISDocument();

                // validate the XML against the schema
                DSXML xml = DSXML.CreateFromString(value);
                ValidateSchema(xml);

                // read the header
                DSXML xHeader = xml["EPCISHeader/sbdh:StandardBusinessDocumentHeader"];
                data.Header = ReadHeader(xHeader);

                // read the master data
                DSXML xEPCISMasterData = xml["EPCISHeader/extension/EPCISMasterData"];
                if (!xEPCISMasterData.IsNull)
                {
                    ReadProductDefinitions(xEPCISMasterData, data.ProductDefinitions);
                    ReadLocations(xEPCISMasterData, data.Locations);
                    ReadTradingParties(xEPCISMasterData, data.TradingParties);
                }

                // foreach object event...
                foreach (var xObject in xml.Elements("//EPCISBody/EventList/ObjectEvent"))
                {
                    IEvent e = ReadObjectEvent(xObject);
                    data.Events.Add(e);
                }

                // foreach transformation event...
                foreach (var xTransform in xml.Elements("//EPCISBody/EventList/extension/TransformationEvent"))
                {
                    IEvent e = ReadTransformationEvent(xTransform);
                    data.Events.Add(e);
                }

                // foreach aggregate event...
                foreach (var xAgg in xml.Elements("//EPCISBody/EventList/AggregationEvent"))
                {
                    IEvent e = ReadAggregationEvent(xAgg);
                    data.Events.Add(e);
                }

                // foreach transaction event...
                foreach (var xTransaction in xml.Elements("//EPCISBody/EventList/TransactionEvent"))
                {
                    IEvent e = ReadTransactionEvent(xTransaction);
                    data.Events.Add(e);
                }

                // foreach association event...
                foreach (var xAssociation in xml.Elements("//EPCISBody/EventList/AssociationEvent"))
                {
                    IEvent e = ReadAssociationEvent(xAssociation);
                    data.Events.Add(e);
                }

                return data;
            }
            catch (Exception Ex)
            {
                DSLogger.Log(0, Ex);
                throw;
            }
        }

        public IEPCISQueryDocument ReadEPCISQueryData(string value)
        {
            try
            {
                IEPCISQueryDocument data = new EPCISQueryDocument();

                // validate the XML against the schema
                DSXML xml = DSXML.CreateFromString(value);
                ValidateQuerySchema(xml);

                // read the header
                DSXML xHeader = xml["EPCISHeader/sbdh:StandardBusinessDocumentHeader"];
                data.Header = ReadHeader(xHeader);

                // read the master data
                var xEPCISMasterData = xml["EPCISHeader/extension/EPCISMasterData"];
                if (!xEPCISMasterData.IsNull)
                {
                    ReadProductDefinitions(xEPCISMasterData, data.ProductDefinitions);
                    ReadLocations(xEPCISMasterData, data.Locations);
                    ReadTradingParties(xEPCISMasterData, data.TradingParties);
                }

                // foreach object event...
                foreach (var xObject in xml.Elements("//EPCISBody/epcisq:QueryResults/resultsBody/EventList/ObjectEvent"))
                {
                    IEvent e = ReadObjectEvent(xObject);
                    data.Events.Add(e);
                }

                // foreach transformation event...
                foreach (var xTransform in xml.Elements("//EPCISBody/epcisq:QueryResults/resultsBody/EventList/extension/TransformationEvent"))
                {
                    IEvent e = ReadTransformationEvent(xTransform);
                    data.Events.Add(e);
                }

                // foreach aggregate event...
                foreach (var xTransform in xml.Elements("//EPCISBody/epcisq:QueryResults/resultsBody/EventList/AggregationEvent"))
                {
                    IEvent e = ReadAggregationEvent(xTransform);
                    data.Events.Add(e);
                }

                // foreach transaction event...
                foreach (var xTransform in xml.Elements("//EPCISBody/epcisq:QueryResults/resultsBody/EventList/TransactionEvent"))
                {
                    IEvent e = ReadTransactionEvent(xTransform);
                    data.Events.Add(e);
                }

                // foreach association event...
                foreach (var xTransform in xml.Elements("//EPCISBody/epcisq:QueryResults/resultsBody/EventList/AssociationEvent"))
                {
                    IEvent e = ReadAssociationEvent(xTransform);
                    data.Events.Add(e);
                }

                return data;
            }
            catch (Exception Ex)
            {
                DSLogger.Log(0, Ex);
                throw;
            }
        }

        private IStandardBusinessDocumentHeader ReadHeader(DSXML xHeader)
        {
            IStandardBusinessDocumentHeader header = new StandardBusinessDocumentHeader();
            if (!xHeader.IsNull)
            {
                if (!xHeader["sbdh:HeaderVersion"].IsNull)
                {
                    header.HeaderVersion = xHeader["sbdh:HeaderVersion"].Value;
                }

                if (!xHeader["sbdh:Sender"].IsNull)
                {
                    header.Sender = ReadHeaderOrganization(xHeader["sbdh:Sender"]);
                }

                if (!xHeader["sbdh:Receiver"].IsNull)
                {
                    header.Receiver = ReadHeaderOrganization(xHeader["sbdh:Receiver"]);
                }

                if (!xHeader["sbdh:DocumentIdentification"].IsNull)
                {
                    DSXML xDocID = xHeader["sbdh:DocumentIdentification"];
                    header.DocumentIdentification = new SBDHDocumentIdentification();
                    header.DocumentIdentification.Standard = xDocID["sbdh:Standard"].Value;
                    header.DocumentIdentification.Type = xDocID["sbdh:Type"].Value;
                    header.DocumentIdentification.TypeVersion = xDocID["sbdh:TypeVersion"].Value;
                    header.DocumentIdentification.InstanceIdentifier = xDocID["sbdh:InstanceIdentifier"].Value;
                    header.DocumentIdentification.MultipleType = xDocID["sbdh:MultipleType"].Value;

                    if (DateTimeOffset.TryParse(xDocID["sbdh:CreationDateAndTime"].Value, out DateTimeOffset dt))
                    {
                        header.DocumentIdentification.CreationDateAndTime = dt;
                    }
                }
            }
            return header;
        }

        private void ReadProductDefinitions(DSXML xEPCISMasterData, List<IProduct> products)
        {
            foreach (DSXML xProductDef in xEPCISMasterData.Elements("VocabularyList/Vocabulary[@type='urn:epcglobal:epcis:vtype:EPCClass']/VocabularyElementList/VocabularyElement"))
            {
                IProduct product = new Product();

                string? owningParty = xProductDef["attribute[@id='urn:epcglobal:cbv:owning_party']"]?.Value;
                if (!string.IsNullOrWhiteSpace(owningParty))
                {
                    product.Owner = IdentifierFactory.ParsePGLN(owningParty);
                }

                string? infoProvider = xProductDef["attribute[@id='urn:epcglobal:cbv:mda#informationProvider']"]?.Value;
                if (!string.IsNullOrWhiteSpace(infoProvider))
                {
                    product.InformationProvider = IdentifierFactory.ParsePGLN(infoProvider);
                }

                if (product.InformationProvider == null)
                {
                    product.InformationProvider = product.Owner;
                }

                product.GTIN = IdentifierFactory.ParseGTIN(xProductDef.Attribute("id"));
                product.Name = xProductDef["attribute[@id='urn:epcglobal:cbv:mda#descriptionShort']"].Value;
                product.Seafood.ProductForm = ProductForm.GetFromKey(xProductDef["attribute[@id='urn:epcglobal:cbv:mda#tradeItemConditionCode']"].Value);

                string scientificName = xProductDef["attribute[@id='urn:epcglobal:cbv:mda#speciesForFisheryStatisticsPurposesName']"].Value;
                if (!string.IsNullOrWhiteSpace(scientificName))
                {
                    Species sp = SpeciesList.GetSpeciesByScientificName(scientificName);
                    if (sp != null)
                    {
                        product.Species.Add(sp);
                    }
                }
                else
                {
                    string alpha3code = xProductDef["attribute[@id='urn:epcglobal:cbv:mda#speciesForFisheryStatisticsPurposesCode']"].Value;
                    if (!string.IsNullOrWhiteSpace(alpha3code))
                    {
                        Species sp = SpeciesList.GetSpeciesByAlphaCode(alpha3code);
                        if (sp != null)
                        {
                            product.Species.Add(sp);
                        }
                    }
                }

                products.Add(product);
            }
        }

        private void ReadLocations(DSXML xEPCISMasterData, List<ILocation> locations)
        {
            foreach (DSXML xLocation in xEPCISMasterData.Elements("VocabularyList/Vocabulary[@type='urn:epcglobal:epcis:vtype:Location']/VocabularyElementList/VocabularyElement"))
            {
                ILocation location = new Location();

                string? owningParty = xLocation["attribute[@id='urn:epcglobal:cbv:owning_party']"]?.Value;
                if (!string.IsNullOrWhiteSpace(owningParty))
                {
                    location.Owner = IdentifierFactory.ParsePGLN(owningParty);
                }

                string? infoProvider = xLocation["attribute[@id='urn:epcglobal:cbv:mda#informationProvider']"]?.Value;
                if (!string.IsNullOrWhiteSpace(infoProvider))
                {
                    location.InformationProvider = IdentifierFactory.ParsePGLN(infoProvider);
                }

                if (location.InformationProvider == null)
                {
                    location.InformationProvider = location.Owner;
                }

                location.GLN = IdentifierFactory.ParseGLN(xLocation.Attribute("id"));
                location.Name = xLocation["attribute[@id='urn:epcglobal:cbv:mda#name']"].Value;
                location.Address = ReadAddress(xLocation);
                location.Vessel = ReadVessel(xLocation);
                locations.Add(location);
            }
        }

        private void ReadTradingParties(DSXML xEPCISMasterData, List<ITradingParty> parties)
        {
            foreach (DSXML xTradingParty in xEPCISMasterData.Elements("VocabularyList/Vocabulary[@type='urn:epcglobal:epcis:vtype:Party']/VocabularyElementList/VocabularyElement"))
            {
                ITradingParty tp = new TradingParty();

                string? owningParty = xTradingParty["attribute[@id='urn:epcglobal:cbv:owning_party']"]?.Value;
                if (!string.IsNullOrWhiteSpace(owningParty))
                {
                    tp.Owner = IdentifierFactory.ParsePGLN(owningParty);
                }

                string? infoProvider = xTradingParty["attribute[@id='urn:epcglobal:cbv:mda#informationProvider']"]?.Value;
                if (!string.IsNullOrWhiteSpace(infoProvider))
                {
                    tp.InformationProvider = IdentifierFactory.ParsePGLN(infoProvider);
                }

                if (tp.InformationProvider == null)
                {
                    tp.InformationProvider = tp.Owner;
                }

                tp.PGLN = IdentifierFactory.ParsePGLN(xTradingParty.Attribute("id"));
                tp.Name = xTradingParty["attribute[@id='urn:epcglobal:cbv:mda#name']"].Value;
                parties.Add(tp);
            }
        }

        private IAddress ReadAddress(DSXML xMasterData)
        {
            IAddress address = null;

            if (!xMasterData["attribute[@id='urn:epcglobal:cbv:mda#streetAddressOne']"].IsNull
             || !xMasterData["attribute[@id='urn:epcglobal:cbv:mda#streetAddressTwo']"].IsNull
             || !xMasterData["attribute[@id='urn:epcglobal:cbv:mda#city']"].IsNull
             || !xMasterData["attribute[@id='urn:epcglobal:cbv:mda#state']"].IsNull
             || !xMasterData["attribute[@id='urn:epcglobal:cbv:mda#postalCode']"].IsNull
             || !xMasterData["attribute[@id='urn:epcglobal:cbv:mda#countryCode']"].IsNull)
            {
                address = new Address();

                if (!xMasterData["attribute[@id='urn:epcglobal:cbv:mda#streetAddressOne']"].IsNull)
                {
                    address.Address1 = xMasterData["attribute[@id='urn:epcglobal:cbv:mda#streetAddressOne']"].Value;
                }

                if (!xMasterData["attribute[@id='urn:epcglobal:cbv:mda#streetAddressTwo']"].IsNull)
                {
                    address.Address2 = xMasterData["attribute[@id='urn:epcglobal:cbv:mda#streetAddressTwo']"].Value;
                }

                if (!xMasterData["attribute[@id='urn:epcglobal:cbv:mda#city']"].IsNull)
                {
                    address.City = xMasterData["attribute[@id='urn:epcglobal:cbv:mda#city']"].Value;
                }

                if (!xMasterData["attribute[@id='urn:epcglobal:cbv:mda#state']"].IsNull)
                {
                    address.State = xMasterData["attribute[@id='urn:epcglobal:cbv:mda#state']"].Value;
                }

                if (!xMasterData["attribute[@id='urn:epcglobal:cbv:mda#postalCode']"].IsNull)
                {
                    address.ZipCode = xMasterData["attribute[@id='urn:epcglobal:cbv:mda#postalCode']"].Value;
                }

                if (!xMasterData["attribute[@id='urn:epcglobal:cbv:mda#countryCode']"].IsNull)
                {
                    string strValue = xMasterData["attribute[@id='urn:epcglobal:cbv:mda#countryCode']"].Value;
                    address.Country = Countries.Parse(strValue);
                }
            }

            return address;
        }

        private IVesselInformation ReadVessel(DSXML xMasterData)
        {
            IVesselInformation vessel = null;

            if (!xMasterData["attribute[@id='urn:epcglobal:cbv:mda#vesselName']"].IsNull
             || !xMasterData["attribute[@id='urn:epcglobal:cbv:mda#vesselID']"].IsNull
             || !xMasterData["attribute[@id='urn:epcglobal:cbv:mda#vesselFlagState']"].IsNull
             || !xMasterData["attribute[@id='urn:gdst:kde#imoNumber']"].IsNull
             || !xMasterData["attribute[@id='urn:gdst:kde#satelliteTrackingAuthority']"].IsNull
             || !xMasterData["attribute[@id='urn:gdst:kde#vesselPublicRegistry']"].IsNull)
            {
                vessel = new VesselInformation();

                if (!xMasterData["attribute[@id='urn:epcglobal:cbv:mda#vesselName']"].IsNull)
                {
                    vessel.VesselName = xMasterData["attribute[@id='urn:epcglobal:cbv:mda#vesselName']"].Value;
                }

                if (!xMasterData["attribute[@id='urn:epcglobal:cbv:mda#vesselID']"].IsNull)
                {
                    vessel.VesselID = xMasterData["attribute[@id='urn:epcglobal:cbv:mda#vesselID']"].Value;
                }

                if (!xMasterData["attribute[@id='urn:gdst:kde#imoNumber']"].IsNull)
                {
                    vessel.IMONumber = xMasterData["attribute[@id='urn:gdst:kde#imoNumber']"].Value;
                }

                if (!xMasterData["attribute[@id='urn:epcglobal:cbv:mda#vesselFlagState']"].IsNull)
                {
                    string strValue = xMasterData["attribute[@id='urn:epcglobal:cbv:mda#vesselFlagState']"].Value;
                    vessel.VesselFlag = Countries.Parse(strValue);
                }

                if (!xMasterData["attribute[@id='urn:gdst:kde#satelliteTrackingAuthority']"].IsNull)
                {
                    string strValue = xMasterData["attribute[@id='urn:gdst:kde#satelliteTrackingAuthority']"].Value;
                    vessel.SatelliteTrackingAuthority = strValue;
                }

                if (!xMasterData["attribute[@id='urn:gdst:kde#vesselPublicRegistry']"].IsNull)
                {
                    string strValue = xMasterData["attribute[@id='urn:gdst:kde#vesselPublicRegistry']"].Value;
                    vessel.VesselPublicRegistry = strValue;
                }
            }

            return vessel;
        }

        private ISBDHOrganization ReadHeaderOrganization(DSXML xOrg)
        {
            ISBDHOrganization org = new SBDHOrganization();

            if (!xOrg["sbdh:Identifier"].IsNull)
            {
                org.Identifier = xOrg["sbdh:Identifier"].Value;
            }

            if (!xOrg["sbdh:ContactInformation/sbdh:Contact"].IsNull)
            {
                org.ContactName = xOrg["sbdh:ContactInformation/sbdh:Contact"].Value;
            }

            if (!xOrg["sbdh:ContactInformation/sbdh:EmailAddress"].IsNull)
            {
                org.EmailAddress = xOrg["sbdh:ContactInformation/sbdh:EmailAddress"].Value;
            }

            return org;
        }

        private void ReadCommonDataElements(DSXML xEvent, IEvent e)
        {
            try
            {
                // action
                if (!xEvent["action"].IsNull)
                {
                    if (Enum.TryParse<EventAction>(xEvent["action"].Value, out EventAction action))
                    {
                        e.Action = action;
                    }
                }

                // business step
                e.BusinessStep = xEvent["bizStep"].Value;

                // disposition
                e.Disposition = xEvent["disposition"].Value;

                // persistent disposition
                if (!xEvent["persistentDisposition"].IsNull)
                {
                    e.PersistentDisposition = new PersistentDisposition();
                    foreach (var xSet in xEvent.Elements("persistentDisposition/set"))
                    {
                        e.PersistentDisposition.Set.Add(xSet.Value);
                    }
                    foreach (var xUnset in xEvent.Elements("persistentDisposition/unset"))
                    {
                        e.PersistentDisposition.Unset.Add(xUnset.Value);
                    }
                }

                // business transactions
                foreach (var xBizTransaction in xEvent.Elements("bizTransactionList/bizTransaction"))
                {
                    IEventBusinessTransaction bizTransaction = new EventBusinessTransaction();
                    bizTransaction.RawType = xBizTransaction.Attribute("type");
                    bizTransaction.Value = xBizTransaction.Value;
                    e.BusinessTransactions.Add(bizTransaction);
                }

                // data owner
                e.DataOwner = TryParsePGLN(xEvent, "cbvmda:informationProvider", "extension/cbvmda:informationProvider");

                // error declaration
                e.ErrorDeclaration = ReadErrorDeclaration(xEvent);

                // event id
                e.EventID = xEvent["baseExtension/eventID"].Value ?? xEvent["eventID"].Value;

                if (!xEvent["baseExtension/errorDeclaration"].IsNull)
                {
                    DSXML xErrDec = xEvent["baseExtension/errorDeclaration"];
                    e.ErrorDeclaration = new ErrorDeclaration();
                    e.ErrorDeclaration.DeclarationTime = xErrDec["declarationTime"].GetValueDateTime();
                    if (!xErrDec["correctiveEventIDs/correctiveEventID"].IsNull)
                    {
                        e.ErrorDeclaration.CorrectingEventIDs = new List<string>();
                        foreach (var xID in xErrDec.Elements("correctiveEventIDs/correctiveEventID"))
                        {
                            e.ErrorDeclaration.CorrectingEventIDs.Add(xID.Value);
                        }
                    }

                    //<errorDeclaration>
                    //    <declarationTime>2022-02-08T19:41:23</declarationTime>
                    //    <correctiveEventIDs>
                    //        <correctiveEventID>0038bdc2-cad3-43eb-88f5-c93eda34ec43</correctiveEventID>
                    //    </correctiveEventIDs>
                    //    <extension />
                    //</errorDeclaration>
                }

                // event time and event timezone offset
                e.EventTime = ReadEventTime(xEvent);
                e.EventTimeOffset = ParseEventTimeZoneOffset(xEvent["eventTimeZoneOffset"].Value);

                // location
                if (!xEvent["bizLocation"].IsNull)
                {
                    e.Location = new EventLocation();
                    e.Location.GLN = TryParseGLN(xEvent, "bizLocation/id");
                }

                // read point
                if (!xEvent["readPoint/id"].IsNull)
                {
                    e.ReadPoint = new EventReadPoint();
                    e.ReadPoint.ID = xEvent["readPoint/id"].Value;
                }

                // product owner
                e.Owner = TryParsePGLN(xEvent, "gdst:productOwner", "extension/gdst:productOwner");

                // read point
                if (!xEvent["readPoint"].IsNull)
                {
                    e.ReadPoint = new EventReadPoint();
                    e.ReadPoint.ID = xEvent["readPoint/id"].Value;
                }

                // source list
                foreach (var xSource in xEvent.Elements("sourceList/source"))
                {
                    IEventSource source = new EventSource();
                    source.RawType = xSource.Attribute("type");
                    source.Value = xSource.Value;
                    e.SourceList.Add(source);
                }
                foreach (var xSource in xEvent.Elements("extension/sourceList/source"))
                {
                    IEventSource source = new EventSource();
                    source.RawType = xSource.Attribute("type");
                    source.Value = xSource.Value;
                    e.SourceList.Add(source);
                }

                // destination list
                foreach (var xDest in xEvent.Elements("destinationList/destination"))
                {
                    IEventDestination dest = new EventDestination();
                    dest.RawType = xDest.Attribute("type");
                    dest.Value = xDest.Value;
                    e.DestinationList.Add(dest);
                }
                foreach (var xDest in xEvent.Elements("extension/destinationList/destination"))
                {
                    IEventDestination dest = new EventDestination();
                    dest.RawType = xDest.Attribute("type");
                    dest.Value = xDest.Value;
                    e.DestinationList.Add(dest);
                }

                // read the ilmd data
                e.ILMD = ReadILMD(xEvent);

                // read the KDEs
                foreach (DSXML x in xEvent.ChildElements)
                {
                    string name = x.Name;

                    // try to look up the KDE for this element
                    IEventKDE kde = IEventKDE.InitializeFromKey(name);
                    if (kde != null)
                    {
                        kde.XmlValue = x;
                        e.KDEs.Add(kde);
                    }
                }

                // read the extension KDEs
                if (!xEvent["extension"].IsNull)
                {
                    foreach (DSXML x in xEvent["extension"].ChildElements)
                    {
                        string name = x.Name;

                        // try to look up the KDE for this element
                        IEventKDE kde = IEventKDE.InitializeFromKey(name);
                        if (kde != null)
                        {
                            kde.XmlValue = x;
                            e.ExtensionKDEs.Add(kde);
                        }
                    }
                }
            }
            catch (Exception Ex)
            {
                DSLogger.Log(0, Ex);
                throw;
            }
        }

        private IEvent ReadObjectEvent(DSXML xObject)
        {
            try
            {
                IObjectEvent e = new ObjectEvent();

                // read the epc list
                foreach (var xEPC in xObject.Elements("epcList/epc"))
                {
                    IEPC epc = TryParseEPC(xEPC);
                    e.AddProduct(epc);
                }

                // read the quantity list
                foreach (var xQuantity in xObject.Elements("extension/quantityList/quantityElement"))
                {
                    IEPC epc = TryParseEPC(xQuantity, "epcClass");
                    double quantity = xQuantity["quantity"].GetValueDouble() ?? 0;
                    string uom = xQuantity["uom"].Value;
                    e.AddProduct(epc, quantity, uom);
                }

                // read the common data elements
                ReadCommonDataElements(xObject, e);

                return e;
            }
            catch (Exception Ex)
            {
                DSLogger.Log(0, Ex);
                throw;
            }
        }

        private IEventILMD ReadILMD(DSXML xEvent)
        {
            IEventILMD ilmd = null;
            DSXML xILMD = xEvent["ilmd"];
            if (xILMD.IsNull)
            {
                xILMD = xEvent["extension/ilmd"];
            }
            if (!xILMD.IsNull)
            {
                ilmd = new EventILMD();
                foreach (DSXML xKDE in xILMD.ChildElements)
                {
                    string name = xKDE.Name;

                    // try to look up the KDE for this element
                    IEventKDE kde = IEventKDE.InitializeFromKey(name);
                    if (kde != null)
                    {
                        kde.XmlValue = xKDE;
                        ilmd.KDEs.Add(kde);
                    }
                }
            }
            return ilmd;
        }

        private IEvent ReadTransactionEvent(DSXML xTransaction)
        {
            try
            {
                ITransactionEvent e = new TransactionEvent();

                // read the epc list
                foreach (var xEPC in xTransaction.Elements("epcList/epc"))
                {
                    IEPC epc = TryParseEPC(xEPC);
                    e.AddProduct(epc);
                }

                // read the quantity list
                foreach (var xQuantity in xTransaction.Elements("extension/quantityList/quantityElement"))
                {
                    IEPC epc = TryParseEPC(xQuantity, "epcClass");
                    double quantity = xQuantity["quantity"].GetValueDouble() ?? 0;
                    string uom = xQuantity["uom"].Value;
                    e.AddProduct(epc, quantity, uom);
                }

                ReadCommonDataElements(xTransaction, e);
                return e;
            }
            catch (Exception Ex)
            {
                DSLogger.Log(0, Ex);
                throw;
            }
        }

        private IEvent ReadTransformationEvent(DSXML xTransform)
        {
            try
            {
                ITransformationEvent e = new TransformationEvent();

                // transformation id
                e.TransformationID = xTransform["transformationID"].Value;

                // read the input epc list
                foreach (var xEPC in xTransform.Elements("inputEPCList/epc"))
                {
                    IEPC epc = TryParseEPC(xEPC);
                    e.AddInput(epc);
                }

                // read the output epc list
                foreach (var xEPC in xTransform.Elements("outputEPCList/epc"))
                {
                    IEPC epc = TryParseEPC(xEPC);
                    e.AddOutput(epc);
                }

                // read the input quantity list
                foreach (var xQuantity in xTransform.Elements("inputQuantityList/quantityElement"))
                {
                    IEPC epc = TryParseEPC(xQuantity, "epcClass");
                    double quantity = xQuantity["quantity"].GetValueDouble() ?? 0;
                    string uom = xQuantity["uom"].Value;
                    e.AddInput(epc, quantity, uom);
                }

                // read the output quantity list
                foreach (var xQuantity in xTransform.Elements("outputQuantityList/quantityElement"))
                {
                    IEPC epc = TryParseEPC(xQuantity, "epcClass");
                    double quantity = xQuantity["quantity"].GetValueDouble() ?? 0;
                    string uom = xQuantity["uom"].Value;
                    e.AddOutput(epc, quantity, uom);
                }

                ReadCommonDataElements(xTransform, e);
                return e;
            }
            catch (Exception Ex)
            {
                DSLogger.Log(0, Ex);
                throw;
            }
        }

        private IEvent ReadAggregationEvent(DSXML xAggregate)
        {
            try
            {
                IAggregationEvent e = new AggregationEvent();

                // read the parent
                e.ParentID = TryParseEPC(xAggregate, "parentID");

                // child epcs
                foreach (var xEPC in xAggregate.Elements("childEPCs/epc"))
                {
                    IEPC epc = TryParseEPC(xEPC);
                    e.AddChild(epc);
                }

                // child quantities
                foreach (var xQuantity in xAggregate.Elements("extension/childQuantityList/quantityElement"))
                {
                    IEPC epc = TryParseEPC(xQuantity, "epcClass");
                    double quantity = xQuantity["quantity"].GetValueDouble() ?? 0;
                    string uom = xQuantity["uom"].Value;
                    e.AddChild(epc, quantity, uom);
                }

                ReadCommonDataElements(xAggregate, e);
                return e;
            }
            catch (Exception Ex)
            {
                DSLogger.Log(0, Ex);
                throw;
            }
        }

        private IEvent ReadAssociationEvent(DSXML xAssociation)
        {
            try
            {
                IAssociationEvent e = new AssociationEvent();

                // read the parent
                e.ParentID = TryParseEPC(xAssociation, "parentID");

                // child epcs
                foreach (var xEPC in xAssociation.Elements("childEPCs/epc"))
                {
                    IEPC epc = TryParseEPC(xEPC);
                    e.AddChild(epc);
                }

                ReadCommonDataElements(xAssociation, e);
                return e;
            }
            catch (Exception Ex)
            {
                DSLogger.Log(0, Ex);
                throw;
            }
        }

        private IErrorDeclaration ReadErrorDeclaration(DSXML xEvent)
        {
            try
            {
                IErrorDeclaration error = null;

                DSXML xError = xEvent["errorDeclaration"];
                if (xError.IsNull)
                {
                    xError = xEvent["extension/errorDeclaration"];
                }
                if (!xError.IsNull)
                {
                    error = new ErrorDeclaration();
                    error.DeclarationTime = DateTime.Parse(xError["declarationTime"].Value);
                    error.RawReason = xError["reason"].Value;
                    foreach (var xCID in xError.Elements("correctiveEventIDs/correctiveEventID"))
                    {
                        error.CorrectingEventIDs.Add(xCID.Value);
                    }
                }

                return error;
            }
            catch (Exception Ex)
            {
                DSLogger.Log(0, Ex);
                throw;
            }
        }

        private IEPC TryParseEPC(DSXML x, params string[] paths)
        {
            try
            {
                IEPC epc = null;
                string error = null;
                string value = null;

                if (paths == null || paths.Length < 1)
                {
                    if (!string.IsNullOrWhiteSpace(x.Value))
                    {
                        value = x.Value;
                        epc = IdentifierFactory.ParseEPC(value);
                    }
                }
                else
                {
                    foreach (string path in paths)
                    {
                        if (!string.IsNullOrWhiteSpace(x[path].Value))
                        {
                            value = x[path].Value;
                            epc = IdentifierFactory.ParseEPC(value);
                            break;
                        }
                    }
                }

                if (!string.IsNullOrWhiteSpace(error))
                {
                    throw new MappingException($"Failed to parse the EPC. {error}. epc= " + value);
                }

                return epc;
            }
            catch (Exception Ex)
            {
                DSLogger.Log(0, Ex);
                throw;
            }
        }

        private IGLN TryParseGLN(DSXML x, params string[] paths)
        {
            try
            {
                IGLN gln = null;
                string error = null;
                string value = null;

                if (paths == null || paths.Length < 1)
                {
                    if (!string.IsNullOrWhiteSpace(x.Value))
                    {
                        value = x.Value;
                        gln = IdentifierFactory.ParseGLN(value);
                    }
                }
                else
                {
                    foreach (string path in paths)
                    {
                        if (!string.IsNullOrWhiteSpace(x[path].Value))
                        {
                            value = x[path].Value;
                            gln = IdentifierFactory.ParseGLN(value);
                            break;
                        }
                    }
                }

                if (!string.IsNullOrWhiteSpace(error))
                {
                    throw new MappingException($"Failed to parse the EPC. {error}. epc= " + value);
                }

                return gln;
            }
            catch (Exception Ex)
            {
                DSLogger.Log(0, Ex);
                throw;
            }
        }

        private IPGLN TryParsePGLN(DSXML x, params string[] paths)
        {
            try
            {
                IPGLN pgln = null;
                string error = null;
                string value = null;

                if (paths == null || paths.Length < 1)
                {
                    if (!string.IsNullOrWhiteSpace(x.Value))
                    {
                        value = x.Value;
                        pgln = IdentifierFactory.ParsePGLN(value);
                    }
                }
                else
                {
                    foreach (string path in paths)
                    {
                        if (!string.IsNullOrWhiteSpace(x[path].Value))
                        {
                            value = x[path].Value;
                            pgln = IdentifierFactory.ParsePGLN(value);
                            break;
                        }
                    }
                }

                if (!string.IsNullOrWhiteSpace(error))
                {
                    throw new MappingException($"Failed to parse the EPC. {error}. epc= " + value);
                }

                return pgln;
            }
            catch (Exception Ex)
            {
                DSLogger.Log(0, Ex);
                throw;
            }
        }

        private double ParseEventTimeZoneOffset(string offset)
        {
            string hours = offset.Substring(1);
            TimeSpan ts = TimeSpan.Parse(hours);
            double dbl = ts.TotalHours;
            if (offset[0] == '-')
            {
                dbl = -dbl;
            }
            return dbl;
        }

        private string ConvertToTimeZoneOffset(double hours)
        {
            TimeSpan ts = TimeSpan.FromHours(hours);
            string offset = $"{ts.Hours.ToString().PadLeft(2, '0')}:{ts.Minutes.ToString().PadLeft(2, '0')}";
            if (hours >= 0) offset = "+" + offset;
            else offset = "-" + offset;
            return offset;
        }

        #endregion Convert from XML to C# Models
    }
}