package org.apache.tomcat.util.compat;
public class JreCompat {
    private static final JreCompat instance;
    private static final boolean jre9Available;
    public static JreCompat getInstance() {
        return JreCompat.instance;
    }
    public static boolean isJre9Available() {
        return JreCompat.jre9Available;
    }
    public boolean isInstanceOfInaccessibleObjectException ( final Throwable t ) {
        return false;
    }
    static {
        if ( Jre9Compat.isSupported() ) {
            instance = new Jre9Compat();
            jre9Available = true;
        } else {
            instance = new JreCompat();
            jre9Available = false;
        }
    }
}
