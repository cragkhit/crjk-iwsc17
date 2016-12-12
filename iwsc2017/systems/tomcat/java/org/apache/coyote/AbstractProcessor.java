package org.apache.coyote;
import java.io.IOException;
import java.io.InterruptedIOException;
import java.nio.ByteBuffer;
import java.util.concurrent.Executor;
import java.util.concurrent.atomic.AtomicBoolean;
import javax.servlet.RequestDispatcher;
import org.apache.tomcat.util.ExceptionUtils;
import org.apache.tomcat.util.buf.ByteChunk;
import org.apache.tomcat.util.net.AbstractEndpoint;
import org.apache.tomcat.util.net.AbstractEndpoint.Handler.SocketState;
import org.apache.tomcat.util.net.DispatchType;
import org.apache.tomcat.util.net.SSLSupport;
import org.apache.tomcat.util.net.SocketEvent;
import org.apache.tomcat.util.net.SocketWrapperBase;
import org.apache.tomcat.util.res.StringManager;
public abstract class AbstractProcessor extends AbstractProcessorLight implements ActionHook {
    private static final StringManager sm = StringManager.getManager ( AbstractProcessor.class );
    protected Adapter adapter;
    protected final AsyncStateMachine asyncStateMachine;
    private volatile long asyncTimeout = -1;
    protected final AbstractEndpoint<?> endpoint;
    protected final Request request;
    protected final Response response;
    protected volatile SocketWrapperBase<?> socketWrapper = null;
    protected volatile SSLSupport sslSupport;
    private ErrorState errorState = ErrorState.NONE;
    protected AbstractProcessor ( Request coyoteRequest, Response coyoteResponse ) {
        this ( null, coyoteRequest, coyoteResponse );
    }
    public AbstractProcessor ( AbstractEndpoint<?> endpoint ) {
        this ( endpoint, new Request(), new Response() );
    }
    private AbstractProcessor ( AbstractEndpoint<?> endpoint, Request coyoteRequest,
                                Response coyoteResponse ) {
        this.endpoint = endpoint;
        asyncStateMachine = new AsyncStateMachine ( this );
        request = coyoteRequest;
        response = coyoteResponse;
        response.setHook ( this );
        request.setResponse ( response );
        request.setHook ( this );
    }
    protected void setErrorState ( ErrorState errorState, Throwable t ) {
        boolean blockIo = this.errorState.isIoAllowed() && !errorState.isIoAllowed();
        this.errorState = this.errorState.getMostSevere ( errorState );
        if ( blockIo && !ContainerThreadMarker.isContainerThread() && isAsync() ) {
            if ( response.getStatus() < 400 ) {
                response.setStatus ( 500 );
            }
            getLog().info ( sm.getString ( "abstractProcessor.nonContainerThreadError" ), t );
            request.setAttribute ( RequestDispatcher.ERROR_EXCEPTION, t );
            socketWrapper.processSocket ( SocketEvent.ERROR, true );
        }
    }
    protected ErrorState getErrorState() {
        return errorState;
    }
    @Override
    public Request getRequest() {
        return request;
    }
    public void setAdapter ( Adapter adapter ) {
        this.adapter = adapter;
    }
    public Adapter getAdapter() {
        return adapter;
    }
    protected final void setSocketWrapper ( SocketWrapperBase<?> socketWrapper ) {
        this.socketWrapper = socketWrapper;
    }
    protected final SocketWrapperBase<?> getSocketWrapper() {
        return socketWrapper;
    }
    @Override
    public final void setSslSupport ( SSLSupport sslSupport ) {
        this.sslSupport = sslSupport;
    }
    protected Executor getExecutor() {
        return endpoint.getExecutor();
    }
    @Override
    public boolean isAsync() {
        return asyncStateMachine.isAsync();
    }
    @Override
    public SocketState asyncPostProcess() {
        return asyncStateMachine.asyncPostProcess();
    }
    @Override
    public final SocketState dispatch ( SocketEvent status ) {
        if ( status == SocketEvent.OPEN_WRITE && response.getWriteListener() != null ) {
            asyncStateMachine.asyncOperation();
            try {
                if ( flushBufferedWrite() ) {
                    return SocketState.LONG;
                }
            } catch ( IOException ioe ) {
                if ( getLog().isDebugEnabled() ) {
                    getLog().debug ( "Unable to write async data.", ioe );
                }
                status = SocketEvent.ERROR;
                request.setAttribute ( RequestDispatcher.ERROR_EXCEPTION, ioe );
            }
        } else if ( status == SocketEvent.OPEN_READ && request.getReadListener() != null ) {
            dispatchNonBlockingRead();
        } else if ( status == SocketEvent.ERROR ) {
            if ( request.getAttribute ( RequestDispatcher.ERROR_EXCEPTION ) == null ) {
                request.setAttribute ( RequestDispatcher.ERROR_EXCEPTION, socketWrapper.getError() );
            }
            if ( request.getReadListener() != null || response.getWriteListener() != null ) {
                asyncStateMachine.asyncOperation();
            }
        }
        RequestInfo rp = request.getRequestProcessor();
        try {
            rp.setStage ( org.apache.coyote.Constants.STAGE_SERVICE );
            if ( !getAdapter().asyncDispatch ( request, response, status ) ) {
                setErrorState ( ErrorState.CLOSE_NOW, null );
            }
        } catch ( InterruptedIOException e ) {
            setErrorState ( ErrorState.CLOSE_CONNECTION_NOW, e );
        } catch ( Throwable t ) {
            ExceptionUtils.handleThrowable ( t );
            setErrorState ( ErrorState.CLOSE_NOW, t );
            getLog().error ( sm.getString ( "http11processor.request.process" ), t );
        }
        rp.setStage ( org.apache.coyote.Constants.STAGE_ENDED );
        if ( getErrorState().isError() ) {
            request.updateCounters();
            return SocketState.CLOSED;
        } else if ( isAsync() ) {
            return SocketState.LONG;
        } else {
            request.updateCounters();
            return dispatchEndRequest();
        }
    }
    @Override
    public final void action ( ActionCode actionCode, Object param ) {
        switch ( actionCode ) {
        case COMMIT: {
            if ( !response.isCommitted() ) {
                try {
                    prepareResponse();
                } catch ( IOException e ) {
                    setErrorState ( ErrorState.CLOSE_CONNECTION_NOW, e );
                }
            }
            break;
        }
        case CLOSE: {
            action ( ActionCode.COMMIT, null );
            try {
                finishResponse();
            } catch ( CloseNowException cne ) {
                setErrorState ( ErrorState.CLOSE_NOW, cne );
            } catch ( IOException e ) {
                setErrorState ( ErrorState.CLOSE_CONNECTION_NOW, e );
            }
            break;
        }
        case ACK: {
            ack();
            break;
        }
        case CLIENT_FLUSH: {
            action ( ActionCode.COMMIT, null );
            try {
                flush();
            } catch ( IOException e ) {
                setErrorState ( ErrorState.CLOSE_CONNECTION_NOW, e );
                response.setErrorException ( e );
            }
            break;
        }
        case AVAILABLE: {
            request.setAvailable ( available ( Boolean.TRUE.equals ( param ) ) );
            break;
        }
        case REQ_SET_BODY_REPLAY: {
            ByteChunk body = ( ByteChunk ) param;
            setRequestBody ( body );
            break;
        }
        case IS_ERROR: {
            ( ( AtomicBoolean ) param ).set ( getErrorState().isError() );
            break;
        }
        case CLOSE_NOW: {
            setSwallowResponse();
            if ( param instanceof Throwable ) {
                setErrorState ( ErrorState.CLOSE_NOW, ( Throwable ) param );
            } else {
                setErrorState ( ErrorState.CLOSE_NOW, null );
            }
            break;
        }
        case DISABLE_SWALLOW_INPUT: {
            disableSwallowRequest();
            setErrorState ( ErrorState.CLOSE_CLEAN, null );
            break;
        }
        case REQ_HOST_ADDR_ATTRIBUTE: {
            if ( getPopulateRequestAttributesFromSocket() && socketWrapper != null ) {
                request.remoteAddr().setString ( socketWrapper.getRemoteAddr() );
            }
            break;
        }
        case REQ_HOST_ATTRIBUTE: {
            populateRequestAttributeRemoteHost();
            break;
        }
        case REQ_LOCALPORT_ATTRIBUTE: {
            if ( getPopulateRequestAttributesFromSocket() && socketWrapper != null ) {
                request.setLocalPort ( socketWrapper.getLocalPort() );
            }
            break;
        }
        case REQ_LOCAL_ADDR_ATTRIBUTE: {
            if ( getPopulateRequestAttributesFromSocket() && socketWrapper != null ) {
                request.localAddr().setString ( socketWrapper.getLocalAddr() );
            }
            break;
        }
        case REQ_LOCAL_NAME_ATTRIBUTE: {
            if ( getPopulateRequestAttributesFromSocket() && socketWrapper != null ) {
                request.localName().setString ( socketWrapper.getLocalName() );
            }
            break;
        }
        case REQ_REMOTEPORT_ATTRIBUTE: {
            if ( getPopulateRequestAttributesFromSocket() && socketWrapper != null ) {
                request.setRemotePort ( socketWrapper.getRemotePort() );
            }
            break;
        }
        case REQ_SSL_ATTRIBUTE: {
            populateSslRequestAttributes();
            break;
        }
        case REQ_SSL_CERTIFICATE: {
            sslReHandShake();
            break;
        }
        case ASYNC_START: {
            asyncStateMachine.asyncStart ( ( AsyncContextCallback ) param );
            break;
        }
        case ASYNC_COMPLETE: {
            clearDispatches();
            if ( asyncStateMachine.asyncComplete() ) {
                socketWrapper.processSocket ( SocketEvent.OPEN_READ, true );
            }
            break;
        }
        case ASYNC_DISPATCH: {
            if ( asyncStateMachine.asyncDispatch() ) {
                socketWrapper.processSocket ( SocketEvent.OPEN_READ, true );
            }
            break;
        }
        case ASYNC_DISPATCHED: {
            asyncStateMachine.asyncDispatched();
            break;
        }
        case ASYNC_ERROR: {
            asyncStateMachine.asyncError();
            break;
        }
        case ASYNC_IS_ASYNC: {
            ( ( AtomicBoolean ) param ).set ( asyncStateMachine.isAsync() );
            break;
        }
        case ASYNC_IS_COMPLETING: {
            ( ( AtomicBoolean ) param ).set ( asyncStateMachine.isCompleting() );
            break;
        }
        case ASYNC_IS_DISPATCHING: {
            ( ( AtomicBoolean ) param ).set ( asyncStateMachine.isAsyncDispatching() );
            break;
        }
        case ASYNC_IS_ERROR: {
            ( ( AtomicBoolean ) param ).set ( asyncStateMachine.isAsyncError() );
            break;
        }
        case ASYNC_IS_STARTED: {
            ( ( AtomicBoolean ) param ).set ( asyncStateMachine.isAsyncStarted() );
            break;
        }
        case ASYNC_IS_TIMINGOUT: {
            ( ( AtomicBoolean ) param ).set ( asyncStateMachine.isAsyncTimingOut() );
            break;
        }
        case ASYNC_RUN: {
            asyncStateMachine.asyncRun ( ( Runnable ) param );
            break;
        }
        case ASYNC_SETTIMEOUT: {
            if ( param == null ) {
                return;
            }
            long timeout = ( ( Long ) param ).longValue();
            setAsyncTimeout ( timeout );
            break;
        }
        case ASYNC_TIMEOUT: {
            AtomicBoolean result = ( AtomicBoolean ) param;
            result.set ( asyncStateMachine.asyncTimeout() );
            break;
        }
        case ASYNC_POST_PROCESS: {
            asyncStateMachine.asyncPostProcess();
            break;
        }
        case REQUEST_BODY_FULLY_READ: {
            AtomicBoolean result = ( AtomicBoolean ) param;
            result.set ( isRequestBodyFullyRead() );
            break;
        }
        case NB_READ_INTEREST: {
            if ( !isRequestBodyFullyRead() ) {
                registerReadInterest();
            }
            break;
        }
        case NB_WRITE_INTEREST: {
            AtomicBoolean isReady = ( AtomicBoolean ) param;
            isReady.set ( isReady() );
            break;
        }
        case DISPATCH_READ: {
            addDispatch ( DispatchType.NON_BLOCKING_READ );
            break;
        }
        case DISPATCH_WRITE: {
            addDispatch ( DispatchType.NON_BLOCKING_WRITE );
            break;
        }
        case DISPATCH_EXECUTE: {
            SocketWrapperBase<?> wrapper = socketWrapper;
            if ( wrapper != null ) {
                executeDispatches ( wrapper );
            }
            break;
        }
        case UPGRADE: {
            doHttpUpgrade ( ( UpgradeToken ) param );
            break;
        }
        case IS_PUSH_SUPPORTED: {
            AtomicBoolean result = ( AtomicBoolean ) param;
            result.set ( isPushSupported() );
            break;
        }
        case PUSH_REQUEST: {
            doPush ( ( PushToken ) param );
            break;
        }
        }
    }
    protected void dispatchNonBlockingRead() {
        asyncStateMachine.asyncOperation();
    }
    @Override
    public void timeoutAsync ( long now ) {
        if ( now < 0 ) {
            doTimeoutAsync();
        } else {
            long asyncTimeout = getAsyncTimeout();
            if ( asyncTimeout > 0 ) {
                long asyncStart = asyncStateMachine.getLastAsyncStart();
                if ( ( now - asyncStart ) > asyncTimeout ) {
                    doTimeoutAsync();
                }
            }
        }
    }
    private void doTimeoutAsync() {
        setAsyncTimeout ( -1 );
        socketWrapper.processSocket ( SocketEvent.TIMEOUT, true );
    }
    public void setAsyncTimeout ( long timeout ) {
        asyncTimeout = timeout;
    }
    public long getAsyncTimeout() {
        return asyncTimeout;
    }
    @Override
    public void recycle() {
        errorState = ErrorState.NONE;
        asyncStateMachine.recycle();
    }
    protected abstract void prepareResponse() throws IOException;
    protected abstract void finishResponse() throws IOException;
    protected abstract void ack();
    protected abstract void flush() throws IOException;
    protected abstract int available ( boolean doRead );
    protected abstract void setRequestBody ( ByteChunk body );
    protected abstract void setSwallowResponse();
    protected abstract void disableSwallowRequest();
    protected boolean getPopulateRequestAttributesFromSocket() {
        return true;
    }
    protected void populateRequestAttributeRemoteHost() {
        if ( getPopulateRequestAttributesFromSocket() && socketWrapper != null ) {
            request.remoteHost().setString ( socketWrapper.getRemoteHost() );
        }
    }
    protected void populateSslRequestAttributes() {
        try {
            if ( sslSupport != null ) {
                Object sslO = sslSupport.getCipherSuite();
                if ( sslO != null ) {
                    request.setAttribute ( SSLSupport.CIPHER_SUITE_KEY, sslO );
                }
                sslO = sslSupport.getPeerCertificateChain();
                if ( sslO != null ) {
                    request.setAttribute ( SSLSupport.CERTIFICATE_KEY, sslO );
                }
                sslO = sslSupport.getKeySize();
                if ( sslO != null ) {
                    request.setAttribute ( SSLSupport.KEY_SIZE_KEY, sslO );
                }
                sslO = sslSupport.getSessionId();
                if ( sslO != null ) {
                    request.setAttribute ( SSLSupport.SESSION_ID_KEY, sslO );
                }
                sslO = sslSupport.getProtocol();
                if ( sslO != null ) {
                    request.setAttribute ( SSLSupport.PROTOCOL_VERSION_KEY, sslO );
                }
                request.setAttribute ( SSLSupport.SESSION_MGR, sslSupport );
            }
        } catch ( Exception e ) {
            getLog().warn ( sm.getString ( "abstractProcessor.socket.ssl" ), e );
        }
    }
    protected void sslReHandShake() {
    }
    protected abstract boolean isRequestBodyFullyRead();
    protected abstract void registerReadInterest();
    protected abstract boolean isReady();
    protected abstract void executeDispatches ( SocketWrapperBase<?> wrapper );
    @Override
    public UpgradeToken getUpgradeToken() {
        throw new IllegalStateException (
            sm.getString ( "abstractProcessor.httpupgrade.notsupported" ) );
    }
    protected void doHttpUpgrade ( UpgradeToken upgradeToken ) {
        throw new UnsupportedOperationException (
            sm.getString ( "abstractProcessor.httpupgrade.notsupported" ) );
    }
    @Override
    public ByteBuffer getLeftoverInput() {
        throw new IllegalStateException ( sm.getString ( "abstractProcessor.httpupgrade.notsupported" ) );
    }
    @Override
    public boolean isUpgrade() {
        return false;
    }
    protected boolean isPushSupported() {
        return false;
    }
    protected void doPush ( PushToken pushToken ) {
        throw new UnsupportedOperationException (
            sm.getString ( "abstractProcessor.pushrequest.notsupported" ) );
    }
    protected abstract boolean flushBufferedWrite() throws IOException ;
    protected abstract SocketState dispatchEndRequest();
}
