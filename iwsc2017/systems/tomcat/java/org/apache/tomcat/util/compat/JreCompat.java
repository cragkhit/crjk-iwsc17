package org.apache.tomcat.util.compat;
public class JreCompat {
    private static final JreCompat instance;
    private static final boolean jre9Available;
    static {
        if ( Jre9Compat.isSupported() ) {
            instance = new Jre9Compat();
            jre9Available = true;
        } else {
            instance = new JreCompat();
            jre9Available = false;
        }
    }
    public static JreCompat getInstance() {
        return instance;
    }
    public static boolean isJre9Available() {
        return jre9Available;
    }
    public boolean isInstanceOfInaccessibleObjectException ( Throwable t ) {
        return false;
    }
}
