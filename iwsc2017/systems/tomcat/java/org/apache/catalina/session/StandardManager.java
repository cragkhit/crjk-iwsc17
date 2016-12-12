package org.apache.catalina.session;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.security.AccessController;
import java.security.PrivilegedActionException;
import java.security.PrivilegedExceptionAction;
import java.util.ArrayList;
import java.util.Iterator;
import javax.servlet.ServletContext;
import org.apache.catalina.Context;
import org.apache.catalina.LifecycleException;
import org.apache.catalina.LifecycleState;
import org.apache.catalina.Loader;
import org.apache.catalina.Session;
import org.apache.catalina.security.SecurityUtil;
import org.apache.catalina.util.CustomObjectInputStream;
import org.apache.juli.logging.Log;
import org.apache.juli.logging.LogFactory;
import org.apache.tomcat.util.ExceptionUtils;
public class StandardManager extends ManagerBase {
    private final Log log = LogFactory.getLog ( StandardManager.class );
    private class PrivilegedDoLoad
        implements PrivilegedExceptionAction<Void> {
        PrivilegedDoLoad() {
        }
        @Override
        public Void run() throws Exception {
            doLoad();
            return null;
        }
    }
    private class PrivilegedDoUnload
        implements PrivilegedExceptionAction<Void> {
        PrivilegedDoUnload() {
        }
        @Override
        public Void run() throws Exception {
            doUnload();
            return null;
        }
    }
    protected static final String name = "StandardManager";
    protected String pathname = "SESSIONS.ser";
    @Override
    public String getName() {
        return name;
    }
    public String getPathname() {
        return pathname;
    }
    public void setPathname ( String pathname ) {
        String oldPathname = this.pathname;
        this.pathname = pathname;
        support.firePropertyChange ( "pathname", oldPathname, this.pathname );
    }
    @Override
    public void load() throws ClassNotFoundException, IOException {
        if ( SecurityUtil.isPackageProtectionEnabled() ) {
            try {
                AccessController.doPrivileged ( new PrivilegedDoLoad() );
            } catch ( PrivilegedActionException ex ) {
                Exception exception = ex.getException();
                if ( exception instanceof ClassNotFoundException ) {
                    throw ( ClassNotFoundException ) exception;
                } else if ( exception instanceof IOException ) {
                    throw ( IOException ) exception;
                }
                if ( log.isDebugEnabled() ) {
                    log.debug ( "Unreported exception in load() ", exception );
                }
            }
        } else {
            doLoad();
        }
    }
    protected void doLoad() throws ClassNotFoundException, IOException {
        if ( log.isDebugEnabled() ) {
            log.debug ( "Start: Loading persisted sessions" );
        }
        sessions.clear();
        File file = file();
        if ( file == null ) {
            return;
        }
        if ( log.isDebugEnabled() ) {
            log.debug ( sm.getString ( "standardManager.loading", pathname ) );
        }
        Loader loader = null;
        ClassLoader classLoader = null;
        Log logger = null;
        try ( FileInputStream fis = new FileInputStream ( file.getAbsolutePath() );
                    BufferedInputStream bis = new BufferedInputStream ( fis ) ) {
            Context c = getContext();
            loader = c.getLoader();
            logger = c.getLogger();
            if ( loader != null ) {
                classLoader = loader.getClassLoader();
            }
            if ( classLoader == null ) {
                classLoader = getClass().getClassLoader();
            }
            synchronized ( sessions ) {
                try ( ObjectInputStream ois = new CustomObjectInputStream ( bis, classLoader, logger,
                            getSessionAttributeValueClassNamePattern(),
                            getWarnOnSessionAttributeFilterFailure() ) ) {
                    Integer count = ( Integer ) ois.readObject();
                    int n = count.intValue();
                    if ( log.isDebugEnabled() ) {
                        log.debug ( "Loading " + n + " persisted sessions" );
                    }
                    for ( int i = 0; i < n; i++ ) {
                        StandardSession session = getNewSession();
                        session.readObjectData ( ois );
                        session.setManager ( this );
                        sessions.put ( session.getIdInternal(), session );
                        session.activate();
                        if ( !session.isValidInternal() ) {
                            session.setValid ( true );
                            session.expire();
                        }
                        sessionCounter++;
                    }
                } finally {
                    if ( file.exists() ) {
                        file.delete();
                    }
                }
            }
        } catch ( FileNotFoundException e ) {
            if ( log.isDebugEnabled() ) {
                log.debug ( "No persisted data file found" );
            }
            return;
        }
        if ( log.isDebugEnabled() ) {
            log.debug ( "Finish: Loading persisted sessions" );
        }
    }
    @Override
    public void unload() throws IOException {
        if ( SecurityUtil.isPackageProtectionEnabled() ) {
            try {
                AccessController.doPrivileged ( new PrivilegedDoUnload() );
            } catch ( PrivilegedActionException ex ) {
                Exception exception = ex.getException();
                if ( exception instanceof IOException ) {
                    throw ( IOException ) exception;
                }
                if ( log.isDebugEnabled() ) {
                    log.debug ( "Unreported exception in unLoad()", exception );
                }
            }
        } else {
            doUnload();
        }
    }
    protected void doUnload() throws IOException {
        if ( log.isDebugEnabled() ) {
            log.debug ( sm.getString ( "standardManager.unloading.debug" ) );
        }
        if ( sessions.isEmpty() ) {
            log.debug ( sm.getString ( "standardManager.unloading.nosessions" ) );
            return;
        }
        File file = file();
        if ( file == null ) {
            return;
        }
        if ( log.isDebugEnabled() ) {
            log.debug ( sm.getString ( "standardManager.unloading", pathname ) );
        }
        ArrayList<StandardSession> list = new ArrayList<>();
        try ( FileOutputStream fos = new FileOutputStream ( file.getAbsolutePath() );
                    BufferedOutputStream bos = new BufferedOutputStream ( fos );
                    ObjectOutputStream oos = new ObjectOutputStream ( bos ) ) {
            synchronized ( sessions ) {
                if ( log.isDebugEnabled() ) {
                    log.debug ( "Unloading " + sessions.size() + " sessions" );
                }
                oos.writeObject ( Integer.valueOf ( sessions.size() ) );
                Iterator<Session> elements = sessions.values().iterator();
                while ( elements.hasNext() ) {
                    StandardSession session =
                        ( StandardSession ) elements.next();
                    list.add ( session );
                    session.passivate();
                    session.writeObjectData ( oos );
                }
            }
        }
        if ( log.isDebugEnabled() ) {
            log.debug ( "Expiring " + list.size() + " persisted sessions" );
        }
        Iterator<StandardSession> expires = list.iterator();
        while ( expires.hasNext() ) {
            StandardSession session = expires.next();
            try {
                session.expire ( false );
            } catch ( Throwable t ) {
                ExceptionUtils.handleThrowable ( t );
            } finally {
                session.recycle();
            }
        }
        if ( log.isDebugEnabled() ) {
            log.debug ( "Unloading complete" );
        }
    }
    @Override
    protected synchronized void startInternal() throws LifecycleException {
        super.startInternal();
        try {
            load();
        } catch ( Throwable t ) {
            ExceptionUtils.handleThrowable ( t );
            log.error ( sm.getString ( "standardManager.managerLoad" ), t );
        }
        setState ( LifecycleState.STARTING );
    }
    @Override
    protected synchronized void stopInternal() throws LifecycleException {
        if ( log.isDebugEnabled() ) {
            log.debug ( "Stopping" );
        }
        setState ( LifecycleState.STOPPING );
        try {
            unload();
        } catch ( Throwable t ) {
            ExceptionUtils.handleThrowable ( t );
            log.error ( sm.getString ( "standardManager.managerUnload" ), t );
        }
        Session sessions[] = findSessions();
        for ( int i = 0; i < sessions.length; i++ ) {
            Session session = sessions[i];
            try {
                if ( session.isValid() ) {
                    session.expire();
                }
            } catch ( Throwable t ) {
                ExceptionUtils.handleThrowable ( t );
            } finally {
                session.recycle();
            }
        }
        super.stopInternal();
    }
    protected File file() {
        if ( pathname == null || pathname.length() == 0 ) {
            return null;
        }
        File file = new File ( pathname );
        if ( !file.isAbsolute() ) {
            Context context = getContext();
            ServletContext servletContext = context.getServletContext();
            File tempdir = ( File ) servletContext.getAttribute ( ServletContext.TEMPDIR );
            if ( tempdir != null ) {
                file = new File ( tempdir, pathname );
            }
        }
        return file;
    }
}
