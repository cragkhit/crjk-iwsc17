package org.apache.jasper.compiler;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.net.JarURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import org.apache.jasper.JasperException;
import org.apache.jasper.JspCompilationContext;
import org.apache.jasper.Options;
import org.apache.jasper.servlet.JspServletWrapper;
import org.apache.juli.logging.Log;
import org.apache.juli.logging.LogFactory;
import org.apache.tomcat.Jar;
import org.apache.tomcat.util.scan.JarFactory;
public abstract class Compiler {
    private final Log log = LogFactory.getLog ( Compiler.class );
    protected JspCompilationContext ctxt;
    protected ErrorDispatcher errDispatcher;
    protected PageInfo pageInfo;
    protected JspServletWrapper jsw;
    protected TagFileProcessor tfp;
    protected Options options;
    protected Node.Nodes pageNodes;
    public void init ( JspCompilationContext ctxt, JspServletWrapper jsw ) {
        this.jsw = jsw;
        this.ctxt = ctxt;
        this.options = ctxt.getOptions();
    }
    public Node.Nodes getPageNodes() {
        return this.pageNodes;
    }
    protected String[] generateJava() throws Exception {
        String[] smapStr = null;
        long t1, t2, t3, t4;
        t1 = t2 = t3 = t4 = 0;
        if ( log.isDebugEnabled() ) {
            t1 = System.currentTimeMillis();
        }
        pageInfo = new PageInfo ( new BeanRepository ( ctxt.getClassLoader(),
                                  errDispatcher ), ctxt.getJspFile(), ctxt.isTagFile() );
        JspConfig jspConfig = options.getJspConfig();
        JspConfig.JspProperty jspProperty = jspConfig.findJspProperty ( ctxt
                                            .getJspFile() );
        if ( jspProperty.isELIgnored() != null ) {
            pageInfo.setELIgnored ( JspUtil.booleanValue ( jspProperty
                                    .isELIgnored() ) );
        }
        if ( jspProperty.isScriptingInvalid() != null ) {
            pageInfo.setScriptingInvalid ( JspUtil.booleanValue ( jspProperty
                                           .isScriptingInvalid() ) );
        }
        if ( jspProperty.getIncludePrelude() != null ) {
            pageInfo.setIncludePrelude ( jspProperty.getIncludePrelude() );
        }
        if ( jspProperty.getIncludeCoda() != null ) {
            pageInfo.setIncludeCoda ( jspProperty.getIncludeCoda() );
        }
        if ( jspProperty.isDeferedSyntaxAllowedAsLiteral() != null ) {
            pageInfo.setDeferredSyntaxAllowedAsLiteral ( JspUtil.booleanValue ( jspProperty
                    .isDeferedSyntaxAllowedAsLiteral() ) );
        }
        if ( jspProperty.isTrimDirectiveWhitespaces() != null ) {
            pageInfo.setTrimDirectiveWhitespaces ( JspUtil.booleanValue ( jspProperty
                                                   .isTrimDirectiveWhitespaces() ) );
        }
        if ( jspProperty.getBuffer() != null ) {
            pageInfo.setBufferValue ( jspProperty.getBuffer(), null,
                                      errDispatcher );
        }
        if ( jspProperty.isErrorOnUndeclaredNamespace() != null ) {
            pageInfo.setErrorOnUndeclaredNamespace (
                JspUtil.booleanValue (
                    jspProperty.isErrorOnUndeclaredNamespace() ) );
        }
        if ( ctxt.isTagFile() ) {
            try {
                double libraryVersion = Double.parseDouble ( ctxt.getTagInfo()
                                        .getTagLibrary().getRequiredVersion() );
                if ( libraryVersion < 2.0 ) {
                    pageInfo.setIsELIgnored ( "true", null, errDispatcher, true );
                }
                if ( libraryVersion < 2.1 ) {
                    pageInfo.setDeferredSyntaxAllowedAsLiteral ( "true", null,
                            errDispatcher, true );
                }
            } catch ( NumberFormatException ex ) {
                errDispatcher.jspError ( ex );
            }
        }
        ctxt.checkOutputDir();
        String javaFileName = ctxt.getServletJavaFileName();
        try {
            ParserController parserCtl = new ParserController ( ctxt, this );
            Node.Nodes directives =
                parserCtl.parseDirectives ( ctxt.getJspFile() );
            Validator.validateDirectives ( this, directives );
            pageNodes = parserCtl.parse ( ctxt.getJspFile() );
            if ( pageInfo.getContentType() == null &&
                    jspProperty.getDefaultContentType() != null ) {
                pageInfo.setContentType ( jspProperty.getDefaultContentType() );
            }
            if ( ctxt.isPrototypeMode() ) {
                try ( ServletWriter writer = setupContextWriter ( javaFileName ) ) {
                    Generator.generate ( writer, this, pageNodes );
                    return null;
                }
            }
            Validator.validateExDirectives ( this, pageNodes );
            if ( log.isDebugEnabled() ) {
                t2 = System.currentTimeMillis();
            }
            Collector.collect ( this, pageNodes );
            tfp = new TagFileProcessor();
            tfp.loadTagFiles ( this, pageNodes );
            if ( log.isDebugEnabled() ) {
                t3 = System.currentTimeMillis();
            }
            ScriptingVariabler.set ( pageNodes, errDispatcher );
            TagPluginManager tagPluginManager = options.getTagPluginManager();
            tagPluginManager.apply ( pageNodes, errDispatcher, pageInfo );
            TextOptimizer.concatenate ( this, pageNodes );
            ELFunctionMapper.map ( pageNodes );
            try ( ServletWriter writer = setupContextWriter ( javaFileName ) ) {
                Generator.generate ( writer, this, pageNodes );
            }
            ctxt.setWriter ( null );
            if ( log.isDebugEnabled() ) {
                t4 = System.currentTimeMillis();
                log.debug ( "Generated " + javaFileName + " total=" + ( t4 - t1 )
                            + " generate=" + ( t4 - t3 ) + " validate=" + ( t2 - t1 ) );
            }
        } catch ( Exception e ) {
            File file = new File ( javaFileName );
            if ( file.exists() ) {
                if ( !file.delete() ) {
                    log.warn ( Localizer.getMessage (
                                   "jsp.warning.compiler.javafile.delete.fail",
                                   file.getAbsolutePath() ) );
                }
            }
            throw e;
        }
        if ( !options.isSmapSuppressed() ) {
            smapStr = SmapUtil.generateSmap ( ctxt, pageNodes );
        }
        tfp.removeProtoTypeFiles ( ctxt.getClassFileName() );
        return smapStr;
    }
    private ServletWriter setupContextWriter ( String javaFileName )
    throws FileNotFoundException, JasperException {
        ServletWriter writer;
        String javaEncoding = ctxt.getOptions().getJavaEncoding();
        OutputStreamWriter osw = null;
        try {
            osw = new OutputStreamWriter (
                new FileOutputStream ( javaFileName ), javaEncoding );
        } catch ( UnsupportedEncodingException ex ) {
            errDispatcher.jspError ( "jsp.error.needAlternateJavaEncoding",
                                     javaEncoding );
        }
        writer = new ServletWriter ( new PrintWriter ( osw ) );
        ctxt.setWriter ( writer );
        return writer;
    }
    protected abstract void generateClass ( String[] smap )
    throws FileNotFoundException, JasperException, Exception;
    public void compile() throws FileNotFoundException, JasperException,
        Exception {
        compile ( true );
    }
    public void compile ( boolean compileClass ) throws FileNotFoundException,
        JasperException, Exception {
        compile ( compileClass, false );
    }
    public void compile ( boolean compileClass, boolean jspcMode )
    throws FileNotFoundException, JasperException, Exception {
        if ( errDispatcher == null ) {
            this.errDispatcher = new ErrorDispatcher ( jspcMode );
        }
        try {
            String[] smap = generateJava();
            File javaFile = new File ( ctxt.getServletJavaFileName() );
            Long jspLastModified = ctxt.getLastModified ( ctxt.getJspFile() );
            javaFile.setLastModified ( jspLastModified.longValue() );
            if ( compileClass ) {
                generateClass ( smap );
                File targetFile = new File ( ctxt.getClassFileName() );
                if ( targetFile.exists() ) {
                    targetFile.setLastModified ( jspLastModified.longValue() );
                    if ( jsw != null ) {
                        jsw.setServletClassLastModifiedTime (
                            jspLastModified.longValue() );
                    }
                }
            }
        } finally {
            if ( tfp != null && ctxt.isPrototypeMode() ) {
                tfp.removeProtoTypeFiles ( null );
            }
            tfp = null;
            errDispatcher = null;
            pageInfo = null;
            if ( !this.options.getDevelopment() ) {
                pageNodes = null;
            }
            if ( ctxt.getWriter() != null ) {
                ctxt.getWriter().close();
                ctxt.setWriter ( null );
            }
        }
    }
    public boolean isOutDated() {
        return isOutDated ( true );
    }
    public boolean isOutDated ( boolean checkClass ) {
        if ( jsw != null
                && ( ctxt.getOptions().getModificationTestInterval() > 0 ) ) {
            if ( jsw.getLastModificationTest()
                    + ( ctxt.getOptions().getModificationTestInterval() * 1000 ) > System
                    .currentTimeMillis() ) {
                return false;
            }
            jsw.setLastModificationTest ( System.currentTimeMillis() );
        }
        File targetFile;
        if ( checkClass ) {
            targetFile = new File ( ctxt.getClassFileName() );
        } else {
            targetFile = new File ( ctxt.getServletJavaFileName() );
        }
        if ( !targetFile.exists() ) {
            return true;
        }
        long targetLastModified = targetFile.lastModified();
        if ( checkClass && jsw != null ) {
            jsw.setServletClassLastModifiedTime ( targetLastModified );
        }
        Long jspRealLastModified = ctxt.getLastModified ( ctxt.getJspFile() );
        if ( jspRealLastModified.longValue() < 0 ) {
            return true;
        }
        if ( targetLastModified != jspRealLastModified.longValue() ) {
            if ( log.isDebugEnabled() ) {
                log.debug ( "Compiler: outdated: " + targetFile + " "
                            + targetLastModified );
            }
            return true;
        }
        if ( jsw == null ) {
            return false;
        }
        Map<String, Long> depends = jsw.getDependants();
        if ( depends == null ) {
            return false;
        }
        Iterator<Entry<String, Long>> it = depends.entrySet().iterator();
        while ( it.hasNext() ) {
            Entry<String, Long> include = it.next();
            try {
                String key = include.getKey();
                URL includeUrl;
                long includeLastModified = 0;
                if ( key.startsWith ( "jar:jar:" ) ) {
                    int entryStart = key.lastIndexOf ( "!/" );
                    String entry = key.substring ( entryStart + 2 );
                    try ( Jar jar = JarFactory.newInstance ( new URL ( key.substring ( 4, entryStart ) ) ) ) {
                        includeLastModified = jar.getLastModified ( entry );
                    }
                } else {
                    if ( key.startsWith ( "jar:" ) || key.startsWith ( "file:" ) ) {
                        includeUrl = new URL ( key );
                    } else {
                        includeUrl = ctxt.getResource ( include.getKey() );
                    }
                    if ( includeUrl == null ) {
                        return true;
                    }
                    URLConnection iuc = includeUrl.openConnection();
                    if ( iuc instanceof JarURLConnection ) {
                        includeLastModified =
                            ( ( JarURLConnection ) iuc ).getJarEntry().getTime();
                    } else {
                        includeLastModified = iuc.getLastModified();
                    }
                    iuc.getInputStream().close();
                }
                if ( includeLastModified != include.getValue().longValue() ) {
                    return true;
                }
            } catch ( Exception e ) {
                if ( log.isDebugEnabled() )
                    log.debug ( "Problem accessing resource. Treat as outdated.",
                                e );
                return true;
            }
        }
        return false;
    }
    public ErrorDispatcher getErrorDispatcher() {
        return errDispatcher;
    }
    public PageInfo getPageInfo() {
        return pageInfo;
    }
    public JspCompilationContext getCompilationContext() {
        return ctxt;
    }
    public void removeGeneratedFiles() {
        removeGeneratedClassFiles();
        try {
            File javaFile = new File ( ctxt.getServletJavaFileName() );
            if ( log.isDebugEnabled() ) {
                log.debug ( "Deleting " + javaFile );
            }
            if ( javaFile.exists() ) {
                if ( !javaFile.delete() ) {
                    log.warn ( Localizer.getMessage (
                                   "jsp.warning.compiler.javafile.delete.fail",
                                   javaFile.getAbsolutePath() ) );
                }
            }
        } catch ( Exception e ) {
            log.warn ( Localizer.getMessage ( "jsp.warning.compiler.classfile.delete.fail.unknown" ),
                       e );
        }
    }
    public void removeGeneratedClassFiles() {
        try {
            File classFile = new File ( ctxt.getClassFileName() );
            if ( log.isDebugEnabled() ) {
                log.debug ( "Deleting " + classFile );
            }
            if ( classFile.exists() ) {
                if ( !classFile.delete() ) {
                    log.warn ( Localizer.getMessage (
                                   "jsp.warning.compiler.classfile.delete.fail",
                                   classFile.getAbsolutePath() ) );
                }
            }
        } catch ( Exception e ) {
            log.warn ( Localizer.getMessage ( "jsp.warning.compiler.classfile.delete.fail.unknown" ),
                       e );
        }
    }
}
