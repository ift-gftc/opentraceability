using Microsoft.Extensions.Configuration;
using OpenTraceability.Mappers;
using OpenTraceability.Models.Events;
using OpenTraceability.TestServer.Models;
using OpenTraceability.TestServer.Services;

namespace OpenTraceability.Tests.Services
{
    [TestFixture]
    [Category("UnitTest")]
    public class EPCISBlobSqlLiteServiceTests
    {
        private EPCISBlobSqlLiteService _service;
        private IConfiguration _config;

        [SetUp]
        public void SetUp()
        {
            // Create a minimal configuration for the service
            var configBuilder = new ConfigurationBuilder();
            configBuilder.AddInMemoryCollection(new Dictionary<string, string?>
            {
                { "ConnectionStrings:sqlite", "Data Source=:memory:" }
            });
            _config = configBuilder.Build();
            _service = new EPCISBlobSqlLiteService(_config);
        }

        [Test]
        public async Task LoadDefaultBlob_WithValidId_ShouldReturnBlob()
        {
            // Arrange
            string id = "beef-leather-example";

            // Act
            var blob = await _service.LoadDefaultBlob(id);

            // Assert
            Assert.That(blob, Is.Not.Null, "Blob should not be null for valid ID");
            Assert.That(blob.ID, Is.EqualTo(id), "Blob ID should match requested ID");
            Assert.That(blob.Format, Is.EqualTo(EPCISDataFormat.JSON), "Blob format should be JSON");
            Assert.That(blob.Version, Is.EqualTo(EPCISVersion.V2), "Blob version should be V2");
            Assert.That(blob.RawData, Is.Not.Null.And.Not.Empty, "Blob RawData should not be empty");
            Assert.That(blob.Created, Is.Not.EqualTo(default(DateTime)), "Blob Created date should be set");
            Assert.That(blob.Created.Kind, Is.EqualTo(DateTimeKind.Utc), "Blob Created date should be in UTC");
        }

        [Test]
        public async Task LoadDefaultBlob_WithValidId_ShouldLoadValidEPCISDocument()
        {
            // Arrange
            string id = "beef-leather-example";

            // Act
            var blob = await _service.LoadDefaultBlob(id);

            // Assert
            Assert.That(blob, Is.Not.Null, "Blob should not be null");
            
            // Verify that the raw data can be parsed into an EPCIS document
            EPCISDocument? doc = null;
            Assert.DoesNotThrow(() => 
            {
                doc = OpenTraceabilityMappers.EPCISDocument.JSON.Map(blob.RawData);
            }, "Blob RawData should be valid EPCIS JSON");
            
            Assert.That(doc, Is.Not.Null, "Parsed EPCIS document should not be null");
        }

        [Test]
        public async Task LoadDefaultBlob_WithInvalidId_ShouldReturnNull()
        {
            // Arrange
            string invalidId = "non-existent-blob-id";

            // Act
            var blob = await _service.LoadDefaultBlob(invalidId);

            // Assert
            Assert.That(blob, Is.Null, "Blob should be null for non-existent ID");
        }

        [Test]
        public async Task LoadDefaultBlob_WithEmptyId_ShouldReturnNull()
        {
            // Arrange
            string emptyId = string.Empty;

            // Act
            var blob = await _service.LoadDefaultBlob(emptyId);

            // Assert
            Assert.That(blob, Is.Null, "Blob should be null for empty ID");
        }

        [Test]
        public async Task LoadDefaultBlob_MultipleCalls_ShouldReturnConsistentResults()
        {
            // Arrange
            string id = "beef-leather-example";

            // Act
            var blob1 = await _service.LoadDefaultBlob(id);
            var blob2 = await _service.LoadDefaultBlob(id);

            // Assert
            Assert.That(blob1, Is.Not.Null, "First blob should not be null");
            Assert.That(blob2, Is.Not.Null, "Second blob should not be null");
            Assert.That(blob1.ID, Is.EqualTo(blob2.ID), "Blob IDs should match");
            Assert.That(blob1.RawData, Is.EqualTo(blob2.RawData), "Blob RawData should match");
            Assert.That(blob1.Format, Is.EqualTo(blob2.Format), "Blob formats should match");
            Assert.That(blob1.Version, Is.EqualTo(blob2.Version), "Blob versions should match");
        }
    }
}
