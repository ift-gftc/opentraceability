using System.ComponentModel.DataAnnotations;

namespace OpenTraceability.TestServer.Core.Data.Entities
{
    /// <summary>
    /// A stored master data vocabulary element (trade item / location / party), serialized as
    /// GS1 Web Vocab JSON-LD. <see cref="ElementType"/> is the assembly-qualified type name so the
    /// element can be deserialized back into the correct (possibly GDST-extended) type.
    /// </summary>
    public class MasterDataRecord
    {
        [Key]
        public long Id { get; set; }

        public string DatasetId { get; set; } = "default";

        /// <summary>
        /// The identifier of the element (GTIN / GLN / PGLN), lowercased.
        /// </summary>
        public string ElementId { get; set; } = string.Empty;

        /// <summary>
        /// The vocabulary type (tradeitem / location / party) for quick filtering.
        /// </summary>
        public string VocabularyType { get; set; } = string.Empty;

        /// <summary>
        /// The assembly-qualified C# type used to deserialize the element.
        /// </summary>
        public string ElementType { get; set; } = string.Empty;

        /// <summary>
        /// The GS1 Web Vocab JSON-LD of the element.
        /// </summary>
        public string ElementJson { get; set; } = string.Empty;
    }
}
