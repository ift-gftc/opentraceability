using System;
using System.Collections.Generic;
using System.Text;

namespace OpenTraceability.Utility
{
    /// <summary>
    /// One reason a value at a given location failed validation, such as a single branch of an
    /// "anyOf". A location can accumulate several reasons.
    /// </summary>
    public class JsonSchemaValidationReason
    {
        public JsonSchemaValidationReason(string keyword, string message)
        {
            Keyword = keyword;
            Message = message;
        }

        /// <summary>The schema keyword that failed, for example "enum", "pattern" or "required".</summary>
        public string Keyword { get; }

        /// <summary>The message produced by the validator.</summary>
        public string Message { get; }
    }

    /// <summary>
    /// All the reasons reported against one location in the validated document.
    /// </summary>
    public class JsonSchemaValidationError
    {
        public JsonSchemaValidationError(
            string instanceLocation,
            string? value,
            IReadOnlyList<JsonSchemaValidationReason> reasons)
        {
            InstanceLocation = instanceLocation;
            Value = value;
            Reasons = reasons;
        }

        /// <summary>
        /// JSON Pointer to the offending element, for example "/bizStep". Empty for the document root.
        /// </summary>
        public string InstanceLocation { get; }

        /// <summary>
        /// The value found at that location, or null when the location holds an object or an array.
        /// </summary>
        public string? Value { get; }

        public IReadOnlyList<JsonSchemaValidationReason> Reasons { get; }

        /// <summary>
        /// Renders the location, the offending value and every reason beneath it.
        /// </summary>
        public override string ToString()
        {
            var text = new StringBuilder();

            text.Append(string.IsNullOrEmpty(InstanceLocation) ? "/" : InstanceLocation);

            if (Value != null)
            {
                text.Append(" — '").Append(Value).Append("'");
            }

            foreach (JsonSchemaValidationReason reason in Reasons)
            {
                text.AppendLine();
                text.Append("    ").Append(reason.Keyword).Append(": ").Append(reason.Message);
            }

            return text.ToString();
        }
    }

    /// <summary>
    /// The outcome of validating a document against a schema.
    /// </summary>
    public class JsonSchemaValidationResult
    {
        public JsonSchemaValidationResult(IReadOnlyList<JsonSchemaValidationError> errors, int totalErrorCount)
        {
            Errors = errors;
            TotalErrorCount = totalErrorCount;
        }

        public static JsonSchemaValidationResult Valid { get; } =
            new JsonSchemaValidationResult(Array.Empty<JsonSchemaValidationError>(), 0);

        public bool IsValid => Errors.Count == 0;

        /// <summary>The errors being reported, one entry per location.</summary>
        public IReadOnlyList<JsonSchemaValidationError> Errors { get; }

        /// <summary>
        /// How many errors were found in total. Equal to Errors.Count unless a cap was applied.
        /// </summary>
        public int TotalErrorCount { get; }
    }
}
