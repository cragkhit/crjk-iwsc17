package org.apache.catalina.authenticator;
import org.ietf.jgss.GSSException;
import org.ietf.jgss.GSSContext;
import java.security.PrivilegedExceptionAction;
public static class AcceptAction implements PrivilegedExceptionAction<byte[]> {
    GSSContext gssContext;
    byte[] decoded;
    public AcceptAction ( final GSSContext context, final byte[] decodedToken ) {
        this.gssContext = context;
        this.decoded = decodedToken;
    }
    @Override
    public byte[] run() throws GSSException {
        return this.gssContext.acceptSecContext ( this.decoded, 0, this.decoded.length );
    }
}
