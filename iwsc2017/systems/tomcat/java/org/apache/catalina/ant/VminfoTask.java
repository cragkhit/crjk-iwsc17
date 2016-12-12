package org.apache.catalina.ant;
import org.apache.tools.ant.BuildException;
public class VminfoTask extends AbstractCatalinaTask {
    @Override
    public void execute() throws BuildException {
        super.execute();
        execute ( "/vminfo" );
    }
}
