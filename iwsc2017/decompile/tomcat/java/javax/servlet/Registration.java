package javax.servlet;
import java.util.Set;
import java.util.Map;
public interface Registration {
    String getName();
    String getClassName();
    boolean setInitParameter ( String p0, String p1 );
    String getInitParameter ( String p0 );
    Set<String> setInitParameters ( Map<String, String> p0 );
    Map<String, String> getInitParameters();
    public interface Dynamic extends Registration {
        void setAsyncSupported ( boolean p0 );
    }
}
