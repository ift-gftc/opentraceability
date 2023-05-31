package queries

import interfaces.IAggregationEvent
import java.net.http.HttpClient
import models.identifiers.*
import mappers.*
import models.events.EventAction
import models.events.EventProductType
import utility.OpenTraceabilitySchemaException
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

        if (results.Errors.isNotEmpty()) {
            return@withContext results
        }

        results.Document ?: throw NullReferenceException("The results.document is NULL, and this should not happen.")

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
            val aggEvents = results.Document.Events.filterIsInstance<IAggregationEvent>()
                .filter { it.Action == EventAction.ADD && it.ParentID !in queriedEpcs }
                .toList()

            if (aggEvents.isNotEmpty()) {
                for (aggEvent in aggEvents) {
                    val parentID = aggEvent.ParentID
                    val childEpcs = aggEvent.Products.filter { it.Type == EventProductType.Child }.map { it.EPC }
                    val nextEvent = results.Document.Events
                        .filter { it.EventTime > aggEvent.EventTime && it.Products.any { it.EPC in childEpcs } }
                        .minByOrNull { it.EventTime }
                    val nextEventTime = nextEvent?.EventTime ?: results.Document.Events.maxByOrNull { it.EventTime }?.EventTime

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
            val mapper = if (options.Format == EPCISDataFormat.JSON) {
                OpenTraceabilityMappers.EPCISQueryDocument.JSON
            } else {
                OpenTraceabilityMappers.EPCISQueryDocument.XML
            }

            val request = HttpRequest.newBuilder()
                .uri(options.URL?.resolve("/events" + parameters.toQueryParameters()))
                .header("Accept", if (options.Format == EPCISDataFormat.XML) "application/xml" else "application/json")
                .header("GS1-EPCIS-Version", options.Version.value)
                .header("GS1-EPCIS-Min", options.Version.value)
                .header("GS1-EPCIS-Max", options.Version.value)
                .header("GS1-CBV-Version", options.Version.value)
                .header("GS1-CBV-XML-Format", "ALWAYS_URN")
                .GET()
                .build()

            val results = EPCISQueryResults()

            try {
                val response = client.send(request, BodyHandlers.ofString())
                val responseBody = response.body()

                if (response.statusCode().isSuccess) {
                    try {
                        val doc = mapper.Map(responseBody)
                        results.document = doc
                    } catch (schemaEx: OpenTraceabilitySchemaException) {
                        results.Errors.add(
                            EPCISQueryError(
                                type = EPCISQueryErrorType.Schema,
                                details = schemaEx.message
                            )
                        )
                    }
                } else {
                    results.Errors.add(
                        EPCISQueryError(
                            type = EPCISQueryErrorType.HTTP,
                            details = "${response.statusCode()} - ${responseBody}"
                        )
                    )
                }
            } catch (ex: Exception) {
                results.Errors.add(
                    EPCISQueryError(
                        type = EPCISQueryErrorType.Exception,
                        details = ex.message
                    )
                )
            }

            if (options.EnableStackTrace) {
                val stackTraceItem = EPCISQueryStackTraceItem(
                    relativeURL = request.uri(),
                    requestHeaders = request.headers().map { it.name() to it.value() },
                    responseStatusCode = response?.statusCode(),
                    responseBody = responseBody,
                    responseHeaders = response?.headers()?.map { it.name() to it.value() }
                )

                results.StackTrace.add(stackTraceItem)

                results.Errors.forEach { it.StackTraceItemID = stackTraceItem.ID }
            }

            results
        }
}
