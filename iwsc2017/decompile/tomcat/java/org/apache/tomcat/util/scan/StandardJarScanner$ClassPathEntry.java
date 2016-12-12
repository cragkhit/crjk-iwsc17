package org.apache.tomcat.util.scan;
import java.net.URL;
private static class ClassPathEntry {
    private final boolean jar;
    private final String name;
    public ClassPathEntry ( final URL url ) {
        String path = url.getPath();
        final int end = path.lastIndexOf ( ".jar" );
        if ( end != -1 ) {
            this.jar = true;
            final int start = path.lastIndexOf ( 47, end );
            this.name = path.substring ( start + 1, end + 4 );
        } else {
            this.jar = false;
            if ( path.endsWith ( "/" ) ) {
                path = path.substring ( 0, path.length() - 1 );
            }
            final int start = path.lastIndexOf ( 47 );
            this.name = path.substring ( start + 1 );
        }
    }
    public boolean isJar() {
        return this.jar;
    }
    public String getName() {
        return this.name;
    }
}
