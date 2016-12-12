package org.apache.catalina.tribes.io;
public interface BufferPoolAPI {
    void setMaxSize ( int p0 );
    XByteBuffer getBuffer ( int p0, boolean p1 );
    void returnBuffer ( XByteBuffer p0 );
    void clear();
}
