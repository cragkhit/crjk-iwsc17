package org.apache.jasper.runtime;
import java.util.Map;
public interface JspSourceDependent {
    Map<String, Long> getDependants();
}
