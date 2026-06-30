using OpenTraceability.Models.Events;
using System;
using System.Collections.Generic;
using System.Runtime.CompilerServices;
using System.Text;

namespace OpenTraceability.GDST.Extensions
{
    public static class EpcisQueryDocumentExtensions
    {
        public static EPCISQueryDocument ToGDST2_0(this EPCISQueryDocument doc)
        {
            // iterate over events
            // update business steps and any missing properties to match the GDST 2.0 standard
            // update relevant master data related to that event, adding product and location classifications as necessary
            // update 
            throw new NotImplementedException();
        }
    }
}
