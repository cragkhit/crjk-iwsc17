package org.apache.jasper.runtime;
import javax.el.CompositeELResolver;
import org.apache.jasper.el.JasperELResolver;
import javax.el.ELContext;
import javax.el.ELContextEvent;
import java.security.AccessController;
import java.security.PrivilegedAction;
import org.apache.jasper.Constants;
import org.apache.jasper.el.ELContextImpl;
import javax.servlet.jsp.JspContext;
import javax.servlet.ServletContext;
import java.util.ArrayList;
import javax.el.ELResolver;
import javax.el.ELContextListener;
import java.util.List;
import javax.el.ExpressionFactory;
import javax.servlet.jsp.JspApplicationContext;
public class JspApplicationContextImpl implements JspApplicationContext {
    private static final String KEY;
    private final ExpressionFactory expressionFactory;
    private final List<ELContextListener> contextListeners;
    private final List<ELResolver> resolvers;
    private boolean instantiated;
    private ELResolver resolver;
    public JspApplicationContextImpl() {
        this.expressionFactory = ExpressionFactory.newInstance();
        this.contextListeners = new ArrayList<ELContextListener>();
        this.resolvers = new ArrayList<ELResolver>();
        this.instantiated = false;
    }
    public void addELContextListener ( final ELContextListener listener ) {
        if ( listener == null ) {
            throw new IllegalArgumentException ( "ELConextListener was null" );
        }
        this.contextListeners.add ( listener );
    }
    public static JspApplicationContextImpl getInstance ( final ServletContext context ) {
        if ( context == null ) {
            throw new IllegalArgumentException ( "ServletContext was null" );
        }
        JspApplicationContextImpl impl = ( JspApplicationContextImpl ) context.getAttribute ( JspApplicationContextImpl.KEY );
        if ( impl == null ) {
            impl = new JspApplicationContextImpl();
            context.setAttribute ( JspApplicationContextImpl.KEY, ( Object ) impl );
        }
        return impl;
    }
    public ELContextImpl createELContext ( final JspContext context ) {
        if ( context == null ) {
            throw new IllegalArgumentException ( "JspContext was null" );
        }
        final ELResolver r = this.createELResolver();
        ELContextImpl ctx;
        if ( Constants.IS_SECURITY_ENABLED ) {
            ctx = AccessController.doPrivileged ( ( PrivilegedAction<ELContextImpl> ) new PrivilegedAction<ELContextImpl>() {
                @Override
                public ELContextImpl run() {
                    return new ELContextImpl ( r );
                }
            } );
        } else {
            ctx = new ELContextImpl ( r );
        }
        ctx.putContext ( ( Class ) JspContext.class, ( Object ) context );
        final ELContextEvent event = new ELContextEvent ( ( ELContext ) ctx );
        for ( int i = 0; i < this.contextListeners.size(); ++i ) {
            this.contextListeners.get ( i ).contextCreated ( event );
        }
        return ctx;
    }
    private ELResolver createELResolver() {
        this.instantiated = true;
        if ( this.resolver == null ) {
            final CompositeELResolver r = new JasperELResolver ( this.resolvers, this.expressionFactory.getStreamELResolver() );
            this.resolver = ( ELResolver ) r;
        }
        return this.resolver;
    }
    public void addELResolver ( final ELResolver resolver ) throws IllegalStateException {
        if ( resolver == null ) {
            throw new IllegalArgumentException ( "ELResolver was null" );
        }
        if ( this.instantiated ) {
            throw new IllegalStateException ( "cannot call addELResolver after the first request has been made" );
        }
        this.resolvers.add ( resolver );
    }
    public ExpressionFactory getExpressionFactory() {
        return this.expressionFactory;
    }
    static {
        KEY = JspApplicationContextImpl.class.getName();
    }
}
