using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;
using DSUtil;
using GS1.Interfaces.Models;
using GS1.Interfaces.Models.Common;
using GS1.Interfaces.Models.Events;
using GS1.Interfaces.Models.Identifiers;
using GS1.Interfaces.Models.Locations;
using GS1.Interfaces.Models.Products;
using GS1.Interfaces.Models.TradingParty;

namespace OpenTraceability.Models.Events
{
    public class EPCISQueryDocument : EPCISBaseDocument, IEPCISBaseDocument<IEPCISQueryDocument>, IEPCISQueryDocument
    {
        public string QueryName { get; set; }

        public IEPCISQueryDocument IsolateProductTree(EPC product)
        {
            IEPCISQueryDocument isolatedDoc = new EPCISQueryDocument();
            IsolateProductTree_Internal(isolatedDoc, product);
            return isolatedDoc;
        }

        public void Merge(IEPCISQueryDocument data)
        {
            this.Events.AddRange(data.Events.Where(e => !this.Events.Exists(e2 => e.EventID == e2.EventID)));
            this.ProductDefinitions.AddRange(data.ProductDefinitions.Where(p => !this.ProductDefinitions.Exists(p2 => p.GTIN.Equals(p2.GTIN))));
            this.Locations.AddRange(data.Locations.Where(l => !this.Locations.Exists(l2 => l.GLN.Equals(l2.GLN))));
            this.TradingParties.AddRange(data.TradingParties.Where(t => !this.TradingParties.Exists(t2 => t.PGLN.Equals(t2.PGLN))));
        }

        public IEPCISDocument ToDocument()
        {
            IEPCISDocument document = new EPCISDocument();
            document.Header = this.Header;
            document.Events = this.Events;
            document.ProductDefinitions = this.ProductDefinitions;
            document.Locations = this.Locations;
            document.TradingParties = this.TradingParties;
            return document;
        }
    }
}
