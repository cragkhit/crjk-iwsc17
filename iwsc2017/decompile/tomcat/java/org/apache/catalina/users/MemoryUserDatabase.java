package org.apache.catalina.users;
import org.apache.juli.logging.LogFactory;
import java.io.Writer;
import java.io.PrintWriter;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.FileOutputStream;
import java.io.File;
import java.io.InputStream;
import java.io.IOException;
import org.apache.tomcat.util.digester.ObjectCreationFactory;
import org.apache.tomcat.util.digester.Digester;
import org.apache.tomcat.util.file.ConfigFileLoader;
import java.util.Iterator;
import org.apache.catalina.User;
import org.apache.tomcat.util.res.StringManager;
import org.apache.catalina.Role;
import org.apache.catalina.Group;
import java.util.HashMap;
import org.apache.juli.logging.Log;
import org.apache.catalina.UserDatabase;
public class MemoryUserDatabase implements UserDatabase {
    private static final Log log;
    protected final HashMap<String, Group> groups;
    protected final String id;
    protected String pathname;
    protected String pathnameOld;
    protected String pathnameNew;
    protected boolean readonly;
    protected final HashMap<String, Role> roles;
    private static final StringManager sm;
    protected final HashMap<String, User> users;
    public MemoryUserDatabase() {
        this ( null );
    }
    public MemoryUserDatabase ( final String id ) {
        this.groups = new HashMap<String, Group>();
        this.pathname = "conf/tomcat-users.xml";
        this.pathnameOld = this.pathname + ".old";
        this.pathnameNew = this.pathname + ".new";
        this.readonly = true;
        this.roles = new HashMap<String, Role>();
        this.users = new HashMap<String, User>();
        this.id = id;
    }
    @Override
    public Iterator<Group> getGroups() {
        synchronized ( this.groups ) {
            return this.groups.values().iterator();
        }
    }
    @Override
    public String getId() {
        return this.id;
    }
    public String getPathname() {
        return this.pathname;
    }
    public void setPathname ( final String pathname ) {
        this.pathname = pathname;
        this.pathnameOld = pathname + ".old";
        this.pathnameNew = pathname + ".new";
    }
    public boolean getReadonly() {
        return this.readonly;
    }
    public void setReadonly ( final boolean readonly ) {
        this.readonly = readonly;
    }
    @Override
    public Iterator<Role> getRoles() {
        synchronized ( this.roles ) {
            return this.roles.values().iterator();
        }
    }
    @Override
    public Iterator<User> getUsers() {
        synchronized ( this.users ) {
            return this.users.values().iterator();
        }
    }
    @Override
    public void close() throws Exception {
        this.save();
        synchronized ( this.groups ) {
            synchronized ( this.users ) {
                this.users.clear();
                this.groups.clear();
            }
        }
    }
    @Override
    public Group createGroup ( final String groupname, final String description ) {
        if ( groupname == null || groupname.length() == 0 ) {
            final String msg = MemoryUserDatabase.sm.getString ( "memoryUserDatabase.nullGroup" );
            MemoryUserDatabase.log.warn ( msg );
            throw new IllegalArgumentException ( msg );
        }
        final MemoryGroup group = new MemoryGroup ( this, groupname, description );
        synchronized ( this.groups ) {
            this.groups.put ( group.getGroupname(), group );
        }
        return group;
    }
    @Override
    public Role createRole ( final String rolename, final String description ) {
        if ( rolename == null || rolename.length() == 0 ) {
            final String msg = MemoryUserDatabase.sm.getString ( "memoryUserDatabase.nullRole" );
            MemoryUserDatabase.log.warn ( msg );
            throw new IllegalArgumentException ( msg );
        }
        final MemoryRole role = new MemoryRole ( this, rolename, description );
        synchronized ( this.roles ) {
            this.roles.put ( role.getRolename(), role );
        }
        return role;
    }
    @Override
    public User createUser ( final String username, final String password, final String fullName ) {
        if ( username == null || username.length() == 0 ) {
            final String msg = MemoryUserDatabase.sm.getString ( "memoryUserDatabase.nullUser" );
            MemoryUserDatabase.log.warn ( msg );
            throw new IllegalArgumentException ( msg );
        }
        final MemoryUser user = new MemoryUser ( this, username, password, fullName );
        synchronized ( this.users ) {
            this.users.put ( user.getUsername(), user );
        }
        return user;
    }
    @Override
    public Group findGroup ( final String groupname ) {
        synchronized ( this.groups ) {
            return this.groups.get ( groupname );
        }
    }
    @Override
    public Role findRole ( final String rolename ) {
        synchronized ( this.roles ) {
            return this.roles.get ( rolename );
        }
    }
    @Override
    public User findUser ( final String username ) {
        synchronized ( this.users ) {
            return this.users.get ( username );
        }
    }
    @Override
    public void open() throws Exception {
        synchronized ( this.groups ) {
            synchronized ( this.users ) {
                this.users.clear();
                this.groups.clear();
                this.roles.clear();
                final String pathName = this.getPathname();
                try ( final InputStream is = ConfigFileLoader.getInputStream ( this.getPathname() ) ) {
                    final Digester digester = new Digester();
                    try {
                        digester.setFeature ( "http://apache.org/xml/features/allow-java-encodings", true );
                    } catch ( Exception e ) {
                        MemoryUserDatabase.log.warn ( MemoryUserDatabase.sm.getString ( "memoryUserDatabase.xmlFeatureEncoding" ), e );
                    }
                    digester.addFactoryCreate ( "tomcat-users/group", new MemoryGroupCreationFactory ( this ), true );
                    digester.addFactoryCreate ( "tomcat-users/role", new MemoryRoleCreationFactory ( this ), true );
                    digester.addFactoryCreate ( "tomcat-users/user", new MemoryUserCreationFactory ( this ), true );
                    digester.parse ( is );
                } catch ( IOException ioe ) {
                    MemoryUserDatabase.log.error ( MemoryUserDatabase.sm.getString ( "memoryUserDatabase.fileNotFound", pathName ) );
                }
            }
        }
    }
    @Override
    public void removeGroup ( final Group group ) {
        synchronized ( this.groups ) {
            final Iterator<User> users = this.getUsers();
            while ( users.hasNext() ) {
                final User user = users.next();
                user.removeGroup ( group );
            }
            this.groups.remove ( group.getGroupname() );
        }
    }
    @Override
    public void removeRole ( final Role role ) {
        synchronized ( this.roles ) {
            final Iterator<Group> groups = this.getGroups();
            while ( groups.hasNext() ) {
                final Group group = groups.next();
                group.removeRole ( role );
            }
            final Iterator<User> users = this.getUsers();
            while ( users.hasNext() ) {
                final User user = users.next();
                user.removeRole ( role );
            }
            this.roles.remove ( role.getRolename() );
        }
    }
    @Override
    public void removeUser ( final User user ) {
        synchronized ( this.users ) {
            this.users.remove ( user.getUsername() );
        }
    }
    public boolean isWriteable() {
        File file = new File ( this.pathname );
        if ( !file.isAbsolute() ) {
            file = new File ( System.getProperty ( "catalina.base" ), this.pathname );
        }
        final File dir = file.getParentFile();
        return dir.exists() && dir.isDirectory() && dir.canWrite();
    }
    @Override
    public void save() throws Exception {
        if ( this.getReadonly() ) {
            MemoryUserDatabase.log.error ( MemoryUserDatabase.sm.getString ( "memoryUserDatabase.readOnly" ) );
            return;
        }
        if ( !this.isWriteable() ) {
            MemoryUserDatabase.log.warn ( MemoryUserDatabase.sm.getString ( "memoryUserDatabase.notPersistable" ) );
            return;
        }
        File fileNew = new File ( this.pathnameNew );
        if ( !fileNew.isAbsolute() ) {
            fileNew = new File ( System.getProperty ( "catalina.base" ), this.pathnameNew );
        }
        PrintWriter writer = null;
        try {
            final FileOutputStream fos = new FileOutputStream ( fileNew );
            final OutputStreamWriter osw = new OutputStreamWriter ( fos, "UTF8" );
            writer = new PrintWriter ( osw );
            writer.println ( "<?xml version='1.0' encoding='utf-8'?>" );
            writer.println ( "<tomcat-users xmlns=\"http://tomcat.apache.org/xml\"" );
            writer.println ( "              xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"" );
            writer.println ( "              xsi:schemaLocation=\"http://tomcat.apache.org/xml tomcat-users.xsd\"" );
            writer.println ( "              version=\"1.0\">" );
            Iterator<?> values = null;
            values = this.getRoles();
            while ( values.hasNext() ) {
                writer.print ( "  " );
                writer.println ( values.next() );
            }
            values = this.getGroups();
            while ( values.hasNext() ) {
                writer.print ( "  " );
                writer.println ( values.next() );
            }
            values = this.getUsers();
            while ( values.hasNext() ) {
                writer.print ( "  " );
                writer.println ( ( ( MemoryUser ) values.next() ).toXml() );
            }
            writer.println ( "</tomcat-users>" );
            if ( writer.checkError() ) {
                writer.close();
                fileNew.delete();
                throw new IOException ( MemoryUserDatabase.sm.getString ( "memoryUserDatabase.writeException", fileNew.getAbsolutePath() ) );
            }
            writer.close();
        } catch ( IOException e ) {
            if ( writer != null ) {
                writer.close();
            }
            fileNew.delete();
            throw e;
        }
        File fileOld = new File ( this.pathnameOld );
        if ( !fileOld.isAbsolute() ) {
            fileOld = new File ( System.getProperty ( "catalina.base" ), this.pathnameOld );
        }
        fileOld.delete();
        File fileOrig = new File ( this.pathname );
        if ( !fileOrig.isAbsolute() ) {
            fileOrig = new File ( System.getProperty ( "catalina.base" ), this.pathname );
        }
        if ( fileOrig.exists() ) {
            fileOld.delete();
            if ( !fileOrig.renameTo ( fileOld ) ) {
                throw new IOException ( MemoryUserDatabase.sm.getString ( "memoryUserDatabase.renameOld", fileOld.getAbsolutePath() ) );
            }
        }
        if ( !fileNew.renameTo ( fileOrig ) ) {
            if ( fileOld.exists() ) {
                fileOld.renameTo ( fileOrig );
            }
            throw new IOException ( MemoryUserDatabase.sm.getString ( "memoryUserDatabase.renameNew", fileOrig.getAbsolutePath() ) );
        }
        fileOld.delete();
    }
    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder ( "MemoryUserDatabase[id=" );
        sb.append ( this.id );
        sb.append ( ",pathname=" );
        sb.append ( this.pathname );
        sb.append ( ",groupCount=" );
        sb.append ( this.groups.size() );
        sb.append ( ",roleCount=" );
        sb.append ( this.roles.size() );
        sb.append ( ",userCount=" );
        sb.append ( this.users.size() );
        sb.append ( "]" );
        return sb.toString();
    }
    static {
        log = LogFactory.getLog ( MemoryUserDatabase.class );
        sm = StringManager.getManager ( "org.apache.catalina.users" );
    }
}
