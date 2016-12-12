package org.apache.coyote.http11;
import javax.servlet.http.HttpUpgradeHandler;
import org.apache.coyote.http11.upgrade.UpgradeProcessorExternal;
import org.apache.coyote.http11.upgrade.UpgradeProcessorInternal;
import org.apache.coyote.http11.upgrade.InternalHttpUpgradeHandler;
import org.apache.coyote.UpgradeToken;
import org.apache.tomcat.util.net.SocketWrapperBase;
import org.apache.coyote.Processor;
import java.util.Locale;
import java.util.Collection;
import java.util.HashSet;
import java.util.StringTokenizer;
import java.util.Iterator;
import java.util.HashMap;
import java.util.ArrayList;
import java.util.Collections;
import java.util.concurrent.ConcurrentHashMap;
import org.apache.tomcat.util.net.AbstractEndpoint;
import org.apache.tomcat.util.net.SSLHostConfig;
import java.util.Map;
import org.apache.coyote.UpgradeProtocol;
import java.util.List;
import java.util.Set;
import org.apache.tomcat.util.res.StringManager;
import org.apache.coyote.AbstractProtocol;
public abstract class AbstractHttp11Protocol<S> extends AbstractProtocol<S> {
    protected static final StringManager sm;
    private int maxSavePostSize;
    private int maxHttpHeaderSize;
    private int connectionUploadTimeout;
    private boolean disableUploadTimeout;
    private String compression;
    private String noCompressionUserAgents;
    private String compressableMimeType;
    private String[] compressableMimeTypes;
    private int compressionMinSize;
    private String restrictedUserAgents;
    private String server;
    private boolean serverRemoveAppProvidedValues;
    private int maxTrailerSize;
    private int maxExtensionSize;
    private int maxSwallowSize;
    private boolean secure;
    private Set<String> allowedTrailerHeaders;
    private final List<UpgradeProtocol> upgradeProtocols;
    private final Map<String, UpgradeProtocol> httpUpgradeProtocols;
    private final Map<String, UpgradeProtocol> negotiatedProtocols;
    private SSLHostConfig defaultSSLHostConfig;
    public AbstractHttp11Protocol ( final AbstractEndpoint<S> endpoint ) {
        super ( endpoint );
        this.maxSavePostSize = 4096;
        this.maxHttpHeaderSize = 8192;
        this.connectionUploadTimeout = 300000;
        this.disableUploadTimeout = true;
        this.compression = "off";
        this.noCompressionUserAgents = null;
        this.compressableMimeType = "text/html,text/xml,text/plain,text/css,text/javascript,application/javascript";
        this.compressableMimeTypes = null;
        this.compressionMinSize = 2048;
        this.restrictedUserAgents = null;
        this.serverRemoveAppProvidedValues = false;
        this.maxTrailerSize = 8192;
        this.maxExtensionSize = 8192;
        this.maxSwallowSize = 2097152;
        this.allowedTrailerHeaders = Collections.newSetFromMap ( new ConcurrentHashMap<String, Boolean>() );
        this.upgradeProtocols = new ArrayList<UpgradeProtocol>();
        this.httpUpgradeProtocols = new HashMap<String, UpgradeProtocol>();
        this.negotiatedProtocols = new HashMap<String, UpgradeProtocol>();
        this.defaultSSLHostConfig = null;
        this.setConnectionTimeout ( 60000 );
        final ConnectionHandler<S> cHandler = new ConnectionHandler<S> ( this );
        this.setHandler ( cHandler );
        this.getEndpoint().setHandler ( cHandler );
    }
    @Override
    public void init() throws Exception {
        for ( final UpgradeProtocol upgradeProtocol : this.upgradeProtocols ) {
            this.configureUpgradeProtocol ( upgradeProtocol );
        }
        super.init();
    }
    @Override
    protected String getProtocolName() {
        return "Http";
    }
    @Override
    protected AbstractEndpoint<S> getEndpoint() {
        return super.getEndpoint();
    }
    public int getMaxSavePostSize() {
        return this.maxSavePostSize;
    }
    public void setMaxSavePostSize ( final int valueI ) {
        this.maxSavePostSize = valueI;
    }
    public int getMaxHttpHeaderSize() {
        return this.maxHttpHeaderSize;
    }
    public void setMaxHttpHeaderSize ( final int valueI ) {
        this.maxHttpHeaderSize = valueI;
    }
    public int getConnectionUploadTimeout() {
        return this.connectionUploadTimeout;
    }
    public void setConnectionUploadTimeout ( final int i ) {
        this.connectionUploadTimeout = i;
    }
    public boolean getDisableUploadTimeout() {
        return this.disableUploadTimeout;
    }
    public void setDisableUploadTimeout ( final boolean isDisabled ) {
        this.disableUploadTimeout = isDisabled;
    }
    public String getCompression() {
        return this.compression;
    }
    public void setCompression ( final String valueS ) {
        this.compression = valueS;
    }
    public String getNoCompressionUserAgents() {
        return this.noCompressionUserAgents;
    }
    public void setNoCompressionUserAgents ( final String valueS ) {
        this.noCompressionUserAgents = valueS;
    }
    public String getCompressableMimeType() {
        return this.compressableMimeType;
    }
    public void setCompressableMimeType ( final String valueS ) {
        this.compressableMimeType = valueS;
        this.compressableMimeTypes = null;
    }
    public String[] getCompressableMimeTypes() {
        String[] result = this.compressableMimeTypes;
        if ( result != null ) {
            return result;
        }
        final List<String> values = new ArrayList<String>();
        final StringTokenizer tokens = new StringTokenizer ( this.compressableMimeType, "," );
        while ( tokens.hasMoreTokens() ) {
            final String token = tokens.nextToken().trim();
            if ( token.length() > 0 ) {
                values.add ( token );
            }
        }
        result = values.toArray ( new String[values.size()] );
        return this.compressableMimeTypes = result;
    }
    public int getCompressionMinSize() {
        return this.compressionMinSize;
    }
    public void setCompressionMinSize ( final int valueI ) {
        this.compressionMinSize = valueI;
    }
    public String getRestrictedUserAgents() {
        return this.restrictedUserAgents;
    }
    public void setRestrictedUserAgents ( final String valueS ) {
        this.restrictedUserAgents = valueS;
    }
    public String getServer() {
        return this.server;
    }
    public void setServer ( final String server ) {
        this.server = server;
    }
    public boolean getServerRemoveAppProvidedValues() {
        return this.serverRemoveAppProvidedValues;
    }
    public void setServerRemoveAppProvidedValues ( final boolean serverRemoveAppProvidedValues ) {
        this.serverRemoveAppProvidedValues = serverRemoveAppProvidedValues;
    }
    public int getMaxTrailerSize() {
        return this.maxTrailerSize;
    }
    public void setMaxTrailerSize ( final int maxTrailerSize ) {
        this.maxTrailerSize = maxTrailerSize;
    }
    public int getMaxExtensionSize() {
        return this.maxExtensionSize;
    }
    public void setMaxExtensionSize ( final int maxExtensionSize ) {
        this.maxExtensionSize = maxExtensionSize;
    }
    public int getMaxSwallowSize() {
        return this.maxSwallowSize;
    }
    public void setMaxSwallowSize ( final int maxSwallowSize ) {
        this.maxSwallowSize = maxSwallowSize;
    }
    public boolean getSecure() {
        return this.secure;
    }
    public void setSecure ( final boolean b ) {
        this.secure = b;
    }
    public void setAllowedTrailerHeaders ( final String commaSeparatedHeaders ) {
        final Set<String> toRemove = new HashSet<String>();
        toRemove.addAll ( this.allowedTrailerHeaders );
        if ( commaSeparatedHeaders != null ) {
            final String[] split;
            final String[] headers = split = commaSeparatedHeaders.split ( "," );
            for ( final String header : split ) {
                final String trimmedHeader = header.trim().toLowerCase ( Locale.ENGLISH );
                if ( toRemove.contains ( trimmedHeader ) ) {
                    toRemove.remove ( trimmedHeader );
                } else {
                    this.allowedTrailerHeaders.add ( trimmedHeader );
                }
            }
            this.allowedTrailerHeaders.removeAll ( toRemove );
        }
    }
    public String getAllowedTrailerHeaders() {
        final List<String> copy = new ArrayList<String> ( this.allowedTrailerHeaders.size() );
        copy.addAll ( this.allowedTrailerHeaders );
        final StringBuilder result = new StringBuilder();
        boolean first = true;
        for ( final String header : copy ) {
            if ( first ) {
                first = false;
            } else {
                result.append ( ',' );
            }
            result.append ( header );
        }
        return result.toString();
    }
    public void addAllowedTrailerHeader ( final String header ) {
        if ( header != null ) {
            this.allowedTrailerHeaders.add ( header.trim().toLowerCase ( Locale.ENGLISH ) );
        }
    }
    public void removeAllowedTrailerHeader ( final String header ) {
        if ( header != null ) {
            this.allowedTrailerHeaders.remove ( header.trim().toLowerCase ( Locale.ENGLISH ) );
        }
    }
    @Override
    public void addUpgradeProtocol ( final UpgradeProtocol upgradeProtocol ) {
        this.upgradeProtocols.add ( upgradeProtocol );
    }
    @Override
    public UpgradeProtocol[] findUpgradeProtocols() {
        return this.upgradeProtocols.toArray ( new UpgradeProtocol[0] );
    }
    private void configureUpgradeProtocol ( final UpgradeProtocol upgradeProtocol ) {
        final boolean isSSLEnabled = this.getEndpoint().isSSLEnabled();
        final String httpUpgradeName = upgradeProtocol.getHttpUpgradeName ( isSSLEnabled );
        boolean httpUpgradeConfigured = false;
        if ( httpUpgradeName != null && httpUpgradeName.length() > 0 ) {
            this.httpUpgradeProtocols.put ( httpUpgradeName, upgradeProtocol );
            httpUpgradeConfigured = true;
            this.getLog().info ( AbstractHttp11Protocol.sm.getString ( "abstractHttp11Protocol.httpUpgradeConfigured", this.getName(), httpUpgradeName ) );
        }
        final String alpnName = upgradeProtocol.getAlpnName();
        if ( alpnName != null && alpnName.length() > 0 ) {
            if ( isSSLEnabled ) {
                this.negotiatedProtocols.put ( alpnName, upgradeProtocol );
                this.getEndpoint().addNegotiatedProtocol ( alpnName );
                this.getLog().info ( AbstractHttp11Protocol.sm.getString ( "abstractHttp11Protocol.alpnConfigured", this.getName(), alpnName ) );
            } else if ( !httpUpgradeConfigured ) {
                this.getLog().error ( AbstractHttp11Protocol.sm.getString ( "abstractHttp11Protocol.alpnWithNoTls", upgradeProtocol.getClass().getName(), alpnName, this.getName() ) );
            }
        }
    }
    public UpgradeProtocol getNegotiatedProtocol ( final String negotiatedName ) {
        return this.negotiatedProtocols.get ( negotiatedName );
    }
    public UpgradeProtocol getUpgradeProtocol ( final String upgradedName ) {
        return this.httpUpgradeProtocols.get ( upgradedName );
    }
    public boolean isSSLEnabled() {
        return this.getEndpoint().isSSLEnabled();
    }
    public void setSSLEnabled ( final boolean SSLEnabled ) {
        this.getEndpoint().setSSLEnabled ( SSLEnabled );
    }
    public boolean getUseSendfile() {
        return this.getEndpoint().getUseSendfile();
    }
    public void setUseSendfile ( final boolean useSendfile ) {
        this.getEndpoint().setUseSendfile ( useSendfile );
    }
    public int getMaxKeepAliveRequests() {
        return this.getEndpoint().getMaxKeepAliveRequests();
    }
    public void setMaxKeepAliveRequests ( final int mkar ) {
        this.getEndpoint().setMaxKeepAliveRequests ( mkar );
    }
    public String getDefaultSSLHostConfigName() {
        return this.getEndpoint().getDefaultSSLHostConfigName();
    }
    public void setDefaultSSLHostConfigName ( final String defaultSSLHostConfigName ) {
        this.getEndpoint().setDefaultSSLHostConfigName ( defaultSSLHostConfigName );
        if ( this.defaultSSLHostConfig != null ) {
            this.defaultSSLHostConfig.setHostName ( defaultSSLHostConfigName );
        }
    }
    @Override
    public void addSslHostConfig ( final SSLHostConfig sslHostConfig ) {
        this.getEndpoint().addSslHostConfig ( sslHostConfig );
    }
    @Override
    public SSLHostConfig[] findSslHostConfigs() {
        return this.getEndpoint().findSslHostConfigs();
    }
    private void registerDefaultSSLHostConfig() {
        if ( this.defaultSSLHostConfig == null ) {
            ( this.defaultSSLHostConfig = new SSLHostConfig() ).setHostName ( this.getDefaultSSLHostConfigName() );
            this.getEndpoint().addSslHostConfig ( this.defaultSSLHostConfig );
        }
    }
    public void setSslEnabledProtocols ( final String enabledProtocols ) {
        this.registerDefaultSSLHostConfig();
        this.defaultSSLHostConfig.setProtocols ( enabledProtocols );
    }
    public void setSSLProtocol ( final String sslProtocol ) {
        this.registerDefaultSSLHostConfig();
        this.defaultSSLHostConfig.setProtocols ( sslProtocol );
    }
    public void setKeystoreFile ( final String keystoreFile ) {
        this.registerDefaultSSLHostConfig();
        this.defaultSSLHostConfig.setCertificateKeystoreFile ( keystoreFile );
    }
    public void setSSLCertificateChainFile ( final String certificateChainFile ) {
        this.registerDefaultSSLHostConfig();
        this.defaultSSLHostConfig.setCertificateChainFile ( certificateChainFile );
    }
    public void setSSLCertificateFile ( final String certificateFile ) {
        this.registerDefaultSSLHostConfig();
        this.defaultSSLHostConfig.setCertificateFile ( certificateFile );
    }
    public void setSSLCertificateKeyFile ( final String certificateKeyFile ) {
        this.registerDefaultSSLHostConfig();
        this.defaultSSLHostConfig.setCertificateKeyFile ( certificateKeyFile );
    }
    public void setAlgorithm ( final String keyManagerAlgorithm ) {
        this.registerDefaultSSLHostConfig();
        this.defaultSSLHostConfig.setKeyManagerAlgorithm ( keyManagerAlgorithm );
    }
    public void setClientAuth ( final String certificateVerification ) {
        this.registerDefaultSSLHostConfig();
        this.defaultSSLHostConfig.setCertificateVerification ( certificateVerification );
    }
    public void setSSLVerifyClient ( final String certificateVerification ) {
        this.registerDefaultSSLHostConfig();
        this.defaultSSLHostConfig.setCertificateVerification ( certificateVerification );
    }
    public void setTrustMaxCertLength ( final int certificateVerificationDepth ) {
        this.registerDefaultSSLHostConfig();
        this.defaultSSLHostConfig.setCertificateVerificationDepth ( certificateVerificationDepth );
    }
    public void setSSLVerifyDepth ( final int certificateVerificationDepth ) {
        this.registerDefaultSSLHostConfig();
        this.defaultSSLHostConfig.setCertificateVerificationDepth ( certificateVerificationDepth );
    }
    public void setUseServerCipherSuitesOrder ( final boolean honorCipherOrder ) {
        this.registerDefaultSSLHostConfig();
        this.defaultSSLHostConfig.setHonorCipherOrder ( honorCipherOrder );
    }
    public void setSSLHonorCipherOrder ( final boolean honorCipherOrder ) {
        this.registerDefaultSSLHostConfig();
        this.defaultSSLHostConfig.setHonorCipherOrder ( honorCipherOrder );
    }
    public void setCiphers ( final String ciphers ) {
        this.registerDefaultSSLHostConfig();
        this.defaultSSLHostConfig.setCiphers ( ciphers );
    }
    public void setSSLCipherSuite ( final String ciphers ) {
        this.registerDefaultSSLHostConfig();
        this.defaultSSLHostConfig.setCiphers ( ciphers );
    }
    public void setKeystorePass ( final String certificateKeystorePassword ) {
        this.registerDefaultSSLHostConfig();
        this.defaultSSLHostConfig.setCertificateKeystorePassword ( certificateKeystorePassword );
    }
    public void setKeyPass ( final String certificateKeyPassword ) {
        this.registerDefaultSSLHostConfig();
        this.defaultSSLHostConfig.setCertificateKeyPassword ( certificateKeyPassword );
    }
    public void setSSLPassword ( final String certificateKeyPassword ) {
        this.registerDefaultSSLHostConfig();
        this.defaultSSLHostConfig.setCertificateKeyPassword ( certificateKeyPassword );
    }
    public void setCrlFile ( final String certificateRevocationListFile ) {
        this.registerDefaultSSLHostConfig();
        this.defaultSSLHostConfig.setCertificateRevocationListFile ( certificateRevocationListFile );
    }
    public void setSSLCARevocationFile ( final String certificateRevocationListFile ) {
        this.registerDefaultSSLHostConfig();
        this.defaultSSLHostConfig.setCertificateRevocationListFile ( certificateRevocationListFile );
    }
    public void setSSLCARevocationPath ( final String certificateRevocationListPath ) {
        this.registerDefaultSSLHostConfig();
        this.defaultSSLHostConfig.setCertificateRevocationListPath ( certificateRevocationListPath );
    }
    public void setKeystoreType ( final String certificateKeystoreType ) {
        this.registerDefaultSSLHostConfig();
        this.defaultSSLHostConfig.setCertificateKeystoreType ( certificateKeystoreType );
    }
    public void setKeystoreProvider ( final String certificateKeystoreProvider ) {
        this.registerDefaultSSLHostConfig();
        this.defaultSSLHostConfig.setCertificateKeystoreProvider ( certificateKeystoreProvider );
    }
    public void setKeyAlias ( final String certificateKeyAlias ) {
        this.registerDefaultSSLHostConfig();
        this.defaultSSLHostConfig.setCertificateKeyAlias ( certificateKeyAlias );
    }
    public void setTruststoreAlgorithm ( final String truststoreAlgorithm ) {
        this.registerDefaultSSLHostConfig();
        this.defaultSSLHostConfig.setTruststoreAlgorithm ( truststoreAlgorithm );
    }
    public void setTruststoreFile ( final String truststoreFile ) {
        this.registerDefaultSSLHostConfig();
        this.defaultSSLHostConfig.setTruststoreFile ( truststoreFile );
    }
    public void setTruststorePass ( final String truststorePassword ) {
        this.registerDefaultSSLHostConfig();
        this.defaultSSLHostConfig.setTruststorePassword ( truststorePassword );
    }
    public void setTruststoreType ( final String truststoreType ) {
        this.registerDefaultSSLHostConfig();
        this.defaultSSLHostConfig.setTruststoreType ( truststoreType );
    }
    public void setTruststoreProvider ( final String truststoreProvider ) {
        this.registerDefaultSSLHostConfig();
        this.defaultSSLHostConfig.setTruststoreProvider ( truststoreProvider );
    }
    public void setSslProtocol ( final String sslProtocol ) {
        this.registerDefaultSSLHostConfig();
        this.defaultSSLHostConfig.setSslProtocol ( sslProtocol );
    }
    public void setSessionCacheSize ( final int sessionCacheSize ) {
        this.registerDefaultSSLHostConfig();
        this.defaultSSLHostConfig.setSessionCacheSize ( sessionCacheSize );
    }
    public void setSessionTimeout ( final int sessionTimeout ) {
        this.registerDefaultSSLHostConfig();
        this.defaultSSLHostConfig.setSessionTimeout ( sessionTimeout );
    }
    public void setSSLCACertificatePath ( final String caCertificatePath ) {
        this.registerDefaultSSLHostConfig();
        this.defaultSSLHostConfig.setCaCertificatePath ( caCertificatePath );
    }
    public void setSSLCACertificateFile ( final String caCertificateFile ) {
        this.registerDefaultSSLHostConfig();
        this.defaultSSLHostConfig.setCaCertificateFile ( caCertificateFile );
    }
    public void setSSLDisableCompression ( final boolean disableCompression ) {
        this.registerDefaultSSLHostConfig();
        this.defaultSSLHostConfig.setDisableCompression ( disableCompression );
    }
    public void setSSLDisableSessionTickets ( final boolean disableSessionTickets ) {
        this.registerDefaultSSLHostConfig();
        this.defaultSSLHostConfig.setDisableSessionTickets ( disableSessionTickets );
    }
    public void setTrustManagerClassName ( final String trustManagerClassName ) {
        this.registerDefaultSSLHostConfig();
        this.defaultSSLHostConfig.setTrustManagerClassName ( trustManagerClassName );
    }
    @Override
    protected Processor createProcessor() {
        final Http11Processor processor = new Http11Processor ( this.getMaxHttpHeaderSize(), this.getEndpoint(), this.getMaxTrailerSize(), this.allowedTrailerHeaders, this.getMaxExtensionSize(), this.getMaxSwallowSize(), this.httpUpgradeProtocols );
        processor.setAdapter ( this.getAdapter() );
        processor.setMaxKeepAliveRequests ( this.getMaxKeepAliveRequests() );
        processor.setConnectionUploadTimeout ( this.getConnectionUploadTimeout() );
        processor.setDisableUploadTimeout ( this.getDisableUploadTimeout() );
        processor.setCompressionMinSize ( this.getCompressionMinSize() );
        processor.setCompression ( this.getCompression() );
        processor.setNoCompressionUserAgents ( this.getNoCompressionUserAgents() );
        processor.setCompressableMimeTypes ( this.getCompressableMimeTypes() );
        processor.setRestrictedUserAgents ( this.getRestrictedUserAgents() );
        processor.setMaxSavePostSize ( this.getMaxSavePostSize() );
        processor.setServer ( this.getServer() );
        processor.setServerRemoveAppProvidedValues ( this.getServerRemoveAppProvidedValues() );
        return processor;
    }
    @Override
    protected Processor createUpgradeProcessor ( final SocketWrapperBase<?> socket, final UpgradeToken upgradeToken ) {
        final HttpUpgradeHandler httpUpgradeHandler = upgradeToken.getHttpUpgradeHandler();
        if ( httpUpgradeHandler instanceof InternalHttpUpgradeHandler ) {
            return new UpgradeProcessorInternal ( socket, upgradeToken );
        }
        return new UpgradeProcessorExternal ( socket, upgradeToken );
    }
    static {
        sm = StringManager.getManager ( AbstractHttp11Protocol.class );
    }
}
