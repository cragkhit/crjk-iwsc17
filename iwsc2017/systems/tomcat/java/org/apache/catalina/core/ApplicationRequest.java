package org.apache.catalina.core;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletRequest;
import javax.servlet.ServletRequestWrapper;
class ApplicationRequest extends ServletRequestWrapper {
    protected static final String specials[] = {
        RequestDispatcher.INCLUDE_REQUEST_URI,
        RequestDispatcher.INCLUDE_CONTEXT_PATH,
        RequestDispatcher.INCLUDE_SERVLET_PATH,
        RequestDispatcher.INCLUDE_PATH_INFO,
        RequestDispatcher.INCLUDE_QUERY_STRING,
        RequestDispatcher.INCLUDE_MAPPING,
        RequestDispatcher.FORWARD_REQUEST_URI,
        RequestDispatcher.FORWARD_CONTEXT_PATH,
        RequestDispatcher.FORWARD_SERVLET_PATH,
        RequestDispatcher.FORWARD_PATH_INFO,
        RequestDispatcher.FORWARD_QUERY_STRING,
        RequestDispatcher.FORWARD_MAPPING
    };
    public ApplicationRequest ( ServletRequest request ) {
        super ( request );
        setRequest ( request );
    }
    protected final HashMap<String, Object> attributes = new HashMap<>();
    @Override
    public Object getAttribute ( String name ) {
        synchronized ( attributes ) {
            return ( attributes.get ( name ) );
        }
    }
    @Override
    public Enumeration<String> getAttributeNames() {
        synchronized ( attributes ) {
            return Collections.enumeration ( attributes.keySet() );
        }
    }
    @Override
    public void removeAttribute ( String name ) {
        synchronized ( attributes ) {
            attributes.remove ( name );
            if ( !isSpecial ( name ) ) {
                getRequest().removeAttribute ( name );
            }
        }
    }
    @Override
    public void setAttribute ( String name, Object value ) {
        synchronized ( attributes ) {
            attributes.put ( name, value );
            if ( !isSpecial ( name ) ) {
                getRequest().setAttribute ( name, value );
            }
        }
    }
    @Override
    public void setRequest ( ServletRequest request ) {
        super.setRequest ( request );
        synchronized ( attributes ) {
            attributes.clear();
            Enumeration<String> names = request.getAttributeNames();
            while ( names.hasMoreElements() ) {
                String name = names.nextElement();
                Object value = request.getAttribute ( name );
                attributes.put ( name, value );
            }
        }
    }
    protected boolean isSpecial ( String name ) {
        for ( int i = 0; i < specials.length; i++ ) {
            if ( specials[i].equals ( name ) ) {
                return true;
            }
        }
        return false;
    }
}
