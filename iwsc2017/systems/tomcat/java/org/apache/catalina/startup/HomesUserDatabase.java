package org.apache.catalina.startup;
import java.io.File;
import java.util.Enumeration;
import java.util.Hashtable;
public final class HomesUserDatabase
    implements UserDatabase {
    public HomesUserDatabase() {
        super();
    }
    private final Hashtable<String, String> homes = new Hashtable<>();
    private UserConfig userConfig = null;
    @Override
    public UserConfig getUserConfig() {
        return ( this.userConfig );
    }
    @Override
    public void setUserConfig ( UserConfig userConfig ) {
        this.userConfig = userConfig;
        init();
    }
    @Override
    public String getHome ( String user ) {
        return homes.get ( user );
    }
    @Override
    public Enumeration<String> getUsers() {
        return ( homes.keys() );
    }
    private void init() {
        String homeBase = userConfig.getHomeBase();
        File homeBaseDir = new File ( homeBase );
        if ( !homeBaseDir.exists() || !homeBaseDir.isDirectory() ) {
            return;
        }
        String homeBaseFiles[] = homeBaseDir.list();
        if ( homeBaseFiles == null ) {
            return;
        }
        for ( int i = 0; i < homeBaseFiles.length; i++ ) {
            File homeDir = new File ( homeBaseDir, homeBaseFiles[i] );
            if ( !homeDir.isDirectory() || !homeDir.canRead() ) {
                continue;
            }
            homes.put ( homeBaseFiles[i], homeDir.toString() );
        }
    }
}
