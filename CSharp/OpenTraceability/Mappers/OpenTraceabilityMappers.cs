using GS1.Mappers.EPCIS;
using OpenTraceability.Interfaces;
using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;

namespace OpenTraceability.Mappers
{
    public static class OpenTraceabilityMappers
    {
        public static EPCISDocumentMappers EPCISDocument = new EPCISDocumentMappers();
        public static EPCISQueryDocumentMappers EPCISQueryDocument = new EPCISQueryDocumentMappers();
    }

    public class EPCISDocumentMappers
    {
        public IEPCISDocumentMapper XML = new EPCISDocumentXMLMapper();
    }

    public class EPCISQueryDocumentMappers
    {
        public IEPCISQueryDocumentMapper XML = new EPCISQueryDocumentXMLMapper();
    }
}
