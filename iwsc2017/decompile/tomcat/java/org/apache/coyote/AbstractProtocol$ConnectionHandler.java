package org.apache.coyote;
import java.util.Iterator;
import org.apache.tomcat.util.modeler.Registry;
import javax.management.ObjectName;
import java.util.Set;
import org.apache.tomcat.InstanceManager;
import javax.servlet.http.HttpUpgradeHandler;
import java.nio.ByteBuffer;
import java.io.IOException;
import java.net.SocketException;
import org.apache.tomcat.util.ExceptionUtils;
import javax.servlet.http.WebConnection;
import org.apache.tomcat.util.net.AbstractEndpoint.Handler;
import org.apache.tomcat.util.net.SocketEvent;
import org.apache.tomcat.util.net.SocketWrapperBase;
import org.apache.juli.logging.Log;
import java.util.concurrent.ConcurrentHashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;
import org.apache.tomcat.util.net.AbstractEndpoint;
protected static class ConnectionHandler<S> implements AbstractEndpoint.Handler<S> {
    private final AbstractProtocol<S> proto;
    private final RequestGroupInfo global;
    private final AtomicLong registerCount;
    private final Map<S, Processor> connections;
    private final RecycledProcessors recycledProcessors;
    public ConnectionHandler ( final AbstractProtocol<S> proto ) {
        this.global = new RequestGroupInfo();
        this.registerCount = new AtomicLong ( 0L );
        this.connections = new ConcurrentHashMap<S, Processor>();
        this.recycledProcessors = new RecycledProcessors ( this );
        this.proto = proto;
    }
    protected AbstractProtocol<S> getProtocol() {
        return this.proto;
    }
    protected Log getLog() {
        return this.getProtocol().getLog();
    }
    @Override
    public Object getGlobal() {
        return this.global;
    }
    @Override
    public void recycle() {
        this.recycledProcessors.clear();
    }
    @Override
    public SocketState process ( final SocketWrapperBase<S> wrapper, final SocketEvent status ) {
        if ( this.getLog().isDebugEnabled() ) {
            this.getLog().debug ( AbstractProtocol.access$000().getString ( "abstractConnectionHandler.process", wrapper.getSocket(), status ) );
        }
        if ( wrapper == null ) {
            return SocketState.CLOSED;
        }
        final S socket = wrapper.getSocket();
        Processor processor = this.connections.get ( socket );
        if ( ( status == SocketEvent.DISCONNECT || status == SocketEvent.ERROR ) && processor == null ) {
            return SocketState.CLOSED;
        }
        ContainerThreadMarker.set();
        try {
            if ( processor == null ) {
                final String negotiatedProtocol = wrapper.getNegotiatedProtocol();
                if ( negotiatedProtocol != null ) {
                    final UpgradeProtocol upgradeProtocol = this.getProtocol().getNegotiatedProtocol ( negotiatedProtocol );
                    if ( upgradeProtocol != null ) {
                        processor = upgradeProtocol.getProcessor ( wrapper, this.getProtocol().getAdapter() );
                    } else if ( !negotiatedProtocol.equals ( "http/1.1" ) ) {
                        if ( this.getLog().isDebugEnabled() ) {
                            this.getLog().debug ( AbstractProtocol.access$000().getString ( "abstractConnectionHandler.negotiatedProcessor.fail", negotiatedProtocol ) );
                        }
                        return SocketState.CLOSED;
                    }
                }
            }
            if ( processor == null ) {
                processor = this.recycledProcessors.pop();
            }
            if ( processor == null ) {
                processor = this.getProtocol().createProcessor();
                this.register ( processor );
            }
            processor.setSslSupport ( wrapper.getSslSupport ( this.getProtocol().getClientCertProvider() ) );
            this.connections.put ( socket, processor );
            this.getProtocol().removeWaitingProcessor ( processor );
            SocketState state = SocketState.CLOSED;
            do {
                state = processor.process ( wrapper, status );
                if ( state == SocketState.UPGRADING ) {
                    final UpgradeToken upgradeToken = processor.getUpgradeToken();
                    final ByteBuffer leftOverInput = processor.getLeftoverInput();
                    if ( upgradeToken == null ) {
                        final UpgradeProtocol upgradeProtocol2 = this.getProtocol().getUpgradeProtocol ( "h2c" );
                        if ( upgradeProtocol2 == null ) {
                            if ( this.getLog().isDebugEnabled() ) {
                                this.getLog().debug ( AbstractProtocol.access$000().getString ( "abstractConnectionHandler.negotiatedProcessor.fail", "h2c" ) );
                            }
                            return SocketState.CLOSED;
                        }
                        processor = upgradeProtocol2.getProcessor ( wrapper, this.getProtocol().getAdapter() );
                        wrapper.unRead ( leftOverInput );
                        this.connections.put ( socket, processor );
                    } else {
                        final HttpUpgradeHandler httpUpgradeHandler = upgradeToken.getHttpUpgradeHandler();
                        this.release ( processor );
                        processor = this.getProtocol().createUpgradeProcessor ( wrapper, upgradeToken );
                        wrapper.unRead ( leftOverInput );
                        wrapper.setUpgraded ( true );
                        this.connections.put ( socket, processor );
                        if ( upgradeToken.getInstanceManager() == null ) {
                            httpUpgradeHandler.init ( ( WebConnection ) processor );
                        } else {
                            final ClassLoader oldCL = upgradeToken.getContextBind().bind ( false, null );
                            try {
                                httpUpgradeHandler.init ( ( WebConnection ) processor );
                            } finally {
                                upgradeToken.getContextBind().unbind ( false, oldCL );
                            }
                        }
                    }
                }
            } while ( state == SocketState.UPGRADING );
            if ( state == SocketState.LONG ) {
                this.longPoll ( wrapper, processor );
                if ( processor.isAsync() ) {
                    this.getProtocol().addWaitingProcessor ( processor );
                }
            } else if ( state == SocketState.OPEN ) {
                this.connections.remove ( socket );
                this.release ( processor );
                wrapper.registerReadInterest();
            } else if ( state == SocketState.SENDFILE ) {
                this.connections.remove ( socket );
                this.release ( processor );
            } else if ( state == SocketState.UPGRADED ) {
                if ( status != SocketEvent.OPEN_WRITE ) {
                    this.longPoll ( wrapper, processor );
                }
            } else {
                this.connections.remove ( socket );
                if ( processor.isUpgrade() ) {
                    final UpgradeToken upgradeToken = processor.getUpgradeToken();
                    final HttpUpgradeHandler httpUpgradeHandler2 = upgradeToken.getHttpUpgradeHandler();
                    final InstanceManager instanceManager = upgradeToken.getInstanceManager();
                    if ( instanceManager == null ) {
                        httpUpgradeHandler2.destroy();
                    } else {
                        final ClassLoader oldCL = upgradeToken.getContextBind().bind ( false, null );
                        try {
                            httpUpgradeHandler2.destroy();
                        } finally {
                            try {
                                instanceManager.destroyInstance ( httpUpgradeHandler2 );
                            } catch ( Throwable e ) {
                                ExceptionUtils.handleThrowable ( e );
                                this.getLog().error ( AbstractProtocol.access$000().getString ( "abstractConnectionHandler.error" ), e );
                            }
                            upgradeToken.getContextBind().unbind ( false, oldCL );
                        }
                    }
                } else {
                    this.release ( processor );
                }
            }
            return state;
        } catch ( SocketException e2 ) {
            this.getLog().debug ( AbstractProtocol.access$000().getString ( "abstractConnectionHandler.socketexception.debug" ), e2 );
        } catch ( IOException e3 ) {
            this.getLog().debug ( AbstractProtocol.access$000().getString ( "abstractConnectionHandler.ioexception.debug" ), e3 );
        } catch ( ProtocolException e4 ) {
            this.getLog().debug ( AbstractProtocol.access$000().getString ( "abstractConnectionHandler.protocolexception.debug" ), e4 );
        } catch ( Throwable e5 ) {
            ExceptionUtils.handleThrowable ( e5 );
            this.getLog().error ( AbstractProtocol.access$000().getString ( "abstractConnectionHandler.error" ), e5 );
        } finally {
            ContainerThreadMarker.clear();
        }
        this.connections.remove ( socket );
        this.release ( processor );
        return SocketState.CLOSED;
    }
    protected void longPoll ( final SocketWrapperBase<?> socket, final Processor processor ) {
        if ( !processor.isAsync() ) {
            socket.registerReadInterest();
        }
    }
    @Override
    public Set<S> getOpenSockets() {
        return this.connections.keySet();
    }
    private void release ( final Processor processor ) {
        if ( processor != null ) {
            processor.recycle();
            if ( !processor.isUpgrade() ) {
                this.recycledProcessors.push ( processor );
            }
        }
    }
    @Override
    public void release ( final SocketWrapperBase<S> socketWrapper ) {
        final S socket = socketWrapper.getSocket();
        final Processor processor = this.connections.remove ( socket );
        this.release ( processor );
    }
    protected void register ( final Processor processor ) {
        if ( this.getProtocol().getDomain() != null ) {
            synchronized ( this ) {
                try {
                    final long count = this.registerCount.incrementAndGet();
                    final RequestInfo rp = processor.getRequest().getRequestProcessor();
                    rp.setGlobalProcessor ( this.global );
                    final ObjectName rpName = new ObjectName ( this.getProtocol().getDomain() + ":type=RequestProcessor,worker=" + this.getProtocol().getName() + ",name=" + this.getProtocol().getProtocolName() + "Request" + count );
                    if ( this.getLog().isDebugEnabled() ) {
                        this.getLog().debug ( "Register " + rpName );
                    }
                    Registry.getRegistry ( null, null ).registerComponent ( rp, rpName, null );
                    rp.setRpName ( rpName );
                } catch ( Exception e ) {
                    this.getLog().warn ( "Error registering request" );
                }
            }
        }
    }
    protected void unregister ( final Processor processor ) {
        if ( this.getProtocol().getDomain() != null ) {
            synchronized ( this ) {
                try {
                    final Request r = processor.getRequest();
                    if ( r == null ) {
                        return;
                    }
                    final RequestInfo rp = r.getRequestProcessor();
                    rp.setGlobalProcessor ( null );
                    final ObjectName rpName = rp.getRpName();
                    if ( this.getLog().isDebugEnabled() ) {
                        this.getLog().debug ( "Unregister " + rpName );
                    }
                    Registry.getRegistry ( null, null ).unregisterComponent ( rpName );
                    rp.setRpName ( null );
                } catch ( Exception e ) {
                    this.getLog().warn ( "Error unregistering request", e );
                }
            }
        }
    }
    @Override
    public final void pause() {
        for ( final Processor processor : this.connections.values() ) {
            processor.pause();
        }
    }
}
