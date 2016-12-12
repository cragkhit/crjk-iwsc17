package org.apache.catalina;
public interface StoreManager extends DistributedManager {
    Store getStore();
    void removeSuper ( Session p0 );
}
