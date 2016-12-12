package org.apache.catalina.tribes;
public interface Member {
    public static final byte[] SHUTDOWN_PAYLOAD = new byte[] {66, 65, 66, 89, 45, 65, 76, 69, 88};
    public String getName();
    public byte[] getHost();
    public int getPort();
    public int getSecurePort();
    public int getUdpPort();
    public long getMemberAliveTime();
    public void setMemberAliveTime ( long memberAliveTime );
    public boolean isReady();
    public boolean isSuspect();
    public boolean isFailing();
    public byte[] getUniqueId();
    public byte[] getPayload();
    public void setPayload ( byte[] payload );
    public byte[] getCommand();
    public void setCommand ( byte[] command );
    public byte[] getDomain();
    public byte[] getData ( boolean getalive );
    public byte[] getData ( boolean getalive, boolean reset );
    public int getDataLength();
    public boolean isLocal();
    public void setLocal ( boolean local );
}
