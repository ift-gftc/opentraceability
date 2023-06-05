package opentraceability.event

import opentraceability.OpenTraceabilityTests
import opentraceability.gdst.MasterData.GDSTLocation
import opentraceability.interfaces.IVocabularyElement
import opentraceability.mappers.OpenTraceabilityMappers
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource
import kotlin.reflect.KClass


class MasterDataTests {

    @Test
    @ParameterizedTest(name = "{0}")
    @ValueSource(strings = ["gs1-vocab-products01.json"])
    fun gs1WebVocabProducts(file: String) {
        val json = OpenTraceabilityTests.readTestData(file)
        val tradeitem = OpenTraceabilityMappers.MasterData.GS1WebVocab.map<opentraceability.models.masterdata.TradeItem>(json)
        val jsonAfter = OpenTraceabilityMappers.MasterData.GS1WebVocab.map(tradeitem)
        OpenTraceabilityTests.compareJSON(json, jsonAfter)
    }

    @Test
    @ParameterizedTest(name = "{0}")
    @ValueSource(strings = ["gs1-vocab-locations01.json"])
    fun gs1WebVocabLocation(file: String) {
        val json = OpenTraceabilityTests.readTestData(file)
        val loc = OpenTraceabilityMappers.MasterData.GS1WebVocab.map<GDSTLocation>(json)
        val jsonAfter = OpenTraceabilityMappers.MasterData.GS1WebVocab.map(loc)
        OpenTraceabilityTests.compareJSON(json, jsonAfter)
    }

    @Test
    @ParameterizedTest(name = "{0}")
    @ValueSource(strings = ["gs1-vocab-tradingparties01.json"])
    fun gs1WebVocabTradingParty(file: String) {
        val json = OpenTraceabilityTests.readTestData(file)
        val tp = OpenTraceabilityMappers.MasterData.GS1WebVocab.map<opentraceability.models.masterdata.TradingParty>(json)
        val jsonAfter = OpenTraceabilityMappers.MasterData.GS1WebVocab.map(tp)
        OpenTraceabilityTests.compareJSON(json, jsonAfter)
    }

    @Test
    @ParameterizedTest(name = "{0}")
    @ValueSource(strings = ["testserver_advancedfilters.jsonld"])
    fun gs1WebVocabEPCISDocument(file: String) {
        val json = OpenTraceabilityTests.readTestData(file)
        val doc = OpenTraceabilityMappers.EPCISDocument.JSON.map(json)
        for (md in doc.masterData) {
            val jsonMD = OpenTraceabilityMappers.MasterData.GS1WebVocab.map(md)
            val mdAfter = OpenTraceabilityMappers.MasterData.GS1WebVocab.map(md::class, jsonMD)
            val jsonMDAfter = OpenTraceabilityMappers.MasterData.GS1WebVocab.map(mdAfter)
            OpenTraceabilityTests.compareJSON(jsonMD, jsonMDAfter)
        }
    }


}