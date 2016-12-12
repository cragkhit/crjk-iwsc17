package org.apache.catalina.ant;
import java.io.IOException;
import org.apache.tools.ant.BuildException;
import java.io.File;
import java.io.PrintStream;
import java.io.OutputStream;
import org.apache.tools.ant.types.RedirectorElement;
import org.apache.tools.ant.taskdefs.Redirector;
import org.apache.tools.ant.Task;
public abstract class BaseRedirectorHelperTask extends Task {
    protected final Redirector redirector;
    protected RedirectorElement redirectorElement;
    protected OutputStream redirectOutStream;
    protected OutputStream redirectErrStream;
    PrintStream redirectOutPrintStream;
    PrintStream redirectErrPrintStream;
    protected boolean failOnError;
    protected boolean redirectOutput;
    protected boolean redirectorConfigured;
    protected boolean alwaysLog;
    public BaseRedirectorHelperTask() {
        this.redirector = new Redirector ( ( Task ) this );
        this.redirectorElement = null;
        this.redirectOutStream = null;
        this.redirectErrStream = null;
        this.redirectOutPrintStream = null;
        this.redirectErrPrintStream = null;
        this.failOnError = true;
        this.redirectOutput = false;
        this.redirectorConfigured = false;
        this.alwaysLog = false;
    }
    public void setFailonerror ( final boolean fail ) {
        this.failOnError = fail;
    }
    public boolean isFailOnError() {
        return this.failOnError;
    }
    public void setOutput ( final File out ) {
        this.redirector.setOutput ( out );
        this.redirectOutput = true;
    }
    public void setError ( final File error ) {
        this.redirector.setError ( error );
        this.redirectOutput = true;
    }
    public void setLogError ( final boolean logError ) {
        this.redirector.setLogError ( logError );
        this.redirectOutput = true;
    }
    public void setOutputproperty ( final String outputProperty ) {
        this.redirector.setOutputProperty ( outputProperty );
        this.redirectOutput = true;
    }
    public void setErrorProperty ( final String errorProperty ) {
        this.redirector.setErrorProperty ( errorProperty );
        this.redirectOutput = true;
    }
    public void setAppend ( final boolean append ) {
        this.redirector.setAppend ( append );
        this.redirectOutput = true;
    }
    public void setAlwaysLog ( final boolean alwaysLog ) {
        this.alwaysLog = alwaysLog;
        this.redirectOutput = true;
    }
    public void setCreateEmptyFiles ( final boolean createEmptyFiles ) {
        this.redirector.setCreateEmptyFiles ( createEmptyFiles );
        this.redirectOutput = true;
    }
    public void addConfiguredRedirector ( final RedirectorElement redirectorElement ) {
        if ( this.redirectorElement != null ) {
            throw new BuildException ( "Cannot have > 1 nested <redirector>s" );
        }
        this.redirectorElement = redirectorElement;
    }
    private void configureRedirector() {
        if ( this.redirectorElement != null ) {
            this.redirectorElement.configure ( this.redirector );
            this.redirectOutput = true;
        }
        this.redirectorConfigured = true;
    }
    protected void openRedirector() {
        if ( !this.redirectorConfigured ) {
            this.configureRedirector();
        }
        if ( this.redirectOutput ) {
            this.redirector.createStreams();
            this.redirectOutStream = this.redirector.getOutputStream();
            this.redirectOutPrintStream = new PrintStream ( this.redirectOutStream );
            this.redirectErrStream = this.redirector.getErrorStream();
            this.redirectErrPrintStream = new PrintStream ( this.redirectErrStream );
        }
    }
    protected void closeRedirector() {
        try {
            if ( this.redirectOutput && this.redirectOutPrintStream != null ) {
                this.redirector.complete();
            }
        } catch ( IOException ioe ) {
            this.log ( "Error closing redirector: " + ioe.getMessage(), 0 );
        }
        this.redirectOutStream = null;
        this.redirectOutPrintStream = null;
        this.redirectErrStream = null;
        this.redirectErrPrintStream = null;
    }
    protected void handleOutput ( final String output ) {
        if ( this.redirectOutput ) {
            if ( this.redirectOutPrintStream == null ) {
                this.openRedirector();
            }
            this.redirectOutPrintStream.println ( output );
            if ( this.alwaysLog ) {
                this.log ( output, 2 );
            }
        } else {
            this.log ( output, 2 );
        }
    }
    protected void handleFlush ( final String output ) {
        this.handleOutput ( output );
        this.redirectOutPrintStream.flush();
    }
    protected void handleErrorOutput ( final String output ) {
        if ( this.redirectOutput ) {
            if ( this.redirectErrPrintStream == null ) {
                this.openRedirector();
            }
            this.redirectErrPrintStream.println ( output );
            if ( this.alwaysLog ) {
                this.log ( output, 0 );
            }
        } else {
            this.log ( output, 0 );
        }
    }
    protected void handleErrorFlush ( final String output ) {
        this.handleErrorOutput ( output );
        this.redirectErrPrintStream.flush();
    }
    protected void handleOutput ( final String output, final int priority ) {
        if ( priority == 0 ) {
            this.handleErrorOutput ( output );
        } else {
            this.handleOutput ( output );
        }
    }
}
