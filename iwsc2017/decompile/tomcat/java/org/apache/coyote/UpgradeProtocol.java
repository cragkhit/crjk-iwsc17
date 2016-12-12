package org.apache.coyote;
import org.apache.coyote.http11.upgrade.InternalHttpUpgradeHandler;
import org.apache.tomcat.util.net.SocketWrapperBase;
public interface UpgradeProtocol {
    String getHttpUpgradeName ( boolean p0 );
    byte[] getAlpnIdentifier();
    String getAlpnName();
    Processor getProcessor ( SocketWrapperBase<?> p0, Adapter p1 );
    InternalHttpUpgradeHandler getInternalUpgradeHandler ( Adapter p0, Request p1 );
    boolean accept ( Request p0 );
}
