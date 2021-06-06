package ch.jalu.configme.wiki;

import ch.jalu.configme.properties.BaseProperty;
import ch.jalu.configme.properties.TypeBasedProperty;
import ch.jalu.configme.properties.convertresult.ConvertErrorRecorder;
import ch.jalu.configme.properties.types.PropertyType;
import ch.jalu.configme.resource.PropertyReader;
import org.junit.jupiter.api.Test;

public class CustomPropertyTypesWikiTest {

    @Test
    void v() {
        // TODO LJ

        new TypeBasedProperty<>("my.path", (byte) 3, new ByteType());
    }

    private static class ByteType implements PropertyType<Byte> {

        @Override
        public Byte convert(Object object, ConvertErrorRecorder errorRecorder) {
            if (object instanceof Number) {
                return ((Number) object).byteValue();
            }
            return null;
        }

        @Override
        public Object toExportValue(Byte value) {
            return value.intValue();
        }
    }

    private static class ByteProperty extends BaseProperty<Byte> {

        public ByteProperty(String path, Byte defaultValue) {
            super(path, defaultValue);
        }

        @Override
        protected Byte getFromReader(PropertyReader reader, ConvertErrorRecorder errorRecorder) {
            Object value = reader.getObject(getPath());
            if (value instanceof Number) {
                return ((Number) value).byteValue();
            }
            return null;
        }

        @Override
        public Object toExportValue(Byte value) {
            return value.intValue();
        }
    }
}
