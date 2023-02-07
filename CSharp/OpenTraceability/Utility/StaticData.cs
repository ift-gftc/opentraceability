using System;
using System.Collections.Generic;
using System.IO;
using System.Text;

namespace OpenTraceability.Utility
{
    /// <summary>
    /// Reads static data files from embedded resources.
    /// </summary>
    public class StaticData
    {
        /// <summary>
        /// Reads static data files from embedded resources.
        /// </summary>
        /// <param name="path">The name of the file in the OpenTraceability.Utility.Data folder.</param>
        /// <returns>The static data file contents as a string.</returns>
        /// <exception cref="Exception">Throws an exception if it fails to find the embedded resource file.</exception>
        public static string ReadData(string path)
        {
            string result = string.Empty;
            using (Stream? stream = typeof(StaticData).Assembly.GetManifestResourceStream("OpenTraceability.Utility.Data." + path))
            {
                if (stream == null)
                {
                    throw new Exception("Failed to read static data from embedded resource at path " + path);
                }

                using (StreamReader sr = new StreamReader(stream))
                {
                    result = sr.ReadToEnd();
                }
            }
            return result;
        }
    }
}
