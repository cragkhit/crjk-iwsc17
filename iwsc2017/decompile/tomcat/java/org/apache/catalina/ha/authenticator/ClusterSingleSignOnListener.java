package org.apache.catalina.ha.authenticator;
import org.apache.catalina.ha.session.ReplicatedSessionListener;
import org.apache.catalina.authenticator.SingleSignOnListener;
public class ClusterSingleSignOnListener extends SingleSignOnListener implements ReplicatedSessionListener {
    private static final long serialVersionUID = 1L;
    public ClusterSingleSignOnListener ( final String ssoId ) {
        super ( ssoId );
    }
}
