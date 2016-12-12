package org.apache.catalina.util;
import java.util.Hashtable;
import java.util.Locale;
import java.io.InputStream;
import org.apache.tomcat.util.ExceptionUtils;
import java.util.Properties;
public class CharsetMapper {
    public static final String DEFAULT_RESOURCE = "/org/apache/catalina/util/CharsetMapperDefault.properties";
    private Properties map;
    public CharsetMapper() {
        this ( "/org/apache/catalina/util/CharsetMapperDefault.properties" );
    }
    public CharsetMapper ( final String name ) {
        this.map = new Properties();
        try ( final InputStream stream = this.getClass().getResourceAsStream ( name ) ) {
            this.map.load ( stream );
        } catch ( Throwable t ) {
            ExceptionUtils.handleThrowable ( t );
            throw new IllegalArgumentException ( t.toString() );
        }
    }
    public String getCharset ( final Locale locale ) {
        String charset = this.map.getProperty ( locale.toString() );
        if ( charset == null ) {
            charset = this.map.getProperty ( locale.getLanguage() + "_" + locale.getCountry() );
            if ( charset == null ) {
                charset = this.map.getProperty ( locale.getLanguage() );
            }
        }
        return charset;
    }
    public void addCharsetMappingFromDeploymentDescriptor ( final String locale, final String charset ) {
        ( ( Hashtable<String, String> ) this.map ).put ( locale, charset );
    }
}
