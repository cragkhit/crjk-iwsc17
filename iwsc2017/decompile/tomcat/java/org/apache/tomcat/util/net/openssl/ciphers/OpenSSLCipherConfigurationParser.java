package org.apache.tomcat.util.net.openssl.ciphers;
import java.io.ObjectInputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.HashMap;
import java.util.LinkedHashMap;
import org.apache.juli.logging.LogFactory;
import java.util.Iterator;
import java.util.HashSet;
import java.util.Set;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Arrays;
import java.util.Collections;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.apache.tomcat.util.res.StringManager;
import org.apache.juli.logging.Log;
public class OpenSSLCipherConfigurationParser {
    private static final Log log;
    private static final StringManager sm;
    private static boolean initialized;
    private static final String SEPARATOR = ":|,| ";
    private static final String EXCLUDE = "!";
    private static final String DELETE = "-";
    private static final String TO_END = "+";
    private static final String AND = "+";
    private static final Map<String, List<Cipher>> aliases;
    private static final String eNULL = "eNULL";
    private static final String aNULL = "aNULL";
    private static final String HIGH = "HIGH";
    private static final String MEDIUM = "MEDIUM";
    private static final String LOW = "LOW";
    private static final String EXPORT = "EXPORT";
    private static final String EXPORT40 = "EXPORT40";
    private static final String EXPORT56 = "EXPORT56";
    private static final String kRSA = "kRSA";
    private static final String aRSA = "aRSA";
    private static final String RSA = "RSA";
    private static final String kEDH = "kEDH";
    private static final String kDHE = "kDHE";
    private static final String EDH = "EDH";
    private static final String DHE = "DHE";
    private static final String kDHr = "kDHr";
    private static final String kDHd = "kDHd";
    private static final String kDH = "kDH";
    private static final String kECDHr = "kECDHr";
    private static final String kECDHe = "kECDHe";
    private static final String kECDH = "kECDH";
    private static final String kEECDH = "kEECDH";
    private static final String EECDH = "EECDH";
    private static final String ECDH = "ECDH";
    private static final String kECDHE = "kECDHE";
    private static final String ECDHE = "ECDHE";
    private static final String EECDHE = "EECDHE";
    private static final String AECDH = "AECDH";
    private static final String DSS = "DSS";
    private static final String aDSS = "aDSS";
    private static final String aDH = "aDH";
    private static final String aECDH = "aECDH";
    private static final String aECDSA = "aECDSA";
    private static final String ECDSA = "ECDSA";
    private static final String kFZA = "kFZA";
    private static final String aFZA = "aFZA";
    private static final String eFZA = "eFZA";
    private static final String FZA = "FZA";
    private static final String DH = "DH";
    private static final String ADH = "ADH";
    private static final String AES128 = "AES128";
    private static final String AES256 = "AES256";
    private static final String AES = "AES";
    private static final String AESGCM = "AESGCM";
    private static final String AESCCM = "AESCCM";
    private static final String AESCCM8 = "AESCCM8";
    private static final String CAMELLIA128 = "CAMELLIA128";
    private static final String CAMELLIA256 = "CAMELLIA256";
    private static final String CAMELLIA = "CAMELLIA";
    private static final String CHACHA20 = "CHACHA20";
    private static final String TRIPLE_DES = "3DES";
    private static final String DES = "DES";
    private static final String RC4 = "RC4";
    private static final String RC2 = "RC2";
    private static final String IDEA = "IDEA";
    private static final String SEED = "SEED";
    private static final String MD5 = "MD5";
    private static final String SHA1 = "SHA1";
    private static final String SHA = "SHA";
    private static final String SHA256 = "SHA256";
    private static final String SHA384 = "SHA384";
    private static final String KRB5 = "KRB5";
    private static final String aGOST = "aGOST";
    private static final String aGOST01 = "aGOST01";
    private static final String aGOST94 = "aGOST94";
    private static final String kGOST = "kGOST";
    private static final String GOST94 = "GOST94";
    private static final String GOST89MAC = "GOST89MAC";
    private static final String aSRP = "aSRP";
    private static final String kSRP = "kSRP";
    private static final String SRP = "SRP";
    private static final String PSK = "PSK";
    private static final String aPSK = "aPSK";
    private static final String kPSK = "kPSK";
    private static final String kRSAPSK = "kRSAPSK";
    private static final String kECDHEPSK = "kECDHEPSK";
    private static final String kDHEPSK = "kDHEPSK";
    private static final String DEFAULT = "DEFAULT";
    private static final String COMPLEMENTOFDEFAULT = "COMPLEMENTOFDEFAULT";
    private static final String ALL = "ALL";
    private static final String COMPLEMENTOFALL = "COMPLEMENTOFALL";
    private static final Map<String, String> jsseToOpenSSL;
    private static final void init() {
        for ( final Cipher cipher : Cipher.values() ) {
            final String alias = cipher.getOpenSSLAlias();
            if ( OpenSSLCipherConfigurationParser.aliases.containsKey ( alias ) ) {
                OpenSSLCipherConfigurationParser.aliases.get ( alias ).add ( cipher );
            } else {
                final List<Cipher> list = new ArrayList<Cipher>();
                list.add ( cipher );
                OpenSSLCipherConfigurationParser.aliases.put ( alias, list );
            }
            OpenSSLCipherConfigurationParser.aliases.put ( cipher.name(), Collections.singletonList ( cipher ) );
            for ( final String openSSlAltName : cipher.getOpenSSLAltNames() ) {
                if ( OpenSSLCipherConfigurationParser.aliases.containsKey ( openSSlAltName ) ) {
                    OpenSSLCipherConfigurationParser.aliases.get ( openSSlAltName ).add ( cipher );
                } else {
                    final List<Cipher> list2 = new ArrayList<Cipher>();
                    list2.add ( cipher );
                    OpenSSLCipherConfigurationParser.aliases.put ( openSSlAltName, list2 );
                }
            }
            OpenSSLCipherConfigurationParser.jsseToOpenSSL.put ( cipher.name(), cipher.getOpenSSLAlias() );
            final Set<String> jsseNames = cipher.getJsseNames();
            for ( final String jsseName : jsseNames ) {
                OpenSSLCipherConfigurationParser.jsseToOpenSSL.put ( jsseName, cipher.getOpenSSLAlias() );
            }
        }
        final List<Cipher> allCiphersList = Arrays.asList ( Cipher.values() );
        Collections.reverse ( allCiphersList );
        final LinkedHashSet<Cipher> allCiphers = defaultSort ( new LinkedHashSet<Cipher> ( allCiphersList ) );
        addListAlias ( "eNULL", filterByEncryption ( allCiphers, Collections.singleton ( Encryption.eNULL ) ) );
        final LinkedHashSet<Cipher> all = new LinkedHashSet<Cipher> ( allCiphers );
        remove ( all, "eNULL" );
        addListAlias ( "ALL", all );
        addListAlias ( "HIGH", filterByEncryptionLevel ( allCiphers, Collections.singleton ( EncryptionLevel.HIGH ) ) );
        addListAlias ( "MEDIUM", filterByEncryptionLevel ( allCiphers, Collections.singleton ( EncryptionLevel.MEDIUM ) ) );
        addListAlias ( "LOW", filterByEncryptionLevel ( allCiphers, Collections.singleton ( EncryptionLevel.LOW ) ) );
        addListAlias ( "EXPORT", filterByEncryptionLevel ( allCiphers, new HashSet<EncryptionLevel> ( Arrays.asList ( EncryptionLevel.EXP40, EncryptionLevel.EXP56 ) ) ) );
        OpenSSLCipherConfigurationParser.aliases.put ( "EXP", OpenSSLCipherConfigurationParser.aliases.get ( "EXPORT" ) );
        addListAlias ( "EXPORT40", filterByEncryptionLevel ( allCiphers, Collections.singleton ( EncryptionLevel.EXP40 ) ) );
        addListAlias ( "EXPORT56", filterByEncryptionLevel ( allCiphers, Collections.singleton ( EncryptionLevel.EXP56 ) ) );
        OpenSSLCipherConfigurationParser.aliases.put ( "NULL", OpenSSLCipherConfigurationParser.aliases.get ( "eNULL" ) );
        OpenSSLCipherConfigurationParser.aliases.put ( "COMPLEMENTOFALL", OpenSSLCipherConfigurationParser.aliases.get ( "eNULL" ) );
        addListAlias ( "aNULL", filterByAuthentication ( allCiphers, Collections.singleton ( Authentication.aNULL ) ) );
        addListAlias ( "kRSA", filterByKeyExchange ( allCiphers, Collections.singleton ( KeyExchange.RSA ) ) );
        addListAlias ( "aRSA", filterByAuthentication ( allCiphers, Collections.singleton ( Authentication.RSA ) ) );
        OpenSSLCipherConfigurationParser.aliases.put ( "RSA", OpenSSLCipherConfigurationParser.aliases.get ( "kRSA" ) );
        addListAlias ( "kEDH", filterByKeyExchange ( allCiphers, Collections.singleton ( KeyExchange.EDH ) ) );
        addListAlias ( "kDHE", filterByKeyExchange ( allCiphers, Collections.singleton ( KeyExchange.EDH ) ) );
        final Set<Cipher> edh = filterByKeyExchange ( allCiphers, Collections.singleton ( KeyExchange.EDH ) );
        edh.removeAll ( filterByAuthentication ( allCiphers, Collections.singleton ( Authentication.aNULL ) ) );
        addListAlias ( "EDH", edh );
        addListAlias ( "DHE", edh );
        addListAlias ( "kDHr", filterByKeyExchange ( allCiphers, Collections.singleton ( KeyExchange.DHr ) ) );
        addListAlias ( "kDHd", filterByKeyExchange ( allCiphers, Collections.singleton ( KeyExchange.DHd ) ) );
        addListAlias ( "kDH", filterByKeyExchange ( allCiphers, new HashSet<KeyExchange> ( Arrays.asList ( KeyExchange.DHr, KeyExchange.DHd ) ) ) );
        addListAlias ( "kECDHr", filterByKeyExchange ( allCiphers, Collections.singleton ( KeyExchange.ECDHr ) ) );
        addListAlias ( "kECDHe", filterByKeyExchange ( allCiphers, Collections.singleton ( KeyExchange.ECDHe ) ) );
        addListAlias ( "kECDH", filterByKeyExchange ( allCiphers, new HashSet<KeyExchange> ( Arrays.asList ( KeyExchange.ECDHe, KeyExchange.ECDHr ) ) ) );
        addListAlias ( "ECDH", filterByKeyExchange ( allCiphers, new HashSet<KeyExchange> ( Arrays.asList ( KeyExchange.ECDHe, KeyExchange.ECDHr, KeyExchange.EECDH ) ) ) );
        addListAlias ( "kECDHE", filterByKeyExchange ( allCiphers, Collections.singleton ( KeyExchange.EECDH ) ) );
        final Set<Cipher> ecdhe = filterByKeyExchange ( allCiphers, Collections.singleton ( KeyExchange.EECDH ) );
        remove ( ecdhe, "aNULL" );
        addListAlias ( "ECDHE", ecdhe );
        addListAlias ( "kEECDH", filterByKeyExchange ( allCiphers, Collections.singleton ( KeyExchange.EECDH ) ) );
        OpenSSLCipherConfigurationParser.aliases.put ( "EECDHE", OpenSSLCipherConfigurationParser.aliases.get ( "kEECDH" ) );
        final Set<Cipher> eecdh = filterByKeyExchange ( allCiphers, Collections.singleton ( KeyExchange.EECDH ) );
        eecdh.removeAll ( filterByAuthentication ( allCiphers, Collections.singleton ( Authentication.aNULL ) ) );
        addListAlias ( "EECDH", eecdh );
        addListAlias ( "aDSS", filterByAuthentication ( allCiphers, Collections.singleton ( Authentication.DSS ) ) );
        OpenSSLCipherConfigurationParser.aliases.put ( "DSS", OpenSSLCipherConfigurationParser.aliases.get ( "aDSS" ) );
        addListAlias ( "aDH", filterByAuthentication ( allCiphers, Collections.singleton ( Authentication.DH ) ) );
        final Set<Cipher> aecdh = filterByKeyExchange ( allCiphers, Collections.singleton ( KeyExchange.EECDH ) );
        addListAlias ( "AECDH", filterByAuthentication ( aecdh, Collections.singleton ( Authentication.aNULL ) ) );
        addListAlias ( "aECDH", filterByAuthentication ( allCiphers, Collections.singleton ( Authentication.ECDH ) ) );
        addListAlias ( "ECDSA", filterByAuthentication ( allCiphers, Collections.singleton ( Authentication.ECDSA ) ) );
        OpenSSLCipherConfigurationParser.aliases.put ( "aECDSA", OpenSSLCipherConfigurationParser.aliases.get ( "ECDSA" ) );
        addListAlias ( "kFZA", filterByKeyExchange ( allCiphers, Collections.singleton ( KeyExchange.FZA ) ) );
        addListAlias ( "aFZA", filterByAuthentication ( allCiphers, Collections.singleton ( Authentication.FZA ) ) );
        addListAlias ( "eFZA", filterByEncryption ( allCiphers, Collections.singleton ( Encryption.FZA ) ) );
        addListAlias ( "FZA", filter ( allCiphers, null, Collections.singleton ( KeyExchange.FZA ), Collections.singleton ( Authentication.FZA ), Collections.singleton ( Encryption.FZA ), null, null ) );
        addListAlias ( "TLSv1.2", filterByProtocol ( allCiphers, Collections.singleton ( Protocol.TLSv1_2 ) ) );
        addListAlias ( "TLSv1.0", filterByProtocol ( allCiphers, Collections.singleton ( Protocol.TLSv1 ) ) );
        addListAlias ( "SSLv3", filterByProtocol ( allCiphers, Collections.singleton ( Protocol.SSLv3 ) ) );
        OpenSSLCipherConfigurationParser.aliases.put ( "TLSv1", OpenSSLCipherConfigurationParser.aliases.get ( "TLSv1.0" ) );
        addListAlias ( "SSLv2", filterByProtocol ( allCiphers, Collections.singleton ( Protocol.SSLv2 ) ) );
        addListAlias ( "DH", filterByKeyExchange ( allCiphers, new HashSet<KeyExchange> ( Arrays.asList ( KeyExchange.DHr, KeyExchange.DHd, KeyExchange.EDH ) ) ) );
        final Set<Cipher> adh = filterByKeyExchange ( allCiphers, Collections.singleton ( KeyExchange.EDH ) );
        adh.retainAll ( filterByAuthentication ( allCiphers, Collections.singleton ( Authentication.aNULL ) ) );
        addListAlias ( "ADH", adh );
        addListAlias ( "AES128", filterByEncryption ( allCiphers, new HashSet<Encryption> ( Arrays.asList ( Encryption.AES128, Encryption.AES128CCM, Encryption.AES128CCM8, Encryption.AES128GCM ) ) ) );
        addListAlias ( "AES256", filterByEncryption ( allCiphers, new HashSet<Encryption> ( Arrays.asList ( Encryption.AES256, Encryption.AES256CCM, Encryption.AES256CCM8, Encryption.AES256GCM ) ) ) );
        addListAlias ( "AES", filterByEncryption ( allCiphers, new HashSet<Encryption> ( Arrays.asList ( Encryption.AES128, Encryption.AES128CCM, Encryption.AES128CCM8, Encryption.AES128GCM, Encryption.AES256, Encryption.AES256CCM, Encryption.AES256CCM8, Encryption.AES256GCM ) ) ) );
        addListAlias ( "AESGCM", filterByEncryption ( allCiphers, new HashSet<Encryption> ( Arrays.asList ( Encryption.AES128GCM, Encryption.AES256GCM ) ) ) );
        addListAlias ( "AESCCM", filterByEncryption ( allCiphers, new HashSet<Encryption> ( Arrays.asList ( Encryption.AES128CCM, Encryption.AES128CCM8, Encryption.AES256CCM, Encryption.AES256CCM8 ) ) ) );
        addListAlias ( "AESCCM8", filterByEncryption ( allCiphers, new HashSet<Encryption> ( Arrays.asList ( Encryption.AES128CCM8, Encryption.AES256CCM8 ) ) ) );
        addListAlias ( "CAMELLIA", filterByEncryption ( allCiphers, new HashSet<Encryption> ( Arrays.asList ( Encryption.CAMELLIA128, Encryption.CAMELLIA256 ) ) ) );
        addListAlias ( "CAMELLIA128", filterByEncryption ( allCiphers, Collections.singleton ( Encryption.CAMELLIA128 ) ) );
        addListAlias ( "CAMELLIA256", filterByEncryption ( allCiphers, Collections.singleton ( Encryption.CAMELLIA256 ) ) );
        addListAlias ( "CHACHA20", filterByEncryption ( allCiphers, Collections.singleton ( Encryption.CHACHA20POLY1305 ) ) );
        addListAlias ( "3DES", filterByEncryption ( allCiphers, Collections.singleton ( Encryption.TRIPLE_DES ) ) );
        addListAlias ( "DES", filterByEncryption ( allCiphers, Collections.singleton ( Encryption.DES ) ) );
        addListAlias ( "RC4", filterByEncryption ( allCiphers, Collections.singleton ( Encryption.RC4 ) ) );
        addListAlias ( "RC2", filterByEncryption ( allCiphers, Collections.singleton ( Encryption.RC2 ) ) );
        addListAlias ( "IDEA", filterByEncryption ( allCiphers, Collections.singleton ( Encryption.IDEA ) ) );
        addListAlias ( "SEED", filterByEncryption ( allCiphers, Collections.singleton ( Encryption.SEED ) ) );
        addListAlias ( "MD5", filterByMessageDigest ( allCiphers, Collections.singleton ( MessageDigest.MD5 ) ) );
        addListAlias ( "SHA1", filterByMessageDigest ( allCiphers, Collections.singleton ( MessageDigest.SHA1 ) ) );
        OpenSSLCipherConfigurationParser.aliases.put ( "SHA", OpenSSLCipherConfigurationParser.aliases.get ( "SHA1" ) );
        addListAlias ( "SHA256", filterByMessageDigest ( allCiphers, Collections.singleton ( MessageDigest.SHA256 ) ) );
        addListAlias ( "SHA384", filterByMessageDigest ( allCiphers, Collections.singleton ( MessageDigest.SHA384 ) ) );
        addListAlias ( "aGOST", filterByAuthentication ( allCiphers, new HashSet<Authentication> ( Arrays.asList ( Authentication.GOST01, Authentication.GOST94 ) ) ) );
        addListAlias ( "aGOST01", filterByAuthentication ( allCiphers, Collections.singleton ( Authentication.GOST01 ) ) );
        addListAlias ( "aGOST94", filterByAuthentication ( allCiphers, Collections.singleton ( Authentication.GOST94 ) ) );
        addListAlias ( "kGOST", filterByKeyExchange ( allCiphers, Collections.singleton ( KeyExchange.GOST ) ) );
        addListAlias ( "GOST94", filterByMessageDigest ( allCiphers, Collections.singleton ( MessageDigest.GOST94 ) ) );
        addListAlias ( "GOST89MAC", filterByMessageDigest ( allCiphers, Collections.singleton ( MessageDigest.GOST89MAC ) ) );
        addListAlias ( "PSK", filter ( allCiphers, null, new HashSet<KeyExchange> ( Arrays.asList ( KeyExchange.PSK, KeyExchange.RSAPSK, KeyExchange.DHEPSK, KeyExchange.ECDHEPSK ) ), Collections.singleton ( Authentication.PSK ), null, null, null ) );
        addListAlias ( "aPSK", filterByAuthentication ( allCiphers, Collections.singleton ( Authentication.PSK ) ) );
        addListAlias ( "kPSK", filterByKeyExchange ( allCiphers, Collections.singleton ( KeyExchange.PSK ) ) );
        addListAlias ( "kRSAPSK", filterByKeyExchange ( allCiphers, Collections.singleton ( KeyExchange.RSAPSK ) ) );
        addListAlias ( "kECDHEPSK", filterByKeyExchange ( allCiphers, Collections.singleton ( KeyExchange.ECDHEPSK ) ) );
        addListAlias ( "kDHEPSK", filterByKeyExchange ( allCiphers, Collections.singleton ( KeyExchange.DHEPSK ) ) );
        addListAlias ( "KRB5", filter ( allCiphers, null, Collections.singleton ( KeyExchange.KRB5 ), Collections.singleton ( Authentication.KRB5 ), null, null, null ) );
        addListAlias ( "aSRP", filterByAuthentication ( allCiphers, Collections.singleton ( Authentication.SRP ) ) );
        addListAlias ( "kSRP", filterByKeyExchange ( allCiphers, Collections.singleton ( KeyExchange.SRP ) ) );
        addListAlias ( "SRP", filterByKeyExchange ( allCiphers, Collections.singleton ( KeyExchange.SRP ) ) );
        OpenSSLCipherConfigurationParser.initialized = true;
        addListAlias ( "DEFAULT", parse ( "ALL:!EXPORT:!eNULL:!aNULL:!SSLv2:!DES:!RC2:!RC4:!DSS:!SEED:!IDEA:!CAMELLIA:!AESCCM:!3DES" ) );
        LinkedHashSet<Cipher> complementOfDefault = filterByKeyExchange ( all, new HashSet<KeyExchange> ( Arrays.asList ( KeyExchange.EDH, KeyExchange.EECDH ) ) );
        complementOfDefault = filterByAuthentication ( complementOfDefault, Collections.singleton ( Authentication.aNULL ) );
        complementOfDefault.removeAll ( OpenSSLCipherConfigurationParser.aliases.get ( "eNULL" ) );
        complementOfDefault.addAll ( ( Collection<?> ) OpenSSLCipherConfigurationParser.aliases.get ( "SSLv2" ) );
        complementOfDefault.addAll ( ( Collection<?> ) OpenSSLCipherConfigurationParser.aliases.get ( "EXPORT" ) );
        complementOfDefault.addAll ( ( Collection<?> ) OpenSSLCipherConfigurationParser.aliases.get ( "DES" ) );
        complementOfDefault.addAll ( ( Collection<?> ) OpenSSLCipherConfigurationParser.aliases.get ( "3DES" ) );
        complementOfDefault.addAll ( ( Collection<?> ) OpenSSLCipherConfigurationParser.aliases.get ( "RC2" ) );
        complementOfDefault.addAll ( ( Collection<?> ) OpenSSLCipherConfigurationParser.aliases.get ( "RC4" ) );
        complementOfDefault.addAll ( ( Collection<?> ) OpenSSLCipherConfigurationParser.aliases.get ( "aDSS" ) );
        complementOfDefault.addAll ( ( Collection<?> ) OpenSSLCipherConfigurationParser.aliases.get ( "SEED" ) );
        complementOfDefault.addAll ( ( Collection<?> ) OpenSSLCipherConfigurationParser.aliases.get ( "IDEA" ) );
        complementOfDefault.addAll ( ( Collection<?> ) OpenSSLCipherConfigurationParser.aliases.get ( "CAMELLIA" ) );
        complementOfDefault.addAll ( ( Collection<?> ) OpenSSLCipherConfigurationParser.aliases.get ( "AESCCM" ) );
        defaultSort ( complementOfDefault );
        addListAlias ( "COMPLEMENTOFDEFAULT", complementOfDefault );
    }
    static void addListAlias ( final String alias, final Set<Cipher> ciphers ) {
        OpenSSLCipherConfigurationParser.aliases.put ( alias, new ArrayList<Cipher> ( ciphers ) );
    }
    static void moveToEnd ( final LinkedHashSet<Cipher> ciphers, final String alias ) {
        moveToEnd ( ciphers, OpenSSLCipherConfigurationParser.aliases.get ( alias ) );
    }
    static void moveToEnd ( final LinkedHashSet<Cipher> ciphers, final Collection<Cipher> toBeMovedCiphers ) {
        final List<Cipher> movedCiphers = new ArrayList<Cipher> ( toBeMovedCiphers );
        movedCiphers.retainAll ( ciphers );
        ciphers.removeAll ( movedCiphers );
        ciphers.addAll ( ( Collection<?> ) movedCiphers );
    }
    static void moveToStart ( final LinkedHashSet<Cipher> ciphers, final Collection<Cipher> toBeMovedCiphers ) {
        final List<Cipher> movedCiphers = new ArrayList<Cipher> ( toBeMovedCiphers );
        final List<Cipher> originalCiphers = new ArrayList<Cipher> ( ciphers );
        movedCiphers.retainAll ( ciphers );
        ciphers.clear();
        ciphers.addAll ( ( Collection<?> ) movedCiphers );
        ciphers.addAll ( ( Collection<?> ) originalCiphers );
    }
    static void add ( final LinkedHashSet<Cipher> ciphers, final String alias ) {
        ciphers.addAll ( ( Collection<?> ) OpenSSLCipherConfigurationParser.aliases.get ( alias ) );
    }
    static void remove ( final Set<Cipher> ciphers, final String alias ) {
        ciphers.removeAll ( OpenSSLCipherConfigurationParser.aliases.get ( alias ) );
    }
    static LinkedHashSet<Cipher> strengthSort ( final LinkedHashSet<Cipher> ciphers ) {
        final Set<Integer> keySizes = new HashSet<Integer>();
        for ( final Cipher cipher : ciphers ) {
            keySizes.add ( cipher.getStrength_bits() );
        }
        final List<Integer> strength_bits = new ArrayList<Integer> ( keySizes );
        Collections.sort ( strength_bits );
        Collections.reverse ( strength_bits );
        final LinkedHashSet<Cipher> result = new LinkedHashSet<Cipher> ( ciphers );
        for ( final int strength : strength_bits ) {
            moveToEnd ( result, filterByStrengthBits ( ciphers, strength ) );
        }
        return result;
    }
    static LinkedHashSet<Cipher> defaultSort ( final LinkedHashSet<Cipher> ciphers ) {
        final LinkedHashSet<Cipher> result = new LinkedHashSet<Cipher> ( ciphers.size() );
        final LinkedHashSet<Cipher> ecdh = new LinkedHashSet<Cipher> ( ciphers.size() );
        ecdh.addAll ( ( Collection<?> ) filterByKeyExchange ( ciphers, Collections.singleton ( KeyExchange.EECDH ) ) );
        final Set<Encryption> aes = new HashSet<Encryption> ( Arrays.asList ( Encryption.AES128, Encryption.AES128CCM, Encryption.AES128CCM8, Encryption.AES128GCM, Encryption.AES256, Encryption.AES256CCM, Encryption.AES256CCM8, Encryption.AES256GCM ) );
        result.addAll ( ( Collection<?> ) filterByEncryption ( ecdh, aes ) );
        result.addAll ( ( Collection<?> ) filterByEncryption ( ciphers, aes ) );
        result.addAll ( ( Collection<?> ) ecdh );
        result.addAll ( ( Collection<?> ) ciphers );
        moveToEnd ( result, filterByMessageDigest ( result, Collections.singleton ( MessageDigest.MD5 ) ) );
        moveToEnd ( result, filterByAuthentication ( result, Collections.singleton ( Authentication.aNULL ) ) );
        moveToEnd ( result, filterByAuthentication ( result, Collections.singleton ( Authentication.ECDH ) ) );
        moveToEnd ( result, filterByKeyExchange ( result, Collections.singleton ( KeyExchange.RSA ) ) );
        moveToEnd ( result, filterByKeyExchange ( result, Collections.singleton ( KeyExchange.PSK ) ) );
        moveToEnd ( result, filterByEncryption ( result, Collections.singleton ( Encryption.RC4 ) ) );
        return strengthSort ( result );
    }
    static Set<Cipher> filterByStrengthBits ( final Set<Cipher> ciphers, final int strength_bits ) {
        final Set<Cipher> result = new LinkedHashSet<Cipher> ( ciphers.size() );
        for ( final Cipher cipher : ciphers ) {
            if ( cipher.getStrength_bits() == strength_bits ) {
                result.add ( cipher );
            }
        }
        return result;
    }
    static Set<Cipher> filterByProtocol ( final Set<Cipher> ciphers, final Set<Protocol> protocol ) {
        return filter ( ciphers, protocol, null, null, null, null, null );
    }
    static LinkedHashSet<Cipher> filterByKeyExchange ( final Set<Cipher> ciphers, final Set<KeyExchange> kx ) {
        return filter ( ciphers, null, kx, null, null, null, null );
    }
    static LinkedHashSet<Cipher> filterByAuthentication ( final Set<Cipher> ciphers, final Set<Authentication> au ) {
        return filter ( ciphers, null, null, au, null, null, null );
    }
    static Set<Cipher> filterByEncryption ( final Set<Cipher> ciphers, final Set<Encryption> enc ) {
        return filter ( ciphers, null, null, null, enc, null, null );
    }
    static Set<Cipher> filterByEncryptionLevel ( final Set<Cipher> ciphers, final Set<EncryptionLevel> level ) {
        return filter ( ciphers, null, null, null, null, level, null );
    }
    static Set<Cipher> filterByMessageDigest ( final Set<Cipher> ciphers, final Set<MessageDigest> mac ) {
        return filter ( ciphers, null, null, null, null, null, mac );
    }
    static LinkedHashSet<Cipher> filter ( final Set<Cipher> ciphers, final Set<Protocol> protocol, final Set<KeyExchange> kx, final Set<Authentication> au, final Set<Encryption> enc, final Set<EncryptionLevel> level, final Set<MessageDigest> mac ) {
        final LinkedHashSet<Cipher> result = new LinkedHashSet<Cipher> ( ciphers.size() );
        for ( final Cipher cipher : ciphers ) {
            if ( protocol != null && protocol.contains ( cipher.getProtocol() ) ) {
                result.add ( cipher );
            }
            if ( kx != null && kx.contains ( cipher.getKx() ) ) {
                result.add ( cipher );
            }
            if ( au != null && au.contains ( cipher.getAu() ) ) {
                result.add ( cipher );
            }
            if ( enc != null && enc.contains ( cipher.getEnc() ) ) {
                result.add ( cipher );
            }
            if ( level != null && level.contains ( cipher.getLevel() ) ) {
                result.add ( cipher );
            }
            if ( mac != null && mac.contains ( cipher.getMac() ) ) {
                result.add ( cipher );
            }
        }
        return result;
    }
    public static LinkedHashSet<Cipher> parse ( final String expression ) {
        if ( !OpenSSLCipherConfigurationParser.initialized ) {
            init();
        }
        final String[] elements = expression.split ( ":|,| " );
        final LinkedHashSet<Cipher> ciphers = new LinkedHashSet<Cipher>();
        final Set<Cipher> removedCiphers = new HashSet<Cipher>();
        for ( final String element : elements ) {
            if ( element.startsWith ( "-" ) ) {
                final String alias = element.substring ( 1 );
                if ( OpenSSLCipherConfigurationParser.aliases.containsKey ( alias ) ) {
                    remove ( ciphers, alias );
                }
            } else if ( element.startsWith ( "!" ) ) {
                final String alias = element.substring ( 1 );
                if ( OpenSSLCipherConfigurationParser.aliases.containsKey ( alias ) ) {
                    removedCiphers.addAll ( OpenSSLCipherConfigurationParser.aliases.get ( alias ) );
                } else {
                    OpenSSLCipherConfigurationParser.log.warn ( OpenSSLCipherConfigurationParser.sm.getString ( "jsse.openssl.unknownElement", alias ) );
                }
            } else if ( element.startsWith ( "+" ) ) {
                final String alias = element.substring ( 1 );
                if ( OpenSSLCipherConfigurationParser.aliases.containsKey ( alias ) ) {
                    moveToEnd ( ciphers, alias );
                }
            } else {
                if ( "@STRENGTH".equals ( element ) ) {
                    strengthSort ( ciphers );
                    break;
                }
                if ( OpenSSLCipherConfigurationParser.aliases.containsKey ( element ) ) {
                    add ( ciphers, element );
                } else if ( element.contains ( "+" ) ) {
                    final String[] intersections = element.split ( "\\+" );
                    if ( intersections.length > 0 && OpenSSLCipherConfigurationParser.aliases.containsKey ( intersections[0] ) ) {
                        final List<Cipher> result = new ArrayList<Cipher> ( OpenSSLCipherConfigurationParser.aliases.get ( intersections[0] ) );
                        for ( int i = 1; i < intersections.length; ++i ) {
                            if ( OpenSSLCipherConfigurationParser.aliases.containsKey ( intersections[i] ) ) {
                                result.retainAll ( OpenSSLCipherConfigurationParser.aliases.get ( intersections[i] ) );
                            }
                        }
                        ciphers.addAll ( ( Collection<?> ) result );
                    }
                }
            }
        }
        ciphers.removeAll ( removedCiphers );
        return ciphers;
    }
    public static List<String> convertForJSSE ( final Collection<Cipher> ciphers ) {
        final List<String> result = new ArrayList<String> ( ciphers.size() );
        for ( final Cipher cipher : ciphers ) {
            result.addAll ( cipher.getJsseNames() );
        }
        if ( OpenSSLCipherConfigurationParser.log.isDebugEnabled() ) {
            OpenSSLCipherConfigurationParser.log.debug ( OpenSSLCipherConfigurationParser.sm.getString ( "jsse.openssl.effectiveCiphers", displayResult ( ciphers, true, "," ) ) );
        }
        return result;
    }
    public static List<String> parseExpression ( final String expression ) {
        return convertForJSSE ( parse ( expression ) );
    }
    public static String jsseToOpenSSL ( final String jsseCipherName ) {
        if ( !OpenSSLCipherConfigurationParser.initialized ) {
            init();
        }
        return OpenSSLCipherConfigurationParser.jsseToOpenSSL.get ( jsseCipherName );
    }
    public static String openSSLToJsse ( final String opensslCipherName ) {
        if ( !OpenSSLCipherConfigurationParser.initialized ) {
            init();
        }
        final List<Cipher> ciphers = OpenSSLCipherConfigurationParser.aliases.get ( opensslCipherName );
        if ( ciphers == null || ciphers.size() != 1 ) {
            return null;
        }
        final Cipher cipher = ciphers.get ( 0 );
        return cipher.getJsseNames().iterator().next();
    }
    static String displayResult ( final Collection<Cipher> ciphers, final boolean useJSSEFormat, final String separator ) {
        if ( ciphers.isEmpty() ) {
            return "";
        }
        final StringBuilder builder = new StringBuilder ( ciphers.size() * 16 );
        for ( final Cipher cipher : ciphers ) {
            if ( useJSSEFormat ) {
                for ( final String name : cipher.getJsseNames() ) {
                    builder.append ( name );
                    builder.append ( separator );
                }
            } else {
                builder.append ( cipher.getOpenSSLAlias() );
            }
            builder.append ( separator );
        }
        return builder.toString().substring ( 0, builder.length() - 1 );
    }
    static {
        log = LogFactory.getLog ( OpenSSLCipherConfigurationParser.class );
        sm = StringManager.getManager ( "org.apache.tomcat.util.net.jsse.res" );
        OpenSSLCipherConfigurationParser.initialized = false;
        aliases = new LinkedHashMap<String, List<Cipher>>();
        jsseToOpenSSL = new HashMap<String, String>();
    }
}
