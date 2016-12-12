package org.apache.tomcat.util.descriptor.tld;
import java.lang.reflect.Method;
import org.apache.tomcat.util.digester.Rule;
private static class GenericBooleanRule extends Rule {
    private final Method setter;
    private GenericBooleanRule ( final Class<?> type, final String setterName ) {
        try {
            this.setter = type.getMethod ( setterName, Boolean.TYPE );
        } catch ( NoSuchMethodException e ) {
            throw new IllegalArgumentException ( e );
        }
    }
    @Override
    public void body ( final String namespace, final String name, String text ) throws Exception {
        if ( null != text ) {
            text = text.trim();
        }
        final boolean value = "true".equalsIgnoreCase ( text ) || "yes".equalsIgnoreCase ( text );
        this.setter.invoke ( this.digester.peek(), value );
    }
}
