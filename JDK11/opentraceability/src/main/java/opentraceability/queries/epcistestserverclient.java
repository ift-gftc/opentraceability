package opentraceability.queries;

import java.net.URI;
import java.net.URL;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

import opentraceability.interfaces.IEPCISDocumentMapper;
import opentraceability.models.documents.EPCISBaseDocument;
import opentraceability.models.model.enums.EPCISDataFormat;
import opentraceability.models.model.enums.EPCISVersion;
import opentraceability.models.model.interfaces.EPC;
import opentraceability.models.request.*;
import opentraceability.models.results.EPCISQueryResults;
import opentraceability.mappers.OpenTraceabilityMappers;
import opentraceability.utility.HttpClientPool;


public class EPCISTestServerClient {

    private String _baseURL;
    private EPCISDataFormat _format;
    private EPCISVersion _version;

    private OkHttpClient.Builder _okHttpBuilder = new OkHttpClient.Builder()
            .connectTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS);

    public EPCISTestServerClient(String baseURL, EPCISDataFormat format, EPCISVersion version) {
        _baseURL = baseURL;
        _format = format;
        _version = version;
    }

    private OkHttpClient getClient() {
        return _okHttpBuilder.build();
    }

    public String postEPCISDocument(EPCISDocument document) throws Exception {
        String baseUrl = _baseURL.trim().endsWith("/") ? _baseURL.substring(0, _baseURL.length()-1) : _baseURL;
        String actualBlobId = UUID.randomUUID().toString();
        String url = baseUrl + "/epcis/" + actualBlobId + "/events";

        IEPCISDocumentMapper mapper = _format == EPCISDataFormat.JSON ?
                OpenTraceabilityMappers.EPCISDocument.JSON :
                OpenTraceabilityMappers.EPCISDocument.XML;

        String contentType = _format == EPCISDataFormat.JSON ? "application/json" : "application/xml";

        OkHttpClient client = getClient();
        Request request = new Request.Builder().url(url)
            .header("Accept", _format == EPCISDataFormat.XML ? "application/xml" : "application/json")
            .header("GS1-EPCIS-Version", _version == EPCISVersion.V1 ? "1.2" : "2.0")
            .header("GS1-EPCIS-Min", _version == EPCISVersion.V1 ? "1.2" : "2.0")
            .header("GS1-EPCIS-Max", _version == EPCISVersion.V1 ? "1.2" : "2.0")
            .header("GS1-CBV-Version", _version == EPCISVersion.V1 ? "1.2" : "2.0")
            .header("GS1-CBV-XML-Format", "ALWAYS_URN")
            .post(RequestBody.create(MediaType.parse(contentType), mapper.map(document)))
            .build();

        Response response = client.newCall(request).execute();
        if (!response.isSuccessful()) {
            String contentStr = response.body().string();
            throw new Exception(String.format("%d - %s - %s",response.code(), response.message(), contentStr));
        }
        return actualBlobId;
    }


    public EPCISQueryResults queryEvents(String blobId, EPCISQueryParameters parameters) throws Exception {
        String url = _baseURL.trim() + "/epcis/" + blobId;

        EPCISQueryInterfaceOptions options = new EPCISQueryInterfaceOptions();
        options.URL = new URI(url);
        options.Format = _format;
        options.Version = _version;
        options.EnableStackTrace = true;

        return EPCISTraceabilityResolver.queryEvents(options,parameters,getClient());
    }

    public EPCISQueryResults traceback(String blobId, EPC epc) throws Exception {
        String url = _baseURL.trim() + "/epcis/" + blobId;

        EPCISQueryInterfaceOptions options = new EPCISQueryInterfaceOptions();
        options.url = new URI(url);
        options.format = _format;
        options.version = _version;
        options.enableStackTrace = true;

        return EPCISTraceabilityResolver.traceback(options, epc, getClient());
    }

    public void resolveMasterData(String blobId, EPCISBaseDocument document) throws Exception {
        String url = _baseURL.trim() + "/digitallink/" + blobId;

        DigitalLinkQueryOptions options = new DigitalLinkQueryOptions();
        options.url = new URI(url);
        options.EnableStackTrace = true;

        MasterDataResolver.resolveMasterData(options, document, getClient());
    }
}