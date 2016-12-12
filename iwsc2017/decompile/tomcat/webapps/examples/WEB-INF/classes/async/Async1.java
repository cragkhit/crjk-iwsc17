// 
// Decompiled by Procyon v0.5.29
// 

package async;

import org.apache.juli.logging.LogFactory;
import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.AsyncContext;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletRequest;
import org.apache.juli.logging.Log;
import javax.servlet.http.HttpServlet;

public class Async1 extends HttpServlet
{
    private static final long serialVersionUID = 1L;
    private static final Log log;
    
    protected void service(final HttpServletRequest req, final HttpServletResponse resp) throws ServletException, IOException {
        final AsyncContext actx = req.startAsync();
        actx.setTimeout(30000L);
        final Runnable run = new Runnable() {
            @Override
            public void run() {
                try {
                    final String path = "/jsp/async/async1.jsp";
                    Thread.currentThread().setName("Async1-Thread");
                    Async1.log.info((Object)"Putting AsyncThread to sleep");
                    Thread.sleep(2000L);
                    Async1.log.info((Object)("Dispatching to " + path));
                    actx.dispatch(path);
                }
                catch (InterruptedException x) {
                    Async1.log.error((Object)"Async1", (Throwable)x);
                }
                catch (IllegalStateException x2) {
                    Async1.log.error((Object)"Async1", (Throwable)x2);
                }
            }
        };
        final Thread t = new Thread(run);
        t.start();
    }
    
    static {
        log = LogFactory.getLog((Class)Async1.class);
    }
}
