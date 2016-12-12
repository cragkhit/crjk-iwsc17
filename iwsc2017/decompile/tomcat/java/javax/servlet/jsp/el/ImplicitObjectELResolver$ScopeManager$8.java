package javax.servlet.jsp.el;
import java.util.Enumeration;
class ImplicitObjectELResolver$ScopeManager$8 extends ScopeMap<String[]> {
    @Override
    protected String[] getAttribute ( final String name ) {
        return ScopeManager.access$100 ( ScopeManager.this ).getRequest().getParameterValues ( name );
    }
    @Override
    protected Enumeration<String> getAttributeNames() {
        return ScopeManager.access$100 ( ScopeManager.this ).getRequest().getParameterNames();
    }
}
