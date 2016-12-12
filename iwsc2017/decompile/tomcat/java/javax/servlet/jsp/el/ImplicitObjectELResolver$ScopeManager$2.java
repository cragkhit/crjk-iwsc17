package javax.servlet.jsp.el;
import java.util.Vector;
import javax.servlet.http.HttpServletRequest;
import java.util.Enumeration;
import javax.servlet.http.Cookie;
class ImplicitObjectELResolver$ScopeManager$2 extends ScopeMap<Cookie> {
    @Override
    protected Enumeration<String> getAttributeNames() {
        final Cookie[] c = ( ( HttpServletRequest ) ScopeManager.access$100 ( ScopeManager.this ).getRequest() ).getCookies();
        if ( c != null ) {
            final Vector<String> v = new Vector<String>();
            for ( int i = 0; i < c.length; ++i ) {
                v.add ( c[i].getName() );
            }
            return v.elements();
        }
        return null;
    }
    @Override
    protected Cookie getAttribute ( final String name ) {
        final Cookie[] c = ( ( HttpServletRequest ) ScopeManager.access$100 ( ScopeManager.this ).getRequest() ).getCookies();
        if ( c != null ) {
            for ( int i = 0; i < c.length; ++i ) {
                if ( name.equals ( c[i].getName() ) ) {
                    return c[i];
                }
            }
        }
        return null;
    }
}
