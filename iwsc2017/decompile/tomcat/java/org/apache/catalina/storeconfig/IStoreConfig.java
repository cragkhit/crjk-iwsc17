package org.apache.catalina.storeconfig;
import org.apache.catalina.Context;
import org.apache.catalina.Host;
import org.apache.catalina.Service;
import java.io.PrintWriter;
import org.apache.catalina.Server;
public interface IStoreConfig {
    StoreRegistry getRegistry();
    void setRegistry ( StoreRegistry p0 );
    Server getServer();
    void setServer ( Server p0 );
    void storeConfig();
    boolean store ( Server p0 );
    void store ( PrintWriter p0, int p1, Server p2 ) throws Exception;
    void store ( PrintWriter p0, int p1, Service p2 ) throws Exception;
    void store ( PrintWriter p0, int p1, Host p2 ) throws Exception;
    boolean store ( Context p0 );
    void store ( PrintWriter p0, int p1, Context p2 ) throws Exception;
}
