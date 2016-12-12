package org.apache.catalina.servlets;
import org.apache.catalina.WebResource;
private static class PrecompressedResource {
    public final WebResource resource;
    public final CompressionFormat format;
    private PrecompressedResource ( final WebResource resource, final CompressionFormat format ) {
        this.resource = resource;
        this.format = format;
    }
}
