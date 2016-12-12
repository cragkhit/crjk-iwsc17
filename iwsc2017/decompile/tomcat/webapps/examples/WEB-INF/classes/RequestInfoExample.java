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

public class RequestInfoExample extends HttpServlet
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
        final String title = RequestInfoExample.RB.getString("requestinfo.title");
        out.println("<title>" + title + "</title>");
        out.println("</head>");
        out.println("<body bgcolor=\"white\">");
        out.println("<a href=\"../reqinfo.html\">");
        out.println("<img src=\"../images/code.gif\" height=24 width=24 align=right border=0 alt=\"view code\"></a>");
        out.println("<a href=\"../index.html\">");
        out.println("<img src=\"../images/return.gif\" height=24 width=24 align=right border=0 alt=\"return\"></a>");
        out.println("<h3>" + title + "</h3>");
        out.println("<table border=0><tr><td>");
        out.println(RequestInfoExample.RB.getString("requestinfo.label.method"));
        out.println("</td><td>");
        out.println(HTMLFilter.filter(request.getMethod()));
        out.println("</td></tr><tr><td>");
        out.println(RequestInfoExample.RB.getString("requestinfo.label.requesturi"));
        out.println("</td><td>");
        out.println(HTMLFilter.filter(request.getRequestURI()));
        out.println("</td></tr><tr><td>");
        out.println(RequestInfoExample.RB.getString("requestinfo.label.protocol"));
        out.println("</td><td>");
        out.println(HTMLFilter.filter(request.getProtocol()));
        out.println("</td></tr><tr><td>");
        out.println(RequestInfoExample.RB.getString("requestinfo.label.pathinfo"));
        out.println("</td><td>");
        out.println(HTMLFilter.filter(request.getPathInfo()));
        out.println("</td></tr><tr><td>");
        out.println(RequestInfoExample.RB.getString("requestinfo.label.remoteaddr"));
        out.println("</td><td>");
        out.println(HTMLFilter.filter(request.getRemoteAddr()));
        out.println("</td></tr>");
        final String cipherSuite = (String)request.getAttribute("javax.servlet.request.cipher_suite");
        if (cipherSuite != null) {
            out.println("<tr><td>");
            out.println("SSLCipherSuite:");
            out.println("</td><td>");
            out.println(HTMLFilter.filter(cipherSuite));
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
