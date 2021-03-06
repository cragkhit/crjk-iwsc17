package org.apache.catalina;
import java.util.Iterator;
public interface UserDatabase {
    public Iterator<Group> getGroups();
    public String getId();
    public Iterator<Role> getRoles();
    public Iterator<User> getUsers();
    public void close() throws Exception;
    public Group createGroup ( String groupname, String description );
    public Role createRole ( String rolename, String description );
    public User createUser ( String username, String password,
                             String fullName );
    public Group findGroup ( String groupname );
    public Role findRole ( String rolename );
    public User findUser ( String username );
    public void open() throws Exception;
    public void removeGroup ( Group group );
    public void removeRole ( Role role );
    public void removeUser ( User user );
    public void save() throws Exception;
}
