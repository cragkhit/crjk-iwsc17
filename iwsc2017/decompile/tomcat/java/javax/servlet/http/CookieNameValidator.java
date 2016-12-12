package javax.servlet.http;
import java.text.MessageFormat;
import java.util.BitSet;
import java.util.ResourceBundle;
class CookieNameValidator {
    private static final String LSTRING_FILE = "javax.servlet.http.LocalStrings";
    protected static final ResourceBundle lStrings;
    protected final BitSet allowed;
    protected CookieNameValidator ( final String separators ) {
        ( this.allowed = new BitSet ( 128 ) ).set ( 32, 127 );
        for ( int i = 0; i < separators.length(); ++i ) {
            final char ch = separators.charAt ( i );
            this.allowed.clear ( ch );
        }
    }
    void validate ( final String name ) {
        if ( name == null || name.length() == 0 ) {
            throw new IllegalArgumentException ( CookieNameValidator.lStrings.getString ( "err.cookie_name_blank" ) );
        }
        if ( !this.isToken ( name ) ) {
            final String errMsg = CookieNameValidator.lStrings.getString ( "err.cookie_name_is_token" );
            throw new IllegalArgumentException ( MessageFormat.format ( errMsg, name ) );
        }
    }
    private boolean isToken ( final String possibleToken ) {
        for ( int len = possibleToken.length(), i = 0; i < len; ++i ) {
            final char c = possibleToken.charAt ( i );
            if ( !this.allowed.get ( c ) ) {
                return false;
            }
        }
        return true;
    }
    static {
        lStrings = ResourceBundle.getBundle ( "javax.servlet.http.LocalStrings" );
    }
}
