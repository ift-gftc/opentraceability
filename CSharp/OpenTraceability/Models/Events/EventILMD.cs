using OpenTraceability.Interfaces;

namespace OpenTraceability.Models.Events
{
    /// <summary>
    /// The ILMD section of the event.
    /// </summary>
    public class EventILMD
    {
        /// <summary>
        /// A list of KDEs contained in the ILMD section of the event.
        /// </summary>
        public List<IEventKDE> KDEs { get; set; } = new List<IEventKDE>();

        /// <summary>
        /// Gets a KDE by the type and key value.
        /// </summary>
        /// <typeparam name="T">The C# type of the KDE.</typeparam>
        /// <param name="key">The key value of the KDE.</param>
        /// <returns>The instance of the IEventKDE that matches the parameters.</returns>
        public T? GetKDE<T>(string key)
        {
            IEventKDE? kde = KDEs.Find(k => k.Key == key);
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
            IEventKDE? kde = KDEs.Find(k => k.ValueType == typeof(T));
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