package javax.servlet.jsp;
import javax.el.ExpressionFactory;
import javax.el.ELResolver;
import javax.el.ELContextListener;
public interface JspApplicationContext {
    void addELContextListener ( ELContextListener p0 );
    void addELResolver ( ELResolver p0 ) throws IllegalStateException;
    ExpressionFactory getExpressionFactory();
}
