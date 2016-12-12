package org.apache.tomcat.util.bcel.classfile;
public class JavaClass {
    private final int access_flags;
    private final String class_name;
    private final String superclass_name;
    private final String[] interface_names;
    private final Annotations runtimeVisibleAnnotations;
    JavaClass ( final String class_name, final String superclass_name,
                final int access_flags, final ConstantPool constant_pool, final String[] interface_names,
                final Annotations runtimeVisibleAnnotations ) {
        this.access_flags = access_flags;
        this.runtimeVisibleAnnotations = runtimeVisibleAnnotations;
        this.class_name = class_name;
        this.superclass_name = superclass_name;
        this.interface_names = interface_names;
    }
    public final int getAccessFlags() {
        return access_flags;
    }
    public AnnotationEntry[] getAnnotationEntries() {
        if ( runtimeVisibleAnnotations != null ) {
            return runtimeVisibleAnnotations.getAnnotationEntries();
        }
        return null;
    }
    public String getClassName() {
        return class_name;
    }
    public String[] getInterfaceNames() {
        return interface_names;
    }
    public String getSuperclassName() {
        return superclass_name;
    }
}
