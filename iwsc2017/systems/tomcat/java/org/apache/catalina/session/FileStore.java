package org.apache.catalina.session;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import javax.servlet.ServletContext;
import org.apache.catalina.Context;
import org.apache.catalina.Globals;
import org.apache.catalina.Session;
import org.apache.juli.logging.Log;
public final class FileStore extends StoreBase {
    private static final String FILE_EXT = ".session";
    private String directory = ".";
    private File directoryFile = null;
    private static final String storeName = "fileStore";
    private static final String threadName = "FileStore";
    public String getDirectory() {
        return directory;
    }
    public void setDirectory ( String path ) {
        String oldDirectory = this.directory;
        this.directory = path;
        this.directoryFile = null;
        support.firePropertyChange ( "directory", oldDirectory, this.directory );
    }
    public String getThreadName() {
        return threadName;
    }
    @Override
    public String getStoreName() {
        return storeName;
    }
    @Override
    public int getSize() throws IOException {
        File file = directory();
        if ( file == null ) {
            return ( 0 );
        }
        String files[] = file.list();
        int keycount = 0;
        if ( files != null ) {
            for ( int i = 0; i < files.length; i++ ) {
                if ( files[i].endsWith ( FILE_EXT ) ) {
                    keycount++;
                }
            }
        }
        return keycount;
    }
    @Override
    public void clear() throws IOException {
        String[] keys = keys();
        for ( int i = 0; i < keys.length; i++ ) {
            remove ( keys[i] );
        }
    }
    @Override
    public String[] keys() throws IOException {
        File file = directory();
        if ( file == null ) {
            return ( new String[0] );
        }
        String files[] = file.list();
        if ( ( files == null ) || ( files.length < 1 ) ) {
            return ( new String[0] );
        }
        ArrayList<String> list = new ArrayList<>();
        int n = FILE_EXT.length();
        for ( int i = 0; i < files.length; i++ ) {
            if ( files[i].endsWith ( FILE_EXT ) ) {
                list.add ( files[i].substring ( 0, files[i].length() - n ) );
            }
        }
        return list.toArray ( new String[list.size()] );
    }
    @Override
    public Session load ( String id ) throws ClassNotFoundException, IOException {
        File file = file ( id );
        if ( file == null ) {
            return null;
        }
        if ( !file.exists() ) {
            return null;
        }
        Context context = getManager().getContext();
        Log contextLog = context.getLogger();
        if ( contextLog.isDebugEnabled() ) {
            contextLog.debug ( sm.getString ( getStoreName() + ".loading", id, file.getAbsolutePath() ) );
        }
        ClassLoader oldThreadContextCL = context.bind ( Globals.IS_SECURITY_ENABLED, null );
        try ( FileInputStream fis = new FileInputStream ( file.getAbsolutePath() );
                    ObjectInputStream ois = getObjectInputStream ( fis ) ) {
            StandardSession session = ( StandardSession ) manager.createEmptySession();
            session.readObjectData ( ois );
            session.setManager ( manager );
            return session;
        } catch ( FileNotFoundException e ) {
            if ( contextLog.isDebugEnabled() ) {
                contextLog.debug ( "No persisted data file found" );
            }
            return null;
        } finally {
            context.unbind ( Globals.IS_SECURITY_ENABLED, oldThreadContextCL );
        }
    }
    @Override
    public void remove ( String id ) throws IOException {
        File file = file ( id );
        if ( file == null ) {
            return;
        }
        if ( manager.getContext().getLogger().isDebugEnabled() ) {
            manager.getContext().getLogger().debug ( sm.getString ( getStoreName() + ".removing",
                    id, file.getAbsolutePath() ) );
        }
        file.delete();
    }
    @Override
    public void save ( Session session ) throws IOException {
        File file = file ( session.getIdInternal() );
        if ( file == null ) {
            return;
        }
        if ( manager.getContext().getLogger().isDebugEnabled() ) {
            manager.getContext().getLogger().debug ( sm.getString ( getStoreName() + ".saving",
                    session.getIdInternal(), file.getAbsolutePath() ) );
        }
        try ( FileOutputStream fos = new FileOutputStream ( file.getAbsolutePath() );
                    ObjectOutputStream oos = new ObjectOutputStream ( new BufferedOutputStream ( fos ) ) ) {
            ( ( StandardSession ) session ).writeObjectData ( oos );
        }
    }
    private File directory() throws IOException {
        if ( this.directory == null ) {
            return null;
        }
        if ( this.directoryFile != null ) {
            return this.directoryFile;
        }
        File file = new File ( this.directory );
        if ( !file.isAbsolute() ) {
            Context context = manager.getContext();
            ServletContext servletContext = context.getServletContext();
            File work = ( File ) servletContext.getAttribute ( ServletContext.TEMPDIR );
            file = new File ( work, this.directory );
        }
        if ( !file.exists() || !file.isDirectory() ) {
            if ( !file.delete() && file.exists() ) {
                throw new IOException ( sm.getString ( "fileStore.deleteFailed", file ) );
            }
            if ( !file.mkdirs() && !file.isDirectory() ) {
                throw new IOException ( sm.getString ( "fileStore.createFailed", file ) );
            }
        }
        this.directoryFile = file;
        return file;
    }
    private File file ( String id ) throws IOException {
        if ( this.directory == null ) {
            return null;
        }
        String filename = id + FILE_EXT;
        File file = new File ( directory(), filename );
        return file;
    }
}
