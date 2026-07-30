using OpenTraceability.Models.Identifiers;
using OpenTraceability.Utility;
using System;

namespace OpenTraceability.Tests.Identifiers
{
    /// <summary>
    /// Tests pinning the nullability contracts of the identifier types (EPC, GTIN, GLN, PGLN) and
    /// <see cref="Measurement"/> after the nullable-annotation cleanup: TryParse out-parameter guarantees,
    /// the null-means-valid DetectIssue convention that IsGLN/IsPGLN/IsGTIN rely on, and the
    /// null-handling of the equality operators and comparisons.
    /// </summary>
    [TestFixture]
    [Category("UnitTest")]
    public class IdentifierContractTests
    {
        private const string ValidEPC = "urn:epc:id:sgtin:0614141.107346.2017";
        private const string ValidGTIN = "urn:epc:idpat:sgtin:08600031303.00";
        private const string ValidGLN = "urn:test:location:loc:1234.567";
        private const string ValidPGLN = "urn:test:party:1234";
        private const string Invalid = "not-a-valid-identifier ";

        // -- TryParse: valid input yields a non-null identifier and a null error --

        /// <summary>
        /// TryParse on a valid identifier must return true with a non-null value and a null error.
        /// </summary>
        [Test]
        public void TryParse_ValidIdentifiers_ReturnsTrueWithValueAndNullError()
        {
            // Act & Assert
            Assert.That(EPC.TryParse(ValidEPC, out EPC? epc, out string? epcError), Is.True);
            Assert.That(epc, Is.Not.Null);
            Assert.That(epcError, Is.Null);

            Assert.That(GTIN.TryParse(ValidGTIN, out GTIN? gtin, out string? gtinError), Is.True);
            Assert.That(gtin, Is.Not.Null);
            Assert.That(gtinError, Is.Null);

            Assert.That(GLN.TryParse(ValidGLN, out GLN? gln, out string? glnError), Is.True);
            Assert.That(gln, Is.Not.Null);
            Assert.That(glnError, Is.Null);

            Assert.That(PGLN.TryParse(ValidPGLN, out PGLN? pgln, out string? pglnError), Is.True);
            Assert.That(pgln, Is.Not.Null);
            Assert.That(pglnError, Is.Null);
        }

        /// <summary>
        /// TryParse on an invalid identifier must return false with a null value and a non-null error.
        /// </summary>
        [Test]
        public void TryParse_InvalidIdentifiers_ReturnsFalseWithNullValueAndError()
        {
            // Act & Assert
            Assert.That(EPC.TryParse(Invalid, out EPC? epc, out string? epcError), Is.False);
            Assert.That(epc, Is.Null);
            Assert.That(epcError, Is.Not.Null.And.Not.Empty);

            Assert.That(GTIN.TryParse(Invalid, out GTIN? gtin, out string? gtinError), Is.False);
            Assert.That(gtin, Is.Null);
            Assert.That(gtinError, Is.Not.Null.And.Not.Empty);

            Assert.That(GLN.TryParse(Invalid, out GLN? gln, out string? glnError), Is.False);
            Assert.That(gln, Is.Null);
            Assert.That(glnError, Is.Not.Null.And.Not.Empty);

            Assert.That(PGLN.TryParse(Invalid, out PGLN? pgln, out string? pglnError), Is.False);
            Assert.That(pgln, Is.Null);
            Assert.That(pglnError, Is.Not.Null.And.Not.Empty);
        }

        // -- DetectIssue: null still means valid, so the Is* helpers keep working --

        /// <summary>
        /// DetectIssue must return null for a valid identifier (the null-means-valid contract that
        /// IsGLN/IsPGLN/IsGTIN depend on) and a message for an invalid or null identifier.
        /// </summary>
        [Test]
        public void DetectIssue_NullMeansValidContract_IsPreserved()
        {
            // Assert - valid identifiers report no issue
            Assert.That(GLN.DetectGLNIssue(ValidGLN), Is.Null);
            Assert.That(PGLN.DetectPGLNIssue(ValidPGLN), Is.Null);
            Assert.That(GTIN.DetectGTINIssue(ValidGTIN), Is.Null);

            // Assert - invalid and null identifiers report an issue
            Assert.That(GLN.DetectGLNIssue(Invalid), Is.Not.Null);
            Assert.That(PGLN.DetectPGLNIssue(Invalid), Is.Not.Null);
            Assert.That(EPC.DetectEPCIssue(null), Is.Not.Null);
            Assert.That(GLN.DetectGLNIssue(null), Is.Not.Null);
            Assert.That(PGLN.DetectPGLNIssue(null), Is.Not.Null);
        }

        /// <summary>
        /// The Is* helpers must return true for valid identifiers and false for invalid ones.
        /// </summary>
        [Test]
        public void IsHelpers_ValidAndInvalidIdentifiers_ReturnExpectedResult()
        {
            Assert.That(GLN.IsGLN(ValidGLN), Is.True);
            Assert.That(GLN.IsGLN(Invalid), Is.False);

            Assert.That(PGLN.IsPGLN(ValidPGLN), Is.True);
            Assert.That(PGLN.IsPGLN(Invalid), Is.False);

            Assert.That(GTIN.IsGTIN(ValidGTIN), Is.True);
            Assert.That(GTIN.IsGTIN(Invalid), Is.False);
        }

        // -- Equality operators: null combinations --

        /// <summary>
        /// The GLN equality operators must handle every null combination consistently
        /// (this pins the fix for the copy-paste bug previously masked by a CS8602 pragma).
        /// </summary>
        [Test]
        public void GLNOperators_NullCombinations_BehaveConsistently()
        {
            // Arrange
            GLN a = new GLN(ValidGLN);
            GLN a2 = new GLN(ValidGLN);
            GLN b = new GLN("urn:test:location:loc:9999.111");
            GLN? nullGLN = null;

            // Assert
            Assert.That(nullGLN == null, Is.True);
            Assert.That(nullGLN != null, Is.False);
            Assert.That(a == null, Is.False);
            Assert.That(a != null, Is.True);
            Assert.That(null == a, Is.False);
            Assert.That(null != a, Is.True);
            Assert.That(a == a2, Is.True);
            Assert.That(a != a2, Is.False);
            Assert.That(a == b, Is.False);
            Assert.That(a != b, Is.True);
        }

        /// <summary>
        /// The PGLN equality operators must handle every null combination consistently with GLN.
        /// </summary>
        [Test]
        public void PGLNOperators_NullCombinations_BehaveConsistently()
        {
            // Arrange
            PGLN a = new PGLN(ValidPGLN);
            PGLN a2 = new PGLN(ValidPGLN);
            PGLN b = new PGLN("urn:test:party:9999");
            PGLN? nullPGLN = null;

            // Assert
            Assert.That(nullPGLN == null, Is.True);
            Assert.That(nullPGLN != null, Is.False);
            Assert.That(a == null, Is.False);
            Assert.That(a != null, Is.True);
            Assert.That(null == a, Is.False);
            Assert.That(null != a, Is.True);
            Assert.That(a == a2, Is.True);
            Assert.That(a != a2, Is.False);
            Assert.That(a == b, Is.False);
            Assert.That(a != b, Is.True);
        }

        /// <summary>
        /// The EPC equality operators must handle every null combination consistently.
        /// </summary>
        [Test]
        public void EPCOperators_NullCombinations_BehaveConsistently()
        {
            // Arrange
            EPC a = new EPC(ValidEPC);
            EPC a2 = new EPC(ValidEPC);
            EPC b = new EPC("urn:epc:id:sgtin:0614141.107346.2018");
            EPC? nullEPC = null;

            // Assert
            Assert.That(nullEPC == null, Is.True);
            Assert.That(nullEPC != null, Is.False);
            Assert.That(a == null, Is.False);
            Assert.That(a != null, Is.True);
            Assert.That(a == a2, Is.True);
            Assert.That(a != a2, Is.False);
            Assert.That(a == b, Is.False);
            Assert.That(a != b, Is.True);
        }

        /// <summary>
        /// An EPC of type URI or SSCC never assigns the GTIN, so the property must be null rather
        /// than a default value.
        /// </summary>
        [Test]
        public void EPC_UriType_HasNullGTIN()
        {
            // Act
            EPC epc = new EPC("https://example.com/some/uri");

            // Assert
            Assert.That(epc.Type, Is.EqualTo(EPCType.URI));
            Assert.That(epc.GTIN, Is.Null);
            Assert.That(epc.SerialLotNumber, Is.Null);
        }
    }
}
