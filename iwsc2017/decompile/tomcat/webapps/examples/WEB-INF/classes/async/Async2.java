// 
// Decompiled by Procyon v0.5.29
// 

package async;

import org.apache.juli.logging.LogFactory;
import javax.servlet.ServletException;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import javax.servlet.AsyncContext;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletRequest;
import org.apache.juli.logging.Log;
import javax.servlet.http.HttpServlet;

public class Async2 extends HttpServlet
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
                    Thread.currentThread().setName("Async2-Thread");
                    Async2.log.info((Object)"Putting AsyncThread to sleep");
                    Thread.sleep(2000L);
                    Async2.log.info((Object)"Writing data.");
                    final Date date = new Date(System.currentTimeMillis());
                    final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss Z");
                    actx.getResponse().getWriter().write("Output from background thread. Time: " + sdf.format(date) + "\n");
                    actx.complete();
                }
                catch (InterruptedException x) {
                    Async2.log.error((Object)"Async2", (Throwable)x);
                }
                catch (IllegalStateException x2) {
                    Async2.log.error((Object)"Async2", (Throwable)x2);
                }
                catch (IOException x3) {
                    Async2.log.error((Object)"Async2", (Throwable)x3);
                }
            }
        };
        final Thread t = new Thread(run);
        t.start();
    }
    
    static {
        log = LogFactory.getLog((Class)Async2.class);
    }
}
