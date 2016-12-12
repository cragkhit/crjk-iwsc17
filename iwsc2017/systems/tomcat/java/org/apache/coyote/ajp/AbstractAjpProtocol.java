package org.apache.coyote.ajp;
import org.apache.coyote.AbstractProtocol;
import org.apache.coyote.Processor;
import org.apache.coyote.UpgradeProtocol;
import org.apache.coyote.UpgradeToken;
import org.apache.tomcat.util.net.AbstractEndpoint;
import org.apache.tomcat.util.net.SSLHostConfig;
import org.apache.tomcat.util.net.SocketWrapperBase;
import org.apache.tomcat.util.res.StringManager;
public abstract class AbstractAjpProtocol<S> extends AbstractProtocol<S> {
    protected static final StringManager sm = StringManager.getManager ( AbstractAjpProtocol.class );
    public AbstractAjpProtocol ( AbstractEndpoint<S> endpoint ) {
        super ( endpoint );
        setConnectionTimeout ( Constants.DEFAULT_CONNECTION_TIMEOUT );
        getEndpoint().setUseSendfile ( false );
        ConnectionHandler<S> cHandler = new ConnectionHandler<> ( this );
        setHandler ( cHandler );
        getEndpoint().setHandler ( cHandler );
    }
    @Override
    protected String getProtocolName() {
        return "Ajp";
    }
    @Override
    protected AbstractEndpoint<S> getEndpoint() {
        return super.getEndpoint();
    }
    @Override
    protected UpgradeProtocol getNegotiatedProtocol ( String name ) {
        return null;
    }
    @Override
    protected UpgradeProtocol getUpgradeProtocol ( String name ) {
        return null;
    }
    protected boolean ajpFlush = true;
    public boolean getAjpFlush() {
        return ajpFlush;
    }
    public void setAjpFlush ( boolean ajpFlush ) {
        this.ajpFlush = ajpFlush;
    }
    private boolean tomcatAuthentication = true;
    public boolean getTomcatAuthentication() {
        return tomcatAuthentication;
    }
    public void setTomcatAuthentication ( boolean tomcatAuthentication ) {
        this.tomcatAuthentication = tomcatAuthentication;
    }
    private boolean tomcatAuthorization = false;
    public boolean getTomcatAuthorization() {
        return tomcatAuthorization;
    }
    public void setTomcatAuthorization ( boolean tomcatAuthorization ) {
        this.tomcatAuthorization = tomcatAuthorization;
    }
    private String requiredSecret = null;
    public void setRequiredSecret ( String requiredSecret ) {
        this.requiredSecret = requiredSecret;
    }
    private int packetSize = Constants.MAX_PACKET_SIZE;
    public int getPacketSize() {
        return packetSize;
    }
    public void setPacketSize ( int packetSize ) {
        if ( packetSize < Constants.MAX_PACKET_SIZE ) {
            this.packetSize = Constants.MAX_PACKET_SIZE;
        } else {
            this.packetSize = packetSize;
        }
    }
    @Override
    public void addSslHostConfig ( SSLHostConfig sslHostConfig ) {
        getLog().warn ( sm.getString ( "ajpprotocol.noSSL", sslHostConfig.getHostName() ) );
    }
    @Override
    public SSLHostConfig[] findSslHostConfigs() {
        return new SSLHostConfig[0];
    }
    @Override
    public void addUpgradeProtocol ( UpgradeProtocol upgradeProtocol ) {
        getLog().warn ( sm.getString ( "ajpprotocol.noUpgrade", upgradeProtocol.getClass().getName() ) );
    }
    @Override
    public UpgradeProtocol[] findUpgradeProtocols() {
        return new UpgradeProtocol[0];
    }
    @Override
    protected Processor createProcessor() {
        AjpProcessor processor = new AjpProcessor ( getPacketSize(), getEndpoint() );
        processor.setAdapter ( getAdapter() );
        processor.setAjpFlush ( getAjpFlush() );
        processor.setTomcatAuthentication ( getTomcatAuthentication() );
        processor.setTomcatAuthorization ( getTomcatAuthorization() );
        processor.setRequiredSecret ( requiredSecret );
        processor.setKeepAliveTimeout ( getKeepAliveTimeout() );
        processor.setClientCertProvider ( getClientCertProvider() );
        return processor;
    }
    @Override
    protected Processor createUpgradeProcessor ( SocketWrapperBase<?> socket,
            UpgradeToken upgradeToken ) {
        throw new IllegalStateException ( sm.getString ( "ajpprotocol.noUpgradeHandler",
                                          upgradeToken.getHttpUpgradeHandler().getClass().getName() ) );
    }
}
