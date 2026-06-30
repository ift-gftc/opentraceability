using OpenTraceability.Models.Events;
using System;
using System.Collections.Generic;
using System.Text;

namespace OpenTraceability.GDST.Extensions
{
    public static class EpcisDocumentExtensions
    {
        public static EPCISDocument ToGDST2_0(this EPCISDocument doc)
        {
            // iterate over events
            // update business steps and any missing properties to match the GDST 2.0 standard
            // update relevant master data related to that event, adding product and location classifications as necessary
            // update 
            throw new NotImplementedException();
        }
    }
}
