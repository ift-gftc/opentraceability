using OpenTraceability.Models.Common;
using OpenTraceability.Models.MasterData;

namespace OpenTraceability.Tests.Events
{
    /// <summary>
    /// Unit tests for the conversions between the GS1 Web Vocabulary <see cref="Geo"/> and the CBV geoLocation /
    /// geoFence master data attribute values, and for the derivation fallback on <see cref="Location"/>.
    /// </summary>
    [TestFixture]
    [Category("UnitTest")]
    public class GeoTests
    {
        // FromGeoLocation - parsing RFC 5870 geo URIs.

        /// <summary>
        /// A plain geo URI must parse into a gs1:GeoCoordinates with the latitude and longitude.
        /// </summary>
        [Test]
        public void FromGeoLocation_ValidGeoUri_ReturnsGeoCoordinates()
        {
            // Act
            Geo? geo = Geo.FromGeoLocation("geo:50.942239,6.89835");

            // Assert
            Assert.That(geo, Is.Not.Null);
            Assert.That(geo!.JsonLDType, Is.EqualTo("gs1:GeoCoordinates"));
            Assert.That(geo.Latitude!.Value, Is.EqualTo(50.942239).Within(0.0000001));
            Assert.That(geo.Longitude!.Value, Is.EqualTo(6.89835).Within(0.0000001));
            Assert.That(geo.Polygon, Is.Null);
        }

        /// <summary>
        /// An optional altitude component and URI parameters must be ignored; the prefix is case-insensitive.
        /// </summary>
        [Test]
        [TestCase("geo:48.2,16.3,183")]
        [TestCase("geo:48.2,16.3;u=35")]
        [TestCase("geo:48.2,16.3,183;crs=wgs84;u=35")]
        [TestCase("GEO:48.2,16.3")]
        public void FromGeoLocation_AltitudeParametersOrCasing_ParsesLatitudeAndLongitude(string uri)
        {
            // Act
            Geo? geo = Geo.FromGeoLocation(uri);

            // Assert
            Assert.That(geo, Is.Not.Null, $"The geo URI '{uri}' should parse.");
            Assert.That(geo!.Latitude!.Value, Is.EqualTo(48.2).Within(0.0000001));
            Assert.That(geo.Longitude!.Value, Is.EqualTo(16.3).Within(0.0000001));
        }

        /// <summary>
        /// Null, empty, and malformed values must derive nothing rather than throw.
        /// </summary>
        [Test]
        [TestCase(null)]
        [TestCase("")]
        [TestCase("   ")]
        [TestCase("50.942239,6.89835")]
        [TestCase("geo:")]
        [TestCase("geo:50.942239")]
        [TestCase("geo:abc,def")]
        public void FromGeoLocation_NullOrMalformed_ReturnsNull(string? uri)
        {
            // Act & Assert
            Assert.That(Geo.FromGeoLocation(uri), Is.Null);
        }

        // FromGeoFence - parsing CBV [longitude, latitude] coordinate arrays.

        /// <summary>
        /// A simple single ring must convert into a gs1:GeoShape polygon with the pairs swapped into "lat lon" order.
        /// </summary>
        [Test]
        public void FromGeoFence_SimpleRing_ReturnsGeoShapePolygon()
        {
            // Act
            Geo? geo = Geo.FromGeoFence("[[6.898247,50.942499],[6.898292,50.942275],[6.898247,50.942499]]");

            // Assert
            Assert.That(geo, Is.Not.Null);
            Assert.That(geo!.JsonLDType, Is.EqualTo("gs1:GeoShape"));
            Assert.That(geo.Polygon, Is.EqualTo("50.942499 6.898247 50.942275 6.898292 50.942499 6.898247"));
            Assert.That(geo.Latitude, Is.Null);
            Assert.That(geo.Longitude, Is.Null);
        }

        /// <summary>
        /// Integer coordinates are valid JSON numbers and must convert like floats.
        /// </summary>
        [Test]
        public void FromGeoFence_IntegerCoordinates_ReturnsGeoShapePolygon()
        {
            // Act
            Geo? geo = Geo.FromGeoFence("[[1,2],[3,4]]");

            // Assert
            Assert.That(geo, Is.Not.Null);
            Assert.That(geo!.Polygon, Is.EqualTo("2 1 4 3"));
        }

        /// <summary>
        /// Multi-polygons and holes cannot be represented by a gs1:GeoShape polygon and must derive nothing;
        /// malformed values must derive nothing rather than throw.
        /// </summary>
        [Test]
        [TestCase(null)]
        [TestCase("")]
        [TestCase("   ")]
        [TestCase("[]")]
        [TestCase("[[[6.898247,50.942499],[6.898292,50.942275]]]")]
        [TestCase("[[6.898247,50.942499,100],[6.898292,50.942275,100]]")]
        [TestCase("[[\"a\",\"b\"]]")]
        [TestCase("[6.898247,50.942499]")]
        [TestCase("{}")]
        [TestCase("not json")]
        public void FromGeoFence_NullNestedOrMalformed_ReturnsNull(string? geoFence)
        {
            // Act & Assert
            Assert.That(Geo.FromGeoFence(geoFence), Is.Null);
        }

        // ToGeoLocationUri / ToGeoFenceJson - deriving the master data attribute values.

        /// <summary>
        /// A gs1:GeoCoordinates must derive a geo URI and no geoFence.
        /// </summary>
        [Test]
        public void ToGeoLocationUri_GeoCoordinates_ReturnsGeoUri()
        {
            // Arrange
            Geo geo = new Geo { JsonLDType = "gs1:GeoCoordinates", Latitude = new WebVocabFloat { Value = 50.942239 }, Longitude = new WebVocabFloat { Value = 6.89835 } };

            // Act & Assert
            Assert.That(geo.ToGeoLocationUri(), Is.EqualTo("geo:50.942239,6.89835"));
            Assert.That(geo.ToGeoFenceJson(), Is.Null, "A gs1:GeoCoordinates cannot be represented as a geoFence.");
        }

        /// <summary>
        /// A gs1:GeoShape polygon must derive a geoFence JSON array with the points swapped back into
        /// [longitude, latitude] pair order, and no geo URI.
        /// </summary>
        [Test]
        public void ToGeoFenceJson_GeoShapePolygon_ReturnsCoordinateArray()
        {
            // Arrange
            Geo geo = new Geo { JsonLDType = "gs1:GeoShape", Polygon = "50.942499 6.898247 50.942275 6.898292 50.942499 6.898247" };

            // Act & Assert
            Assert.That(geo.ToGeoFenceJson(), Is.EqualTo("[[6.898247,50.942499],[6.898292,50.942275],[6.898247,50.942499]]"));
            Assert.That(geo.ToGeoLocationUri(), Is.Null, "A gs1:GeoShape cannot be represented as a geo URI.");
        }

        /// <summary>
        /// Incomplete or non-convertible geo values must derive nothing rather than throw.
        /// </summary>
        [Test]
        public void ToConversions_IncompleteGeo_ReturnNull()
        {
            // A coordinate without a longitude has no geo URI.
            Geo missingLongitude = new Geo { JsonLDType = "gs1:GeoCoordinates", Latitude = new WebVocabFloat { Value = 50.0 } };
            Assert.That(missingLongitude.ToGeoLocationUri(), Is.Null);

            // A shape without a polygon (e.g. circle-only) has no geoFence.
            Geo circleOnly = new Geo { JsonLDType = "gs1:GeoShape", Circle = "50.942239 6.89835 100" };
            Assert.That(circleOnly.ToGeoFenceJson(), Is.Null);

            // An odd number of polygon values cannot form coordinate pairs.
            Geo oddPolygon = new Geo { JsonLDType = "gs1:GeoShape", Polygon = "50.942499 6.898247 50.942275" };
            Assert.That(oddPolygon.ToGeoFenceJson(), Is.Null);

            // Unparseable polygon values have no geoFence.
            Geo garbagePolygon = new Geo { JsonLDType = "gs1:GeoShape", Polygon = "abc def" };
            Assert.That(garbagePolygon.ToGeoFenceJson(), Is.Null);
        }

        /// <summary>
        /// The From/To pairs must be exact inverses so no data is lost converting between the representations.
        /// </summary>
        [Test]
        public void Conversions_RoundTrip_LoseNoData()
        {
            // The polygon survives GeoShape -> geoFence -> GeoShape.
            Geo shape = new Geo { JsonLDType = "gs1:GeoShape", Polygon = "50.942499 6.898247 50.942275 6.898292 50.942499 6.898247" };
            Geo? shapeAfter = Geo.FromGeoFence(shape.ToGeoFenceJson());
            Assert.That(shapeAfter!.Polygon, Is.EqualTo(shape.Polygon));

            // The coordinates survive GeoCoordinates -> geoLocation -> GeoCoordinates.
            Geo point = new Geo { JsonLDType = "gs1:GeoCoordinates", Latitude = new WebVocabFloat { Value = 50.942239 }, Longitude = new WebVocabFloat { Value = 6.89835 } };
            Geo? pointAfter = Geo.FromGeoLocation(point.ToGeoLocationUri());
            Assert.That(pointAfter!.Latitude!.Value, Is.EqualTo(point.Latitude!.Value));
            Assert.That(pointAfter.Longitude!.Value, Is.EqualTo(point.Longitude!.Value));
        }

        // Location - derivation fallback between the master data attributes and the web vocab geo property.

        /// <summary>
        /// The geoFence must take precedence over the geoLocation when both master data attributes are populated.
        /// </summary>
        [Test]
        public void Location_GeoFenceAndGeoLocationSet_GeoDerivesGeoShape()
        {
            // Arrange
            Location loc = new Location { GeoLocation = "geo:50.942239,6.89835", GeoFence = "[[6.898247,50.942499],[6.898292,50.942275],[6.898247,50.942499]]" };

            // Act & Assert
            Assert.That(loc.Geo, Is.Not.Null);
            Assert.That(loc.Geo!.JsonLDType, Is.EqualTo("gs1:GeoShape"), "The geoFence should take precedence over the geoLocation.");
        }

        /// <summary>
        /// A geoLocation alone must derive a gs1:GeoCoordinates.
        /// </summary>
        [Test]
        public void Location_OnlyGeoLocationSet_GeoDerivesGeoCoordinates()
        {
            // Arrange
            Location loc = new Location { GeoLocation = "geo:50.942239,6.89835" };

            // Act & Assert
            Assert.That(loc.Geo, Is.Not.Null);
            Assert.That(loc.Geo!.JsonLDType, Is.EqualTo("gs1:GeoCoordinates"));
            Assert.That(loc.GeoFence, Is.Null, "A gs1:GeoCoordinates should not derive a geoFence.");
        }

        /// <summary>
        /// An explicitly set geo must win over derivation from the master data attributes.
        /// </summary>
        [Test]
        public void Location_ExplicitGeoSet_WinsOverMasterDataAttributes()
        {
            // Arrange
            Geo explicitGeo = new Geo { JsonLDType = "gs1:GeoCoordinates", Latitude = new WebVocabFloat { Value = 1.0 }, Longitude = new WebVocabFloat { Value = 2.0 } };
            Location loc = new Location { Geo = explicitGeo, GeoFence = "[[6.898247,50.942499],[6.898292,50.942275],[6.898247,50.942499]]" };

            // Act & Assert
            Assert.That(loc.Geo, Is.SameAs(explicitGeo));
        }

        /// <summary>
        /// A geo holding a gs1:GeoCoordinates must derive only the geoLocation master data attribute.
        /// </summary>
        [Test]
        public void Location_OnlyGeoCoordinatesSet_DerivesGeoLocationAttribute()
        {
            // Arrange
            Location loc = new Location { Geo = new Geo { JsonLDType = "gs1:GeoCoordinates", Latitude = new WebVocabFloat { Value = 50.942239 }, Longitude = new WebVocabFloat { Value = 6.89835 } } };

            // Act & Assert
            Assert.That(loc.GeoLocation, Is.EqualTo("geo:50.942239,6.89835"));
            Assert.That(loc.GeoFence, Is.Null);
        }

        /// <summary>
        /// A geo holding a gs1:GeoShape polygon must derive only the geoFence master data attribute.
        /// </summary>
        [Test]
        public void Location_OnlyGeoShapeSet_DerivesGeoFenceAttribute()
        {
            // Arrange
            Location loc = new Location { Geo = new Geo { JsonLDType = "gs1:GeoShape", Polygon = "50.942499 6.898247 50.942275 6.898292 50.942499 6.898247" } };

            // Act & Assert
            Assert.That(loc.GeoFence, Is.EqualTo("[[6.898247,50.942499],[6.898292,50.942275],[6.898247,50.942499]]"));
            Assert.That(loc.GeoLocation, Is.Null);
        }

        /// <summary>
        /// An explicitly set master data attribute must win over derivation from the geo property.
        /// </summary>
        [Test]
        public void Location_ExplicitGeoLocationSet_WinsOverGeoDerivation()
        {
            // Arrange
            Location loc = new Location { Geo = new Geo { JsonLDType = "gs1:GeoCoordinates", Latitude = new WebVocabFloat { Value = 1.0 }, Longitude = new WebVocabFloat { Value = 2.0 } }, GeoLocation = "geo:50.942239,6.89835" };

            // Act & Assert
            Assert.That(loc.GeoLocation, Is.EqualTo("geo:50.942239,6.89835"));
        }
    }
}
