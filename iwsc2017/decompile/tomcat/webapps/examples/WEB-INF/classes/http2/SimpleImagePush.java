// 
// Decompiled by Procyon v0.5.29
// 

package http2;

import java.io.IOException;
import javax.servlet.ServletException;
import java.io.PrintWriter;
import javax.servlet.http.PushBuilder;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServlet;

public class SimpleImagePush extends HttpServlet
{
    private static final long serialVersionUID = 1L;
    
    protected void doGet(final HttpServletRequest req, final HttpServletResponse resp) throws ServletException, IOException {
        final PushBuilder pb = req.getPushBuilder().path("servlets/images/code.gif");
        pb.push();
        resp.setCharacterEncoding("UTF-8");
        resp.setContentType("text/html");
        final PrintWriter pw = resp.getWriter();
        pw.println("<html>");
        pw.println("<body>");
        pw.println("<p>The following image was provided via a push request.</p>");
        pw.println("<img src=\"" + req.getContextPath() + "/servlets/images/code.gif\"/>");
        pw.println("</body>");
        pw.println("</html>");
        pw.flush();
    }
}
