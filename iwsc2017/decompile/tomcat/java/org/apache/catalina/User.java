package org.apache.catalina;
import java.util.Iterator;
import java.security.Principal;
public interface User extends Principal {
    String getFullName();
    void setFullName ( String p0 );
    Iterator<Group> getGroups();
    String getPassword();
    void setPassword ( String p0 );
    Iterator<Role> getRoles();
    UserDatabase getUserDatabase();
    String getUsername();
    void setUsername ( String p0 );
    void addGroup ( Group p0 );
    void addRole ( Role p0 );
    boolean isInGroup ( Group p0 );
    boolean isInRole ( Role p0 );
    void removeGroup ( Group p0 );
    void removeGroups();
    void removeRole ( Role p0 );
    void removeRoles();
}
