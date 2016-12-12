package org.apache.catalina;
import org.apache.catalina.mapper.Mapper;
import org.apache.catalina.connector.Connector;
public interface Service extends Lifecycle {
    Engine getContainer();
    void setContainer ( Engine p0 );
    String getName();
    void setName ( String p0 );
    Server getServer();
    void setServer ( Server p0 );
    ClassLoader getParentClassLoader();
    void setParentClassLoader ( ClassLoader p0 );
    String getDomain();
    void addConnector ( Connector p0 );
    Connector[] findConnectors();
    void removeConnector ( Connector p0 );
    void addExecutor ( Executor p0 );
    Executor[] findExecutors();
    Executor getExecutor ( String p0 );
    void removeExecutor ( Executor p0 );
    Mapper getMapper();
}
