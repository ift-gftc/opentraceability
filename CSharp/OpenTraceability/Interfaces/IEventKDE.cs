using Newtonsoft.Json.Linq;
using System;
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
    public interface IEventKDE
    {
        /// <summary>
        /// The key to the Event KDE that should include the namespace in the name.
        /// </summary>
        string Key { get; }

        /// <summary>
        /// The C# type of the KDE.
        /// </summary>
        Type ValueType { get; }

        /// <summary>
        /// Sets the value of the KDE using the JSON-LD in a string.
        /// </summary>
        /// <param name="json">The KDE expressed as JSON-LD.</param>
        void SetFromJson(JToken json);

        /// <summary>
        /// Gets the KDE in a JSON-LD format.
        /// </summary>
        /// <returns>The KDE expressed as JSON-LD.</returns>
        JToken? GetJson();

        /// <summary>
        /// Sets the KDE from the XML expression of it.
        /// </summary>
        /// <param name="xml">The KDE expressed as XML.</param>
        void SetFromXml(XElement xml);

        /// <summary>
        /// Gets the KDE in an XML format.
        /// </summary>
        /// <returns>The KDE expressed as XML.</returns>
        XElement? GetXml();
    }
}
