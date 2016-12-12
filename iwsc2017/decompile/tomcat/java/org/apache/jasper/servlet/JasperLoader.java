package org.apache.jasper.servlet;
import java.security.CodeSource;
import java.io.IOException;
import java.io.InputStream;
import org.apache.jasper.Constants;
import java.net.URL;
import java.security.PermissionCollection;
import java.net.URLClassLoader;
public class JasperLoader extends URLClassLoader {
    private final PermissionCollection permissionCollection;
    private final SecurityManager securityManager;
    public JasperLoader ( final URL[] urls, final ClassLoader parent, final PermissionCollection permissionCollection ) {
        super ( urls, parent );
        this.permissionCollection = permissionCollection;
        this.securityManager = System.getSecurityManager();
    }
    @Override
    public Class<?> loadClass ( final String name ) throws ClassNotFoundException {
        return this.loadClass ( name, false );
    }
    public synchronized Class<?> loadClass ( final String name, final boolean resolve ) throws ClassNotFoundException {
        Class<?> clazz = null;
        clazz = this.findLoadedClass ( name );
        if ( clazz != null ) {
            if ( resolve ) {
                this.resolveClass ( clazz );
            }
            return clazz;
        }
        if ( this.securityManager != null ) {
            final int dot = name.lastIndexOf ( 46 );
            if ( dot >= 0 ) {
                try {
                    if ( !"org.apache.jasper.runtime".equalsIgnoreCase ( name.substring ( 0, dot ) ) ) {
                        this.securityManager.checkPackageAccess ( name.substring ( 0, dot ) );
                    }
                } catch ( SecurityException se ) {
                    final String error = "Security Violation, attempt to use Restricted Class: " + name;
                    se.printStackTrace();
                    throw new ClassNotFoundException ( error );
                }
            }
        }
        if ( !name.startsWith ( Constants.JSP_PACKAGE_NAME + '.' ) ) {
            clazz = this.getParent().loadClass ( name );
            if ( resolve ) {
                this.resolveClass ( clazz );
            }
            return clazz;
        }
        return this.findClass ( name );
    }
    @Override
    public InputStream getResourceAsStream ( final String name ) {
        InputStream is = this.getParent().getResourceAsStream ( name );
        if ( is == null ) {
            final URL url = this.findResource ( name );
            if ( url != null ) {
                try {
                    is = url.openStream();
                } catch ( IOException ex ) {}
            }
        }
        return is;
    }
    public final PermissionCollection getPermissions ( final CodeSource codeSource ) {
        return this.permissionCollection;
    }
}
