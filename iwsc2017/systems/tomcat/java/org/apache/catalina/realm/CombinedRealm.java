package org.apache.catalina.realm;
import java.security.Principal;
import java.security.cert.X509Certificate;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import javax.management.ObjectName;
import org.apache.catalina.Container;
import org.apache.catalina.Lifecycle;
import org.apache.catalina.LifecycleException;
import org.apache.catalina.Realm;
import org.apache.juli.logging.Log;
import org.apache.juli.logging.LogFactory;
import org.ietf.jgss.GSSContext;
import org.ietf.jgss.GSSException;
import org.ietf.jgss.GSSName;
public class CombinedRealm extends RealmBase {
    private static final Log log = LogFactory.getLog ( CombinedRealm.class );
    protected final List<Realm> realms = new LinkedList<>();
    protected static final String name = "CombinedRealm";
    public void addRealm ( Realm theRealm ) {
        realms.add ( theRealm );
        if ( log.isDebugEnabled() ) {
            sm.getString ( "combinedRealm.addRealm",
                           theRealm.getClass().getName(),
                           Integer.toString ( realms.size() ) );
        }
    }
    public ObjectName[] getRealms() {
        ObjectName[] result = new ObjectName[realms.size()];
        for ( Realm realm : realms ) {
            if ( realm instanceof RealmBase ) {
                result[realms.indexOf ( realm )] =
                    ( ( RealmBase ) realm ).getObjectName();
            }
        }
        return result;
    }
    public Realm[] getNestedRealms() {
        return realms.toArray ( new Realm[0] );
    }
    @Override
    public Principal authenticate ( String username, String clientDigest,
                                    String nonce, String nc, String cnonce, String qop,
                                    String realmName, String md5a2 ) {
        Principal authenticatedUser = null;
        for ( Realm realm : realms ) {
            if ( log.isDebugEnabled() ) {
                log.debug ( sm.getString ( "combinedRealm.authStart", username,
                                           realm.getClass().getName() ) );
            }
            authenticatedUser = realm.authenticate ( username, clientDigest, nonce,
                                nc, cnonce, qop, realmName, md5a2 );
            if ( authenticatedUser == null ) {
                if ( log.isDebugEnabled() ) {
                    log.debug ( sm.getString ( "combinedRealm.authFail", username,
                                               realm.getClass().getName() ) );
                }
            } else {
                if ( log.isDebugEnabled() ) {
                    log.debug ( sm.getString ( "combinedRealm.authSuccess",
                                               username, realm.getClass().getName() ) );
                }
                break;
            }
        }
        return authenticatedUser;
    }
    @Override
    public Principal authenticate ( String username ) {
        Principal authenticatedUser = null;
        for ( Realm realm : realms ) {
            if ( log.isDebugEnabled() ) {
                log.debug ( sm.getString ( "combinedRealm.authStart", username,
                                           realm.getClass().getName() ) );
            }
            authenticatedUser = realm.authenticate ( username );
            if ( authenticatedUser == null ) {
                if ( log.isDebugEnabled() ) {
                    log.debug ( sm.getString ( "combinedRealm.authFail", username,
                                               realm.getClass().getName() ) );
                }
            } else {
                if ( log.isDebugEnabled() ) {
                    log.debug ( sm.getString ( "combinedRealm.authSuccess",
                                               username, realm.getClass().getName() ) );
                }
                break;
            }
        }
        return authenticatedUser;
    }
    @Override
    public Principal authenticate ( String username, String credentials ) {
        Principal authenticatedUser = null;
        for ( Realm realm : realms ) {
            if ( log.isDebugEnabled() ) {
                log.debug ( sm.getString ( "combinedRealm.authStart", username,
                                           realm.getClass().getName() ) );
            }
            authenticatedUser = realm.authenticate ( username, credentials );
            if ( authenticatedUser == null ) {
                if ( log.isDebugEnabled() ) {
                    log.debug ( sm.getString ( "combinedRealm.authFail", username,
                                               realm.getClass().getName() ) );
                }
            } else {
                if ( log.isDebugEnabled() ) {
                    log.debug ( sm.getString ( "combinedRealm.authSuccess",
                                               username, realm.getClass().getName() ) );
                }
                break;
            }
        }
        return authenticatedUser;
    }
    @Override
    public void setContainer ( Container container ) {
        for ( Realm realm : realms ) {
            if ( realm instanceof RealmBase ) {
                ( ( RealmBase ) realm ).setRealmPath (
                    getRealmPath() + "/realm" + realms.indexOf ( realm ) );
            }
            realm.setContainer ( container );
        }
        super.setContainer ( container );
    }
    @Override
    protected void startInternal() throws LifecycleException {
        Iterator<Realm> iter = realms.iterator();
        while ( iter.hasNext() ) {
            Realm realm = iter.next();
            if ( realm instanceof Lifecycle ) {
                try {
                    ( ( Lifecycle ) realm ).start();
                } catch ( LifecycleException e ) {
                    iter.remove();
                    log.error ( sm.getString ( "combinedRealm.realmStartFail",
                                               realm.getClass().getName() ), e );
                }
            }
        }
        super.startInternal();
    }
    @Override
    protected void stopInternal() throws LifecycleException {
        super.stopInternal();
        for ( Realm realm : realms ) {
            if ( realm instanceof Lifecycle ) {
                ( ( Lifecycle ) realm ).stop();
            }
        }
    }
    @Override
    protected void destroyInternal() throws LifecycleException {
        for ( Realm realm : realms ) {
            if ( realm instanceof Lifecycle ) {
                ( ( Lifecycle ) realm ).destroy();
            }
        }
        super.destroyInternal();
    }
    @Override
    public void backgroundProcess() {
        super.backgroundProcess();
        for ( Realm r : realms ) {
            r.backgroundProcess();
        }
    }
    @Override
    public Principal authenticate ( X509Certificate[] certs ) {
        Principal authenticatedUser = null;
        String username = null;
        if ( certs != null && certs.length > 0 ) {
            username = certs[0].getSubjectDN().getName();
        }
        for ( Realm realm : realms ) {
            if ( log.isDebugEnabled() ) {
                log.debug ( sm.getString ( "combinedRealm.authStart", username,
                                           realm.getClass().getName() ) );
            }
            authenticatedUser = realm.authenticate ( certs );
            if ( authenticatedUser == null ) {
                if ( log.isDebugEnabled() ) {
                    log.debug ( sm.getString ( "combinedRealm.authFail", username,
                                               realm.getClass().getName() ) );
                }
            } else {
                if ( log.isDebugEnabled() ) {
                    log.debug ( sm.getString ( "combinedRealm.authSuccess",
                                               username, realm.getClass().getName() ) );
                }
                break;
            }
        }
        return authenticatedUser;
    }
    @Override
    public Principal authenticate ( GSSContext gssContext, boolean storeCreds ) {
        if ( gssContext.isEstablished() ) {
            Principal authenticatedUser = null;
            String username = null;
            GSSName name = null;
            try {
                name = gssContext.getSrcName();
            } catch ( GSSException e ) {
                log.warn ( sm.getString ( "realmBase.gssNameFail" ), e );
                return null;
            }
            username = name.toString();
            for ( Realm realm : realms ) {
                if ( log.isDebugEnabled() ) {
                    log.debug ( sm.getString ( "combinedRealm.authStart",
                                               username, realm.getClass().getName() ) );
                }
                authenticatedUser = realm.authenticate ( gssContext, storeCreds );
                if ( authenticatedUser == null ) {
                    if ( log.isDebugEnabled() ) {
                        log.debug ( sm.getString ( "combinedRealm.authFail",
                                                   username, realm.getClass().getName() ) );
                    }
                } else {
                    if ( log.isDebugEnabled() ) {
                        log.debug ( sm.getString ( "combinedRealm.authSuccess",
                                                   username, realm.getClass().getName() ) );
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
        return name;
    }
    @Override
    protected String getPassword ( String username ) {
        UnsupportedOperationException uoe =
            new UnsupportedOperationException (
            sm.getString ( "combinedRealm.getPassword" ) );
        log.error ( sm.getString ( "combinedRealm.unexpectedMethod" ), uoe );
        throw uoe;
    }
    @Override
    protected Principal getPrincipal ( String username ) {
        UnsupportedOperationException uoe =
            new UnsupportedOperationException (
            sm.getString ( "combinedRealm.getPrincipal" ) );
        log.error ( sm.getString ( "combinedRealm.unexpectedMethod" ), uoe );
        throw uoe;
    }
    @Override
    public boolean isAvailable() {
        for ( Realm realm : realms ) {
            if ( !realm.isAvailable() ) {
                return false;
            }
        }
        return true;
    }
}
