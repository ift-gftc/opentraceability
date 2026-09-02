using Newtonsoft.Json.Linq;
using NUnit.Framework.Internal;
using OpenTraceability.GDST.MasterData;
using OpenTraceability.Interfaces;
using OpenTraceability.Mappers;
using OpenTraceability.Models.Events;
using OpenTraceability.Models.MasterData;
using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;

namespace OpenTraceability.Tests.Events
{
    [TestFixture]
    [Category("UnitTest")]
    public class MasterDataTests
    {
        [Test]
        [TestCase("gs1-vocab-products01.json", VocabularyType.Tradeitem)]
        public void GS1WebVocab_VocabularyElement_Deserializes(string file, VocabularyType vocabularyType)
        {
            // read object events from test data specified in the file argument
            string json = OpenTraceabilityTests.ReadTestData(file);

            // map into a trade item
            VocabularyElement element = (VocabularyElement)OpenTraceabilityMappers.MasterData.GS1WebVocab.Map<VocabularyElement>(json);
            Assert.That(element.VocabularyType == vocabularyType);
        }

        [Test]
        [TestCase("gs1-vocab-products01.json")]
        public void GS1WebVocab_Products(string file)
        {
            // read object events from test data specified in the file argument
            string json = OpenTraceabilityTests.ReadTestData(file);

            // map into a trade item
            Tradeitem tradeitem = (Tradeitem)OpenTraceabilityMappers.MasterData.GS1WebVocab.Map<Tradeitem>(json);

            // map back into json
            string jsonAfter = OpenTraceabilityMappers.MasterData.GS1WebVocab.Map(tradeitem);

            // compare the JSON
            OpenTraceabilityTests.CompareJSON(json, jsonAfter);
        }

        [Test]
        [TestCase("gs1-vocab-locations01.json")]
        public void GS1WebVocab_Location(string file)
        {
            // read object events from test data specified in the file argument
            string json = OpenTraceabilityTests.ReadTestData(file);

            // map into a trade item
            GDSTLocation loc = (GDSTLocation)OpenTraceabilityMappers.MasterData.GS1WebVocab.Map<GDSTLocation>(json);

            // map back into json
            string jsonAfter = OpenTraceabilityMappers.MasterData.GS1WebVocab.Map(loc);

            // compare the JSON
            OpenTraceabilityTests.CompareJSON(json, jsonAfter);
        }

        [Test]
        [TestCase("gs1-vocab-tradingparties01.json")]
        public void GS1WebVocab_TradingParty(string file)
        {
            // read object events from test data specified in the file argument
            string json = OpenTraceabilityTests.ReadTestData(file);

            // map into a trade item
            TradingParty tp = (TradingParty)OpenTraceabilityMappers.MasterData.GS1WebVocab.Map<TradingParty>(json);

            // map back into json
            string jsonAfter = OpenTraceabilityMappers.MasterData.GS1WebVocab.Map(tp);

            // compare the JSON
            OpenTraceabilityTests.CompareJSON(json, jsonAfter);
        }

        [Test]
        [TestCase("testserver_advancedfilters.jsonld")]
        public void GS1WebVocab_EPCISDocument(string file)
        {
            // read object events from test data specified in the file argument
            string json = OpenTraceabilityTests.ReadTestData(file);

            // map into a trade item
            EPCISDocument doc = OpenTraceabilityMappers.EPCISDocument.JSON.Map(json);

            // foreach master data item
            foreach (var md in doc.MasterData)
            {
                string jsonMD = OpenTraceabilityMappers.MasterData.GS1WebVocab.Map(md);

                IVocabularyElement mdAfter = OpenTraceabilityMappers.MasterData.GS1WebVocab.Map(md.GetType(), jsonMD);

                // map back into json
                string jsonMDAfter = OpenTraceabilityMappers.MasterData.GS1WebVocab.Map(mdAfter);

                // compare the JSON
                OpenTraceabilityTests.CompareJSON(jsonMD, jsonMDAfter);
            }
        }

        /// <summary>
        /// The geo property must round-trip through the model whether it holds a gs1:GeoCoordinates or a gs1:GeoShape.
        /// </summary>
        [Test]
        [TestCase("webvocab_location_geocoordinates.jsonld")]
        [TestCase("webvocab_location_geoshape.jsonld")]
        public void GS1WebVocab_Location_Geo_RoundTrips(string file)
        {
            // Arrange
            string json = OpenTraceabilityTests.ReadTestData(file);

            // Act
            IVocabularyElement loc = OpenTraceabilityMappers.MasterData.GS1WebVocab.Map<Location>(json);
            string jsonAfter = OpenTraceabilityMappers.MasterData.GS1WebVocab.Map(loc);

            // Assert
            OpenTraceabilityTests.CompareJSON(json, jsonAfter);
        }

        /// <summary>
        /// A geo property holding a gs1:GeoCoordinates must map its latitude/longitude into the typed model.
        /// </summary>
        [Test]
        public void GS1WebVocab_Location_GeoCoordinates_DeserializesTypedValues()
        {
            // Arrange
            string json = OpenTraceabilityTests.ReadTestData("webvocab_location_geocoordinates.jsonld");

            // Act
            Location loc = (Location)OpenTraceabilityMappers.MasterData.GS1WebVocab.Map<Location>(json);

            // Assert
            Assert.That(loc.Geo, Is.Not.Null, "The geo property should have been deserialized into the model.");
            Assert.That(loc.Geo!.JsonLDType, Is.EqualTo("gs1:GeoCoordinates"));
            Assert.That(loc.Geo.Latitude, Is.Not.Null);
            Assert.That(loc.Geo.Latitude!.Value, Is.EqualTo(51.5175264).Within(0.0000001));
            Assert.That(loc.Geo.Latitude.Type, Is.EqualTo("xsd:float"));
            Assert.That(loc.Geo.Longitude, Is.Not.Null);
            Assert.That(loc.Geo.Longitude!.Value, Is.EqualTo(-0.1134413).Within(0.0000001));
            Assert.That(loc.Geo.Longitude.Type, Is.EqualTo("xsd:float"));
            Assert.That(loc.Geo.Circle, Is.Null, "A gs1:GeoCoordinates should not populate any gs1:GeoShape properties.");
            Assert.That(loc.Geo.Line, Is.Null, "A gs1:GeoCoordinates should not populate any gs1:GeoShape properties.");
            Assert.That(loc.Geo.Polygon, Is.Null, "A gs1:GeoCoordinates should not populate any gs1:GeoShape properties.");
        }

        /// <summary>
        /// A geo property holding a gs1:GeoShape must map its polygon into the typed model.
        /// </summary>
        [Test]
        public void GS1WebVocab_Location_GeoShape_DeserializesTypedValues()
        {
            // Arrange
            string json = OpenTraceabilityTests.ReadTestData("webvocab_location_geoshape.jsonld");

            // Act
            Location loc = (Location)OpenTraceabilityMappers.MasterData.GS1WebVocab.Map<Location>(json);

            // Assert
            Assert.That(loc.Geo, Is.Not.Null, "The geo property should have been deserialized into the model.");
            Assert.That(loc.Geo!.JsonLDType, Is.EqualTo("gs1:GeoShape"));
            Assert.That(loc.Geo.Polygon, Is.EqualTo("51.5175264 -0.1134413 51.5180011 -0.1140215 51.5170598 -0.1145322 51.5175264 -0.1134413"));
            Assert.That(loc.Geo.Circle, Is.Null);
            Assert.That(loc.Geo.Line, Is.Null);
            Assert.That(loc.Geo.Latitude, Is.Null, "A gs1:GeoShape should not populate any gs1:GeoCoordinates properties.");
            Assert.That(loc.Geo.Longitude, Is.Null, "A gs1:GeoShape should not populate any gs1:GeoCoordinates properties.");
        }

        /// <summary>
        /// A location read from an EPCIS document with only a geoLocation master data attribute must serve a web vocab
        /// definition whose geo property holds the equivalent gs1:GeoCoordinates.
        /// </summary>
        [Test]
        public void EPCISMasterData_GeoLocation_SerializesGeoCoordinatesInWebVocab()
        {
            // Arrange
            string json = OpenTraceabilityTests.ReadTestData("epcis_location_geo_masterdata.jsonld");
            EPCISDocument doc = OpenTraceabilityMappers.EPCISDocument.JSON.Map(json);
            Location loc = doc.MasterData.OfType<Location>().First(l => l.GLN?.ToString() == "urn:epc:id:sgln:08600031303.1.1");
            Assert.That(loc.GeoLocation, Is.EqualTo("geo:50.942239,6.89835"), "The geoLocation master data attribute should have been read verbatim.");

            // Act
            string webVocabJson = OpenTraceabilityMappers.MasterData.GS1WebVocab.Map(loc);
            JObject jVocab = JObject.Parse(webVocabJson);

            // Assert
            JToken? jGeo = jVocab["geo"];
            Assert.That(jGeo, Is.Not.Null, "The web vocab JSON should contain a geo property derived from the geoLocation attribute.");
            Assert.That(jGeo!["@type"]?.ToString(), Is.EqualTo("gs1:GeoCoordinates"));
            Assert.That((double)jGeo["latitude"]!["@value"]!, Is.EqualTo(50.942239).Within(0.0000001));
            Assert.That((double)jGeo["longitude"]!["@value"]!, Is.EqualTo(6.89835).Within(0.0000001));
        }

        /// <summary>
        /// A location read from an EPCIS document with both geoLocation and geoFence master data attributes must serve
        /// a web vocab definition whose geo property holds the gs1:GeoShape polygon (geoFence takes precedence), with
        /// the CBV [longitude, latitude] pairs swapped into the polygon's "lat lon" point order.
        /// </summary>
        [Test]
        public void EPCISMasterData_GeoFence_SerializesGeoShapeInWebVocab()
        {
            // Arrange
            string json = OpenTraceabilityTests.ReadTestData("epcis_location_geo_masterdata.jsonld");
            EPCISDocument doc = OpenTraceabilityMappers.EPCISDocument.JSON.Map(json);
            Location loc = doc.MasterData.OfType<Location>().First(l => l.GLN?.ToString() == "urn:epc:id:sgln:08600031303.1.2");
            Assert.That(loc.GeoFence, Is.Not.Null, "The geoFence master data attribute should have been read.");

            // Act
            string webVocabJson = OpenTraceabilityMappers.MasterData.GS1WebVocab.Map(loc);
            JObject jVocab = JObject.Parse(webVocabJson);

            // Assert
            JToken? jGeo = jVocab["geo"];
            Assert.That(jGeo, Is.Not.Null, "The web vocab JSON should contain a geo property derived from the geoFence attribute.");
            Assert.That(jGeo!["@type"]?.ToString(), Is.EqualTo("gs1:GeoShape"), "The geoFence should take precedence over the geoLocation.");
            Assert.That(jGeo["polygon"]?.ToString(), Is.EqualTo("50.942499 6.898247 50.942275 6.898292 50.942263 6.898094 50.942106 6.898126 50.94213 6.898526 50.942512 6.898451 50.942499 6.898247"));
        }

        /// <summary>
        /// The raw geoLocation and geoFence master data attribute strings must survive an EPCIS JSON round-trip
        /// unchanged; the derived geo values never replace the originals.
        /// </summary>
        [Test]
        public void EPCISMasterData_GeoAttributes_RoundTripThroughEPCISJson()
        {
            // Arrange
            string json = OpenTraceabilityTests.ReadTestData("epcis_location_geo_masterdata.jsonld");
            EPCISDocument doc = OpenTraceabilityMappers.EPCISDocument.JSON.Map(json);
            Location locBefore = doc.MasterData.OfType<Location>().First(l => l.GLN?.ToString() == "urn:epc:id:sgln:08600031303.1.2");

            // Act
            string jsonAfter = OpenTraceabilityMappers.EPCISDocument.JSON.Map(doc);
            EPCISDocument docAfter = OpenTraceabilityMappers.EPCISDocument.JSON.Map(jsonAfter);
            Location locAfter = docAfter.MasterData.OfType<Location>().First(l => l.GLN?.ToString() == "urn:epc:id:sgln:08600031303.1.2");

            // Assert
            Assert.That(locAfter.GeoLocation, Is.EqualTo(locBefore.GeoLocation), "The geoLocation attribute string should round-trip verbatim.");
            Assert.That(locAfter.GeoFence, Is.EqualTo(locBefore.GeoFence), "The geoFence attribute string should round-trip verbatim.");
        }

        /// <summary>
        /// Starting from a web vocab definition, converting into an EPCIS document, and converting back to web vocab
        /// must lose no geo data: the geo property derives the geoLocation/geoFence master data attributes on the way
        /// out and is derived back from them on the way in.
        /// </summary>
        [Test]
        [TestCase("webvocab_location_geocoordinates.jsonld", "urn:epcglobal:cbv:mda#geoLocation", "geo:51.5175264,-0.1134413")]
        [TestCase("webvocab_location_geoshape.jsonld", "urn:epcglobal:cbv:mda#geoFence", "[[-0.1134413,51.5175264],[-0.1140215,51.5180011],[-0.1145322,51.5170598],[-0.1134413,51.5175264]]")]
        public void GS1WebVocab_Geo_RoundTripsThroughEPCISDocument(string file, string expectedAttributeId, string expectedAttributeValue)
        {
            // Arrange
            string json = OpenTraceabilityTests.ReadTestData(file);
            Location locBefore = (Location)OpenTraceabilityMappers.MasterData.GS1WebVocab.Map<Location>(json);
            locBefore.GLN = new Models.Identifiers.GLN("urn:epc:id:sgln:08600031303.1.1");

            EPCISDocument doc = new EPCISDocument();
            doc.EPCISVersion = EPCISVersion.V2;
            doc.CreationDate = DateTimeOffset.UtcNow;
            doc.MasterData.Add(locBefore);

            // Act
            string epcisJson = OpenTraceabilityMappers.EPCISDocument.JSON.Map(doc);
            EPCISDocument docAfter = OpenTraceabilityMappers.EPCISDocument.JSON.Map(epcisJson);
            Location locAfter = docAfter.MasterData.OfType<Location>().First();

            // Assert - the EPCIS document carries the derived master data attribute.
            JObject jDoc = JObject.Parse(epcisJson);
            JArray jAttributes = (JArray)jDoc["epcisHeader"]!["epcisMasterData"]!["vocabularyList"]![0]!["vocabularyElementList"]![0]!["attributes"]!;
            JToken? jAttribute = jAttributes.FirstOrDefault(a => a["id"]?.ToString() == expectedAttributeId);
            Assert.That(jAttribute, Is.Not.Null, $"The EPCIS document should contain the derived {expectedAttributeId} attribute.");
            Assert.That(jAttribute!["attribute"]?.ToString(), Is.EqualTo(expectedAttributeValue));

            // Assert - the geo property survives the full circle unchanged.
            Assert.That(locAfter.Geo, Is.Not.Null, "The geo property should be derived back from the master data attributes.");
            Assert.That(locAfter.Geo!.JsonLDType, Is.EqualTo(locBefore.Geo!.JsonLDType));
            Assert.That(locAfter.Geo.Latitude?.Value, Is.EqualTo(locBefore.Geo.Latitude?.Value));
            Assert.That(locAfter.Geo.Longitude?.Value, Is.EqualTo(locBefore.Geo.Longitude?.Value));
            Assert.That(locAfter.Geo.Polygon, Is.EqualTo(locBefore.Geo.Polygon));
        }
    }
}
