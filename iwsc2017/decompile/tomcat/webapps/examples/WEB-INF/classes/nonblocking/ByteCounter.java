// 
// Decompiled by Procyon v0.5.29
// 

package nonblocking;

import java.nio.charset.StandardCharsets;
import javax.servlet.ServletOutputStream;
import javax.servlet.ServletInputStream;
import javax.servlet.WriteListener;
import javax.servlet.ReadListener;
import javax.servlet.AsyncContext;
import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServlet;

public class ByteCounter extends HttpServlet
{
    private static final long serialVersionUID = 1L;
    
    protected void doGet(final HttpServletRequest req, final HttpServletResponse resp) throws ServletException, IOException {
        resp.setContentType("text/plain");
        resp.setCharacterEncoding("UTF-8");
        resp.getWriter().println("Try again using a POST request.");
    }
    
    protected void doPost(final HttpServletRequest req, final HttpServletResponse resp) throws ServletException, IOException {
        resp.setContentType("text/plain");
        resp.setCharacterEncoding("UTF-8");
        final AsyncContext ac = req.startAsync();
        final CounterListener listener = new CounterListener(ac, req.getInputStream(), resp.getOutputStream());
    }
    
    private static class CounterListener implements ReadListener, WriteListener
    {
        private final AsyncContext ac;
        private final ServletInputStream sis;
        private final ServletOutputStream sos;
        private volatile boolean readFinished;
        private volatile long totalBytesRead;
        private byte[] buffer;
        
        private CounterListener(final AsyncContext ac, final ServletInputStream sis, final ServletOutputStream sos) {
            this.readFinished = false;
            this.totalBytesRead = 0L;
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
                    this.totalBytesRead += read;
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
                final String msg = "Total bytes written = [" + this.totalBytesRead + "]";
                this.sos.write(msg.getBytes(StandardCharsets.UTF_8));
                this.ac.complete();
            }
        }
        
        public void onError(final Throwable throwable) {
            this.ac.complete();
        }
    }
}
