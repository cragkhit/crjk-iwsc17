package org.apache.catalina;
import org.apache.catalina.connector.Connector;
import org.apache.catalina.mapper.Mapper;
public interface Service extends Lifecycle {
    public Engine getContainer();
    public void setContainer ( Engine engine );
    public String getName();
    public void setName ( String name );
    public Server getServer();
    public void setServer ( Server server );
    public ClassLoader getParentClassLoader();
    public void setParentClassLoader ( ClassLoader parent );
    public String getDomain();
    public void addConnector ( Connector connector );
    public Connector[] findConnectors();
    public void removeConnector ( Connector connector );
    public void addExecutor ( Executor ex );
    public Executor[] findExecutors();
    public Executor getExecutor ( String name );
    public void removeExecutor ( Executor ex );
    Mapper getMapper();
}
