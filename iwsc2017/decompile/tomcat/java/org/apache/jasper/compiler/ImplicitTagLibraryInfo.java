package org.apache.jasper.compiler;
import java.util.Collection;
import javax.servlet.jsp.tagext.TagInfo;
import org.apache.tomcat.Jar;
import org.apache.tomcat.util.descriptor.tld.TaglibXml;
import javax.servlet.ServletContext;
import java.net.URL;
import java.util.Iterator;
import java.util.Set;
import org.xml.sax.SAXException;
import java.io.IOException;
import org.apache.jasper.JasperException;
import org.apache.tomcat.util.digester.RuleSet;
import org.apache.tomcat.util.descriptor.tld.TldParser;
import org.apache.tomcat.util.descriptor.tld.ImplicitTldRuleSet;
import org.apache.tomcat.util.descriptor.tld.TldResourcePath;
import javax.servlet.jsp.tagext.FunctionInfo;
import org.apache.jasper.JspCompilationContext;
import javax.servlet.jsp.tagext.TagFileInfo;
import java.util.Vector;
import java.util.Hashtable;
import javax.servlet.jsp.tagext.TagLibraryInfo;
class ImplicitTagLibraryInfo extends TagLibraryInfo {
    private static final String WEB_INF_TAGS = "/WEB-INF/tags";
    private static final String TAG_FILE_SUFFIX = ".tag";
    private static final String TAGX_FILE_SUFFIX = ".tagx";
    private static final String TAGS_SHORTNAME = "tags";
    private static final String TLIB_VERSION = "1.0";
    private static final String JSP_VERSION = "2.0";
    private static final String IMPLICIT_TLD = "implicit.tld";
    private final Hashtable<String, String> tagFileMap;
    private final ParserController pc;
    private final PageInfo pi;
    private final Vector<TagFileInfo> vec;
    public ImplicitTagLibraryInfo ( final JspCompilationContext ctxt, final ParserController pc, final PageInfo pi, final String prefix, final String tagdir, final ErrorDispatcher err ) throws JasperException {
        super ( prefix, ( String ) null );
        this.pc = pc;
        this.pi = pi;
        this.tagFileMap = new Hashtable<String, String>();
        this.vec = new Vector<TagFileInfo>();
        this.functions = new FunctionInfo[0];
        this.tlibversion = "1.0";
        this.jspversion = "2.0";
        if ( !tagdir.startsWith ( "/WEB-INF/tags" ) ) {
            err.jspError ( "jsp.error.invalid.tagdir", tagdir );
        }
        if ( tagdir.equals ( "/WEB-INF/tags" ) || tagdir.equals ( "/WEB-INF/tags/" ) ) {
            this.shortname = "tags";
        } else {
            this.shortname = tagdir.substring ( "/WEB-INF/tags".length() );
            this.shortname = this.shortname.replace ( '/', '-' );
        }
        final Set<String> dirList = ctxt.getResourcePaths ( tagdir );
        if ( dirList != null ) {
            for ( final String path : dirList ) {
                if ( path.endsWith ( ".tag" ) || path.endsWith ( ".tagx" ) ) {
                    final String suffix = path.endsWith ( ".tag" ) ? ".tag" : ".tagx";
                    String tagName = path.substring ( path.lastIndexOf ( 47 ) + 1 );
                    tagName = tagName.substring ( 0, tagName.lastIndexOf ( suffix ) );
                    this.tagFileMap.put ( tagName, path );
                } else {
                    if ( !path.endsWith ( "implicit.tld" ) ) {
                        continue;
                    }
                    TaglibXml taglibXml;
                    try {
                        final URL url = ctxt.getResource ( path );
                        final TldResourcePath resourcePath = new TldResourcePath ( url, path );
                        final ServletContext servletContext = ctxt.getServletContext();
                        final boolean validate = Boolean.parseBoolean ( servletContext.getInitParameter ( "org.apache.jasper.XML_VALIDATE_TLD" ) );
                        final String blockExternalString = servletContext.getInitParameter ( "org.apache.jasper.XML_BLOCK_EXTERNAL" );
                        final boolean blockExternal = blockExternalString == null || Boolean.parseBoolean ( blockExternalString );
                        final TldParser parser = new TldParser ( true, validate, new ImplicitTldRuleSet(), blockExternal );
                        taglibXml = parser.parse ( resourcePath );
                    } catch ( IOException | SAXException e ) {
                        err.jspError ( e );
                        throw new JasperException ( e );
                    }
                    this.tlibversion = taglibXml.getTlibVersion();
                    this.jspversion = taglibXml.getJspVersion();
                    try {
                        final double version = Double.parseDouble ( this.jspversion );
                        if ( version < 2.0 ) {
                            err.jspError ( "jsp.error.invalid.implicit.version", path );
                        }
                    } catch ( NumberFormatException e2 ) {
                        err.jspError ( "jsp.error.invalid.implicit.version", path );
                    }
                    if ( pi == null ) {
                        continue;
                    }
                    pi.addDependant ( path, ctxt.getLastModified ( path ) );
                }
            }
        }
    }
    public TagFileInfo getTagFile ( final String shortName ) {
        TagFileInfo tagFile = super.getTagFile ( shortName );
        if ( tagFile == null ) {
            final String path = this.tagFileMap.get ( shortName );
            if ( path == null ) {
                return null;
            }
            TagInfo tagInfo = null;
            try {
                tagInfo = TagFileProcessor.parseTagFileDirectives ( this.pc, shortName, path, null, this );
            } catch ( JasperException je ) {
                throw new RuntimeException ( je.toString(), ( Throwable ) je );
            }
            tagFile = new TagFileInfo ( shortName, path, tagInfo );
            this.vec.addElement ( tagFile );
            this.tagFiles = new TagFileInfo[this.vec.size()];
            this.vec.copyInto ( this.tagFiles );
        }
        return tagFile;
    }
    public TagLibraryInfo[] getTagLibraryInfos() {
        final Collection<TagLibraryInfo> coll = this.pi.getTaglibs();
        return coll.toArray ( new TagLibraryInfo[0] );
    }
}
