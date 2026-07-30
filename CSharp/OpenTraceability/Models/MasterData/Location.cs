using Newtonsoft.Json.Linq;
using OpenTraceability.Interfaces;
using OpenTraceability.Models.Common;
using OpenTraceability.Models.Events.KDEs;
using OpenTraceability.Models.Identifiers;
using OpenTraceability.Utility;
using OpenTraceability.Utility.Attributes;
using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;

namespace OpenTraceability.Models.MasterData
{
    public class Location : IVocabularyElement
    {
        public string? ID { get => GLN?.ToString(); }

        public string? EPCISType { get; set; } = "urn:epcglobal:epcis:vtype:Location";

        [OpenTraceabilityJson("@type")]
        public string? JsonLDType { get; set; } = "gs1:Place";

        public VocabularyType VocabularyType => VocabularyType.Location;

        public JToken? Context { get; set; }

        [OpenTraceabilityJson("globalLocationNumber")]
        public GLN? GLN { get; set; }

        [OpenTraceabilityJson("cbvmda:owning_party")]
        [OpenTraceabilityMasterData("urn:epcglobal:cbv:owning_party")]
        public PGLN? OwningParty { get; set; }

        [OpenTraceabilityJson("cbvmda:informationProvider")]
        [OpenTraceabilityMasterData("urn:epcglobal:cbv:mda#informationProvider")]
        public PGLN? InformationProvider { get; set; }

        [OpenTraceabilityObject]
        [OpenTraceabilityMasterData("https://gs1.org/cbv/cbvmda:certificationList")]
        public CertificationList? CertificationList { get; set; }

        [OpenTraceabilityJson("name")]
        [OpenTraceabilityMasterData("urn:epcglobal:cbv:mda#name")]
        public List<LanguageString>? Name { get; set; }

        [OpenTraceabilityJson("contact")]
        [OpenTraceabilityMasterData("urn:epcglobal:cbv:mda#contact")]
        public string? Contact { get; set; }

        [OpenTraceabilityJson("email")]
        [OpenTraceabilityMasterData("urn:epcglobal:cbv:mda#email")]
        public string? Email { get; set; }

        [OpenTraceabilityJson("telephone")]
        [OpenTraceabilityMasterData("urn:epcglobal:cbv:mda#phone")]
        public string? Phone { get; set; }

        [OpenTraceabilityObject]
        [OpenTraceabilityJson("address")]
        [OpenTraceabilityMasterData]
        public Address? Address { get; set; }

        [OpenTraceabilityJson("cbvmda:unloadingPort")]
        [OpenTraceabilityMasterData("urn:epcglobal:cbv:mda#unloadingPort")]
        public string? UnloadingPort { get; set; }

        private Geo? _geo;

        private string? _geoLocation;

        private string? _geoFence;

        /// <summary>
        /// The geographic position or area of the location in the GS1 Web Vocabulary format, holding either a
        /// gs1:GeoCoordinates or a gs1:GeoShape. Used on the GS1 Web Vocab JSON path. When not explicitly set, the
        /// value is derived from the EPCIS master data attributes <see cref="GeoFence"/> (preferred) or
        /// <see cref="GeoLocation"/>, so a location read from an EPCIS document serves an accurate web vocab definition.
        /// </summary>
        [OpenTraceabilityObject]
        [OpenTraceabilityJson("geo")]
        public Geo? Geo
        {
            get => _geo ?? Common.Geo.FromGeoFence(_geoFence) ?? Common.Geo.FromGeoLocation(_geoLocation);
            set => _geo = value;
        }

        /// <summary>
        /// As a master data attribute, this is a Geo URI as specified in [RFC5870], consisting of the latitude and longitude of a location, in degrees.Optionally, a Geo URI may also include a location’s altitude.
        /// For example, geo:50.942239,6.898350 indicates the geographic position of GS1 Germany’s offices.
        /// When not explicitly set, the value is derived from <see cref="Geo"/> when it holds a gs1:GeoCoordinates,
        /// so a location read from web vocab JSON keeps its geo data when written into an EPCIS document.
        /// </summary>
        [OpenTraceabilityMasterData("urn:epcglobal:cbv:mda#geoLocation")]
        public string? GeoLocation
        {
            get => _geoLocation ?? _geo?.ToGeoLocationUri();
            set => _geoLocation = value;
        }

        /// <summary>
        /// Area polygon (geo-fence) as specified in RFC 7946 consisting of an array of longitude - latitude - coordinates, defined according to the following rules:
        /// The array SHALL consist of at least 4 individual coordinates.The first coordinate of a given array SHALL be identical to the last one.
        /// Each individual coordinate (‘[longitude, latitude]’) and the area polygonitself SHALL be embedded in square brackets.
        /// The array of coordinates SHALL be indicated and processed in sequential order, separated by commas, while following the right-hand rule (i.e., anticlockwise).
        /// If there is the need to define a multi-polygon (e.g., a warehouselocation that is split in two parts as it is separated by a street), each partial area polygon SHALL be embedded in separate square brackets.
        /// If there is a need to define a hole within an area polygon(e.g., if an area within a property pertains to another organisation), the area polygon and the contained hole SHALL be embedded in separate squarebrackets and the coordinates of the hole SHALL be indicated and processed in sequential order while following the left - hand rule(i.e.,clockwise).
        /// When not explicitly set, the value is derived from <see cref="Geo"/> when it holds a gs1:GeoShape polygon,
        /// so a location read from web vocab JSON keeps its geo data when written into an EPCIS document.
        /// </summary>
        [OpenTraceabilityMasterData("urn:epcglobal:cbv:mda#geoFence")]
        public string? GeoFence
        {
            get => _geoFence ?? _geo?.ToGeoFenceJson();
            set => _geoFence = value;
        }

        /// <summary>
        /// These are additional KDEs that were not mapped into the object.
        /// </summary>
        public List<IMasterDataKDE> KDEs { get; set; } = new List<IMasterDataKDE>();
    }
}
