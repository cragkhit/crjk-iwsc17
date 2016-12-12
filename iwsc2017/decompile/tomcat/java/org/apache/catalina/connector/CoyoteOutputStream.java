package org.apache.catalina.connector;
import javax.servlet.WriteListener;
import java.nio.ByteBuffer;
import java.io.IOException;
import org.apache.tomcat.util.res.StringManager;
import javax.servlet.ServletOutputStream;
public class CoyoteOutputStream extends ServletOutputStream {
    protected static final StringManager sm;
    protected OutputBuffer ob;
    protected CoyoteOutputStream ( final OutputBuffer ob ) {
        this.ob = ob;
    }
    protected Object clone() throws CloneNotSupportedException {
        throw new CloneNotSupportedException();
    }
    void clear() {
        this.ob = null;
    }
    public void write ( final int i ) throws IOException {
        final boolean nonBlocking = this.checkNonBlockingWrite();
        this.ob.writeByte ( i );
        if ( nonBlocking ) {
            this.checkRegisterForWrite();
        }
    }
    public void write ( final byte[] b ) throws IOException {
        this.write ( b, 0, b.length );
    }
    public void write ( final byte[] b, final int off, final int len ) throws IOException {
        final boolean nonBlocking = this.checkNonBlockingWrite();
        this.ob.write ( b, off, len );
        if ( nonBlocking ) {
            this.checkRegisterForWrite();
        }
    }
    public void write ( final ByteBuffer from ) throws IOException {
        final boolean nonBlocking = this.checkNonBlockingWrite();
        this.ob.write ( from );
        if ( nonBlocking ) {
            this.checkRegisterForWrite();
        }
    }
    public void flush() throws IOException {
        final boolean nonBlocking = this.checkNonBlockingWrite();
        this.ob.flush();
        if ( nonBlocking ) {
            this.checkRegisterForWrite();
        }
    }
    private boolean checkNonBlockingWrite() {
        final boolean nonBlocking = !this.ob.isBlocking();
        if ( nonBlocking && !this.ob.isReady() ) {
            throw new IllegalStateException ( CoyoteOutputStream.sm.getString ( "coyoteOutputStream.nbNotready" ) );
        }
        return nonBlocking;
    }
    private void checkRegisterForWrite() {
        this.ob.checkRegisterForWrite();
    }
    public void close() throws IOException {
        this.ob.close();
    }
    public boolean isReady() {
        return this.ob.isReady();
    }
    public void setWriteListener ( final WriteListener listener ) {
        this.ob.setWriteListener ( listener );
    }
    static {
        sm = StringManager.getManager ( CoyoteOutputStream.class );
    }
}
