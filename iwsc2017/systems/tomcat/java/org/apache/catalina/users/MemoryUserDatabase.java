package org.apache.catalina.users;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Iterator;
import org.apache.catalina.Globals;
import org.apache.catalina.Group;
import org.apache.catalina.Role;
import org.apache.catalina.User;
import org.apache.catalina.UserDatabase;
import org.apache.juli.logging.Log;
import org.apache.juli.logging.LogFactory;
import org.apache.tomcat.util.digester.AbstractObjectCreationFactory;
import org.apache.tomcat.util.digester.Digester;
import org.apache.tomcat.util.file.ConfigFileLoader;
import org.apache.tomcat.util.res.StringManager;
import org.xml.sax.Attributes;
public class MemoryUserDatabase implements UserDatabase {
    private static final Log log = LogFactory.getLog ( MemoryUserDatabase.class );
    public MemoryUserDatabase() {
        this ( null );
    }
    public MemoryUserDatabase ( String id ) {
        this.id = id;
    }
    protected final HashMap<String, Group> groups = new HashMap<>();
    protected final String id;
    protected String pathname = "conf/tomcat-users.xml";
    protected String pathnameOld = pathname + ".old";
    protected String pathnameNew = pathname + ".new";
    protected boolean readonly = true;
    protected final HashMap<String, Role> roles = new HashMap<>();
    private static final StringManager sm =
        StringManager.getManager ( Constants.Package );
    protected final HashMap<String, User> users = new HashMap<>();
    @Override
    public Iterator<Group> getGroups() {
        synchronized ( groups ) {
            return ( groups.values().iterator() );
        }
    }
    @Override
    public String getId() {
        return ( this.id );
    }
    public String getPathname() {
        return ( this.pathname );
    }
    public void setPathname ( String pathname ) {
        this.pathname = pathname;
        this.pathnameOld = pathname + ".old";
        this.pathnameNew = pathname + ".new";
    }
    public boolean getReadonly() {
        return ( this.readonly );
    }
    public void setReadonly ( boolean readonly ) {
        this.readonly = readonly;
    }
    @Override
    public Iterator<Role> getRoles() {
        synchronized ( roles ) {
            return ( roles.values().iterator() );
        }
    }
    @Override
    public Iterator<User> getUsers() {
        synchronized ( users ) {
            return ( users.values().iterator() );
        }
    }
    @Override
    public void close() throws Exception {
        save();
        synchronized ( groups ) {
            synchronized ( users ) {
                users.clear();
                groups.clear();
            }
        }
    }
    @Override
    public Group createGroup ( String groupname, String description ) {
        if ( groupname == null || groupname.length() == 0 ) {
            String msg = sm.getString ( "memoryUserDatabase.nullGroup" );
            log.warn ( msg );
            throw new IllegalArgumentException ( msg );
        }
        MemoryGroup group = new MemoryGroup ( this, groupname, description );
        synchronized ( groups ) {
            groups.put ( group.getGroupname(), group );
        }
        return ( group );
    }
    @Override
    public Role createRole ( String rolename, String description ) {
        if ( rolename == null || rolename.length() == 0 ) {
            String msg = sm.getString ( "memoryUserDatabase.nullRole" );
            log.warn ( msg );
            throw new IllegalArgumentException ( msg );
        }
        MemoryRole role = new MemoryRole ( this, rolename, description );
        synchronized ( roles ) {
            roles.put ( role.getRolename(), role );
        }
        return ( role );
    }
    @Override
    public User createUser ( String username, String password,
                             String fullName ) {
        if ( username == null || username.length() == 0 ) {
            String msg = sm.getString ( "memoryUserDatabase.nullUser" );
            log.warn ( msg );
            throw new IllegalArgumentException ( msg );
        }
        MemoryUser user = new MemoryUser ( this, username, password, fullName );
        synchronized ( users ) {
            users.put ( user.getUsername(), user );
        }
        return ( user );
    }
    @Override
    public Group findGroup ( String groupname ) {
        synchronized ( groups ) {
            return groups.get ( groupname );
        }
    }
    @Override
    public Role findRole ( String rolename ) {
        synchronized ( roles ) {
            return roles.get ( rolename );
        }
    }
    @Override
    public User findUser ( String username ) {
        synchronized ( users ) {
            return users.get ( username );
        }
    }
    @Override
    public void open() throws Exception {
        synchronized ( groups ) {
            synchronized ( users ) {
                users.clear();
                groups.clear();
                roles.clear();
                String pathName = getPathname();
                try ( InputStream is = ConfigFileLoader.getInputStream ( getPathname() ) ) {
                    Digester digester = new Digester();
                    try {
                        digester.setFeature (
                            "http://apache.org/xml/features/allow-java-encodings", true );
                    } catch ( Exception e ) {
                        log.warn ( sm.getString ( "memoryUserDatabase.xmlFeatureEncoding" ), e );
                    }
                    digester.addFactoryCreate ( "tomcat-users/group",
                                                new MemoryGroupCreationFactory ( this ), true );
                    digester.addFactoryCreate ( "tomcat-users/role",
                                                new MemoryRoleCreationFactory ( this ), true );
                    digester.addFactoryCreate ( "tomcat-users/user",
                                                new MemoryUserCreationFactory ( this ), true );
                    digester.parse ( is );
                } catch ( IOException ioe ) {
                    log.error ( sm.getString ( "memoryUserDatabase.fileNotFound", pathName ) );
                    return;
                }
            }
        }
    }
    @Override
    public void removeGroup ( Group group ) {
        synchronized ( groups ) {
            Iterator<User> users = getUsers();
            while ( users.hasNext() ) {
                User user = users.next();
                user.removeGroup ( group );
            }
            groups.remove ( group.getGroupname() );
        }
    }
    @Override
    public void removeRole ( Role role ) {
        synchronized ( roles ) {
            Iterator<Group> groups = getGroups();
            while ( groups.hasNext() ) {
                Group group = groups.next();
                group.removeRole ( role );
            }
            Iterator<User> users = getUsers();
            while ( users.hasNext() ) {
                User user = users.next();
                user.removeRole ( role );
            }
            roles.remove ( role.getRolename() );
        }
    }
    @Override
    public void removeUser ( User user ) {
        synchronized ( users ) {
            users.remove ( user.getUsername() );
        }
    }
    public boolean isWriteable() {
        File file = new File ( pathname );
        if ( !file.isAbsolute() ) {
            file = new File ( System.getProperty ( Globals.CATALINA_BASE_PROP ),
                              pathname );
        }
        File dir = file.getParentFile();
        return dir.exists() && dir.isDirectory() && dir.canWrite();
    }
    @Override
    public void save() throws Exception {
        if ( getReadonly() ) {
            log.error ( sm.getString ( "memoryUserDatabase.readOnly" ) );
            return;
        }
        if ( !isWriteable() ) {
            log.warn ( sm.getString ( "memoryUserDatabase.notPersistable" ) );
            return;
        }
        File fileNew = new File ( pathnameNew );
        if ( !fileNew.isAbsolute() ) {
            fileNew =
                new File ( System.getProperty ( Globals.CATALINA_BASE_PROP ), pathnameNew );
        }
        PrintWriter writer = null;
        try {
            FileOutputStream fos = new FileOutputStream ( fileNew );
            OutputStreamWriter osw = new OutputStreamWriter ( fos, "UTF8" );
            writer = new PrintWriter ( osw );
            writer.println ( "<?xml version='1.0' encoding='utf-8'?>" );
            writer.println ( "<tomcat-users xmlns=\"http://tomcat.apache.org/xml\"" );
            writer.println ( "              xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"" );
            writer.println ( "              xsi:schemaLocation=\"http://tomcat.apache.org/xml tomcat-users.xsd\"" );
            writer.println ( "              version=\"1.0\">" );
            Iterator<?> values = null;
            values = getRoles();
            while ( values.hasNext() ) {
                writer.print ( "  " );
                writer.println ( values.next() );
            }
            values = getGroups();
            while ( values.hasNext() ) {
                writer.print ( "  " );
                writer.println ( values.next() );
            }
            values = getUsers();
            while ( values.hasNext() ) {
                writer.print ( "  " );
                writer.println ( ( ( MemoryUser ) values.next() ).toXml() );
            }
            writer.println ( "</tomcat-users>" );
            if ( writer.checkError() ) {
                writer.close();
                fileNew.delete();
                throw new IOException
                ( sm.getString ( "memoryUserDatabase.writeException",
                                 fileNew.getAbsolutePath() ) );
            }
            writer.close();
        } catch ( IOException e ) {
            if ( writer != null ) {
                writer.close();
            }
            fileNew.delete();
            throw e;
        }
        File fileOld = new File ( pathnameOld );
        if ( !fileOld.isAbsolute() ) {
            fileOld =
                new File ( System.getProperty ( Globals.CATALINA_BASE_PROP ), pathnameOld );
        }
        fileOld.delete();
        File fileOrig = new File ( pathname );
        if ( !fileOrig.isAbsolute() ) {
            fileOrig =
                new File ( System.getProperty ( Globals.CATALINA_BASE_PROP ), pathname );
        }
        if ( fileOrig.exists() ) {
            fileOld.delete();
            if ( !fileOrig.renameTo ( fileOld ) ) {
                throw new IOException
                ( sm.getString ( "memoryUserDatabase.renameOld",
                                 fileOld.getAbsolutePath() ) );
            }
        }
        if ( !fileNew.renameTo ( fileOrig ) ) {
            if ( fileOld.exists() ) {
                fileOld.renameTo ( fileOrig );
            }
            throw new IOException
            ( sm.getString ( "memoryUserDatabase.renameNew",
                             fileOrig.getAbsolutePath() ) );
        }
        fileOld.delete();
    }
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder ( "MemoryUserDatabase[id=" );
        sb.append ( this.id );
        sb.append ( ",pathname=" );
        sb.append ( pathname );
        sb.append ( ",groupCount=" );
        sb.append ( this.groups.size() );
        sb.append ( ",roleCount=" );
        sb.append ( this.roles.size() );
        sb.append ( ",userCount=" );
        sb.append ( this.users.size() );
        sb.append ( "]" );
        return ( sb.toString() );
    }
}
class MemoryGroupCreationFactory extends AbstractObjectCreationFactory {
    public MemoryGroupCreationFactory ( MemoryUserDatabase database ) {
        this.database = database;
    }
    @Override
    public Object createObject ( Attributes attributes ) {
        String groupname = attributes.getValue ( "groupname" );
        if ( groupname == null ) {
            groupname = attributes.getValue ( "name" );
        }
        String description = attributes.getValue ( "description" );
        String roles = attributes.getValue ( "roles" );
        Group group = database.createGroup ( groupname, description );
        if ( roles != null ) {
            while ( roles.length() > 0 ) {
                String rolename = null;
                int comma = roles.indexOf ( ',' );
                if ( comma >= 0 ) {
                    rolename = roles.substring ( 0, comma ).trim();
                    roles = roles.substring ( comma + 1 );
                } else {
                    rolename = roles.trim();
                    roles = "";
                }
                if ( rolename.length() > 0 ) {
                    Role role = database.findRole ( rolename );
                    if ( role == null ) {
                        role = database.createRole ( rolename, null );
                    }
                    group.addRole ( role );
                }
            }
        }
        return ( group );
    }
    private final MemoryUserDatabase database;
}
class MemoryRoleCreationFactory extends AbstractObjectCreationFactory {
    public MemoryRoleCreationFactory ( MemoryUserDatabase database ) {
        this.database = database;
    }
    @Override
    public Object createObject ( Attributes attributes ) {
        String rolename = attributes.getValue ( "rolename" );
        if ( rolename == null ) {
            rolename = attributes.getValue ( "name" );
        }
        String description = attributes.getValue ( "description" );
        Role role = database.createRole ( rolename, description );
        return ( role );
    }
    private final MemoryUserDatabase database;
}
class MemoryUserCreationFactory extends AbstractObjectCreationFactory {
    public MemoryUserCreationFactory ( MemoryUserDatabase database ) {
        this.database = database;
    }
    @Override
    public Object createObject ( Attributes attributes ) {
        String username = attributes.getValue ( "username" );
        if ( username == null ) {
            username = attributes.getValue ( "name" );
        }
        String password = attributes.getValue ( "password" );
        String fullName = attributes.getValue ( "fullName" );
        if ( fullName == null ) {
            fullName = attributes.getValue ( "fullname" );
        }
        String groups = attributes.getValue ( "groups" );
        String roles = attributes.getValue ( "roles" );
        User user = database.createUser ( username, password, fullName );
        if ( groups != null ) {
            while ( groups.length() > 0 ) {
                String groupname = null;
                int comma = groups.indexOf ( ',' );
                if ( comma >= 0 ) {
                    groupname = groups.substring ( 0, comma ).trim();
                    groups = groups.substring ( comma + 1 );
                } else {
                    groupname = groups.trim();
                    groups = "";
                }
                if ( groupname.length() > 0 ) {
                    Group group = database.findGroup ( groupname );
                    if ( group == null ) {
                        group = database.createGroup ( groupname, null );
                    }
                    user.addGroup ( group );
                }
            }
        }
        if ( roles != null ) {
            while ( roles.length() > 0 ) {
                String rolename = null;
                int comma = roles.indexOf ( ',' );
                if ( comma >= 0 ) {
                    rolename = roles.substring ( 0, comma ).trim();
                    roles = roles.substring ( comma + 1 );
                } else {
                    rolename = roles.trim();
                    roles = "";
                }
                if ( rolename.length() > 0 ) {
                    Role role = database.findRole ( rolename );
                    if ( role == null ) {
                        role = database.createRole ( rolename, null );
                    }
                    user.addRole ( role );
                }
            }
        }
        return ( user );
    }
    private final MemoryUserDatabase database;
}
