package org.apache.catalina.ant;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.net.URLConnection;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.BufferedOutputStream;
import org.apache.tomcat.util.codec.binary.Base64;
import java.nio.charset.StandardCharsets;
import java.net.HttpURLConnection;
import java.net.URL;
import java.io.InputStream;
import org.apache.tools.ant.BuildException;
public abstract class AbstractCatalinaTask extends BaseRedirectorHelperTask {
    private static final String CHARSET = "utf-8";
    protected String charset;
    protected String password;
    protected String url;
    protected String username;
    protected boolean ignoreResponseConstraint;
    public AbstractCatalinaTask() {
        this.charset = "ISO-8859-1";
        this.password = null;
        this.url = "http://localhost:8080/manager/text";
        this.username = null;
        this.ignoreResponseConstraint = false;
    }
    public String getCharset() {
        return this.charset;
    }
    public void setCharset ( final String charset ) {
        this.charset = charset;
    }
    public String getPassword() {
        return this.password;
    }
    public void setPassword ( final String password ) {
        this.password = password;
    }
    public String getUrl() {
        return this.url;
    }
    public void setUrl ( final String url ) {
        this.url = url;
    }
    public String getUsername() {
        return this.username;
    }
    public void setUsername ( final String username ) {
        this.username = username;
    }
    public boolean isIgnoreResponseConstraint() {
        return this.ignoreResponseConstraint;
    }
    public void setIgnoreResponseConstraint ( final boolean ignoreResponseConstraint ) {
        this.ignoreResponseConstraint = ignoreResponseConstraint;
    }
    public void execute() throws BuildException {
        if ( this.username == null || this.password == null || this.url == null ) {
            throw new BuildException ( "Must specify all of 'username', 'password', and 'url'" );
        }
    }
    public void execute ( final String command ) throws BuildException {
        this.execute ( command, null, null, -1L );
    }
    public void execute ( final String command, final InputStream istream, final String contentType, final long contentLength ) throws BuildException {
        URLConnection conn = null;
        InputStreamReader reader = null;
        try {
            conn = new URL ( this.url + command ).openConnection();
            final HttpURLConnection hconn = ( HttpURLConnection ) conn;
            hconn.setAllowUserInteraction ( false );
            hconn.setDoInput ( true );
            hconn.setUseCaches ( false );
            if ( istream != null ) {
                hconn.setDoOutput ( true );
                hconn.setRequestMethod ( "PUT" );
                if ( contentType != null ) {
                    hconn.setRequestProperty ( "Content-Type", contentType );
                }
                if ( contentLength >= 0L ) {
                    hconn.setRequestProperty ( "Content-Length", "" + contentLength );
                    hconn.setFixedLengthStreamingMode ( contentLength );
                }
            } else {
                hconn.setDoOutput ( false );
                hconn.setRequestMethod ( "GET" );
            }
            hconn.setRequestProperty ( "User-Agent", "Catalina-Ant-Task/1.0" );
            final String input = this.username + ":" + this.password;
            final String output = Base64.encodeBase64String ( input.getBytes ( StandardCharsets.ISO_8859_1 ) );
            hconn.setRequestProperty ( "Authorization", "Basic " + output );
            hconn.connect();
            if ( istream != null ) {
                try ( final BufferedOutputStream ostream = new BufferedOutputStream ( hconn.getOutputStream(), 1024 ) ) {
                    final byte[] buffer = new byte[1024];
                    while ( true ) {
                        final int n = istream.read ( buffer );
                        if ( n < 0 ) {
                            break;
                        }
                        ostream.write ( buffer, 0, n );
                    }
                    ostream.flush();
                } finally {
                    try {
                        istream.close();
                    } catch ( Exception ex ) {}
                }
            }
            reader = new InputStreamReader ( hconn.getInputStream(), "utf-8" );
            final StringBuilder buff = new StringBuilder();
            String error = null;
            int msgPriority = 2;
            boolean first = true;
            while ( true ) {
                final int ch = reader.read();
                if ( ch < 0 ) {
                    break;
                }
                if ( ch == 13 || ch == 10 ) {
                    if ( buff.length() <= 0 ) {
                        continue;
                    }
                    final String line = buff.toString();
                    buff.setLength ( 0 );
                    if ( !this.ignoreResponseConstraint && first ) {
                        if ( !line.startsWith ( "OK -" ) ) {
                            error = line;
                            msgPriority = 0;
                        }
                        first = false;
                    }
                    this.handleOutput ( line, msgPriority );
                } else {
                    buff.append ( ( char ) ch );
                }
            }
            if ( buff.length() > 0 ) {
                this.handleOutput ( buff.toString(), msgPriority );
            }
            if ( error != null && this.isFailOnError() ) {
                throw new BuildException ( error );
            }
        } catch ( Exception e ) {
            if ( this.isFailOnError() ) {
                throw new BuildException ( ( Throwable ) e );
            }
            this.handleErrorOutput ( e.getMessage() );
        } finally {
            this.closeRedirector();
            if ( reader != null ) {
                try {
                    reader.close();
                } catch ( IOException ex2 ) {}
                reader = null;
            }
            if ( istream != null ) {
                try {
                    istream.close();
                } catch ( IOException ex3 ) {}
            }
        }
    }
}
