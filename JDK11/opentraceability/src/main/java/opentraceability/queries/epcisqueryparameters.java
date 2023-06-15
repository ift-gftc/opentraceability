package opentraceability.queries;

import com.google.gson.GsonBuilder;
import kotlin.jvm.internal.Reflection;
import kotlin.reflect.KMutableProperty;
import kotlin.reflect.full.KTypeProjection;
import kotlin.reflect.full.KotlinReflectionUtils;
import opentraceability.models.identifiers.EPC;
import opentraceability.models.identifiers.EPCType;
import org.apache.http.client.utils.URIBuilder;
import org.json.JSONObject;

import java.lang.reflect.Field;
import java.net.URI;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.net.http.HttpRequest;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class EPCISQueryParameters {
    public Map<String, Field> propMapping = Stream.of(EPCISQuery.class.getFields())
        .filter(KMutableProperty.class::isInstance)
        .map(KMutableProperty.class::cast)
        .collect(Collectors.toMap(KMutableProperty::getName, Function.identity()));

    public EPCISQueryType queryType = EPCISQueryType.events;
    public final EPCISQuery query = new EPCISQuery();

    public EPCISQueryParameters(){}

    public EPCISQueryParameters(EPC... epcs) {
        for (EPC epc : epcs) {           
            if (epc.Type == EPCType.Class) {
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
            String value = URLDecoder.decode(param.getValue(), "UTF-8");

            Field prop = propMapping.get(key);
            if (prop != null) {
                if (OffsetDateTime.class.isAssignableFrom(KotlinReflectionUtils.getJavaObjectType(prop.getReturnType()))) {
                    String fixedStr = value.replace(" ", "+");
                    OffsetDateTime dt = OffsetDateTime.parse(fixedStr);
                    prop.setter.call(query, dt);
                } else if (List.class.isAssignableFrom(KotlinReflectionUtils.getJavaObjectType(prop.getReturnType()))) {
                    // need to further check the type argument (String, URI) of the MutableList here
                    if (KotlinReflectionUtils.getJavaObjectType(prop.getReturnType()).isAssignableFrom(ArrayList.class)) {
                        List<String> values = Stream.of(value.split("|"))
                          .collect(Collectors.toList());
                        prop.setter.call(query, values);
                    } else if (prop.getReturnType().getArguments().get(0).getType().getClassifier() == String.class) {
                        List<URI> values = Stream.of(value.split("|"))
                          .map(URI::create)
                          .collect(Collectors.toList());
                        prop.setter.call(query, values);
                    }
                }
            }
        }
    }

    public boolean isValid() {
        return true;
    }

    public URI toUri() throws Exception {
        List<String> queryParameters = new ArrayList<>();

        for (Field prop : Reflection.getFields(EPCISQuery.class)) {
            if (prop instanceof KMutableProperty) {
                Object value = prop.get(query);
                if (value instanceof OffsetDateTime) {
                    OffsetDateTime offsetDateTime = ((OffsetDateTime) value);
                    queryParameters.add(prop.getName() + "=" + URLEncoder.encode(offsetDateTime.toString(), "UTF-8"));
                } else if (value instanceof List) {
                    List<?> list = (List<?>) value;
                    if (!list.isEmpty()) {
                        List<String> encodedValues = list.stream().map(Object::toString)
                                .map(it -> URLEncoder.encode(it, "UTF-8"))
                                .collect(Collectors.toList());
                        queryParameters.add(prop.getName() + "=" + String.join("|", encodedValues));
                    }
                }
            }
        }

        String queryString = String.join("&", queryParameters);
        return new URI(queryString);
    }

    public void merge(EPCISQueryParameters queryParameters) {
        for (Field prop : Reflection.getFields(EPCISQuery.class)) {
            if (prop instanceof KMutableProperty) {
                Object otherValue = prop.get(queryParameters.query);
                if (otherValue instanceof OffsetDateTime) {
                    if (otherValue != null) {
                        prop.setter.call(this.query, otherValue);
                    }
                } else if (otherValue instanceof List) {
                    if (!((List<?>) otherValue).isEmpty()) {
                        List<Object> list = (List<Object>) prop.get(this.query);
                        if (list.isEmpty()) {
                            prop.setter.call(this.query, otherValue);
                        } else {
                            list.addAll((Collection<?>) otherValue);
                        }
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
                    String encodedValue = URLEncoder.encode(stringValue, "UTF-8");
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
                                    return it instanceof URI ? URLEncoder.encode(it.toString(), "UTF-8") : URLEncoder.encode(it.toString(), "UTF-8");
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
                String encodedValue = URLEncoder.encode(uriValue.toString(), "UTF-8");
                String queryParam = prop.getName() + "=" + encodedValue;
                queryParameters.add(queryParam);
            } else if (value instanceof OffsetDateTime) {
                OffsetDateTime offsetDateTime = ((OffsetDateTime) value);
                if (offsetDateTime != null) {
                    DateTimeFormatter formatter = DateTimeFormatter.ISO_OFFSET_DATE_TIME;
                    String isoString = offsetDateTime.format(formatter);

                    String queryParam = prop.getName() + "=" + URLEncoder.encode(isoString, "UTF-8");
                    queryParameters.add(queryParam);
                }
            }
        }

        String queryString = String.join("&", queryParameters);
        return "?" + queryString;
    }

    public String toJSON() {
        GsonBuilder gson = new GsonBuilder().setDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
        String json = gson.create().toJson(this.query);

        JSONObject jobject = new JSONObject();
        jobject.put("queryType", this.queryType.toString());
        jobject.put("query", new JSONObject(json));

        return jobject.toString();
    }

}