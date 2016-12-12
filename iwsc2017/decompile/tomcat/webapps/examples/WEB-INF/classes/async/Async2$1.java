// 
// Decompiled by Procyon v0.5.29
// 

package async;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import javax.servlet.AsyncContext;

class Async2$1 implements Runnable {
    final /* synthetic */ AsyncContext val$actx;
    
    @Override
    public void run() {
        try {
            Thread.currentThread().setName("Async2-Thread");
            Async2.access$000().info((Object)"Putting AsyncThread to sleep");
            Thread.sleep(2000L);
            Async2.access$000().info((Object)"Writing data.");
            final Date date = new Date(System.currentTimeMillis());
            final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss Z");
            this.val$actx.getResponse().getWriter().write("Output from background thread. Time: " + sdf.format(date) + "\n");
            this.val$actx.complete();
        }
        catch (InterruptedException x) {
            Async2.access$000().error((Object)"Async2", (Throwable)x);
        }
        catch (IllegalStateException x2) {
            Async2.access$000().error((Object)"Async2", (Throwable)x2);
        }
        catch (IOException x3) {
            Async2.access$000().error((Object)"Async2", (Throwable)x3);
        }
    }
}