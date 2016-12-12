package org.apache.catalina.startup;
import java.io.BufferedReader;
import java.io.FileReader;
import java.util.Enumeration;
import java.util.Hashtable;
import org.apache.juli.logging.Log;
import org.apache.juli.logging.LogFactory;
import org.apache.naming.StringManager;
public final class PasswdUserDatabase implements UserDatabase {
    private static final Log log = LogFactory.getLog ( PasswdUserDatabase.class );
    private static final StringManager sm = StringManager.getManager ( PasswdUserDatabase.class );
    private static final String PASSWORD_FILE = "/etc/passwd";
    private final Hashtable<String, String> homes = new Hashtable<>();
    private UserConfig userConfig = null;
    @Override
    public UserConfig getUserConfig() {
        return userConfig;
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
        return homes.keys();
    }
    private void init() {
        try ( BufferedReader reader = new BufferedReader ( new FileReader ( PASSWORD_FILE ) ) ) {
            String line = reader.readLine();
            while ( line != null ) {
                String tokens[] = line.split ( ":" );
                if ( tokens.length > 5 && tokens[0].length() > 0 && tokens[5].length() > 0 ) {
                    homes.put ( tokens[0], tokens[5] );
                }
                line = reader.readLine();
            }
        } catch ( Exception e ) {
            log.warn ( sm.getString ( "passwdUserDatabase.readFail" ), e );
        }
    }
}
