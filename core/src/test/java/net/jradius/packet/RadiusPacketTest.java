package net.jradius.packet;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;
import net.jradius.packet.attribute.AttributeList;
import net.jradius.packet.attribute.RadiusAttribute;
import net.jradius.packet.attribute.value.AttributeValue;
import net.jradius.packet.attribute.value.StringValue;

public class RadiusPacketTest {

    private static class TestAttribute extends RadiusAttribute {
        private static final long serialVersionUID = 1L;
        public static final long TYPE = 999;
        private AttributeValue value;
        
        public TestAttribute() { }
        
        public TestAttribute(String value) {
            StringValue sv = new StringValue();
            sv.setString(value);
            this.value = sv;
        }

        @Override
        public AttributeValue getValue() {
            return value;
        }
        
        @Override
        public long getFormattedType() {
            return TYPE;
        }

        @Override
        public long getType() {
            return TYPE;
        }

        @Override
        public void setup() {
        }

        @Override
        public String getAttributeName() {
            return "Test-Attribute";
        }
    }

    @Test
    public void testSetAndGetCode() {
        RadiusPacket packet = new RadiusPacket() {
            private static final long serialVersionUID = 1L;
        };
        packet.setCode(1);
        assertEquals(1, packet.getCode());
    }

    @Test
    public void testSetAndGetIdentifier() {
        RadiusPacket packet = new RadiusPacket() {
            private static final long serialVersionUID = 1L;
        };
        packet.setIdentifier(100);
        assertEquals(100, packet.getIdentifier());
    }

    @Test
    public void testAddAttribute() {
        RadiusPacket packet = new RadiusPacket() {
            private static final long serialVersionUID = 1L;
        };
        TestAttribute attr = new TestAttribute();
        packet.addAttribute(attr);
        
        AttributeList list = packet.getAttributes();
        assertEquals(1, list.getSize());
        assertEquals(attr, list.getAttributeList().get(0));
    }

    @Test
    public void testFindAttribute() {
        RadiusPacket packet = new RadiusPacket() {
            private static final long serialVersionUID = 1L;
        };
        TestAttribute attr = new TestAttribute();
        packet.addAttribute(attr);

        RadiusAttribute found = packet.findAttribute(TestAttribute.TYPE);
        assertNotNull(found);
        assertEquals(attr, found);
    }

    @Test
    public void testGetAttributeValue() {
        RadiusPacket packet = new RadiusPacket() {
            private static final long serialVersionUID = 1L;
        };
        TestAttribute attr = new TestAttribute("hello");
        packet.addAttribute(attr);

        Object value = packet.getAttributeValue(TestAttribute.TYPE);
        assertEquals("hello", value);
    }
}
