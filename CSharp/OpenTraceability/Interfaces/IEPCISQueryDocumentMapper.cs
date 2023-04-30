using OpenTraceability.Models.Events;
using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;

namespace OpenTraceability.Interfaces
{
    /// <summary>
    /// Interface that will map a string into an EPCIS Document and back.
    /// </summary>
    public interface IEPCISQueryDocumentMapper
    {
        /// <summary>
        /// Maps a string value into an EPCIS Query document.
        /// </summary>
        /// <param name="strValue">The string value representing the EPCIS Query document.</param>
        /// <returns>An EPCIS document.</returns>
        public EPCISQueryDocument Map(string strValue, bool checkSchema = true);

        /// <summary>
        /// Maps an EPCIS document into a string value.
        /// </summary>
        /// <param name="doc">The EPCIS Query Document to map into the string value.</param>
        /// <returns>A string value representing the EPCIS Query document.</returns>
        public string Map(EPCISQueryDocument doc);
    }
}