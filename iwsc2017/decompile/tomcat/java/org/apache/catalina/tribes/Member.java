package org.apache.catalina.tribes;
public interface Member {
    public static final byte[] SHUTDOWN_PAYLOAD = { 66, 65, 66, 89, 45, 65, 76, 69, 88 };
    String getName();
    byte[] getHost();
    int getPort();
    int getSecurePort();
    int getUdpPort();
    long getMemberAliveTime();
    void setMemberAliveTime ( long p0 );
    boolean isReady();
    boolean isSuspect();
    boolean isFailing();
    byte[] getUniqueId();
    byte[] getPayload();
    void setPayload ( byte[] p0 );
    byte[] getCommand();
    void setCommand ( byte[] p0 );
    byte[] getDomain();
    byte[] getData ( boolean p0 );
    byte[] getData ( boolean p0, boolean p1 );
    int getDataLength();
    boolean isLocal();
    void setLocal ( boolean p0 );
}
