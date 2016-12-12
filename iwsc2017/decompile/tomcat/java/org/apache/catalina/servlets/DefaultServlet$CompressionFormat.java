package org.apache.catalina.servlets;
import java.io.Serializable;
protected static class CompressionFormat implements Serializable {
    private static final long serialVersionUID = 1L;
    public final String extension;
    public final String encoding;
    public CompressionFormat ( final String extension, final String encoding ) {
        this.extension = extension;
        this.encoding = encoding;
    }
}
