package org.apache.catalina.core;
import java.util.Collection;
import java.util.Collections;
import java.util.Enumeration;
import javax.servlet.ServletRequest;
import java.util.HashMap;
import javax.servlet.ServletRequestWrapper;
class ApplicationRequest extends ServletRequestWrapper {
    protected static final String[] specials;
    protected final HashMap<String, Object> attributes;
    public ApplicationRequest ( final ServletRequest request ) {
        super ( request );
        this.attributes = new HashMap<String, Object>();
        this.setRequest ( request );
    }
    public Object getAttribute ( final String name ) {
        synchronized ( this.attributes ) {
            return this.attributes.get ( name );
        }
    }
    public Enumeration<String> getAttributeNames() {
        synchronized ( this.attributes ) {
            return Collections.enumeration ( this.attributes.keySet() );
        }
    }
    public void removeAttribute ( final String name ) {
        synchronized ( this.attributes ) {
            this.attributes.remove ( name );
            if ( !this.isSpecial ( name ) ) {
                this.getRequest().removeAttribute ( name );
            }
        }
    }
    public void setAttribute ( final String name, final Object value ) {
        synchronized ( this.attributes ) {
            this.attributes.put ( name, value );
            if ( !this.isSpecial ( name ) ) {
                this.getRequest().setAttribute ( name, value );
            }
        }
    }
    public void setRequest ( final ServletRequest request ) {
        super.setRequest ( request );
        synchronized ( this.attributes ) {
            this.attributes.clear();
            final Enumeration<String> names = ( Enumeration<String> ) request.getAttributeNames();
            while ( names.hasMoreElements() ) {
                final String name = names.nextElement();
                final Object value = request.getAttribute ( name );
                this.attributes.put ( name, value );
            }
        }
    }
    protected boolean isSpecial ( final String name ) {
        for ( int i = 0; i < ApplicationRequest.specials.length; ++i ) {
            if ( ApplicationRequest.specials[i].equals ( name ) ) {
                return true;
            }
        }
        return false;
    }
    static {
        specials = new String[] { "javax.servlet.include.request_uri", "javax.servlet.include.context_path", "javax.servlet.include.servlet_path", "javax.servlet.include.path_info", "javax.servlet.include.query_string", "javax.servlet.include.mapping", "javax.servlet.forward.request_uri", "javax.servlet.forward.context_path", "javax.servlet.forward.servlet_path", "javax.servlet.forward.path_info", "javax.servlet.forward.query_string", "javax.servlet.forward.mapping" };
    }
}
