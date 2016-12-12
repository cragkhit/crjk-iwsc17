package org.apache.tomcat.util.descriptor.web;
import java.io.Serializable;
public class InjectionTarget implements Serializable {
    private static final long serialVersionUID = 1L;
    private String targetClass;
    private String targetName;
    public InjectionTarget() {
    }
    public InjectionTarget ( final String targetClass, final String targetName ) {
        this.targetClass = targetClass;
        this.targetName = targetName;
    }
    public String getTargetClass() {
        return this.targetClass;
    }
    public void setTargetClass ( final String targetClass ) {
        this.targetClass = targetClass;
    }
    public String getTargetName() {
        return this.targetName;
    }
    public void setTargetName ( final String targetName ) {
        this.targetName = targetName;
    }
}
