package org.apache.tomcat.util.net;
import java.util.Iterator;
import java.util.Map;
import java.io.IOException;
import java.net.SocketTimeoutException;
import java.util.concurrent.atomic.AtomicInteger;
import org.apache.tomcat.util.ExceptionUtils;
import org.apache.tomcat.jni.Error;
import org.apache.tomcat.jni.File;
import org.apache.tomcat.jni.Socket;
import org.apache.tomcat.jni.Poll;
import org.apache.tomcat.jni.OS;
import org.apache.tomcat.jni.Pool;
import java.util.ArrayList;
import java.util.HashMap;
public class Sendfile implements Runnable {
    protected long sendfilePollset;
    protected long pool;
    protected long[] desc;
    protected HashMap<Long, SendfileData> sendfileData;
    protected int sendfileCount;
    protected ArrayList<SendfileData> addS;
    private volatile boolean sendfileRunning;
    public Sendfile() {
        this.sendfilePollset = 0L;
        this.pool = 0L;
        this.sendfileRunning = true;
    }
    public int getSendfileCount() {
        return this.sendfileCount;
    }
    protected void init() {
        this.pool = Pool.create ( AprEndpoint.this.serverSockPool );
        int size = AprEndpoint.this.sendfileSize;
        if ( size <= 0 ) {
            size = ( ( OS.IS_WIN32 || OS.IS_WIN64 ) ? 1024 : 16384 );
        }
        this.sendfilePollset = AprEndpoint.this.allocatePoller ( size, this.pool, AprEndpoint.this.getConnectionTimeout() );
        if ( this.sendfilePollset == 0L && size > 1024 ) {
            size = 1024;
            this.sendfilePollset = AprEndpoint.this.allocatePoller ( size, this.pool, AprEndpoint.this.getConnectionTimeout() );
        }
        if ( this.sendfilePollset == 0L ) {
            size = 62;
            this.sendfilePollset = AprEndpoint.this.allocatePoller ( size, this.pool, AprEndpoint.this.getConnectionTimeout() );
        }
        this.desc = new long[size * 2];
        this.sendfileData = new HashMap<Long, SendfileData> ( size );
        this.addS = new ArrayList<SendfileData>();
    }
    protected void destroy() {
        this.sendfileRunning = false;
        try {
            synchronized ( this ) {
                this.notify();
                this.wait ( AprEndpoint.this.pollTime / 1000 );
            }
        } catch ( InterruptedException ex ) {}
        for ( int i = this.addS.size() - 1; i >= 0; --i ) {
            final SendfileData data = this.addS.get ( i );
            AprEndpoint.access$000 ( AprEndpoint.this, data.socket );
        }
        final int rv = Poll.pollset ( this.sendfilePollset, this.desc );
        if ( rv > 0 ) {
            for ( int n = 0; n < rv; ++n ) {
                AprEndpoint.access$000 ( AprEndpoint.this, this.desc[n * 2 + 1] );
            }
        }
        Pool.destroy ( this.pool );
        this.sendfileData.clear();
    }
    public SendfileState add ( final SendfileData data ) {
        try {
            data.fdpool = Socket.pool ( data.socket );
            data.fd = File.open ( data.fileName, 4129, 0, data.fdpool );
            Socket.timeoutSet ( data.socket, 0L );
            while ( true ) {
                final long nw = Socket.sendfilen ( data.socket, data.fd, data.pos, data.length, 0 );
                if ( nw < 0L ) {
                    if ( -nw != 120002L ) {
                        Pool.destroy ( data.fdpool );
                        data.socket = 0L;
                        return SendfileState.ERROR;
                    }
                    break;
                } else {
                    data.pos += nw;
                    data.length -= nw;
                    if ( data.length == 0L ) {
                        Pool.destroy ( data.fdpool );
                        Socket.timeoutSet ( data.socket, AprEndpoint.this.getConnectionTimeout() * 1000 );
                        return SendfileState.DONE;
                    }
                    continue;
                }
            }
        } catch ( Exception e ) {
            AprEndpoint.access$200().warn ( AbstractEndpoint.sm.getString ( "endpoint.sendfile.error" ), e );
            return SendfileState.ERROR;
        }
        synchronized ( this ) {
            this.addS.add ( data );
            this.notify();
        }
        return SendfileState.PENDING;
    }
    protected void remove ( final SendfileData data ) {
        final int rv = Poll.remove ( this.sendfilePollset, data.socket );
        if ( rv == 0 ) {
            --this.sendfileCount;
        }
        this.sendfileData.remove ( data.socket );
    }
    @Override
    public void run() {
        long maintainTime = 0L;
        while ( this.sendfileRunning ) {
            while ( this.sendfileRunning && AprEndpoint.this.paused ) {
                try {
                    Thread.sleep ( 1000L );
                } catch ( InterruptedException ex ) {}
            }
            while ( this.sendfileRunning && this.sendfileCount < 1 && this.addS.size() < 1 ) {
                maintainTime = 0L;
                try {
                    synchronized ( this ) {
                        this.wait();
                    }
                } catch ( InterruptedException ex2 ) {}
            }
            if ( !this.sendfileRunning ) {
                break;
            }
            try {
                if ( this.addS.size() > 0 ) {
                    synchronized ( this ) {
                        for ( int i = this.addS.size() - 1; i >= 0; --i ) {
                            final SendfileData data = this.addS.get ( i );
                            final int rv = Poll.add ( this.sendfilePollset, data.socket, 4 );
                            if ( rv == 0 ) {
                                this.sendfileData.put ( data.socket, data );
                                ++this.sendfileCount;
                            } else {
                                AprEndpoint.this.getLog().warn ( AbstractEndpoint.sm.getString ( "endpoint.sendfile.addfail", rv, Error.strerror ( rv ) ) );
                                AprEndpoint.access$000 ( AprEndpoint.this, data.socket );
                            }
                        }
                        this.addS.clear();
                    }
                }
                maintainTime += AprEndpoint.this.pollTime;
                int rv2 = Poll.poll ( this.sendfilePollset, AprEndpoint.this.pollTime, this.desc, false );
                if ( rv2 > 0 ) {
                    for ( int n = 0; n < rv2; ++n ) {
                        final SendfileData state = this.sendfileData.get ( this.desc[n * 2 + 1] );
                        if ( ( this.desc[n * 2] & 0x20L ) == 0x20L || ( this.desc[n * 2] & 0x10L ) == 0x10L ) {
                            this.remove ( state );
                            AprEndpoint.access$000 ( AprEndpoint.this, state.socket );
                        } else {
                            final long nw = Socket.sendfilen ( state.socket, state.fd, state.pos, state.length, 0 );
                            if ( nw < 0L ) {
                                this.remove ( state );
                                AprEndpoint.access$000 ( AprEndpoint.this, state.socket );
                            } else {
                                final SendfileData sendfileData = state;
                                sendfileData.pos += nw;
                                final SendfileData sendfileData2 = state;
                                sendfileData2.length -= nw;
                                if ( state.length == 0L ) {
                                    this.remove ( state );
                                    if ( state.keepAlive ) {
                                        Pool.destroy ( state.fdpool );
                                        Socket.timeoutSet ( state.socket, AprEndpoint.this.getConnectionTimeout() * 1000 );
                                        AprEndpoint.this.getPoller().add ( state.socket, AprEndpoint.this.getKeepAliveTimeout(), 1 );
                                    } else {
                                        AprEndpoint.access$000 ( AprEndpoint.this, state.socket );
                                    }
                                }
                            }
                        }
                    }
                } else if ( rv2 < 0 ) {
                    int errn = -rv2;
                    if ( errn != 120001 && errn != 120003 ) {
                        if ( errn > 120000 ) {
                            errn -= 120000;
                        }
                        AprEndpoint.this.getLog().error ( AbstractEndpoint.sm.getString ( "endpoint.apr.pollError", errn, Error.strerror ( errn ) ) );
                        synchronized ( this ) {
                            this.destroy();
                            this.init();
                        }
                        continue;
                    }
                }
                if ( AprEndpoint.this.getConnectionTimeout() <= 0 || maintainTime <= 1000000L || !this.sendfileRunning ) {
                    continue;
                }
                rv2 = Poll.maintain ( this.sendfilePollset, this.desc, false );
                maintainTime = 0L;
                if ( rv2 <= 0 ) {
                    continue;
                }
                for ( int n = 0; n < rv2; ++n ) {
                    final SendfileData state = this.sendfileData.get ( this.desc[n] );
                    this.remove ( state );
                    AprEndpoint.access$000 ( AprEndpoint.this, state.socket );
                }
            } catch ( Throwable t ) {
                ExceptionUtils.handleThrowable ( t );
                AprEndpoint.this.getLog().error ( AbstractEndpoint.sm.getString ( "endpoint.poll.error" ), t );
            }
        }
        synchronized ( this ) {
            this.notifyAll();
        }
    }
}
