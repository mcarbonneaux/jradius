package net.jradius.packet.attribute;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;
import java.util.Map;
import net.jradius.packet.attribute.value.AttributeValue;

public class AttributeFactoryTest {

    private static class MockDictionary implements AttributeDictionary {
        @Override
        public void loadVendorCodes(Map<Long, Class<?>> map) {}

        @Override
        public void loadAttributes(Map<Long, Class<?>> map) {
            map.put(1L, MockAttribute.class);
        }

        @Override
        public void loadAttributesNames(Map<String, Class<?>> map) {
            map.put("Mock-Attribute", MockAttribute.class);
        }
    }

    public static class MockAttribute extends RadiusAttribute {
        private static final long serialVersionUID = 1L;
        public MockAttribute() {
            setup();
        }
        @Override
        public long getFormattedType() { return 1L; }
        @Override
        public long getType() { return 1L; }
        @Override
        public void setup() {
            attributeName = "Mock-Attribute";
            attributeType = 1L;
            attributeValue = new net.jradius.packet.attribute.value.StringValue();
        }
    }

    @Test
    public void testLoadDictionary() throws Exception {
        AttributeFactory.loadAttributeDictionary(new MockDictionary());
        RadiusAttribute attr = AttributeFactory.newAttribute("Mock-Attribute");
        assertNotNull(attr);
        assertTrue(attr instanceof MockAttribute);
        assertEquals(1L, attr.getType());
    }

    @Test
    public void testNewAttributeByType() throws Exception {
        AttributeFactory.loadAttributeDictionary(new MockDictionary());
        RadiusAttribute attr = AttributeFactory.newAttribute(1L, null, false);
        assertNotNull(attr);
        assertTrue(attr instanceof MockAttribute);
        assertNotNull(attr.getValue());
    }

    @Test
    public void testUnknownAttribute() throws Exception {
        RadiusAttribute attr = AttributeFactory.newAttribute(999L, null, false);
        assertNotNull(attr);
        assertTrue(attr instanceof Attr_UnknownAttribute);
        assertEquals(999L, attr.getType());
        assertNotNull(attr.getValue());
    }
}
