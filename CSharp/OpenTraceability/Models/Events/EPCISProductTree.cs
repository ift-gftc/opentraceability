using OpenTraceability.Interfaces;
using OpenTraceability.Models.Identifiers;
using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;

namespace OpenTraceability.Models.Events
{
    /// <summary>
    /// Represents a product-tree where each node is an EPC, and list of input EPCs, and a list of associated events with that EPC.
    /// </summary>
    public class EPCISProductTree
    {
        /// <summary>
        /// The EPC of this node.
        /// </summary>
        public EPC EPC { get; set; }

        /// <summary>
        /// The GLN of the location for where this EPC is in the supply chain.
        /// </summary>
        public GLN GLN { get; set; }

        public List<EPC> SourceEPCs { get; set; } = new List<EPC>();

        /// <summary>
        /// All events associated with this EPC in this section of the product tree.
        /// </summary>
        public List<IEvent> Events { get; set; } = new List<IEvent>();
    }
}
