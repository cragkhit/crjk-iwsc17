package javax.servlet.jsp.el;
import java.util.Enumeration;
class ImplicitObjectELResolver$ScopeManager$1 extends ScopeMap<Object> {
    @Override
    protected void setAttribute ( final String name, final Object value ) {
        ScopeManager.access$100 ( ScopeManager.this ).getServletContext().setAttribute ( name, value );
    }
    @Override
    protected void removeAttribute ( final String name ) {
        ScopeManager.access$100 ( ScopeManager.this ).getServletContext().removeAttribute ( name );
    }
    @Override
    protected Enumeration<String> getAttributeNames() {
        return ScopeManager.access$100 ( ScopeManager.this ).getServletContext().getAttributeNames();
    }
    @Override
    protected Object getAttribute ( final String name ) {
        return ScopeManager.access$100 ( ScopeManager.this ).getServletContext().getAttribute ( name );
    }
}
