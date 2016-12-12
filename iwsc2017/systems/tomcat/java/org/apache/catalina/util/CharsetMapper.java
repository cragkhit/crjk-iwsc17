package org.apache.catalina.util;
import java.io.InputStream;
import java.util.Locale;
import java.util.Properties;
import org.apache.tomcat.util.ExceptionUtils;
public class CharsetMapper {
    public static final String DEFAULT_RESOURCE =
        "/org/apache/catalina/util/CharsetMapperDefault.properties";
    public CharsetMapper() {
        this ( DEFAULT_RESOURCE );
    }
    public CharsetMapper ( String name ) {
        try ( InputStream stream = this.getClass().getResourceAsStream ( name ) ) {
            map.load ( stream );
        } catch ( Throwable t ) {
            ExceptionUtils.handleThrowable ( t );
            throw new IllegalArgumentException ( t.toString() );
        }
    }
    private Properties map = new Properties();
    public String getCharset ( Locale locale ) {
        String charset = map.getProperty ( locale.toString() );
        if ( charset == null ) {
            charset = map.getProperty ( locale.getLanguage() + "_"
                                        + locale.getCountry() );
            if ( charset == null ) {
                charset = map.getProperty ( locale.getLanguage() );
            }
        }
        return ( charset );
    }
    public void addCharsetMappingFromDeploymentDescriptor ( String locale, String charset ) {
        map.put ( locale, charset );
    }
}
