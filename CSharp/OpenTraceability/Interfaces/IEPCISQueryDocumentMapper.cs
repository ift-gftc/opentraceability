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
        EPCISQueryDocument Map(string strValue, bool checkSchema = true);

        /// <summary>
        /// Maps a string value into an EPCIS query document.
        /// </summary>
        /// <param name="strValue">The string value representing the EPCIS query document.</param>
        /// <returns>An EPCIS query document.</returns>
        Task<EPCISQueryDocument> MapAsync(string strValue, bool checkSchema = true);

        /// <summary>
        /// Maps an EPCIS document into a string value.
        /// </summary>
        /// <param name="doc">The EPCIS Query Document to map into the string value.</param>
        /// <param name="checkSchema">Whether the schema should be validated when mapping the document.</param>
        /// <returns>A string value representing the EPCIS Query document.</returns>
        string Map(EPCISQueryDocument doc, bool checkSchema = true);

        /// <summary>
        /// Maps an EPCIS query document into a string value.
        /// </summary>
        /// <param name="doc">The EPCIS Query Document to map into the string value.</param>
        /// <param name="checkSchema">Whether the schema should be validated when mapping the document.</param>
        /// <returns>A string value representing the EPCIS query document.</returns>
        Task<string> MapAsync(EPCISQueryDocument doc, bool checkSchema = true);
    }
}