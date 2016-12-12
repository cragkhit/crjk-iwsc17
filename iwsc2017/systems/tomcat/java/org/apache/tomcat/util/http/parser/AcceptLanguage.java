package org.apache.tomcat.util.http.parser;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
public class AcceptLanguage {
    private final Locale locale;
    private final double quality;
    protected AcceptLanguage ( Locale locale, double quality ) {
        this.locale = locale;
        this.quality = quality;
    }
    public Locale getLocale() {
        return locale;
    }
    public double getQuality() {
        return quality;
    }
    public static List<AcceptLanguage> parse ( StringReader input ) throws IOException {
        List<AcceptLanguage> result = new ArrayList<>();
        do {
            String languageTag = HttpParser.readToken ( input );
            if ( languageTag == null ) {
                HttpParser.skipUntil ( input, 0, ',' );
                continue;
            }
            if ( languageTag.length() == 0 ) {
                break;
            }
            double quality = 1;
            SkipResult lookForSemiColon = HttpParser.skipConstant ( input, ";" );
            if ( lookForSemiColon == SkipResult.FOUND ) {
                quality = HttpParser.readWeight ( input, ',' );
            }
            if ( quality > 0 ) {
                result.add ( new AcceptLanguage ( Locale.forLanguageTag ( languageTag ), quality ) );
            }
        } while ( true );
        return result;
    }
}
