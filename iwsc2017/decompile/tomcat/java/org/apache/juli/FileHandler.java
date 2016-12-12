package org.apache.juli;
import java.io.IOException;
import java.io.Writer;
import java.io.OutputStreamWriter;
import java.io.OutputStream;
import java.io.BufferedOutputStream;
import java.io.FileOutputStream;
import java.io.File;
import java.util.logging.LogManager;
import java.util.logging.ErrorManager;
import java.util.logging.Formatter;
import java.util.logging.Filter;
import java.util.logging.Level;
import java.io.UnsupportedEncodingException;
import java.sql.Timestamp;
import java.util.logging.LogRecord;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.concurrent.locks.ReadWriteLock;
import java.io.PrintWriter;
import java.util.logging.Handler;
public class FileHandler extends Handler {
    private volatile String date;
    private String directory;
    private String prefix;
    private String suffix;
    private boolean rotatable;
    private volatile PrintWriter writer;
    protected final ReadWriteLock writerLock;
    private int bufferSize;
    public FileHandler() {
        this ( null, null, null );
    }
    public FileHandler ( final String directory, final String prefix, final String suffix ) {
        this.date = "";
        this.directory = null;
        this.prefix = null;
        this.suffix = null;
        this.rotatable = true;
        this.writer = null;
        this.writerLock = new ReentrantReadWriteLock();
        this.bufferSize = -1;
        this.directory = directory;
        this.prefix = prefix;
        this.suffix = suffix;
        this.configure();
        this.openWriter();
    }
    @Override
    public void publish ( final LogRecord record ) {
        if ( !this.isLoggable ( record ) ) {
            return;
        }
        final Timestamp ts = new Timestamp ( System.currentTimeMillis() );
        final String tsString = ts.toString().substring ( 0, 19 );
        final String tsDate = tsString.substring ( 0, 10 );
        this.writerLock.readLock().lock();
        try {
            if ( this.rotatable && !this.date.equals ( tsDate ) ) {
                this.writerLock.readLock().unlock();
                this.writerLock.writeLock().lock();
                try {
                    if ( !this.date.equals ( tsDate ) ) {
                        this.closeWriter();
                        this.date = tsDate;
                        this.openWriter();
                    }
                } finally {
                    this.writerLock.readLock().lock();
                    this.writerLock.writeLock().unlock();
                }
            }
            String result = null;
            try {
                result = this.getFormatter().format ( record );
            } catch ( Exception e ) {
                this.reportError ( null, e, 5 );
                return;
            }
            try {
                if ( this.writer != null ) {
                    this.writer.write ( result );
                    if ( this.bufferSize < 0 ) {
                        this.writer.flush();
                    }
                } else {
                    this.reportError ( "FileHandler is closed or not yet initialized, unable to log [" + result + "]", null, 1 );
                }
            } catch ( Exception e ) {
                this.reportError ( null, e, 1 );
            }
        } finally {
            this.writerLock.readLock().unlock();
        }
    }
    @Override
    public void close() {
        this.closeWriter();
    }
    protected void closeWriter() {
        this.writerLock.writeLock().lock();
        try {
            if ( this.writer == null ) {
                return;
            }
            this.writer.write ( this.getFormatter().getTail ( this ) );
            this.writer.flush();
            this.writer.close();
            this.writer = null;
            this.date = "";
        } catch ( Exception e ) {
            this.reportError ( null, e, 3 );
        } finally {
            this.writerLock.writeLock().unlock();
        }
    }
    @Override
    public void flush() {
        this.writerLock.readLock().lock();
        try {
            if ( this.writer == null ) {
                return;
            }
            this.writer.flush();
        } catch ( Exception e ) {
            this.reportError ( null, e, 2 );
        } finally {
            this.writerLock.readLock().unlock();
        }
    }
    private void configure() {
        final Timestamp ts = new Timestamp ( System.currentTimeMillis() );
        final String tsString = ts.toString().substring ( 0, 19 );
        this.date = tsString.substring ( 0, 10 );
        final String className = this.getClass().getName();
        final ClassLoader cl = Thread.currentThread().getContextClassLoader();
        this.rotatable = Boolean.parseBoolean ( this.getProperty ( className + ".rotatable", "true" ) );
        if ( this.directory == null ) {
            this.directory = this.getProperty ( className + ".directory", "logs" );
        }
        if ( this.prefix == null ) {
            this.prefix = this.getProperty ( className + ".prefix", "juli." );
        }
        if ( this.suffix == null ) {
            this.suffix = this.getProperty ( className + ".suffix", ".log" );
        }
        final String sBufferSize = this.getProperty ( className + ".bufferSize", String.valueOf ( this.bufferSize ) );
        try {
            this.bufferSize = Integer.parseInt ( sBufferSize );
        } catch ( NumberFormatException ex ) {}
        final String encoding = this.getProperty ( className + ".encoding", null );
        if ( encoding != null && encoding.length() > 0 ) {
            try {
                this.setEncoding ( encoding );
            } catch ( UnsupportedEncodingException ex2 ) {}
        }
        this.setLevel ( Level.parse ( this.getProperty ( className + ".level", "" + Level.ALL ) ) );
        final String filterName = this.getProperty ( className + ".filter", null );
        if ( filterName != null ) {
            try {
                this.setFilter ( ( Filter ) cl.loadClass ( filterName ).newInstance() );
            } catch ( Exception ex3 ) {}
        }
        final String formatterName = this.getProperty ( className + ".formatter", null );
        if ( formatterName != null ) {
            try {
                this.setFormatter ( ( Formatter ) cl.loadClass ( formatterName ).newInstance() );
            } catch ( Exception e ) {
                this.setFormatter ( new OneLineFormatter() );
            }
        } else {
            this.setFormatter ( new OneLineFormatter() );
        }
        this.setErrorManager ( new ErrorManager() );
    }
    private String getProperty ( final String name, final String defaultValue ) {
        String value = LogManager.getLogManager().getProperty ( name );
        if ( value == null ) {
            value = defaultValue;
        } else {
            value = value.trim();
        }
        return value;
    }
    protected void open() {
        this.openWriter();
    }
    protected void openWriter() {
        final File dir = new File ( this.directory );
        if ( !dir.mkdirs() && !dir.isDirectory() ) {
            this.reportError ( "Unable to create [" + dir + "]", null, 4 );
            this.writer = null;
            return;
        }
        this.writerLock.writeLock().lock();
        FileOutputStream fos = null;
        OutputStream os = null;
        try {
            final File pathname = new File ( dir.getAbsoluteFile(), this.prefix + ( this.rotatable ? this.date : "" ) + this.suffix );
            final File parent = pathname.getParentFile();
            if ( !parent.mkdirs() && !parent.isDirectory() ) {
                this.reportError ( "Unable to create [" + parent + "]", null, 4 );
                this.writer = null;
                return;
            }
            final String encoding = this.getEncoding();
            fos = new FileOutputStream ( pathname, true );
            os = ( ( this.bufferSize > 0 ) ? new BufferedOutputStream ( fos, this.bufferSize ) : fos );
            ( this.writer = new PrintWriter ( ( encoding != null ) ? new OutputStreamWriter ( os, encoding ) : new OutputStreamWriter ( os ), false ) ).write ( this.getFormatter().getHead ( this ) );
        } catch ( Exception e ) {
            this.reportError ( null, e, 4 );
            this.writer = null;
            if ( fos != null ) {
                try {
                    fos.close();
                } catch ( IOException ex ) {}
            }
            if ( os != null ) {
                try {
                    os.close();
                } catch ( IOException ex2 ) {}
            }
        } finally {
            this.writerLock.writeLock().unlock();
        }
    }
}
