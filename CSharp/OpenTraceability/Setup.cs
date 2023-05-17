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
        internal static List<OpenTraceabilityEventProfile> Profiles = new List<OpenTraceabilityEventProfile>();
        internal static ConcurrentDictionary<string, Type> MasterDataTypes = new ConcurrentDictionary<string, Type>();
        internal static ConcurrentDictionary<Type, Type> MasterDataTypeDefault = new ConcurrentDictionary<Type, Type>();

        private static object _locker = new object();
        private static bool _isInitialized = false;

        public static void Initialize()
        {
            lock (_locker)
            {
                if (!_isInitialized)
                {
                    RegisterEventProfile(new OpenTraceabilityEventProfile(typeof(ObjectEvent<EventILMD>), EventType.ObjectEvent));
                    RegisterEventProfile(new OpenTraceabilityEventProfile(typeof(TransactionEvent), EventType.TransactionEvent));
                    RegisterEventProfile(new OpenTraceabilityEventProfile(typeof(TransformationEvent<EventILMD>), EventType.TransformationEvent));
                    RegisterEventProfile(new OpenTraceabilityEventProfile(typeof(AggregationEvent<EventILMD>), EventType.AggregationEvent));
                    RegisterEventProfile(new OpenTraceabilityEventProfile(typeof(AssociationEvent), EventType.AssociationEvent));

                    RegisterMasterDataType<Tradeitem>();
                    RegisterMasterDataType<Location>();
                    RegisterMasterDataType<TradingParty>();

                    JsonConvert.DefaultSettings = () =>
                    {
                        JsonSerializerSettings settings = new JsonSerializerSettings();
                        settings.Converters.Add(new EPCConverter());
                        settings.Converters.Add(new GTINConverter());
                        settings.Converters.Add(new GLNConverter());
                        settings.Converters.Add(new PGLNConverter());

                        return settings;
                    };

                    _isInitialized = true;
                }
            }
        }

        /// <summary>
        /// We use event profiles to automatically load extension events.
        /// </summary>
        public static void RegisterEventProfile(OpenTraceabilityEventProfile profile)
        {
            lock (_locker)
            {
                // if we find another profile with the same hash, then remove that one first...
                foreach (var to_remove in Profiles.Where(p => p.ToString() == profile.ToString()).ToList())
                {
                    Profiles.Remove(to_remove);
                }

                Profiles.Add(profile);
            }
        }

        /// <summary>
        /// This will register a class as a master data type to be deserialized from the master data in an EPCIS Document. If a previouisly
        /// registered C# type has the same EPCIS Vocabulary Element type, it will be repleaced with the new registered class type.
        /// </summary>
        /// <param name="type">This is the VocabularyElementList type value in the XML/JSON.</param>
        /// <param name="classType">This is the C# class to use when deserializing.</param>
        public static void RegisterMasterDataType<T>(Type? defaultFor=null) where T : IVocabularyElement
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

            if (defaultFor != null)
            {
                if (!MasterDataTypeDefault.TryGetValue(defaultFor, out Type? t2))
                {
                    MasterDataTypeDefault.TryAdd(defaultFor, typeof(T));
                }
                else
                {
                    MasterDataTypeDefault[defaultFor] = typeof(T);
                }
            }
        }

        /// <summary>
        /// This will register a class as a master data type to be deserialized from the master data in an EPCIS Document. If a previouisly
        /// registered C# type has the same EPCIS Vocabulary Element type, it will be repleaced with the new registered class type.
        /// </summary>
        /// <param name="type">This is the VocabularyElementList type value in the XML/JSON.</param>
        /// <param name="classType">This is the C# class to use when deserializing.</param>
        public static void RegisterMasterDataType<T, TDefaultFor>() where T : IVocabularyElement
        {
            RegisterMasterDataType<T>(typeof(TDefaultFor));
        }

        public static Type? GetMasterDataTypeDefault(Type type)
        {
            if (MasterDataTypeDefault.TryGetValue(type, out Type? t))
            {
                return t;
            }
            else
            {
                return null;
            }
        }
    }
}
