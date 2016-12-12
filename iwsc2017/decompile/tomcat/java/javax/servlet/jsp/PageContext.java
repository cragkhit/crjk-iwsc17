package javax.servlet.jsp;
import javax.servlet.jsp.tagext.BodyContent;
import javax.servlet.ServletException;
import javax.servlet.ServletContext;
import javax.servlet.ServletConfig;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import javax.servlet.ServletResponse;
import javax.servlet.ServletRequest;
import javax.servlet.Servlet;
public abstract class PageContext extends JspContext {
    public static final int PAGE_SCOPE = 1;
    public static final int REQUEST_SCOPE = 2;
    public static final int SESSION_SCOPE = 3;
    public static final int APPLICATION_SCOPE = 4;
    public static final String PAGE = "javax.servlet.jsp.jspPage";
    public static final String PAGECONTEXT = "javax.servlet.jsp.jspPageContext";
    public static final String REQUEST = "javax.servlet.jsp.jspRequest";
    public static final String RESPONSE = "javax.servlet.jsp.jspResponse";
    public static final String CONFIG = "javax.servlet.jsp.jspConfig";
    public static final String SESSION = "javax.servlet.jsp.jspSession";
    public static final String OUT = "javax.servlet.jsp.jspOut";
    public static final String APPLICATION = "javax.servlet.jsp.jspApplication";
    public static final String EXCEPTION = "javax.servlet.jsp.jspException";
    public abstract void initialize ( final Servlet p0, final ServletRequest p1, final ServletResponse p2, final String p3, final boolean p4, final int p5, final boolean p6 ) throws IOException, IllegalStateException, IllegalArgumentException;
    public abstract void release();
    public abstract HttpSession getSession();
    public abstract Object getPage();
    public abstract ServletRequest getRequest();
    public abstract ServletResponse getResponse();
    public abstract Exception getException();
    public abstract ServletConfig getServletConfig();
    public abstract ServletContext getServletContext();
    public abstract void forward ( final String p0 ) throws ServletException, IOException;
    public abstract void include ( final String p0 ) throws ServletException, IOException;
    public abstract void include ( final String p0, final boolean p1 ) throws ServletException, IOException;
    public abstract void handlePageException ( final Exception p0 ) throws ServletException, IOException;
    public abstract void handlePageException ( final Throwable p0 ) throws ServletException, IOException;
    public BodyContent pushBody() {
        return null;
    }
    public ErrorData getErrorData() {
        int status = 0;
        final Integer status_code = ( Integer ) this.getRequest().getAttribute ( "javax.servlet.error.status_code" );
        if ( status_code != null ) {
            status = status_code;
        }
        return new ErrorData ( ( Throwable ) this.getRequest().getAttribute ( "javax.servlet.error.exception" ), status, ( String ) this.getRequest().getAttribute ( "javax.servlet.error.request_uri" ), ( String ) this.getRequest().getAttribute ( "javax.servlet.error.servlet_name" ) );
    }
}
