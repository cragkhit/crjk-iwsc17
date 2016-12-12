package org.apache.catalina.tribes;
import java.io.Serializable;
import org.apache.catalina.tribes.io.XByteBuffer;
public interface ChannelMessage extends Serializable {
    public Member getAddress();
    public void setAddress ( Member member );
    public long getTimestamp();
    public void setTimestamp ( long timestamp );
    public byte[] getUniqueId();
    public void setMessage ( XByteBuffer buf );
    public XByteBuffer getMessage();
    public int getOptions();
    public void setOptions ( int options );
    public Object clone();
    public Object deepclone();
}
