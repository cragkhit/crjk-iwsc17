package org.apache.catalina.storeconfig;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.sql.Timestamp;
public class StoreFileMover {
    private String filename = "conf/server.xml";
    private String encoding = "UTF-8";
    private String basename = System.getProperty ( "catalina.base" );
    private File configOld;
    private File configNew;
    private File configSave;
    public File getConfigNew() {
        return configNew;
    }
    public File getConfigOld() {
        return configOld;
    }
    public File getConfigSave() {
        return configSave;
    }
    public String getBasename() {
        return basename;
    }
    public void setBasename ( String basename ) {
        this.basename = basename;
    }
    public String getFilename() {
        return filename;
    }
    public void setFilename ( String string ) {
        filename = string;
    }
    public String getEncoding() {
        return encoding;
    }
    public void setEncoding ( String string ) {
        encoding = string;
    }
    public StoreFileMover ( String basename, String filename, String encoding ) {
        setBasename ( basename );
        setEncoding ( encoding );
        setFilename ( filename );
        init();
    }
    public StoreFileMover() {
        init();
    }
    public void init() {
        String configFile = getFilename();
        configOld = new File ( configFile );
        if ( !configOld.isAbsolute() ) {
            configOld = new File ( getBasename(), configFile );
        }
        configNew = new File ( configFile + ".new" );
        if ( !configNew.isAbsolute() ) {
            configNew = new File ( getBasename(), configFile + ".new" );
        }
        if ( !configNew.getParentFile().exists() ) {
            configNew.getParentFile().mkdirs();
        }
        String sb = getTimeTag();
        configSave = new File ( configFile + sb );
        if ( !configSave.isAbsolute() ) {
            configSave = new File ( getBasename(), configFile + sb );
        }
    }
    public void move() throws IOException {
        if ( configOld.renameTo ( configSave ) ) {
            if ( !configNew.renameTo ( configOld ) ) {
                configSave.renameTo ( configOld );
                throw new IOException ( "Cannot rename "
                                        + configNew.getAbsolutePath() + " to "
                                        + configOld.getAbsolutePath() );
            }
        } else {
            if ( !configOld.exists() ) {
                if ( !configNew.renameTo ( configOld ) ) {
                    throw new IOException ( "Cannot move "
                                            + configNew.getAbsolutePath() + " to "
                                            + configOld.getAbsolutePath() );
                }
            } else {
                throw new IOException ( "Cannot rename "
                                        + configOld.getAbsolutePath() + " to "
                                        + configSave.getAbsolutePath() );
            }
        }
    }
    public PrintWriter getWriter() throws IOException {
        return new PrintWriter ( new OutputStreamWriter (
                                     new FileOutputStream ( configNew ), getEncoding() ) );
    }
    protected String getTimeTag() {
        String ts = ( new Timestamp ( System.currentTimeMillis() ) ).toString();
        StringBuffer sb = new StringBuffer ( "." );
        sb.append ( ts.substring ( 0, 10 ) );
        sb.append ( '.' );
        sb.append ( ts.substring ( 11, 13 ) );
        sb.append ( '-' );
        sb.append ( ts.substring ( 14, 16 ) );
        sb.append ( '-' );
        sb.append ( ts.substring ( 17, 19 ) );
        return sb.toString();
    }
}
