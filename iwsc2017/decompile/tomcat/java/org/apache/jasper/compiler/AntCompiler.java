package org.apache.jasper.compiler;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.io.IOException;
import java.io.OutputStream;
import java.io.ByteArrayOutputStream;
import org.apache.tools.ant.DefaultLogger;
import java.io.PrintStream;
import org.apache.jasper.JasperException;
import java.io.FileNotFoundException;
import org.apache.tools.ant.types.PatternSet;
import org.apache.tools.ant.BuildException;
import java.util.StringTokenizer;
import java.io.File;
import org.apache.tools.ant.types.Path;
import org.apache.tools.ant.taskdefs.Javac;
import org.apache.tools.ant.BuildListener;
import org.apache.juli.logging.LogFactory;
import org.apache.tools.ant.Project;
import org.apache.juli.logging.Log;
public class AntCompiler extends Compiler {
    private final Log log;
    protected static final Object javacLock;
    protected Project project;
    protected JasperAntLogger logger;
    public AntCompiler() {
        this.log = LogFactory.getLog ( AntCompiler.class );
        this.project = null;
    }
    protected Project getProject() {
        if ( this.project != null ) {
            return this.project;
        }
        this.project = new Project();
        ( this.logger = new JasperAntLogger() ).setOutputPrintStream ( System.out );
        this.logger.setErrorPrintStream ( System.err );
        this.logger.setMessageOutputLevel ( 2 );
        this.project.addBuildListener ( ( BuildListener ) this.logger );
        if ( System.getProperty ( "catalina.home" ) != null ) {
            this.project.setBasedir ( System.getProperty ( "catalina.home" ) );
        }
        if ( this.options.getCompiler() != null ) {
            if ( this.log.isDebugEnabled() ) {
                this.log.debug ( "Compiler " + this.options.getCompiler() );
            }
            this.project.setProperty ( "build.compiler", this.options.getCompiler() );
        }
        this.project.init();
        return this.project;
    }
    @Override
    protected void generateClass ( final String[] smap ) throws FileNotFoundException, JasperException, Exception {
        long t1 = 0L;
        if ( this.log.isDebugEnabled() ) {
            t1 = System.currentTimeMillis();
        }
        final String javaEncoding = this.ctxt.getOptions().getJavaEncoding();
        final String javaFileName = this.ctxt.getServletJavaFileName();
        final String classpath = this.ctxt.getClassPath();
        final StringBuilder errorReport = new StringBuilder();
        final StringBuilder info = new StringBuilder();
        info.append ( "Compile: javaFileName=" + javaFileName + "\n" );
        info.append ( "    classpath=" + classpath + "\n" );
        SystemLogHandler.setThread();
        this.getProject();
        final Javac javac = ( Javac ) this.project.createTask ( "javac" );
        final Path path = new Path ( this.project );
        path.setPath ( System.getProperty ( "java.class.path" ) );
        info.append ( "    cp=" + System.getProperty ( "java.class.path" ) + "\n" );
        final StringTokenizer tokenizer = new StringTokenizer ( classpath, File.pathSeparator );
        while ( tokenizer.hasMoreElements() ) {
            final String pathElement = tokenizer.nextToken();
            final File repository = new File ( pathElement );
            path.setLocation ( repository );
            info.append ( "    cp=" + repository + "\n" );
        }
        if ( this.log.isDebugEnabled() ) {
            this.log.debug ( "Using classpath: " + System.getProperty ( "java.class.path" ) + File.pathSeparator + classpath );
        }
        final Path srcPath = new Path ( this.project );
        srcPath.setLocation ( this.options.getScratchDir() );
        info.append ( "    work dir=" + this.options.getScratchDir() + "\n" );
        final String exts = System.getProperty ( "java.ext.dirs" );
        if ( exts != null ) {
            final Path extdirs = new Path ( this.project );
            extdirs.setPath ( exts );
            javac.setExtdirs ( extdirs );
            info.append ( "    extension dir=" + exts + "\n" );
        }
        if ( this.ctxt.getOptions().getFork() ) {
            final String endorsed = System.getProperty ( "java.endorsed.dirs" );
            if ( endorsed != null ) {
                final Javac.ImplementationSpecificArgument endorsedArg = javac.createCompilerArg();
                endorsedArg.setLine ( "-J-Djava.endorsed.dirs=" + this.quotePathList ( endorsed ) );
                info.append ( "    endorsed dir=" + this.quotePathList ( endorsed ) + "\n" );
            } else {
                info.append ( "    no endorsed dirs specified\n" );
            }
        }
        javac.setEncoding ( javaEncoding );
        javac.setClasspath ( path );
        javac.setDebug ( this.ctxt.getOptions().getClassDebugInfo() );
        javac.setSrcdir ( srcPath );
        javac.setTempdir ( this.options.getScratchDir() );
        javac.setOptimize ( !this.ctxt.getOptions().getClassDebugInfo() );
        javac.setFork ( this.ctxt.getOptions().getFork() );
        info.append ( "    srcDir=" + srcPath + "\n" );
        if ( this.options.getCompiler() != null ) {
            javac.setCompiler ( this.options.getCompiler() );
            info.append ( "    compiler=" + this.options.getCompiler() + "\n" );
        }
        if ( this.options.getCompilerTargetVM() != null ) {
            javac.setTarget ( this.options.getCompilerTargetVM() );
            info.append ( "   compilerTargetVM=" + this.options.getCompilerTargetVM() + "\n" );
        }
        if ( this.options.getCompilerSourceVM() != null ) {
            javac.setSource ( this.options.getCompilerSourceVM() );
            info.append ( "   compilerSourceVM=" + this.options.getCompilerSourceVM() + "\n" );
        }
        final PatternSet.NameEntry includes = javac.createInclude();
        includes.setName ( this.ctxt.getJavaPath() );
        info.append ( "    include=" + this.ctxt.getJavaPath() + "\n" );
        BuildException be = null;
        try {
            if ( this.ctxt.getOptions().getFork() ) {
                javac.execute();
            } else {
                synchronized ( AntCompiler.javacLock ) {
                    javac.execute();
                }
            }
        } catch ( BuildException e ) {
            be = e;
            this.log.error ( Localizer.getMessage ( "jsp.error.javac" ), ( Throwable ) e );
            this.log.error ( Localizer.getMessage ( "jsp.error.javac.env" ) + info.toString() );
        }
        errorReport.append ( this.logger.getReport() );
        final String errorCapture = SystemLogHandler.unsetThread();
        if ( errorCapture != null ) {
            errorReport.append ( System.lineSeparator() );
            errorReport.append ( errorCapture );
        }
        if ( !this.ctxt.keepGenerated() ) {
            final File javaFile = new File ( javaFileName );
            javaFile.delete();
        }
        if ( be != null ) {
            final String errorReportString = errorReport.toString();
            this.log.error ( Localizer.getMessage ( "jsp.error.compilation", javaFileName, errorReportString ) );
            final JavacErrorDetail[] javacErrors = ErrorDispatcher.parseJavacErrors ( errorReportString, javaFileName, this.pageNodes );
            if ( javacErrors != null ) {
                this.errDispatcher.javacError ( javacErrors );
            } else {
                this.errDispatcher.javacError ( errorReportString, ( Exception ) be );
            }
        }
        if ( this.log.isDebugEnabled() ) {
            final long t2 = System.currentTimeMillis();
            this.log.debug ( "Compiled " + this.ctxt.getServletJavaFileName() + " " + ( t2 - t1 ) + "ms" );
        }
        this.logger = null;
        this.project = null;
        if ( this.ctxt.isPrototypeMode() ) {
            return;
        }
        if ( !this.options.isSmapSuppressed() ) {
            SmapUtil.installSmap ( smap );
        }
    }
    private String quotePathList ( final String list ) {
        final StringBuilder result = new StringBuilder ( list.length() + 10 );
        final StringTokenizer st = new StringTokenizer ( list, File.pathSeparator );
        while ( st.hasMoreTokens() ) {
            final String token = st.nextToken();
            if ( token.indexOf ( 32 ) == -1 ) {
                result.append ( token );
            } else {
                result.append ( '\"' );
                result.append ( token );
                result.append ( '\"' );
            }
            if ( st.hasMoreTokens() ) {
                result.append ( File.pathSeparatorChar );
            }
        }
        return result.toString();
    }
    static {
        javacLock = new Object();
        System.setErr ( new SystemLogHandler ( System.err ) );
    }
    public static class JasperAntLogger extends DefaultLogger {
        protected final StringBuilder reportBuf;
        public JasperAntLogger() {
            this.reportBuf = new StringBuilder();
        }
        protected void printMessage ( final String message, final PrintStream stream, final int priority ) {
        }
        protected void log ( final String message ) {
            this.reportBuf.append ( message );
            this.reportBuf.append ( System.lineSeparator() );
        }
        protected String getReport() {
            final String report = this.reportBuf.toString();
            this.reportBuf.setLength ( 0 );
            return report;
        }
    }
    protected static class SystemLogHandler extends PrintStream {
        protected final PrintStream wrapped;
        protected static final ThreadLocal<PrintStream> streams;
        protected static final ThreadLocal<ByteArrayOutputStream> data;
        public SystemLogHandler ( final PrintStream wrapped ) {
            super ( wrapped );
            this.wrapped = wrapped;
        }
        public static void setThread() {
            final ByteArrayOutputStream baos = new ByteArrayOutputStream();
            SystemLogHandler.data.set ( baos );
            SystemLogHandler.streams.set ( new PrintStream ( baos ) );
        }
        public static String unsetThread() {
            final ByteArrayOutputStream baos = SystemLogHandler.data.get();
            if ( baos == null ) {
                return null;
            }
            SystemLogHandler.streams.set ( null );
            SystemLogHandler.data.set ( null );
            return baos.toString();
        }
        protected PrintStream findStream() {
            PrintStream ps = SystemLogHandler.streams.get();
            if ( ps == null ) {
                ps = this.wrapped;
            }
            return ps;
        }
        @Override
        public void flush() {
            this.findStream().flush();
        }
        @Override
        public void close() {
            this.findStream().close();
        }
        @Override
        public boolean checkError() {
            return this.findStream().checkError();
        }
        @Override
        protected void setError() {
        }
        @Override
        public void write ( final int b ) {
            this.findStream().write ( b );
        }
        @Override
        public void write ( final byte[] b ) throws IOException {
            this.findStream().write ( b );
        }
        @Override
        public void write ( final byte[] buf, final int off, final int len ) {
            this.findStream().write ( buf, off, len );
        }
        @Override
        public void print ( final boolean b ) {
            this.findStream().print ( b );
        }
        @Override
        public void print ( final char c ) {
            this.findStream().print ( c );
        }
        @Override
        public void print ( final int i ) {
            this.findStream().print ( i );
        }
        @Override
        public void print ( final long l ) {
            this.findStream().print ( l );
        }
        @Override
        public void print ( final float f ) {
            this.findStream().print ( f );
        }
        @Override
        public void print ( final double d ) {
            this.findStream().print ( d );
        }
        @Override
        public void print ( final char[] s ) {
            this.findStream().print ( s );
        }
        @Override
        public void print ( final String s ) {
            this.findStream().print ( s );
        }
        @Override
        public void print ( final Object obj ) {
            this.findStream().print ( obj );
        }
        @Override
        public void println() {
            this.findStream().println();
        }
        @Override
        public void println ( final boolean x ) {
            this.findStream().println ( x );
        }
        @Override
        public void println ( final char x ) {
            this.findStream().println ( x );
        }
        @Override
        public void println ( final int x ) {
            this.findStream().println ( x );
        }
        @Override
        public void println ( final long x ) {
            this.findStream().println ( x );
        }
        @Override
        public void println ( final float x ) {
            this.findStream().println ( x );
        }
        @Override
        public void println ( final double x ) {
            this.findStream().println ( x );
        }
        @Override
        public void println ( final char[] x ) {
            this.findStream().println ( x );
        }
        @Override
        public void println ( final String x ) {
            this.findStream().println ( x );
        }
        @Override
        public void println ( final Object x ) {
            this.findStream().println ( x );
        }
        static {
            streams = new ThreadLocal<PrintStream>();
            data = new ThreadLocal<ByteArrayOutputStream>();
        }
    }
}
