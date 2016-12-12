package javax.servlet.jsp.tagext;
import java.util.Map;
public abstract class TagLibraryValidator {
    private Map<String, Object> initParameters;
    public void setInitParameters ( final Map<String, Object> map ) {
        this.initParameters = map;
    }
    public Map<String, Object> getInitParameters() {
        return this.initParameters;
    }
    public ValidationMessage[] validate ( final String prefix, final String uri, final PageData page ) {
        return null;
    }
    public void release() {
        this.initParameters = null;
    }
}
