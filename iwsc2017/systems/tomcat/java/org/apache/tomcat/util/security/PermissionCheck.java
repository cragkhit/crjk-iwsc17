package org.apache.tomcat.util.security;
import java.security.Permission;
public interface PermissionCheck {
    boolean check ( Permission permission );
}
