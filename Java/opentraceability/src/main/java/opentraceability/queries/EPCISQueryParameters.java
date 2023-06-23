package opentraceability.queries;

import opentraceability.models.identifiers.EPC;
import opentraceability.models.identifiers.EPCType;
import opentraceability.utility.ReflectionUtility;
import org.apache.http.NameValuePair;
import org.apache.http.client.utils.URIBuilder;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;

import java.net.URI;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.net.http.HttpRequest;
import java.nio.charset.StandardCharsets;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class EPCISQueryParameters {
    private static final Map<String, Field> propMapping = initializePropertyMapping();
    private static Map<String, Field> initializePropertyMapping() {
        Map<String, Field> mapping = new HashMap<>();
        for (Field field : EPCISQuery.class.getFields())
        {
            mapping.put(field.getName(), field);
        }
        return mapping;
    }

    public EPCISQueryType queryType = EPCISQueryType.events;
    public final EPCISQuery query = new EPCISQuery();

    public EPCISQueryParameters(){}

    public EPCISQueryParameters(EPC... epcs) {
        for (EPC epc : epcs) {           
            if (epc.getType() == EPCType.Class) {
                query.MATCH_anyEPCClass.add(epc.toString());
            } else {
                query.MATCH_anyEPC.add(epc.toString());
            }
        }
    }

    public EPCISQueryParameters(URI uri) throws Exception {
        URIBuilder uriBuilder = new URIBuilder(uri);
        List<NameValuePair> queryParameters = uriBuilder.getQueryParams();
        for (NameValuePair param : queryParameters) {
            String key = param.getName();
            String value = URLDecoder.decode(param.getValue(), StandardCharsets.UTF_8);

            Field prop = propMapping.get(key);
            if (prop != null) {
                if (OffsetDateTime.class.isAssignableFrom(prop.getType())) {
                    String fixedStr = value.replace(" ", "+");
                    OffsetDateTime dt = OffsetDateTime.parse(fixedStr);
                    prop.set(query, dt);
                }
                else if (List.class.isAssignableFrom(prop.getType())) {
                    Class itemType = ReflectionUtility.getItemType(prop.getType());
                    List<String> listValues = new ArrayList<String>();
                    listValues = Arrays.asList(value.split("\\|"));
                    prop.set(query, listValues);
                }
            }
        }
    }

    public boolean isValid() {
        return true;
    }

    public URI toUri() throws Exception {
        List<String> queryParameters = new ArrayList<>();

        for (Field prop : EPCISQuery.class.getFields()) {
            Object value = prop.get(query);
            if (value instanceof OffsetDateTime) {
                OffsetDateTime offsetDateTime = ((OffsetDateTime) value);
                queryParameters.add(prop.getName() + "=" + URLEncoder.encode(offsetDateTime.toString(), StandardCharsets.UTF_8));
            } else if (value instanceof List) {
                List<?> list = (List<?>) value;
                if (!list.isEmpty()) {
                    List<String> encodedValues = list.stream().map(Object::toString)
                            .map(it -> {
                                return URLEncoder.encode(it, StandardCharsets.UTF_8);
                            })
                            .collect(Collectors.toList());
                    queryParameters.add(prop.getName() + "=" + String.join("|", encodedValues));
                }
            }
        }

        String queryString = String.join("&", queryParameters);
        return new URI(queryString);
    }

    public void merge(EPCISQueryParameters queryParameters) throws IllegalAccessException {
        for (Field prop : EPCISQuery.class.getFields()) {
            Object otherValue = prop.get(queryParameters.query);
            if (otherValue instanceof OffsetDateTime) {
                if (otherValue != null) {
                    prop.set(this.query, otherValue);
                }
            } else if (otherValue instanceof List) {
                if (!((List<?>) otherValue).isEmpty()) {
                    List<Object> list = (List<Object>) prop.get(this.query);
                    if (list.isEmpty()) {
                        prop.set(this.query, otherValue);
                    } else {
                        list.addAll((Collection<?>) otherValue);
                    }
                }
            }
        }
    }

    public String toQueryParameters() throws Exception {
        List<String> queryParameters = new ArrayList<>();

        for (java.lang.reflect.Field prop : EPCISQuery.class.getDeclaredFields()) {
            prop.setAccessible(true);
            Object value = prop.get(this.query);

            if (value instanceof String) {
                String stringValue = ((String) value);
                if (!stringValue.isBlank()) {
                    String encodedValue = URLEncoder.encode(stringValue, StandardCharsets.UTF_8);
                    String queryParam = prop.getName() + "=" + encodedValue;
                    queryParameters.add(queryParam);
                }
            } else if (value instanceof List) {
                List<?> list = (List<?>) value;
                if (!list.isEmpty()) {
                    List<String> encodedValues = list.stream()
                            .map(Object::toString)
                            .map(it -> {
                                try {
                                    return URLEncoder.encode(it, StandardCharsets.UTF_8);
                                } catch (Exception e) {
                                    return "";
                                }
                            })
                            .collect(Collectors.toList());

                    String queryParam = prop.getName() + "=" + String.join("%7C", encodedValues);
                    queryParameters.add(queryParam);
                }
            } else if (value instanceof URI) {
                URI uriValue = ((URI) value);
                String encodedValue = URLEncoder.encode(uriValue.toString(), StandardCharsets.UTF_8);
                String queryParam = prop.getName() + "=" + encodedValue;
                queryParameters.add(queryParam);
            } else if (value instanceof OffsetDateTime) {
                OffsetDateTime offsetDateTime = ((OffsetDateTime) value);
                if (offsetDateTime != null) {
                    DateTimeFormatter formatter = DateTimeFormatter.ISO_OFFSET_DATE_TIME;
                    String isoString = offsetDateTime.format(formatter);

                    String queryParam = prop.getName() + "=" + URLEncoder.encode(isoString, StandardCharsets.UTF_8);
                    queryParameters.add(queryParam);
                }
            }
        }

        String queryString = String.join("&", queryParameters);
        return "?" + queryString;
    }

    public String toJSON() {
        JSONObject JSONObject = new JSONObject();
        JSONObject.put("queryType", this.queryType.toString());
        JSONObject.put("query", this.query.toJSON());

        return JSONObject.toString();
    }

}