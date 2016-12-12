package javax.servlet.jsp;
import javax.servlet.ServletContext;
import javax.servlet.ServletResponse;
import javax.servlet.ServletRequest;
import javax.servlet.Servlet;
public abstract class JspFactory {
    private static volatile JspFactory deflt;
    public static void setDefaultFactory ( final JspFactory deflt ) {
        JspFactory.deflt = deflt;
    }
    public static JspFactory getDefaultFactory() {
        return JspFactory.deflt;
    }
    public abstract PageContext getPageContext ( final Servlet p0, final ServletRequest p1, final ServletResponse p2, final String p3, final boolean p4, final int p5, final boolean p6 );
    public abstract void releasePageContext ( final PageContext p0 );
    public abstract JspEngineInfo getEngineInfo();
    public abstract JspApplicationContext getJspApplicationContext ( final ServletContext p0 );
    static {
        JspFactory.deflt = null;
    }
}
