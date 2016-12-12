package javax.servlet.jsp.el;
import javax.servlet.http.HttpServletRequest;
import java.util.Enumeration;
class ImplicitObjectELResolver$ScopeManager$3 extends ScopeMap<String> {
    @Override
    protected Enumeration<String> getAttributeNames() {
        return ( ( HttpServletRequest ) ScopeManager.access$100 ( ScopeManager.this ).getRequest() ).getHeaderNames();
    }
    @Override
    protected String getAttribute ( final String name ) {
        return ( ( HttpServletRequest ) ScopeManager.access$100 ( ScopeManager.this ).getRequest() ).getHeader ( name );
    }
}
