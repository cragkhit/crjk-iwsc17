package org.apache.tomcat.util.net;
public enum CertificateVerification {
    NONE,
    OPTIONAL_NO_CA,
    OPTIONAL,
    REQUIRED;
    public static CertificateVerification fromString ( final String value ) {
        if ( "true".equalsIgnoreCase ( value ) || "yes".equalsIgnoreCase ( value ) || "require".equalsIgnoreCase ( value ) || "required".equalsIgnoreCase ( value ) ) {
            return CertificateVerification.REQUIRED;
        }
        if ( "optional".equalsIgnoreCase ( value ) || "want".equalsIgnoreCase ( value ) ) {
            return CertificateVerification.OPTIONAL;
        }
        if ( "optionalNoCA".equalsIgnoreCase ( value ) || "optional_no_ca".equalsIgnoreCase ( value ) ) {
            return CertificateVerification.OPTIONAL_NO_CA;
        }
        if ( "false".equalsIgnoreCase ( value ) || "no".equalsIgnoreCase ( value ) || "none".equalsIgnoreCase ( value ) ) {
            return CertificateVerification.NONE;
        }
        throw new IllegalArgumentException ( SSLHostConfig.access$000().getString ( "sslHostConfig.certificateVerificationInvalid", value ) );
    }
}
