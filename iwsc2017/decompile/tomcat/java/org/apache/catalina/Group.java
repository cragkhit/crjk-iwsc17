package org.apache.catalina;
import java.util.Iterator;
import java.security.Principal;
public interface Group extends Principal {
    String getDescription();
    void setDescription ( String p0 );
    String getGroupname();
    void setGroupname ( String p0 );
    Iterator<Role> getRoles();
    UserDatabase getUserDatabase();
    Iterator<User> getUsers();
    void addRole ( Role p0 );
    boolean isInRole ( Role p0 );
    void removeRole ( Role p0 );
    void removeRoles();
}
