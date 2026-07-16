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
    ///   3. Removes <c>gdst:productClassification</c> / <c>gdst:locationClassification</c> entries whose
    ///      <c>value</c> is not owned by an allowed module (strict allow-list - unknown values are
    ///      stripped too).
    ///   4. Prunes nulls, empty strings, empty arrays, and empty objects.
    ///   5. Emits compact (non-indented) JSON.
    /// </summary>
    /// <remarks>
    /// The classification value filter only matches the GS1 Web Vocab master-data shape, where
    /// classifications appear under the <c>gdst:productClassification</c> / <c>gdst:locationClassification</c>
    /// keys. Master data embedded in EPCIS documents uses the vocabularyElement shape
    /// (<c>id: urn:gdst:kde#productClassification</c>), which this minifier has never matched - key
    /// stripping does not apply there either. That asymmetry is intentional: the EPCIS query path
    /// serves embedded master data to all tiers, and consumers depend on it.
    /// </remarks>
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
                        if (prop.Value is JArray classifications && IsClassificationList(prop.Name))
                        {
                            FilterClassificationEntries(classifications, prop.Name, allowedModules);
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

        private static bool IsClassificationList(string name)
        {
            return string.Equals(name, "gdst:productClassification", System.StringComparison.OrdinalIgnoreCase)
                || string.Equals(name, "gdst:locationClassification", System.StringComparison.OrdinalIgnoreCase);
        }

        /// <summary>
        /// Removes classification entries whose <c>value</c> is owned by a module not in the allowed
        /// set. The value maps are strict allow-lists (see <see cref="ModuleRegistry"/>): unknown values
        /// are stripped too, so each tier is served exactly the classifications its modules define
        /// (e.g. Core keeps only <c>land facility</c> locations and no product classifications). The
        /// normal empty-array pruning later removes the whole list when every entry was stripped.
        /// </summary>
        private static void FilterClassificationEntries(JArray list, string propertyName, ISet<GdstModule> allowedModules)
        {
            bool isProduct = propertyName.IndexOf("product", System.StringComparison.OrdinalIgnoreCase) >= 0;
            foreach (var item in list.ToList())
            {
                if (item is JObject entry)
                {
                    string? value = entry["value"]?.Value<string>();
                    var module = isProduct ? ModuleRegistry.GetModuleForProductClassification(value) : ModuleRegistry.GetModuleForLocationClassification(value);
                    if (module == null)
                    {
                        // if the classification is not assigned to a module, keep it.
                        continue;
                    }
                    if (!allowedModules.Contains(module.Value))
                    {
                        item.Remove();
                    }
                }
            }
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
