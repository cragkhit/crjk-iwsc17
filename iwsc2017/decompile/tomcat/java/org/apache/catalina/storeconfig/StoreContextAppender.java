package org.apache.catalina.storeconfig;
import java.io.IOException;
import java.io.File;
import org.apache.catalina.Container;
import org.apache.catalina.core.StandardHost;
import org.apache.catalina.core.StandardContext;
import java.io.PrintWriter;
public class StoreContextAppender extends StoreAppender {
    @Override
    protected void printAttribute ( final PrintWriter writer, final int indent, final Object bean, final StoreDescription desc, final String attributeName, final Object bean2, Object value ) {
        if ( this.isPrintValue ( bean, bean2, attributeName, desc ) ) {
            if ( attributeName.equals ( "docBase" ) && bean instanceof StandardContext ) {
                final String docBase = ( ( StandardContext ) bean ).getOriginalDocBase();
                if ( docBase != null ) {
                    value = docBase;
                }
            }
            this.printValue ( writer, indent, attributeName, value );
        }
    }
    @Override
    public boolean isPrintValue ( final Object bean, final Object bean2, final String attrName, final StoreDescription desc ) {
        boolean isPrint = super.isPrintValue ( bean, bean2, attrName, desc );
        if ( isPrint ) {
            final StandardContext context = ( StandardContext ) bean;
            if ( "workDir".equals ( attrName ) ) {
                final String defaultWorkDir = this.getDefaultWorkDir ( context );
                isPrint = !defaultWorkDir.equals ( context.getWorkDir() );
            } else if ( "path".equals ( attrName ) ) {
                isPrint = ( desc.isStoreSeparate() && desc.isExternalAllowed() && context.getConfigFile() == null );
            } else if ( "docBase".equals ( attrName ) ) {
                final Container host = context.getParent();
                if ( host instanceof StandardHost ) {
                    final File appBase = this.getAppBase ( ( StandardHost ) host );
                    final File docBase = this.getDocBase ( context, appBase );
                    isPrint = !appBase.equals ( docBase.getParentFile() );
                }
            }
        }
        return isPrint;
    }
    protected File getAppBase ( final StandardHost host ) {
        File file = new File ( host.getAppBase() );
        if ( !file.isAbsolute() ) {
            file = new File ( System.getProperty ( "catalina.base" ), host.getAppBase() );
        }
        File appBase;
        try {
            appBase = file.getCanonicalFile();
        } catch ( IOException e ) {
            appBase = file;
        }
        return appBase;
    }
    protected File getDocBase ( final StandardContext context, final File appBase ) {
        String contextDocBase = context.getOriginalDocBase();
        if ( contextDocBase == null ) {
            contextDocBase = context.getDocBase();
        }
        File file = new File ( contextDocBase );
        if ( !file.isAbsolute() ) {
            file = new File ( appBase, contextDocBase );
        }
        File docBase;
        try {
            docBase = file.getCanonicalFile();
        } catch ( IOException e ) {
            docBase = file;
        }
        return docBase;
    }
    protected String getDefaultWorkDir ( final StandardContext context ) {
        String defaultWorkDir = null;
        String contextWorkDir = context.getName();
        if ( contextWorkDir.length() == 0 ) {
            contextWorkDir = "_";
        }
        if ( contextWorkDir.startsWith ( "/" ) ) {
            contextWorkDir = contextWorkDir.substring ( 1 );
        }
        final Container host = context.getParent();
        if ( host instanceof StandardHost ) {
            final String hostWorkDir = ( ( StandardHost ) host ).getWorkDir();
            if ( hostWorkDir != null ) {
                defaultWorkDir = hostWorkDir + File.separator + contextWorkDir;
            } else {
                final String engineName = context.getParent().getParent().getName();
                final String hostName = context.getParent().getName();
                defaultWorkDir = "work" + File.separator + engineName + File.separator + hostName + File.separator + contextWorkDir;
            }
        }
        return defaultWorkDir;
    }
    @Override
    public Object defaultInstance ( final Object bean ) throws InstantiationException, IllegalAccessException {
        if ( bean instanceof StandardContext ) {
            final StandardContext defaultContext = new StandardContext();
            return defaultContext;
        }
        return super.defaultInstance ( bean );
    }
}
