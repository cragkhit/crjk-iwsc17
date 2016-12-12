package org.apache.catalina.users;
import org.apache.catalina.UserDatabase;
import org.apache.catalina.Role;
public abstract class AbstractRole implements Role {
    protected String description;
    protected String rolename;
    public AbstractRole() {
        this.description = null;
        this.rolename = null;
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
    public String getRolename() {
        return this.rolename;
    }
    @Override
    public void setRolename ( final String rolename ) {
        this.rolename = rolename;
    }
    @Override
    public abstract UserDatabase getUserDatabase();
    @Override
    public String getName() {
        return this.getRolename();
    }
}
