package org.apache.catalina.connector;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.security.AccessController;
import java.security.PrivilegedActionException;
import java.security.PrivilegedExceptionAction;
import javax.servlet.ReadListener;
import javax.servlet.ServletInputStream;
import org.apache.catalina.security.SecurityUtil;
import org.apache.tomcat.util.res.StringManager;
public class CoyoteInputStream extends ServletInputStream {
    protected static final StringManager sm = StringManager.getManager ( CoyoteInputStream.class );
    protected InputBuffer ib;
    protected CoyoteInputStream ( InputBuffer ib ) {
        this.ib = ib;
    }
    void clear() {
        ib = null;
    }
    @Override
    protected Object clone() throws CloneNotSupportedException {
        throw new CloneNotSupportedException();
    }
    @Override
    public int read() throws IOException {
        checkNonBlockingRead();
        if ( SecurityUtil.isPackageProtectionEnabled() ) {
            try {
                Integer result = AccessController
                .doPrivileged ( new PrivilegedExceptionAction<Integer>() {
                    @Override
                    public Integer run() throws IOException {
                        Integer integer = Integer.valueOf ( ib.readByte() );
                        return integer;
                    }
                } );
                return result.intValue();
            } catch ( PrivilegedActionException pae ) {
                Exception e = pae.getException();
                if ( e instanceof IOException ) {
                    throw ( IOException ) e;
                } else {
                    throw new RuntimeException ( e.getMessage(), e );
                }
            }
        } else {
            return ib.readByte();
        }
    }
    @Override
    public int available() throws IOException {
        if ( SecurityUtil.isPackageProtectionEnabled() ) {
            try {
                Integer result = AccessController
                .doPrivileged ( new PrivilegedExceptionAction<Integer>() {
                    @Override
                    public Integer run() throws IOException {
                        Integer integer = Integer.valueOf ( ib.available() );
                        return integer;
                    }
                } );
                return result.intValue();
            } catch ( PrivilegedActionException pae ) {
                Exception e = pae.getException();
                if ( e instanceof IOException ) {
                    throw ( IOException ) e;
                } else {
                    throw new RuntimeException ( e.getMessage(), e );
                }
            }
        } else {
            return ib.available();
        }
    }
    @Override
    public int read ( final byte[] b ) throws IOException {
        checkNonBlockingRead();
        if ( SecurityUtil.isPackageProtectionEnabled() ) {
            try {
                Integer result = AccessController
                .doPrivileged ( new PrivilegedExceptionAction<Integer>() {
                    @Override
                    public Integer run() throws IOException {
                        Integer integer = Integer.valueOf ( ib.read ( b, 0, b.length ) );
                        return integer;
                    }
                } );
                return result.intValue();
            } catch ( PrivilegedActionException pae ) {
                Exception e = pae.getException();
                if ( e instanceof IOException ) {
                    throw ( IOException ) e;
                } else {
                    throw new RuntimeException ( e.getMessage(), e );
                }
            }
        } else {
            return ib.read ( b, 0, b.length );
        }
    }
    @Override
    public int read ( final byte[] b, final int off, final int len ) throws IOException {
        checkNonBlockingRead();
        if ( SecurityUtil.isPackageProtectionEnabled() ) {
            try {
                Integer result = AccessController
                .doPrivileged ( new PrivilegedExceptionAction<Integer>() {
                    @Override
                    public Integer run() throws IOException {
                        Integer integer = Integer.valueOf ( ib.read ( b, off, len ) );
                        return integer;
                    }
                } );
                return result.intValue();
            } catch ( PrivilegedActionException pae ) {
                Exception e = pae.getException();
                if ( e instanceof IOException ) {
                    throw ( IOException ) e;
                } else {
                    throw new RuntimeException ( e.getMessage(), e );
                }
            }
        } else {
            return ib.read ( b, off, len );
        }
    }
    public int read ( final ByteBuffer b ) throws IOException {
        checkNonBlockingRead();
        if ( SecurityUtil.isPackageProtectionEnabled() ) {
            try {
                Integer result = AccessController
                .doPrivileged ( new PrivilegedExceptionAction<Integer>() {
                    @Override
                    public Integer run() throws IOException {
                        Integer integer = Integer.valueOf ( ib.read ( b ) );
                        return integer;
                    }
                } );
                return result.intValue();
            } catch ( PrivilegedActionException pae ) {
                Exception e = pae.getException();
                if ( e instanceof IOException ) {
                    throw ( IOException ) e;
                } else {
                    throw new RuntimeException ( e.getMessage(), e );
                }
            }
        } else {
            return ib.read ( b );
        }
    }
    @Override
    public int readLine ( byte[] b, int off, int len ) throws IOException {
        return super.readLine ( b, off, len );
    }
    @Override
    public void close() throws IOException {
        if ( SecurityUtil.isPackageProtectionEnabled() ) {
            try {
                AccessController.doPrivileged ( new PrivilegedExceptionAction<Void>() {
                    @Override
                    public Void run() throws IOException {
                        ib.close();
                        return null;
                    }
                } );
            } catch ( PrivilegedActionException pae ) {
                Exception e = pae.getException();
                if ( e instanceof IOException ) {
                    throw ( IOException ) e;
                } else {
                    throw new RuntimeException ( e.getMessage(), e );
                }
            }
        } else {
            ib.close();
        }
    }
    @Override
    public boolean isFinished() {
        return ib.isFinished();
    }
    @Override
    public boolean isReady() {
        return ib.isReady();
    }
    @Override
    public void setReadListener ( ReadListener listener ) {
        ib.setReadListener ( listener );
    }
    private void checkNonBlockingRead() {
        if ( !ib.isBlocking() && !ib.isReady() ) {
            throw new IllegalStateException ( sm.getString ( "coyoteInputStream.nbNotready" ) );
        }
    }
}
