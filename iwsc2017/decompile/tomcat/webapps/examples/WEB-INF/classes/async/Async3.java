// 
// Decompiled by Procyon v0.5.29
// 

package async;

import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.AsyncContext;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServlet;

public class Async3 extends HttpServlet
{
    private static final long serialVersionUID = 1L;
    
    protected void service(final HttpServletRequest req, final HttpServletResponse resp) throws ServletException, IOException {
        final AsyncContext actx = req.startAsync();
        actx.setTimeout(30000L);
        actx.dispatch("/jsp/async/async3.jsp");
    }
}
