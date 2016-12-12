package org.apache.catalina;
import java.util.Iterator;
public interface UserDatabase {
    Iterator<Group> getGroups();
    String getId();
    Iterator<Role> getRoles();
    Iterator<User> getUsers();
    void close() throws Exception;
    Group createGroup ( String p0, String p1 );
    Role createRole ( String p0, String p1 );
    User createUser ( String p0, String p1, String p2 );
    Group findGroup ( String p0 );
    Role findRole ( String p0 );
    User findUser ( String p0 );
    void open() throws Exception;
    void removeGroup ( Group p0 );
    void removeRole ( Role p0 );
    void removeUser ( User p0 );
    void save() throws Exception;
}
