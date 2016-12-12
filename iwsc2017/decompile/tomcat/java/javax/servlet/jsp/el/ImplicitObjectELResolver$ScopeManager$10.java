package javax.servlet.jsp.el;
import java.util.Enumeration;
import javax.servlet.http.HttpSession;
import javax.servlet.http.HttpServletRequest;
class ImplicitObjectELResolver$ScopeManager$10 extends ScopeMap<Object> {
    @Override
    protected void setAttribute ( final String name, final Object value ) {
        ( ( HttpServletRequest ) ScopeManager.access$100 ( ScopeManager.this ).getRequest() ).getSession().setAttribute ( name, value );
    }
    @Override
    protected void removeAttribute ( final String name ) {
        final HttpSession session = ScopeManager.access$100 ( ScopeManager.this ).getSession();
        if ( session != null ) {
            session.removeAttribute ( name );
        }
    }
    @Override
    protected Enumeration<String> getAttributeNames() {
        final HttpSession session = ScopeManager.access$100 ( ScopeManager.this ).getSession();
        if ( session != null ) {
            return session.getAttributeNames();
        }
        return null;
    }
    @Override
    protected Object getAttribute ( final String name ) {
        final HttpSession session = ScopeManager.access$100 ( ScopeManager.this ).getSession();
        if ( session != null ) {
            return session.getAttribute ( name );
        }
        return null;
    }
}
