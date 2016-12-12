// 
// Decompiled by Procyon v0.5.29
// 

package compressionFilters;

import java.io.IOException;
import javax.servlet.ServletException;
import java.util.Enumeration;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServlet;

public class CompressionFilterTestServlet extends HttpServlet
{
    private static final long serialVersionUID = 1L;
    
    public void doGet(final HttpServletRequest request, final HttpServletResponse response) throws ServletException, IOException {
        final ServletOutputStream out = response.getOutputStream();
        response.setContentType("text/plain");
        final Enumeration<String> e = (Enumeration<String>)request.getHeaders("Accept-Encoding");
        while (e.hasMoreElements()) {
            final String name = e.nextElement();
            out.println(name);
            if (name.indexOf("gzip") != -1) {
                out.println("gzip supported -- able to compress");
            }
            else {
                out.println("gzip not supported");
            }
        }
        out.println("Compression Filter Test Servlet");
        out.println("Minimum content length for compression is 128 bytes");
        out.println("**********  32 bytes  **********");
        out.println("**********  32 bytes  **********");
        out.println("**********  32 bytes  **********");
        out.println("**********  32 bytes  **********");
        out.close();
    }
}
