package org.jfree.chart;
import java.util.Map;
import java.awt.RenderingHints;
public final class ChartHints {
    public static final Key KEY_BEGIN_ELEMENT;
    public static final Key KEY_END_ELEMENT;
    static {
        KEY_BEGIN_ELEMENT = new Key ( 0 );
        KEY_END_ELEMENT = new Key ( 1 );
    }
    public static class Key extends RenderingHints.Key {
        public Key ( final int privateKey ) {
            super ( privateKey );
        }
        @Override
        public boolean isCompatibleValue ( final Object val ) {
            switch ( this.intKey() ) {
            case 0: {
                return val == null || val instanceof String || val instanceof Map;
            }
            case 1: {
                return val == null || val instanceof Object;
            }
            default: {
                throw new RuntimeException ( "Not possible!" );
            }
            }
        }
    }
}
