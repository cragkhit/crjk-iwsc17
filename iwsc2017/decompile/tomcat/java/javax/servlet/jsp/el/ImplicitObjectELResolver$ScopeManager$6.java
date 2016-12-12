package javax.servlet.jsp.el;
import java.util.Enumeration;
class ImplicitObjectELResolver$ScopeManager$6 extends ScopeMap<Object> {
    @Override
    protected void setAttribute ( final String name, final Object value ) {
        ScopeManager.access$100 ( ScopeManager.this ).setAttribute ( name, value );
    }
    @Override
    protected void removeAttribute ( final String name ) {
        ScopeManager.access$100 ( ScopeManager.this ).removeAttribute ( name );
    }
    @Override
    protected Enumeration<String> getAttributeNames() {
        return ScopeManager.access$100 ( ScopeManager.this ).getAttributeNamesInScope ( 1 );
    }
    @Override
    protected Object getAttribute ( final String name ) {
        return ScopeManager.access$100 ( ScopeManager.this ).getAttribute ( name );
    }
}
