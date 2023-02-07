using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;
using System.Xml.Linq;

namespace OpenTraceability.Utility
{
    public static class XElementExtensions
    {
        public static Dictionary<string, string> GetDocumentNamespaces(this XDocument x)
        {
            if (x.Root == null) throw new Exception("Root on XDocument is null.");

            var result = x.Root.Attributes().
                         Where(a => a.IsNamespaceDeclaration).
                         GroupBy(a => a.Name.Namespace == XNamespace.None ? String.Empty : a.Name.LocalName,
                                a => XNamespace.Get(a.Value)).
                         ToDictionary(g => g.Key,
                                     g => g.First().NamespaceName);

            return result;
        }
    }
}
