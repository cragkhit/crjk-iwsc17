package javax.servlet.jsp.el;
import java.util.List;
import java.util.ArrayList;
import javax.servlet.http.HttpServletRequest;
import java.util.Enumeration;
class ImplicitObjectELResolver$ScopeManager$4 extends ScopeMap<String[]> {
    @Override
    protected Enumeration<String> getAttributeNames() {
        return ( ( HttpServletRequest ) ScopeManager.access$100 ( ScopeManager.this ).getRequest() ).getHeaderNames();
    }
    @Override
    protected String[] getAttribute ( final String name ) {
        final Enumeration<String> e = ( ( HttpServletRequest ) ScopeManager.access$100 ( ScopeManager.this ).getRequest() ).getHeaders ( name );
        if ( e != null ) {
            final List<String> list = new ArrayList<String>();
            while ( e.hasMoreElements() ) {
                list.add ( e.nextElement() );
            }
            return list.toArray ( new String[list.size()] );
        }
        return null;
    }
}
