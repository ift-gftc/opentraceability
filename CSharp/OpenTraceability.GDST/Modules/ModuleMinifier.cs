using System.Collections.Generic;
using System.Linq;
using Newtonsoft.Json;
using Newtonsoft.Json.Linq;
using OpenTraceability.GDST;

namespace OpenTraceability.GDST.Modules
{
    /// <summary>
    /// Produces a "minified" view of serialized JSON-LD scoped to a set of supported modules:
    ///   1. Removes any property whose JSON-LD key is owned by a module NOT in the allowed set
    ///      (the property is removed entirely - not nulled or emptied).
    ///   2. Removes source/destination list entries whose <c>type</c> value (e.g. <c>owning_party</c>)
    ///      is owned by a module NOT in the allowed set, while keeping <c>location</c> entries and the
    ///      list itself.
    ///   3. Prunes nulls, empty strings, empty arrays, and empty objects.
    ///   4. Emits compact (non-indented) JSON.
    /// </summary>
    public static class ModuleMinifier
    {
        /// <summary>Minifies a JSON-LD string against the allowed module set.</summary>
        public static string Minify(string json, ISet<GdstModule> allowedModules)
        {
            if (string.IsNullOrWhiteSpace(json)) return json;
            JToken root = JToken.Parse(json);
            Process(root, allowedModules);
            return root.ToString(Formatting.None);
        }

        /// <summary>
        /// Recursively strips module-scoped keys and prunes empties, mutating in place.
        /// Returns true if the token should be kept, false if it is empty and should be removed.
        /// </summary>
        private static bool Process(JToken token, ISet<GdstModule> allowedModules)
        {
            switch (token)
            {
                case JObject obj:
                    foreach (var prop in obj.Properties().ToList())
                    {
                        var module = ModuleRegistry.GetModule(prop.Name);
                        if (module.HasValue && !allowedModules.Contains(module.Value))
                        {
                            prop.Remove();
                            continue;
                        }
                        if (prop.Value is JArray list && IsSourceOrDestinationList(prop.Name))
                        {
                            FilterSourceDestEntries(list, allowedModules);
                        }
                        if (!Process(prop.Value, allowedModules))
                        {
                            prop.Remove();
                        }
                    }
                    return obj.Count > 0;

                case JArray arr:
                    foreach (var item in arr.ToList())
                    {
                        if (!Process(item, allowedModules))
                        {
                            item.Remove();
                        }
                    }
                    return arr.Count > 0;

                case JValue value:
                    if (value.Type == JTokenType.Null) return false;
                    if (value.Type == JTokenType.String && string.IsNullOrEmpty(value.Value<string>())) return false;
                    return true;

                default:
                    return true;
            }
        }

        private static bool IsSourceOrDestinationList(string name)
        {
            return string.Equals(name, "sourceList", System.StringComparison.OrdinalIgnoreCase)
                || string.Equals(name, "destinationList", System.StringComparison.OrdinalIgnoreCase);
        }

        /// <summary>
        /// Removes source/destination list entries whose <c>type</c> value is owned by a module not in
        /// the allowed set. Entries with a Core type (e.g. <c>location</c>) are always kept. The normal
        /// empty-array pruning later removes the whole list only if every entry was stripped.
        /// </summary>
        private static void FilterSourceDestEntries(JArray list, ISet<GdstModule> allowedModules)
        {
            foreach (var item in list.ToList())
            {
                if (item is JObject entry)
                {
                    string? type = entry["type"]?.Value<string>();
                    var module = ModuleRegistry.GetModuleForSourceDestinationType(type);
                    if (module.HasValue && !allowedModules.Contains(module.Value))
                    {
                        item.Remove();
                    }
                }
            }
        }
    }
}
