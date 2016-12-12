package org.apache.tomcat.util.net.openssl.ciphers;
import org.apache.tomcat.util.net.Constants;
enum Protocol {
    SSLv3 ( Constants.SSL_PROTO_SSLv3 ),
    SSLv2 ( Constants.SSL_PROTO_SSLv2 ),
    TLSv1 ( Constants.SSL_PROTO_TLSv1 ),
    TLSv1_2 ( Constants.SSL_PROTO_TLSv1_2 );
    private final String openSSLName;
    private Protocol ( String openSSLName ) {
        this.openSSLName = openSSLName;
    }
    String getOpenSSLName() {
        return openSSLName;
    }
}
