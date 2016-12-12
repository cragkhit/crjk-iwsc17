package org.apache.jasper.compiler;
import org.apache.tomcat.Jar;
import java.net.URLConnection;
import java.net.URL;
import org.xml.sax.SAXException;
import java.io.IOException;
import org.apache.jasper.JasperException;
import java.util.Iterator;
import java.util.HashMap;
import org.apache.tomcat.util.descriptor.tld.TaglibXml;
import org.apache.tomcat.util.descriptor.tld.TldParser;
import org.apache.tomcat.util.descriptor.tld.TldResourcePath;
import java.util.Map;
import javax.servlet.ServletContext;
public class TldCache {
    public static final String SERVLET_CONTEXT_ATTRIBUTE_NAME;
    private final ServletContext servletContext;
    private final Map<String, TldResourcePath> uriTldResourcePathMap;
    private final Map<TldResourcePath, TaglibXmlCacheEntry> tldResourcePathTaglibXmlMap;
    private final TldParser tldParser;
    public static TldCache getInstance ( final ServletContext servletContext ) {
        if ( servletContext == null ) {
            throw new IllegalArgumentException ( Localizer.getMessage ( "org.apache.jasper.compiler.TldCache.servletContextNull" ) );
        }
        return ( TldCache ) servletContext.getAttribute ( TldCache.SERVLET_CONTEXT_ATTRIBUTE_NAME );
    }
    public TldCache ( final ServletContext servletContext, final Map<String, TldResourcePath> uriTldResourcePathMap, final Map<TldResourcePath, TaglibXml> tldResourcePathTaglibXmlMap ) {
        this.uriTldResourcePathMap = new HashMap<String, TldResourcePath>();
        this.tldResourcePathTaglibXmlMap = new HashMap<TldResourcePath, TaglibXmlCacheEntry>();
        this.servletContext = servletContext;
        this.uriTldResourcePathMap.putAll ( uriTldResourcePathMap );
        for ( final Map.Entry<TldResourcePath, TaglibXml> entry : tldResourcePathTaglibXmlMap.entrySet() ) {
            final TldResourcePath tldResourcePath = entry.getKey();
            final long[] lastModified = this.getLastModified ( tldResourcePath );
            final TaglibXmlCacheEntry cacheEntry = new TaglibXmlCacheEntry ( entry.getValue(), lastModified[0], lastModified[1] );
            this.tldResourcePathTaglibXmlMap.put ( tldResourcePath, cacheEntry );
        }
        final boolean validate = Boolean.parseBoolean ( servletContext.getInitParameter ( "org.apache.jasper.XML_VALIDATE_TLD" ) );
        final String blockExternalString = servletContext.getInitParameter ( "org.apache.jasper.XML_BLOCK_EXTERNAL" );
        final boolean blockExternal = blockExternalString == null || Boolean.parseBoolean ( blockExternalString );
        this.tldParser = new TldParser ( true, validate, blockExternal );
    }
    public TldResourcePath getTldResourcePath ( final String uri ) {
        return this.uriTldResourcePathMap.get ( uri );
    }
    public TaglibXml getTaglibXml ( final TldResourcePath tldResourcePath ) throws JasperException {
        final TaglibXmlCacheEntry cacheEntry = this.tldResourcePathTaglibXmlMap.get ( tldResourcePath );
        if ( cacheEntry == null ) {
            return null;
        }
        final long[] lastModified = this.getLastModified ( tldResourcePath );
        if ( lastModified[0] != cacheEntry.getWebAppPathLastModified() || lastModified[1] != cacheEntry.getEntryLastModified() ) {
            synchronized ( cacheEntry ) {
                if ( lastModified[0] == cacheEntry.getWebAppPathLastModified() ) {
                    if ( lastModified[1] == cacheEntry.getEntryLastModified() ) {
                        return cacheEntry.getTaglibXml();
                    }
                }
                TaglibXml updatedTaglibXml;
                try {
                    updatedTaglibXml = this.tldParser.parse ( tldResourcePath );
                } catch ( IOException | SAXException e ) {
                    throw new JasperException ( e );
                }
                cacheEntry.setTaglibXml ( updatedTaglibXml );
                cacheEntry.setWebAppPathLastModified ( lastModified[0] );
                cacheEntry.setEntryLastModified ( lastModified[1] );
            }
        }
        return cacheEntry.getTaglibXml();
    }
    private long[] getLastModified ( final TldResourcePath tldResourcePath ) {
        final long[] result = { -1L, -1L };
        try {
            final String webappPath = tldResourcePath.getWebappPath();
            if ( webappPath != null ) {
                final URL url = this.servletContext.getResource ( tldResourcePath.getWebappPath() );
                final URLConnection conn = url.openConnection();
                result[0] = conn.getLastModified();
                if ( "file".equals ( url.getProtocol() ) ) {
                    conn.getInputStream().close();
                }
            }
            try ( final Jar jar = tldResourcePath.openJar() ) {
                if ( jar != null ) {
                    result[1] = jar.getLastModified ( tldResourcePath.getEntryName() );
                }
            }
        } catch ( IOException ex ) {}
        return result;
    }
    static {
        SERVLET_CONTEXT_ATTRIBUTE_NAME = TldCache.class.getName();
    }
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
}
