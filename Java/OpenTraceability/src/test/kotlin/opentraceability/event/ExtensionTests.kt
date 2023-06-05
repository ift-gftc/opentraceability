package opentraceability.event

import opentraceability.OpenTraceabilityTests
import opentraceability.mappers.OpenTraceabilityMappers
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource

class ExtensionTests {

    /*

    @Test
    @ParameterizedTest(name = "{0}")
    @ValueSource(strings = ["gdst_extensions_01.xml"])
    fun xml(file: String) {
        val xmlObjectEvents = OpenTraceabilityTests.readTestData(file)
        val doc = OpenTraceabilityMappers.EPCISQueryDocument.XML.map(xmlObjectEvents)
        assertTrue(doc.events.any { it is GDSTFishingEvent })
        val fishingEvent = doc.events.first { it is GDSTFishingEvent } as GDSTFishingEvent
        assertTrue(fishingEvent.ILMD?.vesselCatchInformationList?.vessels?.isNotEmpty() == true)
        val processingEvent = doc.events.first { it is GDSTProcessingEvent } as GDSTProcessingEvent
        val feedmillEvent = doc.events.first { it is GDSTFeedmillObjectEvent } as GDSTFeedmillObjectEvent
        assertTrue(doc.getMasterData<GDSTLocation>().isNotEmpty())
    }

    */
}