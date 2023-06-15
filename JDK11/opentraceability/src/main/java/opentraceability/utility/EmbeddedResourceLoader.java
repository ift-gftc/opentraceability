package opentraceability.utility;

import org.w3c.dom.Document;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URLClassLoader;
import java.util.HashMap;
import java.util.Map;

public class EmbeddedResourceLoader {
    private Map<String, ClassLoader> assemblyMap = new HashMap<>();

    private ClassLoader getAssembly(String assemblyName) {
        ClassLoader classLoader = assemblyMap.get(assemblyName);
        if (classLoader == null) {
            ClassLoader assembly = EmbeddedResourceLoader.class.getClassLoader();
            InputStream resourceAsStream = assembly.getResourceAsStream(assemblyName + ".jar");
            URLClassLoader urlClassLoader = new URLClassLoader(new java.net.URL[]{assembly.getResource(assemblyName + ".jar")}, assembly);
            classLoader = urlClassLoader;
            assemblyMap.put(assemblyName, classLoader);
        }
        return classLoader;
    }

    public byte[] readBytes(String assemblyName, String resourceName) {
        byte[] raw;
        try {
            ClassLoader classLoader = getAssembly(assemblyName);
            InputStream stream = classLoader.getResourceAsStream(resourceName);
            if (stream == null) {
                throw new Exception("Failed to find the resource in the assembly " + assemblyName + " with the resource name " + resourceName + ".");
            }
            raw = stream.readAllBytes();
        } catch (Exception ex) {
            System.out.println(ex);
            throw new RuntimeException(ex);
        }
        return raw;
    }

    public String readString(String assemblyName, String resourceName) {
        String result;
        try {
            ClassLoader classLoader = getAssembly(assemblyName);
            InputStream stream = classLoader.getResourceAsStream(resourceName);
            if (stream == null) {
                throw new Exception("Failed to find the resource in the assembly " + assemblyName + " with the resource name " + resourceName + ".");
            }
            result = new BufferedReader(new InputStreamReader(stream)).readLine();
        } catch (Exception ex) {
            System.out.println(ex);
            throw new RuntimeException(ex);
        }
        return result;
    }

    public Document readXML(String assemblyName, String resourceName) {
        String xmlStr = readString(assemblyName, resourceName);

        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            return builder.parse(new ByteArrayInputStream(xmlStr.getBytes()));
        } catch (Exception ex) {
            System.out.println(ex);
            throw new RuntimeException(ex);
        }
    }

    public InputStream readStream(String assemblyName, String resourceName) {
        try {
            ClassLoader classLoader = getAssembly(assemblyName);
            return classLoader.getResourceAsStream(resourceName);
        } catch (Exception ex) {
            System.out.println(ex);
            throw new RuntimeException(ex);
        }
    }
}