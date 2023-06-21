package opentraceability;

import java.util.ArrayList;
import java.util.List;

public class OTLogger {
    private static final List<OnLogDelegate> listeners = new ArrayList<>();

    public static void addListener(OnLogDelegate listener) {
        listeners.add(listener);
    }

    public static void removeListener(OnLogDelegate listener) {
        listeners.remove(listener);
    }

    public static void error(Exception ex) {
        OTLog log = new OTLog();
        log.Level = LogLevel.Error;
        log.Exception = ex;
        notifyListeners(log);
    }

    public static void notifyListeners(OTLog log) {
        for (OnLogDelegate listener : listeners) {
            listener.invoke(log);
        }
    }
}