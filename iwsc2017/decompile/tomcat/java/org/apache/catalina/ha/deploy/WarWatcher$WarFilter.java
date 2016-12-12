package org.apache.catalina.ha.deploy;
import java.io.File;
import java.io.FilenameFilter;
protected static class WarFilter implements FilenameFilter {
    @Override
    public boolean accept ( final File path, final String name ) {
        return name != null && name.endsWith ( ".war" );
    }
}
