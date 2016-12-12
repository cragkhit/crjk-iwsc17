package org.apache.catalina.realm;
import java.security.cert.X509Certificate;
public class X509SubjectDnRetriever implements X509UsernameRetriever {
    @Override
    public String getUsername ( final X509Certificate clientCert ) {
        return clientCert.getSubjectDN().getName();
    }
}
