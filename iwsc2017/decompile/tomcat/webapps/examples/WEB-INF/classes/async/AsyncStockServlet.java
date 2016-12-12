// 
// Decompiled by Procyon v0.5.29
// 

package async;

import org.apache.juli.logging.LogFactory;
import javax.servlet.AsyncEvent;
import java.io.PrintWriter;
import java.util.Iterator;
import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletRequest;
import java.util.concurrent.atomic.AtomicInteger;
import javax.servlet.AsyncContext;
import java.util.concurrent.ConcurrentLinkedQueue;
import org.apache.juli.logging.Log;
import javax.servlet.AsyncListener;
import javax.servlet.http.HttpServlet;

public class AsyncStockServlet extends HttpServlet implements Stockticker.TickListener, AsyncListener
{
    private static final long serialVersionUID = 1L;
    private static final Log log;
    private static final ConcurrentLinkedQueue<AsyncContext> clients;
    private static final AtomicInteger clientcount;
    private static final Stockticker ticker;
    
    public AsyncStockServlet() {
        AsyncStockServlet.log.info((Object)"AsyncStockServlet created");
    }
    
    protected void service(final HttpServletRequest req, final HttpServletResponse resp) throws ServletException, IOException {
        if (req.isAsyncStarted()) {
            req.getAsyncContext().complete();
        }
        else if (req.isAsyncSupported()) {
            final AsyncContext actx = req.startAsync();
            actx.addListener((AsyncListener)this);
            resp.setContentType("text/plain");
            AsyncStockServlet.clients.add(actx);
            if (AsyncStockServlet.clientcount.incrementAndGet() == 1) {
                AsyncStockServlet.ticker.addTickListener(this);
            }
        }
        else {
            new Exception("Async Not Supported").printStackTrace();
            resp.sendError(400, "Async is not supported.");
        }
    }
    
    public void tick(final Stockticker.Stock stock) {
        for (final AsyncContext actx : AsyncStockServlet.clients) {
            try {
                this.writeStock(actx, stock);
            }
            catch (Exception ex) {}
        }
    }
    
    public void writeStock(final AsyncContext actx, final Stockticker.Stock stock) {
        final HttpServletResponse response = (HttpServletResponse)actx.getResponse();
        try {
            final PrintWriter writer = response.getWriter();
            writer.write("STOCK#");
            writer.write(stock.getSymbol());
            writer.write("#");
            writer.write(stock.getValueAsString());
            writer.write("#");
            writer.write(stock.getLastChangeAsString());
            writer.write("#");
            writer.write(String.valueOf(stock.getCnt()));
            writer.write("\n");
            writer.flush();
            response.flushBuffer();
        }
        catch (IOException x) {
            try {
                actx.complete();
            }
            catch (Exception ex) {}
        }
    }
    
    public void onComplete(final AsyncEvent event) throws IOException {
        if (AsyncStockServlet.clients.remove(event.getAsyncContext()) && AsyncStockServlet.clientcount.decrementAndGet() == 0) {
            AsyncStockServlet.ticker.removeTickListener(this);
        }
    }
    
    public void onError(final AsyncEvent event) throws IOException {
        event.getAsyncContext().complete();
    }
    
    public void onTimeout(final AsyncEvent event) throws IOException {
        event.getAsyncContext().complete();
    }
    
    public void onStartAsync(final AsyncEvent event) throws IOException {
    }
    
    static {
        log = LogFactory.getLog((Class)AsyncStockServlet.class);
        clients = new ConcurrentLinkedQueue<AsyncContext>();
        clientcount = new AtomicInteger(0);
        ticker = new Stockticker();
    }
}
