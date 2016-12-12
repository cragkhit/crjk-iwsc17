package org.apache.catalina.authenticator;
import java.nio.charset.StandardCharsets;
import org.apache.tomcat.util.codec.binary.Base64;
import org.apache.tomcat.util.buf.ByteChunk;
public static class BasicCredentials {
    private static final String METHOD = "basic ";
    private ByteChunk authorization;
    private int initialOffset;
    private int base64blobOffset;
    private int base64blobLength;
    private String username;
    private String password;
    public BasicCredentials ( final ByteChunk input ) throws IllegalArgumentException {
        this.username = null;
        this.password = null;
        this.authorization = input;
        this.initialOffset = input.getOffset();
        this.parseMethod();
        final byte[] decoded = this.parseBase64();
        this.parseCredentials ( decoded );
    }
    public String getUsername() {
        return this.username;
    }
    public String getPassword() {
        return this.password;
    }
    private void parseMethod() throws IllegalArgumentException {
        if ( this.authorization.startsWithIgnoreCase ( "basic ", 0 ) ) {
            this.base64blobOffset = this.initialOffset + "basic ".length();
            this.base64blobLength = this.authorization.getLength() - "basic ".length();
            return;
        }
        throw new IllegalArgumentException ( "Authorization header method is not \"Basic\"" );
    }
    private byte[] parseBase64() throws IllegalArgumentException {
        final byte[] decoded = Base64.decodeBase64 ( this.authorization.getBuffer(), this.base64blobOffset, this.base64blobLength );
        this.authorization.setOffset ( this.initialOffset );
        if ( decoded == null ) {
            throw new IllegalArgumentException ( "Basic Authorization credentials are not Base64" );
        }
        return decoded;
    }
    private void parseCredentials ( final byte[] decoded ) throws IllegalArgumentException {
        int colon = -1;
        for ( int i = 0; i < decoded.length; ++i ) {
            if ( decoded[i] == 58 ) {
                colon = i;
                break;
            }
        }
        if ( colon < 0 ) {
            this.username = new String ( decoded, StandardCharsets.ISO_8859_1 );
        } else {
            this.username = new String ( decoded, 0, colon, StandardCharsets.ISO_8859_1 );
            this.password = new String ( decoded, colon + 1, decoded.length - colon - 1, StandardCharsets.ISO_8859_1 );
            if ( this.password.length() > 1 ) {
                this.password = this.password.trim();
            }
        }
        if ( this.username.length() > 1 ) {
            this.username = this.username.trim();
        }
    }
}
