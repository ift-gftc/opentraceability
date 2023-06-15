package opentraceability.interfaces;

import org.json.JSONObject;
import org.w3c.dom.Element;

import java.lang.reflect.Type;
import java.util.HashMap;

public abstract class IEventKDE {
    static HashMap<String, Type> registeredKDEs = new HashMap<String, Type>();

    static void RegisterKDE(Type type, String ns, String name) throws Exception {
        String key = ns + ":" + name;

        if (!registeredKDEs.containsKey(key)) {
            registeredKDEs.put(key, type);
        } else {
            String fullName = registeredKDEs.get(key).getTypeName();
            throw new Exception("The KDE " + key + " is already registered with type " + fullName);
        }
    }

    static IEventKDE initializeKDE(String ns, String name) {
        IEventKDE kde = null;

        String key = ns + ":" + name;

        Type kdeType = registeredKDEs.get(key);

        if (kdeType != null) {
            try {
                kde = (IEventKDE) Class.forName(key).newInstance();
            } catch (InstantiationException | IllegalAccessException | ClassNotFoundException e) {
                e.printStackTrace();
            }
        }

        if (kde != null) {
            kde.namespace = ns;
            kde.name = name;
        }

        return kde;
    }

    String namespace = "";
    String name = "";
    Type valueType = null;

    abstract void setFromJson(JSONObject json);

    abstract Object getJson();

    abstract void setFromXml(Element xml);

    abstract Element getXml();
}