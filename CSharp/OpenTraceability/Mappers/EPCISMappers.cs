using GS1.Mappers.EPCIS;
using OpenTraceability.Interfaces;
using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;

namespace OpenTraceability.Mappers
{
    public static class EPCISMappers
    {
        public static EPCISDocumentMappers EPCISDocument = new EPCISDocumentMappers();
    }

    public class EPCISDocumentMappers
    {
        public IEPCISDocumentMapper XML = new EPCISDocumentXMLMapper();
    }
}
