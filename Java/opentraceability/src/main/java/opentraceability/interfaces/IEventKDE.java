package opentraceability.interfaces;

import opentraceability.utility.ReflectionUtility;
import opentraceability.utility.XElement;
import org.json.JSONObject;
import org.w3c.dom.Element;


import java.util.HashMap;

public abstract class IEventKDE {

    public String namespace = "";
    public String name = "";
    public Class valueType = null;


    public abstract void setFromJson(Object json) throws Exception;

    public abstract Object getJson() throws Exception;

    public abstract void setFromXml(XElement xml) throws Exception;

    public abstract XElement getXml() throws Exception;

    static HashMap<String, Class> registeredKDEs = new HashMap<String, Class>();

    public static void RegisterKDE(Class type, String ns, String name) throws Exception {
        String key = ns + ":" + name;

        if (!registeredKDEs.containsKey(key)) {
            registeredKDEs.put(key, type);
        } else {
            String fullName = registeredKDEs.get(key).getTypeName();
            throw new Exception("The KDE " + key + " is already registered with type " + fullName);
        }
    }

    public static IEventKDE initializeKDE(String ns, String name) {
        IEventKDE kde = null;

        String key = ns + ":" + name;

        Class kdeType = registeredKDEs.get(key);

        if (kdeType != null) {
            try {
                kde = (IEventKDE) ReflectionUtility.constructType(kdeType);
            } catch (InstantiationException | IllegalAccessException | ClassNotFoundException e) {
                e.printStackTrace();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }

        if (kde != null) {
            kde.namespace = ns;
            kde.name = name;
        }

        return kde;
    }
}