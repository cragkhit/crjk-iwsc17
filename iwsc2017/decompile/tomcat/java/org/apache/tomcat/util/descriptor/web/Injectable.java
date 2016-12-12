package org.apache.tomcat.util.descriptor.web;
import java.util.List;
public interface Injectable {
    String getName();
    void addInjectionTarget ( String p0, String p1 );
    List<InjectionTarget> getInjectionTargets();
}
