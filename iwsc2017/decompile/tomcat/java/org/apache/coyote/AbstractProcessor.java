package org.apache.coyote;
import java.nio.ByteBuffer;
import org.apache.tomcat.util.net.DispatchType;
import java.util.concurrent.atomic.AtomicBoolean;
import org.apache.tomcat.util.buf.ByteChunk;
import org.apache.tomcat.util.ExceptionUtils;
import java.io.InterruptedIOException;
import java.io.IOException;
import java.util.concurrent.Executor;
import org.apache.tomcat.util.net.SocketEvent;
import org.apache.tomcat.util.net.SSLSupport;
import org.apache.tomcat.util.net.SocketWrapperBase;
import org.apache.tomcat.util.net.AbstractEndpoint;
import org.apache.tomcat.util.res.StringManager;
public abstract class AbstractProcessor extends AbstractProcessorLight implements ActionHook {
    private static final StringManager sm;
    protected Adapter adapter;
    protected final AsyncStateMachine asyncStateMachine;
    private volatile long asyncTimeout;
    protected final AbstractEndpoint<?> endpoint;
    protected final Request request;
    protected final Response response;
    protected volatile SocketWrapperBase<?> socketWrapper;
    protected volatile SSLSupport sslSupport;
    private ErrorState errorState;
    protected AbstractProcessor ( final Request coyoteRequest, final Response coyoteResponse ) {
        this ( null, coyoteRequest, coyoteResponse );
    }
    public AbstractProcessor ( final AbstractEndpoint<?> endpoint ) {
        this ( endpoint, new Request(), new Response() );
    }
    private AbstractProcessor ( final AbstractEndpoint<?> endpoint, final Request coyoteRequest, final Response coyoteResponse ) {
        this.asyncTimeout = -1L;
        this.socketWrapper = null;
        this.errorState = ErrorState.NONE;
        this.endpoint = endpoint;
        this.asyncStateMachine = new AsyncStateMachine ( this );
        this.request = coyoteRequest;
        ( this.response = coyoteResponse ).setHook ( this );
        this.request.setResponse ( this.response );
        this.request.setHook ( this );
    }
    protected void setErrorState ( final ErrorState errorState, final Throwable t ) {
        final boolean blockIo = this.errorState.isIoAllowed() && !errorState.isIoAllowed();
        this.errorState = this.errorState.getMostSevere ( errorState );
        if ( blockIo && !ContainerThreadMarker.isContainerThread() && this.isAsync() ) {
            if ( this.response.getStatus() < 400 ) {
                this.response.setStatus ( 500 );
            }
            this.getLog().info ( AbstractProcessor.sm.getString ( "abstractProcessor.nonContainerThreadError" ), t );
            this.request.setAttribute ( "javax.servlet.error.exception", t );
            this.socketWrapper.processSocket ( SocketEvent.ERROR, true );
        }
    }
    protected ErrorState getErrorState() {
        return this.errorState;
    }
    @Override
    public Request getRequest() {
        return this.request;
    }
    public void setAdapter ( final Adapter adapter ) {
        this.adapter = adapter;
    }
    public Adapter getAdapter() {
        return this.adapter;
    }
    protected final void setSocketWrapper ( final SocketWrapperBase<?> socketWrapper ) {
        this.socketWrapper = socketWrapper;
    }
    protected final SocketWrapperBase<?> getSocketWrapper() {
        return this.socketWrapper;
    }
    @Override
    public final void setSslSupport ( final SSLSupport sslSupport ) {
        this.sslSupport = sslSupport;
    }
    protected Executor getExecutor() {
        return this.endpoint.getExecutor();
    }
    @Override
    public boolean isAsync() {
        return this.asyncStateMachine.isAsync();
    }
    public AbstractEndpoint.Handler.SocketState asyncPostProcess() {
        return this.asyncStateMachine.asyncPostProcess();
    }
    public final AbstractEndpoint.Handler.SocketState dispatch ( SocketEvent status ) {
        if ( status == SocketEvent.OPEN_WRITE && this.response.getWriteListener() != null ) {
            this.asyncStateMachine.asyncOperation();
            try {
                if ( this.flushBufferedWrite() ) {
                    return AbstractEndpoint.Handler.SocketState.LONG;
                }
            } catch ( IOException ioe ) {
                if ( this.getLog().isDebugEnabled() ) {
                    this.getLog().debug ( "Unable to write async data.", ioe );
                }
                status = SocketEvent.ERROR;
                this.request.setAttribute ( "javax.servlet.error.exception", ioe );
            }
        } else if ( status == SocketEvent.OPEN_READ && this.request.getReadListener() != null ) {
            this.dispatchNonBlockingRead();
        } else if ( status == SocketEvent.ERROR ) {
            if ( this.request.getAttribute ( "javax.servlet.error.exception" ) == null ) {
                this.request.setAttribute ( "javax.servlet.error.exception", this.socketWrapper.getError() );
            }
            if ( this.request.getReadListener() != null || this.response.getWriteListener() != null ) {
                this.asyncStateMachine.asyncOperation();
            }
        }
        final RequestInfo rp = this.request.getRequestProcessor();
        try {
            rp.setStage ( 3 );
            if ( !this.getAdapter().asyncDispatch ( this.request, this.response, status ) ) {
                this.setErrorState ( ErrorState.CLOSE_NOW, null );
            }
        } catch ( InterruptedIOException e ) {
            this.setErrorState ( ErrorState.CLOSE_CONNECTION_NOW, e );
        } catch ( Throwable t ) {
            ExceptionUtils.handleThrowable ( t );
            this.setErrorState ( ErrorState.CLOSE_NOW, t );
            this.getLog().error ( AbstractProcessor.sm.getString ( "http11processor.request.process" ), t );
        }
        rp.setStage ( 7 );
        if ( this.getErrorState().isError() ) {
            this.request.updateCounters();
            return AbstractEndpoint.Handler.SocketState.CLOSED;
        }
        if ( this.isAsync() ) {
            return AbstractEndpoint.Handler.SocketState.LONG;
        }
        this.request.updateCounters();
        return this.dispatchEndRequest();
    }
    @Override
    public final void action ( final ActionCode actionCode, final Object param ) {
        switch ( actionCode ) {
        case COMMIT: {
            if ( !this.response.isCommitted() ) {
                try {
                    this.prepareResponse();
                } catch ( IOException e ) {
                    this.setErrorState ( ErrorState.CLOSE_CONNECTION_NOW, e );
                }
                break;
            }
            break;
        }
        case CLOSE: {
            this.action ( ActionCode.COMMIT, null );
            try {
                this.finishResponse();
            } catch ( CloseNowException cne ) {
                this.setErrorState ( ErrorState.CLOSE_NOW, cne );
            } catch ( IOException e ) {
                this.setErrorState ( ErrorState.CLOSE_CONNECTION_NOW, e );
            }
            break;
        }
        case ACK: {
            this.ack();
            break;
        }
        case CLIENT_FLUSH: {
            this.action ( ActionCode.COMMIT, null );
            try {
                this.flush();
            } catch ( IOException e ) {
                this.setErrorState ( ErrorState.CLOSE_CONNECTION_NOW, e );
                this.response.setErrorException ( e );
            }
            break;
        }
        case AVAILABLE: {
            this.request.setAvailable ( this.available ( Boolean.TRUE.equals ( param ) ) );
            break;
        }
        case REQ_SET_BODY_REPLAY: {
            final ByteChunk body = ( ByteChunk ) param;
            this.setRequestBody ( body );
            break;
        }
        case IS_ERROR: {
            ( ( AtomicBoolean ) param ).set ( this.getErrorState().isError() );
            break;
        }
        case CLOSE_NOW: {
            this.setSwallowResponse();
            if ( param instanceof Throwable ) {
                this.setErrorState ( ErrorState.CLOSE_NOW, ( Throwable ) param );
                break;
            }
            this.setErrorState ( ErrorState.CLOSE_NOW, null );
            break;
        }
        case DISABLE_SWALLOW_INPUT: {
            this.disableSwallowRequest();
            this.setErrorState ( ErrorState.CLOSE_CLEAN, null );
            break;
        }
        case REQ_HOST_ADDR_ATTRIBUTE: {
            if ( this.getPopulateRequestAttributesFromSocket() && this.socketWrapper != null ) {
                this.request.remoteAddr().setString ( this.socketWrapper.getRemoteAddr() );
                break;
            }
            break;
        }
        case REQ_HOST_ATTRIBUTE: {
            this.populateRequestAttributeRemoteHost();
            break;
        }
        case REQ_LOCALPORT_ATTRIBUTE: {
            if ( this.getPopulateRequestAttributesFromSocket() && this.socketWrapper != null ) {
                this.request.setLocalPort ( this.socketWrapper.getLocalPort() );
                break;
            }
            break;
        }
        case REQ_LOCAL_ADDR_ATTRIBUTE: {
            if ( this.getPopulateRequestAttributesFromSocket() && this.socketWrapper != null ) {
                this.request.localAddr().setString ( this.socketWrapper.getLocalAddr() );
                break;
            }
            break;
        }
        case REQ_LOCAL_NAME_ATTRIBUTE: {
            if ( this.getPopulateRequestAttributesFromSocket() && this.socketWrapper != null ) {
                this.request.localName().setString ( this.socketWrapper.getLocalName() );
                break;
            }
            break;
        }
        case REQ_REMOTEPORT_ATTRIBUTE: {
            if ( this.getPopulateRequestAttributesFromSocket() && this.socketWrapper != null ) {
                this.request.setRemotePort ( this.socketWrapper.getRemotePort() );
                break;
            }
            break;
        }
        case REQ_SSL_ATTRIBUTE: {
            this.populateSslRequestAttributes();
            break;
        }
        case REQ_SSL_CERTIFICATE: {
            this.sslReHandShake();
            break;
        }
        case ASYNC_START: {
            this.asyncStateMachine.asyncStart ( ( AsyncContextCallback ) param );
            break;
        }
        case ASYNC_COMPLETE: {
            this.clearDispatches();
            if ( this.asyncStateMachine.asyncComplete() ) {
                this.socketWrapper.processSocket ( SocketEvent.OPEN_READ, true );
                break;
            }
            break;
        }
        case ASYNC_DISPATCH: {
            if ( this.asyncStateMachine.asyncDispatch() ) {
                this.socketWrapper.processSocket ( SocketEvent.OPEN_READ, true );
                break;
            }
            break;
        }
        case ASYNC_DISPATCHED: {
            this.asyncStateMachine.asyncDispatched();
            break;
        }
        case ASYNC_ERROR: {
            this.asyncStateMachine.asyncError();
            break;
        }
        case ASYNC_IS_ASYNC: {
            ( ( AtomicBoolean ) param ).set ( this.asyncStateMachine.isAsync() );
            break;
        }
        case ASYNC_IS_COMPLETING: {
            ( ( AtomicBoolean ) param ).set ( this.asyncStateMachine.isCompleting() );
            break;
        }
        case ASYNC_IS_DISPATCHING: {
            ( ( AtomicBoolean ) param ).set ( this.asyncStateMachine.isAsyncDispatching() );
            break;
        }
        case ASYNC_IS_ERROR: {
            ( ( AtomicBoolean ) param ).set ( this.asyncStateMachine.isAsyncError() );
            break;
        }
        case ASYNC_IS_STARTED: {
            ( ( AtomicBoolean ) param ).set ( this.asyncStateMachine.isAsyncStarted() );
            break;
        }
        case ASYNC_IS_TIMINGOUT: {
            ( ( AtomicBoolean ) param ).set ( this.asyncStateMachine.isAsyncTimingOut() );
            break;
        }
        case ASYNC_RUN: {
            this.asyncStateMachine.asyncRun ( ( Runnable ) param );
            break;
        }
        case ASYNC_SETTIMEOUT: {
            if ( param == null ) {
                return;
            }
            final long timeout = ( long ) param;
            this.setAsyncTimeout ( timeout );
            break;
        }
        case ASYNC_TIMEOUT: {
            final AtomicBoolean result = ( AtomicBoolean ) param;
            result.set ( this.asyncStateMachine.asyncTimeout() );
            break;
        }
        case ASYNC_POST_PROCESS: {
            this.asyncStateMachine.asyncPostProcess();
            break;
        }
        case REQUEST_BODY_FULLY_READ: {
            final AtomicBoolean result = ( AtomicBoolean ) param;
            result.set ( this.isRequestBodyFullyRead() );
            break;
        }
        case NB_READ_INTEREST: {
            if ( !this.isRequestBodyFullyRead() ) {
                this.registerReadInterest();
                break;
            }
            break;
        }
        case NB_WRITE_INTEREST: {
            final AtomicBoolean isReady = ( AtomicBoolean ) param;
            isReady.set ( this.isReady() );
            break;
        }
        case DISPATCH_READ: {
            this.addDispatch ( DispatchType.NON_BLOCKING_READ );
            break;
        }
        case DISPATCH_WRITE: {
            this.addDispatch ( DispatchType.NON_BLOCKING_WRITE );
            break;
        }
        case DISPATCH_EXECUTE: {
            final SocketWrapperBase<?> wrapper = this.socketWrapper;
            if ( wrapper != null ) {
                this.executeDispatches ( wrapper );
                break;
            }
            break;
        }
        case UPGRADE: {
            this.doHttpUpgrade ( ( UpgradeToken ) param );
            break;
        }
        case IS_PUSH_SUPPORTED: {
            final AtomicBoolean result = ( AtomicBoolean ) param;
            result.set ( this.isPushSupported() );
            break;
        }
        case PUSH_REQUEST: {
            this.doPush ( ( PushToken ) param );
            break;
        }
        }
    }
    protected void dispatchNonBlockingRead() {
        this.asyncStateMachine.asyncOperation();
    }
    @Override
    public void timeoutAsync ( final long now ) {
        if ( now < 0L ) {
            this.doTimeoutAsync();
        } else {
            final long asyncTimeout = this.getAsyncTimeout();
            if ( asyncTimeout > 0L ) {
                final long asyncStart = this.asyncStateMachine.getLastAsyncStart();
                if ( now - asyncStart > asyncTimeout ) {
                    this.doTimeoutAsync();
                }
            }
        }
    }
    private void doTimeoutAsync() {
        this.setAsyncTimeout ( -1L );
        this.socketWrapper.processSocket ( SocketEvent.TIMEOUT, true );
    }
    public void setAsyncTimeout ( final long timeout ) {
        this.asyncTimeout = timeout;
    }
    public long getAsyncTimeout() {
        return this.asyncTimeout;
    }
    @Override
    public void recycle() {
        this.errorState = ErrorState.NONE;
        this.asyncStateMachine.recycle();
    }
    protected abstract void prepareResponse() throws IOException;
    protected abstract void finishResponse() throws IOException;
    protected abstract void ack();
    protected abstract void flush() throws IOException;
    protected abstract int available ( final boolean p0 );
    protected abstract void setRequestBody ( final ByteChunk p0 );
    protected abstract void setSwallowResponse();
    protected abstract void disableSwallowRequest();
    protected boolean getPopulateRequestAttributesFromSocket() {
        return true;
    }
    protected void populateRequestAttributeRemoteHost() {
        if ( this.getPopulateRequestAttributesFromSocket() && this.socketWrapper != null ) {
            this.request.remoteHost().setString ( this.socketWrapper.getRemoteHost() );
        }
    }
    protected void populateSslRequestAttributes() {
        try {
            if ( this.sslSupport != null ) {
                Object sslO = this.sslSupport.getCipherSuite();
                if ( sslO != null ) {
                    this.request.setAttribute ( "javax.servlet.request.cipher_suite", sslO );
                }
                sslO = this.sslSupport.getPeerCertificateChain();
                if ( sslO != null ) {
                    this.request.setAttribute ( "javax.servlet.request.X509Certificate", sslO );
                }
                sslO = this.sslSupport.getKeySize();
                if ( sslO != null ) {
                    this.request.setAttribute ( "javax.servlet.request.key_size", sslO );
                }
                sslO = this.sslSupport.getSessionId();
                if ( sslO != null ) {
                    this.request.setAttribute ( "javax.servlet.request.ssl_session_id", sslO );
                }
                sslO = this.sslSupport.getProtocol();
                if ( sslO != null ) {
                    this.request.setAttribute ( "org.apache.tomcat.util.net.secure_protocol_version", sslO );
                }
                this.request.setAttribute ( "javax.servlet.request.ssl_session_mgr", this.sslSupport );
            }
        } catch ( Exception e ) {
            this.getLog().warn ( AbstractProcessor.sm.getString ( "abstractProcessor.socket.ssl" ), e );
        }
    }
    protected void sslReHandShake() {
    }
    protected abstract boolean isRequestBodyFullyRead();
    protected abstract void registerReadInterest();
    protected abstract boolean isReady();
    protected abstract void executeDispatches ( final SocketWrapperBase<?> p0 );
    @Override
    public UpgradeToken getUpgradeToken() {
        throw new IllegalStateException ( AbstractProcessor.sm.getString ( "abstractProcessor.httpupgrade.notsupported" ) );
    }
    protected void doHttpUpgrade ( final UpgradeToken upgradeToken ) {
        throw new UnsupportedOperationException ( AbstractProcessor.sm.getString ( "abstractProcessor.httpupgrade.notsupported" ) );
    }
    @Override
    public ByteBuffer getLeftoverInput() {
        throw new IllegalStateException ( AbstractProcessor.sm.getString ( "abstractProcessor.httpupgrade.notsupported" ) );
    }
    @Override
    public boolean isUpgrade() {
        return false;
    }
    protected boolean isPushSupported() {
        return false;
    }
    protected void doPush ( final PushToken pushToken ) {
        throw new UnsupportedOperationException ( AbstractProcessor.sm.getString ( "abstractProcessor.pushrequest.notsupported" ) );
    }
    protected abstract boolean flushBufferedWrite() throws IOException;
    protected abstract AbstractEndpoint.Handler.SocketState dispatchEndRequest();
    static {
        sm = StringManager.getManager ( AbstractProcessor.class );
    }
}
