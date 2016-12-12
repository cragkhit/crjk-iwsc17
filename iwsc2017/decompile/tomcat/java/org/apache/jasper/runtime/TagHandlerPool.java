package org.apache.jasper.runtime;
import org.apache.juli.logging.LogFactory;
import javax.servlet.jsp.JspException;
import org.apache.jasper.Constants;
import javax.servlet.ServletConfig;
import org.apache.tomcat.InstanceManager;
import org.apache.juli.logging.Log;
import javax.servlet.jsp.tagext.Tag;
public class TagHandlerPool {
    private Tag[] handlers;
    public static final String OPTION_TAGPOOL = "tagpoolClassName";
    public static final String OPTION_MAXSIZE = "tagpoolMaxSize";
    private static final Log log;
    private int current;
    protected InstanceManager instanceManager;
    public static TagHandlerPool getTagHandlerPool ( final ServletConfig config ) {
        TagHandlerPool result = null;
        final String tpClassName = getOption ( config, "tagpoolClassName", null );
        if ( tpClassName != null ) {
            try {
                final Class<?> c = Class.forName ( tpClassName );
                result = ( TagHandlerPool ) c.newInstance();
            } catch ( Exception e ) {
                e.printStackTrace();
                result = null;
            }
        }
        if ( result == null ) {
            result = new TagHandlerPool();
        }
        result.init ( config );
        return result;
    }
    protected void init ( final ServletConfig config ) {
        int maxSize = -1;
        final String maxSizeS = getOption ( config, "tagpoolMaxSize", null );
        if ( maxSizeS != null ) {
            try {
                maxSize = Integer.parseInt ( maxSizeS );
            } catch ( Exception ex ) {
                maxSize = -1;
            }
        }
        if ( maxSize < 0 ) {
            maxSize = 5;
        }
        this.handlers = new Tag[maxSize];
        this.current = -1;
        this.instanceManager = InstanceManagerFactory.getInstanceManager ( config );
    }
    public TagHandlerPool() {
        this.instanceManager = null;
    }
    public Tag get ( final Class<? extends Tag> handlerClass ) throws JspException {
        synchronized ( this ) {
            if ( this.current >= 0 ) {
                final Tag handler = this.handlers[this.current--];
                return handler;
            }
        }
        try {
            if ( Constants.USE_INSTANCE_MANAGER_FOR_TAGS ) {
                return ( Tag ) this.instanceManager.newInstance ( handlerClass.getName(), handlerClass.getClassLoader() );
            }
            final Tag instance = ( Tag ) handlerClass.newInstance();
            this.instanceManager.newInstance ( instance );
            return instance;
        } catch ( Exception e ) {
            final Throwable t = ExceptionUtils.unwrapInvocationTargetException ( e );
            ExceptionUtils.handleThrowable ( t );
            throw new JspException ( e.getMessage(), t );
        }
    }
    public void reuse ( final Tag handler ) {
        synchronized ( this ) {
            if ( this.current < this.handlers.length - 1 ) {
                this.handlers[++this.current] = handler;
                return;
            }
        }
        this.doRelease ( handler );
    }
    public synchronized void release() {
        for ( int i = this.current; i >= 0; --i ) {
            this.doRelease ( this.handlers[i] );
        }
    }
    private void doRelease ( final Tag handler ) {
        try {
            handler.release();
        } catch ( Throwable t ) {
            ExceptionUtils.handleThrowable ( t );
            TagHandlerPool.log.warn ( "Error processing release on tag instance of " + handler.getClass().getName(), t );
        }
        try {
            this.instanceManager.destroyInstance ( handler );
        } catch ( Exception e ) {
            final Throwable t2 = ExceptionUtils.unwrapInvocationTargetException ( e );
            ExceptionUtils.handleThrowable ( t2 );
            TagHandlerPool.log.warn ( "Error processing preDestroy on tag instance of " + handler.getClass().getName(), t2 );
        }
    }
    protected static String getOption ( final ServletConfig config, final String name, final String defaultV ) {
        if ( config == null ) {
            return defaultV;
        }
        String value = config.getInitParameter ( name );
        if ( value != null ) {
            return value;
        }
        if ( config.getServletContext() == null ) {
            return defaultV;
        }
        value = config.getServletContext().getInitParameter ( name );
        if ( value != null ) {
            return value;
        }
        return defaultV;
    }
    static {
        log = LogFactory.getLog ( TagHandlerPool.class );
    }
}
