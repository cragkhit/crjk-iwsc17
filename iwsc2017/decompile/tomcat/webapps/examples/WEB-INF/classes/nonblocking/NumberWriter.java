// 
// Decompiled by Procyon v0.5.29
// 

package nonblocking;

import java.nio.charset.StandardCharsets;
import java.util.concurrent.atomic.AtomicInteger;
import javax.servlet.ServletOutputStream;
import javax.servlet.ServletInputStream;
import javax.servlet.WriteListener;
import javax.servlet.ReadListener;
import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.AsyncContext;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServlet;

public class NumberWriter extends HttpServlet
{
    private static final long serialVersionUID = 1L;
    
    protected void doGet(final HttpServletRequest req, final HttpServletResponse resp) throws ServletException, IOException {
        resp.setContentType("text/plain");
        resp.setCharacterEncoding("UTF-8");
        final AsyncContext ac = req.startAsync();
        final NumberWriterListener listener = new NumberWriterListener(ac, req.getInputStream(), resp.getOutputStream());
    }
    
    private static class NumberWriterListener implements ReadListener, WriteListener
    {
        private static final int LIMIT = 10000;
        private final AsyncContext ac;
        private final ServletInputStream sis;
        private final ServletOutputStream sos;
        private final AtomicInteger counter;
        private volatile boolean readFinished;
        private byte[] buffer;
        
        private NumberWriterListener(final AsyncContext ac, final ServletInputStream sis, final ServletOutputStream sos) {
            this.counter = new AtomicInteger(0);
            this.readFinished = false;
            this.buffer = new byte[8192];
            this.ac = ac;
            this.sis = sis;
            this.sos = sos;
            sis.setReadListener((ReadListener)this);
            sos.setWriteListener((WriteListener)this);
        }
        
        public void onDataAvailable() throws IOException {
            int read = 0;
            while (this.sis.isReady() && read > -1) {
                read = this.sis.read(this.buffer);
                if (read > 0) {
                    throw new IOException("Data was present in input stream");
                }
            }
        }
        
        public void onAllDataRead() throws IOException {
            this.readFinished = true;
            if (this.sos.isReady()) {
                this.onWritePossible();
            }
        }
        
        public void onWritePossible() throws IOException {
            if (this.readFinished) {
                int i = this.counter.get();
                for (boolean ready = true; i < 10000 && ready; ready = this.sos.isReady()) {
                    i = this.counter.incrementAndGet();
                    final String msg = String.format("%1$020d\n", i);
                    this.sos.write(msg.getBytes(StandardCharsets.UTF_8));
                }
                if (i == 10000) {
                    this.ac.complete();
                }
            }
        }
        
        public void onError(final Throwable throwable) {
            this.ac.complete();
        }
    }
}
