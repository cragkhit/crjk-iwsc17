package org.apache.catalina.connector;
import javax.servlet.ReadListener;
import java.nio.ByteBuffer;
import java.security.PrivilegedActionException;
import java.security.AccessController;
import java.io.IOException;
import java.security.PrivilegedExceptionAction;
import org.apache.catalina.security.SecurityUtil;
import org.apache.tomcat.util.res.StringManager;
import javax.servlet.ServletInputStream;
public class CoyoteInputStream extends ServletInputStream {
    protected static final StringManager sm;
    protected InputBuffer ib;
    protected CoyoteInputStream ( final InputBuffer ib ) {
        this.ib = ib;
    }
    void clear() {
        this.ib = null;
    }
    protected Object clone() throws CloneNotSupportedException {
        throw new CloneNotSupportedException();
    }
    public int read() throws IOException {
        this.checkNonBlockingRead();
        if ( SecurityUtil.isPackageProtectionEnabled() ) {
            try {
                final Integer result = AccessController.doPrivileged ( ( PrivilegedExceptionAction<Integer> ) new PrivilegedExceptionAction<Integer>() {
                    @Override
                    public Integer run() throws IOException {
                        final Integer integer = CoyoteInputStream.this.ib.readByte();
                        return integer;
                    }
                } );
                return result;
            } catch ( PrivilegedActionException pae ) {
                final Exception e = pae.getException();
                if ( e instanceof IOException ) {
                    throw ( IOException ) e;
                }
                throw new RuntimeException ( e.getMessage(), e );
            }
        }
        return this.ib.readByte();
    }
    public int available() throws IOException {
        if ( SecurityUtil.isPackageProtectionEnabled() ) {
            try {
                final Integer result = AccessController.doPrivileged ( ( PrivilegedExceptionAction<Integer> ) new PrivilegedExceptionAction<Integer>() {
                    @Override
                    public Integer run() throws IOException {
                        final Integer integer = CoyoteInputStream.this.ib.available();
                        return integer;
                    }
                } );
                return result;
            } catch ( PrivilegedActionException pae ) {
                final Exception e = pae.getException();
                if ( e instanceof IOException ) {
                    throw ( IOException ) e;
                }
                throw new RuntimeException ( e.getMessage(), e );
            }
        }
        return this.ib.available();
    }
    public int read ( final byte[] b ) throws IOException {
        this.checkNonBlockingRead();
        if ( SecurityUtil.isPackageProtectionEnabled() ) {
            try {
                final Integer result = AccessController.doPrivileged ( ( PrivilegedExceptionAction<Integer> ) new PrivilegedExceptionAction<Integer>() {
                    @Override
                    public Integer run() throws IOException {
                        final Integer integer = CoyoteInputStream.this.ib.read ( b, 0, b.length );
                        return integer;
                    }
                } );
                return result;
            } catch ( PrivilegedActionException pae ) {
                final Exception e = pae.getException();
                if ( e instanceof IOException ) {
                    throw ( IOException ) e;
                }
                throw new RuntimeException ( e.getMessage(), e );
            }
        }
        return this.ib.read ( b, 0, b.length );
    }
    public int read ( final byte[] b, final int off, final int len ) throws IOException {
        this.checkNonBlockingRead();
        if ( SecurityUtil.isPackageProtectionEnabled() ) {
            try {
                final Integer result = AccessController.doPrivileged ( ( PrivilegedExceptionAction<Integer> ) new PrivilegedExceptionAction<Integer>() {
                    @Override
                    public Integer run() throws IOException {
                        final Integer integer = CoyoteInputStream.this.ib.read ( b, off, len );
                        return integer;
                    }
                } );
                return result;
            } catch ( PrivilegedActionException pae ) {
                final Exception e = pae.getException();
                if ( e instanceof IOException ) {
                    throw ( IOException ) e;
                }
                throw new RuntimeException ( e.getMessage(), e );
            }
        }
        return this.ib.read ( b, off, len );
    }
    public int read ( final ByteBuffer b ) throws IOException {
        this.checkNonBlockingRead();
        if ( SecurityUtil.isPackageProtectionEnabled() ) {
            try {
                final Integer result = AccessController.doPrivileged ( ( PrivilegedExceptionAction<Integer> ) new PrivilegedExceptionAction<Integer>() {
                    @Override
                    public Integer run() throws IOException {
                        final Integer integer = CoyoteInputStream.this.ib.read ( b );
                        return integer;
                    }
                } );
                return result;
            } catch ( PrivilegedActionException pae ) {
                final Exception e = pae.getException();
                if ( e instanceof IOException ) {
                    throw ( IOException ) e;
                }
                throw new RuntimeException ( e.getMessage(), e );
            }
        }
        return this.ib.read ( b );
    }
    public int readLine ( final byte[] b, final int off, final int len ) throws IOException {
        return super.readLine ( b, off, len );
    }
    public void close() throws IOException {
        if ( SecurityUtil.isPackageProtectionEnabled() ) {
            try {
                AccessController.doPrivileged ( ( PrivilegedExceptionAction<Object> ) new PrivilegedExceptionAction<Void>() {
                    @Override
                    public Void run() throws IOException {
                        CoyoteInputStream.this.ib.close();
                        return null;
                    }
                } );
                return;
            } catch ( PrivilegedActionException pae ) {
                final Exception e = pae.getException();
                if ( e instanceof IOException ) {
                    throw ( IOException ) e;
                }
                throw new RuntimeException ( e.getMessage(), e );
            }
        }
        this.ib.close();
    }
    public boolean isFinished() {
        return this.ib.isFinished();
    }
    public boolean isReady() {
        return this.ib.isReady();
    }
    public void setReadListener ( final ReadListener listener ) {
        this.ib.setReadListener ( listener );
    }
    private void checkNonBlockingRead() {
        if ( !this.ib.isBlocking() && !this.ib.isReady() ) {
            throw new IllegalStateException ( CoyoteInputStream.sm.getString ( "coyoteInputStream.nbNotready" ) );
        }
    }
    static {
        sm = StringManager.getManager ( CoyoteInputStream.class );
    }
}
