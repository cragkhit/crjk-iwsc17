package org.apache.catalina.ha;
import javax.servlet.http.HttpSession;
import org.apache.catalina.Session;
public interface ClusterSession extends Session, HttpSession {
    public boolean isPrimarySession();
    public void setPrimarySession ( boolean primarySession );
}
