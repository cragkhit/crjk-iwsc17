package javax.servlet;
import java.util.Map;
import java.util.Set;
public interface Registration {
    public String getName();
    public String getClassName();
    public boolean setInitParameter ( String name, String value );
    public String getInitParameter ( String name );
    public Set<String> setInitParameters ( Map<String, String> initParameters );
    public Map<String, String> getInitParameters();
    public interface Dynamic extends Registration {
        public void setAsyncSupported ( boolean isAsyncSupported );
    }
}
