package org.apache.catalina;
import java.security.Principal;
import java.util.Iterator;
public interface Group extends Principal {
    public String getDescription();
    public void setDescription ( String description );
    public String getGroupname();
    public void setGroupname ( String groupname );
    public Iterator<Role> getRoles();
    public UserDatabase getUserDatabase();
    public Iterator<User> getUsers();
    public void addRole ( Role role );
    public boolean isInRole ( Role role );
    public void removeRole ( Role role );
    public void removeRoles();
}
