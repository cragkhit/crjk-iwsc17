package org.apache.catalina.users;
import org.apache.catalina.Role;
import org.apache.catalina.UserDatabase;
public abstract class AbstractRole implements Role {
    protected String description = null;
    protected String rolename = null;
    @Override
    public String getDescription() {
        return ( this.description );
    }
    @Override
    public void setDescription ( String description ) {
        this.description = description;
    }
    @Override
    public String getRolename() {
        return ( this.rolename );
    }
    @Override
    public void setRolename ( String rolename ) {
        this.rolename = rolename;
    }
    @Override
    public abstract UserDatabase getUserDatabase();
    @Override
    public String getName() {
        return ( getRolename() );
    }
}
