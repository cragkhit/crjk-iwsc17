package javax.servlet;
import java.util.Collection;
import java.util.Set;
public interface ServletRegistration extends Registration {
    Set<String> addMapping ( String... p0 );
    Collection<String> getMappings();
    String getRunAsRole();
    public interface Dynamic extends ServletRegistration, Registration.Dynamic {
        void setLoadOnStartup ( int p0 );
        Set<String> setServletSecurity ( ServletSecurityElement p0 );
        void setMultipartConfig ( MultipartConfigElement p0 );
        void setRunAsRole ( String p0 );
    }
}
