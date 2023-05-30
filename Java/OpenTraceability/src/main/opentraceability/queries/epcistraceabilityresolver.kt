package queries

import java.net.http.HttpClient
import models.identifiers.*
import models.identifiers.EPC
import models.identifiers.PGLN

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.net.URI

object EPCISTraceabilityResolver {
    suspend fun getEPCISQueryInterfaceURL(options: DigitalLinkQueryOptions, epc: EPC, client: HttpClient): URI? =
        withContext(Dispatchers.IO) {
            if (options.URL == null) {
                throw Exception("options.URL is null on the DigitalLinkQueryOptions")
            }

            val relativeUrl = when (epc.Type) {
                EPCType.Class -> "${epc.GTIN?.toDigitalLinkURL()}/10/${epc.SerialLotNumber}"
                EPCType.Instance -> "${epc.GTIN?.toDigitalLinkURL()}/21/${epc.SerialLotNumber}"
                EPCType.SSCC -> "00/${epc.toString()}"
                else -> throw Exception("Cannot build Digital Link URL with EPC $epc. We need either GTIN+LOT, GTIN+SERIAL, or SSCC.")
            }

            val queryParams = listOf("linkType" to "gs1:epcis")

            val url = URI(options.URL.toString().trimEnd('/') + "/$relativeUrl")
                .addQueryParameters(queryParams)
                .normalize()

            val request = HttpRequest.newBuilder()
                .uri(url)
                .GET()
                .build()

            val response = client.send(request, BodyHandlers.ofString())
            if (response.statusCode().isSuccess) {
                val json = response.body()
                val link = json.toDigitalLinks().firstOrNull()
                link?.link?.trimEnd('/')?.toUri()
            } else {
                null
            }
        }

    suspend fun getEPCISQueryInterfaceURL(options: DigitalLinkQueryOptions, pgln: PGLN, client: HttpClient): URI? =
        withContext(Dispatchers.IO) {
            if (options.URL == null) {
                throw Exception("options.URL is null on the DigitalLinkQueryOptions")
            }

            val relativeUrl = pgln.toDigitalLinkURL()
            val queryParams = listOf("linkType" to "gs1:epcis")

            val url = URI(options.URL.toString().trimEnd('/') + "/$relativeUrl")
                .addQueryParameters(queryParams)
                .normalize()

            val request = HttpRequest.newBuilder()
                .uri(url)
                .GET()
                .build()

            val response = client.send(request, BodyHandlers.ofString())
            if (response.statusCode().isSuccess) {
                val json = response.body()
                val link = json.toDigitalLinks().firstOrNull()
                link?.link?.trimEnd('/')?.toUri()
            } else {
                null
            }
        }

    suspend fun traceback(
        options: EPCISQueryInterfaceOptions,
        epc: EPC,
        client: HttpClient,
        additionalParameters: EPCISQueryParameters? = null
    ): EPCISQueryResults = withContext(Dispatchers.IO) {
        val queriedEpcs = mutableSetOf<EPC>(epc)
        val parameters = EPCISQueryParameters(epc)
        additionalParameters?.let { parameters.merge(it) }

        val results = queryEvents(options, parameters, client)

        if (results.errors.isNotEmpty()) {
            return@withContext results
        }

        results.document ?: throw NullReferenceException("The results.document is NULL, and this should not happen.")

        var epcsToQuery = results.document.events.asSequence()
            .flatMap { event ->
                event.products.asSequence()
                    .filter { it.type == EventProductType.Child || it.type == EventProductType.Input }
                    .map { it.epc }
            }
            .distinct()
            .filterNot { it in queriedEpcs }
            .toMutableList()

        for (stack in 0 until 100) {
            if (epcsToQuery.isNotEmpty()) {
                val parameters = EPCISQueryParameters(*epcsToQuery.toTypedArray())
                additionalParameters?.let { parameters.merge(it) }

                val queryResults = queryEvents(options, parameters, client)
                results.merge(queryResults)

                if (queryResults.document != null) {
                    epcsToQuery = queryResults.document.events.asSequence()
                        .flatMap { event ->
                            event.products.asSequence()
                                .filter { it.type == EventProductType.Child || it.type == EventProductType.Input }
                                .map { it.epc }
                        }
                        .distinct()
                        .filterNot { it in queriedEpcs }
                        .toMutableList()
                } else {
                    break
                }
            } else {
                break
            }
        }

        for (stack in 0 until 100) {
            val aggEvents = results.document.events.filterIsInstance<IAggregationEvent>()
                .filter { it.action == EventAction.ADD && it.parentID !in queriedEpcs }
                .toList()

            if (aggEvents.isNotEmpty()) {
                for (aggEvent in aggEvents) {
                    val parentID = aggEvent.parentID
                    val childEpcs = aggEvent.products.filter { it.type == EventProductType.Child }.map { it.epc }
                    val nextEvent = results.document.events
                        .filter { it.eventTime > aggEvent.eventTime && it.products.any { it.epc in childEpcs } }
                        .minByOrNull { it.eventTime }
                    val nextEventTime = nextEvent?.eventTime ?: results.document.events.maxByOrNull { it.eventTime }?.eventTime

                    val parameters = EPCISQueryParameters(parentID)
                    parameters.query.LE_eventTime = nextEventTime
                    additionalParameters?.let { parameters.merge(it) }

                    val queryResults = queryEvents(options, parameters, client)
                    results.merge(queryResults)
                    queriedEpcs.add(parentID)
                }
            } else {
                break
            }
        }

        results
    }

    suspend fun queryEvents(options: EPCISQueryInterfaceOptions, parameters: EPCISQueryParameters, client: HttpClient): EPCISQueryResults =
        withContext(Dispatchers.IO) {
            val mapper = if (options.format == EPCISDataFormat.JSON) {
                OpenTraceabilityMappers.EPCISQueryDocument.JSON
            } else {
                OpenTraceabilityMappers.EPCISQueryDocument.XML
            }

            val request = HttpRequest.newBuilder()
                .uri(options.URL?.resolve("/events" + parameters.toQueryParameters()))
                .header("Accept", if (options.format == EPCISDataFormat.XML) "application/xml" else "application/json")
                .header("GS1-EPCIS-Version", options.version.value)
                .header("GS1-EPCIS-Min", options.version.value)
                .header("GS1-EPCIS-Max", options.version.value)
                .header("GS1-CBV-Version", options.version.value)
                .header("GS1-CBV-XML-Format", "ALWAYS_URN")
                .GET()
                .build()

            val results = EPCISQueryResults()

            try {
                val response = client.send(request, BodyHandlers.ofString())
                val responseBody = response.body()

                if (response.statusCode().isSuccess) {
                    try {
                        val doc = mapper.map(responseBody)
                        results.document = doc
                    } catch (schemaEx: OpenTraceabilitySchemaException) {
                        results.errors.add(
                            EPCISQueryError(
                                type = EPCISQueryErrorType.Schema,
                                details = schemaEx.message
                            )
                        )
                    }
                } else {
                    results.errors.add(
                        EPCISQueryError(
                            type = EPCISQueryErrorType.HTTP,
                            details = "${response.statusCode()} - ${responseBody}"
                        )
                    )
                }
            } catch (ex: Exception) {
                results.errors.add(
                    EPCISQueryError(
                        type = EPCISQueryErrorType.Exception,
                        details = ex.message
                    )
                )
            }

            if (options.enableStackTrace) {
                val stackTraceItem = EPCISQueryStackTraceItem(
                    relativeURL = request.uri(),
                    requestHeaders = request.headers().map { it.name() to it.value() },
                    responseStatusCode = response?.statusCode(),
                    responseBody = responseBody,
                    responseHeaders = response?.headers()?.map { it.name() to it.value() }
                )

                results.stackTrace.add(stackTraceItem)

                results.errors.forEach { it.stackTraceItemID = stackTraceItem.ID }
            }

            results
        }
}
