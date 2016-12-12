package org.apache.tomcat.jni;
public interface ProcErrorCallback {
    public void callback ( long pool, int err, String description );
}
