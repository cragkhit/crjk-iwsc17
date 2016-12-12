package org.apache.tomcat.jni;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
public final class SSLContext {
    public static native long make ( long pool, int protocol, int mode ) throws Exception;
    public static native int free ( long ctx );
    public static native void setContextId ( long ctx, String id );
    public static native void setBIO ( long ctx, long bio, int dir );
    public static native void setOptions ( long ctx, int options );
    public static native int getOptions ( long ctx );
    public static native void clearOptions ( long ctx, int options );
    public static native void setQuietShutdown ( long ctx, boolean mode );
    public static native boolean setCipherSuite ( long ctx, String ciphers )
    throws Exception;
    public static native boolean setCARevocation ( long ctx, String file,
            String path )
    throws Exception;
    public static native boolean setCertificateChainFile ( long ctx, String file,
            boolean skipfirst );
    public static native boolean setCertificate ( long ctx, String cert,
            String key, String password,
            int idx )
    throws Exception;
    public static native long setSessionCacheSize ( long ctx, long size );
    public static native long getSessionCacheSize ( long ctx );
    public static native long setSessionCacheTimeout ( long ctx, long timeoutSeconds );
    public static native long getSessionCacheTimeout ( long ctx );
    public static native long setSessionCacheMode ( long ctx, long mode );
    public static native long getSessionCacheMode ( long ctx );
    public static native long sessionAccept ( long ctx );
    public static native long sessionAcceptGood ( long ctx );
    public static native long sessionAcceptRenegotiate ( long ctx );
    public static native long sessionCacheFull ( long ctx );
    public static native long sessionCbHits ( long ctx );
    public static native long sessionConnect ( long ctx );
    public static native long sessionConnectGood ( long ctx );
    public static native long sessionConnectRenegotiate ( long ctx );
    public static native long sessionHits ( long ctx );
    public static native long sessionMisses ( long ctx );
    public static native long sessionNumber ( long ctx );
    public static native long sessionTimeouts ( long ctx );
    public static native void setSessionTicketKeys ( long ctx, byte[] keys );
    public static native boolean setCACertificate ( long ctx, String file,
            String path )
    throws Exception;
    public static native void setRandom ( long ctx, String file );
    public static native void setShutdownType ( long ctx, int type );
    public static native void setVerify ( long ctx, int level, int depth );
    public static native int setALPN ( long ctx, byte[] proto, int len );
    public static long sniCallBack ( long currentCtx, String sniHostName ) {
        SNICallBack sniCallBack = sniCallBacks.get ( Long.valueOf ( currentCtx ) );
        if ( sniCallBack == null ) {
            return 0;
        }
        return sniCallBack.getSslContext ( sniHostName );
    }
    private static final Map<Long, SNICallBack> sniCallBacks = new ConcurrentHashMap<>();
    public static void registerDefault ( Long defaultSSLContext,
                                         SNICallBack sniCallBack ) {
        sniCallBacks.put ( defaultSSLContext, sniCallBack );
    }
    public static void unregisterDefault ( Long defaultSSLContext ) {
        sniCallBacks.remove ( defaultSSLContext );
    }
    public static interface SNICallBack {
        public long getSslContext ( String sniHostName );
    }
    public static native void setCertVerifyCallback ( long ctx, CertificateVerifier verifier );
    @Deprecated
    public static void setNextProtos ( long ctx, String nextProtos ) {
        setNpnProtos ( ctx, nextProtos.split ( "," ), SSL.SSL_SELECTOR_FAILURE_CHOOSE_MY_LAST_PROTOCOL );
    }
    public static native void setNpnProtos ( long ctx, String[] nextProtos, int selectorFailureBehavior );
    public static native void setAlpnProtos ( long ctx, String[] alpnProtos, int selectorFailureBehavior );
    public static native void setTmpDH ( long ctx, String cert )
    throws Exception;
    public static native void setTmpECDHByCurveName ( long ctx, String curveName )
    throws Exception;
    public static native boolean setSessionIdContext ( long ctx, byte[] sidCtx );
    public static native boolean setCertificateRaw ( long ctx, byte[] cert, byte[] key, int sslAidxRsa );
    public static native boolean addChainCertificateRaw ( long ctx, byte[] cert );
}
