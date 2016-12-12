package org.apache.jasper;
import java.io.File;
import java.util.Enumeration;
import java.util.Map;
import java.util.Properties;
import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.jsp.tagext.TagLibraryInfo;
import org.apache.jasper.compiler.JspConfig;
import org.apache.jasper.compiler.Localizer;
import org.apache.jasper.compiler.TagPluginManager;
import org.apache.jasper.compiler.TldCache;
import org.apache.juli.logging.Log;
import org.apache.juli.logging.LogFactory;
public final class EmbeddedServletOptions implements Options {
    private final Log log = LogFactory.getLog ( EmbeddedServletOptions.class );
    private Properties settings = new Properties();
    private boolean development = true;
    public boolean fork = true;
    private boolean keepGenerated = true;
    private boolean trimSpaces = false;
    private boolean isPoolingEnabled = true;
    private boolean mappedFile = true;
    private boolean classDebugInfo = true;
    private int checkInterval = 0;
    private boolean isSmapSuppressed = false;
    private boolean isSmapDumped = false;
    private boolean genStringAsCharArray = false;
    private boolean errorOnUseBeanInvalidClassAttribute = true;
    private File scratchDir;
    private String ieClassId = "clsid:8AD9C840-044E-11D1-B3E9-00805F499D93";
    private String classpath = null;
    private String compiler = null;
    private String compilerTargetVM = "1.8";
    private String compilerSourceVM = "1.8";
    private String compilerClassName = null;
    private TldCache tldCache = null;
    private JspConfig jspConfig = null;
    private TagPluginManager tagPluginManager = null;
    private String javaEncoding = "UTF8";
    private int modificationTestInterval = 4;
    private boolean recompileOnFail = false;
    private boolean xpoweredBy;
    private boolean displaySourceFragment = true;
    private int maxLoadedJsps = -1;
    private int jspIdleTimeout = -1;
    private boolean strictQuoteEscaping = true;
    private boolean quoteAttributeEL = true;
    public String getProperty ( String name ) {
        return settings.getProperty ( name );
    }
    public void setProperty ( String name, String value ) {
        if ( name != null && value != null ) {
            settings.setProperty ( name, value );
        }
    }
    public void setQuoteAttributeEL ( boolean b ) {
        this.quoteAttributeEL = b;
    }
    @Override
    public boolean getQuoteAttributeEL() {
        return quoteAttributeEL;
    }
    @Override
    public boolean getKeepGenerated() {
        return keepGenerated;
    }
    @Override
    public boolean getTrimSpaces() {
        return trimSpaces;
    }
    @Override
    public boolean isPoolingEnabled() {
        return isPoolingEnabled;
    }
    @Override
    public boolean getMappedFile() {
        return mappedFile;
    }
    @Override
    public boolean getClassDebugInfo() {
        return classDebugInfo;
    }
    @Override
    public int getCheckInterval() {
        return checkInterval;
    }
    @Override
    public int getModificationTestInterval() {
        return modificationTestInterval;
    }
    @Override
    public boolean getRecompileOnFail() {
        return recompileOnFail;
    }
    @Override
    public boolean getDevelopment() {
        return development;
    }
    @Override
    public boolean isSmapSuppressed() {
        return isSmapSuppressed;
    }
    @Override
    public boolean isSmapDumped() {
        return isSmapDumped;
    }
    @Override
    public boolean genStringAsCharArray() {
        return this.genStringAsCharArray;
    }
    @Override
    public String getIeClassId() {
        return ieClassId;
    }
    @Override
    public File getScratchDir() {
        return scratchDir;
    }
    @Override
    public String getClassPath() {
        return classpath;
    }
    @Override
    public boolean isXpoweredBy() {
        return xpoweredBy;
    }
    @Override
    public String getCompiler() {
        return compiler;
    }
    @Override
    public String getCompilerTargetVM() {
        return compilerTargetVM;
    }
    @Override
    public String getCompilerSourceVM() {
        return compilerSourceVM;
    }
    @Override
    public String getCompilerClassName() {
        return compilerClassName;
    }
    @Override
    public boolean getErrorOnUseBeanInvalidClassAttribute() {
        return errorOnUseBeanInvalidClassAttribute;
    }
    public void setErrorOnUseBeanInvalidClassAttribute ( boolean b ) {
        errorOnUseBeanInvalidClassAttribute = b;
    }
    @Override
    public TldCache getTldCache() {
        return tldCache;
    }
    public void setTldCache ( TldCache tldCache ) {
        this.tldCache = tldCache;
    }
    @Override
    public String getJavaEncoding() {
        return javaEncoding;
    }
    @Override
    public boolean getFork() {
        return fork;
    }
    @Override
    public JspConfig getJspConfig() {
        return jspConfig;
    }
    @Override
    public TagPluginManager getTagPluginManager() {
        return tagPluginManager;
    }
    @Override
    public boolean isCaching() {
        return false;
    }
    @Override
    public Map<String, TagLibraryInfo> getCache() {
        return null;
    }
    @Override
    public boolean getDisplaySourceFragment() {
        return displaySourceFragment;
    }
    @Override
    public int getMaxLoadedJsps() {
        return maxLoadedJsps;
    }
    @Override
    public int getJspIdleTimeout() {
        return jspIdleTimeout;
    }
    @Override
    public boolean getStrictQuoteEscaping() {
        return strictQuoteEscaping;
    }
    public EmbeddedServletOptions ( ServletConfig config, ServletContext context ) {
        Enumeration<String> enumeration = config.getInitParameterNames();
        while ( enumeration.hasMoreElements() ) {
            String k = enumeration.nextElement();
            String v = config.getInitParameter ( k );
            setProperty ( k, v );
        }
        String keepgen = config.getInitParameter ( "keepgenerated" );
        if ( keepgen != null ) {
            if ( keepgen.equalsIgnoreCase ( "true" ) ) {
                this.keepGenerated = true;
            } else if ( keepgen.equalsIgnoreCase ( "false" ) ) {
                this.keepGenerated = false;
            } else {
                if ( log.isWarnEnabled() ) {
                    log.warn ( Localizer.getMessage ( "jsp.warning.keepgen" ) );
                }
            }
        }
        String trimsp = config.getInitParameter ( "trimSpaces" );
        if ( trimsp != null ) {
            if ( trimsp.equalsIgnoreCase ( "true" ) ) {
                trimSpaces = true;
            } else if ( trimsp.equalsIgnoreCase ( "false" ) ) {
                trimSpaces = false;
            } else {
                if ( log.isWarnEnabled() ) {
                    log.warn ( Localizer.getMessage ( "jsp.warning.trimspaces" ) );
                }
            }
        }
        this.isPoolingEnabled = true;
        String poolingEnabledParam = config.getInitParameter ( "enablePooling" );
        if ( poolingEnabledParam != null
                && !poolingEnabledParam.equalsIgnoreCase ( "true" ) ) {
            if ( poolingEnabledParam.equalsIgnoreCase ( "false" ) ) {
                this.isPoolingEnabled = false;
            } else {
                if ( log.isWarnEnabled() ) {
                    log.warn ( Localizer.getMessage ( "jsp.warning.enablePooling" ) );
                }
            }
        }
        String mapFile = config.getInitParameter ( "mappedfile" );
        if ( mapFile != null ) {
            if ( mapFile.equalsIgnoreCase ( "true" ) ) {
                this.mappedFile = true;
            } else if ( mapFile.equalsIgnoreCase ( "false" ) ) {
                this.mappedFile = false;
            } else {
                if ( log.isWarnEnabled() ) {
                    log.warn ( Localizer.getMessage ( "jsp.warning.mappedFile" ) );
                }
            }
        }
        String debugInfo = config.getInitParameter ( "classdebuginfo" );
        if ( debugInfo != null ) {
            if ( debugInfo.equalsIgnoreCase ( "true" ) ) {
                this.classDebugInfo  = true;
            } else if ( debugInfo.equalsIgnoreCase ( "false" ) ) {
                this.classDebugInfo  = false;
            } else {
                if ( log.isWarnEnabled() ) {
                    log.warn ( Localizer.getMessage ( "jsp.warning.classDebugInfo" ) );
                }
            }
        }
        String checkInterval = config.getInitParameter ( "checkInterval" );
        if ( checkInterval != null ) {
            try {
                this.checkInterval = Integer.parseInt ( checkInterval );
            } catch ( NumberFormatException ex ) {
                if ( log.isWarnEnabled() ) {
                    log.warn ( Localizer.getMessage ( "jsp.warning.checkInterval" ) );
                }
            }
        }
        String modificationTestInterval = config.getInitParameter ( "modificationTestInterval" );
        if ( modificationTestInterval != null ) {
            try {
                this.modificationTestInterval = Integer.parseInt ( modificationTestInterval );
            } catch ( NumberFormatException ex ) {
                if ( log.isWarnEnabled() ) {
                    log.warn ( Localizer.getMessage ( "jsp.warning.modificationTestInterval" ) );
                }
            }
        }
        String recompileOnFail = config.getInitParameter ( "recompileOnFail" );
        if ( recompileOnFail != null ) {
            if ( recompileOnFail.equalsIgnoreCase ( "true" ) ) {
                this.recompileOnFail = true;
            } else if ( recompileOnFail.equalsIgnoreCase ( "false" ) ) {
                this.recompileOnFail = false;
            } else {
                if ( log.isWarnEnabled() ) {
                    log.warn ( Localizer.getMessage ( "jsp.warning.recompileOnFail" ) );
                }
            }
        }
        String development = config.getInitParameter ( "development" );
        if ( development != null ) {
            if ( development.equalsIgnoreCase ( "true" ) ) {
                this.development = true;
            } else if ( development.equalsIgnoreCase ( "false" ) ) {
                this.development = false;
            } else {
                if ( log.isWarnEnabled() ) {
                    log.warn ( Localizer.getMessage ( "jsp.warning.development" ) );
                }
            }
        }
        String suppressSmap = config.getInitParameter ( "suppressSmap" );
        if ( suppressSmap != null ) {
            if ( suppressSmap.equalsIgnoreCase ( "true" ) ) {
                isSmapSuppressed = true;
            } else if ( suppressSmap.equalsIgnoreCase ( "false" ) ) {
                isSmapSuppressed = false;
            } else {
                if ( log.isWarnEnabled() ) {
                    log.warn ( Localizer.getMessage ( "jsp.warning.suppressSmap" ) );
                }
            }
        }
        String dumpSmap = config.getInitParameter ( "dumpSmap" );
        if ( dumpSmap != null ) {
            if ( dumpSmap.equalsIgnoreCase ( "true" ) ) {
                isSmapDumped = true;
            } else if ( dumpSmap.equalsIgnoreCase ( "false" ) ) {
                isSmapDumped = false;
            } else {
                if ( log.isWarnEnabled() ) {
                    log.warn ( Localizer.getMessage ( "jsp.warning.dumpSmap" ) );
                }
            }
        }
        String genCharArray = config.getInitParameter ( "genStringAsCharArray" );
        if ( genCharArray != null ) {
            if ( genCharArray.equalsIgnoreCase ( "true" ) ) {
                genStringAsCharArray = true;
            } else if ( genCharArray.equalsIgnoreCase ( "false" ) ) {
                genStringAsCharArray = false;
            } else {
                if ( log.isWarnEnabled() ) {
                    log.warn ( Localizer.getMessage ( "jsp.warning.genchararray" ) );
                }
            }
        }
        String errBeanClass = config.getInitParameter ( "errorOnUseBeanInvalidClassAttribute" );
        if ( errBeanClass != null ) {
            if ( errBeanClass.equalsIgnoreCase ( "true" ) ) {
                errorOnUseBeanInvalidClassAttribute = true;
            } else if ( errBeanClass.equalsIgnoreCase ( "false" ) ) {
                errorOnUseBeanInvalidClassAttribute = false;
            } else {
                if ( log.isWarnEnabled() ) {
                    log.warn ( Localizer.getMessage ( "jsp.warning.errBean" ) );
                }
            }
        }
        String ieClassId = config.getInitParameter ( "ieClassId" );
        if ( ieClassId != null ) {
            this.ieClassId = ieClassId;
        }
        String classpath = config.getInitParameter ( "classpath" );
        if ( classpath != null ) {
            this.classpath = classpath;
        }
        String dir = config.getInitParameter ( "scratchdir" );
        if ( dir != null && Constants.IS_SECURITY_ENABLED ) {
            log.info ( Localizer.getMessage ( "jsp.info.ignoreSetting", "scratchdir", dir ) );
            dir = null;
        }
        if ( dir != null ) {
            scratchDir = new File ( dir );
        } else {
            scratchDir = ( File ) context.getAttribute ( ServletContext.TEMPDIR );
            if ( scratchDir == null ) {
                dir = System.getProperty ( "java.io.tmpdir" );
                if ( dir != null ) {
                    scratchDir = new File ( dir );
                }
            }
        }
        if ( this.scratchDir == null ) {
            log.fatal ( Localizer.getMessage ( "jsp.error.no.scratch.dir" ) );
            return;
        }
        if ( ! ( scratchDir.exists() && scratchDir.canRead() &&
                 scratchDir.canWrite() && scratchDir.isDirectory() ) )
            log.fatal ( Localizer.getMessage ( "jsp.error.bad.scratch.dir",
                                               scratchDir.getAbsolutePath() ) );
        this.compiler = config.getInitParameter ( "compiler" );
        String compilerTargetVM = config.getInitParameter ( "compilerTargetVM" );
        if ( compilerTargetVM != null ) {
            this.compilerTargetVM = compilerTargetVM;
        }
        String compilerSourceVM = config.getInitParameter ( "compilerSourceVM" );
        if ( compilerSourceVM != null ) {
            this.compilerSourceVM = compilerSourceVM;
        }
        String javaEncoding = config.getInitParameter ( "javaEncoding" );
        if ( javaEncoding != null ) {
            this.javaEncoding = javaEncoding;
        }
        String compilerClassName = config.getInitParameter ( "compilerClassName" );
        if ( compilerClassName != null ) {
            this.compilerClassName = compilerClassName;
        }
        String fork = config.getInitParameter ( "fork" );
        if ( fork != null ) {
            if ( fork.equalsIgnoreCase ( "true" ) ) {
                this.fork = true;
            } else if ( fork.equalsIgnoreCase ( "false" ) ) {
                this.fork = false;
            } else {
                if ( log.isWarnEnabled() ) {
                    log.warn ( Localizer.getMessage ( "jsp.warning.fork" ) );
                }
            }
        }
        String xpoweredBy = config.getInitParameter ( "xpoweredBy" );
        if ( xpoweredBy != null ) {
            if ( xpoweredBy.equalsIgnoreCase ( "true" ) ) {
                this.xpoweredBy = true;
            } else if ( xpoweredBy.equalsIgnoreCase ( "false" ) ) {
                this.xpoweredBy = false;
            } else {
                if ( log.isWarnEnabled() ) {
                    log.warn ( Localizer.getMessage ( "jsp.warning.xpoweredBy" ) );
                }
            }
        }
        String displaySourceFragment = config.getInitParameter ( "displaySourceFragment" );
        if ( displaySourceFragment != null ) {
            if ( displaySourceFragment.equalsIgnoreCase ( "true" ) ) {
                this.displaySourceFragment = true;
            } else if ( displaySourceFragment.equalsIgnoreCase ( "false" ) ) {
                this.displaySourceFragment = false;
            } else {
                if ( log.isWarnEnabled() ) {
                    log.warn ( Localizer.getMessage ( "jsp.warning.displaySourceFragment" ) );
                }
            }
        }
        String maxLoadedJsps = config.getInitParameter ( "maxLoadedJsps" );
        if ( maxLoadedJsps != null ) {
            try {
                this.maxLoadedJsps = Integer.parseInt ( maxLoadedJsps );
            } catch ( NumberFormatException ex ) {
                if ( log.isWarnEnabled() ) {
                    log.warn ( Localizer.getMessage ( "jsp.warning.maxLoadedJsps", "" + this.maxLoadedJsps ) );
                }
            }
        }
        String jspIdleTimeout = config.getInitParameter ( "jspIdleTimeout" );
        if ( jspIdleTimeout != null ) {
            try {
                this.jspIdleTimeout = Integer.parseInt ( jspIdleTimeout );
            } catch ( NumberFormatException ex ) {
                if ( log.isWarnEnabled() ) {
                    log.warn ( Localizer.getMessage ( "jsp.warning.jspIdleTimeout", "" + this.jspIdleTimeout ) );
                }
            }
        }
        String strictQuoteEscaping = config.getInitParameter ( "strictQuoteEscaping" );
        if ( strictQuoteEscaping != null ) {
            if ( strictQuoteEscaping.equalsIgnoreCase ( "true" ) ) {
                this.strictQuoteEscaping = true;
            } else if ( strictQuoteEscaping.equalsIgnoreCase ( "false" ) ) {
                this.strictQuoteEscaping = false;
            } else {
                if ( log.isWarnEnabled() ) {
                    log.warn ( Localizer.getMessage ( "jsp.warning.strictQuoteEscaping" ) );
                }
            }
        }
        String quoteAttributeEL = config.getInitParameter ( "quoteAttributeEL" );
        if ( quoteAttributeEL != null ) {
            if ( quoteAttributeEL.equalsIgnoreCase ( "true" ) ) {
                this.quoteAttributeEL = true;
            } else if ( quoteAttributeEL.equalsIgnoreCase ( "false" ) ) {
                this.quoteAttributeEL = false;
            } else {
                if ( log.isWarnEnabled() ) {
                    log.warn ( Localizer.getMessage ( "jsp.warning.quoteAttributeEL" ) );
                }
            }
        }
        tldCache = TldCache.getInstance ( context );
        jspConfig = new JspConfig ( context );
        tagPluginManager = new TagPluginManager ( context );
    }
}
