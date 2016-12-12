package org.apache.catalina.storeconfig;
import java.io.PrintWriter;
import org.apache.catalina.Context;
import org.apache.catalina.Host;
import org.apache.catalina.Server;
import org.apache.catalina.Service;
public interface IStoreConfig {
    StoreRegistry getRegistry();
    void setRegistry ( StoreRegistry aRegistry );
    Server getServer();
    void setServer ( Server aServer );
    void storeConfig();
    boolean store ( Server aServer );
    void store ( PrintWriter aWriter, int indent, Server aServer ) throws Exception;
    void store ( PrintWriter aWriter, int indent, Service aService ) throws Exception;
    void store ( PrintWriter aWriter, int indent, Host aHost ) throws Exception;
    boolean store ( Context aContext );
    void store ( PrintWriter aWriter, int indent, Context aContext ) throws Exception;
}
