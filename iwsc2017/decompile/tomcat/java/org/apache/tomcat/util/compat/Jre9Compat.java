package org.apache.tomcat.util.compat;
class Jre9Compat extends JreCompat {
    private static final Class<?> inaccessibleObjectExceptionClazz;
    static boolean isSupported() {
        return Jre9Compat.inaccessibleObjectExceptionClazz != null;
    }
    @Override
    public boolean isInstanceOfInaccessibleObjectException ( final Throwable t ) {
        return t != null && Jre9Compat.inaccessibleObjectExceptionClazz.isAssignableFrom ( t.getClass() );
    }
    static {
        Class<?> c1 = null;
        try {
            c1 = Class.forName ( "java.lang.reflect.InaccessibleObjectException" );
        } catch ( SecurityException ex ) {}
        catch ( ClassNotFoundException ex2 ) {}
        inaccessibleObjectExceptionClazz = c1;
    }
}
