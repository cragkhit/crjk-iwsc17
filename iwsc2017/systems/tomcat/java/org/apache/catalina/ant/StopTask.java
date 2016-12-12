package org.apache.catalina.ant;
import org.apache.tools.ant.BuildException;
public class StopTask extends AbstractCatalinaCommandTask {
    @Override
    public void execute() throws BuildException {
        super.execute();
        execute ( createQueryString ( "/stop" ).toString() );
    }
}
