package org.apache.catalina.storeconfig;
import org.apache.tomcat.util.net.SSLHostConfig;
import org.apache.coyote.UpgradeProtocol;
import org.apache.catalina.LifecycleListener;
import org.apache.catalina.connector.Connector;
import java.io.PrintWriter;
public class ConnectorSF extends StoreFactoryBase {
    @Override
    public void storeChildren ( final PrintWriter aWriter, final int indent, final Object aConnector, final StoreDescription parentDesc ) throws Exception {
        if ( aConnector instanceof Connector ) {
            final Connector connector = ( Connector ) aConnector;
            final LifecycleListener[] listeners = connector.findLifecycleListeners();
            this.storeElementArray ( aWriter, indent, listeners );
            final UpgradeProtocol[] upgradeProtocols = connector.findUpgradeProtocols();
            this.storeElementArray ( aWriter, indent, upgradeProtocols );
            final SSLHostConfig[] hostConfigs = connector.findSslHostConfigs();
            this.storeElementArray ( aWriter, indent, hostConfigs );
        }
    }
    protected void printOpenTag ( final PrintWriter aWriter, final int indent, final Object bean, final StoreDescription aDesc ) throws Exception {
        aWriter.print ( "<" );
        aWriter.print ( aDesc.getTag() );
        this.storeConnectorAttributes ( aWriter, indent, bean, aDesc );
        aWriter.println ( ">" );
    }
    protected void storeConnectorAttributes ( final PrintWriter aWriter, final int indent, final Object bean, final StoreDescription aDesc ) throws Exception {
        if ( aDesc.isAttributes() ) {
            this.getStoreAppender().printAttributes ( aWriter, indent, false, bean, aDesc );
        }
    }
    protected void printTag ( final PrintWriter aWriter, final int indent, final Object bean, final StoreDescription aDesc ) throws Exception {
        aWriter.print ( "<" );
        aWriter.print ( aDesc.getTag() );
        this.storeConnectorAttributes ( aWriter, indent, bean, aDesc );
        aWriter.println ( "/>" );
    }
}
