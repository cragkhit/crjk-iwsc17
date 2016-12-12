import javax.servlet.ServletException;
import java.io.IOException;
import javax.servlet.http.HttpSession;
import java.io.PrintWriter;
import util.CookieFilter;
import util.HTMLFilter;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletRequest;
import java.util.ResourceBundle;
import javax.servlet.http.HttpServlet;

// 
// Decompiled by Procyon v0.5.29
// 

public class CookieExample extends HttpServlet
{
    private static final long serialVersionUID = 1L;
    private static final ResourceBundle RB;
    
    public void doGet(final HttpServletRequest request, final HttpServletResponse response) throws IOException, ServletException {
        final String cookieName = request.getParameter("cookiename");
        final String cookieValue = request.getParameter("cookievalue");
        Cookie aCookie = null;
        if (cookieName != null && cookieValue != null) {
            aCookie = new Cookie(cookieName, cookieValue);
            aCookie.setPath(request.getContextPath() + "/");
            response.addCookie(aCookie);
        }
        response.setContentType("text/html");
        response.setCharacterEncoding("UTF-8");
        final PrintWriter out = response.getWriter();
        out.println("<!DOCTYPE html><html>");
        out.println("<head>");
        out.println("<meta charset=\"UTF-8\" />");
        final String title = CookieExample.RB.getString("cookies.title");
        out.println("<title>" + title + "</title>");
        out.println("</head>");
        out.println("<body bgcolor=\"white\">");
        out.println("<a href=\"../cookies.html\">");
        out.println("<img src=\"../images/code.gif\" height=24 width=24 align=right border=0 alt=\"view code\"></a>");
        out.println("<a href=\"../index.html\">");
        out.println("<img src=\"../images/return.gif\" height=24 width=24 align=right border=0 alt=\"return\"></a>");
        out.println("<h3>" + title + "</h3>");
        final Cookie[] cookies = request.getCookies();
        if (cookies != null && cookies.length > 0) {
            final HttpSession session = request.getSession(false);
            String sessionId = null;
            if (session != null) {
                sessionId = session.getId();
            }
            out.println(CookieExample.RB.getString("cookies.cookies") + "<br>");
            for (int i = 0; i < cookies.length; ++i) {
                final Cookie cookie = cookies[i];
                final String cName = cookie.getName();
                final String cValue = cookie.getValue();
                out.print("Cookie Name: " + HTMLFilter.filter(cName) + "<br>");
                out.println("  Cookie Value: " + HTMLFilter.filter(CookieFilter.filter(cName, cValue, sessionId)) + "<br><br>");
            }
        }
        else {
            out.println(CookieExample.RB.getString("cookies.no-cookies"));
        }
        if (aCookie != null) {
            out.println("<P>");
            out.println(CookieExample.RB.getString("cookies.set") + "<br>");
            out.print(CookieExample.RB.getString("cookies.name") + "  " + HTMLFilter.filter(cookieName) + "<br>");
            out.print(CookieExample.RB.getString("cookies.value") + "  " + HTMLFilter.filter(cookieValue));
        }
        out.println("<P>");
        out.println(CookieExample.RB.getString("cookies.make-cookie") + "<br>");
        out.print("<form action=\"");
        out.println("CookieExample\" method=POST>");
        out.print(CookieExample.RB.getString("cookies.name") + "  ");
        out.println("<input type=text length=20 name=cookiename><br>");
        out.print(CookieExample.RB.getString("cookies.value") + "  ");
        out.println("<input type=text length=20 name=cookievalue><br>");
        out.println("<input type=submit></form>");
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
