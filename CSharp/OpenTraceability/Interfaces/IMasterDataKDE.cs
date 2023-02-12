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
        private static ConcurrentDictionary<string, Type> RegisteredKDEs = new ConcurrentDictionary<string, Type>();

        /// <summary>
        /// Registers a KDE so that it will be deserialized properly and serialized properly.
        /// </summary>
        /// <typeparam name="T">The C# type of the KDE.</typeparam>
        /// <param name="ns">The full URI namespace for the KDE.</param>
        /// <param name="name">The local name of the KDE.</param>
        /// <exception cref="Exception"></exception>
        public static void RegisterKDE<T>(string ns, string name) where T: IMasterDataKDE, new()
        {
            string key = ns + ":" + name;
            if (!RegisteredKDEs.ContainsKey(key))
            {
                RegisteredKDEs.TryAdd(key, typeof(T));
            }
            else
            {
                throw new Exception($"The KDE {key} is already registered with type {RegisteredKDEs[key].FullName}");
            }
        }

        /// <summary>
        /// Initializes a KDE from the namespace and name. The KDE needs to be registered with the "RegisterKDE" method prior to calling this.
        /// </summary>
        /// <param name="ns">The full URI namespace for the KDE.</param>
        /// <param name="name">The local name of the KDE.</param>
        /// <returns>The new copy of the IMasterDataKDE that has been initialzied.</returns>
        public static IMasterDataKDE? InitializeKDE(string ns, string name)
        {
            IMasterDataKDE? kde = null;

            string key = ns + ":" + name;
            if (RegisteredKDEs.TryGetValue(key, out Type? kdeType))
            {
                if (kdeType != null)
                {
                    kde = Activator.CreateInstance(kdeType) as IMasterDataKDE;
                }
            }

            if (kde != null)
            {
                kde.Namespace = ns;
                kde.Name = name;
            }

            return kde;
        }

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
        void SetEPCISFromXml(XElement xml);

        /// <summary>
        /// Gets the KDE in an XML format to be inserted into an EPCIS document.
        /// </summary>
        /// <returns>The KDE expressed as XML.</returns>
        XElement? GetEPCISXml();
    }
}
