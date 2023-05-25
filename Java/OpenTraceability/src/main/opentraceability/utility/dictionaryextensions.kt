package utility

class DictionaryExtensions {
    companion object {
        fun<TValue, TKey> Reverse(source: MutableMap<TKey, TValue>): MutableMap<TValue, TKey>{
            var dictionary: MutableMap<TValue, TKey> = mutableMapOf()

            source.forEach { entry ->
                if (!dictionary.containsKey(entry.value)){
                    dictionary.put(entry.value, entry.key);
                }
            }

            return dictionary
        }
    }
}
