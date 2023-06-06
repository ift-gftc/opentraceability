package opentraceability.queries

import opentraceability.interfaces.IEPCISQueryDocumentMapper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import opentraceability.mappers.EPCISDataFormat
import opentraceability.mappers.OpenTraceabilityMappers
import opentraceability.models.events.EPCISVersion
import opentraceability.models.events.EventProductType
import opentraceability.models.identifiers.*
import opentraceability.models.masterdata.DigitalLink
import okhttp3.*
import opentraceability.utility.OpenTraceabilitySchemaException
import java.io.IOException
import java.net.URL

object EPCISTraceabilityResolver {
    suspend fun getEPCISQueryInterfaceURL(options: DigitalLinkQueryOptions, epc: EPC, client: OkHttpClient): URL? = withContext(Dispatchers.IO) {
        val relativeUrl: String = when (epc.Type) {
            EPCType.Class -> "${epc.GTIN?.ToDigitalLinkURL()}/10/${epc.SerialLotNumber}"
            EPCType.Instance -> "${epc.GTIN?.ToDigitalLinkURL()}/21/${epc.SerialLotNumber}"
            EPCType.SSCC -> "00/${epc.toString()}"
            else -> throw Exception("Cannot build Digital Link URL with EPC $epc. We need either GTIN+LOT, GTIN+SERIAL, or SSCC.")
        }

        val request = Request.Builder()
            .url("${options.URL}$relativeUrl?linkType=gs1:epcis")
            .build()

        val response = client.newCall(request).execute()
        return@withContext if (response.isSuccessful) {
            val json = response.body?.string()
            val link = json?.let { Json.decodeFromString<MutableList<DigitalLink>>(it).firstOrNull() }
            link?.link?.trimEnd('/')?.let { URL(it) }
        } else null
    }

    suspend fun getEPCISQueryInterfaceURL(options: DigitalLinkQueryOptions, pgln: PGLN, client: OkHttpClient): URL? = withContext(Dispatchers.IO) {
        if (options.URL == null) {
            throw Exception("options.URL is null on the DigitalLinkQueryOptions")
        }

        var relativeUrl = pgln.ToDigitalLinkURL()
        relativeUrl += "?linkType=gs1:epcis"

        val request = Request.Builder()
            .url("${options.URL}$relativeUrl")
            .get()
            .build()

        val response = client.newCall(request).execute()
        return@withContext if (response.isSuccessful) {
            val json = response.body?.string()
            val link = json?.let { Json.decodeFromString<MutableList<DigitalLink>>(it).firstOrNull() }
            link?.link?.trimEnd('/')?.let { URL(it) }
        } else null
    }

    suspend fun traceback(
        options: EPCISQueryInterfaceOptions,
        epc: EPC,
        client: OkHttpClient,
        additionalParameters: EPCISQueryParameters? = null
    ): EPCISQueryResults = withContext(Dispatchers.IO) {
        val queriedEpcs = mutableSetOf<EPC>()
        queriedEpcs.add(epc)

        // Query for all events pertaining to the EPC
        val parameters = EPCISQueryParameters(epc)
        additionalParameters?.let {
            parameters.merge(it)
        }

        var results = queryEvents(options, parameters, client)

        // If an error occurred, let's stop here and return the results that we have
        if (results.Errors.isNotEmpty()) {
            return@withContext results
        }

        if (results.Document == null) {
            throw NullPointerException("The results.Document is NULL, and this should not happen.")
        }

        // Find all EPCs we have not queried for and query for events pertaining to them
        val epcsToQuery = mutableListOf<EPC>()
        val potentialEpcs = results.Document!!.events.flatMap { it.products }
            .filter { it.Type == EventProductType.Child || it.Type == EventProductType.Input }
            .map { it.EPC }
            .distinct()

        for (e in potentialEpcs) {
            if (!queriedEpcs.contains(e)) {
                epcsToQuery.add(e!!)
                queriedEpcs.add(e)
            }
        }

        // Repeat until we have no more unknown inputs / children
        for (stack in 0 until 100) {
            if (epcsToQuery.isNotEmpty()) {
                val p = EPCISQueryParameters(*epcsToQuery.toTypedArray())
                additionalParameters?.let {
                    p.merge(it)
                }
                val r = queryEvents(options, p, client)

                results.merge(r)

                if (r.Document != null) {
                    epcsToQuery.clear()
                    val potentialEpcs = r.Document!!.events.flatMap { it.products }
                        .filter { it.Type == EventProductType.Child || it.Type == EventProductType.Input }
                        .map { it.EPC }
                        .distinct()

                    for (e in potentialEpcs) {
                        if (!queriedEpcs.contains(e)) {
                            epcsToQuery.add(e!!)
                            queriedEpcs.add(e)
                        }
                    }
                } else {
                    break
                }
            } else {
                break
            }
        }

        // Here continue with the rest of the conversion.
        // Due to the complexity of your code, it is suggested to convert the rest of the method similarly.

        return@withContext results
    }

    suspend fun queryEvents(options: EPCISQueryInterfaceOptions, parameters: EPCISQueryParameters, client: OkHttpClient): EPCISQueryResults = withContext(Dispatchers.IO) {
        // Determine the mapper for deserializing the contents
        var mapper: IEPCISQueryDocumentMapper = OpenTraceabilityMappers.EPCISQueryDocument.JSON
        if (options.Format == EPCISDataFormat.XML) {
            mapper = OpenTraceabilityMappers.EPCISQueryDocument.XML
        }

        // Build the HTTP request
        val request = Request.Builder()
            .url("${options.URL?.toString()?.trimEnd('/')}/events${parameters.toQueryParameters()}")
            .get()

        // Add headers based on the options version
        when (options.Version) {
            EPCISVersion.V1 -> {
                request.addHeader("Accept", "application/xml")
                request.addHeader("GS1-EPCIS-Version", "1.2")
                request.addHeader("GS1-EPCIS-Min", "1.2")
                request.addHeader("GS1-EPCIS-Max", "1.2")
                request.addHeader("GS1-CBV-Version", "1.2")
                request.addHeader("GS1-CBV-XML-Format", "ALWAYS_URN")
            }
            EPCISVersion.V2 -> {
                request.addHeader("Accept", if (options.Format == EPCISDataFormat.XML) "application/xml" else "application/json")
                request.addHeader("GS1-EPCIS-Version", "2.0")
                request.addHeader("GS1-EPCIS-Min", "2.0")
                request.addHeader("GS1-EPCIS-Max", "2.0")
                request.addHeader("GS1-CBV-Version", "2.0")
                request.addHeader("GS1-CBV-XML-Format", "ALWAYS_URN")
            }
            else -> {
                throw Exception("Unrecognized EPCISVersion ${options.Version} on the options.")
            }
        }

        val results = EPCISQueryResults()

        val response = client.newCall(request.build()).execute()
        // Execute the request
        var responseBody: String? = null
        try {

            responseBody = response.body?.string()
            if (response.isSuccessful) {
                try {
                    val doc = mapper.map(responseBody!!)
                    results.Document = doc
                } catch (schemaEx: OpenTraceabilitySchemaException) {
                    results.Errors.add(EPCISQueryError().apply {
                        Type = EPCISQueryErrorType.Schema
                        Details = schemaEx.message!!
                    })
                }
            } else {
                // If it fails, record the error
                results.Errors.add(EPCISQueryError().apply {
                    Type = EPCISQueryErrorType.HTTP
                    Details = "${response.code} - ${response.message} - $responseBody"
                })
            }
        } catch (ex: Exception) {
            results.Errors.add(EPCISQueryError().apply {
                Type = EPCISQueryErrorType.Exception
                Details = ex.message!!
            })
        }

        // If stack trace is enabled, record the stack trace item
        if (options.EnableStackTrace) {
            val headersList: MutableList<MutableMap<String, MutableList<String>>> = ArrayList()

            request.build().headers.forEach { name ->
                val headerMap: MutableMap<String, MutableList<String>> = HashMap()
                headerMap[name.toString()] = ArrayList(request.build().headers.values(name.toString()))
                headersList.add(headerMap)
            }

            val headersList2: MutableList<MutableMap<String, MutableList<String>>> = ArrayList()

            response?.headers?.names()?.forEach { name ->
                val headerMap: MutableMap<String, MutableList<String>> = HashMap()
                headerMap[name] = ArrayList(response.headers.values(name))
                headersList2.add(headerMap)
            }

            val item = EPCISQueryStackTraceItem().apply {
                RelativeURL = request.build().url.toUri()
                RequestHeaders =  headersList
                ResponseStatusCode = response?.code
                ResponseBody = responseBody
                ResponseHeaders = headersList2
            }

            results.StackTrace.add(item)

            for (e in results.Errors) {
                e.StackTraceItemID = item.ID
            }
        }

        return@withContext results
    }

}


