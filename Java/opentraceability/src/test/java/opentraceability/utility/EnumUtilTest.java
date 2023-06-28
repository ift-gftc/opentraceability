package opentraceability.utility;

import opentraceability.utility.attributes.Description;
import opentraceability.utility.attributes.Display;
import opentraceability.utility.attributes.OpenTraceabilityAttribute;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import static org.junit.jupiter.api.Assertions.*;

class EnumUtilTest {

    @Test
    void getEnumDescription() throws NoSuchFieldException {
        String desc = EnumUtil.GetEnumDescription(TestEnum.Hello);
        assertEquals(desc, "hello_description");
    }

    @Test
    void getEnumDisplayName() throws NoSuchFieldException {
        String desc = EnumUtil.GetEnumDisplayName(TestEnum.Hello);
        assertEquals(desc, "hello_display");
    }

    @Test
    void getEnumAttributes() throws NoSuchFieldException {
        var att = EnumUtil.GetEnumAttributes(TestEnum.Hello, OpenTraceabilityAttribute.class);
        assertNotNull(att);
        assertEquals(att.size(), 1);
    }

    @Test
    void getValues() {
        var values = EnumUtil.GetValues(TestEnum.class);
        assertNotNull(values);
        assertEquals(values.size(), 2);
    }
}

enum TestEnum {

    @Display(Name = "hello_display")
    @Description(Description = "hello_description")
    @OpenTraceabilityAttribute(ns = "", name="hello")
    Hello,

    @Display(Name = "world_display")
    @Description(Description = "world_description")
    @OpenTraceabilityAttribute(ns = "", name="world")
    World
}