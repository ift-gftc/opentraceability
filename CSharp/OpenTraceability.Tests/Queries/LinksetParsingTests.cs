using Newtonsoft.Json;
using OpenTraceability;
using OpenTraceability.Models.MasterData;

namespace OpenTraceability.Tests.Queries
{
    /// <summary>
    /// Unit tests for parsing an RFC 9264 linkset (the <see cref="ResolverVersion.ResolverStandard_1_2_0"/>
    /// resolver response) into the core <see cref="Linkset"/> model, and selecting links by their full
    /// GS1 Web Vocabulary link relation type URI.
    /// </summary>
    [TestFixture]
    [Category("UnitTest")]
    public class LinksetParsingTests
    {
        // A representative slim-profile linkset: one anchor exposing a default link (master data),
        // the descriptive master data link, and an EPCIS link carrying optional metadata.
        private const string SampleLinkset = @"{
  ""linkset"": [
    {
      ""anchor"": ""https://id.gs1.org/01/09506000134352"",
      ""itemDescription"": ""Test Item"",
      ""https://ref.gs1.org/voc/defaultLink"": [ { ""href"": ""https://example.org/masterdata/product/09506000134352"", ""title"": ""Default Master Data"" } ],
      ""https://ref.gs1.org/voc/masterData"": [ { ""href"": ""https://example.org/masterdata/product/09506000134352"", ""title"": ""Master Data"" } ],
      ""https://ref.gs1.org/voc/epcis"": [ { ""href"": ""https://example.org/epcis"", ""title"": ""EPCIS Repository"", ""type"": ""application/json"", ""hreflang"": [""en""], ""context"": [""us""] } ]
    }
  ]
}";

        [Test]
        public void Deserialize_SampleLinkset_ParsesAnchorAndItem()
        {
            // Act
            Linkset? linkset = JsonConvert.DeserializeObject<Linkset>(SampleLinkset);

            // Assert
            Assert.That(linkset, Is.Not.Null);
            Assert.That(linkset!.linkset, Has.Count.EqualTo(1));
            Assert.That(linkset.linkset[0].anchor, Is.EqualTo("https://id.gs1.org/01/09506000134352"));
            Assert.That(linkset.linkset[0].itemDescription, Is.EqualTo("Test Item"));
        }

        [Test]
        public void GetLinks_EpcisUri_ReturnsHrefAndMetadata()
        {
            // Arrange
            LinksetItem item = JsonConvert.DeserializeObject<Linkset>(SampleLinkset)!.linkset[0];

            // Act
            var epcisLinks = item.GetLinks(DigitalLinkVocab.EpcisUri);

            // Assert
            Assert.That(epcisLinks, Has.Count.EqualTo(1));
            Assert.That(epcisLinks[0].href, Is.EqualTo("https://example.org/epcis"));
            Assert.That(epcisLinks[0].type, Is.EqualTo("application/json"));
            Assert.That(epcisLinks[0].hreflang, Does.Contain("en"));
            Assert.That(epcisLinks[0].context, Does.Contain("us"));
        }

        [Test]
        public void GetLinks_MasterDataAndDefaultLink_ReturnExpectedHrefs()
        {
            // Arrange
            LinksetItem item = JsonConvert.DeserializeObject<Linkset>(SampleLinkset)!.linkset[0];

            // Act
            var masterDataLinks = item.GetLinks(DigitalLinkVocab.MasterDataUri);
            var defaultLinks = item.GetLinks(DigitalLinkVocab.DefaultLinkUri);

            // Assert
            Assert.That(masterDataLinks, Has.Count.EqualTo(1));
            Assert.That(masterDataLinks[0].href, Is.EqualTo("https://example.org/masterdata/product/09506000134352"));
            Assert.That(defaultLinks, Has.Count.EqualTo(1));
            Assert.That(defaultLinks[0].href, Is.EqualTo("https://example.org/masterdata/product/09506000134352"));
        }

        [Test]
        public void GetLinks_UnknownLinkType_ReturnsEmpty()
        {
            // Arrange
            LinksetItem item = JsonConvert.DeserializeObject<Linkset>(SampleLinkset)!.linkset[0];

            // Act & Assert
            Assert.That(item.GetLinks("https://ref.gs1.org/voc/pip"), Is.Empty);
        }
    }
}
