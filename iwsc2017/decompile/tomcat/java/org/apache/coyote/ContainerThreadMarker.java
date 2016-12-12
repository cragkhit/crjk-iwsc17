package org.apache.coyote;
public class ContainerThreadMarker {
    private static final ThreadLocal<Boolean> marker;
    public static boolean isContainerThread() {
        final Boolean flag = ContainerThreadMarker.marker.get();
        return flag != null && flag;
    }
    public static void set() {
        ContainerThreadMarker.marker.set ( Boolean.TRUE );
    }
    public static void clear() {
        ContainerThreadMarker.marker.set ( Boolean.FALSE );
    }
    static {
        marker = new ThreadLocal<Boolean>();
    }
}
