package org.apache.catalina;
import java.io.IOException;
import org.apache.catalina.connector.Response;
import org.apache.tomcat.util.descriptor.web.SecurityConstraint;
import org.apache.catalina.connector.Request;
import java.security.cert.X509Certificate;
import org.ietf.jgss.GSSContext;
import java.security.Principal;
import java.beans.PropertyChangeListener;
public interface Realm {
    Container getContainer();
    void setContainer ( Container p0 );
    CredentialHandler getCredentialHandler();
    void setCredentialHandler ( CredentialHandler p0 );
    void addPropertyChangeListener ( PropertyChangeListener p0 );
    Principal authenticate ( String p0 );
    Principal authenticate ( String p0, String p1 );
    Principal authenticate ( String p0, String p1, String p2, String p3, String p4, String p5, String p6, String p7 );
    Principal authenticate ( GSSContext p0, boolean p1 );
    Principal authenticate ( X509Certificate[] p0 );
    void backgroundProcess();
    SecurityConstraint[] findSecurityConstraints ( Request p0, Context p1 );
    boolean hasResourcePermission ( Request p0, Response p1, SecurityConstraint[] p2, Context p3 ) throws IOException;
    boolean hasRole ( Wrapper p0, Principal p1, String p2 );
    boolean hasUserDataPermission ( Request p0, Response p1, SecurityConstraint[] p2 ) throws IOException;
    void removePropertyChangeListener ( PropertyChangeListener p0 );
    String[] getRoles ( Principal p0 );
default boolean isAvailable() {
        return true;
    }
}
