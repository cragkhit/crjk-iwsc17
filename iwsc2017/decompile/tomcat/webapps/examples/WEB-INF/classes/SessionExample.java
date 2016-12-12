import javax.servlet.ServletException;
import java.io.IOException;
import java.util.Enumeration;
import javax.servlet.http.HttpSession;
import java.io.PrintWriter;
import util.HTMLFilter;
import java.util.Date;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletRequest;
import java.util.ResourceBundle;
import javax.servlet.http.HttpServlet;

// 
// Decompiled by Procyon v0.5.29
// 

public class SessionExample extends HttpServlet
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
        final String title = SessionExample.RB.getString("sessions.title");
        out.println("<title>" + title + "</title>");
        out.println("</head>");
        out.println("<body bgcolor=\"white\">");
        out.println("<a href=\"../sessions.html\">");
        out.println("<img src=\"../images/code.gif\" height=24 width=24 align=right border=0 alt=\"view code\"></a>");
        out.println("<a href=\"../index.html\">");
        out.println("<img src=\"../images/return.gif\" height=24 width=24 align=right border=0 alt=\"return\"></a>");
        out.println("<h3>" + title + "</h3>");
        final HttpSession session = request.getSession(true);
        out.println(SessionExample.RB.getString("sessions.id") + " " + session.getId());
        out.println("<br>");
        out.println(SessionExample.RB.getString("sessions.created") + " ");
        out.println(new Date(session.getCreationTime()) + "<br>");
        out.println(SessionExample.RB.getString("sessions.lastaccessed") + " ");
        out.println(new Date(session.getLastAccessedTime()));
        final String dataName = request.getParameter("dataname");
        final String dataValue = request.getParameter("datavalue");
        if (dataName != null && dataValue != null) {
            session.setAttribute(dataName, (Object)dataValue);
        }
        out.println("<P>");
        out.println(SessionExample.RB.getString("sessions.data") + "<br>");
        final Enumeration<String> names = (Enumeration<String>)session.getAttributeNames();
        while (names.hasMoreElements()) {
            final String name = names.nextElement();
            final String value = session.getAttribute(name).toString();
            out.println(HTMLFilter.filter(name) + " = " + HTMLFilter.filter(value) + "<br>");
        }
        out.println("<P>");
        out.print("<form action=\"");
        out.print(response.encodeURL("SessionExample"));
        out.print("\" ");
        out.println("method=POST>");
        out.println(SessionExample.RB.getString("sessions.dataname"));
        out.println("<input type=text size=20 name=dataname>");
        out.println("<br>");
        out.println(SessionExample.RB.getString("sessions.datavalue"));
        out.println("<input type=text size=20 name=datavalue>");
        out.println("<br>");
        out.println("<input type=submit>");
        out.println("</form>");
        out.println("<P>GET based form:<br>");
        out.print("<form action=\"");
        out.print(response.encodeURL("SessionExample"));
        out.print("\" ");
        out.println("method=GET>");
        out.println(SessionExample.RB.getString("sessions.dataname"));
        out.println("<input type=text size=20 name=dataname>");
        out.println("<br>");
        out.println(SessionExample.RB.getString("sessions.datavalue"));
        out.println("<input type=text size=20 name=datavalue>");
        out.println("<br>");
        out.println("<input type=submit>");
        out.println("</form>");
        out.print("<p><a href=\"");
        out.print(HTMLFilter.filter(response.encodeURL("SessionExample?dataname=foo&datavalue=bar")));
        out.println("\" >URL encoded </a>");
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
