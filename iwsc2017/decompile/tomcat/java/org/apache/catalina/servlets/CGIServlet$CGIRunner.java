package org.apache.catalina.servlets;
import java.util.List;
import java.io.Reader;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import org.apache.catalina.util.IOTools;
import java.io.BufferedOutputStream;
import java.util.Collection;
import java.io.IOException;
import java.util.Enumeration;
import java.util.Vector;
import javax.servlet.http.HttpServletResponse;
import java.io.InputStream;
import java.util.ArrayList;
import java.io.File;
import java.util.Hashtable;
protected class CGIRunner {
    private final String command;
    private final Hashtable<String, String> env;
    private final File wd;
    private final ArrayList<String> params;
    private InputStream stdin;
    private HttpServletResponse response;
    private boolean readyToRun;
    protected CGIRunner ( final String command, final Hashtable<String, String> env, final File wd, final ArrayList<String> params ) {
        this.stdin = null;
        this.response = null;
        this.readyToRun = false;
        this.command = command;
        this.env = env;
        this.wd = wd;
        this.params = params;
        this.updateReadyStatus();
    }
    protected void updateReadyStatus() {
        if ( this.command != null && this.env != null && this.wd != null && this.params != null && this.response != null ) {
            this.readyToRun = true;
        } else {
            this.readyToRun = false;
        }
    }
    protected boolean isReady() {
        return this.readyToRun;
    }
    protected void setResponse ( final HttpServletResponse response ) {
        this.response = response;
        this.updateReadyStatus();
    }
    protected void setInput ( final InputStream stdin ) {
        this.stdin = stdin;
        this.updateReadyStatus();
    }
    protected String[] hashToStringArray ( final Hashtable<String, ?> h ) throws NullPointerException {
        final Vector<String> v = new Vector<String>();
        final Enumeration<String> e = h.keys();
        while ( e.hasMoreElements() ) {
            final String k = e.nextElement();
            v.add ( k + "=" + h.get ( k ).toString() );
        }
        final String[] strArr = new String[v.size()];
        v.copyInto ( strArr );
        return strArr;
    }
    protected void run() throws IOException {
        if ( !this.isReady() ) {
            throw new IOException ( this.getClass().getName() + ": not ready to run." );
        }
        if ( CGIServlet.access$100().isDebugEnabled() ) {
            CGIServlet.access$100().debug ( "envp: [" + this.env + "], command: [" + this.command + "]" );
        }
        if ( this.command.indexOf ( File.separator + "." + File.separator ) >= 0 || this.command.indexOf ( File.separator + ".." ) >= 0 || this.command.indexOf ( ".." + File.separator ) >= 0 ) {
            throw new IOException ( this.getClass().getName() + "Illegal Character in CGI command path ('.' or '..') detected.  Not running CGI [" + this.command + "]." );
        }
        Runtime rt = null;
        BufferedReader cgiHeaderReader = null;
        InputStream cgiOutput = null;
        BufferedReader commandsStdErr = null;
        Thread errReaderThread = null;
        BufferedOutputStream commandsStdIn = null;
        Process proc = null;
        int bufRead = -1;
        final List<String> cmdAndArgs = new ArrayList<String>();
        if ( CGIServlet.access$700 ( CGIServlet.this ).length() != 0 ) {
            cmdAndArgs.add ( CGIServlet.access$700 ( CGIServlet.this ) );
        }
        if ( CGIServlet.access$800 ( CGIServlet.this ) != null ) {
            cmdAndArgs.addAll ( CGIServlet.access$800 ( CGIServlet.this ) );
        }
        cmdAndArgs.add ( this.command );
        cmdAndArgs.addAll ( this.params );
        try {
            rt = Runtime.getRuntime();
            proc = rt.exec ( cmdAndArgs.toArray ( new String[cmdAndArgs.size()] ), this.hashToStringArray ( this.env ), this.wd );
            final String sContentLength = this.env.get ( "CONTENT_LENGTH" );
            if ( !"".equals ( sContentLength ) ) {
                commandsStdIn = new BufferedOutputStream ( proc.getOutputStream() );
                IOTools.flow ( this.stdin, commandsStdIn );
                commandsStdIn.flush();
                commandsStdIn.close();
            }
            boolean isRunning = true;
            final BufferedReader stdErrRdr;
            commandsStdErr = ( stdErrRdr = new BufferedReader ( new InputStreamReader ( proc.getErrorStream() ) ) );
            errReaderThread = new Thread() {
                @Override
                public void run() {
                    CGIRunner.this.sendToLog ( stdErrRdr );
                }
            };
            errReaderThread.start();
            final InputStream cgiHeaderStream = new HTTPHeaderInputStream ( proc.getInputStream() );
            cgiHeaderReader = new BufferedReader ( new InputStreamReader ( cgiHeaderStream ) );
            boolean skipBody = false;
            while ( isRunning ) {
                try {
                    String line = null;
                    while ( ( line = cgiHeaderReader.readLine() ) != null && !"".equals ( line ) ) {
                        if ( CGIServlet.access$100().isTraceEnabled() ) {
                            CGIServlet.access$100().trace ( "addHeader(\"" + line + "\")" );
                        }
                        if ( line.startsWith ( "HTTP" ) ) {
                            skipBody = CGIServlet.access$1000 ( CGIServlet.this, this.response, this.getSCFromHttpStatusLine ( line ) );
                        } else if ( line.indexOf ( 58 ) >= 0 ) {
                            final String header = line.substring ( 0, line.indexOf ( 58 ) ).trim();
                            final String value = line.substring ( line.indexOf ( 58 ) + 1 ).trim();
                            if ( header.equalsIgnoreCase ( "status" ) ) {
                                skipBody = CGIServlet.access$1000 ( CGIServlet.this, this.response, this.getSCFromCGIStatusHeader ( value ) );
                            } else {
                                this.response.addHeader ( header, value );
                            }
                        } else {
                            CGIServlet.access$100().info ( CGIServlet.access$200().getString ( "cgiServlet.runBadHeader", line ) );
                        }
                    }
                    final byte[] bBuf = new byte[2048];
                    final OutputStream out = ( OutputStream ) this.response.getOutputStream();
                    cgiOutput = proc.getInputStream();
                    try {
                        while ( !skipBody && ( bufRead = cgiOutput.read ( bBuf ) ) != -1 ) {
                            if ( CGIServlet.access$100().isTraceEnabled() ) {
                                CGIServlet.access$100().trace ( "output " + bufRead + " bytes of data" );
                            }
                            out.write ( bBuf, 0, bufRead );
                        }
                    } finally {
                        if ( bufRead != -1 ) {
                            while ( ( bufRead = cgiOutput.read ( bBuf ) ) != -1 ) {}
                        }
                    }
                    proc.exitValue();
                    isRunning = false;
                } catch ( IllegalThreadStateException e2 ) {
                    try {
                        Thread.sleep ( 500L );
                    } catch ( InterruptedException ex ) {}
                }
            }
        } catch ( IOException e ) {
            CGIServlet.access$100().warn ( CGIServlet.access$200().getString ( "cgiServlet.runFail" ), e );
            throw e;
        } finally {
            if ( cgiHeaderReader != null ) {
                try {
                    cgiHeaderReader.close();
                } catch ( IOException ioe ) {
                    CGIServlet.access$100().warn ( CGIServlet.access$200().getString ( "cgiServlet.runHeaderReaderFail" ), ioe );
                }
            }
            if ( cgiOutput != null ) {
                try {
                    cgiOutput.close();
                } catch ( IOException ioe ) {
                    CGIServlet.access$100().warn ( CGIServlet.access$200().getString ( "cgiServlet.runOutputStreamFail" ), ioe );
                }
            }
            if ( errReaderThread != null ) {
                try {
                    errReaderThread.join ( CGIServlet.access$1100 ( CGIServlet.this ) );
                } catch ( InterruptedException e3 ) {
                    CGIServlet.access$100().warn ( CGIServlet.access$200().getString ( "cgiServlet.runReaderInterupt" ) );
                }
            }
            if ( proc != null ) {
                proc.destroy();
                proc = null;
            }
        }
    }
    private int getSCFromHttpStatusLine ( final String line ) {
        final int statusStart = line.indexOf ( 32 ) + 1;
        if ( statusStart < 1 || line.length() < statusStart + 3 ) {
            CGIServlet.access$100().warn ( CGIServlet.access$200().getString ( "cgiServlet.runInvalidStatus", line ) );
            return 500;
        }
        final String status = line.substring ( statusStart, statusStart + 3 );
        int statusCode;
        try {
            statusCode = Integer.parseInt ( status );
        } catch ( NumberFormatException nfe ) {
            CGIServlet.access$100().warn ( CGIServlet.access$200().getString ( "cgiServlet.runInvalidStatus", status ) );
            return 500;
        }
        return statusCode;
    }
    private int getSCFromCGIStatusHeader ( final String value ) {
        if ( value.length() < 3 ) {
            CGIServlet.access$100().warn ( CGIServlet.access$200().getString ( "cgiServlet.runInvalidStatus", value ) );
            return 500;
        }
        final String status = value.substring ( 0, 3 );
        int statusCode;
        try {
            statusCode = Integer.parseInt ( status );
        } catch ( NumberFormatException nfe ) {
            CGIServlet.access$100().warn ( CGIServlet.access$200().getString ( "cgiServlet.runInvalidStatus", status ) );
            return 500;
        }
        return statusCode;
    }
    private void sendToLog ( final BufferedReader rdr ) {
        String line = null;
        int lineCount = 0;
        try {
            while ( ( line = rdr.readLine() ) != null ) {
                CGIServlet.access$100().warn ( CGIServlet.access$200().getString ( "cgiServlet.runStdErr", line ) );
                ++lineCount;
            }
        } catch ( IOException e ) {
            CGIServlet.access$100().warn ( CGIServlet.access$200().getString ( "cgiServlet.runStdErrFail" ), e );
            try {
                rdr.close();
            } catch ( IOException e ) {
                CGIServlet.access$100().warn ( CGIServlet.access$200().getString ( "cgiServlet.runStdErrFail" ), e );
            }
        } finally {
            try {
                rdr.close();
            } catch ( IOException e2 ) {
                CGIServlet.access$100().warn ( CGIServlet.access$200().getString ( "cgiServlet.runStdErrFail" ), e2 );
            }
        }
        if ( lineCount > 0 ) {
            CGIServlet.access$100().warn ( CGIServlet.access$200().getString ( "cgiServlet.runStdErrCount", lineCount ) );
        }
    }
}
