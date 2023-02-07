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
    public interface IEPCISDocumentMapper
    {
        /// <summary>
        /// Maps a string value into an EPCIS document.
        /// </summary>
        /// <param name="strValue">The string value representing the EPCIS document.</param>
        /// <returns>An EPCIS document.</returns>
        public EPCISDocument Map(string strValue);

        /// <summary>
        /// Maps an EPCIS document into a string value.
        /// </summary>
        /// <param name="doc">The EPCIS Document to map into the string value.</param>
        /// <returns>A string value representing the EPCIS document.</returns>
        public string Map(EPCISDocument doc);
    }
}
