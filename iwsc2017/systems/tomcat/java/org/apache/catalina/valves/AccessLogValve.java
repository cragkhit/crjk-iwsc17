package org.apache.catalina.valves;
import java.io.BufferedWriter;
import java.io.CharArrayWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;
import org.apache.catalina.LifecycleException;
import org.apache.juli.logging.Log;
import org.apache.juli.logging.LogFactory;
import org.apache.tomcat.util.ExceptionUtils;
import org.apache.tomcat.util.buf.B2CConverter;
public class AccessLogValve extends AbstractAccessLogValve {
    private static final Log log = LogFactory.getLog ( AccessLogValve.class );
    public AccessLogValve() {
        super();
    }
    private volatile String dateStamp = "";
    private String directory = "logs";
    protected String prefix = "access_log";
    protected boolean rotatable = true;
    protected boolean renameOnRotate = false;
    private boolean buffered = true;
    protected String suffix = "";
    protected PrintWriter writer = null;
    protected SimpleDateFormat fileDateFormatter = null;
    protected File currentLogFile = null;
    private volatile long rotationLastChecked = 0L;
    private boolean checkExists = false;
    protected String fileDateFormat = ".yyyy-MM-dd";
    protected String encoding = null;
    public String getDirectory() {
        return ( directory );
    }
    public void setDirectory ( String directory ) {
        this.directory = directory;
    }
    public boolean isCheckExists() {
        return checkExists;
    }
    public void setCheckExists ( boolean checkExists ) {
        this.checkExists = checkExists;
    }
    public String getPrefix() {
        return ( prefix );
    }
    public void setPrefix ( String prefix ) {
        this.prefix = prefix;
    }
    public boolean isRotatable() {
        return rotatable;
    }
    public void setRotatable ( boolean rotatable ) {
        this.rotatable = rotatable;
    }
    public boolean isRenameOnRotate() {
        return renameOnRotate;
    }
    public void setRenameOnRotate ( boolean renameOnRotate ) {
        this.renameOnRotate = renameOnRotate;
    }
    public boolean isBuffered() {
        return buffered;
    }
    public void setBuffered ( boolean buffered ) {
        this.buffered = buffered;
    }
    public String getSuffix() {
        return ( suffix );
    }
    public void setSuffix ( String suffix ) {
        this.suffix = suffix;
    }
    public String getFileDateFormat() {
        return fileDateFormat;
    }
    public void setFileDateFormat ( String fileDateFormat ) {
        String newFormat;
        if ( fileDateFormat == null ) {
            newFormat = "";
        } else {
            newFormat = fileDateFormat;
        }
        this.fileDateFormat = newFormat;
        synchronized ( this ) {
            fileDateFormatter = new SimpleDateFormat ( newFormat, Locale.US );
            fileDateFormatter.setTimeZone ( TimeZone.getDefault() );
        }
    }
    public String getEncoding() {
        return encoding;
    }
    public void setEncoding ( String encoding ) {
        if ( encoding != null && encoding.length() > 0 ) {
            this.encoding = encoding;
        } else {
            this.encoding = null;
        }
    }
    @Override
    public synchronized void backgroundProcess() {
        if ( getState().isAvailable() && getEnabled() && writer != null &&
                buffered ) {
            writer.flush();
        }
    }
    public void rotate() {
        if ( rotatable ) {
            long systime = System.currentTimeMillis();
            if ( ( systime - rotationLastChecked ) > 1000 ) {
                synchronized ( this ) {
                    if ( ( systime - rotationLastChecked ) > 1000 ) {
                        rotationLastChecked = systime;
                        String tsDate;
                        tsDate = fileDateFormatter.format ( new Date ( systime ) );
                        if ( !dateStamp.equals ( tsDate ) ) {
                            close ( true );
                            dateStamp = tsDate;
                            open();
                        }
                    }
                }
            }
        }
    }
    public synchronized boolean rotate ( String newFileName ) {
        if ( currentLogFile != null ) {
            File holder = currentLogFile;
            close ( false );
            try {
                holder.renameTo ( new File ( newFileName ) );
            } catch ( Throwable e ) {
                ExceptionUtils.handleThrowable ( e );
                log.error ( sm.getString ( "accessLogValve.rotateFail" ), e );
            }
            dateStamp = fileDateFormatter.format (
                            new Date ( System.currentTimeMillis() ) );
            open();
            return true;
        } else {
            return false;
        }
    }
    private File getLogFile ( boolean useDateStamp ) {
        File dir = new File ( directory );
        if ( !dir.isAbsolute() ) {
            dir = new File ( getContainer().getCatalinaBase(), directory );
        }
        if ( !dir.mkdirs() && !dir.isDirectory() ) {
            log.error ( sm.getString ( "accessLogValve.openDirFail", dir ) );
        }
        File pathname;
        if ( useDateStamp ) {
            pathname = new File ( dir.getAbsoluteFile(), prefix + dateStamp
                                  + suffix );
        } else {
            pathname = new File ( dir.getAbsoluteFile(), prefix + suffix );
        }
        File parent = pathname.getParentFile();
        if ( !parent.mkdirs() && !parent.isDirectory() ) {
            log.error ( sm.getString ( "accessLogValve.openDirFail", parent ) );
        }
        return pathname;
    }
    private void restore() {
        File newLogFile = getLogFile ( false );
        File rotatedLogFile = getLogFile ( true );
        if ( rotatedLogFile.exists() && !newLogFile.exists() &&
                !rotatedLogFile.equals ( newLogFile ) ) {
            try {
                if ( !rotatedLogFile.renameTo ( newLogFile ) ) {
                    log.error ( sm.getString ( "accessLogValve.renameFail", rotatedLogFile, newLogFile ) );
                }
            } catch ( Throwable e ) {
                ExceptionUtils.handleThrowable ( e );
                log.error ( sm.getString ( "accessLogValve.renameFail", rotatedLogFile, newLogFile ), e );
            }
        }
    }
    private synchronized void close ( boolean rename ) {
        if ( writer == null ) {
            return;
        }
        writer.flush();
        writer.close();
        if ( rename && renameOnRotate ) {
            File newLogFile = getLogFile ( true );
            if ( !newLogFile.exists() ) {
                try {
                    if ( !currentLogFile.renameTo ( newLogFile ) ) {
                        log.error ( sm.getString ( "accessLogValve.renameFail", currentLogFile, newLogFile ) );
                    }
                } catch ( Throwable e ) {
                    ExceptionUtils.handleThrowable ( e );
                    log.error ( sm.getString ( "accessLogValve.renameFail", currentLogFile, newLogFile ), e );
                }
            } else {
                log.error ( sm.getString ( "accessLogValve.alreadyExists", currentLogFile, newLogFile ) );
            }
        }
        writer = null;
        dateStamp = "";
        currentLogFile = null;
    }
    @Override
    public void log ( CharArrayWriter message ) {
        rotate();
        if ( checkExists ) {
            synchronized ( this ) {
                if ( currentLogFile != null && !currentLogFile.exists() ) {
                    try {
                        close ( false );
                    } catch ( Throwable e ) {
                        ExceptionUtils.handleThrowable ( e );
                        log.info ( sm.getString ( "accessLogValve.closeFail" ), e );
                    }
                    dateStamp = fileDateFormatter.format (
                                    new Date ( System.currentTimeMillis() ) );
                    open();
                }
            }
        }
        try {
            synchronized ( this ) {
                if ( writer != null ) {
                    message.writeTo ( writer );
                    writer.println ( "" );
                    if ( !buffered ) {
                        writer.flush();
                    }
                }
            }
        } catch ( IOException ioe ) {
            log.warn ( sm.getString (
                           "accessLogValve.writeFail", message.toString() ), ioe );
        }
    }
    protected synchronized void open() {
        File pathname = getLogFile ( rotatable && !renameOnRotate );
        Charset charset = null;
        if ( encoding != null ) {
            try {
                charset = B2CConverter.getCharset ( encoding );
            } catch ( UnsupportedEncodingException ex ) {
                log.error ( sm.getString (
                                "accessLogValve.unsupportedEncoding", encoding ), ex );
            }
        }
        if ( charset == null ) {
            charset = StandardCharsets.ISO_8859_1;
        }
        try {
            writer = new PrintWriter ( new BufferedWriter ( new OutputStreamWriter (
                                           new FileOutputStream ( pathname, true ), charset ), 128000 ),
                                       false );
            currentLogFile = pathname;
        } catch ( IOException e ) {
            writer = null;
            currentLogFile = null;
            log.error ( sm.getString ( "accessLogValve.openFail", pathname ), e );
        }
    }
    @Override
    protected synchronized void startInternal() throws LifecycleException {
        String format = getFileDateFormat();
        fileDateFormatter = new SimpleDateFormat ( format, Locale.US );
        fileDateFormatter.setTimeZone ( TimeZone.getDefault() );
        dateStamp = fileDateFormatter.format ( new Date ( System.currentTimeMillis() ) );
        if ( rotatable && renameOnRotate ) {
            restore();
        }
        open();
        super.startInternal();
    }
    @Override
    protected synchronized void stopInternal() throws LifecycleException {
        super.stopInternal();
        close ( false );
    }
}
