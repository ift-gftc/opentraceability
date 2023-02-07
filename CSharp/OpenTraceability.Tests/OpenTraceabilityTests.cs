using OpenTraceability.Utility;
using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;

namespace OpenTraceability.Tests
{
    /// <summary>
    /// Utility class for the open traceability test cases.
    /// </summary>
    public static class OpenTraceabilityTests
    {
        internal static void CompareXML(string xmlObjectEvents, string xmlObjectEventsAfter)
        {
            throw new NotImplementedException();
        }

        internal static string ReadTestData(string v)
        {
            EmbeddedResourceLoader loader = new EmbeddedResourceLoader();
            string str = loader.ReadString("OpenTraceability.Tests", $"OpenTraceability.Tests.Data.{v}");
            return str;
        }
    }
}
