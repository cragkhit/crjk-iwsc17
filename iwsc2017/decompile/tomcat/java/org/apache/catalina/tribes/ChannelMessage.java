package org.apache.catalina.tribes;
import org.apache.catalina.tribes.io.XByteBuffer;
import java.io.Serializable;
public interface ChannelMessage extends Serializable {
    Member getAddress();
    void setAddress ( Member p0 );
    long getTimestamp();
    void setTimestamp ( long p0 );
    byte[] getUniqueId();
    void setMessage ( XByteBuffer p0 );
    XByteBuffer getMessage();
    int getOptions();
    void setOptions ( int p0 );
    Object clone();
    Object deepclone();
}
