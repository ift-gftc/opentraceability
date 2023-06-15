package opentraceability.utility;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.networknt.schema.JsonSchema;
import com.networknt.schema.JsonSchemaFactory;
import com.networknt.schema.SpecVersion;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

public class JsonSchemaChecker {
    private static Object lock = new Object();
    private static ConcurrentHashMap<String, String> schemaCache = new ConcurrentHashMap<>();

    public static Pair<Boolean, List<String>> isValid(String jsonStr, String schemaURL) throws URISyntaxException, IOException, InterruptedException {
        String schemaStr = schemaCache.get(schemaURL);
        if (schemaStr == null) {
            synchronized (lock) {
                HttpClient client = HttpClient.newHttpClient();
                HttpRequest request = HttpRequest.newBuilder()
                        .uri(new URI(schemaURL))
                        .build();
                HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
                schemaStr = response.body();
                schemaCache.put(schemaURL, schemaStr);
            }
        }

        ObjectMapper objectMapper = new ObjectMapper();

        JsonSchemaFactory schemaFactory = JsonSchemaFactory.getInstance(SpecVersion.VersionFlag.V4);
        JsonSchema mySchema = schemaFactory.getSchema(schemaStr);
        List<String> results = new ArrayList<>();
        boolean isValid = false;
        try {
            JsonNode node = objectMapper.readTree(jsonStr);
            isValid = mySchema.validate(node).isEmpty();
        } catch (Exception e) {
            results.add(e.getMessage() + " :: " + e.getClass().getSimpleName());
        }

        return new Pair<>(isValid, results);
    }
}