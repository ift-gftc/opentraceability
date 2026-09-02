using System;
using System.Collections.Generic;
using OpenTraceability.GDST;

namespace OpenTraceability.TestServer.Core.Models
{
    /// <summary>
    /// Strict module-name validation for API/manifest boundaries. ModuleSet.Parse silently ignores
    /// unknown names, which is fine for trusted config but wrong when a caller hands us a module
    /// list: a typo like "Wildcatch" would silently downgrade the dataset to core-only.
    /// </summary>
    public static class ModuleNames
    {
        /// <summary>
        /// Validates module names case-insensitively against the <see cref="GdstModule"/> enum.
        /// Returns false (with the offending names in <paramref name="invalid"/>) if any name is
        /// unknown. <paramref name="normalized"/> holds the canonical enum names, deduplicated,
        /// without expansion (Core is dropped — it is always implied).
        /// </summary>
        public static bool TryParseStrict(IEnumerable<string>? names, out List<string> normalized, out List<string> invalid)
        {
            normalized = new List<string>();
            invalid = new List<string>();

            if (names == null) return true;

            foreach (var name in names)
            {
                if (string.IsNullOrWhiteSpace(name)) continue;
                if (Enum.TryParse<GdstModule>(name.Trim(), ignoreCase: true, out var m))
                {
                    if (m != GdstModule.Core && !normalized.Contains(m.ToString()))
                    {
                        normalized.Add(m.ToString());
                    }
                }
                else
                {
                    invalid.Add(name.Trim());
                }
            }

            return invalid.Count == 0;
        }
    }
}
