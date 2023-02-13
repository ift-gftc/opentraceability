using OpenTraceability.Interfaces;
using OpenTraceability.Utility;
using OpenTraceability.Utility.Attributes;

namespace OpenTraceability.Models.Events
{
    /// <summary>
    /// The ILMD section of the event.
    /// </summary>
    public class EventILMD
    {
        [OpenTraceabilityExtensionElements]
        public List<IEventKDE> ExtensionKDEs { get; internal set; } = new List<IEventKDE>();

        /// <summary>
        /// Gets a KDE by the type and key value.
        /// </summary>
        /// <typeparam name="T">The C# type of the KDE.</typeparam>
        /// <param name="key">The key value of the KDE.</param>
        /// <returns>The instance of the IEventKDE that matches the parameters.</returns>
        public T? GetKDE<T>(string ns, string name)
        {
            IEventKDE? kde = ExtensionKDEs.Find(k => k.Namespace == ns && k.Name == name);
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
            IEventKDE? kde = ExtensionKDEs.Find(k => k.ValueType == typeof(T));
            if (kde != null)
            {
                if (kde is T)
                {
                    return (T)kde;
                }
            }
            return default;
        }
    }
}