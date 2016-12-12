package javax.security.auth.message.config;
public interface RegistrationContext {
    String getMessageLayer();
    String getAppContext();
    String getDescription();
    boolean isPersistent();
}
