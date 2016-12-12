package org.apache.catalina.ant;
import org.apache.tools.ant.BuildException;
public class FindLeaksTask extends AbstractCatalinaTask {
    private boolean statusLine;
    public FindLeaksTask() {
        this.statusLine = true;
    }
    public void setStatusLine ( final boolean statusLine ) {
        this.statusLine = statusLine;
    }
    public boolean getStatusLine() {
        return this.statusLine;
    }
    @Override
    public void execute() throws BuildException {
        super.execute();
        this.execute ( "/findleaks?statusLine=" + Boolean.toString ( this.statusLine ) );
    }
}
