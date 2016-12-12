package org.apache.tomcat.util.compat;
class Jre9Compat extends JreCompat {
    private static final Class<?> inaccessibleObjectExceptionClazz;
    static {
        Class<?> c1 = null;
        try {
            c1 = Class.forName ( "java.lang.reflect.InaccessibleObjectException" );
        } catch ( SecurityException e ) {
        } catch ( ClassNotFoundException e ) {
        }
        inaccessibleObjectExceptionClazz = c1;
    }
    static boolean isSupported() {
        return inaccessibleObjectExceptionClazz != null;
    }
    @Override
    public boolean isInstanceOfInaccessibleObjectException ( Throwable t ) {
        if ( t == null ) {
            return false;
        }
        return inaccessibleObjectExceptionClazz.isAssignableFrom ( t.getClass() );
    }
}
