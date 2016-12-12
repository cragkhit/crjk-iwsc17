package org.apache.jasper.runtime;
import java.util.Map;
public interface JspSourceDependent {
    public Map<String, Long> getDependants();
}
