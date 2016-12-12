package org.apache.catalina.realm;
import java.security.cert.X509Certificate;
public interface X509UsernameRetriever {
    String getUsername ( X509Certificate p0 );
}
