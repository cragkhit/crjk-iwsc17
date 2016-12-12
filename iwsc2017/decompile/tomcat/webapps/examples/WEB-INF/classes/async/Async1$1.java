// 
// Decompiled by Procyon v0.5.29
// 

package async;

import javax.servlet.AsyncContext;

class Async1$1 implements Runnable {
    final /* synthetic */ AsyncContext val$actx;
    
    @Override
    public void run() {
        try {
            final String path = "/jsp/async/async1.jsp";
            Thread.currentThread().setName("Async1-Thread");
            Async1.access$000().info((Object)"Putting AsyncThread to sleep");
            Thread.sleep(2000L);
            Async1.access$000().info((Object)("Dispatching to " + path));
            this.val$actx.dispatch(path);
        }
        catch (InterruptedException x) {
            Async1.access$000().error((Object)"Async1", (Throwable)x);
        }
        catch (IllegalStateException x2) {
            Async1.access$000().error((Object)"Async1", (Throwable)x2);
        }
    }
}