package javax.servlet;
import java.util.Collection;
import java.util.EnumSet;
public interface FilterRegistration extends Registration {
    public void addMappingForServletNames (
        EnumSet<DispatcherType> dispatcherTypes,
        boolean isMatchAfter, String... servletNames );
    public Collection<String> getServletNameMappings();
    public void addMappingForUrlPatterns (
        EnumSet<DispatcherType> dispatcherTypes,
        boolean isMatchAfter, String... urlPatterns );
    public Collection<String> getUrlPatternMappings();
    public static interface Dynamic
        extends FilterRegistration, Registration.Dynamic {
    }
}
