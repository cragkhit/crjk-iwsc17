package javax.servlet;
import java.util.Set;
public interface ServletContainerInitializer {
    void onStartup ( Set<Class<?>> p0, ServletContext p1 ) throws ServletException;
}
