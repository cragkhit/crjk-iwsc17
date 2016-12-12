package javax.servlet.http;
import java.io.Serializable;
import java.text.MessageFormat;
import java.util.BitSet;
import java.util.Locale;
import java.util.ResourceBundle;
public class Cookie implements Cloneable, Serializable {
    private static final CookieNameValidator validation;
    static {
        boolean strictNaming;
        String prop = System.getProperty ( "org.apache.tomcat.util.http.ServerCookie.STRICT_NAMING" );
        if ( prop != null ) {
            strictNaming = Boolean.parseBoolean ( prop );
        } else {
            strictNaming = Boolean.getBoolean ( "org.apache.catalina.STRICT_SERVLET_COMPLIANCE" );
        }
        if ( strictNaming ) {
            validation = new RFC2109Validator();
        } else {
            validation = new RFC6265Validator();
        }
    }
    private static final long serialVersionUID = 1L;
    private final String name;
    private String value;
    private int version = 0;
    private String comment;
    private String domain;
    private int maxAge = -1;
    private String path;
    private boolean secure;
    private boolean httpOnly;
    public Cookie ( String name, String value ) {
        validation.validate ( name );
        this.name = name;
        this.value = value;
    }
    public void setComment ( String purpose ) {
        comment = purpose;
    }
    public String getComment() {
        return comment;
    }
    public void setDomain ( String pattern ) {
        domain = pattern.toLowerCase ( Locale.ENGLISH );
    }
    public String getDomain() {
        return domain;
    }
    public void setMaxAge ( int expiry ) {
        maxAge = expiry;
    }
    public int getMaxAge() {
        return maxAge;
    }
    public void setPath ( String uri ) {
        path = uri;
    }
    public String getPath() {
        return path;
    }
    public void setSecure ( boolean flag ) {
        secure = flag;
    }
    public boolean getSecure() {
        return secure;
    }
    public String getName() {
        return name;
    }
    public void setValue ( String newValue ) {
        value = newValue;
    }
    public String getValue() {
        return value;
    }
    public int getVersion() {
        return version;
    }
    public void setVersion ( int v ) {
        version = v;
    }
    @Override
    public Object clone() {
        try {
            return super.clone();
        } catch ( CloneNotSupportedException e ) {
            throw new RuntimeException ( e );
        }
    }
    public void setHttpOnly ( boolean httpOnly ) {
        this.httpOnly = httpOnly;
    }
    public boolean isHttpOnly() {
        return httpOnly;
    }
}
class CookieNameValidator {
    private static final String LSTRING_FILE = "javax.servlet.http.LocalStrings";
    protected static final ResourceBundle lStrings = ResourceBundle.getBundle ( LSTRING_FILE );
    protected final BitSet allowed;
    protected CookieNameValidator ( String separators ) {
        allowed = new BitSet ( 128 );
        allowed.set ( 0x20, 0x7f );
        for ( int i = 0; i < separators.length(); i++ ) {
            char ch = separators.charAt ( i );
            allowed.clear ( ch );
        }
    }
    void validate ( String name ) {
        if ( name == null || name.length() == 0 ) {
            throw new IllegalArgumentException ( lStrings.getString ( "err.cookie_name_blank" ) );
        }
        if ( !isToken ( name ) ) {
            String errMsg = lStrings.getString ( "err.cookie_name_is_token" );
            throw new IllegalArgumentException ( MessageFormat.format ( errMsg, name ) );
        }
    }
    private boolean isToken ( String possibleToken ) {
        int len = possibleToken.length();
        for ( int i = 0; i < len; i++ ) {
            char c = possibleToken.charAt ( i );
            if ( !allowed.get ( c ) ) {
                return false;
            }
        }
        return true;
    }
}
class RFC6265Validator extends CookieNameValidator {
    private static final String RFC2616_SEPARATORS = "()<>@,;:\\\"/[]?={} \t";
    RFC6265Validator() {
        super ( RFC2616_SEPARATORS );
    }
}
class RFC2109Validator extends RFC6265Validator {
    RFC2109Validator() {
        boolean allowSlash;
        String prop = System.getProperty ( "org.apache.tomcat.util.http.ServerCookie.FWD_SLASH_IS_SEPARATOR" );
        if ( prop != null ) {
            allowSlash = !Boolean.parseBoolean ( prop );
        } else {
            allowSlash = !Boolean.getBoolean ( "org.apache.catalina.STRICT_SERVLET_COMPLIANCE" );
        }
        if ( allowSlash ) {
            allowed.set ( '/' );
        }
    }
    @Override
    void validate ( String name ) {
        super.validate ( name );
        if ( name.charAt ( 0 ) == '$' ) {
            String errMsg = lStrings.getString ( "err.cookie_name_is_token" );
            throw new IllegalArgumentException ( MessageFormat.format ( errMsg, name ) );
        }
    }
}
