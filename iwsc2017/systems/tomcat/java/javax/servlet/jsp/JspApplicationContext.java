package javax.servlet.jsp;
import javax.el.ELContextListener;
import javax.el.ELResolver;
import javax.el.ExpressionFactory;
public interface JspApplicationContext {
    public void addELContextListener ( ELContextListener listener );
    public void addELResolver ( ELResolver resolver ) throws IllegalStateException;
    public ExpressionFactory getExpressionFactory();
}
