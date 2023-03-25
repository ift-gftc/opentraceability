using Newtonsoft.Json;
using OpenTraceability.Interfaces;
using OpenTraceability.Models.Events;
using OpenTraceability.Models.MasterData;
using OpenTraceability.Utility;
using System;
using System.Collections;
using System.Collections.Concurrent;
using System.Collections.Generic;
using System.Collections.ObjectModel;
using System.Linq;
using System.Reflection;
using System.Runtime.CompilerServices;
using System.Text;
using System.Threading.Tasks;

namespace OpenTraceability
{
    /// <summary>
    /// Static helper class for the Open Traceability library.
    /// </summary>
    public static class Setup
    {
        internal static ConcurrentBag<OpenTraceabilityEventProfile> Profiles = new ConcurrentBag<OpenTraceabilityEventProfile>();
        internal static ConcurrentDictionary<string, Type> MasterDataTypes = new ConcurrentDictionary<string, Type>();

        public static void Initialize()
        {
            RegisterEventProfile(new OpenTraceabilityEventProfile(typeof(ObjectEvent<EventILMD>), EventType.ObjectEvent));
            RegisterEventProfile(new OpenTraceabilityEventProfile(typeof(TransactionEvent), EventType.TransactionEvent));
            RegisterEventProfile(new OpenTraceabilityEventProfile(typeof(TransformationEvent<EventILMD>), EventType.TransformationEvent));
            RegisterEventProfile(new OpenTraceabilityEventProfile(typeof(AggregationEvent<EventILMD>), EventType.AggregationEvent));
            RegisterEventProfile(new OpenTraceabilityEventProfile(typeof(AssociationEvent), EventType.AssociationEvent));

            RegisterMasterDataType<Tradeitem>();
            RegisterMasterDataType<Location>();
            RegisterMasterDataType<TradingParty>();

            var assemblies = GetAssemblies();
            foreach (var assembly in assemblies.Where(a => a.FullName?.StartsWith("OpenTraceability.") == true))
            {
                foreach (var t in assembly.GetTypes())
                {

                }
            }

            JsonConvert.DefaultSettings = () =>
            {
                JsonSerializerSettings settings = new JsonSerializerSettings();
                settings.Converters.Add(new EPCConverter());

                return settings;
            };
        }

        internal static List<Assembly> GetAssemblies()
        {
            var returnAssemblies = new List<Assembly>();
            var loadedAssemblies = new HashSet<string>();
            var assembliesToCheck = new Queue<Assembly>();

            assembliesToCheck.Enqueue(Assembly.GetEntryAssembly());

            while (assembliesToCheck.Count > 0)
            {
                var assemblyToCheck = assembliesToCheck.Dequeue();

                foreach (var reference in assemblyToCheck.GetReferencedAssemblies())
                {
                    if (!loadedAssemblies.Contains(reference.FullName))
                    {
                        var assembly = Assembly.Load(reference);
                        assembliesToCheck.Enqueue(assembly);
                        loadedAssemblies.Add(reference.FullName);
                        returnAssemblies.Add(assembly);
                    }
                }
            }

            return returnAssemblies;
        }

        /// <summary>
        /// We use event profiles to automatically load extension events.
        /// </summary>
        public static void RegisterEventProfile(OpenTraceabilityEventProfile profile)
        {
            Profiles.Add(profile);
        }

        /// <summary>
        /// This will register a class as a master data type to be deserialized from the master data in an EPCIS Document. If a previouisly
        /// registered C# type has the same EPCIS Vocabulary Element type, it will be repleaced with the new registered class type.
        /// </summary>
        /// <param name="type">This is the VocabularyElementList type value in the XML/JSON.</param>
        /// <param name="classType">This is the C# class to use when deserializing.</param>
        public static void RegisterMasterDataType<T>() where T : IVocabularyElement
        {
            IVocabularyElement? v = Activator.CreateInstance(typeof(T)) as IVocabularyElement;
            if (v == null)
            {
                throw new Exception("Failed to create an instance of type T using Activator.CreateInstance");
            }

            string type = v.EPCISType?.ToLower() ?? throw new Exception("The 'Type' property on the instance of T returned a NULL value.");

            if (!MasterDataTypes.TryGetValue(type, out Type? t))
            {
                MasterDataTypes.TryAdd(type, typeof(T));
            }
            else
            {
                MasterDataTypes[type] = typeof(T);
            }
        }
    }
}
