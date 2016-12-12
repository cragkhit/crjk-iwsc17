package org.apache.juli.logging;
import java.util.logging.Handler;
import java.util.logging.ConsoleHandler;
import java.util.logging.Formatter;
import java.util.logging.Level;
import java.util.logging.Logger;
class DirectJDKLog implements Log {
    public final Logger logger;
    private static final String SIMPLE_FMT = "java.util.logging.SimpleFormatter";
    private static final String SIMPLE_CFG = "org.apache.juli.JdkLoggerConfig";
    private static final String FORMATTER = "org.apache.juli.formatter";
    public DirectJDKLog ( final String name ) {
        this.logger = Logger.getLogger ( name );
    }
    @Override
    public final boolean isErrorEnabled() {
        return this.logger.isLoggable ( Level.SEVERE );
    }
    @Override
    public final boolean isWarnEnabled() {
        return this.logger.isLoggable ( Level.WARNING );
    }
    @Override
    public final boolean isInfoEnabled() {
        return this.logger.isLoggable ( Level.INFO );
    }
    @Override
    public final boolean isDebugEnabled() {
        return this.logger.isLoggable ( Level.FINE );
    }
    @Override
    public final boolean isFatalEnabled() {
        return this.logger.isLoggable ( Level.SEVERE );
    }
    @Override
    public final boolean isTraceEnabled() {
        return this.logger.isLoggable ( Level.FINER );
    }
    @Override
    public final void debug ( final Object message ) {
        this.log ( Level.FINE, String.valueOf ( message ), null );
    }
    @Override
    public final void debug ( final Object message, final Throwable t ) {
        this.log ( Level.FINE, String.valueOf ( message ), t );
    }
    @Override
    public final void trace ( final Object message ) {
        this.log ( Level.FINER, String.valueOf ( message ), null );
    }
    @Override
    public final void trace ( final Object message, final Throwable t ) {
        this.log ( Level.FINER, String.valueOf ( message ), t );
    }
    @Override
    public final void info ( final Object message ) {
        this.log ( Level.INFO, String.valueOf ( message ), null );
    }
    @Override
    public final void info ( final Object message, final Throwable t ) {
        this.log ( Level.INFO, String.valueOf ( message ), t );
    }
    @Override
    public final void warn ( final Object message ) {
        this.log ( Level.WARNING, String.valueOf ( message ), null );
    }
    @Override
    public final void warn ( final Object message, final Throwable t ) {
        this.log ( Level.WARNING, String.valueOf ( message ), t );
    }
    @Override
    public final void error ( final Object message ) {
        this.log ( Level.SEVERE, String.valueOf ( message ), null );
    }
    @Override
    public final void error ( final Object message, final Throwable t ) {
        this.log ( Level.SEVERE, String.valueOf ( message ), t );
    }
    @Override
    public final void fatal ( final Object message ) {
        this.log ( Level.SEVERE, String.valueOf ( message ), null );
    }
    @Override
    public final void fatal ( final Object message, final Throwable t ) {
        this.log ( Level.SEVERE, String.valueOf ( message ), t );
    }
    private void log ( final Level level, final String msg, final Throwable ex ) {
        if ( this.logger.isLoggable ( level ) ) {
            final Throwable dummyException = new Throwable();
            final StackTraceElement[] locations = dummyException.getStackTrace();
            String cname = "unknown";
            String method = "unknown";
            if ( locations != null && locations.length > 2 ) {
                final StackTraceElement caller = locations[2];
                cname = caller.getClassName();
                method = caller.getMethodName();
            }
            if ( ex == null ) {
                this.logger.logp ( level, cname, method, msg );
            } else {
                this.logger.logp ( level, cname, method, msg, ex );
            }
        }
    }
    static Log getInstance ( final String name ) {
        return new DirectJDKLog ( name );
    }
    static {
        if ( System.getProperty ( "java.util.logging.config.class" ) == null && System.getProperty ( "java.util.logging.config.file" ) == null ) {
            try {
                Class.forName ( "org.apache.juli.JdkLoggerConfig" ).newInstance();
            } catch ( Throwable t ) {}
            try {
                final Formatter fmt = ( Formatter ) Class.forName ( System.getProperty ( "org.apache.juli.formatter", "java.util.logging.SimpleFormatter" ) ).newInstance();
                final Logger root = Logger.getLogger ( "" );
                for ( final Handler handler : root.getHandlers() ) {
                    if ( handler instanceof ConsoleHandler ) {
                        handler.setFormatter ( fmt );
                    }
                }
            } catch ( Throwable t2 ) {}
        }
    }
}
