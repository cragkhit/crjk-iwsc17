package javax.security.auth.message;
public static class TargetPolicy {
    private final Target[] targets;
    private final ProtectionPolicy protectionPolicy;
    public TargetPolicy ( final Target[] targets, final ProtectionPolicy protectionPolicy ) {
        if ( protectionPolicy == null ) {
            throw new IllegalArgumentException ( "protectionPolicy is null" );
        }
        this.targets = targets;
        this.protectionPolicy = protectionPolicy;
    }
    public Target[] getTargets() {
        if ( this.targets == null || this.targets.length == 0 ) {
            return null;
        }
        return this.targets;
    }
    public ProtectionPolicy getProtectionPolicy() {
        return this.protectionPolicy;
    }
}
