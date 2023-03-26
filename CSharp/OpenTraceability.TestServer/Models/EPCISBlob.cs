using System;
using OpenTraceability.Interfaces;
using OpenTraceability.Mappers;
using OpenTraceability.Models.Events;

namespace OpenTraceability.TestServer.Models
{
    /// <summary>
    /// Represents an EPCIS blob stored into the database.
    /// </summary>
    public class EPCISBlob
	{
        /// <summary>
        /// A UUID for the blob.
        /// </summary>
        public string ID { get; set; } = string.Empty;

        /// <summary>
        /// The EPCIS version of the data stored.
        /// </summary>
        public EPCISVersion Version { get; set; }

        /// <summary>
        /// The data format of the blob.
        /// </summary>
        public EPCISDataFormat Format { get; set; }

        /// <summary>
        /// The raw data for the EPCISDocument.
        /// </summary>
        public string RawData { get; set; } = string.Empty;

        /// <summary>
        /// The UTC datetime of when the blob was saved.
        /// </summary>
        public DateTime Created { get; set; } = DateTime.UtcNow;

        /// <summary>
        /// Convert the RawData, Version, and Format property into an EPCISDocument.
        /// </summary>
        /// <returns></returns>
        public EPCISDocument ToEPCISDocument()
        {
            IEPCISDocumentMapper mapper = OpenTraceabilityMappers.EPCISDocument.XML;
            if (Format == EPCISDataFormat.JSON)
            {
                mapper = OpenTraceabilityMappers.EPCISDocument.JSON;
            }

            var doc = mapper.Map(RawData);
            return doc;
        }
    }
}

