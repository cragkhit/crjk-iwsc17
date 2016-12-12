import javax.servlet.ServletResponse;
import javax.servlet.ServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServlet;

// 
// Decompiled by Procyon v0.5.29
// 

public class ServletToJsp extends HttpServlet
{
    private static final long serialVersionUID = 1L;
    
    public void doGet(final HttpServletRequest request, final HttpServletResponse response) {
        try {
            request.setAttribute("servletName", (Object)"servletToJsp");
            this.getServletConfig().getServletContext().getRequestDispatcher("/jsp/jsptoserv/hello.jsp").forward((ServletRequest)request, (ServletResponse)response);
        }
        catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}
