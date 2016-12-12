package org.apache.catalina.storeconfig;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import org.apache.catalina.Container;
import org.apache.catalina.core.StandardContext;
import org.apache.catalina.core.StandardHost;
public class StoreContextAppender extends StoreAppender {
    @Override
    protected void printAttribute ( PrintWriter writer, int indent, Object bean, StoreDescription desc, String attributeName, Object bean2, Object value ) {
        if ( isPrintValue ( bean, bean2, attributeName, desc ) ) {
            if ( attributeName.equals ( "docBase" ) ) {
                if ( bean instanceof StandardContext ) {
                    String docBase = ( ( StandardContext ) bean ).getOriginalDocBase() ;
                    if ( docBase != null ) {
                        value = docBase ;
                    }
                }
            }
            printValue ( writer, indent, attributeName, value );
        }
    }
    @Override
    public boolean isPrintValue ( Object bean, Object bean2, String attrName,
                                  StoreDescription desc ) {
        boolean isPrint = super.isPrintValue ( bean, bean2, attrName, desc );
        if ( isPrint ) {
            StandardContext context = ( ( StandardContext ) bean );
            if ( "workDir".equals ( attrName ) ) {
                String defaultWorkDir = getDefaultWorkDir ( context );
                isPrint = !defaultWorkDir.equals ( context.getWorkDir() );
            } else if ( "path".equals ( attrName ) ) {
                isPrint = desc.isStoreSeparate()
                          && desc.isExternalAllowed()
                          && context.getConfigFile() == null;
            } else if ( "docBase".equals ( attrName ) ) {
                Container host = context.getParent();
                if ( host instanceof StandardHost ) {
                    File appBase = getAppBase ( ( ( StandardHost ) host ) );
                    File docBase = getDocBase ( context, appBase );
                    isPrint = !appBase.equals ( docBase.getParentFile() );
                }
            }
        }
        return isPrint;
    }
    protected File getAppBase ( StandardHost host ) {
        File appBase;
        File file = new File ( host.getAppBase() );
        if ( !file.isAbsolute() )
            file = new File ( System.getProperty ( "catalina.base" ), host
                              .getAppBase() );
        try {
            appBase = file.getCanonicalFile();
        } catch ( IOException e ) {
            appBase = file;
        }
        return ( appBase );
    }
    protected File getDocBase ( StandardContext context, File appBase ) {
        File docBase;
        String contextDocBase = context.getOriginalDocBase() ;
        if ( contextDocBase == null ) {
            contextDocBase = context.getDocBase() ;
        }
        File file = new File ( contextDocBase );
        if ( !file.isAbsolute() ) {
            file = new File ( appBase, contextDocBase );
        }
        try {
            docBase = file.getCanonicalFile();
        } catch ( IOException e ) {
            docBase = file;
        }
        return ( docBase );
    }
    protected String getDefaultWorkDir ( StandardContext context ) {
        String defaultWorkDir = null;
        String contextWorkDir = context.getName();
        if ( contextWorkDir.length() == 0 ) {
            contextWorkDir = "_";
        }
        if ( contextWorkDir.startsWith ( "/" ) ) {
            contextWorkDir = contextWorkDir.substring ( 1 );
        }
        Container host = context.getParent();
        if ( host instanceof StandardHost ) {
            String hostWorkDir = ( ( StandardHost ) host ).getWorkDir();
            if ( hostWorkDir != null ) {
                defaultWorkDir = hostWorkDir + File.separator + contextWorkDir;
            } else {
                String engineName = context.getParent().getParent().getName();
                String hostName = context.getParent().getName();
                defaultWorkDir = "work" + File.separator + engineName
                                 + File.separator + hostName + File.separator
                                 + contextWorkDir;
            }
        }
        return defaultWorkDir;
    }
    @Override
    public Object defaultInstance ( Object bean ) throws InstantiationException,
        IllegalAccessException {
        if ( bean instanceof StandardContext ) {
            StandardContext defaultContext = new StandardContext();
            return defaultContext;
        } else {
            return super.defaultInstance ( bean );
        }
    }
}
