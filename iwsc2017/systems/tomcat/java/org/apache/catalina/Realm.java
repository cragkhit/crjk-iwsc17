package org.apache.catalina;
import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.security.Principal;
import java.security.cert.X509Certificate;
import org.apache.catalina.connector.Request;
import org.apache.catalina.connector.Response;
import org.apache.tomcat.util.descriptor.web.SecurityConstraint;
import org.ietf.jgss.GSSContext;
public interface Realm {
    public Container getContainer();
    public void setContainer ( Container container );
    public CredentialHandler getCredentialHandler();
    public void setCredentialHandler ( CredentialHandler credentialHandler );
    public void addPropertyChangeListener ( PropertyChangeListener listener );
    public Principal authenticate ( String username );
    public Principal authenticate ( String username, String credentials );
    public Principal authenticate ( String username, String digest,
                                    String nonce, String nc, String cnonce,
                                    String qop, String realm,
                                    String md5a2 );
    public Principal authenticate ( GSSContext gssContext, boolean storeCreds );
    public Principal authenticate ( X509Certificate certs[] );
    public void backgroundProcess();
    public SecurityConstraint [] findSecurityConstraints ( Request request,
            Context context );
    public boolean hasResourcePermission ( Request request,
                                           Response response,
                                           SecurityConstraint [] constraint,
                                           Context context )
    throws IOException;
    public boolean hasRole ( Wrapper wrapper, Principal principal, String role );
    public boolean hasUserDataPermission ( Request request,
                                           Response response,
                                           SecurityConstraint []constraint )
    throws IOException;
    public void removePropertyChangeListener ( PropertyChangeListener listener );
    public String[] getRoles ( Principal principal );
public default boolean isAvailable() {
        return true;
    }
}
