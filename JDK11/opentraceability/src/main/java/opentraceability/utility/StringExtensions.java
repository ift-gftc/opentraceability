package opentraceability.utility;

import org.w3c.dom.Document;
import org.xml.sax.InputSource;

import javax.xml.parsers.DocumentBuilderFactory;
import java.io.StringReader;
import java.net.URI;
import java.net.URISyntaxException;
import java.time.Duration;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.regex.Pattern;

public class StringExtensions {

    private static final Pattern DIGITS_ONLY_REGEX = Pattern.compile("^[0-9]+$");
    private static final Pattern IS_URI_COMPATIBLE_CHARS_REGEX = Pattern.compile("(.*[^._\\-:0-9A-Za-z])");

    public static boolean isOnlyDigits(String str) {
        return DIGITS_ONLY_REGEX.matcher(str).matches();
    }

    public static String Last(String[] strArray) throws Exception
    {
        if (strArray.length == 0)
        {
            throw new Exception("Array is empty when calling Last.");
        }
        else {
            return strArray[strArray.length - 1];
        }
    }

    public static String First(String[] strArray) throws Exception
    {
        if (strArray.length == 0)
        {
            throw new Exception("Array is empty when calling Last.");
        }
        else {
            return strArray[0];
        }
    }

    public static String LastOrDefault(String[] strArray)
    {
        if (strArray.length == 0)
        {
            return null;
        }
        else {
            return strArray[strArray.length - 1];
        }
    }

    public static boolean isURI(String str) {
        try {
            new URI(str);
        } catch (URISyntaxException e) {
            return false;
        }
        return true;
    }

    public static OffsetDateTime tryConvertToDateTimeOffset(String str) {
        DateTimeFormatter formatter = DateTimeFormatter.ISO_OFFSET_DATE_TIME;
        try {
            return OffsetDateTime.parse(str, formatter);
        } catch (DateTimeParseException e) {
            return null;
        }
    }

    public static Duration toDuration(String str) {
        String[] parts = str.split(":");
        long hours = Long.parseLong(parts[0]);
        long minutes = Long.parseLong(parts[1]);
        return Duration.ofHours(hours).plusMinutes(minutes);
    }

    public static List<String> splitXPath(String str) {
        String regex = "(?=[^{}]*(?:{[^{}]*}[^{}]*)*$)";
        str = str.replaceAll(regex, "%SLASH%");
        return List.of(str.split("%SLASH%"));
    }

    public static Document parseXmlToDocument(String str) throws Exception {
        String cleanedXml = removeBOM(str);
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setNamespaceAware(true);
        InputSource inputSource = new InputSource(new StringReader(cleanedXml));
        return factory.newDocumentBuilder().parse(inputSource);
    }

    public static String removeBOM(String str) {
        List<Character> bomMarkers = List.of((char) 0xEF, (char) 0xBB, (char) 0xBF);
        if (bomMarkers.contains(str.charAt(0))) {
            return str.substring(1);
        }
        return str;
    }
}