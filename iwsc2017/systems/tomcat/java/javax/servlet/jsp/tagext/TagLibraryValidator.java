package javax.servlet.jsp.tagext;
import java.util.Map;
public abstract class TagLibraryValidator {
    public TagLibraryValidator() {
    }
    public void setInitParameters ( Map<String, Object> map ) {
        initParameters = map;
    }
    public Map<String, Object> getInitParameters() {
        return initParameters;
    }
    public ValidationMessage[] validate ( String prefix, String uri,
                                          PageData page ) {
        return null;
    }
    public void release() {
        initParameters = null;
    }
    private Map<String, Object> initParameters;
}
