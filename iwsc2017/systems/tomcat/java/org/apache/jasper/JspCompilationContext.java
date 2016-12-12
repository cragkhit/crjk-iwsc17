package org.apache.jasper;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.JarURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.net.URLConnection;
import java.util.Set;
import java.util.jar.JarEntry;
import javax.servlet.ServletContext;
import javax.servlet.jsp.tagext.TagInfo;
import org.apache.jasper.compiler.Compiler;
import org.apache.jasper.compiler.JspRuntimeContext;
import org.apache.jasper.compiler.JspUtil;
import org.apache.jasper.compiler.Localizer;
import org.apache.jasper.compiler.ServletWriter;
import org.apache.jasper.servlet.JasperLoader;
import org.apache.jasper.servlet.JspServletWrapper;
import org.apache.juli.logging.Log;
import org.apache.juli.logging.LogFactory;
import org.apache.tomcat.Jar;
import org.apache.tomcat.util.descriptor.tld.TldResourcePath;
public class JspCompilationContext {
    private final Log log = LogFactory.getLog ( JspCompilationContext.class );
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
    private volatile boolean removed = false;
    private URLClassLoader jspLoader;
    private URL baseUrl;
    private Class<?> servletClass;
    private final boolean isTagFile;
    private boolean protoTypeMode;
    private TagInfo tagInfo;
    private Jar tagJar;
    public JspCompilationContext ( String jspUri, Options options,
                                   ServletContext context, JspServletWrapper jsw,
                                   JspRuntimeContext rctxt ) {
        this ( jspUri, null, options, context, jsw, rctxt, null, false );
    }
    public JspCompilationContext ( String tagfile, TagInfo tagInfo,
                                   Options options, ServletContext context, JspServletWrapper jsw,
                                   JspRuntimeContext rctxt, Jar tagJar ) {
        this ( tagfile, tagInfo, options, context, jsw, rctxt, tagJar, true );
    }
    private JspCompilationContext ( String jspUri, TagInfo tagInfo,
                                    Options options, ServletContext context, JspServletWrapper jsw,
                                    JspRuntimeContext rctxt, Jar tagJar, boolean isTagFile ) {
        this.jspUri = canonicalURI ( jspUri );
        this.options = options;
        this.jsw = jsw;
        this.context = context;
        String baseURI = jspUri.substring ( 0, jspUri.lastIndexOf ( '/' ) + 1 );
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
        if ( classPath != null ) {
            return classPath;
        }
        return rctxt.getClassPath();
    }
    public void setClassPath ( String classPath ) {
        this.classPath = classPath;
    }
    public ClassLoader getClassLoader() {
        if ( loader != null ) {
            return loader;
        }
        return rctxt.getParentClassLoader();
    }
    public void setClassLoader ( ClassLoader loader ) {
        this.loader = loader;
    }
    public ClassLoader getJspLoader() {
        if ( jspLoader == null ) {
            jspLoader = new JasperLoader
            ( new URL[] {baseUrl},
              getClassLoader(),
              rctxt.getPermissionCollection() );
        }
        return jspLoader;
    }
    public void clearJspLoader() {
        jspLoader = null;
    }
    public String getOutputDir() {
        if ( outputDir == null ) {
            createOutputDir();
        }
        return outputDir;
    }
    public Compiler createCompiler() {
        if ( jspCompiler != null ) {
            return jspCompiler;
        }
        jspCompiler = null;
        if ( options.getCompilerClassName() != null ) {
            jspCompiler = createCompiler ( options.getCompilerClassName() );
        } else {
            if ( options.getCompiler() == null ) {
                jspCompiler = createCompiler ( "org.apache.jasper.compiler.JDTCompiler" );
                if ( jspCompiler == null ) {
                    jspCompiler = createCompiler ( "org.apache.jasper.compiler.AntCompiler" );
                }
            } else {
                jspCompiler = createCompiler ( "org.apache.jasper.compiler.AntCompiler" );
                if ( jspCompiler == null ) {
                    jspCompiler = createCompiler ( "org.apache.jasper.compiler.JDTCompiler" );
                }
            }
        }
        if ( jspCompiler == null ) {
            throw new IllegalStateException ( Localizer.getMessage ( "jsp.error.compiler" ) );
        }
        jspCompiler.init ( this, jsw );
        return jspCompiler;
    }
    protected Compiler createCompiler ( String className ) {
        Compiler compiler = null;
        try {
            compiler = ( Compiler ) Class.forName ( className ).newInstance();
        } catch ( InstantiationException e ) {
            log.warn ( Localizer.getMessage ( "jsp.error.compiler" ), e );
        } catch ( IllegalAccessException e ) {
            log.warn ( Localizer.getMessage ( "jsp.error.compiler" ), e );
        } catch ( NoClassDefFoundError e ) {
            if ( log.isDebugEnabled() ) {
                log.debug ( Localizer.getMessage ( "jsp.error.compiler" ), e );
            }
        } catch ( ClassNotFoundException e ) {
            if ( log.isDebugEnabled() ) {
                log.debug ( Localizer.getMessage ( "jsp.error.compiler" ), e );
            }
        }
        return compiler;
    }
    public Compiler getCompiler() {
        return jspCompiler;
    }
    public String resolveRelativeUri ( String uri ) {
        if ( uri.startsWith ( "/" ) || uri.startsWith ( File.separator ) ) {
            return uri;
        } else {
            return baseURI + uri;
        }
    }
    public java.io.InputStream getResourceAsStream ( String res ) {
        return context.getResourceAsStream ( canonicalURI ( res ) );
    }
    public URL getResource ( String res ) throws MalformedURLException {
        return context.getResource ( canonicalURI ( res ) );
    }
    public Set<String> getResourcePaths ( String path ) {
        return context.getResourcePaths ( canonicalURI ( path ) );
    }
    public String getRealPath ( String path ) {
        if ( context != null ) {
            return context.getRealPath ( path );
        }
        return path;
    }
    public Jar getTagFileJar() {
        return this.tagJar;
    }
    public void setTagFileJar ( Jar tagJar ) {
        this.tagJar = tagJar;
    }
    public String getServletClassName() {
        if ( className != null ) {
            return className;
        }
        if ( isTagFile ) {
            className = tagInfo.getTagClassName();
            int lastIndex = className.lastIndexOf ( '.' );
            if ( lastIndex != -1 ) {
                className = className.substring ( lastIndex + 1 );
            }
        } else {
            int iSep = jspUri.lastIndexOf ( '/' ) + 1;
            className = JspUtil.makeJavaIdentifier ( jspUri.substring ( iSep ) );
        }
        return className;
    }
    public void setServletClassName ( String className ) {
        this.className = className;
    }
    public String getJspFile() {
        return jspUri;
    }
    public Long getLastModified ( String resource ) {
        return getLastModified ( resource, tagJar );
    }
    public Long getLastModified ( String resource, Jar tagJar ) {
        long result = -1;
        URLConnection uc = null;
        try {
            if ( tagJar != null ) {
                if ( resource.startsWith ( "/" ) ) {
                    resource = resource.substring ( 1 );
                }
                result = tagJar.getLastModified ( resource );
            } else {
                URL jspUrl = getResource ( resource );
                if ( jspUrl == null ) {
                    incrementRemoved();
                    return Long.valueOf ( result );
                }
                uc = jspUrl.openConnection();
                if ( uc instanceof JarURLConnection ) {
                    JarEntry jarEntry = ( ( JarURLConnection ) uc ).getJarEntry();
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
            if ( log.isDebugEnabled() ) {
                log.debug ( Localizer.getMessage (
                                "jsp.error.lastModified", getJspFile() ), e );
            }
            result = -1;
        } finally {
            if ( uc != null ) {
                try {
                    uc.getInputStream().close();
                } catch ( IOException e ) {
                    if ( log.isDebugEnabled() ) {
                        log.debug ( Localizer.getMessage (
                                        "jsp.error.lastModified", getJspFile() ), e );
                    }
                    result = -1;
                }
            }
        }
        return Long.valueOf ( result );
    }
    public boolean isTagFile() {
        return isTagFile;
    }
    public TagInfo getTagInfo() {
        return tagInfo;
    }
    public void setTagInfo ( TagInfo tagi ) {
        tagInfo = tagi;
    }
    public boolean isPrototypeMode() {
        return protoTypeMode;
    }
    public void setPrototypeMode ( boolean pm ) {
        protoTypeMode = pm;
    }
    public String getServletPackageName() {
        if ( isTagFile() ) {
            String className = tagInfo.getTagClassName();
            int lastIndex = className.lastIndexOf ( '.' );
            String pkgName = "";
            if ( lastIndex != -1 ) {
                pkgName = className.substring ( 0, lastIndex );
            }
            return pkgName;
        } else {
            String dPackageName = getDerivedPackageName();
            if ( dPackageName.length() == 0 ) {
                return basePackageName;
            }
            return basePackageName + '.' + getDerivedPackageName();
        }
    }
    protected String getDerivedPackageName() {
        if ( derivedPackageName == null ) {
            int iSep = jspUri.lastIndexOf ( '/' );
            derivedPackageName = ( iSep > 0 ) ?
                                 JspUtil.makeJavaPackage ( jspUri.substring ( 1, iSep ) ) : "";
        }
        return derivedPackageName;
    }
    public void setServletPackageName ( String servletPackageName ) {
        this.basePackageName = servletPackageName;
    }
    public String getServletJavaFileName() {
        if ( servletJavaFileName == null ) {
            servletJavaFileName = getOutputDir() + getServletClassName() + ".java";
        }
        return servletJavaFileName;
    }
    public Options getOptions() {
        return options;
    }
    public ServletContext getServletContext() {
        return context;
    }
    public JspRuntimeContext getRuntimeContext() {
        return rctxt;
    }
    public String getJavaPath() {
        if ( javaPath != null ) {
            return javaPath;
        }
        if ( isTagFile() ) {
            String tagName = tagInfo.getTagClassName();
            javaPath = tagName.replace ( '.', '/' ) + ".java";
        } else {
            javaPath = getServletPackageName().replace ( '.', '/' ) + '/' +
                       getServletClassName() + ".java";
        }
        return javaPath;
    }
    public String getClassFileName() {
        if ( classFileName == null ) {
            classFileName = getOutputDir() + getServletClassName() + ".class";
        }
        return classFileName;
    }
    public ServletWriter getWriter() {
        return writer;
    }
    public void setWriter ( ServletWriter writer ) {
        this.writer = writer;
    }
    public TldResourcePath getTldResourcePath ( String uri ) {
        return getOptions().getTldCache().getTldResourcePath ( uri );
    }
    public boolean keepGenerated() {
        return getOptions().getKeepGenerated();
    }
    public void incrementRemoved() {
        if ( removed == false && rctxt != null ) {
            rctxt.removeWrapper ( jspUri );
        }
        removed = true;
    }
    public boolean isRemoved() {
        return removed;
    }
    public void compile() throws JasperException, FileNotFoundException {
        createCompiler();
        if ( jspCompiler.isOutDated() ) {
            if ( isRemoved() ) {
                throw new FileNotFoundException ( jspUri );
            }
            try {
                jspCompiler.removeGeneratedFiles();
                jspLoader = null;
                jspCompiler.compile();
                jsw.setReload ( true );
                jsw.setCompilationException ( null );
            } catch ( JasperException ex ) {
                jsw.setCompilationException ( ex );
                if ( options.getDevelopment() && options.getRecompileOnFail() ) {
                    jsw.setLastModificationTest ( -1 );
                }
                throw ex;
            } catch ( FileNotFoundException fnfe ) {
                throw fnfe;
            } catch ( Exception ex ) {
                JasperException je = new JasperException (
                    Localizer.getMessage ( "jsp.error.unable.compile" ),
                    ex );
                jsw.setCompilationException ( je );
                throw je;
            }
        }
    }
    public Class<?> load() throws JasperException {
        try {
            getJspLoader();
            String name = getFQCN();
            servletClass = jspLoader.loadClass ( name );
        } catch ( ClassNotFoundException cex ) {
            throw new JasperException ( Localizer.getMessage ( "jsp.error.unable.load" ),
                                        cex );
        } catch ( Exception ex ) {
            throw new JasperException ( Localizer.getMessage ( "jsp.error.unable.compile" ),
                                        ex );
        }
        removed = false;
        return servletClass;
    }
    public String getFQCN() {
        String name;
        if ( isTagFile() ) {
            name = tagInfo.getTagClassName();
        } else {
            name = getServletPackageName() + "." + getServletClassName();
        }
        return name;
    }
    private static final Object outputDirLock = new Object();
    public void checkOutputDir() {
        if ( outputDir != null ) {
            if ( ! ( new File ( outputDir ) ).exists() ) {
                makeOutputDir();
            }
        } else {
            createOutputDir();
        }
    }
    protected boolean makeOutputDir() {
        synchronized ( outputDirLock ) {
            File outDirFile = new File ( outputDir );
            return ( outDirFile.mkdirs() || outDirFile.isDirectory() );
        }
    }
    protected void createOutputDir() {
        String path = null;
        if ( isTagFile() ) {
            String tagName = tagInfo.getTagClassName();
            path = tagName.replace ( '.', File.separatorChar );
            path = path.substring ( 0, path.lastIndexOf ( File.separatorChar ) );
        } else {
            path = getServletPackageName().replace ( '.', File.separatorChar );
        }
        try {
            File base = options.getScratchDir();
            baseUrl = base.toURI().toURL();
            outputDir = base.getAbsolutePath() + File.separator + path +
                        File.separator;
            if ( !makeOutputDir() ) {
                throw new IllegalStateException ( Localizer.getMessage ( "jsp.error.outputfolder" ) );
            }
        } catch ( MalformedURLException e ) {
            throw new IllegalStateException ( Localizer.getMessage ( "jsp.error.outputfolder" ), e );
        }
    }
    protected static final boolean isPathSeparator ( char c ) {
        return ( c == '/' || c == '\\' );
    }
    protected static final String canonicalURI ( String s ) {
        if ( s == null ) {
            return null;
        }
        StringBuilder result = new StringBuilder();
        final int len = s.length();
        int pos = 0;
        while ( pos < len ) {
            char c = s.charAt ( pos );
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
                    case '\\':
                        pos += 2;
                        continue;
                    case '.':
                        if ( pos + 3 < len && isPathSeparator ( s.charAt ( pos + 3 ) ) ) {
                            pos += 3;
                            int separatorPos = result.length() - 1;
                            while ( separatorPos >= 0 &&
                                    ! isPathSeparator ( result
                                                        .charAt ( separatorPos ) ) ) {
                                --separatorPos;
                            }
                            if ( separatorPos >= 0 ) {
                                result.setLength ( separatorPos );
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
}
