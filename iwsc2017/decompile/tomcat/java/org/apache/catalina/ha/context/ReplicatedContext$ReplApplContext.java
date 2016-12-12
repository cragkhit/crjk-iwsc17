package org.apache.catalina.ha.context;
import java.util.Set;
import java.util.Collections;
import java.util.Collection;
import java.util.HashSet;
import java.util.Enumeration;
import javax.servlet.ServletContext;
import java.util.concurrent.ConcurrentHashMap;
import org.apache.catalina.core.StandardContext;
import java.util.Map;
import org.apache.catalina.core.ApplicationContext;
protected static class ReplApplContext extends ApplicationContext {
    protected final Map<String, Object> tomcatAttributes;
    public ReplApplContext ( final ReplicatedContext context ) {
        super ( context );
        this.tomcatAttributes = new ConcurrentHashMap<String, Object>();
    }
    protected ReplicatedContext getParent() {
        return ( ReplicatedContext ) this.getContext();
    }
    @Override
    protected ServletContext getFacade() {
        return super.getFacade();
    }
    public Map<String, Object> getAttributeMap() {
        return this.attributes;
    }
    public void setAttributeMap ( final Map<String, Object> map ) {
        this.attributes = map;
    }
    @Override
    public void removeAttribute ( final String name ) {
        this.tomcatAttributes.remove ( name );
        super.removeAttribute ( name );
    }
    @Override
    public void setAttribute ( final String name, final Object value ) {
        if ( name == null ) {
            throw new IllegalArgumentException ( ReplicatedContext.access$000().getString ( "applicationContext.setAttribute.namenull" ) );
        }
        if ( value == null ) {
            this.removeAttribute ( name );
            return;
        }
        if ( !this.getParent().getState().isAvailable() || "org.apache.jasper.runtime.JspApplicationContextImpl".equals ( name ) ) {
            this.tomcatAttributes.put ( name, value );
        } else {
            super.setAttribute ( name, value );
        }
    }
    @Override
    public Object getAttribute ( final String name ) {
        final Object obj = this.tomcatAttributes.get ( name );
        if ( obj == null ) {
            return super.getAttribute ( name );
        }
        return obj;
    }
    @Override
    public Enumeration<String> getAttributeNames() {
        final Set<String> names = new HashSet<String>();
        names.addAll ( this.attributes.keySet() );
        return new MultiEnumeration<String> ( new Enumeration[] { super.getAttributeNames(), Collections.enumeration ( names ) } );
    }
}
