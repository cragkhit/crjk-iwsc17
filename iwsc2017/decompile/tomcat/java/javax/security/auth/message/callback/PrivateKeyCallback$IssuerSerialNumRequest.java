package javax.security.auth.message.callback;
import java.math.BigInteger;
import javax.security.auth.x500.X500Principal;
public static class IssuerSerialNumRequest implements Request {
    private final X500Principal issuer;
    private final BigInteger serialNum;
    public IssuerSerialNumRequest ( final X500Principal issuer, final BigInteger serialNum ) {
        this.issuer = issuer;
        this.serialNum = serialNum;
    }
    public X500Principal getIssuer() {
        return this.issuer;
    }
    public BigInteger getSerialNum() {
        return this.serialNum;
    }
}
