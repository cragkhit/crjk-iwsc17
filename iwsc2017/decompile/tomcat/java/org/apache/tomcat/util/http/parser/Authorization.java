package org.apache.tomcat.util.http.parser;
import java.io.IOException;
import java.util.Locale;
import java.util.HashMap;
import java.io.StringReader;
import java.util.Map;
import org.apache.tomcat.util.res.StringManager;
public class Authorization {
    private static final StringManager sm;
    private static final Integer FIELD_TYPE_TOKEN;
    private static final Integer FIELD_TYPE_QUOTED_STRING;
    private static final Integer FIELD_TYPE_TOKEN_OR_QUOTED_STRING;
    private static final Integer FIELD_TYPE_LHEX;
    private static final Integer FIELD_TYPE_QUOTED_TOKEN;
    private static final Map<String, Integer> fieldTypes;
    public static Map<String, String> parseAuthorizationDigest ( final StringReader input ) throws IllegalArgumentException, IOException {
        final Map<String, String> result = new HashMap<String, String>();
        if ( HttpParser.skipConstant ( input, "Digest" ) != SkipResult.FOUND ) {
            return null;
        }
        String field = HttpParser.readToken ( input );
        if ( field == null ) {
            return null;
        }
        while ( !field.equals ( "" ) ) {
            if ( HttpParser.skipConstant ( input, "=" ) != SkipResult.FOUND ) {
                return null;
            }
            Integer type = Authorization.fieldTypes.get ( field.toLowerCase ( Locale.ENGLISH ) );
            if ( type == null ) {
                type = Authorization.FIELD_TYPE_TOKEN_OR_QUOTED_STRING;
            }
            String value = null;
            switch ( type ) {
            case 0: {
                value = HttpParser.readToken ( input );
                break;
            }
            case 1: {
                value = HttpParser.readQuotedString ( input, false );
                break;
            }
            case 2: {
                value = HttpParser.readTokenOrQuotedString ( input, false );
                break;
            }
            case 3: {
                value = HttpParser.readLhex ( input );
                break;
            }
            case 4: {
                value = HttpParser.readQuotedToken ( input );
                break;
            }
            default: {
                throw new IllegalArgumentException ( Authorization.sm.getString ( "authorization.unknownType", type ) );
            }
            }
            if ( value == null ) {
                return null;
            }
            result.put ( field, value );
            if ( HttpParser.skipConstant ( input, "," ) == SkipResult.NOT_FOUND ) {
                return null;
            }
            field = HttpParser.readToken ( input );
            if ( field == null ) {
                return null;
            }
        }
        return result;
    }
    static {
        sm = StringManager.getManager ( Authorization.class );
        FIELD_TYPE_TOKEN = 0;
        FIELD_TYPE_QUOTED_STRING = 1;
        FIELD_TYPE_TOKEN_OR_QUOTED_STRING = 2;
        FIELD_TYPE_LHEX = 3;
        FIELD_TYPE_QUOTED_TOKEN = 4;
        ( fieldTypes = new HashMap<String, Integer>() ).put ( "username", Authorization.FIELD_TYPE_QUOTED_STRING );
        Authorization.fieldTypes.put ( "realm", Authorization.FIELD_TYPE_QUOTED_STRING );
        Authorization.fieldTypes.put ( "nonce", Authorization.FIELD_TYPE_QUOTED_STRING );
        Authorization.fieldTypes.put ( "digest-uri", Authorization.FIELD_TYPE_QUOTED_STRING );
        Authorization.fieldTypes.put ( "response", Authorization.FIELD_TYPE_LHEX );
        Authorization.fieldTypes.put ( "algorithm", Authorization.FIELD_TYPE_QUOTED_TOKEN );
        Authorization.fieldTypes.put ( "cnonce", Authorization.FIELD_TYPE_QUOTED_STRING );
        Authorization.fieldTypes.put ( "opaque", Authorization.FIELD_TYPE_QUOTED_STRING );
        Authorization.fieldTypes.put ( "qop", Authorization.FIELD_TYPE_QUOTED_TOKEN );
        Authorization.fieldTypes.put ( "nc", Authorization.FIELD_TYPE_LHEX );
    }
}
