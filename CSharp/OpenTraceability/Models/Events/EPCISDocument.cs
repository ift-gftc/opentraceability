using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;
using GS1.Interfaces.Models;
using GS1.Interfaces.Models.Common;
using GS1.Interfaces.Models.Events;
using GS1.Interfaces.Models.Identifiers;
using GS1.Interfaces.Models.Locations;
using GS1.Interfaces.Models.Products;
using GS1.Interfaces.Models.TradingParty;

namespace OpenTraceability.Models.Events
{
    public class EPCISDocument : EPCISBaseDocument, IEPCISBaseDocument<IEPCISDocument>, IEPCISDocument
    {
        public IEPCISDocument IsolateProductTree(EPC product)
        {
            IEPCISDocument doc = new EPCISDocument();
            IsolateProductTree_Internal(doc, product);
            return doc;
        }

        public void Merge(IEPCISDocument data)
        {
            this.Events.AddRange(data.Events.Where(e => !this.Events.Exists(e2 => e.EventID == e2.EventID)));
            this.ProductDefinitions.AddRange(data.ProductDefinitions.Where(p => !this.ProductDefinitions.Exists(p2 => p.GTIN.Equals(p2.GTIN))));
            this.Locations.AddRange(data.Locations.Where(l => !this.Locations.Exists(l2 => l.GLN.Equals(l2.GLN))));
            this.TradingParties.AddRange(data.TradingParties.Where(t => !this.TradingParties.Exists(t2 => t.PGLN.Equals(t2.PGLN))));
        }

        public IEPCISQueryDocument ToQueryDocument()
        {
            IEPCISQueryDocument document = new EPCISQueryDocument();
            document.Header = this.Header;
            document.QueryName = "SimpleEventQuery";
            document.TradingParties = this.TradingParties;
            document.ProductDefinitions = this.ProductDefinitions;
            document.Locations = this.Locations;
            document.Events = this.Events;
            return document;
        }
    }
}
