package org.apache.catalina.ant;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.StandardCharsets;
import org.apache.tomcat.util.codec.binary.Base64;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Project;
public abstract class AbstractCatalinaTask extends BaseRedirectorHelperTask {
    private static final String CHARSET = "utf-8";
    protected String charset = "ISO-8859-1";
    public String getCharset() {
        return ( this.charset );
    }
    public void setCharset ( String charset ) {
        this.charset = charset;
    }
    protected String password = null;
    public String getPassword() {
        return ( this.password );
    }
    public void setPassword ( String password ) {
        this.password = password;
    }
    protected String url = "http://localhost:8080/manager/text";
    public String getUrl() {
        return ( this.url );
    }
    public void setUrl ( String url ) {
        this.url = url;
    }
    protected String username = null;
    public String getUsername() {
        return ( this.username );
    }
    public void setUsername ( String username ) {
        this.username = username;
    }
    protected boolean ignoreResponseConstraint = false;
    public boolean isIgnoreResponseConstraint() {
        return ignoreResponseConstraint;
    }
    public void setIgnoreResponseConstraint ( boolean ignoreResponseConstraint ) {
        this.ignoreResponseConstraint = ignoreResponseConstraint;
    }
    @Override
    public void execute() throws BuildException {
        if ( ( username == null ) || ( password == null ) || ( url == null ) ) {
            throw new BuildException
            ( "Must specify all of 'username', 'password', and 'url'" );
        }
    }
    public void execute ( String command ) throws BuildException {
        execute ( command, null, null, -1 );
    }
    public void execute ( String command, InputStream istream,
                          String contentType, long contentLength )
    throws BuildException {
        URLConnection conn = null;
        InputStreamReader reader = null;
        try {
            conn = ( new URL ( url + command ) ).openConnection();
            HttpURLConnection hconn = ( HttpURLConnection ) conn;
            hconn.setAllowUserInteraction ( false );
            hconn.setDoInput ( true );
            hconn.setUseCaches ( false );
            if ( istream != null ) {
                hconn.setDoOutput ( true );
                hconn.setRequestMethod ( "PUT" );
                if ( contentType != null ) {
                    hconn.setRequestProperty ( "Content-Type", contentType );
                }
                if ( contentLength >= 0 ) {
                    hconn.setRequestProperty ( "Content-Length",
                                               "" + contentLength );
                    hconn.setFixedLengthStreamingMode ( contentLength );
                }
            } else {
                hconn.setDoOutput ( false );
                hconn.setRequestMethod ( "GET" );
            }
            hconn.setRequestProperty ( "User-Agent",
                                       "Catalina-Ant-Task/1.0" );
            String input = username + ":" + password;
            String output = Base64.encodeBase64String (
                                input.getBytes ( StandardCharsets.ISO_8859_1 ) );
            hconn.setRequestProperty ( "Authorization",
                                       "Basic " + output );
            hconn.connect();
            if ( istream != null ) {
                try ( BufferedOutputStream ostream =
                                new BufferedOutputStream ( hconn.getOutputStream(), 1024 ); ) {
                    byte buffer[] = new byte[1024];
                    while ( true ) {
                        int n = istream.read ( buffer );
                        if ( n < 0 ) {
                            break;
                        }
                        ostream.write ( buffer, 0, n );
                    }
                    ostream.flush();
                } finally {
                    try {
                        istream.close();
                    } catch ( Exception e ) {
                    }
                }
            }
            reader = new InputStreamReader ( hconn.getInputStream(), CHARSET );
            StringBuilder buff = new StringBuilder();
            String error = null;
            int msgPriority = Project.MSG_INFO;
            boolean first = true;
            while ( true ) {
                int ch = reader.read();
                if ( ch < 0 ) {
                    break;
                } else if ( ( ch == '\r' ) || ( ch == '\n' ) ) {
                    if ( buff.length() > 0 ) {
                        String line = buff.toString();
                        buff.setLength ( 0 );
                        if ( !ignoreResponseConstraint && first ) {
                            if ( !line.startsWith ( "OK -" ) ) {
                                error = line;
                                msgPriority = Project.MSG_ERR;
                            }
                            first = false;
                        }
                        handleOutput ( line, msgPriority );
                    }
                } else {
                    buff.append ( ( char ) ch );
                }
            }
            if ( buff.length() > 0 ) {
                handleOutput ( buff.toString(), msgPriority );
            }
            if ( error != null && isFailOnError() ) {
                throw new BuildException ( error );
            }
        } catch ( Exception e ) {
            if ( isFailOnError() ) {
                throw new BuildException ( e );
            } else {
                handleErrorOutput ( e.getMessage() );
            }
        } finally {
            closeRedirector();
            if ( reader != null ) {
                try {
                    reader.close();
                } catch ( IOException ioe ) {
                }
                reader = null;
            }
            if ( istream != null ) {
                try {
                    istream.close();
                } catch ( IOException ioe ) {
                }
            }
        }
    }
}
