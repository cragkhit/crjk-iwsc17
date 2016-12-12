package org.apache.catalina.storeconfig;
import java.io.PrintWriter;
public interface IStoreFactory {
    StoreAppender getStoreAppender();
    void setStoreAppender ( StoreAppender p0 );
    void setRegistry ( StoreRegistry p0 );
    StoreRegistry getRegistry();
    void store ( PrintWriter p0, int p1, Object p2 ) throws Exception;
    void storeXMLHead ( PrintWriter p0 );
}
