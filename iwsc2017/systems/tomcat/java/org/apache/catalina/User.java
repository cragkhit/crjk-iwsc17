package org.apache.catalina;
import java.security.Principal;
import java.util.Iterator;
public interface User extends Principal {
    public String getFullName();
    public void setFullName ( String fullName );
    public Iterator<Group> getGroups();
    public String getPassword();
    public void setPassword ( String password );
    public Iterator<Role> getRoles();
    public UserDatabase getUserDatabase();
    public String getUsername();
    public void setUsername ( String username );
    public void addGroup ( Group group );
    public void addRole ( Role role );
    public boolean isInGroup ( Group group );
    public boolean isInRole ( Role role );
    public void removeGroup ( Group group );
    public void removeGroups();
    public void removeRole ( Role role );
    public void removeRoles();
}
