package opentraceability.utility;

import java.io.*;
import java.util.*;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import org.w3c.dom.*;
import org.xml.sax.InputSource;

public class Countries {

    public static Map<String, Country> _dirCountries = new HashMap<>();
    public static Map<String, Country> _dirAlpha3Countries = new HashMap<>();
    public static Map<String, Country> _dirNameCountries = new HashMap<>();

    public Countries() throws Exception {
        load();
    }

    public static void load() throws Exception {
        String data = StaticData.readData("/Countries.xml");
        _dirCountries = new HashMap<>();
        _dirNameCountries = new HashMap<>();
        _dirAlpha3Countries = new HashMap<>();
        try {
            DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
            InputSource xmlInput = new InputSource(new StringReader(data));
            Document xmlCountries = dBuilder.parse(xmlInput);

            Node root = xmlCountries.getDocumentElement();

            for (int i = 0; i < root.getChildNodes().getLength(); i++) {
                Node node = root.getChildNodes().item(i);

                if (node.getNodeType() == Node.ELEMENT_NODE) {
                    Element x = (Element) node;
                    Country country = new Country(x);
                    _dirCountries.put(country.abbreviation.toUpperCase(), country);
                    _dirNameCountries.put(country.name.toUpperCase(), country);

                    if (!country.alpha3.isEmpty()) {
                        _dirAlpha3Countries.put(country.alpha3.toUpperCase(), country);
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static List<Country> getCountryList() {
        List<Country> list = new ArrayList<>();

        _dirNameCountries.forEach((k, v) -> list.add(v));
        return list;
    }

    public static Country fromAbbreviation(String code) {
        Country country = null;

        if (!code.isEmpty()) {
            if (_dirCountries != null) {
                if (_dirCountries.containsKey((code.toUpperCase()))) {
                    country = _dirCountries.get(code.toUpperCase());
                }
            }
        }

        return country;
    }

    public static Country fromAlpha3(String code) {
        Country country = null;

        if (!code.isEmpty()) {
            if (_dirAlpha3Countries != null) {
                if (_dirAlpha3Countries.containsKey((code.toUpperCase()))) {
                    country = _dirAlpha3Countries.get(code.toUpperCase());
                }
            }
        }

        return country;
    }

    public static Country fromCountryName(String name) {
        Country country = null;

        if (_dirCountries != null && !name.isEmpty()) {

            if (_dirNameCountries.containsKey((name.toUpperCase()))) {
                country = _dirNameCountries.get(name.toUpperCase());
            } else {
                loop:
                for (Map.Entry<String, Country> element : _dirNameCountries.entrySet()) {
                    if (element.getValue().alternativeName != null) {
                        if (element.getValue().alternativeName.equalsIgnoreCase(name)) {
                            country = element.getValue();
                            break;
                        }
                    }
                }
            }
        }

        return country;
    }

    public static Country fromCountryIso(int iso) {
        Country country = null;

        if (_dirCountries != null) {

            loop:
            for (Map.Entry<String, Country> element : _dirCountries.entrySet()) {
                if (element.getValue().iso == iso) {
                    country = element.getValue();
                    break;
                }
            }

            return country;
        }

        return country;
    }

    public static Country parse(String strValue) {
        try {
            Integer parsedInt = Integer.parseInt(strValue);
            return fromCountryIso(parsedInt);
        }
        catch (Exception ex)
        {
            return fromAbbreviation(strValue) != null ? fromAbbreviation(strValue) : fromAlpha3(strValue) != null ? fromAlpha3(strValue) : fromCountryName(strValue);
        }
    }
}