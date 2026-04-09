package net.jradius.packet.attribute;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;
import java.util.List;
import net.jradius.packet.attribute.value.AttributeValue;
import net.jradius.packet.attribute.value.StringValue;

public class AttributeListTest {

    private static class TestAttribute extends RadiusAttribute {
        private static final long serialVersionUID = 1L;
        private final long type;
        private AttributeValue value;

        public TestAttribute(long type) {
            this.type = type;
        }

        public TestAttribute(long type, String value) {
            this.type = type;
            StringValue sv = new StringValue();
            sv.setString(value);
            this.value = sv;
        }

        @Override
        public long getFormattedType() { return type; }
        @Override
        public long getType() { return type; }
        @Override
        public void setup() {}
        @Override
        public String getAttributeName() { return "Test-" + type; }
        @Override
        public AttributeValue getValue() { return value; }
    }

    @Test
    public void testAddAndGet() {
        AttributeList list = new AttributeList();
        TestAttribute attr = new TestAttribute(1, "val1");
        list.add(attr);
        
        assertEquals(1, list.getSize());
        assertEquals(attr, list.get(1));
    }

    @Test
    public void testAddMultipleSameTypeNoOverwrite() {
        AttributeList list = new AttributeList();
        TestAttribute attr1 = new TestAttribute(1, "val1");
        TestAttribute attr2 = new TestAttribute(1, "val2");
        
        list.add(attr1, false);
        list.add(attr2, false);
        
        assertEquals(2, list.getSize());
        
        Object result = list.get(1, false);
        assertTrue(result instanceof List);
        List<?> resList = (List<?>) result;
        assertEquals(2, resList.size());
        assertEquals(attr1, resList.get(0));
        assertEquals(attr2, resList.get(1));
    }

    @Test
    public void testAddMultipleSameTypeOverwrite() {
        AttributeList list = new AttributeList();
        TestAttribute attr1 = new TestAttribute(1, "val1");
        TestAttribute attr2 = new TestAttribute(1, "val2");
        
        list.add(attr1, true);
        list.add(attr2, true);
        
        assertEquals(1, list.getSize());
        assertEquals(attr2, list.get(1));
    }

    @Test
    public void testRemoveByType() {
        AttributeList list = new AttributeList();
        list.add(new TestAttribute(1));
        list.add(new TestAttribute(2));
        
        list.remove(1);
        assertEquals(1, list.getSize());
        assertNull(list.get(1));
        assertNotNull(list.get(2));
    }

    @Test
    public void testClear() {
        AttributeList list = new AttributeList();
        list.add(new TestAttribute(1));
        list.clear();
        assertEquals(0, list.getSize());
    }
}
