// 
// Decompiled by Procyon v0.5.29
// 

package async;

import javax.servlet.AsyncContext;
import javax.servlet.http.HttpServletRequest;

class Async0$1 implements Runnable {
    final /* synthetic */ HttpServletRequest val$req;
    final /* synthetic */ AsyncContext val$actx;
    
    @Override
    public void run() {
        try {
            this.val$req.setAttribute("dispatch", (Object)Boolean.TRUE);
            Thread.currentThread().setName("Async0-Thread");
            Async0.access$000().info((Object)"Putting AsyncThread to sleep");
            Thread.sleep(2000L);
            Async0.access$000().info((Object)"Dispatching");
            this.val$actx.dispatch();
        }
        catch (InterruptedException x) {
            Async0.access$000().error((Object)"Async1", (Throwable)x);
        }
        catch (IllegalStateException x2) {
            Async0.access$000().error((Object)"Async1", (Throwable)x2);
        }
    }
}