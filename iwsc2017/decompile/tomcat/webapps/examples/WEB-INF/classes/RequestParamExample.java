import javax.servlet.ServletException;
import java.io.IOException;
import java.io.PrintWriter;
import util.HTMLFilter;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletRequest;
import java.util.ResourceBundle;
import javax.servlet.http.HttpServlet;

// 
// Decompiled by Procyon v0.5.29
// 

public class RequestParamExample extends HttpServlet
{
    private static final long serialVersionUID = 1L;
    private static final ResourceBundle RB;
    
    public void doGet(final HttpServletRequest request, final HttpServletResponse response) throws IOException, ServletException {
        response.setContentType("text/html");
        response.setCharacterEncoding("UTF-8");
        final PrintWriter out = response.getWriter();
        out.println("<!DOCTYPE html><html>");
        out.println("<head>");
        out.println("<meta charset=\"UTF-8\" />");
        final String title = RequestParamExample.RB.getString("requestparams.title");
        out.println("<title>" + title + "</title>");
        out.println("</head>");
        out.println("<body bgcolor=\"white\">");
        out.println("<a href=\"../reqparams.html\">");
        out.println("<img src=\"../images/code.gif\" height=24 width=24 align=right border=0 alt=\"view code\"></a>");
        out.println("<a href=\"../index.html\">");
        out.println("<img src=\"../images/return.gif\" height=24 width=24 align=right border=0 alt=\"return\"></a>");
        out.println("<h3>" + title + "</h3>");
        final String firstName = request.getParameter("firstname");
        final String lastName = request.getParameter("lastname");
        out.println(RequestParamExample.RB.getString("requestparams.params-in-req") + "<br>");
        if (firstName != null || lastName != null) {
            out.println(RequestParamExample.RB.getString("requestparams.firstname"));
            out.println(" = " + HTMLFilter.filter(firstName) + "<br>");
            out.println(RequestParamExample.RB.getString("requestparams.lastname"));
            out.println(" = " + HTMLFilter.filter(lastName));
        }
        else {
            out.println(RequestParamExample.RB.getString("requestparams.no-params"));
        }
        out.println("<P>");
        out.print("<form action=\"");
        out.print("RequestParamExample\" ");
        out.println("method=POST>");
        out.println(RequestParamExample.RB.getString("requestparams.firstname"));
        out.println("<input type=text size=20 name=firstname>");
        out.println("<br>");
        out.println(RequestParamExample.RB.getString("requestparams.lastname"));
        out.println("<input type=text size=20 name=lastname>");
        out.println("<br>");
        out.println("<input type=submit>");
        out.println("</form>");
        out.println("</body>");
        out.println("</html>");
    }
    
    public void doPost(final HttpServletRequest request, final HttpServletResponse response) throws IOException, ServletException {
        this.doGet(request, response);
    }
    
    static {
        RB = ResourceBundle.getBundle("LocalStrings");
    }
}
