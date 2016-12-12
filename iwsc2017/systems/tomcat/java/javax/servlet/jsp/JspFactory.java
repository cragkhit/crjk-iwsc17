package javax.servlet.jsp;
import javax.servlet.Servlet;
import javax.servlet.ServletContext;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
public abstract class JspFactory {
    private static volatile JspFactory deflt = null;
    public JspFactory() {
    }
    public static void setDefaultFactory ( JspFactory deflt ) {
        JspFactory.deflt = deflt;
    }
    public static JspFactory getDefaultFactory() {
        return deflt;
    }
    public abstract PageContext getPageContext ( Servlet servlet,
            ServletRequest request, ServletResponse response,
            String errorPageURL, boolean needsSession, int buffer,
            boolean autoflush );
    public abstract void releasePageContext ( PageContext pc );
    public abstract JspEngineInfo getEngineInfo();
    public abstract JspApplicationContext getJspApplicationContext (
        ServletContext context );
}
