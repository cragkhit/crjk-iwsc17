package org.apache.tomcat.util.collections;
import java.util.Locale;
private static class Key {
    private final String key;
    private final String lcKey;
    private Key ( final String key ) {
        this.key = key;
        this.lcKey = key.toLowerCase ( Locale.ENGLISH );
    }
    public String getKey() {
        return this.key;
    }
    @Override
    public int hashCode() {
        return this.lcKey.hashCode();
    }
    @Override
    public boolean equals ( final Object obj ) {
        if ( this == obj ) {
            return true;
        }
        if ( obj == null ) {
            return false;
        }
        if ( this.getClass() != obj.getClass() ) {
            return false;
        }
        final Key other = ( Key ) obj;
        return this.lcKey.equals ( other.lcKey );
    }
    public static Key getInstance ( final Object o ) {
        if ( o instanceof String ) {
            return new Key ( ( String ) o );
        }
        return null;
    }
}
