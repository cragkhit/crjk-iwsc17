package org.apache.tomcat.util.net.openssl;
import org.apache.tomcat.util.net.openssl.ciphers.OpenSSLCipherConfigurationParser;
import java.security.Principal;
import javax.security.cert.CertificateException;
import javax.security.cert.X509Certificate;
import javax.net.ssl.SSLPeerUnverifiedException;
import java.security.cert.Certificate;
import javax.net.ssl.SSLSessionBindingEvent;
import javax.net.ssl.SSLSessionBindingListener;
import java.util.HashMap;
import javax.net.ssl.SSLSessionContext;
import org.apache.tomcat.jni.SSL;
import java.util.Map;
import javax.net.ssl.SSLSession;
private class OpenSSLSession implements SSLSession {
    private Map<String, Object> values;
    private long lastAccessedTime;
    private OpenSSLSession() {
        this.lastAccessedTime = -1L;
    }
    @Override
    public byte[] getId() {
        final byte[] id = SSL.getSessionId ( OpenSSLEngine.access$200 ( OpenSSLEngine.this ) );
        if ( id == null ) {
            throw new IllegalStateException ( OpenSSLEngine.access$300().getString ( "engine.noSession" ) );
        }
        return id;
    }
    @Override
    public SSLSessionContext getSessionContext() {
        return OpenSSLEngine.access$400 ( OpenSSLEngine.this );
    }
    @Override
    public long getCreationTime() {
        return SSL.getTime ( OpenSSLEngine.access$200 ( OpenSSLEngine.this ) ) * 1000L;
    }
    @Override
    public long getLastAccessedTime() {
        return ( this.lastAccessedTime > 0L ) ? this.lastAccessedTime : this.getCreationTime();
    }
    @Override
    public void invalidate() {
    }
    @Override
    public boolean isValid() {
        return false;
    }
    @Override
    public void putValue ( final String name, final Object value ) {
        if ( name == null ) {
            throw new IllegalArgumentException ( OpenSSLEngine.access$300().getString ( "engine.nullName" ) );
        }
        if ( value == null ) {
            throw new IllegalArgumentException ( OpenSSLEngine.access$300().getString ( "engine.nullValue" ) );
        }
        Map<String, Object> values = this.values;
        if ( values == null ) {
            final HashMap<String, Object> values2 = new HashMap<String, Object> ( 2 );
            this.values = values2;
            values = values2;
        }
        final Object old = values.put ( name, value );
        if ( value instanceof SSLSessionBindingListener ) {
            ( ( SSLSessionBindingListener ) value ).valueBound ( new SSLSessionBindingEvent ( this, name ) );
        }
        this.notifyUnbound ( old, name );
    }
    @Override
    public Object getValue ( final String name ) {
        if ( name == null ) {
            throw new IllegalArgumentException ( OpenSSLEngine.access$300().getString ( "engine.nullName" ) );
        }
        if ( this.values == null ) {
            return null;
        }
        return this.values.get ( name );
    }
    @Override
    public void removeValue ( final String name ) {
        if ( name == null ) {
            throw new IllegalArgumentException ( OpenSSLEngine.access$300().getString ( "engine.nullName" ) );
        }
        final Map<String, Object> values = this.values;
        if ( values == null ) {
            return;
        }
        final Object old = values.remove ( name );
        this.notifyUnbound ( old, name );
    }
    @Override
    public String[] getValueNames() {
        final Map<String, Object> values = this.values;
        if ( values == null || values.isEmpty() ) {
            return new String[0];
        }
        return values.keySet().toArray ( new String[values.size()] );
    }
    private void notifyUnbound ( final Object value, final String name ) {
        if ( value instanceof SSLSessionBindingListener ) {
            ( ( SSLSessionBindingListener ) value ).valueUnbound ( new SSLSessionBindingEvent ( this, name ) );
        }
    }
    @Override
    public Certificate[] getPeerCertificates() throws SSLPeerUnverifiedException {
        Certificate[] c = OpenSSLEngine.access$500 ( OpenSSLEngine.this );
        if ( c == null ) {
            if ( SSL.isInInit ( OpenSSLEngine.access$200 ( OpenSSLEngine.this ) ) != 0 ) {
                throw new SSLPeerUnverifiedException ( OpenSSLEngine.access$300().getString ( "engine.unverifiedPeer" ) );
            }
            final byte[][] chain = SSL.getPeerCertChain ( OpenSSLEngine.access$200 ( OpenSSLEngine.this ) );
            byte[] clientCert;
            if ( !OpenSSLEngine.access$600 ( OpenSSLEngine.this ) ) {
                clientCert = SSL.getPeerCertificate ( OpenSSLEngine.access$200 ( OpenSSLEngine.this ) );
            } else {
                clientCert = null;
            }
            if ( chain == null && clientCert == null ) {
                return null;
            }
            int len = 0;
            if ( chain != null ) {
                len += chain.length;
            }
            int i = 0;
            Certificate[] certificates;
            if ( clientCert != null ) {
                certificates = new Certificate[++len];
                certificates[i++] = new OpenSslX509Certificate ( clientCert );
            } else {
                certificates = new Certificate[len];
            }
            if ( chain != null ) {
                int a = 0;
                while ( i < certificates.length ) {
                    certificates[i] = new OpenSslX509Certificate ( chain[a++] );
                    ++i;
                }
            }
            c = OpenSSLEngine.access$502 ( OpenSSLEngine.this, certificates );
        }
        return c;
    }
    @Override
    public Certificate[] getLocalCertificates() {
        return OpenSSLEngine.access$700();
    }
    @Override
    public X509Certificate[] getPeerCertificateChain() throws SSLPeerUnverifiedException {
        X509Certificate[] c = OpenSSLEngine.access$800 ( OpenSSLEngine.this );
        if ( c == null ) {
            if ( SSL.isInInit ( OpenSSLEngine.access$200 ( OpenSSLEngine.this ) ) != 0 ) {
                throw new SSLPeerUnverifiedException ( OpenSSLEngine.access$300().getString ( "engine.unverifiedPeer" ) );
            }
            final byte[][] chain = SSL.getPeerCertChain ( OpenSSLEngine.access$200 ( OpenSSLEngine.this ) );
            if ( chain == null ) {
                throw new SSLPeerUnverifiedException ( OpenSSLEngine.access$300().getString ( "engine.unverifiedPeer" ) );
            }
            final X509Certificate[] peerCerts = new X509Certificate[chain.length];
            for ( int i = 0; i < peerCerts.length; ++i ) {
                try {
                    peerCerts[i] = X509Certificate.getInstance ( chain[i] );
                } catch ( CertificateException e ) {
                    throw new IllegalStateException ( e );
                }
            }
            c = OpenSSLEngine.access$802 ( OpenSSLEngine.this, peerCerts );
        }
        return c;
    }
    @Override
    public Principal getPeerPrincipal() throws SSLPeerUnverifiedException {
        final Certificate[] peer = this.getPeerCertificates();
        if ( peer == null || peer.length == 0 ) {
            return null;
        }
        return this.principal ( peer );
    }
    @Override
    public Principal getLocalPrincipal() {
        final Certificate[] local = this.getLocalCertificates();
        if ( local == null || local.length == 0 ) {
            return null;
        }
        return this.principal ( local );
    }
    private Principal principal ( final Certificate[] certs ) {
        return ( ( java.security.cert.X509Certificate ) certs[0] ).getIssuerX500Principal();
    }
    @Override
    public String getCipherSuite() {
        if ( !OpenSSLEngine.access$900 ( OpenSSLEngine.this ) ) {
            return "SSL_NULL_WITH_NULL_NULL";
        }
        if ( OpenSSLEngine.access$1000 ( OpenSSLEngine.this ) == null ) {
            final String c = OpenSSLCipherConfigurationParser.openSSLToJsse ( SSL.getCipherForSSL ( OpenSSLEngine.access$200 ( OpenSSLEngine.this ) ) );
            if ( c != null ) {
                OpenSSLEngine.access$1002 ( OpenSSLEngine.this, c );
            }
        }
        return OpenSSLEngine.access$1000 ( OpenSSLEngine.this );
    }
    @Override
    public String getProtocol() {
        String applicationProtocol = OpenSSLEngine.access$1100 ( OpenSSLEngine.this );
        if ( applicationProtocol == null ) {
            applicationProtocol = SSL.getNextProtoNegotiated ( OpenSSLEngine.access$200 ( OpenSSLEngine.this ) );
            if ( applicationProtocol == null ) {
                applicationProtocol = OpenSSLEngine.access$1200 ( OpenSSLEngine.this );
            }
            if ( applicationProtocol != null ) {
                OpenSSLEngine.access$1102 ( OpenSSLEngine.this, applicationProtocol.replace ( ':', '_' ) );
            } else {
                OpenSSLEngine.access$1102 ( OpenSSLEngine.this, applicationProtocol = "" );
            }
        }
        final String version = SSL.getVersion ( OpenSSLEngine.access$200 ( OpenSSLEngine.this ) );
        if ( applicationProtocol.isEmpty() ) {
            return version;
        }
        return version + ':' + applicationProtocol;
    }
    @Override
    public String getPeerHost() {
        return null;
    }
    @Override
    public int getPeerPort() {
        return 0;
    }
    @Override
    public int getPacketBufferSize() {
        return 18713;
    }
    @Override
    public int getApplicationBufferSize() {
        return 16384;
    }
}
