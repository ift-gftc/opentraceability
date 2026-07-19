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
    ///   4. Removes <c>cbvmda:certificationList</c> certification entries whose
    ///      <c>gdst:certificationType</c> is owned by a module NOT in the allowed set (unlabelled
    ///      types are kept). Applies wherever the list appears - event root or ilmd.
    ///   5. Prunes objects and arrays that became empty as a result of the removals above.
    ///   6. Emits compact (non-indented) JSON.
    /// </summary>
    /// <remarks>
    /// The classification value filter only matches the GS1 Web Vocab master-data shape, where
    /// classifications appear under the <c>gdst:productClassification</c> / <c>gdst:locationClassification</c>
    /// keys. Master data embedded in EPCIS documents uses the vocabularyElement shape
    /// (<c>id: urn:gdst:kde#productClassification</c>), which this minifier has never matched - key
    /// stripping does not apply there either. That asymmetry is intentional: the EPCIS query path
    /// serves embedded master data to all tiers, and consumers depend on it.
    ///
    /// Minification must never invalidate the document: if the input JSON was valid against the
    /// EPCIS JSON schema, the minified output must be too. That is why only containers emptied by
    /// the minifier itself are pruned - nulls, empty strings, and containers that were already
    /// empty in the input are part of the original (valid) document and are left untouched
    /// (e.g. the schema-required <c>queryName</c> may legitimately be an empty string).
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
        /// Recursively strips module-scoped keys, mutating in place, and prunes containers that the
        /// stripping emptied. Containers that were already empty in the input are kept so that a
        /// schema-valid input stays schema-valid. Returns true if this token or any descendant was
        /// modified by minification.
        /// </summary>
        private static bool Process(JToken token, ISet<GdstModule> allowedModules)
        {
            bool modified = false;
            switch (token)
            {
                case JObject obj:
                    foreach (var prop in obj.Properties().ToList())
                    {
                        var module = ModuleRegistry.GetModule(prop.Name);
                        if (module.HasValue && !allowedModules.Contains(module.Value))
                        {
                            prop.Remove();
                            modified = true;
                            continue;
                        }

                        bool valueModified = false;
                        if (prop.Value is JArray list && IsSourceOrDestinationList(prop.Name))
                        {
                            valueModified |= FilterSourceDestEntries(list, allowedModules);
                        }
                        if (prop.Value is JArray classifications && IsClassificationList(prop.Name))
                        {
                            valueModified |= FilterClassificationEntries(classifications, prop.Name, allowedModules);
                        }
                        if (prop.Value is JObject certList && IsCertificationList(prop.Name))
                        {
                            valueModified |= FilterCertificationEntries(certList, allowedModules);
                        }
                        valueModified |= Process(prop.Value, allowedModules);

                        if (valueModified && IsEmptyContainer(prop.Value))
                        {
                            prop.Remove();
                        }
                        modified |= valueModified;
                    }
                    return modified;

                case JArray arr:
                    foreach (var item in arr.ToList())
                    {
                        bool itemModified = Process(item, allowedModules);
                        if (itemModified && IsEmptyContainer(item))
                        {
                            item.Remove();
                        }
                        modified |= itemModified;
                    }
                    return modified;

                default:
                    return false;
            }
        }

        /// <summary>
        /// True when the token is an object or array with no remaining content.
        /// </summary>
        private static bool IsEmptyContainer(JToken token)
        {
            return (token is JObject obj && obj.Count == 0) || (token is JArray arr && arr.Count == 0);
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

        private static bool IsCertificationList(string name)
        {
            return string.Equals(name, "cbvmda:certificationList", System.StringComparison.OrdinalIgnoreCase);
        }

        /// <summary>
        /// Removes certification entries whose <c>gdst:certificationType</c> is owned by a module not
        /// in the allowed set. Entries with an unknown or missing type are kept (see
        /// <see cref="ModuleRegistry.GetModuleForCertificationType"/>). When every entry is stripped,
        /// the emptied <c>certification</c> property is removed too, so the empty-container pruning
        /// later collapses the whole <c>cbvmda:certificationList</c> object. Returns true if any entry
        /// was removed.
        /// </summary>
        private static bool FilterCertificationEntries(JObject certList, ISet<GdstModule> allowedModules)
        {
            bool removed = false;
            if (certList["certification"] is JArray certifications)
            {
                foreach (var item in certifications.ToList())
                {
                    if (item is JObject entry)
                    {
                        string? certType = entry["gdst:certificationType"]?.Value<string>();
                        var module = ModuleRegistry.GetModuleForCertificationType(certType);
                        if (module.HasValue && !allowedModules.Contains(module.Value))
                        {
                            item.Remove();
                            removed = true;
                        }
                    }
                }

                // Remove the emptied array so the parent certificationList object collapses via the
                // empty-container pruning, keeping the "prune only what minification emptied" invariant.
                if (removed && certifications.Count == 0)
                {
                    certifications.Parent?.Remove();
                }
            }
            return removed;
        }

        /// <summary>
        /// Removes classification entries whose <c>value</c> is owned by a module not in the allowed
        /// set. The value maps are strict allow-lists (see <see cref="ModuleRegistry"/>): unknown values
        /// are stripped too, so each tier is served exactly the classifications its modules define
        /// (e.g. Core keeps only <c>land facility</c> locations and no product classifications). The
        /// empty-container pruning later removes the whole list when every entry was stripped.
        /// Returns true if any entry was removed.
        /// </summary>
        private static bool FilterClassificationEntries(JArray list, string propertyName, ISet<GdstModule> allowedModules)
        {
            bool removed = false;
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
                        removed = true;
                    }
                }
            }
            return removed;
        }

        /// <summary>
        /// Removes source/destination list entries whose <c>type</c> value is owned by a module not in
        /// the allowed set. Entries with a Core type (e.g. <c>location</c>) are always kept. The
        /// empty-container pruning later removes the whole list only if every entry was stripped.
        /// Returns true if any entry was removed.
        /// </summary>
        private static bool FilterSourceDestEntries(JArray list, ISet<GdstModule> allowedModules)
        {
            bool removed = false;
            foreach (var item in list.ToList())
            {
                if (item is JObject entry)
                {
                    string? type = entry["type"]?.Value<string>();
                    var module = ModuleRegistry.GetModuleForSourceDestinationType(type);
                    if (module.HasValue && !allowedModules.Contains(module.Value))
                    {
                        item.Remove();
                        removed = true;
                    }
                }
            }
            return removed;
        }
    }
}
