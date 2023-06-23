package opentraceability.utility;

import java.util.HashMap;
import java.util.Map;

public class DictionaryExtensions {
    public static <TValue, TKey> Map<TValue, TKey> reverse(Map<TKey, TValue> source) {
        Map<TValue, TKey> dictionary = new HashMap<>();

        for (Map.Entry<TKey, TValue> entry : source.entrySet()) {
            if (!dictionary.containsKey(entry.getValue())){
                dictionary.put(entry.getValue(), entry.getKey());
            }
        }

        return dictionary;
    }
}