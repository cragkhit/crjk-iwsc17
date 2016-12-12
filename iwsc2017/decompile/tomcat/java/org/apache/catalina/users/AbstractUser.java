package org.apache.catalina.users;
import org.apache.catalina.Role;
import org.apache.catalina.Group;
import java.util.Iterator;
import org.apache.catalina.User;
public abstract class AbstractUser implements User {
    protected String fullName;
    protected String password;
    protected String username;
    public AbstractUser() {
        this.fullName = null;
        this.password = null;
        this.username = null;
    }
    @Override
    public String getFullName() {
        return this.fullName;
    }
    @Override
    public void setFullName ( final String fullName ) {
        this.fullName = fullName;
    }
    @Override
    public abstract Iterator<Group> getGroups();
    @Override
    public String getPassword() {
        return this.password;
    }
    @Override
    public void setPassword ( final String password ) {
        this.password = password;
    }
    @Override
    public abstract Iterator<Role> getRoles();
    @Override
    public String getUsername() {
        return this.username;
    }
    @Override
    public void setUsername ( final String username ) {
        this.username = username;
    }
    @Override
    public abstract void addGroup ( final Group p0 );
    @Override
    public abstract void addRole ( final Role p0 );
    @Override
    public abstract boolean isInGroup ( final Group p0 );
    @Override
    public abstract boolean isInRole ( final Role p0 );
    @Override
    public abstract void removeGroup ( final Group p0 );
    @Override
    public abstract void removeGroups();
    @Override
    public abstract void removeRole ( final Role p0 );
    @Override
    public abstract void removeRoles();
    @Override
    public String getName() {
        return this.getUsername();
    }
}
