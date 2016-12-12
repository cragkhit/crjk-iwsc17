package org.apache.coyote.http11.upgrade;
import java.io.IOException;
import javax.servlet.ServletInputStream;
import javax.servlet.ServletOutputStream;
import org.apache.coyote.UpgradeToken;
import org.apache.juli.logging.Log;
import org.apache.juli.logging.LogFactory;
import org.apache.tomcat.util.net.AbstractEndpoint.Handler.SocketState;
import org.apache.tomcat.util.net.SSLSupport;
import org.apache.tomcat.util.net.SocketEvent;
import org.apache.tomcat.util.net.SocketWrapperBase;
import org.apache.tomcat.util.res.StringManager;
public class UpgradeProcessorExternal extends UpgradeProcessorBase {
    private static final Log log = LogFactory.getLog ( UpgradeProcessorExternal.class );
    private static final StringManager sm = StringManager.getManager ( UpgradeProcessorExternal.class );
    private final UpgradeServletInputStream upgradeServletInputStream;
    private final UpgradeServletOutputStream upgradeServletOutputStream;
    public UpgradeProcessorExternal ( SocketWrapperBase<?> wrapper,
                                      UpgradeToken upgradeToken ) {
        super ( upgradeToken );
        this.upgradeServletInputStream = new UpgradeServletInputStream ( this, wrapper );
        this.upgradeServletOutputStream = new UpgradeServletOutputStream ( this, wrapper );
        wrapper.setReadTimeout ( INFINITE_TIMEOUT );
        wrapper.setWriteTimeout ( INFINITE_TIMEOUT );
    }
    @Override
    protected Log getLog() {
        return log;
    }
    @Override
    public void close() throws Exception {
        upgradeServletInputStream.close();
        upgradeServletOutputStream.close();
    }
    @Override
    public ServletInputStream getInputStream() throws IOException {
        return upgradeServletInputStream;
    }
    @Override
    public ServletOutputStream getOutputStream() throws IOException {
        return upgradeServletOutputStream;
    }
    @Override
    public final SocketState dispatch ( SocketEvent status ) {
        if ( status == SocketEvent.OPEN_READ ) {
            upgradeServletInputStream.onDataAvailable();
        } else if ( status == SocketEvent.OPEN_WRITE ) {
            upgradeServletOutputStream.onWritePossible();
        } else if ( status == SocketEvent.STOP ) {
            if ( log.isDebugEnabled() ) {
                log.debug ( sm.getString ( "upgradeProcessor.stop" ) );
            }
            try {
                upgradeServletInputStream.close();
            } catch ( IOException ioe ) {
                log.debug ( sm.getString ( "upgradeProcessor.isCloseFail", ioe ) );
            }
            try {
                upgradeServletOutputStream.close();
            } catch ( IOException ioe ) {
                log.debug ( sm.getString ( "upgradeProcessor.osCloseFail", ioe ) );
            }
            return SocketState.CLOSED;
        } else {
            if ( log.isDebugEnabled() ) {
                log.debug ( sm.getString ( "upgradeProcessor.unexpectedState" ) );
            }
            return SocketState.CLOSED;
        }
        if ( upgradeServletInputStream.isClosed() &&
                upgradeServletOutputStream.isClosed() ) {
            if ( log.isDebugEnabled() ) {
                log.debug ( sm.getString ( "upgradeProcessor.requiredClose",
                                           Boolean.valueOf ( upgradeServletInputStream.isClosed() ),
                                           Boolean.valueOf ( upgradeServletOutputStream.isClosed() ) ) );
            }
            return SocketState.CLOSED;
        }
        return SocketState.UPGRADED;
    }
    @Override
    public final void setSslSupport ( SSLSupport sslSupport ) {
    }
    @Override
    public void pause() {
    }
}
