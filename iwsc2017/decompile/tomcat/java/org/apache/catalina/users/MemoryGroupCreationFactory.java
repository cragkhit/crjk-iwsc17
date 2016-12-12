package org.apache.catalina.users;
import org.apache.catalina.Role;
import org.apache.catalina.Group;
import org.xml.sax.Attributes;
import org.apache.tomcat.util.digester.AbstractObjectCreationFactory;
class MemoryGroupCreationFactory extends AbstractObjectCreationFactory {
    private final MemoryUserDatabase database;
    public MemoryGroupCreationFactory ( final MemoryUserDatabase database ) {
        this.database = database;
    }
    @Override
    public Object createObject ( final Attributes attributes ) {
        String groupname = attributes.getValue ( "groupname" );
        if ( groupname == null ) {
            groupname = attributes.getValue ( "name" );
        }
        final String description = attributes.getValue ( "description" );
        String roles = attributes.getValue ( "roles" );
        final Group group = this.database.createGroup ( groupname, description );
        if ( roles != null ) {
            while ( roles.length() > 0 ) {
                String rolename = null;
                final int comma = roles.indexOf ( 44 );
                if ( comma >= 0 ) {
                    rolename = roles.substring ( 0, comma ).trim();
                    roles = roles.substring ( comma + 1 );
                } else {
                    rolename = roles.trim();
                    roles = "";
                }
                if ( rolename.length() > 0 ) {
                    Role role = this.database.findRole ( rolename );
                    if ( role == null ) {
                        role = this.database.createRole ( rolename, null );
                    }
                    group.addRole ( role );
                }
            }
        }
        return group;
    }
}
