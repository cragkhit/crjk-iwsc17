package javax.security.auth.message.callback;
public static class AliasRequest implements Request {
    private final String alias;
    public AliasRequest ( final String alias ) {
        this.alias = alias;
    }
    public String getAlias() {
        return this.alias;
    }
}
