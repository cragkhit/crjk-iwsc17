package org.apache.catalina.startup;
import java.util.Enumeration;
public interface UserDatabase {
    UserConfig getUserConfig();
    void setUserConfig ( UserConfig p0 );
    String getHome ( String p0 );
    Enumeration<String> getUsers();
}
