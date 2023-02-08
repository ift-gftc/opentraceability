using OpenTraceability.Interfaces;
using OpenTraceability.Utility;

namespace OpenTraceability.Models.Events
{
    /// <summary>
    /// The ILMD section of the event.
    /// </summary>
    public class EventILMD
    {
        public Dictionary<string, string> _namespaces = new Dictionary<string, string>();
        public Dictionary<string, string> _prefixes = new Dictionary<string, string>();
        private List<IEventKDE> _kdes = new List<IEventKDE>();

        /// <summary>
        /// A list of KDEs contained in the ILMD section of the event.
        /// </summary>
        public IReadOnlyCollection<IEventKDE> KDEs { get; set; } = new List<IEventKDE>();

        /// <summary>
        /// Adds a KDE to the event.
        /// </summary>
        /// <param name="kde"></param>
        public void AddKDE(IEventKDE kde)
        {
            _kdes.Add(kde);
        }

        /// <summary>
        /// Gets a KDE by the type and key value.
        /// </summary>
        /// <typeparam name="T">The C# type of the KDE.</typeparam>
        /// <param name="key">The key value of the KDE.</param>
        /// <returns>The instance of the IEventKDE that matches the parameters.</returns>
        public T? GetKDE<T>(string ns, string name)
        {
            // if we are given the prefixes...
            if (_prefixes.ContainsKey(ns))
            {
                ns = _prefixes[ns];
            }

            IEventKDE? kde = _kdes.Find(k => k.Namespace == ns && k.Name == name);
            if (kde != null)
            {
                if (kde is T)
                {
                    return (T)kde;
                }
            }
            return default;
        }

        /// <summary>
        /// Gets the first KDE that matches the type provided.
        /// </summary>
        /// <typeparam name="T">The C# type of the KDE.</typeparam>
        /// <returns>The instance of the IEventKDE that matches the parameters.</returns>
        public T? GetKDE<T>()
        {
            IEventKDE? kde = _kdes.Find(k => k.ValueType == typeof(T));
            if (kde != null)
            {
                if (kde is T)
                {
                    return (T)kde;
                }
            }
            return default;
        }

        /// <summary>
        /// Sets the namespaces on the event. This will replace the existing namespaces.
        /// </summary>
        /// <param name="namespaces"></param>
        public void SetNamespaces(Dictionary<string, string> namespaces)
        {
            _namespaces = namespaces;
            _prefixes = namespaces.Reverse();
        }
    }
}