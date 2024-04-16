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
        public string ID { get => GLN?.ToString(); }

        public string EPCISType { get; set; } = "urn:epcglobal:epcis:vtype:Location";

        [OpenTraceabilityJson("@type")]
        public string JsonLDType { get; set; } = "gs1:Place";

        public VocabularyType VocabularyType => VocabularyType.Location;

        public JToken Context { get; set; }

        [OpenTraceabilityJson("globalLocationNumber")]
        public GLN GLN { get; set; }

        [OpenTraceabilityJson("cbvmda:owning_party")]
        [OpenTraceabilityMasterData("urn:epcglobal:cbv:owning_party")]
        public PGLN OwningParty { get; set; }

        [OpenTraceabilityJson("cbvmda:informationProvider")]
        [OpenTraceabilityMasterData("urn:epcglobal:cbv:mda#informationProvider")]
        public PGLN InformationProvider { get; set; }

        [OpenTraceabilityObject]
        [OpenTraceabilityMasterData("https://gs1.org/cbv/cbvmda:certificationList")]
        public CertificationList CertificationList { get; set; }

        [OpenTraceabilityJson("name")]
        [OpenTraceabilityMasterData("urn:epcglobal:cbv:mda#name")]
        public List<LanguageString> Name { get; set; }

        [OpenTraceabilityJson("contact")]
        [OpenTraceabilityMasterData("urn:epcglobal:cbv:mda#contact")]
        public string Contact { get; set; }

        [OpenTraceabilityJson("email")]
        [OpenTraceabilityMasterData("urn:epcglobal:cbv:mda#email")]
        public string Email { get; set; }

        [OpenTraceabilityJson("telephone")]
        [OpenTraceabilityMasterData("urn:epcglobal:cbv:mda#phone")]
        public string Phone { get; set; }

        [OpenTraceabilityObject]
        [OpenTraceabilityJson("address")]
        [OpenTraceabilityMasterData]
        public Address Address { get; set; }

        [OpenTraceabilityJson("cbvmda:unloadingPort")]
        [OpenTraceabilityMasterData("urn:epcglobal:cbv:mda#unloadingPort")]
        public string UnloadingPort { get; set; }

        /// <summary>
        /// These are additional KDEs that were not mapped into the object.
        /// </summary>
        public List<IMasterDataKDE> KDEs { get; set; } = new List<IMasterDataKDE>();
    }
}
