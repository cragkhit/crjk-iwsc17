package org.apache.catalina.ant;
import java.net.URLConnection;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.io.InputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.BufferedInputStream;
import java.net.URL;
import org.apache.tools.ant.BuildException;
import java.util.regex.Pattern;
public class DeployTask extends AbstractCatalinaCommandTask {
    private static final Pattern PROTOCOL_PATTERN;
    protected String config;
    protected String localWar;
    protected String tag;
    protected boolean update;
    protected String war;
    public DeployTask() {
        this.config = null;
        this.localWar = null;
        this.tag = null;
        this.update = false;
        this.war = null;
    }
    public String getConfig() {
        return this.config;
    }
    public void setConfig ( final String config ) {
        this.config = config;
    }
    public String getLocalWar() {
        return this.localWar;
    }
    public void setLocalWar ( final String localWar ) {
        this.localWar = localWar;
    }
    public String getTag() {
        return this.tag;
    }
    public void setTag ( final String tag ) {
        this.tag = tag;
    }
    public boolean getUpdate() {
        return this.update;
    }
    public void setUpdate ( final boolean update ) {
        this.update = update;
    }
    public String getWar() {
        return this.war;
    }
    public void setWar ( final String war ) {
        this.war = war;
    }
    @Override
    public void execute() throws BuildException {
        super.execute();
        if ( this.path == null ) {
            throw new BuildException ( "Must specify 'path' attribute" );
        }
        if ( this.war == null && this.localWar == null && this.config == null && this.tag == null ) {
            throw new BuildException ( "Must specify either 'war', 'localWar', 'config', or 'tag' attribute" );
        }
        BufferedInputStream stream = null;
        String contentType = null;
        long contentLength = -1L;
        if ( this.war != null ) {
            Label_0215: {
                if ( DeployTask.PROTOCOL_PATTERN.matcher ( this.war ).lookingAt() ) {
                    try {
                        final URL url = new URL ( this.war );
                        final URLConnection conn = url.openConnection();
                        contentLength = conn.getContentLengthLong();
                        stream = new BufferedInputStream ( conn.getInputStream(), 1024 );
                        break Label_0215;
                    } catch ( IOException e ) {
                        throw new BuildException ( ( Throwable ) e );
                    }
                }
                FileInputStream fsInput = null;
                try {
                    fsInput = new FileInputStream ( this.war );
                    contentLength = fsInput.getChannel().size();
                    stream = new BufferedInputStream ( fsInput, 1024 );
                } catch ( IOException e2 ) {
                    if ( fsInput != null ) {
                        try {
                            fsInput.close();
                        } catch ( IOException ex ) {}
                    }
                    throw new BuildException ( ( Throwable ) e2 );
                }
            }
            contentType = "application/octet-stream";
        }
        final StringBuilder sb = this.createQueryString ( "/deploy" );
        try {
            if ( this.war == null && this.config != null ) {
                sb.append ( "&config=" );
                sb.append ( URLEncoder.encode ( this.config, this.getCharset() ) );
            }
            if ( this.war == null && this.localWar != null ) {
                sb.append ( "&war=" );
                sb.append ( URLEncoder.encode ( this.localWar, this.getCharset() ) );
            }
            if ( this.update ) {
                sb.append ( "&update=true" );
            }
            if ( this.tag != null ) {
                sb.append ( "&tag=" );
                sb.append ( URLEncoder.encode ( this.tag, this.getCharset() ) );
            }
            this.execute ( sb.toString(), stream, contentType, contentLength );
        } catch ( UnsupportedEncodingException e3 ) {
            throw new BuildException ( "Invalid 'charset' attribute: " + this.getCharset() );
        } finally {
            if ( stream != null ) {
                try {
                    stream.close();
                } catch ( IOException ex2 ) {}
            }
        }
    }
    static {
        PROTOCOL_PATTERN = Pattern.compile ( "\\w{3,5}\\:" );
    }
}
