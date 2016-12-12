package org.apache.tomcat.jni;
import java.util.concurrent.ConcurrentHashMap;
import java.util.Map;
public final class SSLContext {
    private static final Map<Long, SNICallBack> sniCallBacks;
    public static native long make ( final long p0, final int p1, final int p2 ) throws Exception;
    public static native int free ( final long p0 );
    public static native void setContextId ( final long p0, final String p1 );
    public static native void setBIO ( final long p0, final long p1, final int p2 );
    public static native void setOptions ( final long p0, final int p1 );
    public static native int getOptions ( final long p0 );
    public static native void clearOptions ( final long p0, final int p1 );
    public static native void setQuietShutdown ( final long p0, final boolean p1 );
    public static native boolean setCipherSuite ( final long p0, final String p1 ) throws Exception;
    public static native boolean setCARevocation ( final long p0, final String p1, final String p2 ) throws Exception;
    public static native boolean setCertificateChainFile ( final long p0, final String p1, final boolean p2 );
    public static native boolean setCertificate ( final long p0, final String p1, final String p2, final String p3, final int p4 ) throws Exception;
    public static native long setSessionCacheSize ( final long p0, final long p1 );
    public static native long getSessionCacheSize ( final long p0 );
    public static native long setSessionCacheTimeout ( final long p0, final long p1 );
    public static native long getSessionCacheTimeout ( final long p0 );
    public static native long setSessionCacheMode ( final long p0, final long p1 );
    public static native long getSessionCacheMode ( final long p0 );
    public static native long sessionAccept ( final long p0 );
    public static native long sessionAcceptGood ( final long p0 );
    public static native long sessionAcceptRenegotiate ( final long p0 );
    public static native long sessionCacheFull ( final long p0 );
    public static native long sessionCbHits ( final long p0 );
    public static native long sessionConnect ( final long p0 );
    public static native long sessionConnectGood ( final long p0 );
    public static native long sessionConnectRenegotiate ( final long p0 );
    public static native long sessionHits ( final long p0 );
    public static native long sessionMisses ( final long p0 );
    public static native long sessionNumber ( final long p0 );
    public static native long sessionTimeouts ( final long p0 );
    public static native void setSessionTicketKeys ( final long p0, final byte[] p1 );
    public static native boolean setCACertificate ( final long p0, final String p1, final String p2 ) throws Exception;
    public static native void setRandom ( final long p0, final String p1 );
    public static native void setShutdownType ( final long p0, final int p1 );
    public static native void setVerify ( final long p0, final int p1, final int p2 );
    public static native int setALPN ( final long p0, final byte[] p1, final int p2 );
    public static long sniCallBack ( final long currentCtx, final String sniHostName ) {
        final SNICallBack sniCallBack = SSLContext.sniCallBacks.get ( currentCtx );
        if ( sniCallBack == null ) {
            return 0L;
        }
        return sniCallBack.getSslContext ( sniHostName );
    }
    public static void registerDefault ( final Long defaultSSLContext, final SNICallBack sniCallBack ) {
        SSLContext.sniCallBacks.put ( defaultSSLContext, sniCallBack );
    }
    public static void unregisterDefault ( final Long defaultSSLContext ) {
        SSLContext.sniCallBacks.remove ( defaultSSLContext );
    }
    public static native void setCertVerifyCallback ( final long p0, final CertificateVerifier p1 );
    @Deprecated
    public static void setNextProtos ( final long ctx, final String nextProtos ) {
        setNpnProtos ( ctx, nextProtos.split ( "," ), 1 );
    }
    public static native void setNpnProtos ( final long p0, final String[] p1, final int p2 );
    public static native void setAlpnProtos ( final long p0, final String[] p1, final int p2 );
    public static native void setTmpDH ( final long p0, final String p1 ) throws Exception;
    public static native void setTmpECDHByCurveName ( final long p0, final String p1 ) throws Exception;
    public static native boolean setSessionIdContext ( final long p0, final byte[] p1 );
    public static native boolean setCertificateRaw ( final long p0, final byte[] p1, final byte[] p2, final int p3 );
    public static native boolean addChainCertificateRaw ( final long p0, final byte[] p1 );
    static {
        sniCallBacks = new ConcurrentHashMap<Long, SNICallBack>();
    }
    public interface SNICallBack {
        long getSslContext ( String p0 );
    }
}
