package org.apache.catalina.ant;
import org.apache.tools.ant.BuildException;
public class StartTask extends AbstractCatalinaCommandTask {
    @Override
    public void execute() throws BuildException {
        super.execute();
        execute ( createQueryString ( "/start" ).toString() );
    }
}
