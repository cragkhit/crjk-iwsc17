package javax.security.auth.message.callback;
import java.util.Arrays;
import javax.security.auth.Subject;
import javax.security.auth.callback.Callback;
public class PasswordValidationCallback implements Callback {
    private final Subject subject;
    private final String username;
    private char[] password;
    private boolean result;
    public PasswordValidationCallback ( final Subject subject, final String username, final char[] password ) {
        this.subject = subject;
        this.username = username;
        this.password = password;
    }
    public Subject getSubject() {
        return this.subject;
    }
    public String getUsername() {
        return this.username;
    }
    public char[] getPassword() {
        return this.password;
    }
    public void clearPassword() {
        Arrays.fill ( this.password, '\0' );
        this.password = new char[0];
    }
    public void setResult ( final boolean result ) {
        this.result = result;
    }
    public boolean getResult() {
        return this.result;
    }
}
