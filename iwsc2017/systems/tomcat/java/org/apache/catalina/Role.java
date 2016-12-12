package org.apache.catalina;
import java.security.Principal;
public interface Role extends Principal {
    public String getDescription();
    public void setDescription ( String description );
    public String getRolename();
    public void setRolename ( String rolename );
    public UserDatabase getUserDatabase();
}
