// 
// Decompiled by Procyon v0.5.29
// 

package compressionFilters;

import java.util.zip.GZIPOutputStream;
import javax.servlet.WriteListener;
import java.io.IOException;
import java.util.Arrays;
import java.io.OutputStream;
import javax.servlet.ServletOutputStream;

public class CompressionResponseStream extends ServletOutputStream
{
    protected int compressionThreshold;
    protected int compressionBuffer;
    protected String[] compressionMimeTypes;
    private int debug;
    protected byte[] buffer;
    protected int bufferCount;
    protected OutputStream gzipstream;
    protected boolean closed;
    protected final CompressionServletResponseWrapper response;
    protected final ServletOutputStream output;
    
    public CompressionResponseStream(final CompressionServletResponseWrapper responseWrapper, final ServletOutputStream originalOutput) {
        this.compressionThreshold = 0;
        this.compressionBuffer = 0;
        this.compressionMimeTypes = new String[] { "text/html", "text/xml", "text/plain" };
        this.debug = 0;
        this.buffer = null;
        this.bufferCount = 0;
        this.gzipstream = null;
        this.closed = false;
        this.closed = false;
        this.response = responseWrapper;
        this.output = originalOutput;
    }
    
    public void setDebugLevel(final int debug) {
        this.debug = debug;
    }
    
    protected void setCompressionThreshold(final int compressionThreshold) {
        this.compressionThreshold = compressionThreshold;
        this.buffer = new byte[this.compressionThreshold];
        if (this.debug > 1) {
            System.out.println("compressionThreshold is set to " + this.compressionThreshold);
        }
    }
    
    protected void setCompressionBuffer(final int compressionBuffer) {
        this.compressionBuffer = compressionBuffer;
        if (this.debug > 1) {
            System.out.println("compressionBuffer is set to " + this.compressionBuffer);
        }
    }
    
    public void setCompressionMimeTypes(final String[] compressionMimeTypes) {
        this.compressionMimeTypes = compressionMimeTypes;
        if (this.debug > 1) {
            System.out.println("compressionMimeTypes is set to " + Arrays.toString(this.compressionMimeTypes));
        }
    }
    
    public void close() throws IOException {
        if (this.debug > 1) {
            System.out.println("close() @ CompressionResponseStream");
        }
        if (this.closed) {
            throw new IOException("This output stream has already been closed");
        }
        if (this.gzipstream != null) {
            this.flushToGZip();
            this.gzipstream.close();
            this.gzipstream = null;
        }
        else if (this.bufferCount > 0) {
            if (this.debug > 2) {
                System.out.print("output.write(");
                System.out.write(this.buffer, 0, this.bufferCount);
                System.out.println(")");
            }
            this.output.write(this.buffer, 0, this.bufferCount);
            this.bufferCount = 0;
        }
        this.output.close();
        this.closed = true;
    }
    
    public void flush() throws IOException {
        if (this.debug > 1) {
            System.out.println("flush() @ CompressionResponseStream");
        }
        if (this.closed) {
            throw new IOException("Cannot flush a closed output stream");
        }
        if (this.gzipstream != null) {
            this.gzipstream.flush();
        }
    }
    
    public void flushToGZip() throws IOException {
        if (this.debug > 1) {
            System.out.println("flushToGZip() @ CompressionResponseStream");
        }
        if (this.bufferCount > 0) {
            if (this.debug > 1) {
                System.out.println("flushing out to GZipStream, bufferCount = " + this.bufferCount);
            }
            this.writeToGZip(this.buffer, 0, this.bufferCount);
            this.bufferCount = 0;
        }
    }
    
    public void write(final int b) throws IOException {
        if (this.debug > 1) {
            System.out.println("write " + b + " in CompressionResponseStream ");
        }
        if (this.closed) {
            throw new IOException("Cannot write to a closed output stream");
        }
        if (this.bufferCount >= this.buffer.length) {
            this.flushToGZip();
        }
        this.buffer[this.bufferCount++] = (byte)b;
    }
    
    public void write(final byte[] b) throws IOException {
        this.write(b, 0, b.length);
    }
    
    public boolean isReady() {
        return false;
    }
    
    public void setWriteListener(final WriteListener listener) {
    }
    
    public void write(final byte[] b, final int off, final int len) throws IOException {
        if (this.debug > 1) {
            System.out.println("write, bufferCount = " + this.bufferCount + " len = " + len + " off = " + off);
        }
        if (this.debug > 2) {
            System.out.print("write(");
            System.out.write(b, off, len);
            System.out.println(")");
        }
        if (this.closed) {
            throw new IOException("Cannot write to a closed output stream");
        }
        if (len == 0) {
            return;
        }
        if (len <= this.buffer.length - this.bufferCount) {
            System.arraycopy(b, off, this.buffer, this.bufferCount, len);
            this.bufferCount += len;
            return;
        }
        this.flushToGZip();
        if (len <= this.buffer.length - this.bufferCount) {
            System.arraycopy(b, off, this.buffer, this.bufferCount, len);
            this.bufferCount += len;
            return;
        }
        this.writeToGZip(b, off, len);
    }
    
    public void writeToGZip(final byte[] b, final int off, final int len) throws IOException {
        if (this.debug > 1) {
            System.out.println("writeToGZip, len = " + len);
        }
        if (this.debug > 2) {
            System.out.print("writeToGZip(");
            System.out.write(b, off, len);
            System.out.println(")");
        }
        if (this.gzipstream == null) {
            if (this.debug > 1) {
                System.out.println("new GZIPOutputStream");
            }
            boolean alreadyCompressed = false;
            final String contentEncoding = this.response.getHeader("Content-Encoding");
            if (contentEncoding != null) {
                if (contentEncoding.contains("gzip")) {
                    alreadyCompressed = true;
                    if (this.debug > 0) {
                        System.out.println("content is already compressed");
                    }
                }
                else if (this.debug > 0) {
                    System.out.println("content is not compressed yet");
                }
            }
            boolean compressibleMimeType = false;
            if (this.compressionMimeTypes != null) {
                if (this.startsWithStringArray(this.compressionMimeTypes, this.response.getContentType())) {
                    compressibleMimeType = true;
                    if (this.debug > 0) {
                        System.out.println("mime type " + this.response.getContentType() + " is compressible");
                    }
                }
                else if (this.debug > 0) {
                    System.out.println("mime type " + this.response.getContentType() + " is not compressible");
                }
            }
            if (this.response.isCommitted()) {
                if (this.debug > 1) {
                    System.out.print("Response already committed. Using original output stream");
                }
                this.gzipstream = (OutputStream)this.output;
            }
            else if (alreadyCompressed) {
                if (this.debug > 1) {
                    System.out.print("Response already compressed. Using original output stream");
                }
                this.gzipstream = (OutputStream)this.output;
            }
            else if (!compressibleMimeType) {
                if (this.debug > 1) {
                    System.out.print("Response mime type is not compressible. Using original output stream");
                }
                this.gzipstream = (OutputStream)this.output;
            }
            else {
                this.response.addHeader("Content-Encoding", "gzip");
                this.response.setContentLength(-1);
                this.response.setBufferSize(this.compressionBuffer);
                this.gzipstream = new GZIPOutputStream((OutputStream)this.output);
            }
        }
        this.gzipstream.write(b, off, len);
    }
    
    public boolean closed() {
        return this.closed;
    }
    
    private boolean startsWithStringArray(final String[] sArray, final String value) {
        if (value == null) {
            return false;
        }
        for (int i = 0; i < sArray.length; ++i) {
            if (value.startsWith(sArray[i])) {
                return true;
            }
        }
        return false;
    }
}
