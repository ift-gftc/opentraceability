// Polyfill for the nullable-analysis attributes that are missing from netstandard2.0.
// The C# compiler matches these attributes by their full name, so declaring them here
// gives this library the same nullable flow-analysis annotations that newer target
// frameworks get from the BCL. They are internal so they never conflict with the real
// attributes when a consumer targets a framework that already ships them.
namespace System.Diagnostics.CodeAnalysis
{
    /// <summary>
    /// Specifies that when the method returns the given value, the parameter will not be null even if the corresponding type allows it.
    /// </summary>
    [AttributeUsage(AttributeTargets.Parameter, Inherited = false)]
    internal sealed class NotNullWhenAttribute : Attribute
    {
        /// <summary>
        /// Creates the attribute with the specified return value condition.
        /// </summary>
        /// <param name="returnValue">The method return value for which the parameter is guaranteed to be non-null.</param>
        public NotNullWhenAttribute(bool returnValue)
        {
            ReturnValue = returnValue;
        }

        /// <summary>
        /// Gets the return value condition.
        /// </summary>
        public bool ReturnValue { get; }
    }

    /// <summary>
    /// Specifies that when the method returns the given value, the parameter may be null even if the corresponding type disallows it.
    /// </summary>
    [AttributeUsage(AttributeTargets.Parameter, Inherited = false)]
    internal sealed class MaybeNullWhenAttribute : Attribute
    {
        /// <summary>
        /// Creates the attribute with the specified return value condition.
        /// </summary>
        /// <param name="returnValue">The method return value for which the parameter may be null.</param>
        public MaybeNullWhenAttribute(bool returnValue)
        {
            ReturnValue = returnValue;
        }

        /// <summary>
        /// Gets the return value condition.
        /// </summary>
        public bool ReturnValue { get; }
    }
}
