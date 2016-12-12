package javax.servlet.jsp.el;
import java.util.Enumeration;
class ImplicitObjectELResolver$ScopeManager$5 extends ScopeMap<String> {
    @Override
    protected Enumeration<String> getAttributeNames() {
        return ScopeManager.access$100 ( ScopeManager.this ).getServletContext().getInitParameterNames();
    }
    @Override
    protected String getAttribute ( final String name ) {
        return ScopeManager.access$100 ( ScopeManager.this ).getServletContext().getInitParameter ( name );
    }
}
