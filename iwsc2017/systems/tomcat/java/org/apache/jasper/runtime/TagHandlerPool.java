package org.apache.jasper.runtime;
import javax.servlet.ServletConfig;
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.tagext.Tag;
import org.apache.jasper.Constants;
import org.apache.juli.logging.Log;
import org.apache.juli.logging.LogFactory;
import org.apache.tomcat.InstanceManager;
public class TagHandlerPool {
    private Tag[] handlers;
    public static final String OPTION_TAGPOOL = "tagpoolClassName";
    public static final String OPTION_MAXSIZE = "tagpoolMaxSize";
    private static final Log log = LogFactory.getLog ( TagHandlerPool.class );
    private int current;
    protected InstanceManager instanceManager = null;
    public static TagHandlerPool getTagHandlerPool ( ServletConfig config ) {
        TagHandlerPool result = null;
        String tpClassName = getOption ( config, OPTION_TAGPOOL, null );
        if ( tpClassName != null ) {
            try {
                Class<?> c = Class.forName ( tpClassName );
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
    protected void init ( ServletConfig config ) {
        int maxSize = -1;
        String maxSizeS = getOption ( config, OPTION_MAXSIZE, null );
        if ( maxSizeS != null ) {
            try {
                maxSize = Integer.parseInt ( maxSizeS );
            } catch ( Exception ex ) {
                maxSize = -1;
            }
        }
        if ( maxSize < 0 ) {
            maxSize = Constants.MAX_POOL_SIZE;
        }
        this.handlers = new Tag[maxSize];
        this.current = -1;
        instanceManager = InstanceManagerFactory.getInstanceManager ( config );
    }
    public TagHandlerPool() {
    }
    public Tag get ( Class<? extends Tag> handlerClass ) throws JspException {
        Tag handler;
        synchronized ( this ) {
            if ( current >= 0 ) {
                handler = handlers[current--];
                return handler;
            }
        }
        try {
            if ( Constants.USE_INSTANCE_MANAGER_FOR_TAGS ) {
                return ( Tag ) instanceManager.newInstance (
                           handlerClass.getName(), handlerClass.getClassLoader() );
            } else {
                Tag instance = handlerClass.newInstance();
                instanceManager.newInstance ( instance );
                return instance;
            }
        } catch ( Exception e ) {
            Throwable t = ExceptionUtils.unwrapInvocationTargetException ( e );
            ExceptionUtils.handleThrowable ( t );
            throw new JspException ( e.getMessage(), t );
        }
    }
    public void reuse ( Tag handler ) {
        synchronized ( this ) {
            if ( current < ( handlers.length - 1 ) ) {
                handlers[++current] = handler;
                return;
            }
        }
        doRelease ( handler );
    }
    public synchronized void release() {
        for ( int i = current; i >= 0; i-- ) {
            doRelease ( handlers[i] );
        }
    }
    private void doRelease ( Tag handler ) {
        try {
            handler.release();
        } catch ( Throwable t ) {
            ExceptionUtils.handleThrowable ( t );
            log.warn ( "Error processing release on tag instance of "
                       + handler.getClass().getName(), t );
        }
        try {
            instanceManager.destroyInstance ( handler );
        } catch ( Exception e ) {
            Throwable t = ExceptionUtils.unwrapInvocationTargetException ( e );
            ExceptionUtils.handleThrowable ( t );
            log.warn ( "Error processing preDestroy on tag instance of "
                       + handler.getClass().getName(), t );
        }
    }
    protected static String getOption ( ServletConfig config, String name,
                                        String defaultV ) {
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
}
