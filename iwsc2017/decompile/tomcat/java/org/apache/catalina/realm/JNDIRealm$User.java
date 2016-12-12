package org.apache.catalina.realm;
import java.util.Collections;
import java.util.List;
protected static class User {
    private final String username;
    private final String dn;
    private final String password;
    private final List<String> roles;
    private final String userRoleId;
    public User ( final String username, final String dn, final String password, final List<String> roles, final String userRoleId ) {
        this.username = username;
        this.dn = dn;
        this.password = password;
        if ( roles == null ) {
            this.roles = Collections.emptyList();
        } else {
            this.roles = Collections.unmodifiableList ( ( List<? extends String> ) roles );
        }
        this.userRoleId = userRoleId;
    }
    public String getUserName() {
        return this.username;
    }
    public String getDN() {
        return this.dn;
    }
    public String getPassword() {
        return this.password;
    }
    public List<String> getRoles() {
        return this.roles;
    }
    public String getUserRoleId() {
        return this.userRoleId;
    }
}
