package org.apache.catalina.session;
import javax.servlet.ServletContext;
import org.apache.catalina.LifecycleException;
import org.apache.catalina.LifecycleState;
import org.apache.catalina.Session;
import java.util.Iterator;
import org.apache.tomcat.util.ExceptionUtils;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.BufferedOutputStream;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.io.ObjectInputStream;
import org.apache.catalina.Context;
import org.apache.catalina.Loader;
import java.io.File;
import java.io.FileNotFoundException;
import org.apache.catalina.Manager;
import org.apache.catalina.util.CustomObjectInputStream;
import java.io.InputStream;
import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.security.PrivilegedActionException;
import java.io.IOException;
import java.security.PrivilegedExceptionAction;
import java.security.AccessController;
import org.apache.catalina.security.SecurityUtil;
import org.apache.juli.logging.LogFactory;
import org.apache.juli.logging.Log;
public class StandardManager extends ManagerBase {
    private final Log log;
    protected static final String name = "StandardManager";
    protected String pathname;
    public StandardManager() {
        this.log = LogFactory.getLog ( StandardManager.class );
        this.pathname = "SESSIONS.ser";
    }
    @Override
    public String getName() {
        return "StandardManager";
    }
    public String getPathname() {
        return this.pathname;
    }
    public void setPathname ( final String pathname ) {
        final String oldPathname = this.pathname;
        this.pathname = pathname;
        this.support.firePropertyChange ( "pathname", oldPathname, this.pathname );
    }
    @Override
    public void load() throws ClassNotFoundException, IOException {
        if ( SecurityUtil.isPackageProtectionEnabled() ) {
            try {
                AccessController.doPrivileged ( ( PrivilegedExceptionAction<Object> ) new PrivilegedDoLoad() );
            } catch ( PrivilegedActionException ex ) {
                final Exception exception = ex.getException();
                if ( exception instanceof ClassNotFoundException ) {
                    throw ( ClassNotFoundException ) exception;
                }
                if ( exception instanceof IOException ) {
                    throw ( IOException ) exception;
                }
                if ( this.log.isDebugEnabled() ) {
                    this.log.debug ( "Unreported exception in load() ", exception );
                }
            }
        } else {
            this.doLoad();
        }
    }
    protected void doLoad() throws ClassNotFoundException, IOException {
        if ( this.log.isDebugEnabled() ) {
            this.log.debug ( "Start: Loading persisted sessions" );
        }
        this.sessions.clear();
        final File file = this.file();
        if ( file == null ) {
            return;
        }
        if ( this.log.isDebugEnabled() ) {
            this.log.debug ( StandardManager.sm.getString ( "standardManager.loading", this.pathname ) );
        }
        Loader loader = null;
        ClassLoader classLoader = null;
        Log logger = null;
        try ( final FileInputStream fis = new FileInputStream ( file.getAbsolutePath() );
                    final BufferedInputStream bis = new BufferedInputStream ( fis ) ) {
            final Context c = this.getContext();
            loader = c.getLoader();
            logger = c.getLogger();
            if ( loader != null ) {
                classLoader = loader.getClassLoader();
            }
            if ( classLoader == null ) {
                classLoader = this.getClass().getClassLoader();
            }
            synchronized ( this.sessions ) {
                try ( final ObjectInputStream ois = new CustomObjectInputStream ( bis, classLoader, logger, this.getSessionAttributeValueClassNamePattern(), this.getWarnOnSessionAttributeFilterFailure() ) ) {
                    final Integer count = ( Integer ) ois.readObject();
                    final int n = count;
                    if ( this.log.isDebugEnabled() ) {
                        this.log.debug ( "Loading " + n + " persisted sessions" );
                    }
                    for ( int i = 0; i < n; ++i ) {
                        final StandardSession session = this.getNewSession();
                        session.readObjectData ( ois );
                        session.setManager ( this );
                        this.sessions.put ( session.getIdInternal(), session );
                        session.activate();
                        if ( !session.isValidInternal() ) {
                            session.setValid ( true );
                            session.expire();
                        }
                        ++this.sessionCounter;
                    }
                } finally {
                    if ( file.exists() ) {
                        file.delete();
                    }
                }
            }
        } catch ( FileNotFoundException e ) {
            if ( this.log.isDebugEnabled() ) {
                this.log.debug ( "No persisted data file found" );
            }
            return;
        }
        if ( this.log.isDebugEnabled() ) {
            this.log.debug ( "Finish: Loading persisted sessions" );
        }
    }
    @Override
    public void unload() throws IOException {
        if ( SecurityUtil.isPackageProtectionEnabled() ) {
            try {
                AccessController.doPrivileged ( ( PrivilegedExceptionAction<Object> ) new PrivilegedDoUnload() );
            } catch ( PrivilegedActionException ex ) {
                final Exception exception = ex.getException();
                if ( exception instanceof IOException ) {
                    throw ( IOException ) exception;
                }
                if ( this.log.isDebugEnabled() ) {
                    this.log.debug ( "Unreported exception in unLoad()", exception );
                }
            }
        } else {
            this.doUnload();
        }
    }
    protected void doUnload() throws IOException {
        if ( this.log.isDebugEnabled() ) {
            this.log.debug ( StandardManager.sm.getString ( "standardManager.unloading.debug" ) );
        }
        if ( this.sessions.isEmpty() ) {
            this.log.debug ( StandardManager.sm.getString ( "standardManager.unloading.nosessions" ) );
            return;
        }
        final File file = this.file();
        if ( file == null ) {
            return;
        }
        if ( this.log.isDebugEnabled() ) {
            this.log.debug ( StandardManager.sm.getString ( "standardManager.unloading", this.pathname ) );
        }
        final ArrayList<StandardSession> list = new ArrayList<StandardSession>();
        try ( final FileOutputStream fos = new FileOutputStream ( file.getAbsolutePath() );
                    final BufferedOutputStream bos = new BufferedOutputStream ( fos );
                    final ObjectOutputStream oos = new ObjectOutputStream ( bos ) ) {
            synchronized ( this.sessions ) {
                if ( this.log.isDebugEnabled() ) {
                    this.log.debug ( "Unloading " + this.sessions.size() + " sessions" );
                }
                oos.writeObject ( this.sessions.size() );
                for ( final StandardSession session : this.sessions.values() ) {
                    list.add ( session );
                    session.passivate();
                    session.writeObjectData ( oos );
                }
            }
        }
        if ( this.log.isDebugEnabled() ) {
            this.log.debug ( "Expiring " + list.size() + " persisted sessions" );
        }
        for ( final StandardSession session2 : list ) {
            try {
                session2.expire ( false );
            } catch ( Throwable t ) {
                ExceptionUtils.handleThrowable ( t );
            } finally {
                session2.recycle();
            }
        }
        if ( this.log.isDebugEnabled() ) {
            this.log.debug ( "Unloading complete" );
        }
    }
    @Override
    protected synchronized void startInternal() throws LifecycleException {
        super.startInternal();
        try {
            this.load();
        } catch ( Throwable t ) {
            ExceptionUtils.handleThrowable ( t );
            this.log.error ( StandardManager.sm.getString ( "standardManager.managerLoad" ), t );
        }
        this.setState ( LifecycleState.STARTING );
    }
    @Override
    protected synchronized void stopInternal() throws LifecycleException {
        if ( this.log.isDebugEnabled() ) {
            this.log.debug ( "Stopping" );
        }
        this.setState ( LifecycleState.STOPPING );
        try {
            this.unload();
        } catch ( Throwable t ) {
            ExceptionUtils.handleThrowable ( t );
            this.log.error ( StandardManager.sm.getString ( "standardManager.managerUnload" ), t );
        }
        final Session[] sessions = this.findSessions();
        for ( int i = 0; i < sessions.length; ++i ) {
            final Session session = sessions[i];
            try {
                if ( session.isValid() ) {
                    session.expire();
                }
            } catch ( Throwable t2 ) {
                ExceptionUtils.handleThrowable ( t2 );
            } finally {
                session.recycle();
            }
        }
        super.stopInternal();
    }
    protected File file() {
        if ( this.pathname == null || this.pathname.length() == 0 ) {
            return null;
        }
        File file = new File ( this.pathname );
        if ( !file.isAbsolute() ) {
            final Context context = this.getContext();
            final ServletContext servletContext = context.getServletContext();
            final File tempdir = ( File ) servletContext.getAttribute ( "javax.servlet.context.tempdir" );
            if ( tempdir != null ) {
                file = new File ( tempdir, this.pathname );
            }
        }
        return file;
    }
    private class PrivilegedDoLoad implements PrivilegedExceptionAction<Void> {
        @Override
        public Void run() throws Exception {
            StandardManager.this.doLoad();
            return null;
        }
    }
    private class PrivilegedDoUnload implements PrivilegedExceptionAction<Void> {
        @Override
        public Void run() throws Exception {
            StandardManager.this.doUnload();
            return null;
        }
    }
}
