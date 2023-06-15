package opentraceability.queries;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import opentraceability.mappers.*;
import opentraceability.models.events.*;
import opentraceability.models.identifiers.*;
import opentraceability.models.masterdata.*;
import opentraceability.utility.*;

import java.io.IOException;
import java.net.URL;
import java.util.*;

public class EPCISTraceabilityResolver {

    public static URL getEPCISQueryInterfaceURL(DigitalLinkQueryOptions options, EPC epc, OkHttpClient client) throws IOException {
        String relativeUrl;
        switch (epc.Type) {
            case Class:
                relativeUrl = String.format("%1$s/10/%2$s", epc.GTIN.ToDigitalLinkURL(), epc.SerialLotNumber);
                break;
            case Instance:
                relativeUrl = String.format("%1$s/21/%2$s", epc.GTIN.ToDigitalLinkURL(), epc.SerialLotNumber);
                break;
            case SSCC:
                relativeUrl = String.format("00/%1$s", epc.toString());
                break;
            default:
                throw new Exception(String.format("Cannot build Digital Link URL with EPC %1$s. We need either GTIN+LOT, GTIN+SERIAL, or SSCC.", epc));
        }

        Request request = new Request.Builder()
                .url(String.format("%1$s%2$s?linkType=gs1:epcis", options.URL, relativeUrl))
                .build();

        Response response = client.newCall(request).execute();
        if (response.isSuccessful()) {
            String json = response.body().string();
            MutableList<DigitalLink> links = Json.decodeFromString(json, new ArrayList<DigitalLink>().getClass());
            DigitalLink link = links.firstOrNull();
            if (link.link != null) {
                link.link = link.link.trimEnd('/');
            }
            return link?.link?.let(URL::new);
        } else {
            return null;
        }
    }

    public static URL getEPCISQueryInterfaceURL(DigitalLinkQueryOptions options, PGLN pgln, OkHttpClient client) throws IOException {
        String relativeUrl = pgln.ToDigitalLinkURL() + "?linkType=gs1:epcis";

        Request request = new Request.Builder()
                .url(options.URL + relativeUrl)
                .get()
                .build();

        Response response = client.newCall(request).execute();
        if (response.isSuccessful()) {
            MutableList<DigitalLink> links = Json.decodeFromString(response.body().string(), new ArrayList<DigitalLink>().getClass());
            DigitalLink link = links.firstOrNull();

            if (link != null && link.link != null) {
                link.link = link.link.trimEnd('/');
            }
            return link.link?.let(URL::new);
        } else {
            return null;
        }
    }

    public static EPCISQueryResults traceback(EPCISQueryInterfaceOptions options, EPC epc, OkHttpClient client, EPCISQueryParameters additionalParameters) throws IOException {
        HashSet<EPC> queriedEpcs = new HashSet<EPC>();
        MutableList<EPC> epcsToQuery = new ArrayList<EPC>();
        epcsToQuery.add(epc);
        queriedEpcs.add(epc);

        EPCISQueryResults results = new EPCISQueryResults();
        // Query for all events pertaining to the EPC
        EPCISQueryParameters parameters = new EPCISQueryParameters(epc);
        if (additionalParameters != null) {
            parameters.merge(additionalParameters);
        }

        results = queryEvents(options, parameters, client);

        return results;
    }

    public static EPCISQueryResults queryEvents(EPCISQueryInterfaceOptions options, EPCISQueryParameters parameters, OkHttpClient client) throws IOException {
        if (options == null) return null;

        // Determine the mapper for deserializing the contents
        IEPCISQueryDocumentMapper mapper = OpenTraceabilityMappers.EPCISQueryDocument.JSON;
        if (options.Format == EPCISDataFormat.XML) {
            mapper = OpenTraceabilityMappers.EPCISQueryDocument.XML;
        }

        // Build the HTTP request
        Request.Builder requestBuilder = new Request.Builder()
                .url(String.format("%1$s/events%2$s", options.URL.toString().trimEnd('/'), parameters.toQueryParameters()))
                .get();

        // Add headers based on the options version
        switch (options.Version) {
            case V1:
                requestBuilder.addHeader("Accept", "application/xml");
                requestBuilder.addHeader("GS1-EPCIS-Version", "1.2");
                requestBuilder.addHeader("GS1-EPCIS-Min", "1.2");
                requestBuilder.addHeader("GS1-EPCIS-Max", "1.2");
                requestBuilder.addHeader("GS1-CBV-Version", "1.2");
                requestBuilder.addHeader("GS1-CBV-XML-Format", "ALWAYS_URN");
                break;
            case V2:
                requestBuilder.addHeader("Accept",
                        options.Format == EPCISDataFormat.XML ? "application/xml" : "application/json");
                requestBuilder.addHeader("GS1-EPCIS-Version", "2.0");
                requestBuilder.addHeader("GS1-EPCIS-Min", "2.0");
                requestBuilder.addHeader("GS1-EPCIS-Max", "2.0");
                requestBuilder.addHeader("GS1-CBV-Version", "2.0");
                requestBuilder.addHeader("GS1-CBV-XML-Format", "ALWAYS_URN");
                break;
            default:
                throw new Exception(
                        "Unrecognized EPCISVersion " + options.Version + " on the options.");
        }

        Request request = requestBuilder.build();

        EPCISQueryResults results = new EPCISQueryResults();

        Response response = client.newCall(request).execute();

        // Execute the request

        try {
            String responseBody = response.body().string();
            if (response.isSuccessful()) {
                try {
                    EPCISQueryDocument doc = mapper.map(responseBody);
                    results.Document = doc;
                } catch (OpenTraceabilitySchemaException schemaEx) {
                    EPCISQueryError error = new EPCISQueryError();
                    error.Type = EPCISQueryErrorType.Schema;
                    error.Details = schemaEx.message;

                    results.Errors.add(error);
                }
            } else {
                EPCISQueryError error = new EPCISQueryError();
                error.Type = EPCISQueryErrorType.HTTP;
                if(responseBody != null){
                    error.Details = response.code() + " - " + response.message() + " - " + responseBody;
                }
                else{
                    error.Details = response.code() + " -