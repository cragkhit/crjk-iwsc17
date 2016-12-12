package org.apache.catalina.users;
import org.apache.catalina.User;
import org.apache.catalina.UserDatabase;
import org.apache.catalina.Role;
import java.util.Iterator;
import org.apache.catalina.Group;
public abstract class AbstractGroup implements Group {
    protected String description;
    protected String groupname;
    public AbstractGroup() {
        this.description = null;
        this.groupname = null;
    }
    @Override
    public String getDescription() {
        return this.description;
    }
    @Override
    public void setDescription ( final String description ) {
        this.description = description;
    }
    @Override
    public String getGroupname() {
        return this.groupname;
    }
    @Override
    public void setGroupname ( final String groupname ) {
        this.groupname = groupname;
    }
    @Override
    public abstract Iterator<Role> getRoles();
    @Override
    public abstract UserDatabase getUserDatabase();
    @Override
    public abstract Iterator<User> getUsers();
    @Override
    public abstract void addRole ( final Role p0 );
    @Override
    public abstract boolean isInRole ( final Role p0 );
    @Override
    public abstract void removeRole ( final Role p0 );
    @Override
    public abstract void removeRoles();
    @Override
    public String getName() {
        return this.getGroupname();
    }
}
