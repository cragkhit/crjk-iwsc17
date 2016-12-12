package org.jfree.chart;
import java.util.Map;
import java.awt.RenderingHints;
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
