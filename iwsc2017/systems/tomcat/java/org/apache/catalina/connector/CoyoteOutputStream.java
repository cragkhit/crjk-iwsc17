package org.apache.catalina.connector;
import java.io.IOException;
import java.nio.ByteBuffer;
import javax.servlet.ServletOutputStream;
import javax.servlet.WriteListener;
import org.apache.tomcat.util.res.StringManager;
public class CoyoteOutputStream extends ServletOutputStream {
    protected static final StringManager sm = StringManager.getManager ( CoyoteOutputStream.class );
    protected OutputBuffer ob;
    protected CoyoteOutputStream ( OutputBuffer ob ) {
        this.ob = ob;
    }
    @Override
    protected Object clone() throws CloneNotSupportedException {
        throw new CloneNotSupportedException();
    }
    void clear() {
        ob = null;
    }
    @Override
    public void write ( int i ) throws IOException {
        boolean nonBlocking = checkNonBlockingWrite();
        ob.writeByte ( i );
        if ( nonBlocking ) {
            checkRegisterForWrite();
        }
    }
    @Override
    public void write ( byte[] b ) throws IOException {
        write ( b, 0, b.length );
    }
    @Override
    public void write ( byte[] b, int off, int len ) throws IOException {
        boolean nonBlocking = checkNonBlockingWrite();
        ob.write ( b, off, len );
        if ( nonBlocking ) {
            checkRegisterForWrite();
        }
    }
    public void write ( ByteBuffer from ) throws IOException {
        boolean nonBlocking = checkNonBlockingWrite();
        ob.write ( from );
        if ( nonBlocking ) {
            checkRegisterForWrite();
        }
    }
    @Override
    public void flush() throws IOException {
        boolean nonBlocking = checkNonBlockingWrite();
        ob.flush();
        if ( nonBlocking ) {
            checkRegisterForWrite();
        }
    }
    private boolean checkNonBlockingWrite() {
        boolean nonBlocking = !ob.isBlocking();
        if ( nonBlocking && !ob.isReady() ) {
            throw new IllegalStateException ( sm.getString ( "coyoteOutputStream.nbNotready" ) );
        }
        return nonBlocking;
    }
    private void checkRegisterForWrite() {
        ob.checkRegisterForWrite();
    }
    @Override
    public void close() throws IOException {
        ob.close();
    }
    @Override
    public boolean isReady() {
        return ob.isReady();
    }
    @Override
    public void setWriteListener ( WriteListener listener ) {
        ob.setWriteListener ( listener );
    }
}
