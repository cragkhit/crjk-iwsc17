package org.apache.tomcat.util.net;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.Collection;
import java.util.ArrayList;
import javax.net.ssl.SSLParameters;
import javax.net.ssl.SSLEngine;
import org.apache.tomcat.util.net.openssl.ciphers.Cipher;
import java.util.List;
import javax.net.ssl.SSLSessionContext;
import java.security.SecureRandom;
import java.util.Iterator;
import org.apache.tomcat.util.net.openssl.OpenSSLImplementation;
public abstract class AbstractJsseEndpoint<S> extends AbstractEndpoint<S> {
    private String sslImplementationName;
    private int sniParseLimit;
    private SSLImplementation sslImplementation;
    public AbstractJsseEndpoint() {
        this.sslImplementationName = null;
        this.sniParseLimit = 65536;
        this.sslImplementation = null;
    }
    public String getSslImplementationName() {
        return this.sslImplementationName;
    }
    public void setSslImplementationName ( final String s ) {
        this.sslImplementationName = s;
    }
    public SSLImplementation getSslImplementation() {
        return this.sslImplementation;
    }
    public int getSniParseLimit() {
        return this.sniParseLimit;
    }
    public void setSniParseLimit ( final int sniParseLimit ) {
        this.sniParseLimit = sniParseLimit;
    }
    @Override
    protected SSLHostConfig.Type getSslConfigType() {
        if ( OpenSSLImplementation.class.getName().equals ( this.sslImplementationName ) ) {
            return SSLHostConfig.Type.EITHER;
        }
        return SSLHostConfig.Type.JSSE;
    }
    protected void initialiseSsl() throws Exception {
        if ( this.isSSLEnabled() ) {
            this.sslImplementation = SSLImplementation.getInstance ( this.getSslImplementationName() );
            for ( final SSLHostConfig sslHostConfig : this.sslHostConfigs.values() ) {
                this.createSSLContext ( sslHostConfig );
            }
        }
    }
    @Override
    protected void createSSLContext ( final SSLHostConfig sslHostConfig ) throws IllegalArgumentException {
        boolean firstCertificate = true;
        for ( final SSLHostConfigCertificate certificate : sslHostConfig.getCertificates ( true ) ) {
            final SSLUtil sslUtil = this.sslImplementation.getSSLUtil ( certificate );
            if ( firstCertificate ) {
                firstCertificate = false;
                sslHostConfig.setEnabledProtocols ( sslUtil.getEnabledProtocols() );
                sslHostConfig.setEnabledCiphers ( sslUtil.getEnabledCiphers() );
            }
            SSLContext sslContext;
            try {
                sslContext = sslUtil.createSSLContext ( this.negotiableProtocols );
                sslContext.init ( sslUtil.getKeyManagers(), sslUtil.getTrustManagers(), null );
            } catch ( Exception e ) {
                throw new IllegalArgumentException ( e );
            }
            final SSLSessionContext sessionContext = sslContext.getServerSessionContext();
            if ( sessionContext != null ) {
                sslUtil.configureSessionContext ( sessionContext );
            }
            certificate.setSslContext ( sslContext );
        }
    }
    protected void destroySsl() throws Exception {
        if ( this.isSSLEnabled() ) {
            for ( final SSLHostConfig sslHostConfig : this.sslHostConfigs.values() ) {
                this.releaseSSLContext ( sslHostConfig );
            }
        }
    }
    @Override
    protected void releaseSSLContext ( final SSLHostConfig sslHostConfig ) {
        for ( final SSLHostConfigCertificate certificate : sslHostConfig.getCertificates ( true ) ) {
            if ( certificate.getSslContext() != null ) {
                final SSLContext sslContext = certificate.getSslContext();
                if ( sslContext == null ) {
                    continue;
                }
                sslContext.destroy();
            }
        }
    }
    protected SSLEngine createSSLEngine ( final String sniHostName, final List<Cipher> clientRequestedCiphers ) {
        final SSLHostConfig sslHostConfig = this.getSSLHostConfig ( sniHostName );
        final SSLHostConfigCertificate certificate = this.selectCertificate ( sslHostConfig, clientRequestedCiphers );
        final SSLContext sslContext = certificate.getSslContext();
        if ( sslContext == null ) {
            throw new IllegalStateException ( AbstractJsseEndpoint.sm.getString ( "endpoint.jsse.noSslContext", sniHostName ) );
        }
        final SSLEngine engine = sslContext.createSSLEngine();
        switch ( sslHostConfig.getCertificateVerification() ) {
        case NONE: {
            engine.setNeedClientAuth ( false );
            engine.setWantClientAuth ( false );
            break;
        }
        case OPTIONAL:
        case OPTIONAL_NO_CA: {
            engine.setWantClientAuth ( true );
            break;
        }
        case REQUIRED: {
            engine.setNeedClientAuth ( true );
            break;
        }
        }
        engine.setUseClientMode ( false );
        engine.setEnabledCipherSuites ( sslHostConfig.getEnabledCiphers() );
        engine.setEnabledProtocols ( sslHostConfig.getEnabledProtocols() );
        final SSLParameters sslParameters = engine.getSSLParameters();
        sslParameters.setUseCipherSuitesOrder ( sslHostConfig.getHonorCipherOrder() );
        engine.setSSLParameters ( sslParameters );
        return engine;
    }
    private SSLHostConfigCertificate selectCertificate ( final SSLHostConfig sslHostConfig, final List<Cipher> clientCiphers ) {
        final Set<SSLHostConfigCertificate> certificates = sslHostConfig.getCertificates ( true );
        if ( certificates.size() == 1 ) {
            return certificates.iterator().next();
        }
        final LinkedHashSet<Cipher> serverCiphers = sslHostConfig.getCipherList();
        final List<Cipher> candidateCiphers = new ArrayList<Cipher>();
        if ( sslHostConfig.getHonorCipherOrder() ) {
            candidateCiphers.addAll ( serverCiphers );
            candidateCiphers.retainAll ( clientCiphers );
        } else {
            candidateCiphers.addAll ( clientCiphers );
            candidateCiphers.retainAll ( serverCiphers );
        }
        for ( final Cipher candidate : candidateCiphers ) {
            for ( final SSLHostConfigCertificate certificate : certificates ) {
                if ( certificate.getType().isCompatibleWith ( candidate.getAu() ) ) {
                    return certificate;
                }
            }
        }
        return certificates.iterator().next();
    }
    @Override
    public void unbind() throws Exception {
        for ( final SSLHostConfig sslHostConfig : this.sslHostConfigs.values() ) {
            for ( final SSLHostConfigCertificate certificate : sslHostConfig.getCertificates ( true ) ) {
                certificate.setSslContext ( null );
            }
        }
    }
}
