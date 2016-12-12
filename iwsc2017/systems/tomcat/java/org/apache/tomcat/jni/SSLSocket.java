package org.apache.tomcat.jni;
public class SSLSocket {
    public static native int attach ( long ctx, long sock )
    throws Exception;
    public static native int handshake ( long thesocket );
    public static native int renegotiate ( long thesocket );
    public static native void setVerify ( long sock, int level, int depth );
    public static native byte[] getInfoB ( long sock, int id )
    throws Exception;
    public static native String getInfoS ( long sock, int id )
    throws Exception;
    public static native int getInfoI ( long sock, int id )
    throws Exception;
    public static native int getALPN ( long sock, byte[] negotiatedProtocol );
}
