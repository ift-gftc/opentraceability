using Newtonsoft.Json.Linq;
using System;
using System.Collections.Concurrent;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Text.Json;
using System.Threading.Tasks;
using System.Xml.Linq;

namespace OpenTraceability.Interfaces
{
    /// <summary>
    /// This is an interface for handling event KDEs in EPCIS. This can be used on the base event or in the ILMD.
    /// </summary>
    public interface IMasterDataKDE
    {
        /// <summary>
        /// The namespace that the KDE sits under. This should be the full URI and not the prefix.
        /// </summary>
        string Namespace { get; set; }

        /// <summary>
        /// The name of the KDE in the XML/JSON.
        /// </summary>
        string Name { get; set; }

        /// <summary>
        /// The C# type of the KDE.
        /// </summary>
        Type ValueType { get; }

        /// <summary>
        /// Sets the value of the KDE using the JSON-LD in a string.
        /// </summary>
        /// <param name="json">The KDE expressed as JSON-LD.</param>
        void SetFromGS1WebVocabJson(JToken json);

        /// <summary>
        /// Gets the KDE in a JSON-LD format.
        /// </summary>
        /// <returns>The KDE expressed as JSON-LD.</returns>
        JToken? GetGS1WebVocabJson();

        /// <summary>
        /// Sets the KDE from the XML expression from an EPCIS XML document.
        /// </summary>
        /// <param name="xml">The KDE expressed as XML.</param>
        void SetFromEPCISXml(XElement xml);

        /// <summary>
        /// Gets the KDE in an XML format to be inserted into an EPCIS document.
        /// </summary>
        /// <returns>The KDE expressed as XML.</returns>
        XElement? GetEPCISXml();
    }
}
