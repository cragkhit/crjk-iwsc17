package org.apache.jasper.compiler;
import java.net.URLConnection;
import org.apache.tomcat.Jar;
import java.util.Iterator;
import java.net.JarURLConnection;
import org.apache.tomcat.util.scan.JarFactory;
import java.net.URL;
import java.util.Map;
import org.apache.jasper.JasperException;
import java.io.FileNotFoundException;
import java.io.Writer;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.FileOutputStream;
import java.io.File;
import org.apache.juli.logging.LogFactory;
import org.apache.jasper.Options;
import org.apache.jasper.servlet.JspServletWrapper;
import org.apache.jasper.JspCompilationContext;
import org.apache.juli.logging.Log;
public abstract class Compiler {
    private final Log log;
    protected JspCompilationContext ctxt;
    protected ErrorDispatcher errDispatcher;
    protected PageInfo pageInfo;
    protected JspServletWrapper jsw;
    protected TagFileProcessor tfp;
    protected Options options;
    protected Node.Nodes pageNodes;
    public Compiler() {
        this.log = LogFactory.getLog ( Compiler.class );
    }
    public void init ( final JspCompilationContext ctxt, final JspServletWrapper jsw ) {
        this.jsw = jsw;
        this.ctxt = ctxt;
        this.options = ctxt.getOptions();
    }
    public Node.Nodes getPageNodes() {
        return this.pageNodes;
    }
    protected String[] generateJava() throws Exception {
        String[] smapStr = null;
        long t4;
        long t3;
        long t2 = t2 = ( t3 = ( t4 = 0L ) );
        if ( this.log.isDebugEnabled() ) {
            t2 = System.currentTimeMillis();
        }
        this.pageInfo = new PageInfo ( new BeanRepository ( this.ctxt.getClassLoader(), this.errDispatcher ), this.ctxt.getJspFile(), this.ctxt.isTagFile() );
        final JspConfig jspConfig = this.options.getJspConfig();
        final JspConfig.JspProperty jspProperty = jspConfig.findJspProperty ( this.ctxt.getJspFile() );
        if ( jspProperty.isELIgnored() != null ) {
            this.pageInfo.setELIgnored ( JspUtil.booleanValue ( jspProperty.isELIgnored() ) );
        }
        if ( jspProperty.isScriptingInvalid() != null ) {
            this.pageInfo.setScriptingInvalid ( JspUtil.booleanValue ( jspProperty.isScriptingInvalid() ) );
        }
        if ( jspProperty.getIncludePrelude() != null ) {
            this.pageInfo.setIncludePrelude ( jspProperty.getIncludePrelude() );
        }
        if ( jspProperty.getIncludeCoda() != null ) {
            this.pageInfo.setIncludeCoda ( jspProperty.getIncludeCoda() );
        }
        if ( jspProperty.isDeferedSyntaxAllowedAsLiteral() != null ) {
            this.pageInfo.setDeferredSyntaxAllowedAsLiteral ( JspUtil.booleanValue ( jspProperty.isDeferedSyntaxAllowedAsLiteral() ) );
        }
        if ( jspProperty.isTrimDirectiveWhitespaces() != null ) {
            this.pageInfo.setTrimDirectiveWhitespaces ( JspUtil.booleanValue ( jspProperty.isTrimDirectiveWhitespaces() ) );
        }
        if ( jspProperty.getBuffer() != null ) {
            this.pageInfo.setBufferValue ( jspProperty.getBuffer(), null, this.errDispatcher );
        }
        if ( jspProperty.isErrorOnUndeclaredNamespace() != null ) {
            this.pageInfo.setErrorOnUndeclaredNamespace ( JspUtil.booleanValue ( jspProperty.isErrorOnUndeclaredNamespace() ) );
        }
        if ( this.ctxt.isTagFile() ) {
            try {
                final double libraryVersion = Double.parseDouble ( this.ctxt.getTagInfo().getTagLibrary().getRequiredVersion() );
                if ( libraryVersion < 2.0 ) {
                    this.pageInfo.setIsELIgnored ( "true", null, this.errDispatcher, true );
                }
                if ( libraryVersion < 2.1 ) {
                    this.pageInfo.setDeferredSyntaxAllowedAsLiteral ( "true", null, this.errDispatcher, true );
                }
            } catch ( NumberFormatException ex ) {
                this.errDispatcher.jspError ( ex );
            }
        }
        this.ctxt.checkOutputDir();
        final String javaFileName = this.ctxt.getServletJavaFileName();
        try {
            final ParserController parserCtl = new ParserController ( this.ctxt, this );
            final Node.Nodes directives = parserCtl.parseDirectives ( this.ctxt.getJspFile() );
            Validator.validateDirectives ( this, directives );
            this.pageNodes = parserCtl.parse ( this.ctxt.getJspFile() );
            if ( this.pageInfo.getContentType() == null && jspProperty.getDefaultContentType() != null ) {
                this.pageInfo.setContentType ( jspProperty.getDefaultContentType() );
            }
            if ( this.ctxt.isPrototypeMode() ) {
                try ( final ServletWriter writer = this.setupContextWriter ( javaFileName ) ) {
                    Generator.generate ( writer, this, this.pageNodes );
                    return null;
                }
            }
            Validator.validateExDirectives ( this, this.pageNodes );
            if ( this.log.isDebugEnabled() ) {
                t2 = System.currentTimeMillis();
            }
            Collector.collect ( this, this.pageNodes );
            ( this.tfp = new TagFileProcessor() ).loadTagFiles ( this, this.pageNodes );
            if ( this.log.isDebugEnabled() ) {
                t3 = System.currentTimeMillis();
            }
            ScriptingVariabler.set ( this.pageNodes, this.errDispatcher );
            final TagPluginManager tagPluginManager = this.options.getTagPluginManager();
            tagPluginManager.apply ( this.pageNodes, this.errDispatcher, this.pageInfo );
            TextOptimizer.concatenate ( this, this.pageNodes );
            ELFunctionMapper.map ( this.pageNodes );
            try ( final ServletWriter writer2 = this.setupContextWriter ( javaFileName ) ) {
                Generator.generate ( writer2, this, this.pageNodes );
            }
            this.ctxt.setWriter ( null );
            if ( this.log.isDebugEnabled() ) {
                t4 = System.currentTimeMillis();
                this.log.debug ( "Generated " + javaFileName + " total=" + ( t4 - t2 ) + " generate=" + ( t4 - t3 ) + " validate=" + ( t2 - t2 ) );
            }
        } catch ( Exception e ) {
            final File file = new File ( javaFileName );
            if ( file.exists() && !file.delete() ) {
                this.log.warn ( Localizer.getMessage ( "jsp.warning.compiler.javafile.delete.fail", file.getAbsolutePath() ) );
            }
            throw e;
        }
        if ( !this.options.isSmapSuppressed() ) {
            smapStr = SmapUtil.generateSmap ( this.ctxt, this.pageNodes );
        }
        this.tfp.removeProtoTypeFiles ( this.ctxt.getClassFileName() );
        return smapStr;
    }
    private ServletWriter setupContextWriter ( final String javaFileName ) throws FileNotFoundException, JasperException {
        final String javaEncoding = this.ctxt.getOptions().getJavaEncoding();
        OutputStreamWriter osw = null;
        try {
            osw = new OutputStreamWriter ( new FileOutputStream ( javaFileName ), javaEncoding );
        } catch ( UnsupportedEncodingException ex ) {
            this.errDispatcher.jspError ( "jsp.error.needAlternateJavaEncoding", javaEncoding );
        }
        final ServletWriter writer = new ServletWriter ( new PrintWriter ( osw ) );
        this.ctxt.setWriter ( writer );
        return writer;
    }
    protected abstract void generateClass ( final String[] p0 ) throws FileNotFoundException, JasperException, Exception;
    public void compile() throws FileNotFoundException, JasperException, Exception {
        this.compile ( true );
    }
    public void compile ( final boolean compileClass ) throws FileNotFoundException, JasperException, Exception {
        this.compile ( compileClass, false );
    }
    public void compile ( final boolean compileClass, final boolean jspcMode ) throws FileNotFoundException, JasperException, Exception {
        if ( this.errDispatcher == null ) {
            this.errDispatcher = new ErrorDispatcher ( jspcMode );
        }
        try {
            final String[] smap = this.generateJava();
            final File javaFile = new File ( this.ctxt.getServletJavaFileName() );
            final Long jspLastModified = this.ctxt.getLastModified ( this.ctxt.getJspFile() );
            javaFile.setLastModified ( jspLastModified );
            if ( compileClass ) {
                this.generateClass ( smap );
                final File targetFile = new File ( this.ctxt.getClassFileName() );
                if ( targetFile.exists() ) {
                    targetFile.setLastModified ( jspLastModified );
                    if ( this.jsw != null ) {
                        this.jsw.setServletClassLastModifiedTime ( jspLastModified );
                    }
                }
            }
        } finally {
            if ( this.tfp != null && this.ctxt.isPrototypeMode() ) {
                this.tfp.removeProtoTypeFiles ( null );
            }
            this.tfp = null;
            this.errDispatcher = null;
            this.pageInfo = null;
            if ( !this.options.getDevelopment() ) {
                this.pageNodes = null;
            }
            if ( this.ctxt.getWriter() != null ) {
                this.ctxt.getWriter().close();
                this.ctxt.setWriter ( null );
            }
        }
    }
    public boolean isOutDated() {
        return this.isOutDated ( true );
    }
    public boolean isOutDated ( final boolean checkClass ) {
        if ( this.jsw != null && this.ctxt.getOptions().getModificationTestInterval() > 0 ) {
            if ( this.jsw.getLastModificationTest() + this.ctxt.getOptions().getModificationTestInterval() * 1000 > System.currentTimeMillis() ) {
                return false;
            }
            this.jsw.setLastModificationTest ( System.currentTimeMillis() );
        }
        File targetFile;
        if ( checkClass ) {
            targetFile = new File ( this.ctxt.getClassFileName() );
        } else {
            targetFile = new File ( this.ctxt.getServletJavaFileName() );
        }
        if ( !targetFile.exists() ) {
            return true;
        }
        final long targetLastModified = targetFile.lastModified();
        if ( checkClass && this.jsw != null ) {
            this.jsw.setServletClassLastModifiedTime ( targetLastModified );
        }
        final Long jspRealLastModified = this.ctxt.getLastModified ( this.ctxt.getJspFile() );
        if ( jspRealLastModified < 0L ) {
            return true;
        }
        if ( targetLastModified != jspRealLastModified ) {
            if ( this.log.isDebugEnabled() ) {
                this.log.debug ( "Compiler: outdated: " + targetFile + " " + targetLastModified );
            }
            return true;
        }
        if ( this.jsw == null ) {
            return false;
        }
        final Map<String, Long> depends = this.jsw.getDependants();
        if ( depends == null ) {
            return false;
        }
        for ( final Map.Entry<String, Long> include : depends.entrySet() ) {
            try {
                final String key = include.getKey();
                long includeLastModified = 0L;
                if ( key.startsWith ( "jar:jar:" ) ) {
                    final int entryStart = key.lastIndexOf ( "!/" );
                    final String entry = key.substring ( entryStart + 2 );
                    try ( final Jar jar = JarFactory.newInstance ( new URL ( key.substring ( 4, entryStart ) ) ) ) {
                        includeLastModified = jar.getLastModified ( entry );
                    }
                } else {
                    URL includeUrl;
                    if ( key.startsWith ( "jar:" ) || key.startsWith ( "file:" ) ) {
                        includeUrl = new URL ( key );
                    } else {
                        includeUrl = this.ctxt.getResource ( include.getKey() );
                    }
                    if ( includeUrl == null ) {
                        return true;
                    }
                    final URLConnection iuc = includeUrl.openConnection();
                    if ( iuc instanceof JarURLConnection ) {
                        includeLastModified = ( ( JarURLConnection ) iuc ).getJarEntry().getTime();
                    } else {
                        includeLastModified = iuc.getLastModified();
                    }
                    iuc.getInputStream().close();
                }
                if ( includeLastModified != include.getValue() ) {
                    return true;
                }
                continue;
            } catch ( Exception e ) {
                if ( this.log.isDebugEnabled() ) {
                    this.log.debug ( "Problem accessing resource. Treat as outdated.", e );
                }
                return true;
            }
        }
        return false;
    }
    public ErrorDispatcher getErrorDispatcher() {
        return this.errDispatcher;
    }
    public PageInfo getPageInfo() {
        return this.pageInfo;
    }
    public JspCompilationContext getCompilationContext() {
        return this.ctxt;
    }
    public void removeGeneratedFiles() {
        this.removeGeneratedClassFiles();
        try {
            final File javaFile = new File ( this.ctxt.getServletJavaFileName() );
            if ( this.log.isDebugEnabled() ) {
                this.log.debug ( "Deleting " + javaFile );
            }
            if ( javaFile.exists() && !javaFile.delete() ) {
                this.log.warn ( Localizer.getMessage ( "jsp.warning.compiler.javafile.delete.fail", javaFile.getAbsolutePath() ) );
            }
        } catch ( Exception e ) {
            this.log.warn ( Localizer.getMessage ( "jsp.warning.compiler.classfile.delete.fail.unknown" ), e );
        }
    }
    public void removeGeneratedClassFiles() {
        try {
            final File classFile = new File ( this.ctxt.getClassFileName() );
            if ( this.log.isDebugEnabled() ) {
                this.log.debug ( "Deleting " + classFile );
            }
            if ( classFile.exists() && !classFile.delete() ) {
                this.log.warn ( Localizer.getMessage ( "jsp.warning.compiler.classfile.delete.fail", classFile.getAbsolutePath() ) );
            }
        } catch ( Exception e ) {
            this.log.warn ( Localizer.getMessage ( "jsp.warning.compiler.classfile.delete.fail.unknown" ), e );
        }
    }
}
