package opentraceability.queries;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.squareup.okhttp.Headers;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;
import opentraceability.interfaces.IEPCISQueryDocumentMapper;
import opentraceability.mappers.EPCISDataFormat;
import opentraceability.mappers.OpenTraceabilityMappers;
import opentraceability.models.events.EPCISQueryDocument;
import opentraceability.models.events.EPCISVersion;
import opentraceability.models.events.EventProductType;
import opentraceability.models.identifiers.*;
import opentraceability.models.masterdata.DigitalLink;
import opentraceability.utility.OpenTraceabilitySchemaException;
import opentraceability.utility.URLHelper;

import java.io.IOException;
import java.net.URL;
import java.util.*;
import java.util.stream.Collectors;

public class EPCISTraceabilityResolver {
    public static URL getEPCISQueryInterfaceURL(DigitalLinkQueryOptions options, EPC epc, OkHttpClient client) throws Exception {
        String relativeUrl;
        switch (epc.getType()) {
            case Class:
                relativeUrl = epc.getGTIN().ToDigitalLinkURL() + "/10/" + epc.getSerialLotNumber();
                break;
            case Instance:
                relativeUrl = epc.getGTIN().ToDigitalLinkURL() + "/21/" + epc.getSerialLotNumber();
                break;
            case SSCC:
                relativeUrl = "00/" + epc;
                break;
            default:
                throw new Exception("Cannot build Digital Link URL with EPC " + epc + ". We need either GTIN+LOT, GTIN+SERIAL, or SSCC.");
        }

        Request request = new Request.Builder()
                .url(options.url + relativeUrl + "?linkType=gs1:epcis")
                .build();

        try {
            Response response = client.newCall(request).execute();
            if (response.isSuccessful()) {
                String json = response.body().string();
                ObjectMapper objectMapper = new ObjectMapper();
                DigitalLink[] links = objectMapper.readValue(json, DigitalLink[].class);
                if (links.length > 0)
                {
                    DigitalLink link = links[0];
                    return new URL(URLHelper.TrimEndSlash(link.link));
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static URL getEPCISQueryInterfaceURL(DigitalLinkQueryOptions options, PGLN pgln, OkHttpClient client) throws Exception {
        if (options.url == null) {
            throw new Exception("options.URL is null on the DigitalLinkQueryOptions");
        }

        String relativeUrl = pgln.ToDigitalLinkURL() + "?linkType=gs1:epcis";

        Request request = new Request.Builder()
                .url(options.url + relativeUrl)
                .get()
                .build();

        try {
            Response response = client.newCall(request).execute();
            if (response.isSuccessful()) {
                String json = response.body().string();
                ObjectMapper objectMapper = new ObjectMapper();
                DigitalLink[] links = objectMapper.readValue(json, DigitalLink[].class);
                if (links.length > 0)
                {
                    DigitalLink link = links[0];
                    return new URL(URLHelper.TrimEndSlash(link.link));
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static EPCISQueryResults traceback(EPCISQueryInterfaceOptions options, EPC epc, OkHttpClient client, EPCISQueryParameters additionalParameters) throws Exception {
        Set<EPC> queriedEpcs = new HashSet<>();
        queriedEpcs.add(epc);

        EPCISQueryParameters parameters = new EPCISQueryParameters(epc);
        if (additionalParameters != null) {
            parameters.merge(additionalParameters);
        }

        EPCISQueryResults results = queryEvents(options, parameters, client);

        if (!results.Errors.isEmpty()) {
            return results;
        }

        if (results.Document == null) {
            throw new NullPointerException("The results.Document is NULL, and this should not happen.");
        }

        List<EPC> epcsToQuery = new ArrayList<>();
        List<EPC> potentialEpcs = results.Document.events.stream()
                .flatMap(event -> event.getProducts().stream())
                .filter(product -> product.Type == EventProductType.Child || product.Type == EventProductType.Input)
                .map(product -> product.EPC)
                .distinct()
                .collect(Collectors.toList());

        for (EPC e : potentialEpcs) {
            if (!queriedEpcs.contains(e)) {
                epcsToQuery.add(e);
                queriedEpcs.add(e);
            }
        }

        for (int stack = 0; stack < 100; stack++) {
            if (!epcsToQuery.isEmpty()) {
                EPCISQueryParameters p = new EPCISQueryParameters(epcsToQuery.toArray(new EPC[0]));
                if (additionalParameters != null) {
                    p.merge(additionalParameters);
                }
                EPCISQueryResults r = queryEvents(options, p, client);

                results.merge(r);

                if (r.Document != null) {
                    epcsToQuery.clear();
                    List<EPC> newPotentialEpcs = r.Document.events.stream()
                            .flatMap(event -> event.getProducts().stream())
                            .filter(product -> product.Type == EventProductType.Child || product.Type == EventProductType.Input)
                            .map(product -> product.EPC)
                            .distinct()
                            .collect(Collectors.toList());

                    for (EPC e : newPotentialEpcs) {
                        if (!queriedEpcs.contains(e)) {
                            epcsToQuery.add(e);
                            queriedEpcs.add(e);
                        }
                    }
                } else {
                    break;
                }
            } else {
                break;
            }
        }

        // Continue with the rest of the conversion here

        return results;
    }

    public static EPCISQueryResults queryEvents(EPCISQueryInterfaceOptions options, EPCISQueryParameters parameters, OkHttpClient client) throws Exception {
        IEPCISQueryDocumentMapper mapper = OpenTraceabilityMappers.EPCISQueryDocument.JSON;
        if (options.format == EPCISDataFormat.XML) {
            mapper = OpenTraceabilityMappers.EPCISQueryDocument.XML;
        }

        Request.Builder requestBuilder = new Request.Builder()
                .url(options.url.toString().trim() + "/events" + parameters.toQueryParameters())
                .get();

        switch (options.version) {
            case V1:
                requestBuilder.addHeader("Accept", "application/xml");
                requestBuilder.addHeader("GS1-EPCIS-Version", "1.2");
                requestBuilder.addHeader("GS1-EPCIS-Min", "1.2");
                requestBuilder.addHeader("GS1-EPCIS-Max", "1.2");
                requestBuilder.addHeader("GS1-CBV-Version", "1.2");
                requestBuilder.addHeader("GS1-CBV-XML-Format", "ALWAYS_URN");
                break;
            case V2:
                requestBuilder.addHeader("Accept", options.format == EPCISDataFormat.XML ? "application/xml" : "application/json");
                requestBuilder.addHeader("GS1-EPCIS-Version", "2.0");
                requestBuilder.addHeader("GS1-EPCIS-Min", "2.0");
                requestBuilder.addHeader("GS1-EPCIS-Max", "2.0");
                requestBuilder.addHeader("GS1-CBV-Version", "2.0");
                requestBuilder.addHeader("GS1-CBV-XML-Format", "ALWAYS_URN");
                break;
            default:
                throw new Exception("Unrecognized EPCISVersion " + options.version + " on the options.");
        }

        EPCISQueryResults results = new EPCISQueryResults();

        Response response = null;
        try {
            response = client.newCall(requestBuilder.build()).execute();

            String responseBody = null;
            if (response.isSuccessful()) {
                responseBody = response.body().string();
                try {
                    EPCISQueryDocument doc = mapper.map(responseBody, true);
                    results.Document = doc;
                } catch (OpenTraceabilitySchemaException schemaEx) {
                    EPCISQueryError error = new EPCISQueryError();
                    error.Type = EPCISQueryErrorType.Schema;
                    error.Details = schemaEx.getMessage();
                    results.Errors.add(error);
                }
            } else {
                EPCISQueryError error = new EPCISQueryError();
                error.Type = EPCISQueryErrorType.HTTP;
                error.Details = response.code() + " - " + response.message() + " - " + responseBody;
                results.Errors.add(error);
            }
        } catch (IOException e) {
            EPCISQueryError error = new EPCISQueryError();
            error.Type = EPCISQueryErrorType.Exception;
            error.Details = e.getMessage();
            results.Errors.add(error);
        }

        if (options.enableStackTrace) {
            EPCISQueryStackTraceItem item = new EPCISQueryStackTraceItem();
            List<Map<String, List<String>>> requestHeaders = new ArrayList<>();
            for (String name : requestBuilder.build().headers().names()) {
                Map<String, List<String>> headerMap = new HashMap<>();
                headerMap.put(name, new ArrayList<>(requestBuilder.build().headers().values(name)));
                requestHeaders.add(headerMap);
            }
            List<Map<String, List<String>>> responseHeaders = new ArrayList<>();
            Headers responseHeadersObj = response.headers();
            for (String name : responseHeadersObj.names()) {
                Map<String, List<String>> headerMap = new HashMap<>();
                headerMap.put(name, new ArrayList<>(responseHeadersObj.values(name)));
                responseHeaders.add(headerMap);
            }

            item.RelativeURL = requestBuilder.build().url().toURI();
            item.RequestHeaders = requestHeaders;
            item.ResponseStatusCode = (response.code());
            item.ResponseBody = response.body().string();
            item.ResponseHeaders = responseHeaders;

            results.StackTrace.add(item);

            for (EPCISQueryError error : results.Errors) {
                error.StackTraceItemID = item.ID;
            }
        }

        return results;
    }
}
