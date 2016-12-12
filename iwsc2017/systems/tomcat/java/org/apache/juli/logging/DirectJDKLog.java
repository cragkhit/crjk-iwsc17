package org.apache.juli.logging;
import java.util.logging.ConsoleHandler;
import java.util.logging.Formatter;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.Logger;
class DirectJDKLog implements Log {
    public final Logger logger;
    private static final String SIMPLE_FMT = "java.util.logging.SimpleFormatter";
    private static final String SIMPLE_CFG = "org.apache.juli.JdkLoggerConfig";
    private static final String FORMATTER = "org.apache.juli.formatter";
    static {
        if ( System.getProperty ( "java.util.logging.config.class" ) == null  &&
                System.getProperty ( "java.util.logging.config.file" ) == null ) {
            try {
                Class.forName ( SIMPLE_CFG ).newInstance();
            } catch ( Throwable t ) {
            }
            try {
                Formatter fmt = ( Formatter ) Class.forName ( System.getProperty ( FORMATTER, SIMPLE_FMT ) ).newInstance();
                Logger root = Logger.getLogger ( "" );
                for ( Handler handler : root.getHandlers() ) {
                    if ( handler instanceof  ConsoleHandler ) {
                        handler.setFormatter ( fmt );
                    }
                }
            } catch ( Throwable t ) {
            }
        }
    }
    public DirectJDKLog ( String name ) {
        logger = Logger.getLogger ( name );
    }
    @Override
    public final boolean isErrorEnabled() {
        return logger.isLoggable ( Level.SEVERE );
    }
    @Override
    public final boolean isWarnEnabled() {
        return logger.isLoggable ( Level.WARNING );
    }
    @Override
    public final boolean isInfoEnabled() {
        return logger.isLoggable ( Level.INFO );
    }
    @Override
    public final boolean isDebugEnabled() {
        return logger.isLoggable ( Level.FINE );
    }
    @Override
    public final boolean isFatalEnabled() {
        return logger.isLoggable ( Level.SEVERE );
    }
    @Override
    public final boolean isTraceEnabled() {
        return logger.isLoggable ( Level.FINER );
    }
    @Override
    public final void debug ( Object message ) {
        log ( Level.FINE, String.valueOf ( message ), null );
    }
    @Override
    public final void debug ( Object message, Throwable t ) {
        log ( Level.FINE, String.valueOf ( message ), t );
    }
    @Override
    public final void trace ( Object message ) {
        log ( Level.FINER, String.valueOf ( message ), null );
    }
    @Override
    public final void trace ( Object message, Throwable t ) {
        log ( Level.FINER, String.valueOf ( message ), t );
    }
    @Override
    public final void info ( Object message ) {
        log ( Level.INFO, String.valueOf ( message ), null );
    }
    @Override
    public final void info ( Object message, Throwable t ) {
        log ( Level.INFO, String.valueOf ( message ), t );
    }
    @Override
    public final void warn ( Object message ) {
        log ( Level.WARNING, String.valueOf ( message ), null );
    }
    @Override
    public final void warn ( Object message, Throwable t ) {
        log ( Level.WARNING, String.valueOf ( message ), t );
    }
    @Override
    public final void error ( Object message ) {
        log ( Level.SEVERE, String.valueOf ( message ), null );
    }
    @Override
    public final void error ( Object message, Throwable t ) {
        log ( Level.SEVERE, String.valueOf ( message ), t );
    }
    @Override
    public final void fatal ( Object message ) {
        log ( Level.SEVERE, String.valueOf ( message ), null );
    }
    @Override
    public final void fatal ( Object message, Throwable t ) {
        log ( Level.SEVERE, String.valueOf ( message ), t );
    }
    private void log ( Level level, String msg, Throwable ex ) {
        if ( logger.isLoggable ( level ) ) {
            Throwable dummyException = new Throwable();
            StackTraceElement locations[] = dummyException.getStackTrace();
            String cname = "unknown";
            String method = "unknown";
            if ( locations != null && locations.length > 2 ) {
                StackTraceElement caller = locations[2];
                cname = caller.getClassName();
                method = caller.getMethodName();
            }
            if ( ex == null ) {
                logger.logp ( level, cname, method, msg );
            } else {
                logger.logp ( level, cname, method, msg, ex );
            }
        }
    }
    static Log getInstance ( String name ) {
        return new DirectJDKLog ( name );
    }
}
