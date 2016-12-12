package org.apache.catalina.ssi;
import javax.servlet.WriteListener;
import java.io.ByteArrayOutputStream;
import javax.servlet.ServletOutputStream;
public class ByteArrayServletOutputStream extends ServletOutputStream {
    protected final ByteArrayOutputStream buf;
    public ByteArrayServletOutputStream() {
        this.buf = new ByteArrayOutputStream();
    }
    public byte[] toByteArray() {
        return this.buf.toByteArray();
    }
    public void write ( final int b ) {
        this.buf.write ( b );
    }
    public boolean isReady() {
        return false;
    }
    public void setWriteListener ( final WriteListener listener ) {
    }
}
