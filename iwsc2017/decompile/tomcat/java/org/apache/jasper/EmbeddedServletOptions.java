package org.apache.jasper;
import java.util.Enumeration;
import org.apache.jasper.compiler.Localizer;
import org.apache.juli.logging.LogFactory;
import javax.servlet.ServletContext;
import javax.servlet.ServletConfig;
import javax.servlet.jsp.tagext.TagLibraryInfo;
import java.util.Map;
import org.apache.jasper.compiler.TagPluginManager;
import org.apache.jasper.compiler.JspConfig;
import org.apache.jasper.compiler.TldCache;
import java.io.File;
import java.util.Properties;
import org.apache.juli.logging.Log;
public final class EmbeddedServletOptions implements Options {
    private final Log log;
    private Properties settings;
    private boolean development;
    public boolean fork;
    private boolean keepGenerated;
    private boolean trimSpaces;
    private boolean isPoolingEnabled;
    private boolean mappedFile;
    private boolean classDebugInfo;
    private int checkInterval;
    private boolean isSmapSuppressed;
    private boolean isSmapDumped;
    private boolean genStringAsCharArray;
    private boolean errorOnUseBeanInvalidClassAttribute;
    private File scratchDir;
    private String ieClassId;
    private String classpath;
    private String compiler;
    private String compilerTargetVM;
    private String compilerSourceVM;
    private String compilerClassName;
    private TldCache tldCache;
    private JspConfig jspConfig;
    private TagPluginManager tagPluginManager;
    private String javaEncoding;
    private int modificationTestInterval;
    private boolean recompileOnFail;
    private boolean xpoweredBy;
    private boolean displaySourceFragment;
    private int maxLoadedJsps;
    private int jspIdleTimeout;
    private boolean strictQuoteEscaping;
    private boolean quoteAttributeEL;
    public String getProperty ( final String name ) {
        return this.settings.getProperty ( name );
    }
    public void setProperty ( final String name, final String value ) {
        if ( name != null && value != null ) {
            this.settings.setProperty ( name, value );
        }
    }
    public void setQuoteAttributeEL ( final boolean b ) {
        this.quoteAttributeEL = b;
    }
    @Override
    public boolean getQuoteAttributeEL() {
        return this.quoteAttributeEL;
    }
    @Override
    public boolean getKeepGenerated() {
        return this.keepGenerated;
    }
    @Override
    public boolean getTrimSpaces() {
        return this.trimSpaces;
    }
    @Override
    public boolean isPoolingEnabled() {
        return this.isPoolingEnabled;
    }
    @Override
    public boolean getMappedFile() {
        return this.mappedFile;
    }
    @Override
    public boolean getClassDebugInfo() {
        return this.classDebugInfo;
    }
    @Override
    public int getCheckInterval() {
        return this.checkInterval;
    }
    @Override
    public int getModificationTestInterval() {
        return this.modificationTestInterval;
    }
    @Override
    public boolean getRecompileOnFail() {
        return this.recompileOnFail;
    }
    @Override
    public boolean getDevelopment() {
        return this.development;
    }
    @Override
    public boolean isSmapSuppressed() {
        return this.isSmapSuppressed;
    }
    @Override
    public boolean isSmapDumped() {
        return this.isSmapDumped;
    }
    @Override
    public boolean genStringAsCharArray() {
        return this.genStringAsCharArray;
    }
    @Override
    public String getIeClassId() {
        return this.ieClassId;
    }
    @Override
    public File getScratchDir() {
        return this.scratchDir;
    }
    @Override
    public String getClassPath() {
        return this.classpath;
    }
    @Override
    public boolean isXpoweredBy() {
        return this.xpoweredBy;
    }
    @Override
    public String getCompiler() {
        return this.compiler;
    }
    @Override
    public String getCompilerTargetVM() {
        return this.compilerTargetVM;
    }
    @Override
    public String getCompilerSourceVM() {
        return this.compilerSourceVM;
    }
    @Override
    public String getCompilerClassName() {
        return this.compilerClassName;
    }
    @Override
    public boolean getErrorOnUseBeanInvalidClassAttribute() {
        return this.errorOnUseBeanInvalidClassAttribute;
    }
    public void setErrorOnUseBeanInvalidClassAttribute ( final boolean b ) {
        this.errorOnUseBeanInvalidClassAttribute = b;
    }
    @Override
    public TldCache getTldCache() {
        return this.tldCache;
    }
    public void setTldCache ( final TldCache tldCache ) {
        this.tldCache = tldCache;
    }
    @Override
    public String getJavaEncoding() {
        return this.javaEncoding;
    }
    @Override
    public boolean getFork() {
        return this.fork;
    }
    @Override
    public JspConfig getJspConfig() {
        return this.jspConfig;
    }
    @Override
    public TagPluginManager getTagPluginManager() {
        return this.tagPluginManager;
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
        return this.displaySourceFragment;
    }
    @Override
    public int getMaxLoadedJsps() {
        return this.maxLoadedJsps;
    }
    @Override
    public int getJspIdleTimeout() {
        return this.jspIdleTimeout;
    }
    @Override
    public boolean getStrictQuoteEscaping() {
        return this.strictQuoteEscaping;
    }
    public EmbeddedServletOptions ( final ServletConfig config, final ServletContext context ) {
        this.log = LogFactory.getLog ( EmbeddedServletOptions.class );
        this.settings = new Properties();
        this.development = true;
        this.fork = true;
        this.keepGenerated = true;
        this.trimSpaces = false;
        this.isPoolingEnabled = true;
        this.mappedFile = true;
        this.classDebugInfo = true;
        this.checkInterval = 0;
        this.isSmapSuppressed = false;
        this.isSmapDumped = false;
        this.genStringAsCharArray = false;
        this.errorOnUseBeanInvalidClassAttribute = true;
        this.ieClassId = "clsid:8AD9C840-044E-11D1-B3E9-00805F499D93";
        this.classpath = null;
        this.compiler = null;
        this.compilerTargetVM = "1.8";
        this.compilerSourceVM = "1.8";
        this.compilerClassName = null;
        this.tldCache = null;
        this.jspConfig = null;
        this.tagPluginManager = null;
        this.javaEncoding = "UTF8";
        this.modificationTestInterval = 4;
        this.recompileOnFail = false;
        this.displaySourceFragment = true;
        this.maxLoadedJsps = -1;
        this.jspIdleTimeout = -1;
        this.strictQuoteEscaping = true;
        this.quoteAttributeEL = true;
        final Enumeration<String> enumeration = ( Enumeration<String> ) config.getInitParameterNames();
        while ( enumeration.hasMoreElements() ) {
            final String k = enumeration.nextElement();
            final String v = config.getInitParameter ( k );
            this.setProperty ( k, v );
        }
        final String keepgen = config.getInitParameter ( "keepgenerated" );
        if ( keepgen != null ) {
            if ( keepgen.equalsIgnoreCase ( "true" ) ) {
                this.keepGenerated = true;
            } else if ( keepgen.equalsIgnoreCase ( "false" ) ) {
                this.keepGenerated = false;
            } else if ( this.log.isWarnEnabled() ) {
                this.log.warn ( Localizer.getMessage ( "jsp.warning.keepgen" ) );
            }
        }
        final String trimsp = config.getInitParameter ( "trimSpaces" );
        if ( trimsp != null ) {
            if ( trimsp.equalsIgnoreCase ( "true" ) ) {
                this.trimSpaces = true;
            } else if ( trimsp.equalsIgnoreCase ( "false" ) ) {
                this.trimSpaces = false;
            } else if ( this.log.isWarnEnabled() ) {
                this.log.warn ( Localizer.getMessage ( "jsp.warning.trimspaces" ) );
            }
        }
        this.isPoolingEnabled = true;
        final String poolingEnabledParam = config.getInitParameter ( "enablePooling" );
        if ( poolingEnabledParam != null && !poolingEnabledParam.equalsIgnoreCase ( "true" ) ) {
            if ( poolingEnabledParam.equalsIgnoreCase ( "false" ) ) {
                this.isPoolingEnabled = false;
            } else if ( this.log.isWarnEnabled() ) {
                this.log.warn ( Localizer.getMessage ( "jsp.warning.enablePooling" ) );
            }
        }
        final String mapFile = config.getInitParameter ( "mappedfile" );
        if ( mapFile != null ) {
            if ( mapFile.equalsIgnoreCase ( "true" ) ) {
                this.mappedFile = true;
            } else if ( mapFile.equalsIgnoreCase ( "false" ) ) {
                this.mappedFile = false;
            } else if ( this.log.isWarnEnabled() ) {
                this.log.warn ( Localizer.getMessage ( "jsp.warning.mappedFile" ) );
            }
        }
        final String debugInfo = config.getInitParameter ( "classdebuginfo" );
        if ( debugInfo != null ) {
            if ( debugInfo.equalsIgnoreCase ( "true" ) ) {
                this.classDebugInfo = true;
            } else if ( debugInfo.equalsIgnoreCase ( "false" ) ) {
                this.classDebugInfo = false;
            } else if ( this.log.isWarnEnabled() ) {
                this.log.warn ( Localizer.getMessage ( "jsp.warning.classDebugInfo" ) );
            }
        }
        final String checkInterval = config.getInitParameter ( "checkInterval" );
        if ( checkInterval != null ) {
            try {
                this.checkInterval = Integer.parseInt ( checkInterval );
            } catch ( NumberFormatException ex ) {
                if ( this.log.isWarnEnabled() ) {
                    this.log.warn ( Localizer.getMessage ( "jsp.warning.checkInterval" ) );
                }
            }
        }
        final String modificationTestInterval = config.getInitParameter ( "modificationTestInterval" );
        if ( modificationTestInterval != null ) {
            try {
                this.modificationTestInterval = Integer.parseInt ( modificationTestInterval );
            } catch ( NumberFormatException ex2 ) {
                if ( this.log.isWarnEnabled() ) {
                    this.log.warn ( Localizer.getMessage ( "jsp.warning.modificationTestInterval" ) );
                }
            }
        }
        final String recompileOnFail = config.getInitParameter ( "recompileOnFail" );
        if ( recompileOnFail != null ) {
            if ( recompileOnFail.equalsIgnoreCase ( "true" ) ) {
                this.recompileOnFail = true;
            } else if ( recompileOnFail.equalsIgnoreCase ( "false" ) ) {
                this.recompileOnFail = false;
            } else if ( this.log.isWarnEnabled() ) {
                this.log.warn ( Localizer.getMessage ( "jsp.warning.recompileOnFail" ) );
            }
        }
        final String development = config.getInitParameter ( "development" );
        if ( development != null ) {
            if ( development.equalsIgnoreCase ( "true" ) ) {
                this.development = true;
            } else if ( development.equalsIgnoreCase ( "false" ) ) {
                this.development = false;
            } else if ( this.log.isWarnEnabled() ) {
                this.log.warn ( Localizer.getMessage ( "jsp.warning.development" ) );
            }
        }
        final String suppressSmap = config.getInitParameter ( "suppressSmap" );
        if ( suppressSmap != null ) {
            if ( suppressSmap.equalsIgnoreCase ( "true" ) ) {
                this.isSmapSuppressed = true;
            } else if ( suppressSmap.equalsIgnoreCase ( "false" ) ) {
                this.isSmapSuppressed = false;
            } else if ( this.log.isWarnEnabled() ) {
                this.log.warn ( Localizer.getMessage ( "jsp.warning.suppressSmap" ) );
            }
        }
        final String dumpSmap = config.getInitParameter ( "dumpSmap" );
        if ( dumpSmap != null ) {
            if ( dumpSmap.equalsIgnoreCase ( "true" ) ) {
                this.isSmapDumped = true;
            } else if ( dumpSmap.equalsIgnoreCase ( "false" ) ) {
                this.isSmapDumped = false;
            } else if ( this.log.isWarnEnabled() ) {
                this.log.warn ( Localizer.getMessage ( "jsp.warning.dumpSmap" ) );
            }
        }
        final String genCharArray = config.getInitParameter ( "genStringAsCharArray" );
        if ( genCharArray != null ) {
            if ( genCharArray.equalsIgnoreCase ( "true" ) ) {
                this.genStringAsCharArray = true;
            } else if ( genCharArray.equalsIgnoreCase ( "false" ) ) {
                this.genStringAsCharArray = false;
            } else if ( this.log.isWarnEnabled() ) {
                this.log.warn ( Localizer.getMessage ( "jsp.warning.genchararray" ) );
            }
        }
        final String errBeanClass = config.getInitParameter ( "errorOnUseBeanInvalidClassAttribute" );
        if ( errBeanClass != null ) {
            if ( errBeanClass.equalsIgnoreCase ( "true" ) ) {
                this.errorOnUseBeanInvalidClassAttribute = true;
            } else if ( errBeanClass.equalsIgnoreCase ( "false" ) ) {
                this.errorOnUseBeanInvalidClassAttribute = false;
            } else if ( this.log.isWarnEnabled() ) {
                this.log.warn ( Localizer.getMessage ( "jsp.warning.errBean" ) );
            }
        }
        final String ieClassId = config.getInitParameter ( "ieClassId" );
        if ( ieClassId != null ) {
            this.ieClassId = ieClassId;
        }
        final String classpath = config.getInitParameter ( "classpath" );
        if ( classpath != null ) {
            this.classpath = classpath;
        }
        String dir = config.getInitParameter ( "scratchdir" );
        if ( dir != null && Constants.IS_SECURITY_ENABLED ) {
            this.log.info ( Localizer.getMessage ( "jsp.info.ignoreSetting", "scratchdir", dir ) );
            dir = null;
        }
        if ( dir != null ) {
            this.scratchDir = new File ( dir );
        } else {
            this.scratchDir = ( File ) context.getAttribute ( "javax.servlet.context.tempdir" );
            if ( this.scratchDir == null ) {
                dir = System.getProperty ( "java.io.tmpdir" );
                if ( dir != null ) {
                    this.scratchDir = new File ( dir );
                }
            }
        }
        if ( this.scratchDir == null ) {
            this.log.fatal ( Localizer.getMessage ( "jsp.error.no.scratch.dir" ) );
            return;
        }
        if ( !this.scratchDir.exists() || !this.scratchDir.canRead() || !this.scratchDir.canWrite() || !this.scratchDir.isDirectory() ) {
            this.log.fatal ( Localizer.getMessage ( "jsp.error.bad.scratch.dir", this.scratchDir.getAbsolutePath() ) );
        }
        this.compiler = config.getInitParameter ( "compiler" );
        final String compilerTargetVM = config.getInitParameter ( "compilerTargetVM" );
        if ( compilerTargetVM != null ) {
            this.compilerTargetVM = compilerTargetVM;
        }
        final String compilerSourceVM = config.getInitParameter ( "compilerSourceVM" );
        if ( compilerSourceVM != null ) {
            this.compilerSourceVM = compilerSourceVM;
        }
        final String javaEncoding = config.getInitParameter ( "javaEncoding" );
        if ( javaEncoding != null ) {
            this.javaEncoding = javaEncoding;
        }
        final String compilerClassName = config.getInitParameter ( "compilerClassName" );
        if ( compilerClassName != null ) {
            this.compilerClassName = compilerClassName;
        }
        final String fork = config.getInitParameter ( "fork" );
        if ( fork != null ) {
            if ( fork.equalsIgnoreCase ( "true" ) ) {
                this.fork = true;
            } else if ( fork.equalsIgnoreCase ( "false" ) ) {
                this.fork = false;
            } else if ( this.log.isWarnEnabled() ) {
                this.log.warn ( Localizer.getMessage ( "jsp.warning.fork" ) );
            }
        }
        final String xpoweredBy = config.getInitParameter ( "xpoweredBy" );
        if ( xpoweredBy != null ) {
            if ( xpoweredBy.equalsIgnoreCase ( "true" ) ) {
                this.xpoweredBy = true;
            } else if ( xpoweredBy.equalsIgnoreCase ( "false" ) ) {
                this.xpoweredBy = false;
            } else if ( this.log.isWarnEnabled() ) {
                this.log.warn ( Localizer.getMessage ( "jsp.warning.xpoweredBy" ) );
            }
        }
        final String displaySourceFragment = config.getInitParameter ( "displaySourceFragment" );
        if ( displaySourceFragment != null ) {
            if ( displaySourceFragment.equalsIgnoreCase ( "true" ) ) {
                this.displaySourceFragment = true;
            } else if ( displaySourceFragment.equalsIgnoreCase ( "false" ) ) {
                this.displaySourceFragment = false;
            } else if ( this.log.isWarnEnabled() ) {
                this.log.warn ( Localizer.getMessage ( "jsp.warning.displaySourceFragment" ) );
            }
        }
        final String maxLoadedJsps = config.getInitParameter ( "maxLoadedJsps" );
        if ( maxLoadedJsps != null ) {
            try {
                this.maxLoadedJsps = Integer.parseInt ( maxLoadedJsps );
            } catch ( NumberFormatException ex3 ) {
                if ( this.log.isWarnEnabled() ) {
                    this.log.warn ( Localizer.getMessage ( "jsp.warning.maxLoadedJsps", "" + this.maxLoadedJsps ) );
                }
            }
        }
        final String jspIdleTimeout = config.getInitParameter ( "jspIdleTimeout" );
        if ( jspIdleTimeout != null ) {
            try {
                this.jspIdleTimeout = Integer.parseInt ( jspIdleTimeout );
            } catch ( NumberFormatException ex4 ) {
                if ( this.log.isWarnEnabled() ) {
                    this.log.warn ( Localizer.getMessage ( "jsp.warning.jspIdleTimeout", "" + this.jspIdleTimeout ) );
                }
            }
        }
        final String strictQuoteEscaping = config.getInitParameter ( "strictQuoteEscaping" );
        if ( strictQuoteEscaping != null ) {
            if ( strictQuoteEscaping.equalsIgnoreCase ( "true" ) ) {
                this.strictQuoteEscaping = true;
            } else if ( strictQuoteEscaping.equalsIgnoreCase ( "false" ) ) {
                this.strictQuoteEscaping = false;
            } else if ( this.log.isWarnEnabled() ) {
                this.log.warn ( Localizer.getMessage ( "jsp.warning.strictQuoteEscaping" ) );
            }
        }
        final String quoteAttributeEL = config.getInitParameter ( "quoteAttributeEL" );
        if ( quoteAttributeEL != null ) {
            if ( quoteAttributeEL.equalsIgnoreCase ( "true" ) ) {
                this.quoteAttributeEL = true;
            } else if ( quoteAttributeEL.equalsIgnoreCase ( "false" ) ) {
                this.quoteAttributeEL = false;
            } else if ( this.log.isWarnEnabled() ) {
                this.log.warn ( Localizer.getMessage ( "jsp.warning.quoteAttributeEL" ) );
            }
        }
        this.tldCache = TldCache.getInstance ( context );
        this.jspConfig = new JspConfig ( context );
        this.tagPluginManager = new TagPluginManager ( context );
    }
}
