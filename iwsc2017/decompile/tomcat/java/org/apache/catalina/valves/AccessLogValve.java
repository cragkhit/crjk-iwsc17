package org.apache.catalina.valves;
import org.apache.juli.logging.LogFactory;
import org.apache.catalina.LifecycleException;
import java.nio.charset.Charset;
import java.io.BufferedWriter;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.FileOutputStream;
import java.nio.charset.StandardCharsets;
import java.io.UnsupportedEncodingException;
import org.apache.tomcat.util.buf.B2CConverter;
import java.io.IOException;
import java.io.Writer;
import java.io.CharArrayWriter;
import org.apache.tomcat.util.ExceptionUtils;
import java.util.Date;
import java.util.TimeZone;
import java.util.Locale;
import java.io.File;
import java.text.SimpleDateFormat;
import java.io.PrintWriter;
import org.apache.juli.logging.Log;
public class AccessLogValve extends AbstractAccessLogValve {
    private static final Log log;
    private volatile String dateStamp;
    private String directory;
    protected String prefix;
    protected boolean rotatable;
    protected boolean renameOnRotate;
    private boolean buffered;
    protected String suffix;
    protected PrintWriter writer;
    protected SimpleDateFormat fileDateFormatter;
    protected File currentLogFile;
    private volatile long rotationLastChecked;
    private boolean checkExists;
    protected String fileDateFormat;
    protected String encoding;
    public AccessLogValve() {
        this.dateStamp = "";
        this.directory = "logs";
        this.prefix = "access_log";
        this.rotatable = true;
        this.renameOnRotate = false;
        this.buffered = true;
        this.suffix = "";
        this.writer = null;
        this.fileDateFormatter = null;
        this.currentLogFile = null;
        this.rotationLastChecked = 0L;
        this.checkExists = false;
        this.fileDateFormat = ".yyyy-MM-dd";
        this.encoding = null;
    }
    public String getDirectory() {
        return this.directory;
    }
    public void setDirectory ( final String directory ) {
        this.directory = directory;
    }
    public boolean isCheckExists() {
        return this.checkExists;
    }
    public void setCheckExists ( final boolean checkExists ) {
        this.checkExists = checkExists;
    }
    public String getPrefix() {
        return this.prefix;
    }
    public void setPrefix ( final String prefix ) {
        this.prefix = prefix;
    }
    public boolean isRotatable() {
        return this.rotatable;
    }
    public void setRotatable ( final boolean rotatable ) {
        this.rotatable = rotatable;
    }
    public boolean isRenameOnRotate() {
        return this.renameOnRotate;
    }
    public void setRenameOnRotate ( final boolean renameOnRotate ) {
        this.renameOnRotate = renameOnRotate;
    }
    public boolean isBuffered() {
        return this.buffered;
    }
    public void setBuffered ( final boolean buffered ) {
        this.buffered = buffered;
    }
    public String getSuffix() {
        return this.suffix;
    }
    public void setSuffix ( final String suffix ) {
        this.suffix = suffix;
    }
    public String getFileDateFormat() {
        return this.fileDateFormat;
    }
    public void setFileDateFormat ( final String fileDateFormat ) {
        String newFormat;
        if ( fileDateFormat == null ) {
            newFormat = "";
        } else {
            newFormat = fileDateFormat;
        }
        this.fileDateFormat = newFormat;
        synchronized ( this ) {
            ( this.fileDateFormatter = new SimpleDateFormat ( newFormat, Locale.US ) ).setTimeZone ( TimeZone.getDefault() );
        }
    }
    public String getEncoding() {
        return this.encoding;
    }
    public void setEncoding ( final String encoding ) {
        if ( encoding != null && encoding.length() > 0 ) {
            this.encoding = encoding;
        } else {
            this.encoding = null;
        }
    }
    @Override
    public synchronized void backgroundProcess() {
        if ( this.getState().isAvailable() && this.getEnabled() && this.writer != null && this.buffered ) {
            this.writer.flush();
        }
    }
    public void rotate() {
        if ( this.rotatable ) {
            final long systime = System.currentTimeMillis();
            if ( systime - this.rotationLastChecked > 1000L ) {
                synchronized ( this ) {
                    if ( systime - this.rotationLastChecked > 1000L ) {
                        this.rotationLastChecked = systime;
                        final String tsDate = this.fileDateFormatter.format ( new Date ( systime ) );
                        if ( !this.dateStamp.equals ( tsDate ) ) {
                            this.close ( true );
                            this.dateStamp = tsDate;
                            this.open();
                        }
                    }
                }
            }
        }
    }
    public synchronized boolean rotate ( final String newFileName ) {
        if ( this.currentLogFile != null ) {
            final File holder = this.currentLogFile;
            this.close ( false );
            try {
                holder.renameTo ( new File ( newFileName ) );
            } catch ( Throwable e ) {
                ExceptionUtils.handleThrowable ( e );
                AccessLogValve.log.error ( AccessLogValve.sm.getString ( "accessLogValve.rotateFail" ), e );
            }
            this.dateStamp = this.fileDateFormatter.format ( new Date ( System.currentTimeMillis() ) );
            this.open();
            return true;
        }
        return false;
    }
    private File getLogFile ( final boolean useDateStamp ) {
        File dir = new File ( this.directory );
        if ( !dir.isAbsolute() ) {
            dir = new File ( this.getContainer().getCatalinaBase(), this.directory );
        }
        if ( !dir.mkdirs() && !dir.isDirectory() ) {
            AccessLogValve.log.error ( AccessLogValve.sm.getString ( "accessLogValve.openDirFail", dir ) );
        }
        File pathname;
        if ( useDateStamp ) {
            pathname = new File ( dir.getAbsoluteFile(), this.prefix + this.dateStamp + this.suffix );
        } else {
            pathname = new File ( dir.getAbsoluteFile(), this.prefix + this.suffix );
        }
        final File parent = pathname.getParentFile();
        if ( !parent.mkdirs() && !parent.isDirectory() ) {
            AccessLogValve.log.error ( AccessLogValve.sm.getString ( "accessLogValve.openDirFail", parent ) );
        }
        return pathname;
    }
    private void restore() {
        final File newLogFile = this.getLogFile ( false );
        final File rotatedLogFile = this.getLogFile ( true );
        if ( rotatedLogFile.exists() && !newLogFile.exists() && !rotatedLogFile.equals ( newLogFile ) ) {
            try {
                if ( !rotatedLogFile.renameTo ( newLogFile ) ) {
                    AccessLogValve.log.error ( AccessLogValve.sm.getString ( "accessLogValve.renameFail", rotatedLogFile, newLogFile ) );
                }
            } catch ( Throwable e ) {
                ExceptionUtils.handleThrowable ( e );
                AccessLogValve.log.error ( AccessLogValve.sm.getString ( "accessLogValve.renameFail", rotatedLogFile, newLogFile ), e );
            }
        }
    }
    private synchronized void close ( final boolean rename ) {
        if ( this.writer == null ) {
            return;
        }
        this.writer.flush();
        this.writer.close();
        if ( rename && this.renameOnRotate ) {
            final File newLogFile = this.getLogFile ( true );
            if ( !newLogFile.exists() ) {
                try {
                    if ( !this.currentLogFile.renameTo ( newLogFile ) ) {
                        AccessLogValve.log.error ( AccessLogValve.sm.getString ( "accessLogValve.renameFail", this.currentLogFile, newLogFile ) );
                    }
                } catch ( Throwable e ) {
                    ExceptionUtils.handleThrowable ( e );
                    AccessLogValve.log.error ( AccessLogValve.sm.getString ( "accessLogValve.renameFail", this.currentLogFile, newLogFile ), e );
                }
            } else {
                AccessLogValve.log.error ( AccessLogValve.sm.getString ( "accessLogValve.alreadyExists", this.currentLogFile, newLogFile ) );
            }
        }
        this.writer = null;
        this.dateStamp = "";
        this.currentLogFile = null;
    }
    public void log ( final CharArrayWriter message ) {
        this.rotate();
        if ( this.checkExists ) {
            synchronized ( this ) {
                if ( this.currentLogFile != null && !this.currentLogFile.exists() ) {
                    try {
                        this.close ( false );
                    } catch ( Throwable e ) {
                        ExceptionUtils.handleThrowable ( e );
                        AccessLogValve.log.info ( AccessLogValve.sm.getString ( "accessLogValve.closeFail" ), e );
                    }
                    this.dateStamp = this.fileDateFormatter.format ( new Date ( System.currentTimeMillis() ) );
                    this.open();
                }
            }
        }
        try {
            synchronized ( this ) {
                if ( this.writer != null ) {
                    message.writeTo ( this.writer );
                    this.writer.println ( "" );
                    if ( !this.buffered ) {
                        this.writer.flush();
                    }
                }
            }
        } catch ( IOException ioe ) {
            AccessLogValve.log.warn ( AccessLogValve.sm.getString ( "accessLogValve.writeFail", message.toString() ), ioe );
        }
    }
    protected synchronized void open() {
        final File pathname = this.getLogFile ( this.rotatable && !this.renameOnRotate );
        Charset charset = null;
        if ( this.encoding != null ) {
            try {
                charset = B2CConverter.getCharset ( this.encoding );
            } catch ( UnsupportedEncodingException ex ) {
                AccessLogValve.log.error ( AccessLogValve.sm.getString ( "accessLogValve.unsupportedEncoding", this.encoding ), ex );
            }
        }
        if ( charset == null ) {
            charset = StandardCharsets.ISO_8859_1;
        }
        try {
            this.writer = new PrintWriter ( new BufferedWriter ( new OutputStreamWriter ( new FileOutputStream ( pathname, true ), charset ), 128000 ), false );
            this.currentLogFile = pathname;
        } catch ( IOException e ) {
            this.writer = null;
            this.currentLogFile = null;
            AccessLogValve.log.error ( AccessLogValve.sm.getString ( "accessLogValve.openFail", pathname ), e );
        }
    }
    @Override
    protected synchronized void startInternal() throws LifecycleException {
        final String format = this.getFileDateFormat();
        ( this.fileDateFormatter = new SimpleDateFormat ( format, Locale.US ) ).setTimeZone ( TimeZone.getDefault() );
        this.dateStamp = this.fileDateFormatter.format ( new Date ( System.currentTimeMillis() ) );
        if ( this.rotatable && this.renameOnRotate ) {
            this.restore();
        }
        this.open();
        super.startInternal();
    }
    @Override
    protected synchronized void stopInternal() throws LifecycleException {
        super.stopInternal();
        this.close ( false );
    }
    static {
        log = LogFactory.getLog ( AccessLogValve.class );
    }
}
