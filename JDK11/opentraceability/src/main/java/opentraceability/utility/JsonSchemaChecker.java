package opentraceability.utility;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.networknt.schema.JsonSchema;
import com.networknt.schema.JsonSchemaFactory;
import com.networknt.schema.ValidationMessage;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class JsonSchemaChecker {
    private static final Object lock = new Object();
    private static final ConcurrentHashMap<String, JsonSchema> schemaCache = new ConcurrentHashMap<>();

    public static Pair<Boolean, List<String>> isValid(String jsonStr, String schemaURL) throws URISyntaxException, IOException, InterruptedException {
        JsonSchema mySchema = schemaCache.get(schemaURL);
        if (mySchema == null) {
            synchronized (lock) {
                HttpClient client = HttpClient.newHttpClient();
                HttpRequest request = HttpRequest.newBuilder()
                        .uri(new URI(schemaURL))
                        .build();
                HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
                String schemaStr = response.body();
                mySchema = JsonSchemaFactory.getInstance().getSchema(schemaStr);
                schemaCache.put(schemaURL, mySchema);
            }
        }

        ObjectMapper objectMapper = new ObjectMapper();
        List<String> results = new ArrayList<>();
        boolean isValid = false;
        try {
            JsonNode node = objectMapper.readTree(jsonStr);
            Set<ValidationMessage> validationMessages = mySchema.validate(node);
            isValid = validationMessages.isEmpty();
            for (ValidationMessage message : validationMessages) {
                results.add(message.getMessage());
            }
        } catch (Exception e) {
            results.add(e.getMessage() + " :: " + e.getClass().getSimpleName());
        }

        return new Pair<>(isValid, results);
    }
}
