// 
// Decompiled by Procyon v0.5.29
// 

package async;

import org.apache.juli.logging.LogFactory;
import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.AsyncContext;
import java.text.SimpleDateFormat;
import java.util.Date;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletRequest;
import org.apache.juli.logging.Log;
import javax.servlet.http.HttpServlet;

public class Async0 extends HttpServlet
{
    private static final long serialVersionUID = 1L;
    private static final Log log;
    
    protected void service(final HttpServletRequest req, final HttpServletResponse resp) throws ServletException, IOException {
        if (Boolean.TRUE.equals(req.getAttribute("dispatch"))) {
            Async0.log.info((Object)"Received dispatch, completing on the worker thread.");
            Async0.log.info((Object)("After complete called started:" + req.isAsyncStarted()));
            final Date date = new Date(System.currentTimeMillis());
            final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss Z");
            resp.getWriter().write("Async dispatch worked: " + sdf.format(date) + "\n");
        }
        else {
            resp.setContentType("text/plain");
            final AsyncContext actx = req.startAsync();
            actx.setTimeout(Long.MAX_VALUE);
            final Runnable run = new Runnable() {
                @Override
                public void run() {
                    try {
                        req.setAttribute("dispatch", (Object)Boolean.TRUE);
                        Thread.currentThread().setName("Async0-Thread");
                        Async0.log.info((Object)"Putting AsyncThread to sleep");
                        Thread.sleep(2000L);
                        Async0.log.info((Object)"Dispatching");
                        actx.dispatch();
                    }
                    catch (InterruptedException x) {
                        Async0.log.error((Object)"Async1", (Throwable)x);
                    }
                    catch (IllegalStateException x2) {
                        Async0.log.error((Object)"Async1", (Throwable)x2);
                    }
                }
            };
            final Thread t = new Thread(run);
            t.start();
        }
    }
    
    static {
        log = LogFactory.getLog((Class)Async0.class);
    }
}
