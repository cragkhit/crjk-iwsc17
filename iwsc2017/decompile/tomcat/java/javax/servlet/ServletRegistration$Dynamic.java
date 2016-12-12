package javax.servlet;
import java.util.Set;
public interface Dynamic extends ServletRegistration, Registration.Dynamic {
    void setLoadOnStartup ( int p0 );
    Set<String> setServletSecurity ( ServletSecurityElement p0 );
    void setMultipartConfig ( MultipartConfigElement p0 );
    void setRunAsRole ( String p0 );
}
