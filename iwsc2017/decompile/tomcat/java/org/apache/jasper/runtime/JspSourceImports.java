package org.apache.jasper.runtime;
import java.util.Set;
public interface JspSourceImports {
    Set<String> getPackageImports();
    Set<String> getClassImports();
}
