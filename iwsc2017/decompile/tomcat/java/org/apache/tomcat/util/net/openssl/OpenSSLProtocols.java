package org.apache.tomcat.util.net.openssl;
import java.util.Collection;
import java.util.Collections;
import java.util.ArrayList;
import java.util.List;
public class OpenSSLProtocols {
    private List<String> openSSLProtocols;
    public OpenSSLProtocols ( final String preferredJSSEProto ) {
        Collections.addAll ( this.openSSLProtocols = new ArrayList<String>(), new String[] { "TLSv1.2", "TLSv1.1", "TLSv1", "SSLv3", "SSLv2" } );
        if ( this.openSSLProtocols.contains ( preferredJSSEProto ) ) {
            this.openSSLProtocols.remove ( preferredJSSEProto );
            this.openSSLProtocols.add ( 0, preferredJSSEProto );
        }
    }
    public String[] getProtocols() {
        return this.openSSLProtocols.toArray ( new String[this.openSSLProtocols.size()] );
    }
}
