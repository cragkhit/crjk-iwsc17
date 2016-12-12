package org.apache.catalina.ant;
import org.apache.tools.ant.BuildException;
public class ThreaddumpTask extends AbstractCatalinaTask {
    @Override
    public void execute() throws BuildException {
        super.execute();
        execute ( "/threaddump" );
    }
}
