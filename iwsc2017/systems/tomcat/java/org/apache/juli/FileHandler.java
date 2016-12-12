package org.apache.juli;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.sql.Timestamp;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.logging.ErrorManager;
import java.util.logging.Filter;
import java.util.logging.Formatter;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.LogRecord;
public class FileHandler
    extends Handler {
    public FileHandler() {
        this ( null, null, null );
    }
    public FileHandler ( String directory, String prefix, String suffix ) {
        this.directory = directory;
        this.prefix = prefix;
        this.suffix = suffix;
        configure();
        openWriter();
    }
    private volatile String date = "";
    private String directory = null;
    private String prefix = null;
    private String suffix = null;
    private boolean rotatable = true;
    private volatile PrintWriter writer = null;
    protected final ReadWriteLock writerLock = new ReentrantReadWriteLock();
    private int bufferSize = -1;
    @Override
    public void publish ( LogRecord record ) {
        if ( !isLoggable ( record ) ) {
            return;
        }
        Timestamp ts = new Timestamp ( System.currentTimeMillis() );
        String tsString = ts.toString().substring ( 0, 19 );
        String tsDate = tsString.substring ( 0, 10 );
        writerLock.readLock().lock();
        try {
            if ( rotatable && !date.equals ( tsDate ) ) {
                writerLock.readLock().unlock();
                writerLock.writeLock().lock();
                try {
                    if ( !date.equals ( tsDate ) ) {
                        closeWriter();
                        date = tsDate;
                        openWriter();
                    }
                } finally {
                    writerLock.readLock().lock();
                    writerLock.writeLock().unlock();
                }
            }
            String result = null;
            try {
                result = getFormatter().format ( record );
            } catch ( Exception e ) {
                reportError ( null, e, ErrorManager.FORMAT_FAILURE );
                return;
            }
            try {
                if ( writer != null ) {
                    writer.write ( result );
                    if ( bufferSize < 0 ) {
                        writer.flush();
                    }
                } else {
                    reportError ( "FileHandler is closed or not yet initialized, unable to log [" + result + "]", null, ErrorManager.WRITE_FAILURE );
                }
            } catch ( Exception e ) {
                reportError ( null, e, ErrorManager.WRITE_FAILURE );
                return;
            }
        } finally {
            writerLock.readLock().unlock();
        }
    }
    @Override
    public void close() {
        closeWriter();
    }
    protected void closeWriter() {
        writerLock.writeLock().lock();
        try {
            if ( writer == null ) {
                return;
            }
            writer.write ( getFormatter().getTail ( this ) );
            writer.flush();
            writer.close();
            writer = null;
            date = "";
        } catch ( Exception e ) {
            reportError ( null, e, ErrorManager.CLOSE_FAILURE );
        } finally {
            writerLock.writeLock().unlock();
        }
    }
    @Override
    public void flush() {
        writerLock.readLock().lock();
        try {
            if ( writer == null ) {
                return;
            }
            writer.flush();
        } catch ( Exception e ) {
            reportError ( null, e, ErrorManager.FLUSH_FAILURE );
        } finally {
            writerLock.readLock().unlock();
        }
    }
    private void configure() {
        Timestamp ts = new Timestamp ( System.currentTimeMillis() );
        String tsString = ts.toString().substring ( 0, 19 );
        date = tsString.substring ( 0, 10 );
        String className = this.getClass().getName();
        ClassLoader cl = Thread.currentThread().getContextClassLoader();
        rotatable = Boolean.parseBoolean ( getProperty ( className + ".rotatable", "true" ) );
        if ( directory == null ) {
            directory = getProperty ( className + ".directory", "logs" );
        }
        if ( prefix == null ) {
            prefix = getProperty ( className + ".prefix", "juli." );
        }
        if ( suffix == null ) {
            suffix = getProperty ( className + ".suffix", ".log" );
        }
        String sBufferSize = getProperty ( className + ".bufferSize", String.valueOf ( bufferSize ) );
        try {
            bufferSize = Integer.parseInt ( sBufferSize );
        } catch ( NumberFormatException ignore ) {
        }
        String encoding = getProperty ( className + ".encoding", null );
        if ( encoding != null && encoding.length() > 0 ) {
            try {
                setEncoding ( encoding );
            } catch ( UnsupportedEncodingException ex ) {
            }
        }
        setLevel ( Level.parse ( getProperty ( className + ".level", "" + Level.ALL ) ) );
        String filterName = getProperty ( className + ".filter", null );
        if ( filterName != null ) {
            try {
                setFilter ( ( Filter ) cl.loadClass ( filterName ).newInstance() );
            } catch ( Exception e ) {
            }
        }
        String formatterName = getProperty ( className + ".formatter", null );
        if ( formatterName != null ) {
            try {
                setFormatter ( ( Formatter ) cl.loadClass ( formatterName ).newInstance() );
            } catch ( Exception e ) {
                setFormatter ( new OneLineFormatter() );
            }
        } else {
            setFormatter ( new OneLineFormatter() );
        }
        setErrorManager ( new ErrorManager() );
    }
    private String getProperty ( String name, String defaultValue ) {
        String value = LogManager.getLogManager().getProperty ( name );
        if ( value == null ) {
            value = defaultValue;
        } else {
            value = value.trim();
        }
        return value;
    }
    protected void open() {
        openWriter();
    }
    protected void openWriter() {
        File dir = new File ( directory );
        if ( !dir.mkdirs() && !dir.isDirectory() ) {
            reportError ( "Unable to create [" + dir + "]", null,
                          ErrorManager.OPEN_FAILURE );
            writer = null;
            return;
        }
        writerLock.writeLock().lock();
        FileOutputStream fos = null;
        OutputStream os = null;
        try {
            File pathname = new File ( dir.getAbsoluteFile(), prefix
                                       + ( rotatable ? date : "" ) + suffix );
            File parent = pathname.getParentFile();
            if ( !parent.mkdirs() && !parent.isDirectory() ) {
                reportError ( "Unable to create [" + parent + "]", null,
                              ErrorManager.OPEN_FAILURE );
                writer = null;
                return;
            }
            String encoding = getEncoding();
            fos = new FileOutputStream ( pathname, true );
            os = bufferSize > 0 ? new BufferedOutputStream ( fos, bufferSize ) : fos;
            writer = new PrintWriter (
                ( encoding != null ) ? new OutputStreamWriter ( os, encoding )
                : new OutputStreamWriter ( os ), false );
            writer.write ( getFormatter().getHead ( this ) );
        } catch ( Exception e ) {
            reportError ( null, e, ErrorManager.OPEN_FAILURE );
            writer = null;
            if ( fos != null ) {
                try {
                    fos.close();
                } catch ( IOException e1 ) {
                }
            }
            if ( os != null ) {
                try {
                    os.close();
                } catch ( IOException e1 ) {
                }
            }
        } finally {
            writerLock.writeLock().unlock();
        }
    }
}
