using OpenTraceability.Utility;
using System;

namespace OpenTraceability.Tests.Events
{
    /// <summary>
    /// Tests pinning the comparison and null-handling behavior of <see cref="Measurement"/> after the
    /// nullable-annotation cleanup, including the corrected CompareTo(object) logic (previously every
    /// non-null argument compared as 1) and the corrected GetUniquenessKey guard (previously CS0472
    /// dead code comparing a double to null).
    /// </summary>
    [TestFixture]
    [Category("UnitTest")]
    public class MeasurementTests
    {
        /// <summary>
        /// CompareTo must order measurements by value: smaller is -1, equal is 0, greater is 1.
        /// </summary>
        [Test]
        public void CompareTo_Measurements_OrdersByValue()
        {
            // Arrange
            Measurement one = new Measurement(1, "KGM");
            Measurement two = new Measurement(2, "KGM");
            Measurement alsoTwo = new Measurement(2, "KGM");

            // Assert
            Assert.That(one.CompareTo(two), Is.EqualTo(-1));
            Assert.That(two.CompareTo(one), Is.EqualTo(1));
            Assert.That(two.CompareTo(alsoTwo), Is.EqualTo(0));
        }

        /// <summary>
        /// Any measurement must compare greater than null, per the standard IComparable convention.
        /// </summary>
        [Test]
        public void CompareTo_Null_ReturnsOne()
        {
            // Arrange
            Measurement m = new Measurement(1, "KGM");

            // Assert
            Assert.That(m.CompareTo((Measurement?)null), Is.EqualTo(1));
            Assert.That(m.CompareTo((object?)null), Is.EqualTo(1));
        }

        /// <summary>
        /// CompareTo(object) must dispatch to the typed comparison for measurements and reject other types.
        /// </summary>
        [Test]
        public void CompareTo_Object_DispatchesToTypedComparison()
        {
            // Arrange
            Measurement one = new Measurement(1, "KGM");
            Measurement two = new Measurement(2, "KGM");

            // Assert
            Assert.That(one.CompareTo((object)two), Is.EqualTo(-1));
            Assert.That(() => one.CompareTo("not a measurement"), Throws.Exception);
        }

        /// <summary>
        /// The equality operators must handle every null combination without throwing.
        /// </summary>
        [Test]
        public void EqualityOperators_NullCombinations_BehaveConsistently()
        {
            // Arrange
            Measurement a = new Measurement(5, "KGM");
            Measurement a2 = new Measurement(5, "KGM");
            Measurement? nullMeasurement = null;

            // Assert
            Assert.That(nullMeasurement == null, Is.True);
            Assert.That(nullMeasurement != null, Is.False);
            Assert.That(a == null, Is.False);
            Assert.That(a != null, Is.True);
            Assert.That(a == a2, Is.True);
            Assert.That(a != a2, Is.False);
        }

        /// <summary>
        /// TryParse must return null for unparsable input and a measurement for valid input.
        /// </summary>
        [Test]
        public void TryParse_ValidAndInvalidInput_ReturnsMeasurementOrNull()
        {
            // Act
            Measurement? valid = Measurement.TryParse("5 KGM");
            Measurement? invalid = Measurement.TryParse("garbage");
            Measurement? empty = Measurement.TryParse(null);

            // Assert
            Assert.That(valid, Is.Not.Null);
            Assert.That(valid!.Value, Is.EqualTo(5));
            Assert.That(invalid, Is.Null);
            Assert.That(empty, Is.Null);
        }

        /// <summary>
        /// A measurement with a unit must produce a non-empty uniqueness key based on its base unit.
        /// </summary>
        [Test]
        public void GetUniquenessKey_WithUnit_ReturnsBaseUnitKey()
        {
            // Arrange
            Measurement m = new Measurement(5, "KGM");

            // Act
            string key = m.GetUniquenessKey(1);

            // Assert
            Assert.That(key, Is.Not.Null.And.Not.Empty);
            Assert.That(key, Is.EqualTo(m.ToBase().ToString().Trim()));
        }
    }
}
