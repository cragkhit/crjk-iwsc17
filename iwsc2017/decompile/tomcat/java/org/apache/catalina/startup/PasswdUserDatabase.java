package org.apache.catalina.startup;
import org.apache.juli.logging.LogFactory;
import java.io.Reader;
import java.io.BufferedReader;
import java.io.FileReader;
import java.util.Enumeration;
import java.util.Hashtable;
import org.apache.naming.StringManager;
import org.apache.juli.logging.Log;
public final class PasswdUserDatabase implements UserDatabase {
    private static final Log log;
    private static final StringManager sm;
    private static final String PASSWORD_FILE = "/etc/passwd";
    private final Hashtable<String, String> homes;
    private UserConfig userConfig;
    public PasswdUserDatabase() {
        this.homes = new Hashtable<String, String>();
        this.userConfig = null;
    }
    @Override
    public UserConfig getUserConfig() {
        return this.userConfig;
    }
    @Override
    public void setUserConfig ( final UserConfig userConfig ) {
        this.userConfig = userConfig;
        this.init();
    }
    @Override
    public String getHome ( final String user ) {
        return this.homes.get ( user );
    }
    @Override
    public Enumeration<String> getUsers() {
        return this.homes.keys();
    }
    private void init() {
        try ( final BufferedReader reader = new BufferedReader ( new FileReader ( "/etc/passwd" ) ) ) {
            for ( String line = reader.readLine(); line != null; line = reader.readLine() ) {
                final String[] tokens = line.split ( ":" );
                if ( tokens.length > 5 && tokens[0].length() > 0 && tokens[5].length() > 0 ) {
                    this.homes.put ( tokens[0], tokens[5] );
                }
            }
        } catch ( Exception e ) {
            PasswdUserDatabase.log.warn ( PasswdUserDatabase.sm.getString ( "passwdUserDatabase.readFail" ), e );
        }
    }
    static {
        log = LogFactory.getLog ( PasswdUserDatabase.class );
        sm = StringManager.getManager ( PasswdUserDatabase.class );
    }
}
