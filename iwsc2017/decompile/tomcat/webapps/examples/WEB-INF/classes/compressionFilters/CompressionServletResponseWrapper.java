// 
// Decompiled by Procyon v0.5.29
// 

package compressionFilters;

import java.io.Writer;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.io.PrintWriter;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletResponseWrapper;

public class CompressionServletResponseWrapper extends HttpServletResponseWrapper
{
    protected final HttpServletResponse origResponse;
    protected ServletOutputStream stream;
    protected PrintWriter writer;
    protected int compressionThreshold;
    protected int compressionBuffer;
    protected String[] compressionMimeTypes;
    protected int debug;
    private final Map<String, String> headerCopies;
    
    public CompressionServletResponseWrapper(final HttpServletResponse response) {
        super(response);
        this.stream = null;
        this.writer = null;
        this.compressionThreshold = 0;
        this.compressionBuffer = 8192;
        this.compressionMimeTypes = new String[] { "text/html", "text/xml", "text/plain" };
        this.debug = 0;
        this.headerCopies = new HashMap<String, String>();
        this.origResponse = response;
        if (this.debug > 1) {
            System.out.println("CompressionServletResponseWrapper constructor gets called");
        }
    }
    
    public void setCompressionThreshold(final int threshold) {
        if (this.debug > 1) {
            System.out.println("setCompressionThreshold to " + threshold);
        }
        this.compressionThreshold = threshold;
    }
    
    public void setCompressionBuffer(final int buffer) {
        if (this.debug > 1) {
            System.out.println("setCompressionBuffer to " + buffer);
        }
        this.compressionBuffer = buffer;
    }
    
    public void setCompressionMimeTypes(final String[] mimeTypes) {
        if (this.debug > 1) {
            System.out.println("setCompressionMimeTypes to " + Arrays.toString(mimeTypes));
        }
        this.compressionMimeTypes = mimeTypes;
    }
    
    public void setDebugLevel(final int debug) {
        this.debug = debug;
    }
    
    protected ServletOutputStream createOutputStream() throws IOException {
        if (this.debug > 1) {
            System.out.println("createOutputStream gets called");
        }
        final CompressionResponseStream stream = new CompressionResponseStream(this, this.origResponse.getOutputStream());
        stream.setDebugLevel(this.debug);
        stream.setCompressionThreshold(this.compressionThreshold);
        stream.setCompressionBuffer(this.compressionBuffer);
        stream.setCompressionMimeTypes(this.compressionMimeTypes);
        return stream;
    }
    
    public void finishResponse() {
        try {
            if (this.writer != null) {
                this.writer.close();
            }
            else if (this.stream != null) {
                this.stream.close();
            }
        }
        catch (IOException ex) {}
    }
    
    public void flushBuffer() throws IOException {
        if (this.debug > 1) {
            System.out.println("flush buffer @ GZipServletResponseWrapper");
        }
        ((CompressionResponseStream)this.stream).flush();
    }
    
    public ServletOutputStream getOutputStream() throws IOException {
        if (this.writer != null) {
            throw new IllegalStateException("getWriter() has already been called for this response");
        }
        if (this.stream == null) {
            this.stream = this.createOutputStream();
        }
        if (this.debug > 1) {
            System.out.println("stream is set to " + this.stream + " in getOutputStream");
        }
        return this.stream;
    }
    
    public PrintWriter getWriter() throws IOException {
        if (this.writer != null) {
            return this.writer;
        }
        if (this.stream != null) {
            throw new IllegalStateException("getOutputStream() has already been called for this response");
        }
        this.stream = this.createOutputStream();
        if (this.debug > 1) {
            System.out.println("stream is set to " + this.stream + " in getWriter");
        }
        final String charEnc = this.origResponse.getCharacterEncoding();
        if (this.debug > 1) {
            System.out.println("character encoding is " + charEnc);
        }
        if (charEnc != null) {
            this.writer = new PrintWriter(new OutputStreamWriter((OutputStream)this.stream, charEnc));
        }
        else {
            this.writer = new PrintWriter((OutputStream)this.stream);
        }
        return this.writer;
    }
    
    public String getHeader(final String name) {
        return this.headerCopies.get(name);
    }
    
    public void addHeader(final String name, final String value) {
        if (this.headerCopies.containsKey(name)) {
            final String existingValue = this.headerCopies.get(name);
            if (existingValue != null && existingValue.length() > 0) {
                this.headerCopies.put(name, existingValue + "," + value);
            }
            else {
                this.headerCopies.put(name, value);
            }
        }
        else {
            this.headerCopies.put(name, value);
        }
        super.addHeader(name, value);
    }
    
    public void setHeader(final String name, final String value) {
        this.headerCopies.put(name, value);
        super.setHeader(name, value);
    }
}
