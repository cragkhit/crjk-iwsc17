package org.apache.catalina.ha.deploy;
import java.io.File;
public interface FileChangeListener {
    void fileModified ( File p0 );
    void fileRemoved ( File p0 );
}
