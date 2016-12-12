package org.apache.jasper;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.HashSet;
import javax.servlet.jsp.JspFactory;
import org.apache.jasper.runtime.JspFactoryImpl;
import java.io.OutputStreamWriter;
import java.io.InputStream;
import java.io.InputStreamReader;
import org.apache.tools.ant.util.FileUtils;
import java.net.URLClassLoader;
import java.util.ArrayList;
import org.apache.tools.ant.AntClassLoader;
import java.net.URL;
import org.xml.sax.SAXException;
import java.io.OutputStream;
import java.util.Iterator;
import org.apache.juli.logging.LogFactory;
import java.util.Stack;
import org.apache.jasper.compiler.Compiler;
import java.io.FileNotFoundException;
import org.apache.jasper.servlet.JspServletWrapper;
import javax.servlet.ServletContext;
import java.io.FileOutputStream;
import java.io.FileInputStream;
import java.io.EOFException;
import java.io.Reader;
import java.io.PrintWriter;
import java.io.BufferedReader;
import java.io.IOException;
import java.util.StringTokenizer;
import org.apache.tools.ant.BuildException;
import org.apache.jasper.compiler.Localizer;
import java.util.Vector;
import java.util.HashMap;
import org.apache.jasper.servlet.TldScanner;
import org.apache.jasper.compiler.TagPluginManager;
import org.apache.jasper.compiler.JspConfig;
import org.apache.jasper.compiler.TldCache;
import org.apache.jasper.compiler.JspRuntimeContext;
import org.apache.jasper.servlet.JspCServletContext;
import java.io.CharArrayWriter;
import java.io.Writer;
import java.util.List;
import javax.servlet.jsp.tagext.TagLibraryInfo;
import java.util.Map;
import java.io.File;
import java.util.Set;
import org.apache.juli.logging.Log;
import org.apache.tools.ant.Task;
public class JspC extends Task implements Options {
    public static final String DEFAULT_IE_CLASS_ID = "clsid:8AD9C840-044E-11D1-B3E9-00805F499D93";
    private static final Log log;
    protected static final String SWITCH_VERBOSE = "-v";
    protected static final String SWITCH_HELP = "-help";
    protected static final String SWITCH_OUTPUT_DIR = "-d";
    protected static final String SWITCH_PACKAGE_NAME = "-p";
    protected static final String SWITCH_CACHE = "-cache";
    protected static final String SWITCH_CLASS_NAME = "-c";
    protected static final String SWITCH_FULL_STOP = "--";
    protected static final String SWITCH_COMPILE = "-compile";
    protected static final String SWITCH_SOURCE = "-source";
    protected static final String SWITCH_TARGET = "-target";
    protected static final String SWITCH_URI_BASE = "-uribase";
    protected static final String SWITCH_URI_ROOT = "-uriroot";
    protected static final String SWITCH_FILE_WEBAPP = "-webapp";
    protected static final String SWITCH_WEBAPP_INC = "-webinc";
    protected static final String SWITCH_WEBAPP_XML = "-webxml";
    protected static final String SWITCH_WEBAPP_XML_ENCODING = "-webxmlencoding";
    protected static final String SWITCH_ADD_WEBAPP_XML_MAPPINGS = "-addwebxmlmappings";
    protected static final String SWITCH_MAPPED = "-mapped";
    protected static final String SWITCH_XPOWERED_BY = "-xpoweredBy";
    protected static final String SWITCH_TRIM_SPACES = "-trimSpaces";
    protected static final String SWITCH_CLASSPATH = "-classpath";
    protected static final String SWITCH_DIE = "-die";
    protected static final String SWITCH_POOLING = "-poolingEnabled";
    protected static final String SWITCH_ENCODING = "-javaEncoding";
    protected static final String SWITCH_SMAP = "-smap";
    protected static final String SWITCH_DUMP_SMAP = "-dumpsmap";
    protected static final String SWITCH_VALIDATE_TLD = "-validateTld";
    protected static final String SWITCH_VALIDATE_XML = "-validateXml";
    protected static final String SWITCH_NO_BLOCK_EXTERNAL = "-no-blockExternal";
    protected static final String SWITCH_NO_STRICT_QUOTE_ESCAPING = "-no-strictQuoteEscaping";
    protected static final String SWITCH_QUOTE_ATTRIBUTE_EL = "-quoteAttributeEL";
    protected static final String SWITCH_NO_QUOTE_ATTRIBUTE_EL = "-no-quoteAttributeEL";
    protected static final String SHOW_SUCCESS = "-s";
    protected static final String LIST_ERRORS = "-l";
    protected static final int INC_WEBXML = 10;
    protected static final int ALL_WEBXML = 20;
    protected static final int DEFAULT_DIE_LEVEL = 1;
    protected static final int NO_DIE_LEVEL = 0;
    protected static final Set<String> insertBefore;
    protected String classPath;
    protected ClassLoader loader;
    protected boolean trimSpaces;
    protected boolean genStringAsCharArray;
    protected boolean validateTld;
    protected boolean validateXml;
    protected boolean blockExternal;
    protected boolean strictQuoteEscaping;
    protected boolean quoteAttributeEL;
    protected boolean xpoweredBy;
    protected boolean mappedFile;
    protected boolean poolingEnabled;
    protected File scratchDir;
    protected String ieClassId;
    protected String targetPackage;
    protected String targetClassName;
    protected String uriBase;
    protected String uriRoot;
    protected int dieLevel;
    protected boolean helpNeeded;
    protected boolean compile;
    protected boolean smapSuppressed;
    protected boolean smapDumped;
    protected boolean caching;
    protected final Map<String, TagLibraryInfo> cache;
    protected String compiler;
    protected String compilerTargetVM;
    protected String compilerSourceVM;
    protected boolean classDebugInfo;
    protected boolean failOnError;
    protected List<String> extensions;
    protected final List<String> pages;
    protected boolean errorOnUseBeanInvalidClassAttribute;
    protected String javaEncoding;
    protected String webxmlFile;
    protected int webxmlLevel;
    protected String webxmlEncoding;
    protected boolean addWebXmlMappings;
    protected Writer mapout;
    protected CharArrayWriter servletout;
    protected CharArrayWriter mappingout;
    protected JspCServletContext context;
    protected JspRuntimeContext rctxt;
    protected TldCache tldCache;
    protected JspConfig jspConfig;
    protected TagPluginManager tagPluginManager;
    protected TldScanner scanner;
    protected boolean verbose;
    protected boolean listErrors;
    protected boolean showSuccess;
    protected int argPos;
    protected boolean fullstop;
    protected String[] args;
    public JspC() {
        this.classPath = null;
        this.loader = null;
        this.trimSpaces = false;
        this.genStringAsCharArray = false;
        this.blockExternal = true;
        this.strictQuoteEscaping = true;
        this.quoteAttributeEL = true;
        this.mappedFile = false;
        this.poolingEnabled = true;
        this.ieClassId = "clsid:8AD9C840-044E-11D1-B3E9-00805F499D93";
        this.helpNeeded = false;
        this.compile = false;
        this.smapSuppressed = true;
        this.smapDumped = false;
        this.caching = true;
        this.cache = new HashMap<String, TagLibraryInfo>();
        this.compiler = null;
        this.compilerTargetVM = "1.8";
        this.compilerSourceVM = "1.8";
        this.classDebugInfo = true;
        this.failOnError = true;
        this.pages = new Vector<String>();
        this.errorOnUseBeanInvalidClassAttribute = true;
        this.javaEncoding = "UTF-8";
        this.webxmlEncoding = "UTF-8";
        this.addWebXmlMappings = false;
        this.tldCache = null;
        this.jspConfig = null;
        this.tagPluginManager = null;
        this.scanner = null;
        this.verbose = false;
        this.listErrors = false;
        this.showSuccess = false;
        this.fullstop = false;
    }
    public static void main ( final String[] arg ) {
        if ( arg.length == 0 ) {
            System.out.println ( Localizer.getMessage ( "jspc.usage" ) );
        } else {
            final JspC jspc = new JspC();
            try {
                jspc.setArgs ( arg );
                if ( jspc.helpNeeded ) {
                    System.out.println ( Localizer.getMessage ( "jspc.usage" ) );
                } else {
                    jspc.execute();
                }
            } catch ( JasperException je ) {
                System.err.println ( je );
                if ( jspc.dieLevel != 0 ) {
                    System.exit ( jspc.dieLevel );
                }
            } catch ( BuildException je2 ) {
                System.err.println ( je2 );
                if ( jspc.dieLevel != 0 ) {
                    System.exit ( jspc.dieLevel );
                }
            }
        }
    }
    public void setArgs ( final String[] arg ) throws JasperException {
        this.args = arg;
        this.dieLevel = 0;
        String tok;
        while ( ( tok = this.nextArg() ) != null ) {
            if ( tok.equals ( "-v" ) ) {
                this.verbose = true;
                this.showSuccess = true;
                this.listErrors = true;
            } else if ( tok.equals ( "-d" ) ) {
                tok = this.nextArg();
                this.setOutputDir ( tok );
            } else if ( tok.equals ( "-p" ) ) {
                this.targetPackage = this.nextArg();
            } else if ( tok.equals ( "-compile" ) ) {
                this.compile = true;
            } else if ( tok.equals ( "-c" ) ) {
                this.targetClassName = this.nextArg();
            } else if ( tok.equals ( "-uribase" ) ) {
                this.uriBase = this.nextArg();
            } else if ( tok.equals ( "-uriroot" ) ) {
                this.setUriroot ( this.nextArg() );
            } else if ( tok.equals ( "-webapp" ) ) {
                this.setUriroot ( this.nextArg() );
            } else if ( tok.equals ( "-s" ) ) {
                this.showSuccess = true;
            } else if ( tok.equals ( "-l" ) ) {
                this.listErrors = true;
            } else if ( tok.equals ( "-webinc" ) ) {
                this.webxmlFile = this.nextArg();
                if ( this.webxmlFile == null ) {
                    continue;
                }
                this.webxmlLevel = 10;
            } else if ( tok.equals ( "-webxml" ) ) {
                this.webxmlFile = this.nextArg();
                if ( this.webxmlFile == null ) {
                    continue;
                }
                this.webxmlLevel = 20;
            } else if ( tok.equals ( "-webxmlencoding" ) ) {
                this.setWebXmlEncoding ( this.nextArg() );
            } else if ( tok.equals ( "-addwebxmlmappings" ) ) {
                this.setAddWebXmlMappings ( true );
            } else if ( tok.equals ( "-mapped" ) ) {
                this.mappedFile = true;
            } else if ( tok.equals ( "-xpoweredBy" ) ) {
                this.xpoweredBy = true;
            } else if ( tok.equals ( "-trimSpaces" ) ) {
                this.setTrimSpaces ( true );
            } else if ( tok.equals ( "-cache" ) ) {
                tok = this.nextArg();
                if ( "false".equals ( tok ) ) {
                    this.caching = false;
                } else {
                    this.caching = true;
                }
            } else if ( tok.equals ( "-classpath" ) ) {
                this.setClassPath ( this.nextArg() );
            } else if ( tok.startsWith ( "-die" ) ) {
                try {
                    this.dieLevel = Integer.parseInt ( tok.substring ( "-die".length() ) );
                } catch ( NumberFormatException nfe ) {
                    this.dieLevel = 1;
                }
            } else if ( tok.equals ( "-help" ) ) {
                this.helpNeeded = true;
            } else if ( tok.equals ( "-poolingEnabled" ) ) {
                tok = this.nextArg();
                if ( "false".equals ( tok ) ) {
                    this.poolingEnabled = false;
                } else {
                    this.poolingEnabled = true;
                }
            } else if ( tok.equals ( "-javaEncoding" ) ) {
                this.setJavaEncoding ( this.nextArg() );
            } else if ( tok.equals ( "-source" ) ) {
                this.setCompilerSourceVM ( this.nextArg() );
            } else if ( tok.equals ( "-target" ) ) {
                this.setCompilerTargetVM ( this.nextArg() );
            } else if ( tok.equals ( "-smap" ) ) {
                this.smapSuppressed = false;
            } else if ( tok.equals ( "-dumpsmap" ) ) {
                this.smapDumped = true;
            } else if ( tok.equals ( "-validateTld" ) ) {
                this.setValidateTld ( true );
            } else if ( tok.equals ( "-validateXml" ) ) {
                this.setValidateXml ( true );
            } else if ( tok.equals ( "-no-blockExternal" ) ) {
                this.setBlockExternal ( false );
            } else if ( tok.equals ( "-no-strictQuoteEscaping" ) ) {
                this.setStrictQuoteEscaping ( false );
            } else if ( tok.equals ( "-quoteAttributeEL" ) ) {
                this.setQuoteAttributeEL ( true );
            } else if ( tok.equals ( "-no-quoteAttributeEL" ) ) {
                this.setQuoteAttributeEL ( false );
            } else {
                if ( tok.startsWith ( "-" ) ) {
                    throw new JasperException ( "Unrecognized option: " + tok + ".  Use -help for help." );
                }
                if ( !this.fullstop ) {
                    --this.argPos;
                    break;
                }
                break;
            }
        }
        while ( true ) {
            final String file = this.nextFile();
            if ( file == null ) {
                break;
            }
            this.pages.add ( file );
        }
    }
    public boolean getKeepGenerated() {
        return true;
    }
    public boolean getTrimSpaces() {
        return this.trimSpaces;
    }
    public void setTrimSpaces ( final boolean ts ) {
        this.trimSpaces = ts;
    }
    public boolean isPoolingEnabled() {
        return this.poolingEnabled;
    }
    public void setPoolingEnabled ( final boolean poolingEnabled ) {
        this.poolingEnabled = poolingEnabled;
    }
    public boolean isXpoweredBy() {
        return this.xpoweredBy;
    }
    public void setXpoweredBy ( final boolean xpoweredBy ) {
        this.xpoweredBy = xpoweredBy;
    }
    public boolean getDisplaySourceFragment() {
        return true;
    }
    public int getMaxLoadedJsps() {
        return -1;
    }
    public int getJspIdleTimeout() {
        return -1;
    }
    public boolean getErrorOnUseBeanInvalidClassAttribute() {
        return this.errorOnUseBeanInvalidClassAttribute;
    }
    public void setErrorOnUseBeanInvalidClassAttribute ( final boolean b ) {
        this.errorOnUseBeanInvalidClassAttribute = b;
    }
    public boolean getMappedFile() {
        return this.mappedFile;
    }
    public void setMappedFile ( final boolean b ) {
        this.mappedFile = b;
    }
    public void setClassDebugInfo ( final boolean b ) {
        this.classDebugInfo = b;
    }
    public boolean getClassDebugInfo() {
        return this.classDebugInfo;
    }
    public boolean isCaching() {
        return this.caching;
    }
    public void setCaching ( final boolean caching ) {
        this.caching = caching;
    }
    public Map<String, TagLibraryInfo> getCache() {
        return this.cache;
    }
    public int getCheckInterval() {
        return 0;
    }
    public int getModificationTestInterval() {
        return 0;
    }
    public boolean getRecompileOnFail() {
        return false;
    }
    public boolean getDevelopment() {
        return false;
    }
    public boolean isSmapSuppressed() {
        return this.smapSuppressed;
    }
    public void setSmapSuppressed ( final boolean smapSuppressed ) {
        this.smapSuppressed = smapSuppressed;
    }
    public boolean isSmapDumped() {
        return this.smapDumped;
    }
    public void setSmapDumped ( final boolean smapDumped ) {
        this.smapDumped = smapDumped;
    }
    public void setGenStringAsCharArray ( final boolean genStringAsCharArray ) {
        this.genStringAsCharArray = genStringAsCharArray;
    }
    public boolean genStringAsCharArray() {
        return this.genStringAsCharArray;
    }
    public void setIeClassId ( final String ieClassId ) {
        this.ieClassId = ieClassId;
    }
    public String getIeClassId() {
        return this.ieClassId;
    }
    public File getScratchDir() {
        return this.scratchDir;
    }
    public String getCompiler() {
        return this.compiler;
    }
    public void setCompiler ( final String c ) {
        this.compiler = c;
    }
    public String getCompilerClassName() {
        return null;
    }
    public String getCompilerTargetVM() {
        return this.compilerTargetVM;
    }
    public void setCompilerTargetVM ( final String vm ) {
        this.compilerTargetVM = vm;
    }
    public String getCompilerSourceVM() {
        return this.compilerSourceVM;
    }
    public void setCompilerSourceVM ( final String vm ) {
        this.compilerSourceVM = vm;
    }
    public TldCache getTldCache() {
        return this.tldCache;
    }
    public String getJavaEncoding() {
        return this.javaEncoding;
    }
    public void setJavaEncoding ( final String encodingName ) {
        this.javaEncoding = encodingName;
    }
    public boolean getFork() {
        return false;
    }
    public String getClassPath() {
        if ( this.classPath != null ) {
            return this.classPath;
        }
        return System.getProperty ( "java.class.path" );
    }
    public void setClassPath ( final String s ) {
        this.classPath = s;
    }
    public List<String> getExtensions() {
        return this.extensions;
    }
    protected void addExtension ( final String extension ) {
        if ( extension != null ) {
            if ( this.extensions == null ) {
                this.extensions = new Vector<String>();
            }
            this.extensions.add ( extension );
        }
    }
    public void setUriroot ( final String s ) {
        if ( s == null ) {
            this.uriRoot = null;
            return;
        }
        try {
            this.uriRoot = this.resolveFile ( s ).getCanonicalPath();
        } catch ( Exception ex ) {
            this.uriRoot = s;
        }
    }
    public void setJspFiles ( final String jspFiles ) {
        if ( jspFiles == null ) {
            return;
        }
        final StringTokenizer tok = new StringTokenizer ( jspFiles, "," );
        while ( tok.hasMoreTokens() ) {
            this.pages.add ( tok.nextToken() );
        }
    }
    public void setCompile ( final boolean b ) {
        this.compile = b;
    }
    public void setVerbose ( final int level ) {
        if ( level > 0 ) {
            this.verbose = true;
            this.showSuccess = true;
            this.listErrors = true;
        }
    }
    public void setValidateTld ( final boolean b ) {
        this.validateTld = b;
    }
    public boolean isValidateTld() {
        return this.validateTld;
    }
    public void setValidateXml ( final boolean b ) {
        this.validateXml = b;
    }
    public boolean isValidateXml() {
        return this.validateXml;
    }
    public void setBlockExternal ( final boolean b ) {
        this.blockExternal = b;
    }
    public boolean isBlockExternal() {
        return this.blockExternal;
    }
    public void setStrictQuoteEscaping ( final boolean b ) {
        this.strictQuoteEscaping = b;
    }
    public boolean getStrictQuoteEscaping() {
        return this.strictQuoteEscaping;
    }
    public void setQuoteAttributeEL ( final boolean b ) {
        this.quoteAttributeEL = b;
    }
    public boolean getQuoteAttributeEL() {
        return this.quoteAttributeEL;
    }
    public void setListErrors ( final boolean b ) {
        this.listErrors = b;
    }
    public void setOutputDir ( final String s ) {
        if ( s != null ) {
            this.scratchDir = this.resolveFile ( s ).getAbsoluteFile();
        } else {
            this.scratchDir = null;
        }
    }
    public void setPackage ( final String p ) {
        this.targetPackage = p;
    }
    public void setClassName ( final String p ) {
        this.targetClassName = p;
    }
    public void setWebXmlFragment ( final String s ) {
        this.webxmlFile = this.resolveFile ( s ).getAbsolutePath();
        this.webxmlLevel = 10;
    }
    public void setWebXml ( final String s ) {
        this.webxmlFile = this.resolveFile ( s ).getAbsolutePath();
        this.webxmlLevel = 20;
    }
    public void setWebXmlEncoding ( final String encoding ) {
        this.webxmlEncoding = encoding;
    }
    public void setAddWebXmlMappings ( final boolean b ) {
        this.addWebXmlMappings = b;
    }
    public void setFailOnError ( final boolean b ) {
        this.failOnError = b;
    }
    public boolean getFailOnError() {
        return this.failOnError;
    }
    public JspConfig getJspConfig() {
        return this.jspConfig;
    }
    public TagPluginManager getTagPluginManager() {
        return this.tagPluginManager;
    }
    public void generateWebMapping ( final String file, final JspCompilationContext clctxt ) throws IOException {
        if ( JspC.log.isDebugEnabled() ) {
            JspC.log.debug ( "Generating web mapping for file " + file + " using compilation context " + clctxt );
        }
        final String className = clctxt.getServletClassName();
        final String packageName = clctxt.getServletPackageName();
        String thisServletName;
        if ( "".equals ( packageName ) ) {
            thisServletName = className;
        } else {
            thisServletName = packageName + '.' + className;
        }
        if ( this.servletout != null ) {
            this.servletout.write ( "\n    <servlet>\n        <servlet-name>" );
            this.servletout.write ( thisServletName );
            this.servletout.write ( "</servlet-name>\n        <servlet-class>" );
            this.servletout.write ( thisServletName );
            this.servletout.write ( "</servlet-class>\n    </servlet>\n" );
        }
        if ( this.mappingout != null ) {
            this.mappingout.write ( "\n    <servlet-mapping>\n        <servlet-name>" );
            this.mappingout.write ( thisServletName );
            this.mappingout.write ( "</servlet-name>\n        <url-pattern>" );
            this.mappingout.write ( file.replace ( '\\', '/' ) );
            this.mappingout.write ( "</url-pattern>\n    </servlet-mapping>\n" );
        }
    }
    protected void mergeIntoWebXml() throws IOException {
        final File webappBase = new File ( this.uriRoot );
        final File webXml = new File ( webappBase, "WEB-INF/web.xml" );
        final File webXml2 = new File ( webappBase, "WEB-INF/web2.xml" );
        final String insertStartMarker = Localizer.getMessage ( "jspc.webinc.insertStart" );
        final String insertEndMarker = Localizer.getMessage ( "jspc.webinc.insertEnd" );
        try ( final BufferedReader reader = new BufferedReader ( this.openWebxmlReader ( webXml ) );
                    final BufferedReader fragmentReader = new BufferedReader ( this.openWebxmlReader ( new File ( this.webxmlFile ) ) );
                    final PrintWriter writer = new PrintWriter ( this.openWebxmlWriter ( webXml2 ) ) ) {
            boolean inserted = false;
            int current = reader.read();
            Label_0119:
            while ( current > -1 ) {
                if ( current == 60 ) {
                    String element = this.getElement ( reader );
                    if ( !inserted && JspC.insertBefore.contains ( element ) ) {
                        writer.println ( insertStartMarker );
                        while ( true ) {
                            final String line = fragmentReader.readLine();
                            if ( line == null ) {
                                break;
                            }
                            writer.println ( line );
                        }
                        writer.println();
                        writer.println ( insertEndMarker );
                        writer.println();
                        writer.write ( element );
                        inserted = true;
                    } else if ( element.equals ( insertStartMarker ) ) {
                        while ( true ) {
                            current = reader.read();
                            if ( current < 0 ) {
                                throw new EOFException();
                            }
                            if ( current != 60 ) {
                                continue;
                            }
                            element = this.getElement ( reader );
                            if ( element.equals ( insertEndMarker ) ) {
                                for ( current = reader.read(); current == 10 || current == 13; current = reader.read() ) {}
                                continue Label_0119;
                            }
                        }
                    } else {
                        writer.write ( element );
                    }
                } else {
                    writer.write ( current );
                }
                current = reader.read();
            }
        }
        try ( final FileInputStream fis = new FileInputStream ( webXml2 );
                    final FileOutputStream fos = new FileOutputStream ( webXml ) ) {
            final byte[] buf = new byte[512];
            while ( true ) {
                final int n = fis.read ( buf );
                if ( n < 0 ) {
                    break;
                }
                fos.write ( buf, 0, n );
            }
        }
        if ( !webXml2.delete() && JspC.log.isDebugEnabled() ) {
            JspC.log.debug ( Localizer.getMessage ( "jspc.delete.fail", webXml2.toString() ) );
        }
        if ( !new File ( this.webxmlFile ).delete() && JspC.log.isDebugEnabled() ) {
            JspC.log.debug ( Localizer.getMessage ( "jspc.delete.fail", this.webxmlFile ) );
        }
    }
    private String getElement ( final Reader reader ) throws IOException {
        final StringBuilder result = new StringBuilder();
        result.append ( '<' );
        boolean done = false;
        while ( !done ) {
            int current;
            for ( current = reader.read(); current != 62; current = reader.read() ) {
                if ( current < 0 ) {
                    throw new EOFException();
                }
                result.append ( ( char ) current );
            }
            result.append ( ( char ) current );
            final int len = result.length();
            if ( len > 4 && result.substring ( 0, 4 ).equals ( "<!--" ) ) {
                if ( len < 7 || !result.substring ( len - 3, len ).equals ( "-->" ) ) {
                    continue;
                }
                done = true;
            } else {
                done = true;
            }
        }
        return result.toString();
    }
    protected void processFile ( final String file ) throws JasperException {
        if ( JspC.log.isDebugEnabled() ) {
            JspC.log.debug ( "Processing file: " + file );
        }
        ClassLoader originalClassLoader = null;
        try {
            if ( this.scratchDir == null ) {
                String temp = System.getProperty ( "java.io.tmpdir" );
                if ( temp == null ) {
                    temp = "";
                }
                this.scratchDir = new File ( temp ).getAbsoluteFile();
            }
            final String jspUri = file.replace ( '\\', '/' );
            final JspCompilationContext clctxt = new JspCompilationContext ( jspUri, this, ( ServletContext ) this.context, null, this.rctxt );
            if ( this.targetClassName != null && this.targetClassName.length() > 0 ) {
                clctxt.setServletClassName ( this.targetClassName );
                this.targetClassName = null;
            }
            if ( this.targetPackage != null ) {
                clctxt.setServletPackageName ( this.targetPackage );
            }
            originalClassLoader = Thread.currentThread().getContextClassLoader();
            Thread.currentThread().setContextClassLoader ( this.loader );
            clctxt.setClassLoader ( this.loader );
            clctxt.setClassPath ( this.classPath );
            final Compiler clc = clctxt.createCompiler();
            if ( clc.isOutDated ( this.compile ) ) {
                if ( JspC.log.isDebugEnabled() ) {
                    JspC.log.debug ( jspUri + " is out dated, compiling..." );
                }
                clc.compile ( this.compile, true );
            }
            this.generateWebMapping ( file, clctxt );
            if ( this.showSuccess ) {
                JspC.log.info ( "Built File: " + file );
            }
        } catch ( JasperException je ) {
            Throwable rootCause;
            for ( rootCause = ( Throwable ) je; rootCause instanceof JasperException && ( ( JasperException ) rootCause ).getRootCause() != null; rootCause = ( ( JasperException ) rootCause ).getRootCause() ) {}
            if ( rootCause != je ) {
                JspC.log.error ( Localizer.getMessage ( "jspc.error.generalException", file ), rootCause );
            }
            if ( this.getFailOnError() ) {
                throw je;
            }
            JspC.log.error ( je.getMessage() );
        } catch ( Exception e ) {
            if ( e instanceof FileNotFoundException && JspC.log.isWarnEnabled() ) {
                JspC.log.warn ( Localizer.getMessage ( "jspc.error.fileDoesNotExist", e.getMessage() ) );
            }
            throw new JasperException ( e );
        } finally {
            if ( originalClassLoader != null ) {
                Thread.currentThread().setContextClassLoader ( originalClassLoader );
            }
        }
    }
    public void scanFiles ( final File base ) {
        final Stack<String> dirs = new Stack<String>();
        dirs.push ( base.toString() );
        if ( this.getExtensions() == null || this.getExtensions().size() < 2 ) {
            this.addExtension ( "jsp" );
            this.addExtension ( "jspx" );
        }
        while ( !dirs.isEmpty() ) {
            final String s = dirs.pop();
            final File f = new File ( s );
            if ( f.exists() && f.isDirectory() ) {
                final String[] files = f.list();
                for ( int i = 0; files != null && i < files.length; ++i ) {
                    final File f2 = new File ( s, files[i] );
                    if ( f2.isDirectory() ) {
                        dirs.push ( f2.getPath() );
                    } else {
                        final String path = f2.getPath();
                        final String uri = path.substring ( this.uriRoot.length() );
                        final String ext = files[i].substring ( files[i].lastIndexOf ( 46 ) + 1 );
                        if ( this.getExtensions().contains ( ext ) || this.jspConfig.isJspPage ( uri ) ) {
                            this.pages.add ( path );
                        }
                    }
                }
            }
        }
    }
    public void execute() {
        while ( true ) {
            if ( JspC.log.isDebugEnabled() ) {
                JspC.log.debug ( "execute() starting for " + this.pages.size() + " pages." );
                try {
                    if ( this.uriRoot == null ) {
                        if ( this.pages.size() == 0 ) {
                            throw new JasperException ( Localizer.getMessage ( "jsp.error.jspc.missingTarget" ) );
                        }
                        final String firstJsp = this.pages.get ( 0 );
                        final File firstJspF = new File ( firstJsp );
                        if ( !firstJspF.exists() ) {
                            throw new JasperException ( Localizer.getMessage ( "jspc.error.fileDoesNotExist", firstJsp ) );
                        }
                        this.locateUriRoot ( firstJspF );
                    }
                    if ( this.uriRoot == null ) {
                        throw new JasperException ( Localizer.getMessage ( "jsp.error.jspc.no_uriroot" ) );
                    }
                    final File uriRootF = new File ( this.uriRoot );
                    if ( !uriRootF.isDirectory() ) {
                        throw new JasperException ( Localizer.getMessage ( "jsp.error.jspc.uriroot_not_dir" ) );
                    }
                    if ( this.loader == null ) {
                        this.loader = this.initClassLoader();
                    }
                    if ( this.context == null ) {
                        this.initServletContext ( this.loader );
                    }
                    if ( this.pages.size() == 0 ) {
                        this.scanFiles ( uriRootF );
                    }
                    this.initWebXml();
                    for ( String nextjsp : this.pages ) {
                        File fjsp = new File ( nextjsp );
                        if ( !fjsp.isAbsolute() ) {
                            fjsp = new File ( uriRootF, nextjsp );
                        }
                        if ( !fjsp.exists() ) {
                            if ( !JspC.log.isWarnEnabled() ) {
                                continue;
                            }
                            JspC.log.warn ( Localizer.getMessage ( "jspc.error.fileDoesNotExist", fjsp.toString() ) );
                        } else {
                            final String s = fjsp.getAbsolutePath();
                            if ( s.startsWith ( this.uriRoot ) ) {
                                nextjsp = s.substring ( this.uriRoot.length() );
                            }
                            if ( nextjsp.startsWith ( "." + File.separatorChar ) ) {
                                nextjsp = nextjsp.substring ( 2 );
                            }
                            this.processFile ( nextjsp );
                        }
                    }
                    this.completeWebXml();
                    if ( this.addWebXmlMappings ) {
                        this.mergeIntoWebXml();
                    }
                } catch ( IOException ioe ) {
                    throw new BuildException ( ( Throwable ) ioe );
                } catch ( JasperException je ) {
                    Throwable rootCause;
                    for ( rootCause = ( Throwable ) je; rootCause instanceof JasperException && ( ( JasperException ) rootCause ).getRootCause() != null; rootCause = ( ( JasperException ) rootCause ).getRootCause() ) {}
                    if ( rootCause != je ) {
                        rootCause.printStackTrace();
                    }
                    throw new BuildException ( ( Throwable ) je );
                } finally {
                    if ( this.loader != null ) {
                        LogFactory.release ( this.loader );
                    }
                }
                return;
            }
            continue;
        }
    }
    protected String nextArg() {
        if ( this.argPos >= this.args.length || ( this.fullstop = "--".equals ( this.args[this.argPos] ) ) ) {
            return null;
        }
        return this.args[this.argPos++];
    }
    protected String nextFile() {
        if ( this.fullstop ) {
            ++this.argPos;
        }
        if ( this.argPos >= this.args.length ) {
            return null;
        }
        return this.args[this.argPos++];
    }
    protected void initWebXml() throws JasperException {
        try {
            if ( this.webxmlLevel >= 10 ) {
                this.mapout = this.openWebxmlWriter ( new File ( this.webxmlFile ) );
                this.servletout = new CharArrayWriter();
                this.mappingout = new CharArrayWriter();
            } else {
                this.mapout = null;
                this.servletout = null;
                this.mappingout = null;
            }
            if ( this.webxmlLevel >= 20 ) {
                this.mapout.write ( Localizer.getMessage ( "jspc.webxml.header", this.webxmlEncoding ) );
                this.mapout.flush();
            } else if ( this.webxmlLevel >= 10 && !this.addWebXmlMappings ) {
                this.mapout.write ( Localizer.getMessage ( "jspc.webinc.header" ) );
                this.mapout.flush();
            }
        } catch ( IOException ioe ) {
            this.mapout = null;
            this.servletout = null;
            this.mappingout = null;
            throw new JasperException ( ioe );
        }
    }
    protected void completeWebXml() {
        if ( this.mapout != null ) {
            try {
                this.servletout.writeTo ( this.mapout );
                this.mappingout.writeTo ( this.mapout );
                if ( this.webxmlLevel >= 20 ) {
                    this.mapout.write ( Localizer.getMessage ( "jspc.webxml.footer" ) );
                } else if ( this.webxmlLevel >= 10 && !this.addWebXmlMappings ) {
                    this.mapout.write ( Localizer.getMessage ( "jspc.webinc.footer" ) );
                }
                this.mapout.close();
            } catch ( IOException ex ) {}
        }
    }
    protected void initTldScanner ( final JspCServletContext context, final ClassLoader classLoader ) {
        if ( this.scanner != null ) {
            return;
        }
        ( this.scanner = this.newTldScanner ( context, true, this.isValidateTld(), this.isBlockExternal() ) ).setClassLoader ( classLoader );
    }
    protected TldScanner newTldScanner ( final JspCServletContext context, final boolean namespaceAware, final boolean validate, final boolean blockExternal ) {
        return new TldScanner ( ( ServletContext ) context, namespaceAware, validate, blockExternal );
    }
    protected void initServletContext ( final ClassLoader classLoader ) throws IOException, JasperException {
        final PrintWriter log = new PrintWriter ( System.out );
        final URL resourceBase = new File ( this.uriRoot ).getCanonicalFile().toURI().toURL();
        this.context = new JspCServletContext ( log, resourceBase, classLoader, this.isValidateXml(), this.isBlockExternal() );
        if ( this.isValidateTld() ) {
            this.context.setInitParameter ( "org.apache.jasper.XML_VALIDATE_TLD", "true" );
        }
        this.initTldScanner ( this.context, classLoader );
        try {
            this.scanner.scan();
        } catch ( SAXException e ) {
            throw new JasperException ( e );
        }
        this.tldCache = new TldCache ( ( ServletContext ) this.context, this.scanner.getUriTldResourcePathMap(), this.scanner.getTldResourcePathTaglibXmlMap() );
        this.context.setAttribute ( TldCache.SERVLET_CONTEXT_ATTRIBUTE_NAME, this.tldCache );
        this.rctxt = new JspRuntimeContext ( ( ServletContext ) this.context, this );
        this.jspConfig = new JspConfig ( ( ServletContext ) this.context );
        this.tagPluginManager = new TagPluginManager ( ( ServletContext ) this.context );
    }
    protected ClassLoader initClassLoader() throws IOException {
        this.classPath = this.getClassPath();
        final ClassLoader jspcLoader = this.getClass().getClassLoader();
        if ( jspcLoader instanceof AntClassLoader ) {
            this.classPath = this.classPath + File.pathSeparator + ( ( AntClassLoader ) jspcLoader ).getClasspath();
        }
        final ArrayList<URL> urls = new ArrayList<URL>();
        final StringTokenizer tokenizer = new StringTokenizer ( this.classPath, File.pathSeparator );
        while ( tokenizer.hasMoreTokens() ) {
            final String path = tokenizer.nextToken();
            try {
                final File libFile = new File ( path );
                urls.add ( libFile.toURI().toURL() );
            } catch ( IOException ioe ) {
                throw new RuntimeException ( ioe.toString() );
            }
        }
        final File webappBase = new File ( this.uriRoot );
        if ( webappBase.exists() ) {
            final File classes = new File ( webappBase, "/WEB-INF/classes" );
            try {
                if ( classes.exists() ) {
                    this.classPath = this.classPath + File.pathSeparator + classes.getCanonicalPath();
                    urls.add ( classes.getCanonicalFile().toURI().toURL() );
                }
            } catch ( IOException ioe2 ) {
                throw new RuntimeException ( ioe2.toString() );
            }
            final File lib = new File ( webappBase, "/WEB-INF/lib" );
            if ( lib.exists() && lib.isDirectory() ) {
                final String[] libs = lib.list();
                if ( libs != null ) {
                    for ( int i = 0; i < libs.length; ++i ) {
                        if ( libs[i].length() >= 5 ) {
                            final String ext = libs[i].substring ( libs[i].length() - 4 );
                            if ( !".jar".equalsIgnoreCase ( ext ) ) {
                                if ( ".tld".equalsIgnoreCase ( ext ) ) {
                                    JspC.log.warn ( "TLD files should not be placed in /WEB-INF/lib" );
                                }
                            } else {
                                try {
                                    final File libFile2 = new File ( lib, libs[i] );
                                    this.classPath = this.classPath + File.pathSeparator + libFile2.getAbsolutePath();
                                    urls.add ( libFile2.getAbsoluteFile().toURI().toURL() );
                                } catch ( IOException ioe3 ) {
                                    throw new RuntimeException ( ioe3.toString() );
                                }
                            }
                        }
                    }
                }
            }
        }
        final URL[] urlsA = new URL[urls.size()];
        urls.toArray ( urlsA );
        return this.loader = new URLClassLoader ( urlsA, this.getClass().getClassLoader() );
    }
    protected void locateUriRoot ( File f ) {
        String tUriBase = this.uriBase;
        if ( tUriBase == null ) {
            tUriBase = "/";
        }
        try {
            if ( f.exists() ) {
                f = new File ( f.getAbsolutePath() );
                while ( true ) {
                    final File g = new File ( f, "WEB-INF" );
                    if ( g.exists() && g.isDirectory() ) {
                        this.uriRoot = f.getCanonicalPath();
                        this.uriBase = tUriBase;
                        if ( JspC.log.isInfoEnabled() ) {
                            JspC.log.info ( Localizer.getMessage ( "jspc.implicit.uriRoot", this.uriRoot ) );
                            break;
                        }
                        break;
                    } else {
                        if ( f.exists() && f.isDirectory() ) {
                            tUriBase = "/" + f.getName() + "/" + tUriBase;
                        }
                        final String fParent = f.getParent();
                        if ( fParent == null ) {
                            break;
                        }
                        f = new File ( fParent );
                    }
                }
                if ( this.uriRoot != null ) {
                    final File froot = new File ( this.uriRoot );
                    this.uriRoot = froot.getCanonicalPath();
                }
            }
        } catch ( IOException ex ) {}
    }
    protected File resolveFile ( final String s ) {
        if ( this.getProject() == null ) {
            return FileUtils.getFileUtils().resolveFile ( ( File ) null, s );
        }
        return FileUtils.getFileUtils().resolveFile ( this.getProject().getBaseDir(), s );
    }
    private Reader openWebxmlReader ( final File file ) throws IOException {
        final FileInputStream fis = new FileInputStream ( file );
        try {
            return ( this.webxmlEncoding != null ) ? new InputStreamReader ( fis, this.webxmlEncoding ) : new InputStreamReader ( fis );
        } catch ( IOException ex ) {
            fis.close();
            throw ex;
        }
    }
    private Writer openWebxmlWriter ( final File file ) throws IOException {
        final FileOutputStream fos = new FileOutputStream ( file );
        try {
            return ( this.webxmlEncoding != null ) ? new OutputStreamWriter ( fos, this.webxmlEncoding ) : new OutputStreamWriter ( fos );
        } catch ( IOException ex ) {
            fos.close();
            throw ex;
        }
    }
    static {
        JspFactory.setDefaultFactory ( ( JspFactory ) new JspFactoryImpl() );
        log = LogFactory.getLog ( JspC.class );
        ( insertBefore = new HashSet<String>() ).add ( "</web-app>" );
        JspC.insertBefore.add ( "<servlet-mapping>" );
        JspC.insertBefore.add ( "<session-config>" );
        JspC.insertBefore.add ( "<mime-mapping>" );
        JspC.insertBefore.add ( "<welcome-file-list>" );
        JspC.insertBefore.add ( "<error-page>" );
        JspC.insertBefore.add ( "<taglib>" );
        JspC.insertBefore.add ( "<resource-env-ref>" );
        JspC.insertBefore.add ( "<resource-ref>" );
        JspC.insertBefore.add ( "<security-constraint>" );
        JspC.insertBefore.add ( "<login-config>" );
        JspC.insertBefore.add ( "<security-role>" );
        JspC.insertBefore.add ( "<env-entry>" );
        JspC.insertBefore.add ( "<ejb-ref>" );
        JspC.insertBefore.add ( "<ejb-local-ref>" );
    }
}
