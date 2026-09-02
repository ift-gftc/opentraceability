using Newtonsoft.Json;
using Newtonsoft.Json.Linq;
using OpenTraceability.Utility.Attributes;
using System;
using System.Collections.Generic;
using System.Globalization;
using System.Text;

namespace OpenTraceability.Models.Common
{
    /// <summary>
    /// The geographic position or area of a gs1:Place (https://ref.gs1.org/voc/Place), as carried in the "geo" property.
    /// </summary>
    /// <remarks>
    /// In the GS1 Web Vocabulary the "geo" property can hold one of two types: a gs1:GeoCoordinates
    /// (https://ref.gs1.org/voc/GeoCoordinates) point or a gs1:GeoShape (https://ref.gs1.org/voc/GeoShape) area.
    /// Rather than polymorphic dispatch in the mapper, this class is a union of both types' properties with
    /// <see cref="JsonLDType"/> as the discriminator; whichever properties are not used remain null and are
    /// omitted during serialization.
    /// </remarks>
    public class Geo
    {
        /// <summary>
        /// The JSON-LD @type of the geo value: either "gs1:GeoCoordinates" or "gs1:GeoShape".
        /// </summary>
        [OpenTraceabilityJson("@type")]
        public string? JsonLDType { get; set; }

        /// <summary>
        /// The latitude of the location, in degrees. Only set when the geo value is a gs1:GeoCoordinates.
        /// </summary>
        [OpenTraceabilityObject]
        [OpenTraceabilityJson("latitude")]
        public WebVocabFloat? Latitude { get; set; }

        /// <summary>
        /// The longitude of the location, in degrees. Only set when the geo value is a gs1:GeoCoordinates.
        /// </summary>
        [OpenTraceabilityObject]
        [OpenTraceabilityJson("longitude")]
        public WebVocabFloat? Longitude { get; set; }

        /// <summary>
        /// A circle is the circular region of a specified radius centred at a specified latitude and longitude.
        /// A circle is expressed as a pair followed by a radius in meters. Only set when the geo value is a gs1:GeoShape.
        /// </summary>
        [OpenTraceabilityJson("circle")]
        public string? Circle { get; set; }

        /// <summary>
        /// A line is a point-to-point path consisting of two or more point objects separated by a space.
        /// A single line segment (i.e., straight line) is expressed as two points. A multi-line path (i.e., open polygon)
        /// is expressed as a series of three or more points. For a closed shape, gs1:polygon SHALL be used.
        /// Only set when the geo value is a gs1:GeoShape.
        /// </summary>
        [OpenTraceabilityJson("line")]
        public string? Line { get; set; }

        /// <summary>
        /// A polygon is the area enclosed by a point-to-point path for which the starting and ending points are the same.
        /// A polygon is expressed as a series of four or more space delimited points where the first and final points are identical.
        /// Only set when the geo value is a gs1:GeoShape.
        /// </summary>
        [OpenTraceabilityJson("polygon")]
        public string? Polygon { get; set; }

        /// <summary>
        /// Creates a gs1:GeoCoordinates geo from a CBV geoLocation master data attribute, which is an RFC 5870 geo URI
        /// such as "geo:50.942239,6.898350".
        /// </summary>
        /// <remarks>
        /// URI parameters (everything from the first ';') are stripped and an optional third altitude component is
        /// ignored, because gs1:GeoCoordinates carries only latitude and longitude. This is the inverse of
        /// <see cref="ToGeoLocationUri"/>. Never throws; derivation runs inside property getters during serialization.
        /// </remarks>
        /// <param name="geoLocationUri">The geo URI to parse. May be null.</param>
        /// <returns>The parsed geo, or <see langword="null"/> when the value is null, not a geo URI, or malformed.</returns>
        public static Geo? FromGeoLocation(string? geoLocationUri)
        {
            if (string.IsNullOrWhiteSpace(geoLocationUri) || !geoLocationUri!.StartsWith("geo:", StringComparison.OrdinalIgnoreCase))
            {
                return null;
            }

            // Strip URI parameters (e.g. ";crs=wgs84;u=35") before splitting the coordinate components.
            string coordinates = geoLocationUri.Substring("geo:".Length);
            int parametersIndex = coordinates.IndexOf(';');
            if (parametersIndex >= 0)
            {
                coordinates = coordinates.Substring(0, parametersIndex);
            }

            string[] parts = coordinates.Split(',');
            if (parts.Length < 2)
            {
                return null;
            }

            if (!double.TryParse(parts[0], NumberStyles.Float, CultureInfo.InvariantCulture, out double latitude) || !double.TryParse(parts[1], NumberStyles.Float, CultureInfo.InvariantCulture, out double longitude))
            {
                return null;
            }

            return new Geo { JsonLDType = "gs1:GeoCoordinates", Latitude = new WebVocabFloat { Value = latitude }, Longitude = new WebVocabFloat { Value = longitude } };
        }

        /// <summary>
        /// Creates a gs1:GeoShape geo from a CBV geoFence master data attribute, which is a JSON array of
        /// [longitude, latitude] coordinate pairs, e.g. "[[6.898247,50.942499],[6.898292,50.942275],...]".
        /// </summary>
        /// <remarks>
        /// Only a simple single ring is convertible: every element must be a 2-element numeric array. Multi-polygons
        /// and holes (deeper nesting) cannot be represented by a gs1:GeoShape polygon and derive nothing; the raw
        /// geoFence string still round-trips on the EPCIS master data path. Pairs are swapped into the polygon's
        /// "lat lon lat lon ..." point order. This is the inverse of <see cref="ToGeoFenceJson"/>. Never throws.
        /// </remarks>
        /// <param name="geoFenceJson">The geoFence JSON array to parse. May be null.</param>
        /// <returns>The parsed geo, or <see langword="null"/> when the value is null, malformed, or not a simple single ring.</returns>
        public static Geo? FromGeoFence(string? geoFenceJson)
        {
            if (string.IsNullOrWhiteSpace(geoFenceJson))
            {
                return null;
            }

            JArray ring;
            try
            {
                ring = JArray.Parse(geoFenceJson!);
            }
            catch (JsonReaderException)
            {
                return null;
            }

            List<string> points = new List<string>();
            foreach (JToken token in ring)
            {
                if (!(token is JArray pair) || pair.Count != 2 || !IsNumber(pair[0]) || !IsNumber(pair[1]))
                {
                    return null;
                }

                double longitude = (double)pair[0]!;
                double latitude = (double)pair[1]!;
                points.Add(latitude.ToString(CultureInfo.InvariantCulture) + " " + longitude.ToString(CultureInfo.InvariantCulture));
            }

            if (points.Count == 0)
            {
                return null;
            }

            return new Geo { JsonLDType = "gs1:GeoShape", Polygon = string.Join(" ", points) };
        }

        /// <summary>
        /// Converts a gs1:GeoCoordinates geo into a CBV geoLocation master data attribute value (an RFC 5870 geo URI).
        /// </summary>
        /// <remarks>
        /// This is the inverse of <see cref="FromGeoLocation"/>, so a location that starts as web vocabulary JSON keeps
        /// its geo data when written into an EPCIS document and read back.
        /// </remarks>
        /// <returns>The geo URI, or <see langword="null"/> when the geo is not a complete gs1:GeoCoordinates.</returns>
        public string? ToGeoLocationUri()
        {
            if (JsonLDType != "gs1:GeoCoordinates" || Latitude == null || Longitude == null)
            {
                return null;
            }

            return "geo:" + Latitude.Value.ToString(CultureInfo.InvariantCulture) + "," + Longitude.Value.ToString(CultureInfo.InvariantCulture);
        }

        /// <summary>
        /// Converts a gs1:GeoShape polygon into a CBV geoFence master data attribute value (a JSON array of
        /// [longitude, latitude] coordinate pairs).
        /// </summary>
        /// <remarks>
        /// The polygon's "lat lon lat lon ..." points are swapped back into [longitude, latitude] pair order — the
        /// exact inverse of <see cref="FromGeoFence"/>. Circle- or line-only shapes, an odd number of values, and
        /// unparseable numbers cannot be represented as a geoFence and return null.
        /// </remarks>
        /// <returns>The geoFence JSON array, or <see langword="null"/> when the geo has no convertible polygon.</returns>
        public string? ToGeoFenceJson()
        {
            if (JsonLDType != "gs1:GeoShape" || string.IsNullOrWhiteSpace(Polygon))
            {
                return null;
            }

            string[] values = Polygon!.Split(new[] { ' ' }, StringSplitOptions.RemoveEmptyEntries);
            if (values.Length == 0 || values.Length % 2 != 0)
            {
                return null;
            }

            JArray ring = new JArray();
            for (int i = 0; i < values.Length; i += 2)
            {
                if (!double.TryParse(values[i], NumberStyles.Float, CultureInfo.InvariantCulture, out double latitude) || !double.TryParse(values[i + 1], NumberStyles.Float, CultureInfo.InvariantCulture, out double longitude))
                {
                    return null;
                }

                ring.Add(new JArray(longitude, latitude));
            }

            return ring.ToString(Formatting.None);
        }

        /// <summary>
        /// Determines whether the JSON token is a numeric value (integer or float).
        /// </summary>
        private static bool IsNumber(JToken? token)
        {
            return token != null && (token.Type == JTokenType.Float || token.Type == JTokenType.Integer);
        }
    }
}
