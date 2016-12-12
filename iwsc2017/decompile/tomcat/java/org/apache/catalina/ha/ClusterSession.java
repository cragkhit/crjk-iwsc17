package org.apache.catalina.ha;
import javax.servlet.http.HttpSession;
import org.apache.catalina.Session;
public interface ClusterSession extends Session, HttpSession {
    boolean isPrimarySession();
    void setPrimarySession ( boolean p0 );
}
