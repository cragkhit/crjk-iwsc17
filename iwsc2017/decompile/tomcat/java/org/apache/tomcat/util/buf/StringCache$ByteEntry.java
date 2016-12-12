package org.apache.tomcat.util.buf;
import java.nio.charset.Charset;
private static class ByteEntry {
    private byte[] name;
    private Charset charset;
    private String value;
    private ByteEntry() {
        this.name = null;
        this.charset = null;
        this.value = null;
    }
    @Override
    public String toString() {
        return this.value;
    }
    @Override
    public int hashCode() {
        return this.value.hashCode();
    }
    @Override
    public boolean equals ( final Object obj ) {
        return obj instanceof ByteEntry && this.value.equals ( ( ( ByteEntry ) obj ).value );
    }
}
