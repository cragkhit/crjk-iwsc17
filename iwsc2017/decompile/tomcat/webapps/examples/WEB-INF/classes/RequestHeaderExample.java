import javax.servlet.ServletException;
import java.io.IOException;
import javax.servlet.http.HttpSession;
import java.util.Enumeration;
import java.io.PrintWriter;
import util.CookieFilter;
import java.util.Locale;
import util.HTMLFilter;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletRequest;
import java.util.ResourceBundle;
import javax.servlet.http.HttpServlet;

// 
// Decompiled by Procyon v0.5.29
// 

public class RequestHeaderExample extends HttpServlet
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
        final String title = RequestHeaderExample.RB.getString("requestheader.title");
        out.println("<title>" + title + "</title>");
        out.println("</head>");
        out.println("<body bgcolor=\"white\">");
        out.println("<a href=\"../reqheaders.html\">");
        out.println("<img src=\"../images/code.gif\" height=24 width=24 align=right border=0 alt=\"view code\"></a>");
        out.println("<a href=\"../index.html\">");
        out.println("<img src=\"../images/return.gif\" height=24 width=24 align=right border=0 alt=\"return\"></a>");
        out.println("<h3>" + title + "</h3>");
        out.println("<table border=0>");
        final Enumeration<String> e = (Enumeration<String>)request.getHeaderNames();
        while (e.hasMoreElements()) {
            final String headerName = e.nextElement();
            final String headerValue = request.getHeader(headerName);
            out.println("<tr><td bgcolor=\"#CCCCCC\">");
            out.println(HTMLFilter.filter(headerName));
            out.println("</td><td>");
            if (headerName.toLowerCase(Locale.ENGLISH).contains("cookie")) {
                final HttpSession session = request.getSession(false);
                String sessionId = null;
                if (session != null) {
                    sessionId = session.getId();
                }
                out.println(HTMLFilter.filter(CookieFilter.filter(headerValue, sessionId)));
            }
            else {
                out.println(HTMLFilter.filter(headerValue));
            }
            out.println("</td></tr>");
        }
        out.println("</table>");
    }
    
    public void doPost(final HttpServletRequest request, final HttpServletResponse response) throws IOException, ServletException {
        this.doGet(request, response);
    }
    
    static {
        RB = ResourceBundle.getBundle("LocalStrings");
    }
}
