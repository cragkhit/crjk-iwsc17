package org.apache.coyote.ajp;
import org.apache.coyote.UpgradeToken;
import org.apache.tomcat.util.net.SocketWrapperBase;
import org.apache.coyote.Processor;
import org.apache.tomcat.util.net.SSLHostConfig;
import org.apache.coyote.UpgradeProtocol;
import org.apache.tomcat.util.net.AbstractEndpoint;
import org.apache.tomcat.util.res.StringManager;
import org.apache.coyote.AbstractProtocol;
public abstract class AbstractAjpProtocol<S> extends AbstractProtocol<S> {
    protected static final StringManager sm;
    protected boolean ajpFlush;
    private boolean tomcatAuthentication;
    private boolean tomcatAuthorization;
    private String requiredSecret;
    private int packetSize;
    public AbstractAjpProtocol ( final AbstractEndpoint<S> endpoint ) {
        super ( endpoint );
        this.ajpFlush = true;
        this.tomcatAuthentication = true;
        this.tomcatAuthorization = false;
        this.requiredSecret = null;
        this.packetSize = 8192;
        this.setConnectionTimeout ( -1 );
        this.getEndpoint().setUseSendfile ( false );
        final ConnectionHandler<S> cHandler = new ConnectionHandler<S> ( this );
        this.setHandler ( cHandler );
        this.getEndpoint().setHandler ( cHandler );
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
    protected UpgradeProtocol getNegotiatedProtocol ( final String name ) {
        return null;
    }
    @Override
    protected UpgradeProtocol getUpgradeProtocol ( final String name ) {
        return null;
    }
    public boolean getAjpFlush() {
        return this.ajpFlush;
    }
    public void setAjpFlush ( final boolean ajpFlush ) {
        this.ajpFlush = ajpFlush;
    }
    public boolean getTomcatAuthentication() {
        return this.tomcatAuthentication;
    }
    public void setTomcatAuthentication ( final boolean tomcatAuthentication ) {
        this.tomcatAuthentication = tomcatAuthentication;
    }
    public boolean getTomcatAuthorization() {
        return this.tomcatAuthorization;
    }
    public void setTomcatAuthorization ( final boolean tomcatAuthorization ) {
        this.tomcatAuthorization = tomcatAuthorization;
    }
    public void setRequiredSecret ( final String requiredSecret ) {
        this.requiredSecret = requiredSecret;
    }
    public int getPacketSize() {
        return this.packetSize;
    }
    public void setPacketSize ( final int packetSize ) {
        if ( packetSize < 8192 ) {
            this.packetSize = 8192;
        } else {
            this.packetSize = packetSize;
        }
    }
    @Override
    public void addSslHostConfig ( final SSLHostConfig sslHostConfig ) {
        this.getLog().warn ( AbstractAjpProtocol.sm.getString ( "ajpprotocol.noSSL", sslHostConfig.getHostName() ) );
    }
    @Override
    public SSLHostConfig[] findSslHostConfigs() {
        return new SSLHostConfig[0];
    }
    @Override
    public void addUpgradeProtocol ( final UpgradeProtocol upgradeProtocol ) {
        this.getLog().warn ( AbstractAjpProtocol.sm.getString ( "ajpprotocol.noUpgrade", upgradeProtocol.getClass().getName() ) );
    }
    @Override
    public UpgradeProtocol[] findUpgradeProtocols() {
        return new UpgradeProtocol[0];
    }
    @Override
    protected Processor createProcessor() {
        final AjpProcessor processor = new AjpProcessor ( this.getPacketSize(), this.getEndpoint() );
        processor.setAdapter ( this.getAdapter() );
        processor.setAjpFlush ( this.getAjpFlush() );
        processor.setTomcatAuthentication ( this.getTomcatAuthentication() );
        processor.setTomcatAuthorization ( this.getTomcatAuthorization() );
        processor.setRequiredSecret ( this.requiredSecret );
        processor.setKeepAliveTimeout ( this.getKeepAliveTimeout() );
        processor.setClientCertProvider ( this.getClientCertProvider() );
        return processor;
    }
    @Override
    protected Processor createUpgradeProcessor ( final SocketWrapperBase<?> socket, final UpgradeToken upgradeToken ) {
        throw new IllegalStateException ( AbstractAjpProtocol.sm.getString ( "ajpprotocol.noUpgradeHandler", upgradeToken.getHttpUpgradeHandler().getClass().getName() ) );
    }
    static {
        sm = StringManager.getManager ( AbstractAjpProtocol.class );
    }
}
