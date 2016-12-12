package org.apache.jasper;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.io.FileNotFoundException;
import org.apache.tomcat.util.descriptor.tld.TldResourcePath;
import java.util.jar.JarEntry;
import java.net.URLConnection;
import java.io.IOException;
import java.net.JarURLConnection;
import org.apache.jasper.compiler.JspUtil;
import java.util.Set;
import java.net.MalformedURLException;
import java.io.InputStream;
import java.io.File;
import org.apache.jasper.compiler.Localizer;
import org.apache.jasper.servlet.JasperLoader;
import org.apache.juli.logging.LogFactory;
import org.apache.tomcat.Jar;
import javax.servlet.jsp.tagext.TagInfo;
import java.net.URL;
import java.net.URLClassLoader;
import org.apache.jasper.compiler.JspRuntimeContext;
import javax.servlet.ServletContext;
import org.apache.jasper.compiler.Compiler;
import org.apache.jasper.servlet.JspServletWrapper;
import org.apache.jasper.compiler.ServletWriter;
import org.apache.juli.logging.Log;
public class JspCompilationContext {
    private final Log log;
    private String className;
    private final String jspUri;
    private String basePackageName;
    private String derivedPackageName;
    private String servletJavaFileName;
    private String javaPath;
    private String classFileName;
    private ServletWriter writer;
    private final Options options;
    private final JspServletWrapper jsw;
    private Compiler jspCompiler;
    private String classPath;
    private final String baseURI;
    private String outputDir;
    private final ServletContext context;
    private ClassLoader loader;
    private final JspRuntimeContext rctxt;
    private volatile boolean removed;
    private URLClassLoader jspLoader;
    private URL baseUrl;
    private Class<?> servletClass;
    private final boolean isTagFile;
    private boolean protoTypeMode;
    private TagInfo tagInfo;
    private Jar tagJar;
    private static final Object outputDirLock;
    public JspCompilationContext ( final String jspUri, final Options options, final ServletContext context, final JspServletWrapper jsw, final JspRuntimeContext rctxt ) {
        this ( jspUri, null, options, context, jsw, rctxt, null, false );
    }
    public JspCompilationContext ( final String tagfile, final TagInfo tagInfo, final Options options, final ServletContext context, final JspServletWrapper jsw, final JspRuntimeContext rctxt, final Jar tagJar ) {
        this ( tagfile, tagInfo, options, context, jsw, rctxt, tagJar, true );
    }
    private JspCompilationContext ( final String jspUri, final TagInfo tagInfo, final Options options, final ServletContext context, final JspServletWrapper jsw, final JspRuntimeContext rctxt, final Jar tagJar, final boolean isTagFile ) {
        this.log = LogFactory.getLog ( JspCompilationContext.class );
        this.removed = false;
        this.jspUri = canonicalURI ( jspUri );
        this.options = options;
        this.jsw = jsw;
        this.context = context;
        String baseURI = jspUri.substring ( 0, jspUri.lastIndexOf ( 47 ) + 1 );
        if ( baseURI.isEmpty() ) {
            baseURI = "/";
        } else if ( baseURI.charAt ( 0 ) != '/' ) {
            baseURI = "/" + baseURI;
        }
        if ( baseURI.charAt ( baseURI.length() - 1 ) != '/' ) {
            baseURI += '/';
        }
        this.baseURI = baseURI;
        this.rctxt = rctxt;
        this.basePackageName = Constants.JSP_PACKAGE_NAME;
        this.tagInfo = tagInfo;
        this.tagJar = tagJar;
        this.isTagFile = isTagFile;
    }
    public String getClassPath() {
        if ( this.classPath != null ) {
            return this.classPath;
        }
        return this.rctxt.getClassPath();
    }
    public void setClassPath ( final String classPath ) {
        this.classPath = classPath;
    }
    public ClassLoader getClassLoader() {
        if ( this.loader != null ) {
            return this.loader;
        }
        return this.rctxt.getParentClassLoader();
    }
    public void setClassLoader ( final ClassLoader loader ) {
        this.loader = loader;
    }
    public ClassLoader getJspLoader() {
        if ( this.jspLoader == null ) {
            this.jspLoader = new JasperLoader ( new URL[] { this.baseUrl }, this.getClassLoader(), this.rctxt.getPermissionCollection() );
        }
        return this.jspLoader;
    }
    public void clearJspLoader() {
        this.jspLoader = null;
    }
    public String getOutputDir() {
        if ( this.outputDir == null ) {
            this.createOutputDir();
        }
        return this.outputDir;
    }
    public Compiler createCompiler() {
        if ( this.jspCompiler != null ) {
            return this.jspCompiler;
        }
        this.jspCompiler = null;
        if ( this.options.getCompilerClassName() != null ) {
            this.jspCompiler = this.createCompiler ( this.options.getCompilerClassName() );
        } else if ( this.options.getCompiler() == null ) {
            this.jspCompiler = this.createCompiler ( "org.apache.jasper.compiler.JDTCompiler" );
            if ( this.jspCompiler == null ) {
                this.jspCompiler = this.createCompiler ( "org.apache.jasper.compiler.AntCompiler" );
            }
        } else {
            this.jspCompiler = this.createCompiler ( "org.apache.jasper.compiler.AntCompiler" );
            if ( this.jspCompiler == null ) {
                this.jspCompiler = this.createCompiler ( "org.apache.jasper.compiler.JDTCompiler" );
            }
        }
        if ( this.jspCompiler == null ) {
            throw new IllegalStateException ( Localizer.getMessage ( "jsp.error.compiler" ) );
        }
        this.jspCompiler.init ( this, this.jsw );
        return this.jspCompiler;
    }
    protected Compiler createCompiler ( final String className ) {
        Compiler compiler = null;
        try {
            compiler = ( Compiler ) Class.forName ( className ).newInstance();
        } catch ( InstantiationException e ) {
            this.log.warn ( Localizer.getMessage ( "jsp.error.compiler" ), e );
        } catch ( IllegalAccessException e2 ) {
            this.log.warn ( Localizer.getMessage ( "jsp.error.compiler" ), e2 );
        } catch ( NoClassDefFoundError e3 ) {
            if ( this.log.isDebugEnabled() ) {
                this.log.debug ( Localizer.getMessage ( "jsp.error.compiler" ), e3 );
            }
        } catch ( ClassNotFoundException e4 ) {
            if ( this.log.isDebugEnabled() ) {
                this.log.debug ( Localizer.getMessage ( "jsp.error.compiler" ), e4 );
            }
        }
        return compiler;
    }
    public Compiler getCompiler() {
        return this.jspCompiler;
    }
    public String resolveRelativeUri ( final String uri ) {
        if ( uri.startsWith ( "/" ) || uri.startsWith ( File.separator ) ) {
            return uri;
        }
        return this.baseURI + uri;
    }
    public InputStream getResourceAsStream ( final String res ) {
        return this.context.getResourceAsStream ( canonicalURI ( res ) );
    }
    public URL getResource ( final String res ) throws MalformedURLException {
        return this.context.getResource ( canonicalURI ( res ) );
    }
    public Set<String> getResourcePaths ( final String path ) {
        return ( Set<String> ) this.context.getResourcePaths ( canonicalURI ( path ) );
    }
    public String getRealPath ( final String path ) {
        if ( this.context != null ) {
            return this.context.getRealPath ( path );
        }
        return path;
    }
    public Jar getTagFileJar() {
        return this.tagJar;
    }
    public void setTagFileJar ( final Jar tagJar ) {
        this.tagJar = tagJar;
    }
    public String getServletClassName() {
        if ( this.className != null ) {
            return this.className;
        }
        if ( this.isTagFile ) {
            this.className = this.tagInfo.getTagClassName();
            final int lastIndex = this.className.lastIndexOf ( 46 );
            if ( lastIndex != -1 ) {
                this.className = this.className.substring ( lastIndex + 1 );
            }
        } else {
            final int iSep = this.jspUri.lastIndexOf ( 47 ) + 1;
            this.className = JspUtil.makeJavaIdentifier ( this.jspUri.substring ( iSep ) );
        }
        return this.className;
    }
    public void setServletClassName ( final String className ) {
        this.className = className;
    }
    public String getJspFile() {
        return this.jspUri;
    }
    public Long getLastModified ( final String resource ) {
        return this.getLastModified ( resource, this.tagJar );
    }
    public Long getLastModified ( String resource, final Jar tagJar ) {
        long result = -1L;
        URLConnection uc = null;
        try {
            if ( tagJar != null ) {
                if ( resource.startsWith ( "/" ) ) {
                    resource = resource.substring ( 1 );
                }
                result = tagJar.getLastModified ( resource );
            } else {
                final URL jspUrl = this.getResource ( resource );
                if ( jspUrl == null ) {
                    this.incrementRemoved();
                    return result;
                }
                uc = jspUrl.openConnection();
                if ( uc instanceof JarURLConnection ) {
                    final JarEntry jarEntry = ( ( JarURLConnection ) uc ).getJarEntry();
                    if ( jarEntry != null ) {
                        result = jarEntry.getTime();
                    } else {
                        result = uc.getLastModified();
                    }
                } else {
                    result = uc.getLastModified();
                }
            }
        } catch ( IOException e ) {
            if ( this.log.isDebugEnabled() ) {
                this.log.debug ( Localizer.getMessage ( "jsp.error.lastModified", this.getJspFile() ), e );
            }
            result = -1L;
            if ( uc != null ) {
                try {
                    uc.getInputStream().close();
                } catch ( IOException e ) {
                    if ( this.log.isDebugEnabled() ) {
                        this.log.debug ( Localizer.getMessage ( "jsp.error.lastModified", this.getJspFile() ), e );
                    }
                    result = -1L;
                }
            }
        } finally {
            if ( uc != null ) {
                try {
                    uc.getInputStream().close();
                } catch ( IOException e2 ) {
                    if ( this.log.isDebugEnabled() ) {
                        this.log.debug ( Localizer.getMessage ( "jsp.error.lastModified", this.getJspFile() ), e2 );
                    }
                    result = -1L;
                }
            }
        }
        return result;
    }
    public boolean isTagFile() {
        return this.isTagFile;
    }
    public TagInfo getTagInfo() {
        return this.tagInfo;
    }
    public void setTagInfo ( final TagInfo tagi ) {
        this.tagInfo = tagi;
    }
    public boolean isPrototypeMode() {
        return this.protoTypeMode;
    }
    public void setPrototypeMode ( final boolean pm ) {
        this.protoTypeMode = pm;
    }
    public String getServletPackageName() {
        if ( this.isTagFile() ) {
            final String className = this.tagInfo.getTagClassName();
            final int lastIndex = className.lastIndexOf ( 46 );
            String pkgName = "";
            if ( lastIndex != -1 ) {
                pkgName = className.substring ( 0, lastIndex );
            }
            return pkgName;
        }
        final String dPackageName = this.getDerivedPackageName();
        if ( dPackageName.length() == 0 ) {
            return this.basePackageName;
        }
        return this.basePackageName + '.' + this.getDerivedPackageName();
    }
    protected String getDerivedPackageName() {
        if ( this.derivedPackageName == null ) {
            final int iSep = this.jspUri.lastIndexOf ( 47 );
            this.derivedPackageName = ( ( iSep > 0 ) ? JspUtil.makeJavaPackage ( this.jspUri.substring ( 1, iSep ) ) : "" );
        }
        return this.derivedPackageName;
    }
    public void setServletPackageName ( final String servletPackageName ) {
        this.basePackageName = servletPackageName;
    }
    public String getServletJavaFileName() {
        if ( this.servletJavaFileName == null ) {
            this.servletJavaFileName = this.getOutputDir() + this.getServletClassName() + ".java";
        }
        return this.servletJavaFileName;
    }
    public Options getOptions() {
        return this.options;
    }
    public ServletContext getServletContext() {
        return this.context;
    }
    public JspRuntimeContext getRuntimeContext() {
        return this.rctxt;
    }
    public String getJavaPath() {
        if ( this.javaPath != null ) {
            return this.javaPath;
        }
        if ( this.isTagFile() ) {
            final String tagName = this.tagInfo.getTagClassName();
            this.javaPath = tagName.replace ( '.', '/' ) + ".java";
        } else {
            this.javaPath = this.getServletPackageName().replace ( '.', '/' ) + '/' + this.getServletClassName() + ".java";
        }
        return this.javaPath;
    }
    public String getClassFileName() {
        if ( this.classFileName == null ) {
            this.classFileName = this.getOutputDir() + this.getServletClassName() + ".class";
        }
        return this.classFileName;
    }
    public ServletWriter getWriter() {
        return this.writer;
    }
    public void setWriter ( final ServletWriter writer ) {
        this.writer = writer;
    }
    public TldResourcePath getTldResourcePath ( final String uri ) {
        return this.getOptions().getTldCache().getTldResourcePath ( uri );
    }
    public boolean keepGenerated() {
        return this.getOptions().getKeepGenerated();
    }
    public void incrementRemoved() {
        if ( !this.removed && this.rctxt != null ) {
            this.rctxt.removeWrapper ( this.jspUri );
        }
        this.removed = true;
    }
    public boolean isRemoved() {
        return this.removed;
    }
    public void compile() throws JasperException, FileNotFoundException {
        this.createCompiler();
        if ( this.jspCompiler.isOutDated() ) {
            if ( this.isRemoved() ) {
                throw new FileNotFoundException ( this.jspUri );
            }
            try {
                this.jspCompiler.removeGeneratedFiles();
                this.jspLoader = null;
                this.jspCompiler.compile();
                this.jsw.setReload ( true );
                this.jsw.setCompilationException ( null );
            } catch ( JasperException ex ) {
                this.jsw.setCompilationException ( ex );
                if ( this.options.getDevelopment() && this.options.getRecompileOnFail() ) {
                    this.jsw.setLastModificationTest ( -1L );
                }
                throw ex;
            } catch ( FileNotFoundException fnfe ) {
                throw fnfe;
            } catch ( Exception ex2 ) {
                final JasperException je = new JasperException ( Localizer.getMessage ( "jsp.error.unable.compile" ), ex2 );
                this.jsw.setCompilationException ( je );
                throw je;
            }
        }
    }
    public Class<?> load() throws JasperException {
        try {
            this.getJspLoader();
            final String name = this.getFQCN();
            this.servletClass = this.jspLoader.loadClass ( name );
        } catch ( ClassNotFoundException cex ) {
            throw new JasperException ( Localizer.getMessage ( "jsp.error.unable.load" ), cex );
        } catch ( Exception ex ) {
            throw new JasperException ( Localizer.getMessage ( "jsp.error.unable.compile" ), ex );
        }
        this.removed = false;
        return this.servletClass;
    }
    public String getFQCN() {
        String name;
        if ( this.isTagFile() ) {
            name = this.tagInfo.getTagClassName();
        } else {
            name = this.getServletPackageName() + "." + this.getServletClassName();
        }
        return name;
    }
    public void checkOutputDir() {
        if ( this.outputDir != null ) {
            if ( !new File ( this.outputDir ).exists() ) {
                this.makeOutputDir();
            }
        } else {
            this.createOutputDir();
        }
    }
    protected boolean makeOutputDir() {
        synchronized ( JspCompilationContext.outputDirLock ) {
            final File outDirFile = new File ( this.outputDir );
            return outDirFile.mkdirs() || outDirFile.isDirectory();
        }
    }
    protected void createOutputDir() {
        String path = null;
        if ( this.isTagFile() ) {
            final String tagName = this.tagInfo.getTagClassName();
            path = tagName.replace ( '.', File.separatorChar );
            path = path.substring ( 0, path.lastIndexOf ( File.separatorChar ) );
        } else {
            path = this.getServletPackageName().replace ( '.', File.separatorChar );
        }
        try {
            final File base = this.options.getScratchDir();
            this.baseUrl = base.toURI().toURL();
            this.outputDir = base.getAbsolutePath() + File.separator + path + File.separator;
            if ( !this.makeOutputDir() ) {
                throw new IllegalStateException ( Localizer.getMessage ( "jsp.error.outputfolder" ) );
            }
        } catch ( MalformedURLException e ) {
            throw new IllegalStateException ( Localizer.getMessage ( "jsp.error.outputfolder" ), e );
        }
    }
    protected static final boolean isPathSeparator ( final char c ) {
        return c == '/' || c == '\\';
    }
    protected static final String canonicalURI ( final String s ) {
        if ( s == null ) {
            return null;
        }
        final StringBuilder result = new StringBuilder();
        final int len = s.length();
        int pos = 0;
        while ( pos < len ) {
            final char c = s.charAt ( pos );
            if ( isPathSeparator ( c ) ) {
                while ( pos + 1 < len && isPathSeparator ( s.charAt ( pos + 1 ) ) ) {
                    ++pos;
                }
                if ( pos + 1 < len && s.charAt ( pos + 1 ) == '.' ) {
                    if ( pos + 2 >= len ) {
                        break;
                    }
                    switch ( s.charAt ( pos + 2 ) ) {
                    case '/':
                    case '\\': {
                        pos += 2;
                        continue;
                    }
                    case '.': {
                        if ( pos + 3 >= len || !isPathSeparator ( s.charAt ( pos + 3 ) ) ) {
                            break;
                        }
                        pos += 3;
                        int separatorPos;
                        for ( separatorPos = result.length() - 1; separatorPos >= 0 && !isPathSeparator ( result.charAt ( separatorPos ) ); --separatorPos ) {}
                        if ( separatorPos >= 0 ) {
                            result.setLength ( separatorPos );
                            continue;
                        }
                        continue;
                    }
                    }
                }
            }
            result.append ( c );
            ++pos;
        }
        return result.toString();
    }
    static {
        outputDirLock = new Object();
    }
}
