package javax.servlet;
import java.util.Collection;
import java.util.EnumSet;
public interface FilterRegistration extends Registration {
    void addMappingForServletNames ( EnumSet<DispatcherType> p0, boolean p1, String... p2 );
    Collection<String> getServletNameMappings();
    void addMappingForUrlPatterns ( EnumSet<DispatcherType> p0, boolean p1, String... p2 );
    Collection<String> getUrlPatternMappings();
    public interface Dynamic extends FilterRegistration, Registration.Dynamic {
    }
}
