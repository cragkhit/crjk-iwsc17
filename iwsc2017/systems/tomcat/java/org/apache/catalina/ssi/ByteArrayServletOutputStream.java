package org.apache.catalina.ssi;
import java.io.ByteArrayOutputStream;
import javax.servlet.ServletOutputStream;
import javax.servlet.WriteListener;
public class ByteArrayServletOutputStream extends ServletOutputStream {
    protected final ByteArrayOutputStream buf;
    public ByteArrayServletOutputStream() {
        buf = new ByteArrayOutputStream();
    }
    public byte[] toByteArray() {
        return buf.toByteArray();
    }
    @Override
    public void write ( int b ) {
        buf.write ( b );
    }
    @Override
    public boolean isReady() {
        return false;
    }
    @Override
    public void setWriteListener ( WriteListener listener ) {
    }
}
