package org.apache.tomcat.util.net;
import org.apache.juli.logging.LogFactory;
import java.io.InputStream;
import java.io.IOException;
import java.io.FileNotFoundException;
import org.apache.tomcat.util.file.ConfigFileLoader;
import java.security.KeyStore;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.Collection;
import org.apache.tomcat.util.res.StringManager;
import org.apache.juli.logging.Log;
public abstract class SSLUtilBase implements SSLUtil {
    private static final Log log;
    private static final StringManager sm;
    protected final SSLHostConfigCertificate certificate;
    private final String[] enabledProtocols;
    private final String[] enabledCiphers;
    protected SSLUtilBase ( final SSLHostConfigCertificate certificate ) {
        this.certificate = certificate;
        final SSLHostConfig sslHostConfig = certificate.getSSLHostConfig();
        final Set<String> configuredProtocols = sslHostConfig.getProtocols();
        final Set<String> implementedProtocols = this.getImplementedProtocols();
        final List<String> enabledProtocols = getEnabled ( "protocols", this.getLog(), true, configuredProtocols, implementedProtocols );
        this.enabledProtocols = enabledProtocols.toArray ( new String[enabledProtocols.size()] );
        final List<String> configuredCiphers = sslHostConfig.getJsseCipherNames();
        final Set<String> implementedCiphers = this.getImplementedCiphers();
        final List<String> enabledCiphers = getEnabled ( "ciphers", this.getLog(), false, configuredCiphers, implementedCiphers );
        this.enabledCiphers = enabledCiphers.toArray ( new String[enabledCiphers.size()] );
    }
    static <T> List<T> getEnabled ( final String name, final Log log, final boolean warnOnSkip, final Collection<T> configured, final Collection<T> implemented ) {
        final List<T> enabled = new ArrayList<T>();
        if ( implemented.size() == 0 ) {
            enabled.addAll ( ( Collection<? extends T> ) configured );
        } else {
            enabled.addAll ( ( Collection<? extends T> ) configured );
            enabled.retainAll ( implemented );
            if ( enabled.isEmpty() ) {
                throw new IllegalArgumentException ( SSLUtilBase.sm.getString ( "sslUtilBase.noneSupported", name, configured ) );
            }
            if ( log.isDebugEnabled() ) {
                log.debug ( SSLUtilBase.sm.getString ( "sslUtilBase.active", name, enabled ) );
            }
            if ( ( log.isDebugEnabled() || warnOnSkip ) && enabled.size() != configured.size() ) {
                final List<T> skipped = new ArrayList<T>();
                skipped.addAll ( ( Collection<? extends T> ) configured );
                skipped.removeAll ( enabled );
                final String msg = SSLUtilBase.sm.getString ( "sslUtilBase.skipped", name, skipped );
                if ( warnOnSkip ) {
                    log.warn ( msg );
                } else {
                    log.debug ( msg );
                }
            }
        }
        return enabled;
    }
    static KeyStore getStore ( final String type, final String provider, final String path, final String pass ) throws IOException {
        KeyStore ks = null;
        InputStream istream = null;
        try {
            if ( provider == null ) {
                ks = KeyStore.getInstance ( type );
            } else {
                ks = KeyStore.getInstance ( type, provider );
            }
            if ( ( !"PKCS11".equalsIgnoreCase ( type ) && !"".equalsIgnoreCase ( path ) ) || "NONE".equalsIgnoreCase ( path ) ) {
                istream = ConfigFileLoader.getInputStream ( path );
            }
            char[] storePass = null;
            if ( pass != null && !"".equals ( pass ) ) {
                storePass = pass.toCharArray();
            }
            ks.load ( istream, storePass );
        } catch ( FileNotFoundException fnfe ) {
            SSLUtilBase.log.error ( SSLUtilBase.sm.getString ( "jsse.keystore_load_failed", type, path, fnfe.getMessage() ), fnfe );
            throw fnfe;
        } catch ( IOException ioe ) {
            throw ioe;
        } catch ( Exception ex ) {
            final String msg = SSLUtilBase.sm.getString ( "jsse.keystore_load_failed", type, path, ex.getMessage() );
            SSLUtilBase.log.error ( msg, ex );
            throw new IOException ( msg );
        } finally {
            if ( istream != null ) {
                try {
                    istream.close();
                } catch ( IOException ex2 ) {}
            }
        }
        return ks;
    }
    @Override
    public String[] getEnabledProtocols() {
        return this.enabledProtocols;
    }
    @Override
    public String[] getEnabledCiphers() {
        return this.enabledCiphers;
    }
    protected abstract Set<String> getImplementedProtocols();
    protected abstract Set<String> getImplementedCiphers();
    protected abstract Log getLog();
    static {
        log = LogFactory.getLog ( SSLUtilBase.class );
        sm = StringManager.getManager ( SSLUtilBase.class );
    }
}
