package org.apache.catalina.startup;
import org.apache.tomcat.util.bcel.classfile.JavaClass;
import javax.servlet.ServletContainerInitializer;
import java.util.Set;
static class JavaClassCacheEntry {
    public final String superclassName;
    public final String[] interfaceNames;
    private Set<ServletContainerInitializer> sciSet;
    public JavaClassCacheEntry ( final JavaClass javaClass ) {
        this.sciSet = null;
        this.superclassName = javaClass.getSuperclassName();
        this.interfaceNames = javaClass.getInterfaceNames();
    }
    public String getSuperclassName() {
        return this.superclassName;
    }
    public String[] getInterfaceNames() {
        return this.interfaceNames;
    }
    public Set<ServletContainerInitializer> getSciSet() {
        return this.sciSet;
    }
    public void setSciSet ( final Set<ServletContainerInitializer> sciSet ) {
        this.sciSet = sciSet;
    }
}
