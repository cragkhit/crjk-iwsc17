package org.apache.jasper.runtime;
import javax.servlet.jsp.JspApplicationContext;
import javax.servlet.ServletContext;
import java.io.IOException;
import javax.servlet.jsp.JspEngineInfo;
import java.security.PrivilegedAction;
import java.security.AccessController;
import org.apache.jasper.Constants;
import javax.servlet.jsp.PageContext;
import javax.servlet.ServletResponse;
import javax.servlet.ServletRequest;
import javax.servlet.Servlet;
import javax.servlet.jsp.JspFactory;
public class JspFactoryImpl extends JspFactory {
    private static final String SPEC_VERSION = "2.3";
    private static final boolean USE_POOL;
    private static final int POOL_SIZE;
    private final ThreadLocal<PageContextPool> localPool;
    public JspFactoryImpl() {
        this.localPool = new ThreadLocal<PageContextPool>();
    }
    public PageContext getPageContext ( final Servlet servlet, final ServletRequest request, final ServletResponse response, final String errorPageURL, final boolean needsSession, final int bufferSize, final boolean autoflush ) {
        if ( Constants.IS_SECURITY_ENABLED ) {
            final PrivilegedGetPageContext dp = new PrivilegedGetPageContext ( this, servlet, request, response, errorPageURL, needsSession, bufferSize, autoflush );
            return AccessController.doPrivileged ( ( PrivilegedAction<PageContext> ) dp );
        }
        return this.internalGetPageContext ( servlet, request, response, errorPageURL, needsSession, bufferSize, autoflush );
    }
    public void releasePageContext ( final PageContext pc ) {
        if ( pc == null ) {
            return;
        }
        if ( Constants.IS_SECURITY_ENABLED ) {
            final PrivilegedReleasePageContext dp = new PrivilegedReleasePageContext ( this, pc );
            AccessController.doPrivileged ( ( PrivilegedAction<Object> ) dp );
        } else {
            this.internalReleasePageContext ( pc );
        }
    }
    public JspEngineInfo getEngineInfo() {
        return new JspEngineInfo() {
            public String getSpecificationVersion() {
                return "2.3";
            }
        };
    }
    private PageContext internalGetPageContext ( final Servlet servlet, final ServletRequest request, final ServletResponse response, final String errorPageURL, final boolean needsSession, final int bufferSize, final boolean autoflush ) {
        PageContext pc;
        if ( JspFactoryImpl.USE_POOL ) {
            PageContextPool pool = this.localPool.get();
            if ( pool == null ) {
                pool = new PageContextPool();
                this.localPool.set ( pool );
            }
            pc = pool.get();
            if ( pc == null ) {
                pc = new PageContextImpl();
            }
        } else {
            pc = new PageContextImpl();
        }
        try {
            pc.initialize ( servlet, request, response, errorPageURL, needsSession, bufferSize, autoflush );
        } catch ( IOException ex ) {}
        return pc;
    }
    private void internalReleasePageContext ( final PageContext pc ) {
        pc.release();
        if ( JspFactoryImpl.USE_POOL && pc instanceof PageContextImpl ) {
            this.localPool.get().put ( pc );
        }
    }
    public JspApplicationContext getJspApplicationContext ( final ServletContext context ) {
        if ( Constants.IS_SECURITY_ENABLED ) {
            return AccessController.doPrivileged ( ( PrivilegedAction<JspApplicationContext> ) new PrivilegedAction<JspApplicationContext>() {
                @Override
                public JspApplicationContext run() {
                    return ( JspApplicationContext ) JspApplicationContextImpl.getInstance ( context );
                }
            } );
        }
        return ( JspApplicationContext ) JspApplicationContextImpl.getInstance ( context );
    }
    static {
        USE_POOL = Boolean.parseBoolean ( System.getProperty ( "org.apache.jasper.runtime.JspFactoryImpl.USE_POOL", "true" ) );
        POOL_SIZE = Integer.parseInt ( System.getProperty ( "org.apache.jasper.runtime.JspFactoryImpl.POOL_SIZE", "8" ) );
    }
    private static class PrivilegedGetPageContext implements PrivilegedAction<PageContext> {
        private JspFactoryImpl factory;
        private Servlet servlet;
        private ServletRequest request;
        private ServletResponse response;
        private String errorPageURL;
        private boolean needsSession;
        private int bufferSize;
        private boolean autoflush;
        PrivilegedGetPageContext ( final JspFactoryImpl factory, final Servlet servlet, final ServletRequest request, final ServletResponse response, final String errorPageURL, final boolean needsSession, final int bufferSize, final boolean autoflush ) {
            this.factory = factory;
            this.servlet = servlet;
            this.request = request;
            this.response = response;
            this.errorPageURL = errorPageURL;
            this.needsSession = needsSession;
            this.bufferSize = bufferSize;
            this.autoflush = autoflush;
        }
        @Override
        public PageContext run() {
            return this.factory.internalGetPageContext ( this.servlet, this.request, this.response, this.errorPageURL, this.needsSession, this.bufferSize, this.autoflush );
        }
    }
    private static class PrivilegedReleasePageContext implements PrivilegedAction<Void> {
        private JspFactoryImpl factory;
        private PageContext pageContext;
        PrivilegedReleasePageContext ( final JspFactoryImpl factory, final PageContext pageContext ) {
            this.factory = factory;
            this.pageContext = pageContext;
        }
        @Override
        public Void run() {
            this.factory.internalReleasePageContext ( this.pageContext );
            return null;
        }
    }
    private static final class PageContextPool {
        private final PageContext[] pool;
        private int current;
        public PageContextPool() {
            this.current = -1;
            this.pool = new PageContext[JspFactoryImpl.POOL_SIZE];
        }
        public void put ( final PageContext o ) {
            if ( this.current < JspFactoryImpl.POOL_SIZE - 1 ) {
                ++this.current;
                this.pool[this.current] = o;
            }
        }
        public PageContext get() {
            PageContext item = null;
            if ( this.current >= 0 ) {
                item = this.pool[this.current];
                --this.current;
            }
            return item;
        }
    }
}
