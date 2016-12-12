package org.apache.catalina;
import java.security.Principal;
import org.ietf.jgss.GSSCredential;
public interface TomcatPrincipal extends Principal {
    Principal getUserPrincipal();
    GSSCredential getGssCredential();
    void logout() throws Exception;
}
