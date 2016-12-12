package org.apache.catalina.ant;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.Task;
import org.apache.tools.ant.taskdefs.Redirector;
import org.apache.tools.ant.types.RedirectorElement;
public abstract class BaseRedirectorHelperTask extends Task {
    protected final Redirector redirector = new Redirector ( this );
    protected RedirectorElement redirectorElement = null;
    protected OutputStream redirectOutStream = null;
    protected OutputStream redirectErrStream = null;
    PrintStream redirectOutPrintStream = null;
    PrintStream redirectErrPrintStream = null;
    protected boolean failOnError = true;
    protected boolean redirectOutput = false;
    protected boolean redirectorConfigured = false;
    protected boolean alwaysLog = false;
    public void setFailonerror ( boolean fail ) {
        failOnError = fail;
    }
    public boolean isFailOnError() {
        return failOnError;
    }
    public void setOutput ( File out ) {
        redirector.setOutput ( out );
        redirectOutput = true;
    }
    public void setError ( File error ) {
        redirector.setError ( error );
        redirectOutput = true;
    }
    public void setLogError ( boolean logError ) {
        redirector.setLogError ( logError );
        redirectOutput = true;
    }
    public void setOutputproperty ( String outputProperty ) {
        redirector.setOutputProperty ( outputProperty );
        redirectOutput = true;
    }
    public void setErrorProperty ( String errorProperty ) {
        redirector.setErrorProperty ( errorProperty );
        redirectOutput = true;
    }
    public void setAppend ( boolean append ) {
        redirector.setAppend ( append );
        redirectOutput = true;
    }
    public void setAlwaysLog ( boolean alwaysLog ) {
        this.alwaysLog = alwaysLog;
        redirectOutput = true;
    }
    public void setCreateEmptyFiles ( boolean createEmptyFiles ) {
        redirector.setCreateEmptyFiles ( createEmptyFiles );
        redirectOutput = true;
    }
    public void addConfiguredRedirector ( RedirectorElement redirectorElement ) {
        if ( this.redirectorElement != null ) {
            throw new BuildException ( "Cannot have > 1 nested <redirector>s" );
        } else {
            this.redirectorElement = redirectorElement;
        }
    }
    private void configureRedirector() {
        if ( redirectorElement != null ) {
            redirectorElement.configure ( redirector );
            redirectOutput = true;
        }
        redirectorConfigured = true;
    }
    protected void openRedirector() {
        if ( ! redirectorConfigured ) {
            configureRedirector();
        }
        if ( redirectOutput ) {
            redirector.createStreams();
            redirectOutStream = redirector.getOutputStream();
            redirectOutPrintStream = new PrintStream ( redirectOutStream );
            redirectErrStream = redirector.getErrorStream();
            redirectErrPrintStream = new PrintStream ( redirectErrStream );
        }
    }
    protected void closeRedirector() {
        try {
            if ( redirectOutput && redirectOutPrintStream != null ) {
                redirector.complete();
            }
        } catch ( IOException ioe ) {
            log ( "Error closing redirector: "
                  + ioe.getMessage(), Project.MSG_ERR );
        }
        redirectOutStream = null;
        redirectOutPrintStream = null;
        redirectErrStream = null;
        redirectErrPrintStream = null;
    }
    @Override
    protected void handleOutput ( String output ) {
        if ( redirectOutput ) {
            if ( redirectOutPrintStream == null ) {
                openRedirector();
            }
            redirectOutPrintStream.println ( output );
            if ( alwaysLog ) {
                log ( output, Project.MSG_INFO );
            }
        } else {
            log ( output, Project.MSG_INFO );
        }
    }
    @Override
    protected void handleFlush ( String output ) {
        handleOutput ( output );
        redirectOutPrintStream.flush();
    }
    @Override
    protected void handleErrorOutput ( String output ) {
        if ( redirectOutput ) {
            if ( redirectErrPrintStream == null ) {
                openRedirector();
            }
            redirectErrPrintStream.println ( output );
            if ( alwaysLog ) {
                log ( output, Project.MSG_ERR );
            }
        } else {
            log ( output, Project.MSG_ERR );
        }
    }
    @Override
    protected void handleErrorFlush ( String output ) {
        handleErrorOutput ( output );
        redirectErrPrintStream.flush();
    }
    protected void handleOutput ( String output, int priority ) {
        if ( priority == Project.MSG_ERR ) {
            handleErrorOutput ( output );
        } else {
            handleOutput ( output );
        }
    }
}
