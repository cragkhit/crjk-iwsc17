package org.apache.tomcat.util.digester;
import java.security.Permission;
import java.util.PropertyPermission;
import org.apache.tomcat.util.security.PermissionCheck;
import org.apache.tomcat.util.IntrospectionUtils;
private class SystemPropertySource implements IntrospectionUtils.PropertySource {
    @Override
    public String getProperty ( final String key ) {
        final ClassLoader cl = Digester.this.getClassLoader();
        if ( cl instanceof PermissionCheck ) {
            final Permission p = new PropertyPermission ( key, "read" );
            if ( ! ( ( PermissionCheck ) cl ).check ( p ) ) {
                return null;
            }
        }
        return System.getProperty ( key );
    }
}
