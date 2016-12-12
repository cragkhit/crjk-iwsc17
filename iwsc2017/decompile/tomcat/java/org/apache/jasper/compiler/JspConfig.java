package org.apache.jasper.compiler;
import java.util.ArrayList;
import java.util.Iterator;
import javax.servlet.descriptor.JspConfigDescriptor;
import java.util.Collection;
import javax.servlet.descriptor.JspPropertyGroupDescriptor;
import org.apache.juli.logging.LogFactory;
import javax.servlet.ServletContext;
import java.util.Vector;
import org.apache.juli.logging.Log;
public class JspConfig {
    private final Log log;
    private Vector<JspPropertyGroup> jspProperties;
    private final ServletContext ctxt;
    private volatile boolean initialized;
    private static final String defaultIsXml;
    private String defaultIsELIgnored;
    private static final String defaultIsScriptingInvalid;
    private String defaultDeferedSyntaxAllowedAsLiteral;
    private static final String defaultTrimDirectiveWhitespaces;
    private static final String defaultDefaultContentType;
    private static final String defaultBuffer;
    private static final String defaultErrorOnUndeclaredNamespace = "false";
    private JspProperty defaultJspProperty;
    public JspConfig ( final ServletContext ctxt ) {
        this.log = LogFactory.getLog ( JspConfig.class );
        this.jspProperties = null;
        this.initialized = false;
        this.defaultIsELIgnored = null;
        this.defaultDeferedSyntaxAllowedAsLiteral = null;
        this.ctxt = ctxt;
    }
    private void processWebDotXml() {
        if ( this.ctxt.getEffectiveMajorVersion() < 2 ) {
            this.defaultIsELIgnored = "true";
            this.defaultDeferedSyntaxAllowedAsLiteral = "true";
            return;
        }
        if ( this.ctxt.getEffectiveMajorVersion() == 2 ) {
            if ( this.ctxt.getEffectiveMinorVersion() < 5 ) {
                this.defaultDeferedSyntaxAllowedAsLiteral = "true";
            }
            if ( this.ctxt.getEffectiveMinorVersion() < 4 ) {
                this.defaultIsELIgnored = "true";
                return;
            }
        }
        final JspConfigDescriptor jspConfig = this.ctxt.getJspConfigDescriptor();
        if ( jspConfig == null ) {
            return;
        }
        this.jspProperties = new Vector<JspPropertyGroup>();
        final Collection<JspPropertyGroupDescriptor> jspPropertyGroups = ( Collection<JspPropertyGroupDescriptor> ) jspConfig.getJspPropertyGroups();
        for ( final JspPropertyGroupDescriptor jspPropertyGroup : jspPropertyGroups ) {
            final Collection<String> urlPatterns = ( Collection<String> ) jspPropertyGroup.getUrlPatterns();
            if ( urlPatterns.size() == 0 ) {
                continue;
            }
            final JspProperty property = new JspProperty ( jspPropertyGroup.getIsXml(), jspPropertyGroup.getElIgnored(), jspPropertyGroup.getScriptingInvalid(), jspPropertyGroup.getPageEncoding(), jspPropertyGroup.getIncludePreludes(), jspPropertyGroup.getIncludeCodas(), jspPropertyGroup.getDeferredSyntaxAllowedAsLiteral(), jspPropertyGroup.getTrimDirectiveWhitespaces(), jspPropertyGroup.getDefaultContentType(), jspPropertyGroup.getBuffer(), jspPropertyGroup.getErrorOnUndeclaredNamespace() );
            for ( final String urlPattern : urlPatterns ) {
                String path = null;
                String extension = null;
                if ( urlPattern.indexOf ( 42 ) < 0 ) {
                    path = urlPattern;
                } else {
                    final int i = urlPattern.lastIndexOf ( 47 );
                    String file;
                    if ( i >= 0 ) {
                        path = urlPattern.substring ( 0, i + 1 );
                        file = urlPattern.substring ( i + 1 );
                    } else {
                        file = urlPattern;
                    }
                    if ( file.equals ( "*" ) ) {
                        extension = "*";
                    } else if ( file.startsWith ( "*." ) ) {
                        extension = file.substring ( file.indexOf ( 46 ) + 1 );
                    }
                    final boolean isStar = "*".equals ( extension );
                    if ( ( path == null && ( extension == null || isStar ) ) || ( path != null && !isStar ) ) {
                        if ( this.log.isWarnEnabled() ) {
                            this.log.warn ( Localizer.getMessage ( "jsp.warning.bad.urlpattern.propertygroup", urlPattern ) );
                            continue;
                        }
                        continue;
                    }
                }
                final JspPropertyGroup propertyGroup = new JspPropertyGroup ( path, extension, property );
                this.jspProperties.addElement ( propertyGroup );
            }
        }
    }
    private void init() {
        if ( !this.initialized ) {
            synchronized ( this ) {
                if ( !this.initialized ) {
                    this.processWebDotXml();
                    this.defaultJspProperty = new JspProperty ( JspConfig.defaultIsXml, this.defaultIsELIgnored, JspConfig.defaultIsScriptingInvalid, null, null, null, this.defaultDeferedSyntaxAllowedAsLiteral, JspConfig.defaultTrimDirectiveWhitespaces, JspConfig.defaultDefaultContentType, JspConfig.defaultBuffer, "false" );
                    this.initialized = true;
                }
            }
        }
    }
    private JspPropertyGroup selectProperty ( final JspPropertyGroup prev, final JspPropertyGroup curr ) {
        if ( prev == null ) {
            return curr;
        }
        if ( prev.getExtension() == null ) {
            return prev;
        }
        if ( curr.getExtension() == null ) {
            return curr;
        }
        final String prevPath = prev.getPath();
        final String currPath = curr.getPath();
        if ( prevPath == null && currPath == null ) {
            return prev;
        }
        if ( prevPath == null && currPath != null ) {
            return curr;
        }
        if ( prevPath != null && currPath == null ) {
            return prev;
        }
        if ( prevPath.length() >= currPath.length() ) {
            return prev;
        }
        return curr;
    }
    public JspProperty findJspProperty ( final String uri ) {
        this.init();
        if ( this.jspProperties == null || uri.endsWith ( ".tag" ) || uri.endsWith ( ".tagx" ) ) {
            return this.defaultJspProperty;
        }
        String uriPath = null;
        int index = uri.lastIndexOf ( 47 );
        if ( index >= 0 ) {
            uriPath = uri.substring ( 0, index + 1 );
        }
        String uriExtension = null;
        index = uri.lastIndexOf ( 46 );
        if ( index >= 0 ) {
            uriExtension = uri.substring ( index + 1 );
        }
        final Collection<String> includePreludes = new ArrayList<String>();
        final Collection<String> includeCodas = new ArrayList<String>();
        JspPropertyGroup isXmlMatch = null;
        JspPropertyGroup elIgnoredMatch = null;
        JspPropertyGroup scriptingInvalidMatch = null;
        JspPropertyGroup pageEncodingMatch = null;
        JspPropertyGroup deferedSyntaxAllowedAsLiteralMatch = null;
        JspPropertyGroup trimDirectiveWhitespacesMatch = null;
        JspPropertyGroup defaultContentTypeMatch = null;
        JspPropertyGroup bufferMatch = null;
        JspPropertyGroup errorOnUndeclaredNamespaceMatch = null;
        for ( final JspPropertyGroup jpg : this.jspProperties ) {
            final JspProperty jp = jpg.getJspProperty();
            final String extension = jpg.getExtension();
            final String path = jpg.getPath();
            if ( extension == null ) {
                if ( !uri.equals ( path ) ) {
                    continue;
                }
            } else {
                if ( path != null && uriPath != null && !uriPath.startsWith ( path ) ) {
                    continue;
                }
                if ( !extension.equals ( "*" ) && !extension.equals ( uriExtension ) ) {
                    continue;
                }
            }
            if ( jp.getIncludePrelude() != null ) {
                includePreludes.addAll ( jp.getIncludePrelude() );
            }
            if ( jp.getIncludeCoda() != null ) {
                includeCodas.addAll ( jp.getIncludeCoda() );
            }
            if ( jp.isXml() != null ) {
                isXmlMatch = this.selectProperty ( isXmlMatch, jpg );
            }
            if ( jp.isELIgnored() != null ) {
                elIgnoredMatch = this.selectProperty ( elIgnoredMatch, jpg );
            }
            if ( jp.isScriptingInvalid() != null ) {
                scriptingInvalidMatch = this.selectProperty ( scriptingInvalidMatch, jpg );
            }
            if ( jp.getPageEncoding() != null ) {
                pageEncodingMatch = this.selectProperty ( pageEncodingMatch, jpg );
            }
            if ( jp.isDeferedSyntaxAllowedAsLiteral() != null ) {
                deferedSyntaxAllowedAsLiteralMatch = this.selectProperty ( deferedSyntaxAllowedAsLiteralMatch, jpg );
            }
            if ( jp.isTrimDirectiveWhitespaces() != null ) {
                trimDirectiveWhitespacesMatch = this.selectProperty ( trimDirectiveWhitespacesMatch, jpg );
            }
            if ( jp.getDefaultContentType() != null ) {
                defaultContentTypeMatch = this.selectProperty ( defaultContentTypeMatch, jpg );
            }
            if ( jp.getBuffer() != null ) {
                bufferMatch = this.selectProperty ( bufferMatch, jpg );
            }
            if ( jp.isErrorOnUndeclaredNamespace() != null ) {
                errorOnUndeclaredNamespaceMatch = this.selectProperty ( errorOnUndeclaredNamespaceMatch, jpg );
            }
        }
        String isXml = JspConfig.defaultIsXml;
        String isELIgnored = this.defaultIsELIgnored;
        String isScriptingInvalid = JspConfig.defaultIsScriptingInvalid;
        String pageEncoding = null;
        String isDeferedSyntaxAllowedAsLiteral = this.defaultDeferedSyntaxAllowedAsLiteral;
        String isTrimDirectiveWhitespaces = JspConfig.defaultTrimDirectiveWhitespaces;
        String defaultContentType = JspConfig.defaultDefaultContentType;
        String buffer = JspConfig.defaultBuffer;
        String errorOnUndelcaredNamespace = "false";
        if ( isXmlMatch != null ) {
            isXml = isXmlMatch.getJspProperty().isXml();
        }
        if ( elIgnoredMatch != null ) {
            isELIgnored = elIgnoredMatch.getJspProperty().isELIgnored();
        }
        if ( scriptingInvalidMatch != null ) {
            isScriptingInvalid = scriptingInvalidMatch.getJspProperty().isScriptingInvalid();
        }
        if ( pageEncodingMatch != null ) {
            pageEncoding = pageEncodingMatch.getJspProperty().getPageEncoding();
        }
        if ( deferedSyntaxAllowedAsLiteralMatch != null ) {
            isDeferedSyntaxAllowedAsLiteral = deferedSyntaxAllowedAsLiteralMatch.getJspProperty().isDeferedSyntaxAllowedAsLiteral();
        }
        if ( trimDirectiveWhitespacesMatch != null ) {
            isTrimDirectiveWhitespaces = trimDirectiveWhitespacesMatch.getJspProperty().isTrimDirectiveWhitespaces();
        }
        if ( defaultContentTypeMatch != null ) {
            defaultContentType = defaultContentTypeMatch.getJspProperty().getDefaultContentType();
        }
        if ( bufferMatch != null ) {
            buffer = bufferMatch.getJspProperty().getBuffer();
        }
        if ( errorOnUndeclaredNamespaceMatch != null ) {
            errorOnUndelcaredNamespace = errorOnUndeclaredNamespaceMatch.getJspProperty().isErrorOnUndeclaredNamespace();
        }
        return new JspProperty ( isXml, isELIgnored, isScriptingInvalid, pageEncoding, includePreludes, includeCodas, isDeferedSyntaxAllowedAsLiteral, isTrimDirectiveWhitespaces, defaultContentType, buffer, errorOnUndelcaredNamespace );
    }
    public boolean isJspPage ( final String uri ) {
        this.init();
        if ( this.jspProperties == null ) {
            return false;
        }
        String uriPath = null;
        int index = uri.lastIndexOf ( 47 );
        if ( index >= 0 ) {
            uriPath = uri.substring ( 0, index + 1 );
        }
        String uriExtension = null;
        index = uri.lastIndexOf ( 46 );
        if ( index >= 0 ) {
            uriExtension = uri.substring ( index + 1 );
        }
        for ( final JspPropertyGroup jpg : this.jspProperties ) {
            final String extension = jpg.getExtension();
            final String path = jpg.getPath();
            if ( extension == null ) {
                if ( uri.equals ( path ) ) {
                    return true;
                }
                continue;
            } else {
                if ( ( path == null || path.equals ( uriPath ) ) && ( extension.equals ( "*" ) || extension.equals ( uriExtension ) ) ) {
                    return true;
                }
                continue;
            }
        }
        return false;
    }
    static {
        defaultIsXml = null;
        defaultIsScriptingInvalid = null;
        defaultTrimDirectiveWhitespaces = null;
        defaultDefaultContentType = null;
        defaultBuffer = null;
    }
    public static class JspPropertyGroup {
        private final String path;
        private final String extension;
        private final JspProperty jspProperty;
        JspPropertyGroup ( final String path, final String extension, final JspProperty jspProperty ) {
            this.path = path;
            this.extension = extension;
            this.jspProperty = jspProperty;
        }
        public String getPath() {
            return this.path;
        }
        public String getExtension() {
            return this.extension;
        }
        public JspProperty getJspProperty() {
            return this.jspProperty;
        }
    }
    public static class JspProperty {
        private final String isXml;
        private final String elIgnored;
        private final String scriptingInvalid;
        private final String pageEncoding;
        private final Collection<String> includePrelude;
        private final Collection<String> includeCoda;
        private final String deferedSyntaxAllowedAsLiteral;
        private final String trimDirectiveWhitespaces;
        private final String defaultContentType;
        private final String buffer;
        private final String errorOnUndeclaredNamespace;
        public JspProperty ( final String isXml, final String elIgnored, final String scriptingInvalid, final String pageEncoding, final Collection<String> includePrelude, final Collection<String> includeCoda, final String deferedSyntaxAllowedAsLiteral, final String trimDirectiveWhitespaces, final String defaultContentType, final String buffer, final String errorOnUndeclaredNamespace ) {
            this.isXml = isXml;
            this.elIgnored = elIgnored;
            this.scriptingInvalid = scriptingInvalid;
            this.pageEncoding = pageEncoding;
            this.includePrelude = includePrelude;
            this.includeCoda = includeCoda;
            this.deferedSyntaxAllowedAsLiteral = deferedSyntaxAllowedAsLiteral;
            this.trimDirectiveWhitespaces = trimDirectiveWhitespaces;
            this.defaultContentType = defaultContentType;
            this.buffer = buffer;
            this.errorOnUndeclaredNamespace = errorOnUndeclaredNamespace;
        }
        public String isXml() {
            return this.isXml;
        }
        public String isELIgnored() {
            return this.elIgnored;
        }
        public String isScriptingInvalid() {
            return this.scriptingInvalid;
        }
        public String getPageEncoding() {
            return this.pageEncoding;
        }
        public Collection<String> getIncludePrelude() {
            return this.includePrelude;
        }
        public Collection<String> getIncludeCoda() {
            return this.includeCoda;
        }
        public String isDeferedSyntaxAllowedAsLiteral() {
            return this.deferedSyntaxAllowedAsLiteral;
        }
        public String isTrimDirectiveWhitespaces() {
            return this.trimDirectiveWhitespaces;
        }
        public String getDefaultContentType() {
            return this.defaultContentType;
        }
        public String getBuffer() {
            return this.buffer;
        }
        public String isErrorOnUndeclaredNamespace() {
            return this.errorOnUndeclaredNamespace;
        }
    }
}
