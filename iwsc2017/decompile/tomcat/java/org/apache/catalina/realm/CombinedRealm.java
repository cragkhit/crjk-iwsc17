package org.apache.catalina.realm;
import org.apache.juli.logging.LogFactory;
import org.ietf.jgss.GSSName;
import org.ietf.jgss.GSSException;
import org.ietf.jgss.GSSContext;
import java.security.cert.X509Certificate;
import org.apache.catalina.LifecycleException;
import org.apache.catalina.Lifecycle;
import org.apache.catalina.Container;
import java.security.Principal;
import java.util.Iterator;
import javax.management.ObjectName;
import java.util.LinkedList;
import org.apache.catalina.Realm;
import java.util.List;
import org.apache.juli.logging.Log;
public class CombinedRealm extends RealmBase {
    private static final Log log;
    protected final List<Realm> realms;
    protected static final String name = "CombinedRealm";
    public CombinedRealm() {
        this.realms = new LinkedList<Realm>();
    }
    public void addRealm ( final Realm theRealm ) {
        this.realms.add ( theRealm );
        if ( CombinedRealm.log.isDebugEnabled() ) {
            CombinedRealm.sm.getString ( "combinedRealm.addRealm", theRealm.getClass().getName(), Integer.toString ( this.realms.size() ) );
        }
    }
    public ObjectName[] getRealms() {
        final ObjectName[] result = new ObjectName[this.realms.size()];
        for ( final Realm realm : this.realms ) {
            if ( realm instanceof RealmBase ) {
                result[this.realms.indexOf ( realm )] = ( ( RealmBase ) realm ).getObjectName();
            }
        }
        return result;
    }
    public Realm[] getNestedRealms() {
        return this.realms.toArray ( new Realm[0] );
    }
    @Override
    public Principal authenticate ( final String username, final String clientDigest, final String nonce, final String nc, final String cnonce, final String qop, final String realmName, final String md5a2 ) {
        Principal authenticatedUser = null;
        for ( final Realm realm : this.realms ) {
            if ( CombinedRealm.log.isDebugEnabled() ) {
                CombinedRealm.log.debug ( CombinedRealm.sm.getString ( "combinedRealm.authStart", username, realm.getClass().getName() ) );
            }
            authenticatedUser = realm.authenticate ( username, clientDigest, nonce, nc, cnonce, qop, realmName, md5a2 );
            if ( authenticatedUser == null ) {
                if ( !CombinedRealm.log.isDebugEnabled() ) {
                    continue;
                }
                CombinedRealm.log.debug ( CombinedRealm.sm.getString ( "combinedRealm.authFail", username, realm.getClass().getName() ) );
            } else {
                if ( CombinedRealm.log.isDebugEnabled() ) {
                    CombinedRealm.log.debug ( CombinedRealm.sm.getString ( "combinedRealm.authSuccess", username, realm.getClass().getName() ) );
                    break;
                }
                break;
            }
        }
        return authenticatedUser;
    }
    @Override
    public Principal authenticate ( final String username ) {
        Principal authenticatedUser = null;
        for ( final Realm realm : this.realms ) {
            if ( CombinedRealm.log.isDebugEnabled() ) {
                CombinedRealm.log.debug ( CombinedRealm.sm.getString ( "combinedRealm.authStart", username, realm.getClass().getName() ) );
            }
            authenticatedUser = realm.authenticate ( username );
            if ( authenticatedUser == null ) {
                if ( !CombinedRealm.log.isDebugEnabled() ) {
                    continue;
                }
                CombinedRealm.log.debug ( CombinedRealm.sm.getString ( "combinedRealm.authFail", username, realm.getClass().getName() ) );
            } else {
                if ( CombinedRealm.log.isDebugEnabled() ) {
                    CombinedRealm.log.debug ( CombinedRealm.sm.getString ( "combinedRealm.authSuccess", username, realm.getClass().getName() ) );
                    break;
                }
                break;
            }
        }
        return authenticatedUser;
    }
    @Override
    public Principal authenticate ( final String username, final String credentials ) {
        Principal authenticatedUser = null;
        for ( final Realm realm : this.realms ) {
            if ( CombinedRealm.log.isDebugEnabled() ) {
                CombinedRealm.log.debug ( CombinedRealm.sm.getString ( "combinedRealm.authStart", username, realm.getClass().getName() ) );
            }
            authenticatedUser = realm.authenticate ( username, credentials );
            if ( authenticatedUser == null ) {
                if ( !CombinedRealm.log.isDebugEnabled() ) {
                    continue;
                }
                CombinedRealm.log.debug ( CombinedRealm.sm.getString ( "combinedRealm.authFail", username, realm.getClass().getName() ) );
            } else {
                if ( CombinedRealm.log.isDebugEnabled() ) {
                    CombinedRealm.log.debug ( CombinedRealm.sm.getString ( "combinedRealm.authSuccess", username, realm.getClass().getName() ) );
                    break;
                }
                break;
            }
        }
        return authenticatedUser;
    }
    @Override
    public void setContainer ( final Container container ) {
        for ( final Realm realm : this.realms ) {
            if ( realm instanceof RealmBase ) {
                ( ( RealmBase ) realm ).setRealmPath ( this.getRealmPath() + "/realm" + this.realms.indexOf ( realm ) );
            }
            realm.setContainer ( container );
        }
        super.setContainer ( container );
    }
    @Override
    protected void startInternal() throws LifecycleException {
        final Iterator<Realm> iter = this.realms.iterator();
        while ( iter.hasNext() ) {
            final Realm realm = iter.next();
            if ( realm instanceof Lifecycle ) {
                try {
                    ( ( Lifecycle ) realm ).start();
                } catch ( LifecycleException e ) {
                    iter.remove();
                    CombinedRealm.log.error ( CombinedRealm.sm.getString ( "combinedRealm.realmStartFail", realm.getClass().getName() ), e );
                }
            }
        }
        super.startInternal();
    }
    @Override
    protected void stopInternal() throws LifecycleException {
        super.stopInternal();
        for ( final Realm realm : this.realms ) {
            if ( realm instanceof Lifecycle ) {
                ( ( Lifecycle ) realm ).stop();
            }
        }
    }
    @Override
    protected void destroyInternal() throws LifecycleException {
        for ( final Realm realm : this.realms ) {
            if ( realm instanceof Lifecycle ) {
                ( ( Lifecycle ) realm ).destroy();
            }
        }
        super.destroyInternal();
    }
    @Override
    public void backgroundProcess() {
        super.backgroundProcess();
        for ( final Realm r : this.realms ) {
            r.backgroundProcess();
        }
    }
    @Override
    public Principal authenticate ( final X509Certificate[] certs ) {
        Principal authenticatedUser = null;
        String username = null;
        if ( certs != null && certs.length > 0 ) {
            username = certs[0].getSubjectDN().getName();
        }
        for ( final Realm realm : this.realms ) {
            if ( CombinedRealm.log.isDebugEnabled() ) {
                CombinedRealm.log.debug ( CombinedRealm.sm.getString ( "combinedRealm.authStart", username, realm.getClass().getName() ) );
            }
            authenticatedUser = realm.authenticate ( certs );
            if ( authenticatedUser == null ) {
                if ( !CombinedRealm.log.isDebugEnabled() ) {
                    continue;
                }
                CombinedRealm.log.debug ( CombinedRealm.sm.getString ( "combinedRealm.authFail", username, realm.getClass().getName() ) );
            } else {
                if ( CombinedRealm.log.isDebugEnabled() ) {
                    CombinedRealm.log.debug ( CombinedRealm.sm.getString ( "combinedRealm.authSuccess", username, realm.getClass().getName() ) );
                    break;
                }
                break;
            }
        }
        return authenticatedUser;
    }
    @Override
    public Principal authenticate ( final GSSContext gssContext, final boolean storeCreds ) {
        if ( gssContext.isEstablished() ) {
            Principal authenticatedUser = null;
            String username = null;
            GSSName name = null;
            try {
                name = gssContext.getSrcName();
            } catch ( GSSException e ) {
                CombinedRealm.log.warn ( CombinedRealm.sm.getString ( "realmBase.gssNameFail" ), e );
                return null;
            }
            username = name.toString();
            for ( final Realm realm : this.realms ) {
                if ( CombinedRealm.log.isDebugEnabled() ) {
                    CombinedRealm.log.debug ( CombinedRealm.sm.getString ( "combinedRealm.authStart", username, realm.getClass().getName() ) );
                }
                authenticatedUser = realm.authenticate ( gssContext, storeCreds );
                if ( authenticatedUser == null ) {
                    if ( !CombinedRealm.log.isDebugEnabled() ) {
                        continue;
                    }
                    CombinedRealm.log.debug ( CombinedRealm.sm.getString ( "combinedRealm.authFail", username, realm.getClass().getName() ) );
                } else {
                    if ( CombinedRealm.log.isDebugEnabled() ) {
                        CombinedRealm.log.debug ( CombinedRealm.sm.getString ( "combinedRealm.authSuccess", username, realm.getClass().getName() ) );
                        break;
                    }
                    break;
                }
            }
            return authenticatedUser;
        }
        return null;
    }
    @Override
    protected String getName() {
        return "CombinedRealm";
    }
    @Override
    protected String getPassword ( final String username ) {
        final UnsupportedOperationException uoe = new UnsupportedOperationException ( CombinedRealm.sm.getString ( "combinedRealm.getPassword" ) );
        CombinedRealm.log.error ( CombinedRealm.sm.getString ( "combinedRealm.unexpectedMethod" ), uoe );
        throw uoe;
    }
    @Override
    protected Principal getPrincipal ( final String username ) {
        final UnsupportedOperationException uoe = new UnsupportedOperationException ( CombinedRealm.sm.getString ( "combinedRealm.getPrincipal" ) );
        CombinedRealm.log.error ( CombinedRealm.sm.getString ( "combinedRealm.unexpectedMethod" ), uoe );
        throw uoe;
    }
    @Override
    public boolean isAvailable() {
        for ( final Realm realm : this.realms ) {
            if ( !realm.isAvailable() ) {
                return false;
            }
        }
        return true;
    }
    static {
        log = LogFactory.getLog ( CombinedRealm.class );
    }
}
