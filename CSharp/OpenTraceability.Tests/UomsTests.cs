using NUnit.Framework;
using OpenTraceability.Utility;

namespace OpenTraceability.Tests
{
    [TestFixture]
    public class UomsTests
    {
        [Test]
        public void ParsesCase()
        {
            var uom = UOM.ParseFromName("CA");

            Assert.That(uom.UNCode, Is.EqualTo("CA"));
            Assert.That(uom.UnitDimension, Is.EqualTo("count"));
        }
    }
}
