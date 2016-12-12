package org.apache.jasper.compiler;
import org.apache.tomcat.util.descriptor.tld.TaglibXml;
private static class TaglibXmlCacheEntry {
    private volatile TaglibXml taglibXml;
    private volatile long webAppPathLastModified;
    private volatile long entryLastModified;
    public TaglibXmlCacheEntry ( final TaglibXml taglibXml, final long webAppPathLastModified, final long entryLastModified ) {
        this.taglibXml = taglibXml;
        this.webAppPathLastModified = webAppPathLastModified;
        this.entryLastModified = entryLastModified;
    }
    public TaglibXml getTaglibXml() {
        return this.taglibXml;
    }
    public void setTaglibXml ( final TaglibXml taglibXml ) {
        this.taglibXml = taglibXml;
    }
    public long getWebAppPathLastModified() {
        return this.webAppPathLastModified;
    }
    public void setWebAppPathLastModified ( final long webAppPathLastModified ) {
        this.webAppPathLastModified = webAppPathLastModified;
    }
    public long getEntryLastModified() {
        return this.entryLastModified;
    }
    public void setEntryLastModified ( final long entryLastModified ) {
        this.entryLastModified = entryLastModified;
    }
}
