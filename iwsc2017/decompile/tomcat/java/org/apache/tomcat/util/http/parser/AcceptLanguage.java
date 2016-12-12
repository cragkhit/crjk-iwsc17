package org.apache.tomcat.util.http.parser;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.io.StringReader;
import java.util.Locale;
public class AcceptLanguage {
    private final Locale locale;
    private final double quality;
    protected AcceptLanguage ( final Locale locale, final double quality ) {
        this.locale = locale;
        this.quality = quality;
    }
    public Locale getLocale() {
        return this.locale;
    }
    public double getQuality() {
        return this.quality;
    }
    public static List<AcceptLanguage> parse ( final StringReader input ) throws IOException {
        final List<AcceptLanguage> result = new ArrayList<AcceptLanguage>();
        while ( true ) {
            final String languageTag = HttpParser.readToken ( input );
            if ( languageTag == null ) {
                HttpParser.skipUntil ( input, 0, ',' );
            } else {
                if ( languageTag.length() == 0 ) {
                    break;
                }
                double quality = 1.0;
                final SkipResult lookForSemiColon = HttpParser.skipConstant ( input, ";" );
                if ( lookForSemiColon == SkipResult.FOUND ) {
                    quality = HttpParser.readWeight ( input, ',' );
                }
                if ( quality <= 0.0 ) {
                    continue;
                }
                result.add ( new AcceptLanguage ( Locale.forLanguageTag ( languageTag ), quality ) );
            }
        }
        return result;
    }
}
