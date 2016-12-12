package org.apache.catalina.ant;
import org.apache.tools.ant.BuildException;
public class FindLeaksTask extends AbstractCatalinaTask {
    private boolean statusLine = true;
    public void setStatusLine ( boolean statusLine ) {
        this.statusLine = statusLine;
    }
    public boolean getStatusLine() {
        return statusLine;
    }
    @Override
    public void execute() throws BuildException {
        super.execute();
        execute ( "/findleaks?statusLine=" + Boolean.toString ( statusLine ) );
    }
}
