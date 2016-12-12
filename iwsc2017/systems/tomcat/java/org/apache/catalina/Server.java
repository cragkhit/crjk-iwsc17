package org.apache.catalina;
import java.io.File;
import org.apache.catalina.deploy.NamingResourcesImpl;
import org.apache.catalina.startup.Catalina;
public interface Server extends Lifecycle {
    public NamingResourcesImpl getGlobalNamingResources();
    public void setGlobalNamingResources
    ( NamingResourcesImpl globalNamingResources );
    public javax.naming.Context getGlobalNamingContext();
    public int getPort();
    public void setPort ( int port );
    public String getAddress();
    public void setAddress ( String address );
    public String getShutdown();
    public void setShutdown ( String shutdown );
    public ClassLoader getParentClassLoader();
    public void setParentClassLoader ( ClassLoader parent );
    public Catalina getCatalina();
    public void setCatalina ( Catalina catalina );
    public File getCatalinaBase();
    public void setCatalinaBase ( File catalinaBase );
    public File getCatalinaHome();
    public void setCatalinaHome ( File catalinaHome );
    public void addService ( Service service );
    public void await();
    public Service findService ( String name );
    public Service[] findServices();
    public void removeService ( Service service );
    public Object getNamingToken();
}
