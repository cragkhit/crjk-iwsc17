package org.apache.jasper.servlet;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLClassLoader;
import java.security.CodeSource;
import java.security.PermissionCollection;
import org.apache.jasper.Constants;
public class JasperLoader extends URLClassLoader {
    private final PermissionCollection permissionCollection;
    private final SecurityManager securityManager;
    public JasperLoader ( URL[] urls, ClassLoader parent,
                          PermissionCollection permissionCollection ) {
        super ( urls, parent );
        this.permissionCollection = permissionCollection;
        this.securityManager = System.getSecurityManager();
    }
    @Override
    public Class<?> loadClass ( String name ) throws ClassNotFoundException {
        return ( loadClass ( name, false ) );
    }
    @Override
    public synchronized Class<?> loadClass ( final String name, boolean resolve )
    throws ClassNotFoundException {
        Class<?> clazz = null;
        clazz = findLoadedClass ( name );
        if ( clazz != null ) {
            if ( resolve ) {
                resolveClass ( clazz );
            }
            return ( clazz );
        }
        if ( securityManager != null ) {
            int dot = name.lastIndexOf ( '.' );
            if ( dot >= 0 ) {
                try {
                    if ( !"org.apache.jasper.runtime".equalsIgnoreCase ( name.substring ( 0, dot ) ) ) {
                        securityManager.checkPackageAccess ( name.substring ( 0, dot ) );
                    }
                } catch ( SecurityException se ) {
                    String error = "Security Violation, attempt to use " +
                                   "Restricted Class: " + name;
                    se.printStackTrace();
                    throw new ClassNotFoundException ( error );
                }
            }
        }
        if ( !name.startsWith ( Constants.JSP_PACKAGE_NAME + '.' ) ) {
            clazz = getParent().loadClass ( name );
            if ( resolve ) {
                resolveClass ( clazz );
            }
            return clazz;
        }
        return findClass ( name );
    }
    @Override
    public InputStream getResourceAsStream ( String name ) {
        InputStream is = getParent().getResourceAsStream ( name );
        if ( is == null ) {
            URL url = findResource ( name );
            if ( url != null ) {
                try {
                    is = url.openStream();
                } catch ( IOException e ) {
                }
            }
        }
        return is;
    }
    @Override
    public final PermissionCollection getPermissions ( CodeSource codeSource ) {
        return permissionCollection;
    }
}
