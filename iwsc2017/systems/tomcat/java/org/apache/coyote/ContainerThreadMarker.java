package org.apache.coyote;
public class ContainerThreadMarker {
    private static final ThreadLocal<Boolean> marker = new ThreadLocal<>();
    public static boolean isContainerThread() {
        Boolean flag = marker.get();
        if ( flag == null ) {
            return false;
        } else {
            return flag.booleanValue();
        }
    }
    public static void set() {
        marker.set ( Boolean.TRUE );
    }
    public static void clear() {
        marker.set ( Boolean.FALSE );
    }
}
