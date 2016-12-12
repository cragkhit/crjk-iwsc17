package javax.security.auth.message.callback;
import javax.crypto.SecretKey;
import javax.security.auth.callback.Callback;
public class SecretKeyCallback implements Callback {
    private final Request request;
    private SecretKey key;
    public SecretKeyCallback ( final Request request ) {
        this.request = request;
    }
    public Request getRequest() {
        return this.request;
    }
    public void setKey ( final SecretKey key ) {
        this.key = key;
    }
    public SecretKey getKey() {
        return this.key;
    }
    public static class AliasRequest implements Request {
        private final String alias;
        public AliasRequest ( final String alias ) {
            this.alias = alias;
        }
        public String getAlias() {
            return this.alias;
        }
    }
    public interface Request {
    }
}
