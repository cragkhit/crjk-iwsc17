package org.apache.catalina.realm;
import java.security.cert.X509Certificate;
public interface X509UsernameRetriever {
    public String getUsername ( X509Certificate cert );
}
