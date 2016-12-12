package org.apache.tomcat.util.net.openssl.ciphers;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Collections;
import java.util.Collection;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
public enum Cipher {
    TLS_RSA_WITH_NULL_MD5 ( 1, "NULL-MD5", KeyExchange.RSA, Authentication.RSA, Encryption.eNULL, MessageDigest.MD5, Protocol.SSLv3, false, EncryptionLevel.STRONG_NONE, false, 0, 0, new String[] { "SSL_RSA_WITH_NULL_MD5" }, ( String[] ) null ),
    TLS_RSA_WITH_NULL_SHA ( 2, "NULL-SHA", KeyExchange.RSA, Authentication.RSA, Encryption.eNULL, MessageDigest.SHA1, Protocol.SSLv3, false, EncryptionLevel.STRONG_NONE, true, 0, 0, new String[] { "SSL_RSA_WITH_NULL_SHA" }, ( String[] ) null ),
    TLS_RSA_EXPORT_WITH_RC4_40_MD5 ( 3, "EXP-RC4-MD5", KeyExchange.RSA, Authentication.RSA, Encryption.RC4, MessageDigest.MD5, Protocol.SSLv3, true, EncryptionLevel.EXP40, false, 40, 128, new String[] { "SSL_RSA_EXPORT_WITH_RC4_40_MD5" }, ( String[] ) null ),
    TLS_RSA_WITH_RC4_128_MD5 ( 4, "RC4-MD5", KeyExchange.RSA, Authentication.RSA, Encryption.RC4, MessageDigest.MD5, Protocol.SSLv3, false, EncryptionLevel.MEDIUM, false, 128, 128, new String[] { "SSL_RSA_WITH_RC4_128_MD5" }, ( String[] ) null ),
    TLS_RSA_WITH_RC4_128_SHA ( 5, "RC4-SHA", KeyExchange.RSA, Authentication.RSA, Encryption.RC4, MessageDigest.SHA1, Protocol.SSLv3, false, EncryptionLevel.MEDIUM, false, 128, 128, new String[] { "SSL_RSA_WITH_RC4_128_SHA" }, ( String[] ) null ),
    TLS_RSA_EXPORT_WITH_RC2_CBC_40_MD5 ( 6, "EXP-RC2-CBC-MD5", KeyExchange.RSA, Authentication.RSA, Encryption.RC2, MessageDigest.MD5, Protocol.SSLv3, true, EncryptionLevel.EXP40, false, 40, 128, new String[] { "SSL_RSA_EXPORT_WITH_RC2_CBC_40_MD5" }, ( String[] ) null ),
    TLS_RSA_WITH_IDEA_CBC_SHA ( 7, "IDEA-CBC-SHA", KeyExchange.RSA, Authentication.RSA, Encryption.IDEA, MessageDigest.SHA1, Protocol.SSLv3, false, EncryptionLevel.MEDIUM, false, 128, 128, new String[] { "SSL_RSA_WITH_IDEA_CBC_SHA" }, ( String[] ) null ),
    TLS_RSA_EXPORT_WITH_DES40_CBC_SHA ( 8, "EXP-DES-CBC-SHA", KeyExchange.RSA, Authentication.RSA, Encryption.DES, MessageDigest.SHA1, Protocol.SSLv3, true, EncryptionLevel.EXP40, false, 40, 56, new String[] { "SSL_RSA_EXPORT_WITH_DES40_CBC_SHA" }, ( String[] ) null ),
    TLS_RSA_WITH_DES_CBC_SHA ( 9, "DES-CBC-SHA", KeyExchange.RSA, Authentication.RSA, Encryption.DES, MessageDigest.SHA1, Protocol.SSLv3, false, EncryptionLevel.LOW, false, 56, 56, new String[] { "SSL_RSA_WITH_DES_CBC_SHA" }, ( String[] ) null ),
    TLS_RSA_WITH_3DES_EDE_CBC_SHA ( 10, "DES-CBC3-SHA", KeyExchange.RSA, Authentication.RSA, Encryption.TRIPLE_DES, MessageDigest.SHA1, Protocol.SSLv3, false, EncryptionLevel.MEDIUM, true, 112, 168, new String[] { "SSL_RSA_WITH_3DES_EDE_CBC_SHA" }, ( String[] ) null ),
    TLS_DH_DSS_EXPORT_WITH_DES40_CBC_SHA ( 11, "EXP-DH-DSS-DES-CBC-SHA", KeyExchange.DHd, Authentication.DH, Encryption.DES, MessageDigest.SHA1, Protocol.SSLv3, true, EncryptionLevel.EXP40, false, 40, 56, new String[] { "SSL_DH_DSS_EXPORT_WITH_DES40_CBC_SHA" }, ( String[] ) null ),
    TLS_DH_DSS_WITH_DES_CBC_SHA ( 12, "DH-DSS-DES-CBC-SHA", KeyExchange.DHd, Authentication.DH, Encryption.DES, MessageDigest.SHA1, Protocol.SSLv3, false, EncryptionLevel.LOW, false, 56, 56, new String[] { "SSL_DH_DSS_WITH_DES_CBC_SHA" }, ( String[] ) null ),
    TLS_DH_DSS_WITH_3DES_EDE_CBC_SHA ( 13, "DH-DSS-DES-CBC3-SHA", KeyExchange.DHd, Authentication.DH, Encryption.TRIPLE_DES, MessageDigest.SHA1, Protocol.SSLv3, false, EncryptionLevel.MEDIUM, true, 112, 168, new String[] { "SSL_DH_DSS_WITH_3DES_EDE_CBC_SHA" }, ( String[] ) null ),
    TLS_DH_RSA_EXPORT_WITH_DES40_CBC_SHA ( 14, "EXP-DH-RSA-DES-CBC-SHA", KeyExchange.DHr, Authentication.DH, Encryption.DES, MessageDigest.SHA1, Protocol.SSLv3, true, EncryptionLevel.EXP40, false, 40, 56, new String[] { "SSL_DH_RSA_EXPORT_WITH_DES40_CBC_SHA" }, ( String[] ) null ),
    TLS_DH_RSA_WITH_DES_CBC_SHA ( 15, "DH-RSA-DES-CBC-SHA", KeyExchange.DHr, Authentication.DH, Encryption.DES, MessageDigest.SHA1, Protocol.SSLv3, false, EncryptionLevel.LOW, false, 56, 56, new String[] { "SSL_DH_RSA_WITH_DES_CBC_SHA" }, ( String[] ) null ),
    TLS_DH_RSA_WITH_3DES_EDE_CBC_SHA ( 16, "DH-RSA-DES-CBC3-SHA", KeyExchange.DHr, Authentication.DH, Encryption.TRIPLE_DES, MessageDigest.SHA1, Protocol.SSLv3, false, EncryptionLevel.MEDIUM, true, 112, 168, new String[] { "SSL_DH_RSA_WITH_3DES_EDE_CBC_SHA" }, ( String[] ) null ),
    TLS_DHE_DSS_EXPORT_WITH_DES40_CBC_SHA ( 17, "EXP-DHE-DSS-DES-CBC-SHA", KeyExchange.EDH, Authentication.DSS, Encryption.DES, MessageDigest.SHA1, Protocol.SSLv3, true, EncryptionLevel.EXP40, false, 40, 56, new String[] { "SSL_DHE_DSS_EXPORT_WITH_DES40_CBC_SHA" }, new String[] { "EXP-EDH-DSS-DES-CBC-SHA" } ),
    TLS_DHE_DSS_WITH_DES_CBC_SHA ( 18, "DHE-DSS-DES-CBC-SHA", KeyExchange.EDH, Authentication.DSS, Encryption.DES, MessageDigest.SHA1, Protocol.SSLv3, false, EncryptionLevel.LOW, false, 56, 56, new String[] { "SSL_DHE_DSS_WITH_DES_CBC_SHA" }, new String[] { "EDH-DSS-DES-CBC-SHA" } ),
    TLS_DHE_DSS_WITH_3DES_EDE_CBC_SHA ( 19, "DHE-DSS-DES-CBC3-SHA", KeyExchange.EDH, Authentication.DSS, Encryption.TRIPLE_DES, MessageDigest.SHA1, Protocol.SSLv3, false, EncryptionLevel.MEDIUM, true, 112, 168, new String[] { "SSL_DHE_DSS_WITH_3DES_EDE_CBC_SHA" }, new String[] { "EDH-DSS-DES-CBC3-SHA" } ),
    TLS_DHE_RSA_EXPORT_WITH_DES40_CBC_SHA ( 20, "EXP-DHE-RSA-DES-CBC-SHA", KeyExchange.EDH, Authentication.RSA, Encryption.DES, MessageDigest.SHA1, Protocol.SSLv3, true, EncryptionLevel.EXP40, false, 40, 56, new String[] { "SSL_DHE_RSA_EXPORT_WITH_DES40_CBC_SHA" }, new String[] { "EXP-EDH-RSA-DES-CBC-SHA" } ),
    TLS_DHE_RSA_WITH_DES_CBC_SHA ( 21, "DHE-RSA-DES-CBC-SHA", KeyExchange.EDH, Authentication.RSA, Encryption.DES, MessageDigest.SHA1, Protocol.SSLv3, false, EncryptionLevel.LOW, false, 56, 56, new String[] { "SSL_DHE_RSA_WITH_DES_CBC_SHA" }, new String[] { "EDH-RSA-DES-CBC-SHA" } ),
    TLS_DHE_RSA_WITH_3DES_EDE_CBC_SHA ( 22, "DHE-RSA-DES-CBC3-SHA", KeyExchange.EDH, Authentication.RSA, Encryption.TRIPLE_DES, MessageDigest.SHA1, Protocol.SSLv3, false, EncryptionLevel.MEDIUM, true, 112, 168, new String[] { "SSL_DHE_RSA_WITH_3DES_EDE_CBC_SHA" }, new String[] { "EDH-RSA-DES-CBC3-SHA" } ),
    TLS_DH_anon_EXPORT_WITH_RC4_40_MD5 ( 23, "EXP-ADH-RC4-MD5", KeyExchange.EDH, Authentication.aNULL, Encryption.RC4, MessageDigest.MD5, Protocol.SSLv3, true, EncryptionLevel.EXP40, false, 40, 128, new String[] { "SSL_DH_anon_EXPORT_WITH_RC4_40_MD5" }, ( String[] ) null ),
    TLS_DH_anon_WITH_RC4_128_MD5 ( 24, "ADH-RC4-MD5", KeyExchange.EDH, Authentication.aNULL, Encryption.RC4, MessageDigest.MD5, Protocol.SSLv3, false, EncryptionLevel.MEDIUM, false, 128, 128, new String[] { "SSL_DH_anon_WITH_RC4_128_MD5" }, ( String[] ) null ),
    TLS_DH_anon_EXPORT_WITH_DES40_CBC_SHA ( 25, "EXP-ADH-DES-CBC-SHA", KeyExchange.EDH, Authentication.aNULL, Encryption.DES, MessageDigest.SHA1, Protocol.SSLv3, true, EncryptionLevel.EXP40, false, 40, 128, new String[] { "SSL_DH_anon_EXPORT_WITH_DES40_CBC_SHA" }, ( String[] ) null ),
    TLS_DH_anon_WITH_DES_CBC_SHA ( 26, "ADH-DES-CBC-SHA", KeyExchange.EDH, Authentication.aNULL, Encryption.DES, MessageDigest.SHA1, Protocol.SSLv3, false, EncryptionLevel.LOW, false, 56, 56, new String[] { "SSL_DH_anon_WITH_DES_CBC_SHA" }, ( String[] ) null ),
    TLS_DH_anon_WITH_3DES_EDE_CBC_SHA ( 27, "ADH-DES-CBC3-SHA", KeyExchange.EDH, Authentication.aNULL, Encryption.TRIPLE_DES, MessageDigest.SHA1, Protocol.SSLv3, false, EncryptionLevel.MEDIUM, true, 112, 168, new String[] { "SSL_DH_anon_WITH_3DES_EDE_CBC_SHA" }, ( String[] ) null ),
    TLS_PSK_WITH_NULL_SHA ( 44, "PSK-NULL-SHA", KeyExchange.PSK, Authentication.PSK, Encryption.eNULL, MessageDigest.SHA1, Protocol.SSLv3, false, EncryptionLevel.STRONG_NONE, true, 0, 0, ( String[] ) null, ( String[] ) null ),
    TLS_DHE_PSK_WITH_NULL_SHA ( 45, "DHE-PSK-NULL-SHA", KeyExchange.DHEPSK, Authentication.PSK, Encryption.eNULL, MessageDigest.SHA1, Protocol.SSLv3, false, EncryptionLevel.STRONG_NONE, true, 0, 0, ( String[] ) null, ( String[] ) null ),
    TLS_RSA_PSK_WITH_NULL_SHA ( 46, "RSA-PSK-NULL-SHA", KeyExchange.RSAPSK, Authentication.RSA, Encryption.eNULL, MessageDigest.SHA1, Protocol.SSLv3, false, EncryptionLevel.STRONG_NONE, true, 0, 0, ( String[] ) null, ( String[] ) null ),
    TLS_RSA_WITH_AES_128_CBC_SHA ( 47, "AES128-SHA", KeyExchange.RSA, Authentication.RSA, Encryption.AES128, MessageDigest.SHA1, Protocol.SSLv3, false, EncryptionLevel.HIGH, true, 128, 128, ( String[] ) null, ( String[] ) null ),
    TLS_DH_DSS_WITH_AES_128_CBC_SHA ( 48, "DH-DSS-AES128-SHA", KeyExchange.DHd, Authentication.DH, Encryption.AES128, MessageDigest.SHA1, Protocol.SSLv3, false, EncryptionLevel.HIGH, true, 128, 128, ( String[] ) null, ( String[] ) null ),
    TLS_DH_RSA_WITH_AES_128_CBC_SHA ( 49, "DH-RSA-AES128-SHA", KeyExchange.DHr, Authentication.DH, Encryption.AES128, MessageDigest.SHA1, Protocol.SSLv3, false, EncryptionLevel.HIGH, true, 128, 128, ( String[] ) null, ( String[] ) null ),
    TLS_DHE_DSS_WITH_AES_128_CBC_SHA ( 50, "DHE-DSS-AES128-SHA", KeyExchange.EDH, Authentication.DSS, Encryption.AES128, MessageDigest.SHA1, Protocol.SSLv3, false, EncryptionLevel.HIGH, true, 128, 128, ( String[] ) null, ( String[] ) null ),
    TLS_DHE_RSA_WITH_AES_128_CBC_SHA ( 51, "DHE-RSA-AES128-SHA", KeyExchange.EDH, Authentication.RSA, Encryption.AES128, MessageDigest.SHA1, Protocol.SSLv3, false, EncryptionLevel.HIGH, true, 128, 128, ( String[] ) null, ( String[] ) null ),
    TLS_DH_anon_WITH_AES_128_CBC_SHA ( 52, "ADH-AES128-SHA", KeyExchange.EDH, Authentication.aNULL, Encryption.AES128, MessageDigest.SHA1, Protocol.SSLv3, false, EncryptionLevel.HIGH, true, 128, 128, ( String[] ) null, ( String[] ) null ),
    TLS_RSA_WITH_AES_256_CBC_SHA ( 53, "AES256-SHA", KeyExchange.RSA, Authentication.RSA, Encryption.AES256, MessageDigest.SHA1, Protocol.SSLv3, false, EncryptionLevel.HIGH, true, 256, 256, ( String[] ) null, ( String[] ) null ),
    TLS_DH_DSS_WITH_AES_256_CBC_SHA ( 54, "DH-DSS-AES256-SHA", KeyExchange.DHd, Authentication.DH, Encryption.AES256, MessageDigest.SHA1, Protocol.SSLv3, false, EncryptionLevel.HIGH, true, 256, 256, ( String[] ) null, ( String[] ) null ),
    TLS_DH_RSA_WITH_AES_256_CBC_SHA ( 55, "DH-RSA-AES256-SHA", KeyExchange.DHr, Authentication.DH, Encryption.AES256, MessageDigest.SHA1, Protocol.SSLv3, false, EncryptionLevel.HIGH, true, 256, 256, ( String[] ) null, ( String[] ) null ),
    TLS_DHE_DSS_WITH_AES_256_CBC_SHA ( 56, "DHE-DSS-AES256-SHA", KeyExchange.EDH, Authentication.DSS, Encryption.AES256, MessageDigest.SHA1, Protocol.SSLv3, false, EncryptionLevel.HIGH, true, 256, 256, ( String[] ) null, ( String[] ) null ),
    TLS_DHE_RSA_WITH_AES_256_CBC_SHA ( 57, "DHE-RSA-AES256-SHA", KeyExchange.EDH, Authentication.RSA, Encryption.AES256, MessageDigest.SHA1, Protocol.SSLv3, false, EncryptionLevel.HIGH, true, 256, 256, ( String[] ) null, ( String[] ) null ),
    TLS_DH_anon_WITH_AES_256_CBC_SHA ( 58, "ADH-AES256-SHA", KeyExchange.EDH, Authentication.aNULL, Encryption.AES256, MessageDigest.SHA1, Protocol.SSLv3, false, EncryptionLevel.HIGH, true, 256, 256, ( String[] ) null, ( String[] ) null ),
    TLS_RSA_WITH_NULL_SHA256 ( 59, "NULL-SHA256", KeyExchange.RSA, Authentication.RSA, Encryption.eNULL, MessageDigest.SHA256, Protocol.TLSv1_2, false, EncryptionLevel.STRONG_NONE, true, 0, 0, ( String[] ) null, ( String[] ) null ),
    TLS_RSA_WITH_AES_128_CBC_SHA256 ( 60, "AES128-SHA256", KeyExchange.RSA, Authentication.RSA, Encryption.AES128, MessageDigest.SHA256, Protocol.TLSv1_2, false, EncryptionLevel.HIGH, true, 128, 128, ( String[] ) null, ( String[] ) null ),
    TLS_RSA_WITH_AES_256_CBC_SHA256 ( 61, "AES256-SHA256", KeyExchange.RSA, Authentication.RSA, Encryption.AES256, MessageDigest.SHA256, Protocol.TLSv1_2, false, EncryptionLevel.HIGH, true, 256, 256, ( String[] ) null, ( String[] ) null ),
    TLS_DH_DSS_WITH_AES_128_CBC_SHA256 ( 62, "DH-DSS-AES128-SHA256", KeyExchange.DHd, Authentication.DH, Encryption.AES128, MessageDigest.SHA256, Protocol.TLSv1_2, false, EncryptionLevel.HIGH, true, 128, 128, ( String[] ) null, ( String[] ) null ),
    TLS_DH_RSA_WITH_AES_128_CBC_SHA256 ( 63, "DH-RSA-AES128-SHA256", KeyExchange.DHr, Authentication.DH, Encryption.AES128, MessageDigest.SHA256, Protocol.TLSv1_2, false, EncryptionLevel.HIGH, true, 128, 128, ( String[] ) null, ( String[] ) null ),
    TLS_DHE_DSS_WITH_AES_128_CBC_SHA256 ( 64, "DHE-DSS-AES128-SHA256", KeyExchange.EDH, Authentication.DSS, Encryption.AES128, MessageDigest.SHA256, Protocol.TLSv1_2, false, EncryptionLevel.HIGH, true, 128, 128, ( String[] ) null, ( String[] ) null ),
    TLS_RSA_WITH_CAMELLIA_128_CBC_SHA ( 65, "CAMELLIA128-SHA", KeyExchange.RSA, Authentication.RSA, Encryption.CAMELLIA128, MessageDigest.SHA1, Protocol.SSLv3, false, EncryptionLevel.HIGH, false, 128, 128, ( String[] ) null, ( String[] ) null ),
    TLS_DH_DSS_WITH_CAMELLIA_128_CBC_SHA ( 66, "DH-DSS-CAMELLIA128-SHA", KeyExchange.DHd, Authentication.DH, Encryption.CAMELLIA128, MessageDigest.SHA1, Protocol.SSLv3, false, EncryptionLevel.HIGH, false, 128, 128, ( String[] ) null, ( String[] ) null ),
    TLS_DH_RSA_WITH_CAMELLIA_128_CBC_SHA ( 67, "DH-RSA-CAMELLIA128-SHA", KeyExchange.DHr, Authentication.DH, Encryption.CAMELLIA128, MessageDigest.SHA1, Protocol.SSLv3, false, EncryptionLevel.HIGH, false, 128, 128, ( String[] ) null, ( String[] ) null ),
    TLS_DHE_DSS_WITH_CAMELLIA_128_CBC_SHA ( 68, "DHE-DSS-CAMELLIA128-SHA", KeyExchange.EDH, Authentication.DSS, Encryption.CAMELLIA128, MessageDigest.SHA1, Protocol.SSLv3, false, EncryptionLevel.HIGH, false, 128, 128, ( String[] ) null, ( String[] ) null ),
    TLS_DHE_RSA_WITH_CAMELLIA_128_CBC_SHA ( 69, "DHE-RSA-CAMELLIA128-SHA", KeyExchange.EDH, Authentication.RSA, Encryption.CAMELLIA128, MessageDigest.SHA1, Protocol.SSLv3, false, EncryptionLevel.HIGH, false, 128, 128, ( String[] ) null, ( String[] ) null ),
    TLS_DH_anon_WITH_CAMELLIA_128_CBC_SHA ( 70, "ADH-CAMELLIA128-SHA", KeyExchange.EDH, Authentication.aNULL, Encryption.CAMELLIA128, MessageDigest.SHA1, Protocol.SSLv3, false, EncryptionLevel.HIGH, false, 128, 128, ( String[] ) null, ( String[] ) null ),
    TLS_RSA_EXPORT1024_WITH_RC4_56_MD5 ( 96, "EXP1024-RC4-MD5", KeyExchange.RSA, Authentication.RSA, Encryption.RC4, MessageDigest.MD5, Protocol.TLSv1, true, EncryptionLevel.EXP56, false, 56, 128, new String[] { "SSL_RSA_EXPORT1024_WITH_RC4_56_MD5" }, ( String[] ) null ),
    TLS_RSA_EXPORT1024_WITH_RC2_CBC_56_MD5 ( 97, "EXP1024-RC2-CBC-MD5", KeyExchange.RSA, Authentication.RSA, Encryption.RC2, MessageDigest.MD5, Protocol.TLSv1, true, EncryptionLevel.EXP56, false, 56, 128, new String[] { "SSL_RSA_EXPORT1024_WITH_RC2_CBC_56_MD5" }, ( String[] ) null ),
    TLS_RSA_EXPORT1024_WITH_DES_CBC_SHA ( 98, "EXP1024-DES-CBC-SHA", KeyExchange.RSA, Authentication.RSA, Encryption.DES, MessageDigest.SHA1, Protocol.TLSv1, true, EncryptionLevel.EXP56, false, 56, 56, new String[] { "SSL_RSA_EXPORT1024_WITH_DES_CBC_SHA" }, ( String[] ) null ),
    TLS_DHE_DSS_EXPORT1024_WITH_DES_CBC_SHA ( 99, "EXP1024-DHE-DSS-DES-CBC-SHA", KeyExchange.EDH, Authentication.DSS, Encryption.DES, MessageDigest.SHA1, Protocol.TLSv1, true, EncryptionLevel.EXP56, false, 56, 56, new String[] { "SSL_DHE_DSS_EXPORT1024_WITH_DES_CBC_SHA" }, ( String[] ) null ),
    TLS_RSA_EXPORT1024_WITH_RC4_56_SHA ( 100, "EXP1024-RC4-SHA", KeyExchange.RSA, Authentication.RSA, Encryption.RC4, MessageDigest.SHA1, Protocol.TLSv1, true, EncryptionLevel.EXP56, false, 56, 128, new String[] { "SSL_RSA_EXPORT1024_WITH_RC4_56_SHA" }, ( String[] ) null ),
    TLS_DHE_DSS_EXPORT1024_WITH_RC4_56_SHA ( 101, "EXP1024-DHE-DSS-RC4-SHA", KeyExchange.EDH, Authentication.DSS, Encryption.RC4, MessageDigest.SHA1, Protocol.TLSv1, true, EncryptionLevel.EXP56, false, 56, 128, new String[] { "SSL_DHE_DSS_EXPORT1024_WITH_RC4_56_SHA" }, ( String[] ) null ),
    TLS_DHE_DSS_WITH_RC4_128_SHA ( 102, "DHE-DSS-RC4-SHA", KeyExchange.EDH, Authentication.DSS, Encryption.RC4, MessageDigest.SHA1, Protocol.TLSv1, false, EncryptionLevel.MEDIUM, false, 128, 128, new String[] { "SSL_DHE_DSS_WITH_RC4_128_SHA" }, ( String[] ) null ),
    TLS_DHE_RSA_WITH_AES_128_CBC_SHA256 ( 103, "DHE-RSA-AES128-SHA256", KeyExchange.EDH, Authentication.RSA, Encryption.AES128, MessageDigest.SHA256, Protocol.TLSv1_2, false, EncryptionLevel.HIGH, true, 128, 128, ( String[] ) null, ( String[] ) null ),
    TLS_DH_DSS_WITH_AES_256_CBC_SHA256 ( 104, "DH-DSS-AES256-SHA256", KeyExchange.DHd, Authentication.DH, Encryption.AES256, MessageDigest.SHA256, Protocol.TLSv1_2, false, EncryptionLevel.HIGH, true, 256, 256, ( String[] ) null, ( String[] ) null ),
    TLS_DH_RSA_WITH_AES_256_CBC_SHA256 ( 105, "DH-RSA-AES256-SHA256", KeyExchange.DHr, Authentication.DH, Encryption.AES256, MessageDigest.SHA256, Protocol.TLSv1_2, false, EncryptionLevel.HIGH, true, 256, 256, ( String[] ) null, ( String[] ) null ),
    TLS_DHE_DSS_WITH_AES_256_CBC_SHA256 ( 106, "DHE-DSS-AES256-SHA256", KeyExchange.EDH, Authentication.DSS, Encryption.AES256, MessageDigest.SHA256, Protocol.TLSv1_2, false, EncryptionLevel.HIGH, true, 256, 256, ( String[] ) null, ( String[] ) null ),
    TLS_DHE_RSA_WITH_AES_256_CBC_SHA256 ( 107, "DHE-RSA-AES256-SHA256", KeyExchange.EDH, Authentication.RSA, Encryption.AES256, MessageDigest.SHA256, Protocol.TLSv1_2, false, EncryptionLevel.HIGH, true, 256, 256, ( String[] ) null, ( String[] ) null ),
    TLS_DH_anon_WITH_AES_128_CBC_SHA256 ( 108, "ADH-AES128-SHA256", KeyExchange.EDH, Authentication.aNULL, Encryption.AES128, MessageDigest.SHA256, Protocol.TLSv1_2, false, EncryptionLevel.HIGH, true, 128, 128, ( String[] ) null, ( String[] ) null ),
    TLS_DH_anon_WITH_AES_256_CBC_SHA256 ( 109, "ADH-AES256-SHA256", KeyExchange.EDH, Authentication.aNULL, Encryption.AES256, MessageDigest.SHA256, Protocol.TLSv1_2, false, EncryptionLevel.HIGH, true, 256, 256, ( String[] ) null, ( String[] ) null ),
    TLS_RSA_WITH_CAMELLIA_256_CBC_SHA ( 132, "CAMELLIA256-SHA", KeyExchange.RSA, Authentication.RSA, Encryption.CAMELLIA256, MessageDigest.SHA1, Protocol.SSLv3, false, EncryptionLevel.HIGH, false, 256, 256, ( String[] ) null, ( String[] ) null ),
    TLS_DH_DSS_WITH_CAMELLIA_256_CBC_SHA ( 133, "DH-DSS-CAMELLIA256-SHA", KeyExchange.DHd, Authentication.DH, Encryption.CAMELLIA256, MessageDigest.SHA1, Protocol.SSLv3, false, EncryptionLevel.HIGH, false, 256, 256, ( String[] ) null, ( String[] ) null ),
    TLS_DH_RSA_WITH_CAMELLIA_256_CBC_SHA ( 134, "DH-RSA-CAMELLIA256-SHA", KeyExchange.DHr, Authentication.DH, Encryption.CAMELLIA256, MessageDigest.SHA1, Protocol.SSLv3, false, EncryptionLevel.HIGH, false, 256, 256, ( String[] ) null, ( String[] ) null ),
    TLS_DHE_DSS_WITH_CAMELLIA_256_CBC_SHA ( 135, "DHE-DSS-CAMELLIA256-SHA", KeyExchange.EDH, Authentication.DSS, Encryption.CAMELLIA256, MessageDigest.SHA1, Protocol.SSLv3, false, EncryptionLevel.HIGH, false, 256, 256, ( String[] ) null, ( String[] ) null ),
    TLS_DHE_RSA_WITH_CAMELLIA_256_CBC_SHA ( 136, "DHE-RSA-CAMELLIA256-SHA", KeyExchange.EDH, Authentication.RSA, Encryption.CAMELLIA256, MessageDigest.SHA1, Protocol.SSLv3, false, EncryptionLevel.HIGH, false, 256, 256, ( String[] ) null, ( String[] ) null ),
    TLS_DH_anon_WITH_CAMELLIA_256_CBC_SHA ( 137, "ADH-CAMELLIA256-SHA", KeyExchange.EDH, Authentication.aNULL, Encryption.CAMELLIA256, MessageDigest.SHA1, Protocol.SSLv3, false, EncryptionLevel.HIGH, false, 256, 256, ( String[] ) null, ( String[] ) null ),
    TLS_PSK_WITH_RC4_128_SHA ( 138, "PSK-RC4-SHA", KeyExchange.PSK, Authentication.PSK, Encryption.RC4, MessageDigest.SHA1, Protocol.SSLv3, false, EncryptionLevel.MEDIUM, false, 128, 128, ( String[] ) null, ( String[] ) null ),
    TLS_PSK_WITH_3DES_EDE_CBC_SHA ( 139, "PSK-3DES-EDE-CBC-SHA", KeyExchange.PSK, Authentication.PSK, Encryption.TRIPLE_DES, MessageDigest.SHA1, Protocol.SSLv3, false, EncryptionLevel.MEDIUM, true, 112, 168, ( String[] ) null, ( String[] ) null ),
    TLS_PSK_WITH_AES_128_CBC_SHA ( 140, "PSK-AES128-CBC-SHA", KeyExchange.PSK, Authentication.PSK, Encryption.AES128, MessageDigest.SHA1, Protocol.SSLv3, false, EncryptionLevel.HIGH, true, 128, 128, ( String[] ) null, ( String[] ) null ),
    TLS_PSK_WITH_AES_256_CBC_SHA ( 141, "PSK-AES256-CBC-SHA", KeyExchange.PSK, Authentication.PSK, Encryption.AES256, MessageDigest.SHA1, Protocol.SSLv3, false, EncryptionLevel.HIGH, true, 256, 256, ( String[] ) null, ( String[] ) null ),
    TLS_DHE_PSK_WITH_RC4_128_SHA ( 142, "DHE-PSK-RC4-SHA", KeyExchange.DHEPSK, Authentication.PSK, Encryption.RC4, MessageDigest.SHA1, Protocol.SSLv3, false, EncryptionLevel.MEDIUM, false, 128, 128, ( String[] ) null, ( String[] ) null ),
    TLS_DHE_PSK_WITH_3DES_EDE_CBC_SHA ( 143, "DHE-PSK-3DES-EDE-CBC-SHA", KeyExchange.DHEPSK, Authentication.PSK, Encryption.TRIPLE_DES, MessageDigest.SHA1, Protocol.SSLv3, false, EncryptionLevel.MEDIUM, true, 112, 168, ( String[] ) null, ( String[] ) null ),
    TLS_DHE_PSK_WITH_AES_128_CBC_SHA ( 144, "DHE-PSK-AES128-CBC-SHA", KeyExchange.DHEPSK, Authentication.PSK, Encryption.AES128, MessageDigest.SHA1, Protocol.SSLv3, false, EncryptionLevel.HIGH, true, 128, 128, ( String[] ) null, ( String[] ) null ),
    TLS_DHE_PSK_WITH_AES_256_CBC_SHA ( 145, "DHE-PSK-AES256-CBC-SHA", KeyExchange.DHEPSK, Authentication.PSK, Encryption.AES256, MessageDigest.SHA1, Protocol.SSLv3, false, EncryptionLevel.HIGH, true, 256, 256, ( String[] ) null, ( String[] ) null ),
    TLS_RSA_PSK_WITH_RC4_128_SHA ( 146, "RSA-PSK-RC4-SHA", KeyExchange.RSAPSK, Authentication.RSA, Encryption.RC4, MessageDigest.SHA1, Protocol.SSLv3, false, EncryptionLevel.MEDIUM, false, 128, 128, ( String[] ) null, ( String[] ) null ),
    TLS_RSA_PSK_WITH_3DES_EDE_CBC_SHA ( 147, "RSA-PSK-3DES-EDE-CBC-SHA", KeyExchange.RSAPSK, Authentication.RSA, Encryption.TRIPLE_DES, MessageDigest.SHA1, Protocol.SSLv3, false, EncryptionLevel.MEDIUM, true, 112, 168, ( String[] ) null, ( String[] ) null ),
    TLS_RSA_PSK_WITH_AES_128_CBC_SHA ( 148, "RSA-PSK-AES128-CBC-SHA", KeyExchange.RSAPSK, Authentication.RSA, Encryption.AES128, MessageDigest.SHA1, Protocol.SSLv3, false, EncryptionLevel.HIGH, true, 128, 128, ( String[] ) null, ( String[] ) null ),
    TLS_RSA_PSK_WITH_AES_256_CBC_SHA ( 149, "RSA-PSK-AES256-CBC-SHA", KeyExchange.RSAPSK, Authentication.RSA, Encryption.AES256, MessageDigest.SHA1, Protocol.SSLv3, false, EncryptionLevel.HIGH, true, 256, 256, ( String[] ) null, ( String[] ) null ),
    TLS_RSA_WITH_SEED_CBC_SHA ( 150, "SEED-SHA", KeyExchange.RSA, Authentication.RSA, Encryption.SEED, MessageDigest.SHA1, Protocol.SSLv3, false, EncryptionLevel.MEDIUM, false, 128, 128, ( String[] ) null, ( String[] ) null ),
    TLS_DH_DSS_WITH_SEED_CBC_SHA ( 151, "DH-DSS-SEED-SHA", KeyExchange.DHd, Authentication.DH, Encryption.SEED, MessageDigest.SHA1, Protocol.SSLv3, false, EncryptionLevel.MEDIUM, false, 128, 128, ( String[] ) null, ( String[] ) null ),
    TLS_DH_RSA_WITH_SEED_CBC_SHA ( 152, "DH-RSA-SEED-SHA", KeyExchange.DHr, Authentication.DH, Encryption.SEED, MessageDigest.SHA1, Protocol.SSLv3, false, EncryptionLevel.MEDIUM, false, 128, 128, ( String[] ) null, ( String[] ) null ),
    TLS_DHE_DSS_WITH_SEED_CBC_SHA ( 153, "DHE-DSS-SEED-SHA", KeyExchange.EDH, Authentication.DSS, Encryption.SEED, MessageDigest.SHA1, Protocol.SSLv3, false, EncryptionLevel.MEDIUM, false, 128, 128, ( String[] ) null, ( String[] ) null ),
    TLS_DHE_RSA_WITH_SEED_CBC_SHA ( 154, "DHE-RSA-SEED-SHA", KeyExchange.EDH, Authentication.RSA, Encryption.SEED, MessageDigest.SHA1, Protocol.SSLv3, false, EncryptionLevel.MEDIUM, false, 128, 128, ( String[] ) null, ( String[] ) null ),
    TLS_DH_anon_WITH_SEED_CBC_SHA ( 155, "ADH-SEED-SHA", KeyExchange.EDH, Authentication.aNULL, Encryption.SEED, MessageDigest.SHA1, Protocol.SSLv3, false, EncryptionLevel.MEDIUM, false, 128, 128, ( String[] ) null, ( String[] ) null ),
    TLS_RSA_WITH_AES_128_GCM_SHA256 ( 156, "AES128-GCM-SHA256", KeyExchange.RSA, Authentication.RSA, Encryption.AES128GCM, MessageDigest.AEAD, Protocol.TLSv1_2, false, EncryptionLevel.HIGH, true, 128, 128, ( String[] ) null, ( String[] ) null ),
    TLS_RSA_WITH_AES_256_GCM_SHA384 ( 157, "AES256-GCM-SHA384", KeyExchange.RSA, Authentication.RSA, Encryption.AES256GCM, MessageDigest.AEAD, Protocol.TLSv1_2, false, EncryptionLevel.HIGH, true, 256, 256, ( String[] ) null, ( String[] ) null ),
    TLS_DHE_RSA_WITH_AES_128_GCM_SHA256 ( 158, "DHE-RSA-AES128-GCM-SHA256", KeyExchange.EDH, Authentication.RSA, Encryption.AES128GCM, MessageDigest.AEAD, Protocol.TLSv1_2, false, EncryptionLevel.HIGH, true, 128, 128, ( String[] ) null, ( String[] ) null ),
    TLS_DHE_RSA_WITH_AES_256_GCM_SHA384 ( 159, "DHE-RSA-AES256-GCM-SHA384", KeyExchange.EDH, Authentication.RSA, Encryption.AES256GCM, MessageDigest.AEAD, Protocol.TLSv1_2, false, EncryptionLevel.HIGH, true, 256, 256, ( String[] ) null, ( String[] ) null ),
    TLS_DH_RSA_WITH_AES_128_GCM_SHA256 ( 160, "DH-RSA-AES128-GCM-SHA256", KeyExchange.DHr, Authentication.DH, Encryption.AES128GCM, MessageDigest.AEAD, Protocol.TLSv1_2, false, EncryptionLevel.HIGH, true, 128, 128, ( String[] ) null, ( String[] ) null ),
    TLS_DH_RSA_WITH_AES_256_GCM_SHA384 ( 161, "DH-RSA-AES256-GCM-SHA384", KeyExchange.DHr, Authentication.DH, Encryption.AES256GCM, MessageDigest.AEAD, Protocol.TLSv1_2, false, EncryptionLevel.HIGH, true, 256, 256, ( String[] ) null, ( String[] ) null ),
    TLS_DHE_DSS_WITH_AES_128_GCM_SHA256 ( 162, "DHE-DSS-AES128-GCM-SHA256", KeyExchange.EDH, Authentication.DSS, Encryption.AES128GCM, MessageDigest.AEAD, Protocol.TLSv1_2, false, EncryptionLevel.HIGH, true, 128, 128, ( String[] ) null, ( String[] ) null ),
    TLS_DHE_DSS_WITH_AES_256_GCM_SHA384 ( 163, "DHE-DSS-AES256-GCM-SHA384", KeyExchange.EDH, Authentication.DSS, Encryption.AES256GCM, MessageDigest.AEAD, Protocol.TLSv1_2, false, EncryptionLevel.HIGH, true, 256, 256, ( String[] ) null, ( String[] ) null ),
    TLS_DH_DSS_WITH_AES_128_GCM_SHA256 ( 164, "DH-DSS-AES128-GCM-SHA256", KeyExchange.DHd, Authentication.DH, Encryption.AES128GCM, MessageDigest.AEAD, Protocol.TLSv1_2, false, EncryptionLevel.HIGH, true, 128, 128, ( String[] ) null, ( String[] ) null ),
    TLS_DH_DSS_WITH_AES_256_GCM_SHA384 ( 165, "DH-DSS-AES256-GCM-SHA384", KeyExchange.DHd, Authentication.DH, Encryption.AES256GCM, MessageDigest.AEAD, Protocol.TLSv1_2, false, EncryptionLevel.HIGH, true, 256, 256, ( String[] ) null, ( String[] ) null ),
    TLS_DH_anon_WITH_AES_128_GCM_SHA256 ( 166, "ADH-AES128-GCM-SHA256", KeyExchange.EDH, Authentication.aNULL, Encryption.AES128GCM, MessageDigest.AEAD, Protocol.TLSv1_2, false, EncryptionLevel.HIGH, true, 128, 128, ( String[] ) null, ( String[] ) null ),
    TLS_DH_anon_WITH_AES_256_GCM_SHA384 ( 167, "ADH-AES256-GCM-SHA384", KeyExchange.EDH, Authentication.aNULL, Encryption.AES256GCM, MessageDigest.AEAD, Protocol.TLSv1_2, false, EncryptionLevel.HIGH, true, 256, 256, ( String[] ) null, ( String[] ) null ),
    TLS_PSK_WITH_AES_128_GCM_SHA256 ( 168, "PSK-AES128-GCM-SHA256", KeyExchange.PSK, Authentication.PSK, Encryption.AES128GCM, MessageDigest.AEAD, Protocol.TLSv1_2, false, EncryptionLevel.HIGH, true, 128, 128, ( String[] ) null, ( String[] ) null ),
    TLS_PSK_WITH_AES_256_GCM_SHA384 ( 169, "PSK-AES256-GCM-SHA384", KeyExchange.PSK, Authentication.PSK, Encryption.AES256GCM, MessageDigest.AEAD, Protocol.TLSv1_2, false, EncryptionLevel.HIGH, true, 256, 256, ( String[] ) null, ( String[] ) null ),
    TLS_DHE_PSK_WITH_AES_128_GCM_SHA256 ( 170, "DHE-PSK-AES128-GCM-SHA256", KeyExchange.DHEPSK, Authentication.PSK, Encryption.AES128GCM, MessageDigest.AEAD, Protocol.TLSv1_2, false, EncryptionLevel.HIGH, true, 128, 128, ( String[] ) null, ( String[] ) null ),
    TLS_DHE_PSK_WITH_AES_256_GCM_SHA384 ( 171, "DHE-PSK-AES256-GCM-SHA384", KeyExchange.DHEPSK, Authentication.PSK, Encryption.AES256GCM, MessageDigest.AEAD, Protocol.TLSv1_2, false, EncryptionLevel.HIGH, true, 256, 256, ( String[] ) null, ( String[] ) null ),
    TLS_RSA_PSK_WITH_AES_128_GCM_SHA256 ( 172, "RSA-PSK-AES128-GCM-SHA256", KeyExchange.RSAPSK, Authentication.RSA, Encryption.AES128GCM, MessageDigest.AEAD, Protocol.TLSv1_2, false, EncryptionLevel.HIGH, true, 128, 128, ( String[] ) null, ( String[] ) null ),
    TLS_RSA_PSK_WITH_AES_256_GCM_SHA384 ( 173, "RSA-PSK-AES256-GCM-SHA384", KeyExchange.RSAPSK, Authentication.RSA, Encryption.AES256GCM, MessageDigest.AEAD, Protocol.TLSv1_2, false, EncryptionLevel.HIGH, true, 256, 256, ( String[] ) null, ( String[] ) null ),
    TLS_PSK_WITH_AES_128_CBC_SHA256 ( 174, "PSK-AES128-CBC-SHA256", KeyExchange.PSK, Authentication.PSK, Encryption.AES128, MessageDigest.SHA256, Protocol.TLSv1, false, EncryptionLevel.HIGH, true, 128, 128, ( String[] ) null, ( String[] ) null ),
    TLS_PSK_WITH_AES_256_CBC_SHA384 ( 175, "PSK-AES256-CBC-SHA384", KeyExchange.PSK, Authentication.PSK, Encryption.AES256, MessageDigest.SHA384, Protocol.TLSv1, false, EncryptionLevel.HIGH, true, 256, 256, ( String[] ) null, ( String[] ) null ),
    TLS_PSK_WITH_NULL_SHA256 ( 176, "PSK-NULL-SHA256", KeyExchange.PSK, Authentication.PSK, Encryption.eNULL, MessageDigest.SHA256, Protocol.TLSv1, false, EncryptionLevel.STRONG_NONE, true, 0, 0, ( String[] ) null, ( String[] ) null ),
    TLS_PSK_WITH_NULL_SHA384 ( 177, "PSK-NULL-SHA384", KeyExchange.PSK, Authentication.PSK, Encryption.eNULL, MessageDigest.SHA384, Protocol.TLSv1, false, EncryptionLevel.STRONG_NONE, true, 0, 0, ( String[] ) null, ( String[] ) null ),
    TLS_DHE_PSK_WITH_AES_128_CBC_SHA256 ( 178, "DHE-PSK-AES128-CBC-SHA256", KeyExchange.DHEPSK, Authentication.PSK, Encryption.AES128, MessageDigest.SHA256, Protocol.TLSv1, false, EncryptionLevel.HIGH, true, 128, 128, ( String[] ) null, ( String[] ) null ),
    TLS_DHE_PSK_WITH_AES_256_CBC_SHA384 ( 179, "DHE-PSK-AES256-CBC-SHA384", KeyExchange.DHEPSK, Authentication.PSK, Encryption.AES256, MessageDigest.SHA384, Protocol.TLSv1, false, EncryptionLevel.HIGH, true, 256, 256, ( String[] ) null, ( String[] ) null ),
    TLS_DHE_PSK_WITH_NULL_SHA256 ( 180, "DHE-PSK-NULL-SHA256", KeyExchange.DHEPSK, Authentication.PSK, Encryption.eNULL, MessageDigest.SHA256, Protocol.TLSv1, false, EncryptionLevel.STRONG_NONE, true, 0, 0, ( String[] ) null, ( String[] ) null ),
    TLS_DHE_PSK_WITH_NULL_SHA384 ( 181, "DHE-PSK-NULL-SHA384", KeyExchange.DHEPSK, Authentication.PSK, Encryption.eNULL, MessageDigest.SHA384, Protocol.TLSv1, false, EncryptionLevel.STRONG_NONE, true, 0, 0, ( String[] ) null, ( String[] ) null ),
    TLS_RSA_PSK_WITH_AES_128_CBC_SHA256 ( 182, "RSA-PSK-AES128-CBC-SHA256", KeyExchange.RSAPSK, Authentication.RSA, Encryption.AES128, MessageDigest.SHA256, Protocol.TLSv1, false, EncryptionLevel.HIGH, true, 128, 128, ( String[] ) null, ( String[] ) null ),
    TLS_RSA_PSK_WITH_AES_256_CBC_SHA384 ( 183, "RSA-PSK-AES256-CBC-SHA384", KeyExchange.RSAPSK, Authentication.RSA, Encryption.AES256, MessageDigest.SHA384, Protocol.TLSv1, false, EncryptionLevel.HIGH, true, 256, 256, ( String[] ) null, ( String[] ) null ),
    TLS_RSA_PSK_WITH_NULL_SHA256 ( 184, "RSA-PSK-NULL-SHA256", KeyExchange.RSAPSK, Authentication.RSA, Encryption.eNULL, MessageDigest.SHA256, Protocol.TLSv1, false, EncryptionLevel.STRONG_NONE, true, 0, 0, ( String[] ) null, ( String[] ) null ),
    TLS_RSA_PSK_WITH_NULL_SHA384 ( 185, "RSA-PSK-NULL-SHA384", KeyExchange.RSAPSK, Authentication.RSA, Encryption.eNULL, MessageDigest.SHA384, Protocol.TLSv1, false, EncryptionLevel.STRONG_NONE, true, 0, 0, ( String[] ) null, ( String[] ) null ),
    TLS_RSA_WITH_CAMELLIA_128_CBC_SHA256 ( 186, "CAMELLIA128-SHA256", KeyExchange.RSA, Authentication.RSA, Encryption.CAMELLIA128, MessageDigest.SHA256, Protocol.TLSv1_2, false, EncryptionLevel.HIGH, false, 128, 128, ( String[] ) null, ( String[] ) null ),
    TLS_DH_DSS_WITH_CAMELLIA_128_CBC_SHA256 ( 187, "DH-DSS-CAMELLIA128-SHA256", KeyExchange.DHd, Authentication.DH, Encryption.CAMELLIA128, MessageDigest.SHA256, Protocol.TLSv1_2, false, EncryptionLevel.HIGH, false, 128, 128, ( String[] ) null, ( String[] ) null ),
    TLS_DH_RSA_WITH_CAMELLIA_128_CBC_SHA256 ( 188, "DH-RSA-CAMELLIA128-SHA256", KeyExchange.DHr, Authentication.DH, Encryption.CAMELLIA128, MessageDigest.SHA256, Protocol.TLSv1_2, false, EncryptionLevel.HIGH, false, 128, 128, ( String[] ) null, ( String[] ) null ),
    TLS_DHE_DSS_WITH_CAMELLIA_128_CBC_SHA256 ( 189, "DHE-DSS-CAMELLIA128-SHA256", KeyExchange.EDH, Authentication.DSS, Encryption.CAMELLIA128, MessageDigest.SHA256, Protocol.TLSv1_2, false, EncryptionLevel.HIGH, false, 128, 128, ( String[] ) null, ( String[] ) null ),
    TLS_DHE_RSA_WITH_CAMELLIA_128_CBC_SHA256 ( 190, "DHE-RSA-CAMELLIA128-SHA256", KeyExchange.EDH, Authentication.RSA, Encryption.CAMELLIA128, MessageDigest.SHA256, Protocol.TLSv1_2, false, EncryptionLevel.HIGH, false, 128, 128, ( String[] ) null, ( String[] ) null ),
    TLS_DH_anon_WITH_CAMELLIA_128_CBC_SHA256 ( 191, "ADH-CAMELLIA128-SHA256", KeyExchange.EDH, Authentication.aNULL, Encryption.CAMELLIA128, MessageDigest.SHA256, Protocol.TLSv1_2, false, EncryptionLevel.HIGH, false, 128, 128, ( String[] ) null, ( String[] ) null ),
    TLS_RSA_WITH_CAMELLIA_256_CBC_SHA256 ( 192, "CAMELLIA256-SHA256", KeyExchange.RSA, Authentication.RSA, Encryption.CAMELLIA256, MessageDigest.SHA256, Protocol.TLSv1_2, false, EncryptionLevel.HIGH, false, 256, 256, ( String[] ) null, ( String[] ) null ),
    TLS_DH_DSS_WITH_CAMELLIA_256_CBC_SHA256 ( 193, "DH-DSS-CAMELLIA256-SHA256", KeyExchange.DHd, Authentication.DH, Encryption.CAMELLIA256, MessageDigest.SHA256, Protocol.TLSv1_2, false, EncryptionLevel.HIGH, false, 256, 256, ( String[] ) null, ( String[] ) null ),
    TLS_DH_RSA_WITH_CAMELLIA_256_CBC_SHA256 ( 194, "DH-RSA-CAMELLIA256-SHA256", KeyExchange.DHr, Authentication.DH, Encryption.CAMELLIA256, MessageDigest.SHA256, Protocol.TLSv1_2, false, EncryptionLevel.HIGH, false, 256, 256, ( String[] ) null, ( String[] ) null ),
    TLS_DHE_DSS_WITH_CAMELLIA_256_CBC_SHA256 ( 195, "DHE-DSS-CAMELLIA256-SHA256", KeyExchange.EDH, Authentication.DSS, Encryption.CAMELLIA256, MessageDigest.SHA256, Protocol.TLSv1_2, false, EncryptionLevel.HIGH, false, 256, 256, ( String[] ) null, ( String[] ) null ),
    TLS_DHE_RSA_WITH_CAMELLIA_256_CBC_SHA256 ( 196, "DHE-RSA-CAMELLIA256-SHA256", KeyExchange.EDH, Authentication.RSA, Encryption.CAMELLIA256, MessageDigest.SHA256, Protocol.TLSv1_2, false, EncryptionLevel.HIGH, false, 256, 256, ( String[] ) null, ( String[] ) null ),
    TLS_DH_anon_WITH_CAMELLIA_256_CBC_SHA256 ( 197, "ADH-CAMELLIA256-SHA256", KeyExchange.EDH, Authentication.aNULL, Encryption.CAMELLIA256, MessageDigest.SHA256, Protocol.TLSv1_2, false, EncryptionLevel.HIGH, false, 256, 256, ( String[] ) null, ( String[] ) null ),
    TLS_ECDH_ECDSA_WITH_NULL_SHA ( 49153, "ECDH-ECDSA-NULL-SHA", KeyExchange.ECDHe, Authentication.ECDH, Encryption.eNULL, MessageDigest.SHA1, Protocol.SSLv3, false, EncryptionLevel.STRONG_NONE, true, 0, 0, ( String[] ) null, ( String[] ) null ),
    TLS_ECDH_ECDSA_WITH_RC4_128_SHA ( 49154, "ECDH-ECDSA-RC4-SHA", KeyExchange.ECDHe, Authentication.ECDH, Encryption.RC4, MessageDigest.SHA1, Protocol.SSLv3, false, EncryptionLevel.MEDIUM, false, 128, 128, ( String[] ) null, ( String[] ) null ),
    TLS_ECDH_ECDSA_WITH_3DES_EDE_CBC_SHA ( 49155, "ECDH-ECDSA-DES-CBC3-SHA", KeyExchange.ECDHe, Authentication.ECDH, Encryption.TRIPLE_DES, MessageDigest.SHA1, Protocol.SSLv3, false, EncryptionLevel.MEDIUM, true, 112, 168, ( String[] ) null, ( String[] ) null ),
    TLS_ECDH_ECDSA_WITH_AES_128_CBC_SHA ( 49156, "ECDH-ECDSA-AES128-SHA", KeyExchange.ECDHe, Authentication.ECDH, Encryption.AES128, MessageDigest.SHA1, Protocol.SSLv3, false, EncryptionLevel.HIGH, true, 128, 128, ( String[] ) null, ( String[] ) null ),
    TLS_ECDH_ECDSA_WITH_AES_256_CBC_SHA ( 49157, "ECDH-ECDSA-AES256-SHA", KeyExchange.ECDHe, Authentication.ECDH, Encryption.AES256, MessageDigest.SHA1, Protocol.SSLv3, false, EncryptionLevel.HIGH, true, 256, 256, ( String[] ) null, ( String[] ) null ),
    TLS_ECDHE_ECDSA_WITH_NULL_SHA ( 49158, "ECDHE-ECDSA-NULL-SHA", KeyExchange.EECDH, Authentication.ECDSA, Encryption.eNULL, MessageDigest.SHA1, Protocol.SSLv3, false, EncryptionLevel.STRONG_NONE, true, 0, 0, ( String[] ) null, ( String[] ) null ),
    TLS_ECDHE_ECDSA_WITH_RC4_128_SHA ( 49159, "ECDHE-ECDSA-RC4-SHA", KeyExchange.EECDH, Authentication.ECDSA, Encryption.RC4, MessageDigest.SHA1, Protocol.SSLv3, false, EncryptionLevel.MEDIUM, false, 128, 128, ( String[] ) null, ( String[] ) null ),
    TLS_ECDHE_ECDSA_WITH_3DES_EDE_CBC_SHA ( 49160, "ECDHE-ECDSA-DES-CBC3-SHA", KeyExchange.EECDH, Authentication.ECDSA, Encryption.TRIPLE_DES, MessageDigest.SHA1, Protocol.SSLv3, false, EncryptionLevel.MEDIUM, true, 112, 168, ( String[] ) null, ( String[] ) null ),
    TLS_ECDHE_ECDSA_WITH_AES_128_CBC_SHA ( 49161, "ECDHE-ECDSA-AES128-SHA", KeyExchange.EECDH, Authentication.ECDSA, Encryption.AES128, MessageDigest.SHA1, Protocol.SSLv3, false, EncryptionLevel.HIGH, true, 128, 128, ( String[] ) null, ( String[] ) null ),
    TLS_ECDHE_ECDSA_WITH_AES_256_CBC_SHA ( 49162, "ECDHE-ECDSA-AES256-SHA", KeyExchange.EECDH, Authentication.ECDSA, Encryption.AES256, MessageDigest.SHA1, Protocol.SSLv3, false, EncryptionLevel.HIGH, true, 256, 256, ( String[] ) null, ( String[] ) null ),
    TLS_ECDH_RSA_WITH_NULL_SHA ( 49163, "ECDH-RSA-NULL-SHA", KeyExchange.ECDHr, Authentication.ECDH, Encryption.eNULL, MessageDigest.SHA1, Protocol.SSLv3, false, EncryptionLevel.STRONG_NONE, true, 0, 0, ( String[] ) null, ( String[] ) null ),
    TLS_ECDH_RSA_WITH_RC4_128_SHA ( 49164, "ECDH-RSA-RC4-SHA", KeyExchange.ECDHr, Authentication.ECDH, Encryption.RC4, MessageDigest.SHA1, Protocol.SSLv3, false, EncryptionLevel.MEDIUM, false, 128, 128, ( String[] ) null, ( String[] ) null ),
    TLS_ECDH_RSA_WITH_3DES_EDE_CBC_SHA ( 49165, "ECDH-RSA-DES-CBC3-SHA", KeyExchange.ECDHr, Authentication.ECDH, Encryption.TRIPLE_DES, MessageDigest.SHA1, Protocol.SSLv3, false, EncryptionLevel.MEDIUM, true, 112, 168, ( String[] ) null, ( String[] ) null ),
    TLS_ECDH_RSA_WITH_AES_128_CBC_SHA ( 49166, "ECDH-RSA-AES128-SHA", KeyExchange.ECDHr, Authentication.ECDH, Encryption.AES128, MessageDigest.SHA1, Protocol.SSLv3, false, EncryptionLevel.HIGH, true, 128, 128, ( String[] ) null, ( String[] ) null ),
    TLS_ECDH_RSA_WITH_AES_256_CBC_SHA ( 49167, "ECDH-RSA-AES256-SHA", KeyExchange.ECDHr, Authentication.ECDH, Encryption.AES256, MessageDigest.SHA1, Protocol.SSLv3, false, EncryptionLevel.HIGH, true, 256, 256, ( String[] ) null, ( String[] ) null ),
    TLS_ECDHE_RSA_WITH_NULL_SHA ( 49168, "ECDHE-RSA-NULL-SHA", KeyExchange.EECDH, Authentication.RSA, Encryption.eNULL, MessageDigest.SHA1, Protocol.SSLv3, false, EncryptionLevel.STRONG_NONE, true, 0, 0, ( String[] ) null, ( String[] ) null ),
    TLS_ECDHE_RSA_WITH_RC4_128_SHA ( 49169, "ECDHE-RSA-RC4-SHA", KeyExchange.EECDH, Authentication.RSA, Encryption.RC4, MessageDigest.SHA1, Protocol.SSLv3, false, EncryptionLevel.MEDIUM, false, 128, 128, ( String[] ) null, ( String[] ) null ),
    TLS_ECDHE_RSA_WITH_3DES_EDE_CBC_SHA ( 49170, "ECDHE-RSA-DES-CBC3-SHA", KeyExchange.EECDH, Authentication.RSA, Encryption.TRIPLE_DES, MessageDigest.SHA1, Protocol.SSLv3, false, EncryptionLevel.MEDIUM, true, 112, 168, ( String[] ) null, ( String[] ) null ),
    TLS_ECDHE_RSA_WITH_AES_128_CBC_SHA ( 49171, "ECDHE-RSA-AES128-SHA", KeyExchange.EECDH, Authentication.RSA, Encryption.AES128, MessageDigest.SHA1, Protocol.SSLv3, false, EncryptionLevel.HIGH, true, 128, 128, ( String[] ) null, ( String[] ) null ),
    TLS_ECDHE_RSA_WITH_AES_256_CBC_SHA ( 49172, "ECDHE-RSA-AES256-SHA", KeyExchange.EECDH, Authentication.RSA, Encryption.AES256, MessageDigest.SHA1, Protocol.SSLv3, false, EncryptionLevel.HIGH, true, 256, 256, ( String[] ) null, ( String[] ) null ),
    TLS_ECDH_anon_WITH_NULL_SHA ( 49173, "AECDH-NULL-SHA", KeyExchange.EECDH, Authentication.aNULL, Encryption.eNULL, MessageDigest.SHA1, Protocol.SSLv3, false, EncryptionLevel.STRONG_NONE, true, 0, 0, ( String[] ) null, ( String[] ) null ),
    TLS_ECDH_anon_WITH_RC4_128_SHA ( 49174, "AECDH-RC4-SHA", KeyExchange.EECDH, Authentication.aNULL, Encryption.RC4, MessageDigest.SHA1, Protocol.SSLv3, false, EncryptionLevel.MEDIUM, false, 128, 128, ( String[] ) null, ( String[] ) null ),
    TLS_ECDH_anon_WITH_3DES_EDE_CBC_SHA ( 49175, "AECDH-DES-CBC3-SHA", KeyExchange.EECDH, Authentication.aNULL, Encryption.TRIPLE_DES, MessageDigest.SHA1, Protocol.SSLv3, false, EncryptionLevel.MEDIUM, true, 112, 168, ( String[] ) null, ( String[] ) null ),
    TLS_ECDH_anon_WITH_AES_128_CBC_SHA ( 49176, "AECDH-AES128-SHA", KeyExchange.EECDH, Authentication.aNULL, Encryption.AES128, MessageDigest.SHA1, Protocol.SSLv3, false, EncryptionLevel.HIGH, true, 128, 128, ( String[] ) null, ( String[] ) null ),
    TLS_ECDH_anon_WITH_AES_256_CBC_SHA ( 49177, "AECDH-AES256-SHA", KeyExchange.EECDH, Authentication.aNULL, Encryption.AES256, MessageDigest.SHA1, Protocol.SSLv3, false, EncryptionLevel.HIGH, true, 256, 256, ( String[] ) null, ( String[] ) null ),
    TLS_SRP_SHA_WITH_3DES_EDE_CBC_SHA ( 49178, "SRP-3DES-EDE-CBC-SHA", KeyExchange.SRP, Authentication.SRP, Encryption.TRIPLE_DES, MessageDigest.SHA1, Protocol.SSLv3, false, EncryptionLevel.MEDIUM, false, 112, 168, ( String[] ) null, ( String[] ) null ),
    TLS_SRP_SHA_RSA_WITH_3DES_EDE_CBC_SHA ( 49179, "SRP-RSA-3DES-EDE-CBC-SHA", KeyExchange.SRP, Authentication.RSA, Encryption.TRIPLE_DES, MessageDigest.SHA1, Protocol.SSLv3, false, EncryptionLevel.MEDIUM, false, 112, 168, ( String[] ) null, ( String[] ) null ),
    TLS_SRP_SHA_DSS_WITH_3DES_EDE_CBC_SHA ( 49180, "SRP-DSS-3DES-EDE-CBC-SHA", KeyExchange.SRP, Authentication.DSS, Encryption.TRIPLE_DES, MessageDigest.SHA1, Protocol.SSLv3, false, EncryptionLevel.MEDIUM, false, 112, 168, ( String[] ) null, ( String[] ) null ),
    TLS_SRP_SHA_WITH_AES_128_CBC_SHA ( 49181, "SRP-AES-128-CBC-SHA", KeyExchange.SRP, Authentication.SRP, Encryption.AES128, MessageDigest.SHA1, Protocol.SSLv3, false, EncryptionLevel.HIGH, false, 128, 128, ( String[] ) null, ( String[] ) null ),
    TLS_SRP_SHA_RSA_WITH_AES_128_CBC_SHA ( 49182, "SRP-RSA-AES-128-CBC-SHA", KeyExchange.SRP, Authentication.RSA, Encryption.AES128, MessageDigest.SHA1, Protocol.SSLv3, false, EncryptionLevel.HIGH, false, 128, 128, ( String[] ) null, ( String[] ) null ),
    TLS_SRP_SHA_DSS_WITH_AES_128_CBC_SHA ( 49183, "SRP-DSS-AES-128-CBC-SHA", KeyExchange.SRP, Authentication.DSS, Encryption.AES128, MessageDigest.SHA1, Protocol.SSLv3, false, EncryptionLevel.HIGH, false, 128, 128, ( String[] ) null, ( String[] ) null ),
    TLS_SRP_SHA_WITH_AES_256_CBC_SHA ( 49184, "SRP-AES-256-CBC-SHA", KeyExchange.SRP, Authentication.SRP, Encryption.AES256, MessageDigest.SHA1, Protocol.SSLv3, false, EncryptionLevel.HIGH, false, 256, 256, ( String[] ) null, ( String[] ) null ),
    TLS_SRP_SHA_RSA_WITH_AES_256_CBC_SHA ( 49185, "SRP-RSA-AES-256-CBC-SHA", KeyExchange.SRP, Authentication.RSA, Encryption.AES256, MessageDigest.SHA1, Protocol.SSLv3, false, EncryptionLevel.HIGH, false, 256, 256, ( String[] ) null, ( String[] ) null ),
    TLS_SRP_SHA_DSS_WITH_AES_256_CBC_SHA ( 49186, "SRP-DSS-AES-256-CBC-SHA", KeyExchange.SRP, Authentication.DSS, Encryption.AES256, MessageDigest.SHA1, Protocol.SSLv3, false, EncryptionLevel.HIGH, false, 256, 256, ( String[] ) null, ( String[] ) null ),
    TLS_ECDHE_ECDSA_WITH_AES_128_CBC_SHA256 ( 49187, "ECDHE-ECDSA-AES128-SHA256", KeyExchange.EECDH, Authentication.ECDSA, Encryption.AES128, MessageDigest.SHA256, Protocol.TLSv1_2, false, EncryptionLevel.HIGH, true, 128, 128, ( String[] ) null, ( String[] ) null ),
    TLS_ECDHE_ECDSA_WITH_AES_256_CBC_SHA384 ( 49188, "ECDHE-ECDSA-AES256-SHA384", KeyExchange.EECDH, Authentication.ECDSA, Encryption.AES256, MessageDigest.SHA384, Protocol.TLSv1_2, false, EncryptionLevel.HIGH, true, 256, 256, ( String[] ) null, ( String[] ) null ),
    TLS_ECDH_ECDSA_WITH_AES_128_CBC_SHA256 ( 49189, "ECDH-ECDSA-AES128-SHA256", KeyExchange.ECDHe, Authentication.ECDH, Encryption.AES128, MessageDigest.SHA256, Protocol.TLSv1_2, false, EncryptionLevel.HIGH, true, 128, 128, ( String[] ) null, ( String[] ) null ),
    TLS_ECDH_ECDSA_WITH_AES_256_CBC_SHA384 ( 49190, "ECDH-ECDSA-AES256-SHA384", KeyExchange.ECDHe, Authentication.ECDH, Encryption.AES256, MessageDigest.SHA384, Protocol.TLSv1_2, false, EncryptionLevel.HIGH, true, 256, 256, ( String[] ) null, ( String[] ) null ),
    TLS_ECDHE_RSA_WITH_AES_128_CBC_SHA256 ( 49191, "ECDHE-RSA-AES128-SHA256", KeyExchange.EECDH, Authentication.RSA, Encryption.AES128, MessageDigest.SHA256, Protocol.TLSv1_2, false, EncryptionLevel.HIGH, true, 128, 128, ( String[] ) null, ( String[] ) null ),
    TLS_ECDHE_RSA_WITH_AES_256_CBC_SHA384 ( 49192, "ECDHE-RSA-AES256-SHA384", KeyExchange.EECDH, Authentication.RSA, Encryption.AES256, MessageDigest.SHA384, Protocol.TLSv1_2, false, EncryptionLevel.HIGH, true, 256, 256, ( String[] ) null, ( String[] ) null ),
    TLS_ECDH_RSA_WITH_AES_128_CBC_SHA256 ( 49193, "ECDH-RSA-AES128-SHA256", KeyExchange.ECDHr, Authentication.ECDH, Encryption.AES128, MessageDigest.SHA256, Protocol.TLSv1_2, false, EncryptionLevel.HIGH, true, 128, 128, ( String[] ) null, ( String[] ) null ),
    TLS_ECDH_RSA_WITH_AES_256_CBC_SHA384 ( 49194, "ECDH-RSA-AES256-SHA384", KeyExchange.ECDHr, Authentication.ECDH, Encryption.AES256, MessageDigest.SHA384, Protocol.TLSv1_2, false, EncryptionLevel.HIGH, true, 256, 256, ( String[] ) null, ( String[] ) null ),
    TLS_ECDHE_ECDSA_WITH_AES_128_GCM_SHA256 ( 49195, "ECDHE-ECDSA-AES128-GCM-SHA256", KeyExchange.EECDH, Authentication.ECDSA, Encryption.AES128GCM, MessageDigest.AEAD, Protocol.TLSv1_2, false, EncryptionLevel.HIGH, true, 128, 128, ( String[] ) null, ( String[] ) null ),
    TLS_ECDHE_ECDSA_WITH_AES_256_GCM_SHA384 ( 49196, "ECDHE-ECDSA-AES256-GCM-SHA384", KeyExchange.EECDH, Authentication.ECDSA, Encryption.AES256GCM, MessageDigest.AEAD, Protocol.TLSv1_2, false, EncryptionLevel.HIGH, true, 256, 256, ( String[] ) null, ( String[] ) null ),
    TLS_ECDH_ECDSA_WITH_AES_128_GCM_SHA256 ( 49197, "ECDH-ECDSA-AES128-GCM-SHA256", KeyExchange.ECDHe, Authentication.ECDH, Encryption.AES128GCM, MessageDigest.AEAD, Protocol.TLSv1_2, false, EncryptionLevel.HIGH, true, 128, 128, ( String[] ) null, ( String[] ) null ),
    TLS_ECDH_ECDSA_WITH_AES_256_GCM_SHA384 ( 49198, "ECDH-ECDSA-AES256-GCM-SHA384", KeyExchange.ECDHe, Authentication.ECDH, Encryption.AES256GCM, MessageDigest.AEAD, Protocol.TLSv1_2, false, EncryptionLevel.HIGH, true, 256, 256, ( String[] ) null, ( String[] ) null ),
    TLS_ECDHE_RSA_WITH_AES_128_GCM_SHA256 ( 49199, "ECDHE-RSA-AES128-GCM-SHA256", KeyExchange.EECDH, Authentication.RSA, Encryption.AES128GCM, MessageDigest.AEAD, Protocol.TLSv1_2, false, EncryptionLevel.HIGH, true, 128, 128, ( String[] ) null, ( String[] ) null ),
    TLS_ECDHE_RSA_WITH_AES_256_GCM_SHA384 ( 49200, "ECDHE-RSA-AES256-GCM-SHA384", KeyExchange.EECDH, Authentication.RSA, Encryption.AES256GCM, MessageDigest.AEAD, Protocol.TLSv1_2, false, EncryptionLevel.HIGH, true, 256, 256, ( String[] ) null, ( String[] ) null ),
    TLS_ECDH_RSA_WITH_AES_128_GCM_SHA256 ( 49201, "ECDH-RSA-AES128-GCM-SHA256", KeyExchange.ECDHr, Authentication.ECDH, Encryption.AES128GCM, MessageDigest.AEAD, Protocol.TLSv1_2, false, EncryptionLevel.HIGH, true, 128, 128, ( String[] ) null, ( String[] ) null ),
    TLS_ECDH_RSA_WITH_AES_256_GCM_SHA384 ( 49202, "ECDH-RSA-AES256-GCM-SHA384", KeyExchange.ECDHr, Authentication.ECDH, Encryption.AES256GCM, MessageDigest.AEAD, Protocol.TLSv1_2, false, EncryptionLevel.HIGH, true, 256, 256, ( String[] ) null, ( String[] ) null ),
    TLS_ECDHE_PSK_WITH_RC4_128_SHA ( 49203, "ECDHE-PSK-RC4-SHA", KeyExchange.ECDHEPSK, Authentication.PSK, Encryption.RC4, MessageDigest.SHA1, Protocol.SSLv3, false, EncryptionLevel.MEDIUM, false, 128, 128, ( String[] ) null, ( String[] ) null ),
    TLS_ECDHE_PSK_WITH_3DES_EDE_CBC_SHA ( 49204, "ECDHE-PSK-3DES-EDE-CBC-SHA", KeyExchange.ECDHEPSK, Authentication.PSK, Encryption.TRIPLE_DES, MessageDigest.SHA1, Protocol.SSLv3, false, EncryptionLevel.MEDIUM, true, 112, 168, ( String[] ) null, ( String[] ) null ),
    TLS_ECDHE_PSK_WITH_AES_128_CBC_SHA ( 49205, "ECDHE-PSK-AES128-CBC-SHA", KeyExchange.ECDHEPSK, Authentication.PSK, Encryption.AES128, MessageDigest.SHA1, Protocol.SSLv3, false, EncryptionLevel.HIGH, true, 128, 128, ( String[] ) null, ( String[] ) null ),
    TLS_ECDHE_PSK_WITH_AES_256_CBC_SHA ( 49206, "ECDHE-PSK-AES256-CBC-SHA", KeyExchange.ECDHEPSK, Authentication.PSK, Encryption.AES256, MessageDigest.SHA1, Protocol.SSLv3, false, EncryptionLevel.HIGH, true, 256, 256, ( String[] ) null, ( String[] ) null ),
    TLS_ECDHE_PSK_WITH_AES_128_CBC_SHA256 ( 49207, "ECDHE-PSK-AES128-CBC-SHA256", KeyExchange.ECDHEPSK, Authentication.PSK, Encryption.AES128, MessageDigest.SHA256, Protocol.TLSv1, false, EncryptionLevel.HIGH, true, 128, 128, ( String[] ) null, ( String[] ) null ),
    TLS_ECDHE_PSK_WITH_AES_256_CBC_SHA384 ( 49208, "ECDHE-PSK-AES256-CBC-SHA384", KeyExchange.ECDHEPSK, Authentication.PSK, Encryption.AES256, MessageDigest.SHA384, Protocol.TLSv1, false, EncryptionLevel.HIGH, true, 256, 256, ( String[] ) null, ( String[] ) null ),
    TLS_ECDHE_PSK_WITH_NULL_SHA ( 49209, "ECDHE-PSK-NULL-SHA", KeyExchange.ECDHEPSK, Authentication.PSK, Encryption.eNULL, MessageDigest.SHA1, Protocol.SSLv3, false, EncryptionLevel.STRONG_NONE, true, 0, 0, ( String[] ) null, ( String[] ) null ),
    TLS_ECDHE_PSK_WITH_NULL_SHA256 ( 49210, "ECDHE-PSK-NULL-SHA256", KeyExchange.ECDHEPSK, Authentication.PSK, Encryption.eNULL, MessageDigest.SHA256, Protocol.TLSv1, false, EncryptionLevel.STRONG_NONE, true, 0, 0, ( String[] ) null, ( String[] ) null ),
    TLS_ECDHE_PSK_WITH_NULL_SHA384 ( 49211, "ECDHE-PSK-NULL-SHA384", KeyExchange.ECDHEPSK, Authentication.PSK, Encryption.eNULL, MessageDigest.SHA384, Protocol.TLSv1, false, EncryptionLevel.STRONG_NONE, true, 0, 0, ( String[] ) null, ( String[] ) null ),
    TLS_ECDHE_ECDSA_WITH_CAMELLIA_128_CBC_SHA256 ( 49266, "ECDHE-ECDSA-CAMELLIA128-SHA256", KeyExchange.EECDH, Authentication.ECDSA, Encryption.CAMELLIA128, MessageDigest.SHA256, Protocol.TLSv1_2, false, EncryptionLevel.HIGH, true, 128, 128, ( String[] ) null, ( String[] ) null ),
    TLS_ECDHE_ECDSA_WITH_CAMELLIA_256_CBC_SHA384 ( 49267, "ECDHE-ECDSA-CAMELLIA256-SHA384", KeyExchange.EECDH, Authentication.ECDSA, Encryption.CAMELLIA256, MessageDigest.SHA384, Protocol.TLSv1_2, false, EncryptionLevel.HIGH, true, 256, 256, ( String[] ) null, ( String[] ) null ),
    TLS_ECDH_ECDSA_WITH_CAMELLIA_128_CBC_SHA256 ( 49268, "ECDH-ECDSA-CAMELLIA128-SHA256", KeyExchange.ECDHe, Authentication.ECDH, Encryption.CAMELLIA128, MessageDigest.SHA256, Protocol.TLSv1_2, false, EncryptionLevel.HIGH, true, 128, 128, ( String[] ) null, ( String[] ) null ),
    TLS_ECDH_ECDSA_WITH_CAMELLIA_256_CBC_SHA384 ( 49269, "ECDH-ECDSA-CAMELLIA256-SHA384", KeyExchange.ECDHe, Authentication.ECDH, Encryption.CAMELLIA256, MessageDigest.SHA384, Protocol.TLSv1_2, false, EncryptionLevel.HIGH, true, 256, 256, ( String[] ) null, ( String[] ) null ),
    TLS_ECDHE_RSA_WITH_CAMELLIA_128_CBC_SHA256 ( 49270, "ECDHE-RSA-CAMELLIA128-SHA256", KeyExchange.EECDH, Authentication.RSA, Encryption.CAMELLIA128, MessageDigest.SHA256, Protocol.TLSv1_2, false, EncryptionLevel.HIGH, true, 128, 128, ( String[] ) null, ( String[] ) null ),
    TLS_ECDHE_RSA_WITH_CAMELLIA_256_CBC_SHA384 ( 49271, "ECDHE-RSA-CAMELLIA256-SHA384", KeyExchange.EECDH, Authentication.RSA, Encryption.CAMELLIA256, MessageDigest.SHA384, Protocol.TLSv1_2, false, EncryptionLevel.HIGH, true, 256, 256, ( String[] ) null, ( String[] ) null ),
    TLS_ECDH_RSA_WITH_CAMELLIA_128_CBC_SHA256 ( 49272, "ECDH-RSA-CAMELLIA128-SHA256", KeyExchange.ECDHr, Authentication.ECDH, Encryption.CAMELLIA128, MessageDigest.SHA256, Protocol.TLSv1_2, false, EncryptionLevel.HIGH, true, 128, 128, ( String[] ) null, ( String[] ) null ),
    TLS_ECDH_RSA_WITH_CAMELLIA_256_CBC_SHA384 ( 49273, "ECDH-RSA-CAMELLIA256-SHA384", KeyExchange.ECDHr, Authentication.ECDH, Encryption.CAMELLIA256, MessageDigest.SHA384, Protocol.TLSv1_2, false, EncryptionLevel.HIGH, true, 256, 256, ( String[] ) null, ( String[] ) null ),
    TLS_PSK_WITH_CAMELLIA_128_CBC_SHA256 ( 49300, "PSK-CAMELLIA128-SHA256", KeyExchange.PSK, Authentication.PSK, Encryption.CAMELLIA128, MessageDigest.SHA256, Protocol.TLSv1, false, EncryptionLevel.HIGH, false, 128, 128, ( String[] ) null, ( String[] ) null ),
    TLS_PSK_WITH_CAMELLIA_256_CBC_SHA384 ( 49301, "PSK-CAMELLIA256-SHA384", KeyExchange.PSK, Authentication.PSK, Encryption.CAMELLIA256, MessageDigest.SHA384, Protocol.TLSv1, false, EncryptionLevel.HIGH, false, 256, 256, ( String[] ) null, ( String[] ) null ),
    TLS_DHE_PSK_WITH_CAMELLIA_128_CBC_SHA256 ( 49302, "DHE-PSK-CAMELLIA128-SHA256", KeyExchange.DHEPSK, Authentication.PSK, Encryption.CAMELLIA128, MessageDigest.SHA256, Protocol.TLSv1, false, EncryptionLevel.HIGH, false, 128, 128, ( String[] ) null, ( String[] ) null ),
    TLS_DHE_PSK_WITH_CAMELLIA_256_CBC_SHA384 ( 49303, "DHE-PSK-CAMELLIA256-SHA384", KeyExchange.DHEPSK, Authentication.PSK, Encryption.CAMELLIA256, MessageDigest.SHA384, Protocol.TLSv1, false, EncryptionLevel.HIGH, false, 256, 256, ( String[] ) null, ( String[] ) null ),
    TLS_RSA_PSK_WITH_CAMELLIA_128_CBC_SHA256 ( 49304, "RSA-PSK-CAMELLIA128-SHA256", KeyExchange.RSAPSK, Authentication.RSA, Encryption.CAMELLIA128, MessageDigest.SHA256, Protocol.TLSv1, false, EncryptionLevel.HIGH, false, 128, 128, ( String[] ) null, ( String[] ) null ),
    TLS_RSA_PSK_WITH_CAMELLIA_256_CBC_SHA384 ( 49305, "RSA-PSK-CAMELLIA256-SHA384", KeyExchange.RSAPSK, Authentication.RSA, Encryption.CAMELLIA256, MessageDigest.SHA384, Protocol.TLSv1, false, EncryptionLevel.HIGH, false, 256, 256, ( String[] ) null, ( String[] ) null ),
    TLS_ECDHE_PSK_WITH_CAMELLIA_128_CBC_SHA256 ( 49306, "ECDHE-PSK-CAMELLIA128-SHA256", KeyExchange.ECDHEPSK, Authentication.PSK, Encryption.CAMELLIA128, MessageDigest.SHA256, Protocol.TLSv1, false, EncryptionLevel.HIGH, false, 128, 128, ( String[] ) null, ( String[] ) null ),
    TLS_ECDHE_PSK_WITH_CAMELLIA_256_CBC_SHA384 ( 49307, "ECDHE-PSK-CAMELLIA256-SHA384", KeyExchange.ECDHEPSK, Authentication.PSK, Encryption.CAMELLIA256, MessageDigest.SHA384, Protocol.TLSv1, false, EncryptionLevel.HIGH, false, 256, 256, ( String[] ) null, ( String[] ) null ),
    TLS_RSA_WITH_AES_128_CCM ( 49308, "AES128-CCM", KeyExchange.RSA, Authentication.RSA, Encryption.AES128CCM, MessageDigest.AEAD, Protocol.TLSv1_2, false, EncryptionLevel.HIGH, false, 128, 128, ( String[] ) null, ( String[] ) null ),
    TLS_RSA_WITH_AES_256_CCM ( 49309, "AES256-CCM", KeyExchange.RSA, Authentication.RSA, Encryption.AES256CCM, MessageDigest.AEAD, Protocol.TLSv1_2, false, EncryptionLevel.HIGH, false, 256, 256, ( String[] ) null, ( String[] ) null ),
    TLS_DHE_RSA_WITH_AES_128_CCM ( 49310, "DHE-RSA-AES128-CCM", KeyExchange.EDH, Authentication.RSA, Encryption.AES128CCM, MessageDigest.AEAD, Protocol.TLSv1_2, false, EncryptionLevel.HIGH, false, 128, 128, ( String[] ) null, ( String[] ) null ),
    TLS_DHE_RSA_WITH_AES_256_CCM ( 49311, "DHE-RSA-AES256-CCM", KeyExchange.EDH, Authentication.RSA, Encryption.AES256CCM, MessageDigest.AEAD, Protocol.TLSv1_2, false, EncryptionLevel.HIGH, false, 256, 256, ( String[] ) null, ( String[] ) null ),
    TLS_RSA_WITH_AES_128_CCM_8 ( 49312, "AES128-CCM8", KeyExchange.RSA, Authentication.RSA, Encryption.AES128CCM8, MessageDigest.AEAD, Protocol.TLSv1_2, false, EncryptionLevel.HIGH, false, 128, 128, ( String[] ) null, ( String[] ) null ),
    TLS_RSA_WITH_AES_256_CCM_8 ( 49313, "AES256-CCM8", KeyExchange.RSA, Authentication.RSA, Encryption.AES256CCM8, MessageDigest.AEAD, Protocol.TLSv1_2, false, EncryptionLevel.HIGH, false, 256, 256, ( String[] ) null, ( String[] ) null ),
    TLS_DHE_RSA_WITH_AES_128_CCM_8 ( 49314, "DHE-RSA-AES128-CCM8", KeyExchange.EDH, Authentication.RSA, Encryption.AES128CCM8, MessageDigest.AEAD, Protocol.TLSv1_2, false, EncryptionLevel.HIGH, false, 128, 128, ( String[] ) null, ( String[] ) null ),
    TLS_DHE_RSA_WITH_AES_256_CCM_8 ( 49315, "DHE-RSA-AES256-CCM8", KeyExchange.EDH, Authentication.RSA, Encryption.AES256CCM8, MessageDigest.AEAD, Protocol.TLSv1_2, false, EncryptionLevel.HIGH, false, 256, 256, ( String[] ) null, ( String[] ) null ),
    TLS_PSK_WITH_AES_128_CCM ( 49316, "PSK-AES128-CCM", KeyExchange.PSK, Authentication.PSK, Encryption.AES128CCM, MessageDigest.AEAD, Protocol.TLSv1_2, false, EncryptionLevel.HIGH, false, 128, 128, ( String[] ) null, ( String[] ) null ),
    TLS_PSK_WITH_AES_256_CCM ( 49317, "PSK-AES256-CCM", KeyExchange.PSK, Authentication.PSK, Encryption.AES256CCM, MessageDigest.AEAD, Protocol.TLSv1_2, false, EncryptionLevel.HIGH, false, 256, 256, ( String[] ) null, ( String[] ) null ),
    TLS_DHE_PSK_WITH_AES_128_CCM ( 49318, "DHE-PSK-AES128-CCM", KeyExchange.DHEPSK, Authentication.PSK, Encryption.AES128CCM, MessageDigest.AEAD, Protocol.TLSv1_2, false, EncryptionLevel.HIGH, false, 128, 128, ( String[] ) null, ( String[] ) null ),
    TLS_DHE_PSK_WITH_AES_256_CCM ( 49319, "DHE-PSK-AES256-CCM", KeyExchange.DHEPSK, Authentication.PSK, Encryption.AES256CCM, MessageDigest.AEAD, Protocol.TLSv1_2, false, EncryptionLevel.HIGH, false, 256, 256, ( String[] ) null, ( String[] ) null ),
    TLS_PSK_WITH_AES_128_CCM_8 ( 49320, "PSK-AES128-CCM8", KeyExchange.PSK, Authentication.PSK, Encryption.AES128CCM8, MessageDigest.AEAD, Protocol.TLSv1_2, false, EncryptionLevel.HIGH, false, 128, 128, ( String[] ) null, ( String[] ) null ),
    TLS_PSK_WITH_AES_256_CCM_8 ( 49321, "PSK-AES256-CCM8", KeyExchange.PSK, Authentication.PSK, Encryption.AES256CCM8, MessageDigest.AEAD, Protocol.TLSv1_2, false, EncryptionLevel.HIGH, false, 256, 256, ( String[] ) null, ( String[] ) null ),
    TLS_PSK_DHE_WITH_AES_128_CCM_8 ( 49322, "DHE-PSK-AES128-CCM8", KeyExchange.DHEPSK, Authentication.PSK, Encryption.AES128CCM8, MessageDigest.AEAD, Protocol.TLSv1_2, false, EncryptionLevel.HIGH, false, 128, 128, ( String[] ) null, ( String[] ) null ),
    TLS_PSK_DHE_WITH_AES_256_CCM_8 ( 49323, "DHE-PSK-AES256-CCM8", KeyExchange.DHEPSK, Authentication.PSK, Encryption.AES256CCM8, MessageDigest.AEAD, Protocol.TLSv1_2, false, EncryptionLevel.HIGH, false, 256, 256, ( String[] ) null, ( String[] ) null ),
    TLS_ECDHE_ECDSA_WITH_AES_128_CCM ( 49324, "ECDHE-ECDSA-AES128-CCM", KeyExchange.EECDH, Authentication.ECDSA, Encryption.AES128CCM, MessageDigest.AEAD, Protocol.TLSv1_2, false, EncryptionLevel.HIGH, false, 128, 128, ( String[] ) null, ( String[] ) null ),
    TLS_ECDHE_ECDSA_WITH_AES_256_CCM ( 49325, "ECDHE-ECDSA-AES256-CCM", KeyExchange.EECDH, Authentication.ECDSA, Encryption.AES256CCM, MessageDigest.AEAD, Protocol.TLSv1_2, false, EncryptionLevel.HIGH, false, 256, 256, ( String[] ) null, ( String[] ) null ),
    TLS_ECDHE_ECDSA_WITH_AES_128_CCM_8 ( 49326, "ECDHE-ECDSA-AES128-CCM8", KeyExchange.EECDH, Authentication.ECDSA, Encryption.AES128CCM8, MessageDigest.AEAD, Protocol.TLSv1_2, false, EncryptionLevel.HIGH, false, 128, 128, ( String[] ) null, ( String[] ) null ),
    TLS_ECDHE_ECDSA_WITH_AES_256_CCM_8 ( 49327, "ECDHE-ECDSA-AES256-CCM8", KeyExchange.EECDH, Authentication.ECDSA, Encryption.AES256CCM8, MessageDigest.AEAD, Protocol.TLSv1_2, false, EncryptionLevel.HIGH, false, 256, 256, ( String[] ) null, ( String[] ) null ),
    TLS_ECDHE_RSA_WITH_CHACHA20_POLY1305_SHA256 ( 52392, "ECDHE-RSA-CHACHA20-POLY1305", KeyExchange.EECDH, Authentication.RSA, Encryption.CHACHA20POLY1305, MessageDigest.AEAD, Protocol.TLSv1_2, false, EncryptionLevel.HIGH, false, 256, 256, ( String[] ) null, ( String[] ) null ),
    TLS_ECDHE_ECDSA_WITH_CHACHA20_POLY1305_SHA256 ( 52393, "ECDHE-ECDSA-CHACHA20-POLY1305", KeyExchange.EECDH, Authentication.ECDSA, Encryption.CHACHA20POLY1305, MessageDigest.AEAD, Protocol.TLSv1_2, false, EncryptionLevel.HIGH, false, 256, 256, ( String[] ) null, ( String[] ) null ),
    TLS_DHE_RSA_WITH_CHACHA20_POLY1305_SHA256 ( 52394, "DHE-RSA-CHACHA20-POLY1305", KeyExchange.EDH, Authentication.RSA, Encryption.CHACHA20POLY1305, MessageDigest.AEAD, Protocol.TLSv1_2, false, EncryptionLevel.HIGH, false, 256, 256, ( String[] ) null, ( String[] ) null ),
    TLS_PSK_WITH_CHACHA20_POLY1305_SHA256 ( 52395, "PSK-CHACHA20-POLY1305", KeyExchange.PSK, Authentication.PSK, Encryption.CHACHA20POLY1305, MessageDigest.AEAD, Protocol.TLSv1_2, false, EncryptionLevel.HIGH, false, 256, 256, ( String[] ) null, ( String[] ) null ),
    TLS_ECDHE_PSK_WITH_CHACHA20_POLY1305_SHA256 ( 52396, "ECDHE-PSK-CHACHA20-POLY1305", KeyExchange.ECDHEPSK, Authentication.PSK, Encryption.CHACHA20POLY1305, MessageDigest.AEAD, Protocol.TLSv1_2, false, EncryptionLevel.HIGH, false, 256, 256, ( String[] ) null, ( String[] ) null ),
    TLS_DHE_PSK_WITH_CHACHA20_POLY1305_SHA256 ( 52397, "DHE-PSK-CHACHA20-POLY1305", KeyExchange.DHEPSK, Authentication.PSK, Encryption.CHACHA20POLY1305, MessageDigest.AEAD, Protocol.TLSv1_2, false, EncryptionLevel.HIGH, false, 256, 256, ( String[] ) null, ( String[] ) null ),
    TLS_RSA_PSK_WITH_CHACHA20_POLY1305_SHA256 ( 52398, "RSA-PSK-CHACHA20-POLY1305", KeyExchange.RSAPSK, Authentication.RSA, Encryption.CHACHA20POLY1305, MessageDigest.AEAD, Protocol.TLSv1_2, false, EncryptionLevel.HIGH, false, 256, 256, ( String[] ) null, ( String[] ) null ),
    SSL_CK_RC4_128_WITH_MD5 ( -1, "RC4-MD5", KeyExchange.RSA, Authentication.RSA, Encryption.RC4, MessageDigest.MD5, Protocol.SSLv2, false, EncryptionLevel.MEDIUM, false, 128, 128, ( String[] ) null, ( String[] ) null ),
    SSL2_RC4_128_EXPORT40_WITH_MD5 ( -1, "EXP-RC4-MD5", KeyExchange.RSA, Authentication.RSA, Encryption.RC4, MessageDigest.MD5, Protocol.SSLv2, true, EncryptionLevel.EXP40, false, 40, 128, new String[] { "SSL_RC4_128_EXPORT40_WITH_MD5" }, ( String[] ) null ),
    SSL_CK_RC2_128_CBC_WITH_MD5 ( -1, "RC2-CBC-MD5", KeyExchange.RSA, Authentication.RSA, Encryption.RC2, MessageDigest.MD5, Protocol.SSLv2, false, EncryptionLevel.MEDIUM, false, 128, 128, ( String[] ) null, ( String[] ) null ),
    SSL_CK_RC2_128_CBC_EXPORT40_WITH_MD5 ( -1, "EXP-RC2-CBC-MD5", KeyExchange.RSA, Authentication.RSA, Encryption.RC2, MessageDigest.MD5, Protocol.SSLv2, true, EncryptionLevel.EXP40, false, 40, 128, ( String[] ) null, ( String[] ) null ),
    SSL2_IDEA_128_CBC_WITH_MD5 ( -1, "IDEA-CBC-MD5", KeyExchange.RSA, Authentication.RSA, Encryption.IDEA, MessageDigest.MD5, Protocol.SSLv2, false, EncryptionLevel.MEDIUM, false, 128, 128, new String[] { "SSL_CK_IDEA_128_CBC_WITH_MD5" }, ( String[] ) null ),
    SSL2_DES_64_CBC_WITH_MD5 ( -1, "DES-CBC-MD5", KeyExchange.RSA, Authentication.RSA, Encryption.DES, MessageDigest.MD5, Protocol.SSLv2, false, EncryptionLevel.LOW, false, 56, 56, new String[] { "SSL_CK_DES_64_CBC_WITH_MD5" }, ( String[] ) null ),
    SSL2_DES_192_EDE3_CBC_WITH_MD5 ( -1, "DES-CBC3-MD5", KeyExchange.RSA, Authentication.RSA, Encryption.TRIPLE_DES, MessageDigest.MD5, Protocol.SSLv2, false, EncryptionLevel.MEDIUM, false, 112, 168, new String[] { "SSL_CK_DES_192_EDE3_CBC_WITH_MD5" }, ( String[] ) null );
    private final int id;
    private final String openSSLAlias;
    private final Set<String> openSSLAltNames;
    private final Set<String> jsseNames;
    private final KeyExchange kx;
    private final Authentication au;
    private final Encryption enc;
    private final MessageDigest mac;
    private final Protocol protocol;
    private final boolean export;
    private final EncryptionLevel level;
    private final boolean fipsCompatible;
    private final int strength_bits;
    private final int alg_bits;
    private static final Map<Integer, Cipher> idMap;
    private Cipher ( final int id, final String openSSLAlias, final KeyExchange kx, final Authentication au, final Encryption enc, final MessageDigest mac, final Protocol protocol, final boolean export, final EncryptionLevel level, final boolean fipsCompatible, final int strength_bits, final int alg_bits, final String[] jsseAltNames, final String[] openSSlAltNames ) {
        this.id = id;
        this.openSSLAlias = openSSLAlias;
        if ( openSSlAltNames != null && openSSlAltNames.length != 0 ) {
            final Set<String> altNames = new HashSet<String>();
            altNames.addAll ( Arrays.asList ( openSSlAltNames ) );
            this.openSSLAltNames = Collections.unmodifiableSet ( ( Set<? extends String> ) altNames );
        } else {
            this.openSSLAltNames = Collections.emptySet();
        }
        final Set<String> jsseNames = new LinkedHashSet<String>();
        if ( jsseAltNames != null && jsseAltNames.length != 0 ) {
            jsseNames.addAll ( Arrays.asList ( jsseAltNames ) );
        }
        jsseNames.add ( this.name() );
        this.jsseNames = Collections.unmodifiableSet ( ( Set<? extends String> ) jsseNames );
        this.kx = kx;
        this.au = au;
        this.enc = enc;
        this.mac = mac;
        this.protocol = protocol;
        this.export = export;
        this.level = level;
        this.fipsCompatible = fipsCompatible;
        this.strength_bits = strength_bits;
        this.alg_bits = alg_bits;
    }
    public int getId() {
        return this.id;
    }
    public String getOpenSSLAlias() {
        return this.openSSLAlias;
    }
    public Set<String> getOpenSSLAltNames() {
        return this.openSSLAltNames;
    }
    public Set<String> getJsseNames() {
        return this.jsseNames;
    }
    public KeyExchange getKx() {
        return this.kx;
    }
    public Authentication getAu() {
        return this.au;
    }
    public Encryption getEnc() {
        return this.enc;
    }
    public MessageDigest getMac() {
        return this.mac;
    }
    public Protocol getProtocol() {
        return this.protocol;
    }
    public boolean isExport() {
        return this.export;
    }
    public EncryptionLevel getLevel() {
        return this.level;
    }
    public boolean isFipsCompatible() {
        return this.fipsCompatible;
    }
    public int getStrength_bits() {
        return this.strength_bits;
    }
    public int getAlg_bits() {
        return this.alg_bits;
    }
    public static Cipher valueOf ( final int cipherId ) {
        return Cipher.idMap.get ( cipherId );
    }
    static {
        idMap = new HashMap<Integer, Cipher>();
        for ( final Cipher cipher : values() ) {
            final int id = cipher.getId();
            if ( id > 0 && id < 65535 ) {
                Cipher.idMap.put ( id, cipher );
            }
        }
    }
}
