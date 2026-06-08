using OpenTraceability.GDST;

namespace OpenTraceability.TestServer.Core.Modules
{
    /// <summary>
    /// Helpers for working with the set of modules a server supports.
    /// </summary>
    public static class ModuleSet
    {
        /// <summary>
        /// Expands a configured set of modules by applying the implied-module rules:
        /// Core is always present, and Wildcaught/Aquaculture both imply Seafood.
        /// </summary>
        public static HashSet<GdstModule> Expand(IEnumerable<GdstModule>? configured)
        {
            var set = new HashSet<GdstModule> { GdstModule.Core };
            if (configured != null)
            {
                foreach (var m in configured)
                {
                    set.Add(m);
                }
            }
            if (set.Contains(GdstModule.Wildcaught) || set.Contains(GdstModule.Aquaculture))
            {
                set.Add(GdstModule.Seafood);
            }
            return set;
        }

        /// <summary>
        /// Parses module names (case-insensitive) into the enum, ignoring unknown values.
        /// </summary>
        public static HashSet<GdstModule> Parse(IEnumerable<string>? names)
        {
            var modules = new List<GdstModule>();
            if (names != null)
            {
                foreach (var name in names)
                {
                    if (Enum.TryParse<GdstModule>(name?.Trim(), ignoreCase: true, out var m))
                    {
                        modules.Add(m);
                    }
                }
            }
            return Expand(modules);
        }
    }
}
