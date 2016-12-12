package org.apache.catalina;
import org.ietf.jgss.GSSCredential;
import java.security.Principal;
public interface TomcatPrincipal extends Principal {
    Principal getUserPrincipal();
    GSSCredential getGssCredential();
    void logout() throws Exception;
}
