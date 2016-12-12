package javax.servlet.jsp.el;
import java.util.Enumeration;
class ImplicitObjectELResolver$ScopeManager$7 extends ScopeMap<String> {
    @Override
    protected Enumeration<String> getAttributeNames() {
        return ScopeManager.access$100 ( ScopeManager.this ).getRequest().getParameterNames();
    }
    @Override
    protected String getAttribute ( final String name ) {
        return ScopeManager.access$100 ( ScopeManager.this ).getRequest().getParameter ( name );
    }
}
