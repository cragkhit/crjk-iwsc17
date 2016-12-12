package javax.servlet.http;
import java.text.MessageFormat;
class RFC2109Validator extends RFC6265Validator {
    RFC2109Validator() {
        final String prop = System.getProperty ( "org.apache.tomcat.util.http.ServerCookie.FWD_SLASH_IS_SEPARATOR" );
        boolean allowSlash;
        if ( prop != null ) {
            allowSlash = !Boolean.parseBoolean ( prop );
        } else {
            allowSlash = !Boolean.getBoolean ( "org.apache.catalina.STRICT_SERVLET_COMPLIANCE" );
        }
        if ( allowSlash ) {
            this.allowed.set ( 47 );
        }
    }
    @Override
    void validate ( final String name ) {
        super.validate ( name );
        if ( name.charAt ( 0 ) == '$' ) {
            final String errMsg = RFC2109Validator.lStrings.getString ( "err.cookie_name_is_token" );
            throw new IllegalArgumentException ( MessageFormat.format ( errMsg, name ) );
        }
    }
}
