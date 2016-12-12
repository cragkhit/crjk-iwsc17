package org.apache.catalina.startup;
import java.util.Enumeration;
public interface UserDatabase {
    public UserConfig getUserConfig();
    public void setUserConfig ( UserConfig userConfig );
    public String getHome ( String user );
    public Enumeration<String> getUsers();
}
