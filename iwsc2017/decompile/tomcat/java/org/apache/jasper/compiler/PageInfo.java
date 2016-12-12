package org.apache.jasper.compiler;
import org.apache.jasper.JasperException;
import java.util.List;
import org.apache.jasper.Constants;
import java.util.Collection;
import java.util.HashSet;
import javax.el.ExpressionFactory;
import java.util.LinkedList;
import javax.servlet.jsp.tagext.TagLibraryInfo;
import java.util.HashMap;
import java.util.Set;
import java.util.Map;
import java.util.Vector;
class PageInfo {
    private final Vector<String> imports;
    private final Map<String, Long> dependants;
    private final BeanRepository beanRepository;
    private final Set<String> varInfoNames;
    private final HashMap<String, TagLibraryInfo> taglibsMap;
    private final HashMap<String, String> jspPrefixMapper;
    private final HashMap<String, LinkedList<String>> xmlPrefixMapper;
    private final HashMap<String, Mark> nonCustomTagPrefixMap;
    private final String jspFile;
    private static final String defaultLanguage = "java";
    private String language;
    private final String defaultExtends;
    private String xtends;
    private String contentType;
    private String session;
    private boolean isSession;
    private String bufferValue;
    private int buffer;
    private String autoFlush;
    private boolean isAutoFlush;
    private String isThreadSafeValue;
    private boolean isThreadSafe;
    private String isErrorPageValue;
    private boolean isErrorPage;
    private String errorPage;
    private String info;
    private boolean scriptless;
    private boolean scriptingInvalid;
    private String isELIgnoredValue;
    private boolean isELIgnored;
    private String deferredSyntaxAllowedAsLiteralValue;
    private boolean deferredSyntaxAllowedAsLiteral;
    private final ExpressionFactory expressionFactory;
    private String trimDirectiveWhitespacesValue;
    private boolean trimDirectiveWhitespaces;
    private String omitXmlDecl;
    private String doctypeName;
    private String doctypePublic;
    private String doctypeSystem;
    private boolean isJspPrefixHijacked;
    private final HashSet<String> prefixes;
    private boolean hasJspRoot;
    private Collection<String> includePrelude;
    private Collection<String> includeCoda;
    private final Vector<String> pluginDcls;
    private boolean errorOnUndeclaredNamepsace;
    private final boolean isTagFile;
    PageInfo ( final BeanRepository beanRepository, final String jspFile, final boolean isTagFile ) {
        this.defaultExtends = Constants.JSP_SERVLET_BASE;
        this.contentType = null;
        this.isSession = true;
        this.buffer = 8192;
        this.isAutoFlush = true;
        this.isThreadSafe = true;
        this.isErrorPage = false;
        this.errorPage = null;
        this.scriptless = false;
        this.scriptingInvalid = false;
        this.isELIgnored = false;
        this.deferredSyntaxAllowedAsLiteral = false;
        this.expressionFactory = ExpressionFactory.newInstance();
        this.trimDirectiveWhitespaces = false;
        this.omitXmlDecl = null;
        this.doctypeName = null;
        this.doctypePublic = null;
        this.doctypeSystem = null;
        this.hasJspRoot = false;
        this.errorOnUndeclaredNamepsace = false;
        this.isTagFile = isTagFile;
        this.jspFile = jspFile;
        this.beanRepository = beanRepository;
        this.varInfoNames = new HashSet<String>();
        this.taglibsMap = new HashMap<String, TagLibraryInfo>();
        this.jspPrefixMapper = new HashMap<String, String>();
        this.xmlPrefixMapper = new HashMap<String, LinkedList<String>>();
        this.nonCustomTagPrefixMap = new HashMap<String, Mark>();
        this.imports = new Vector<String>();
        this.dependants = new HashMap<String, Long>();
        this.includePrelude = new Vector<String>();
        this.includeCoda = new Vector<String>();
        this.pluginDcls = new Vector<String>();
        this.prefixes = new HashSet<String>();
        this.imports.addAll ( Constants.STANDARD_IMPORTS );
    }
    public boolean isTagFile() {
        return this.isTagFile;
    }
    public boolean isPluginDeclared ( final String id ) {
        if ( this.pluginDcls.contains ( id ) ) {
            return true;
        }
        this.pluginDcls.add ( id );
        return false;
    }
    public void addImports ( final List<String> imports ) {
        this.imports.addAll ( imports );
    }
    public void addImport ( final String imp ) {
        this.imports.add ( imp );
    }
    public List<String> getImports() {
        return this.imports;
    }
    public String getJspFile() {
        return this.jspFile;
    }
    public void addDependant ( final String d, final Long lastModified ) {
        if ( !this.dependants.containsKey ( d ) && !this.jspFile.equals ( d ) ) {
            this.dependants.put ( d, lastModified );
        }
    }
    public Map<String, Long> getDependants() {
        return this.dependants;
    }
    public BeanRepository getBeanRepository() {
        return this.beanRepository;
    }
    public void setScriptless ( final boolean s ) {
        this.scriptless = s;
    }
    public boolean isScriptless() {
        return this.scriptless;
    }
    public void setScriptingInvalid ( final boolean s ) {
        this.scriptingInvalid = s;
    }
    public boolean isScriptingInvalid() {
        return this.scriptingInvalid;
    }
    public Collection<String> getIncludePrelude() {
        return this.includePrelude;
    }
    public void setIncludePrelude ( final Collection<String> prelude ) {
        this.includePrelude = prelude;
    }
    public Collection<String> getIncludeCoda() {
        return this.includeCoda;
    }
    public void setIncludeCoda ( final Collection<String> coda ) {
        this.includeCoda = coda;
    }
    public void setHasJspRoot ( final boolean s ) {
        this.hasJspRoot = s;
    }
    public boolean hasJspRoot() {
        return this.hasJspRoot;
    }
    public String getOmitXmlDecl() {
        return this.omitXmlDecl;
    }
    public void setOmitXmlDecl ( final String omit ) {
        this.omitXmlDecl = omit;
    }
    public String getDoctypeName() {
        return this.doctypeName;
    }
    public void setDoctypeName ( final String doctypeName ) {
        this.doctypeName = doctypeName;
    }
    public String getDoctypeSystem() {
        return this.doctypeSystem;
    }
    public void setDoctypeSystem ( final String doctypeSystem ) {
        this.doctypeSystem = doctypeSystem;
    }
    public String getDoctypePublic() {
        return this.doctypePublic;
    }
    public void setDoctypePublic ( final String doctypePublic ) {
        this.doctypePublic = doctypePublic;
    }
    public void setIsJspPrefixHijacked ( final boolean isHijacked ) {
        this.isJspPrefixHijacked = isHijacked;
    }
    public boolean isJspPrefixHijacked() {
        return this.isJspPrefixHijacked;
    }
    public void addPrefix ( final String prefix ) {
        this.prefixes.add ( prefix );
    }
    public boolean containsPrefix ( final String prefix ) {
        return this.prefixes.contains ( prefix );
    }
    public void addTaglib ( final String uri, final TagLibraryInfo info ) {
        this.taglibsMap.put ( uri, info );
    }
    public TagLibraryInfo getTaglib ( final String uri ) {
        return this.taglibsMap.get ( uri );
    }
    public Collection<TagLibraryInfo> getTaglibs() {
        return this.taglibsMap.values();
    }
    public boolean hasTaglib ( final String uri ) {
        return this.taglibsMap.containsKey ( uri );
    }
    public void addPrefixMapping ( final String prefix, final String uri ) {
        this.jspPrefixMapper.put ( prefix, uri );
    }
    public void pushPrefixMapping ( final String prefix, final String uri ) {
        LinkedList<String> stack = this.xmlPrefixMapper.get ( prefix );
        if ( stack == null ) {
            stack = new LinkedList<String>();
            this.xmlPrefixMapper.put ( prefix, stack );
        }
        stack.addFirst ( uri );
    }
    public void popPrefixMapping ( final String prefix ) {
        final LinkedList<String> stack = this.xmlPrefixMapper.get ( prefix );
        stack.removeFirst();
    }
    public String getURI ( final String prefix ) {
        String uri = null;
        final LinkedList<String> stack = this.xmlPrefixMapper.get ( prefix );
        if ( stack == null || stack.size() == 0 ) {
            uri = this.jspPrefixMapper.get ( prefix );
        } else {
            uri = stack.getFirst();
        }
        return uri;
    }
    public void setLanguage ( final String value, final Node n, final ErrorDispatcher err, final boolean pagedir ) throws JasperException {
        if ( !"java".equalsIgnoreCase ( value ) ) {
            if ( pagedir ) {
                err.jspError ( n, "jsp.error.page.language.nonjava", new String[0] );
            } else {
                err.jspError ( n, "jsp.error.tag.language.nonjava", new String[0] );
            }
        }
        this.language = value;
    }
    public String getLanguage ( final boolean useDefault ) {
        return ( this.language == null && useDefault ) ? "java" : this.language;
    }
    public void setExtends ( final String value ) {
        this.xtends = value;
    }
    public String getExtends ( final boolean useDefault ) {
        return ( this.xtends == null && useDefault ) ? this.defaultExtends : this.xtends;
    }
    public String getExtends() {
        return this.getExtends ( true );
    }
    public void setContentType ( final String value ) {
        this.contentType = value;
    }
    public String getContentType() {
        return this.contentType;
    }
    public void setBufferValue ( final String value, final Node n, final ErrorDispatcher err ) throws JasperException {
        if ( "none".equalsIgnoreCase ( value ) ) {
            this.buffer = 0;
        } else {
            if ( value == null || !value.endsWith ( "kb" ) ) {
                if ( n == null ) {
                    err.jspError ( "jsp.error.page.invalid.buffer", new String[0] );
                } else {
                    err.jspError ( n, "jsp.error.page.invalid.buffer", new String[0] );
                }
            }
            try {
                final int k = Integer.parseInt ( value.substring ( 0, value.length() - 2 ) );
                this.buffer = k * 1024;
            } catch ( NumberFormatException e ) {
                if ( n == null ) {
                    err.jspError ( "jsp.error.page.invalid.buffer", new String[0] );
                } else {
                    err.jspError ( n, "jsp.error.page.invalid.buffer", new String[0] );
                }
            }
        }
        this.bufferValue = value;
    }
    public String getBufferValue() {
        return this.bufferValue;
    }
    public int getBuffer() {
        return this.buffer;
    }
    public void setSession ( final String value, final Node n, final ErrorDispatcher err ) throws JasperException {
        if ( "true".equalsIgnoreCase ( value ) ) {
            this.isSession = true;
        } else if ( "false".equalsIgnoreCase ( value ) ) {
            this.isSession = false;
        } else {
            err.jspError ( n, "jsp.error.page.invalid.session", new String[0] );
        }
        this.session = value;
    }
    public String getSession() {
        return this.session;
    }
    public boolean isSession() {
        return this.isSession;
    }
    public void setAutoFlush ( final String value, final Node n, final ErrorDispatcher err ) throws JasperException {
        if ( "true".equalsIgnoreCase ( value ) ) {
            this.isAutoFlush = true;
        } else if ( "false".equalsIgnoreCase ( value ) ) {
            this.isAutoFlush = false;
        } else {
            err.jspError ( n, "jsp.error.autoFlush.invalid", new String[0] );
        }
        this.autoFlush = value;
    }
    public String getAutoFlush() {
        return this.autoFlush;
    }
    public boolean isAutoFlush() {
        return this.isAutoFlush;
    }
    public void setIsThreadSafe ( final String value, final Node n, final ErrorDispatcher err ) throws JasperException {
        if ( "true".equalsIgnoreCase ( value ) ) {
            this.isThreadSafe = true;
        } else if ( "false".equalsIgnoreCase ( value ) ) {
            this.isThreadSafe = false;
        } else {
            err.jspError ( n, "jsp.error.page.invalid.isthreadsafe", new String[0] );
        }
        this.isThreadSafeValue = value;
    }
    public String getIsThreadSafe() {
        return this.isThreadSafeValue;
    }
    public boolean isThreadSafe() {
        return this.isThreadSafe;
    }
    public void setInfo ( final String value ) {
        this.info = value;
    }
    public String getInfo() {
        return this.info;
    }
    public void setErrorPage ( final String value ) {
        this.errorPage = value;
    }
    public String getErrorPage() {
        return this.errorPage;
    }
    public void setIsErrorPage ( final String value, final Node n, final ErrorDispatcher err ) throws JasperException {
        if ( "true".equalsIgnoreCase ( value ) ) {
            this.isErrorPage = true;
        } else if ( "false".equalsIgnoreCase ( value ) ) {
            this.isErrorPage = false;
        } else {
            err.jspError ( n, "jsp.error.page.invalid.iserrorpage", new String[0] );
        }
        this.isErrorPageValue = value;
    }
    public String getIsErrorPage() {
        return this.isErrorPageValue;
    }
    public boolean isErrorPage() {
        return this.isErrorPage;
    }
    public void setIsELIgnored ( final String value, final Node n, final ErrorDispatcher err, final boolean pagedir ) throws JasperException {
        if ( "true".equalsIgnoreCase ( value ) ) {
            this.isELIgnored = true;
        } else if ( "false".equalsIgnoreCase ( value ) ) {
            this.isELIgnored = false;
        } else if ( pagedir ) {
            err.jspError ( n, "jsp.error.page.invalid.iselignored", new String[0] );
        } else {
            err.jspError ( n, "jsp.error.tag.invalid.iselignored", new String[0] );
        }
        this.isELIgnoredValue = value;
    }
    public void setDeferredSyntaxAllowedAsLiteral ( final String value, final Node n, final ErrorDispatcher err, final boolean pagedir ) throws JasperException {
        if ( "true".equalsIgnoreCase ( value ) ) {
            this.deferredSyntaxAllowedAsLiteral = true;
        } else if ( "false".equalsIgnoreCase ( value ) ) {
            this.deferredSyntaxAllowedAsLiteral = false;
        } else if ( pagedir ) {
            err.jspError ( n, "jsp.error.page.invalid.deferredsyntaxallowedasliteral", new String[0] );
        } else {
            err.jspError ( n, "jsp.error.tag.invalid.deferredsyntaxallowedasliteral", new String[0] );
        }
        this.deferredSyntaxAllowedAsLiteralValue = value;
    }
    public void setTrimDirectiveWhitespaces ( final String value, final Node n, final ErrorDispatcher err, final boolean pagedir ) throws JasperException {
        if ( "true".equalsIgnoreCase ( value ) ) {
            this.trimDirectiveWhitespaces = true;
        } else if ( "false".equalsIgnoreCase ( value ) ) {
            this.trimDirectiveWhitespaces = false;
        } else if ( pagedir ) {
            err.jspError ( n, "jsp.error.page.invalid.trimdirectivewhitespaces", new String[0] );
        } else {
            err.jspError ( n, "jsp.error.tag.invalid.trimdirectivewhitespaces", new String[0] );
        }
        this.trimDirectiveWhitespacesValue = value;
    }
    public void setELIgnored ( final boolean s ) {
        this.isELIgnored = s;
    }
    public String getIsELIgnored() {
        return this.isELIgnoredValue;
    }
    public boolean isELIgnored() {
        return this.isELIgnored;
    }
    public void putNonCustomTagPrefix ( final String prefix, final Mark where ) {
        this.nonCustomTagPrefixMap.put ( prefix, where );
    }
    public Mark getNonCustomTagPrefix ( final String prefix ) {
        return this.nonCustomTagPrefixMap.get ( prefix );
    }
    public String getDeferredSyntaxAllowedAsLiteral() {
        return this.deferredSyntaxAllowedAsLiteralValue;
    }
    public boolean isDeferredSyntaxAllowedAsLiteral() {
        return this.deferredSyntaxAllowedAsLiteral;
    }
    public void setDeferredSyntaxAllowedAsLiteral ( final boolean isELDeferred ) {
        this.deferredSyntaxAllowedAsLiteral = isELDeferred;
    }
    public ExpressionFactory getExpressionFactory() {
        return this.expressionFactory;
    }
    public String getTrimDirectiveWhitespaces() {
        return this.trimDirectiveWhitespacesValue;
    }
    public boolean isTrimDirectiveWhitespaces() {
        return this.trimDirectiveWhitespaces;
    }
    public void setTrimDirectiveWhitespaces ( final boolean trimDirectiveWhitespaces ) {
        this.trimDirectiveWhitespaces = trimDirectiveWhitespaces;
    }
    public Set<String> getVarInfoNames() {
        return this.varInfoNames;
    }
    public boolean isErrorOnUndeclaredNamespace() {
        return this.errorOnUndeclaredNamepsace;
    }
    public void setErrorOnUndeclaredNamespace ( final boolean errorOnUndeclaredNamespace ) {
        this.errorOnUndeclaredNamepsace = errorOnUndeclaredNamespace;
    }
}
