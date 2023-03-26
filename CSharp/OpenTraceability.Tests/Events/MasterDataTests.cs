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
    public class MasterDataTests
    {
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
    }
}
