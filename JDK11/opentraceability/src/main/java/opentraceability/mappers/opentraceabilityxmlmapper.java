package opentraceability.mappers;

import opentraceability.interfaces.IEvent;
import opentraceability.interfaces.IEventKDE;
import opentraceability.models.common.LanguageString;
import opentraceability.models.events.EPCISVersion;
import opentraceability.models.events.EventAction;
import opentraceability.models.events.EventProduct;
import opentraceability.models.identifiers.Country;
import opentraceability.models.identifiers.GLN;
import opentraceability.models.identifiers.EPC;
import opentraceability.models.identifiers.PGLN;
import opentraceability.models.identifiers.UOM;
import opentraceability.utility.OTLogger;
import opentraceability.utility.StringExtensions;
import opentraceability.utility.attributes.OpenTraceabilityAttribute;
import org.dom4j.Attribute;
import org.dom4j.Element;
import org.dom4j.Node;

import java.net.URI;
import java.time.Duration;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import static opentraceability.mappers.OTMappingTypeInformation.*;

public class OpenTraceabilityXmlMapper {

    public static Element toXml(String xname, Object value, EPCISVersion version, boolean required) {
        if (value != null) {
            Element x = createXmlElement(xname);
            Element xvalue = x;

            // make sure we have created the xml element correctly.
            List<String> xParts = StringExtensions.splitXPath(xname);
            while (xParts.size() > 1) {
                String p = xParts.remove(0);
                if (xvalue.element(p) == null) {
                    xvalue.addElement(p);
                }
                xvalue