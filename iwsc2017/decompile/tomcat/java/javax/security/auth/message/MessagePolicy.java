package javax.security.auth.message;
public class MessagePolicy {
    private final TargetPolicy[] targetPolicies;
    private final boolean mandatory;
    public MessagePolicy ( final TargetPolicy[] targetPolicies, final boolean mandatory ) {
        if ( targetPolicies == null ) {
            throw new IllegalArgumentException ( "targetPolicies is null" );
        }
        this.targetPolicies = targetPolicies;
        this.mandatory = mandatory;
    }
    public boolean isMandatory() {
        return this.mandatory;
    }
    public TargetPolicy[] getTargetPolicies() {
        if ( this.targetPolicies.length == 0 ) {
            return null;
        }
        return this.targetPolicies;
    }
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
    public interface Target {
        Object get ( MessageInfo p0 );
        void remove ( MessageInfo p0 );
        void put ( MessageInfo p0, Object p1 );
    }
    public interface ProtectionPolicy {
        public static final String AUTHENTICATE_SENDER = "#authenticateSender";
        public static final String AUTHENTICATE_CONTENT = "#authenticateContent";
        public static final String AUTHENTICATE_RECIPIENT = "#authenticateRecipient";
        String getID();
    }
}
