package opentraceability.utility;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class ListExtensions
{
    public static <T> T FirstOrDefault(Stream<T> stream)
    {
        List collection = Arrays.asList(stream.toArray());
        if (collection.isEmpty()) {
            return null;
        }
        else {
            return (T)collection.get(0);
        }
    }

    public static <T> T LastOrDefault(Stream<T> stream) {
        List collection = Arrays.asList(stream.toArray());
        if (collection.isEmpty()) {
            return null;
        }
        else {
            return (T)collection.get(collection.size() - 1);
        }
    }
}
