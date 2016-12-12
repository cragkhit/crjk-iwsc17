package org.apache.jasper.compiler;
import javax.servlet.jsp.tagext.ValidationMessage;
import javax.servlet.jsp.tagext.PageData;
import java.util.Map;
import java.util.Hashtable;
import org.apache.tomcat.util.descriptor.tld.ValidatorXml;
import javax.servlet.jsp.tagext.TagVariableInfo;
import javax.servlet.jsp.tagext.TagAttributeInfo;
import javax.servlet.jsp.tagext.TagExtraInfo;
import java.net.URISyntaxException;
import java.net.URI;
import java.util.Collection;
import java.util.Set;
import java.util.Iterator;
import java.util.List;
import org.apache.tomcat.util.descriptor.tld.TaglibXml;
import java.net.URLConnection;
import java.net.URL;
import java.util.HashSet;
import org.apache.tomcat.util.descriptor.tld.TagFileXml;
import org.apache.tomcat.util.descriptor.tld.TagXml;
import java.util.ArrayList;
import java.io.IOException;
import org.apache.jasper.JasperException;
import org.apache.tomcat.Jar;
import org.apache.tomcat.util.descriptor.tld.TldResourcePath;
import javax.servlet.jsp.tagext.FunctionInfo;
import javax.servlet.jsp.tagext.TagFileInfo;
import javax.servlet.jsp.tagext.TagInfo;
import java.io.Writer;
import java.io.StringWriter;
import java.io.PrintWriter;
import javax.servlet.jsp.tagext.TagLibraryValidator;
import org.apache.jasper.JspCompilationContext;
import javax.servlet.jsp.tagext.TagLibraryInfo;
class TagLibraryInfoImpl extends TagLibraryInfo implements TagConstants {
    private final JspCompilationContext ctxt;
    private final PageInfo pi;
    private final ErrorDispatcher err;
    private final ParserController parserController;
    private TagLibraryValidator tagLibraryValidator;
    private static void print ( final String name, final String value, final PrintWriter w ) {
        if ( value != null ) {
            w.print ( name + " = {\n\t" );
            w.print ( value );
            w.print ( "\n}\n" );
        }
    }
    public String toString() {
        final StringWriter sw = new StringWriter();
        final PrintWriter out = new PrintWriter ( sw );
        print ( "tlibversion", this.tlibversion, out );
        print ( "jspversion", this.jspversion, out );
        print ( "shortname", this.shortname, out );
        print ( "urn", this.urn, out );
        print ( "info", this.info, out );
        print ( "uri", this.uri, out );
        print ( "tagLibraryValidator", "" + this.tagLibraryValidator, out );
        for ( final TagInfo tag : this.tags ) {
            out.println ( tag.toString() );
        }
        for ( final TagFileInfo tagFile : this.tagFiles ) {
            out.println ( tagFile.toString() );
        }
        for ( final FunctionInfo function : this.functions ) {
            out.println ( function.toString() );
        }
        return sw.toString();
    }
    public TagLibraryInfoImpl ( final JspCompilationContext ctxt, final ParserController pc, final PageInfo pi, final String prefix, final String uriIn, TldResourcePath tldResourcePath, final ErrorDispatcher err ) throws JasperException {
        super ( prefix, uriIn );
        this.ctxt = ctxt;
        this.parserController = pc;
        this.pi = pi;
        this.err = err;
        if ( tldResourcePath == null ) {
            tldResourcePath = this.generateTldResourcePath ( this.uri, ctxt );
        }
        try ( final Jar jar = tldResourcePath.openJar() ) {
            final PageInfo pageInfo = ctxt.createCompiler().getPageInfo();
            if ( pageInfo != null ) {
                final String path = tldResourcePath.getWebappPath();
                if ( path != null ) {
                    pageInfo.addDependant ( path, ctxt.getLastModified ( path, null ) );
                }
                if ( jar != null ) {
                    if ( path == null ) {
                        final URL jarUrl = jar.getJarFileURL();
                        long lastMod = -1L;
                        URLConnection urlConn = null;
                        try {
                            urlConn = jarUrl.openConnection();
                            lastMod = urlConn.getLastModified();
                        } catch ( IOException ioe ) {
                            throw new JasperException ( ioe );
                        } finally {
                            if ( urlConn != null ) {
                                try {
                                    urlConn.getInputStream().close();
                                } catch ( IOException ex ) {}
                            }
                        }
                        pageInfo.addDependant ( jarUrl.toExternalForm(), lastMod );
                    }
                    final String entryName = tldResourcePath.getEntryName();
                    try {
                        pageInfo.addDependant ( jar.getURL ( entryName ), jar.getLastModified ( entryName ) );
                    } catch ( IOException ioe2 ) {
                        throw new JasperException ( ioe2 );
                    }
                }
            }
            if ( tldResourcePath.getUrl() == null ) {
                err.jspError ( "jsp.error.tld.missing", prefix, this.uri );
            }
            final TaglibXml taglibXml = ctxt.getOptions().getTldCache().getTaglibXml ( tldResourcePath );
            if ( taglibXml == null ) {
                err.jspError ( "jsp.error.tld.missing", prefix, this.uri );
            }
            final String v = taglibXml.getJspVersion();
            this.jspversion = v;
            this.tlibversion = taglibXml.getTlibVersion();
            this.shortname = taglibXml.getShortName();
            this.urn = taglibXml.getUri();
            this.info = taglibXml.getInfo();
            this.tagLibraryValidator = this.createValidator ( taglibXml.getValidator() );
            final List<TagInfo> tagInfos = new ArrayList<TagInfo>();
            for ( final TagXml tagXml : taglibXml.getTags() ) {
                tagInfos.add ( this.createTagInfo ( tagXml ) );
            }
            final List<TagFileInfo> tagFileInfos = new ArrayList<TagFileInfo>();
            for ( final TagFileXml tagFileXml : taglibXml.getTagFiles() ) {
                tagFileInfos.add ( this.createTagFileInfo ( tagFileXml, jar ) );
            }
            final Set<String> names = new HashSet<String>();
            final List<FunctionInfo> functionInfos = taglibXml.getFunctions();
            for ( final FunctionInfo functionInfo : functionInfos ) {
                final String name = functionInfo.getName();
                if ( !names.add ( name ) ) {
                    err.jspError ( "jsp.error.tld.fn.duplicate.name", name, this.uri );
                }
            }
            if ( this.tlibversion == null ) {
                err.jspError ( "jsp.error.tld.mandatory.element.missing", "tlib-version", this.uri );
            }
            if ( this.jspversion == null ) {
                err.jspError ( "jsp.error.tld.mandatory.element.missing", "jsp-version", this.uri );
            }
            this.tags = tagInfos.toArray ( new TagInfo[tagInfos.size()] );
            this.tagFiles = tagFileInfos.toArray ( new TagFileInfo[tagFileInfos.size()] );
            this.functions = functionInfos.toArray ( new FunctionInfo[functionInfos.size()] );
        } catch ( IOException ioe3 ) {
            throw new JasperException ( ioe3 );
        }
    }
    public TagLibraryInfo[] getTagLibraryInfos() {
        final Collection<TagLibraryInfo> coll = this.pi.getTaglibs();
        return coll.toArray ( new TagLibraryInfo[coll.size()] );
    }
    private TldResourcePath generateTldResourcePath ( String uri, final JspCompilationContext ctxt ) throws JasperException {
        if ( uri.indexOf ( 58 ) != -1 ) {
            this.err.jspError ( "jsp.error.taglibDirective.absUriCannotBeResolved", uri );
        } else if ( uri.charAt ( 0 ) != '/' ) {
            uri = ctxt.resolveRelativeUri ( uri );
            try {
                uri = new URI ( uri ).normalize().toString();
                if ( uri.startsWith ( "../" ) ) {
                    this.err.jspError ( "jsp.error.taglibDirective.uriInvalid", uri );
                }
            } catch ( URISyntaxException e ) {
                this.err.jspError ( "jsp.error.taglibDirective.uriInvalid", uri );
            }
        }
        URL url = null;
        try {
            url = ctxt.getResource ( uri );
        } catch ( Exception ex ) {
            this.err.jspError ( "jsp.error.tld.unable_to_get_jar", uri, ex.toString() );
        }
        if ( uri.endsWith ( ".jar" ) ) {
            if ( url == null ) {
                this.err.jspError ( "jsp.error.tld.missing_jar", uri );
            }
            return new TldResourcePath ( url, uri, "META-INF/taglib.tld" );
        }
        if ( uri.startsWith ( "/WEB-INF/lib/" ) || uri.startsWith ( "/WEB-INF/classes/" ) || ( uri.startsWith ( "/WEB-INF/tags/" ) && uri.endsWith ( ".tld" ) && !uri.endsWith ( "implicit.tld" ) ) ) {
            this.err.jspError ( "jsp.error.tld.invalid_tld_file", uri );
        }
        return new TldResourcePath ( url, uri );
    }
    private TagInfo createTagInfo ( final TagXml tagXml ) throws JasperException {
        final String teiClassName = tagXml.getTeiClass();
        TagExtraInfo tei = null;
        if ( teiClassName != null && !teiClassName.isEmpty() ) {
            try {
                final Class<?> teiClass = this.ctxt.getClassLoader().loadClass ( teiClassName );
                tei = ( TagExtraInfo ) teiClass.newInstance();
            } catch ( Exception e ) {
                this.err.jspError ( e, "jsp.error.teiclass.instantiation", teiClassName );
            }
        }
        final List<TagAttributeInfo> attributeInfos = tagXml.getAttributes();
        final List<TagVariableInfo> variableInfos = tagXml.getVariables();
        return new TagInfo ( tagXml.getName(), tagXml.getTagClass(), tagXml.getBodyContent(), tagXml.getInfo(), ( TagLibraryInfo ) this, tei, ( TagAttributeInfo[] ) attributeInfos.toArray ( new TagAttributeInfo[attributeInfos.size()] ), tagXml.getDisplayName(), tagXml.getSmallIcon(), tagXml.getLargeIcon(), ( TagVariableInfo[] ) variableInfos.toArray ( new TagVariableInfo[variableInfos.size()] ), tagXml.hasDynamicAttributes() );
    }
    private TagFileInfo createTagFileInfo ( final TagFileXml tagFileXml, final Jar jar ) throws JasperException {
        final String name = tagFileXml.getName();
        final String path = tagFileXml.getPath();
        if ( path == null ) {
            this.err.jspError ( "jsp.error.tagfile.missingPath", new String[0] );
        } else if ( !path.startsWith ( "/META-INF/tags" ) && !path.startsWith ( "/WEB-INF/tags" ) ) {
            this.err.jspError ( "jsp.error.tagfile.illegalPath", path );
        }
        final TagInfo tagInfo = TagFileProcessor.parseTagFileDirectives ( this.parserController, name, path, jar, this );
        return new TagFileInfo ( name, path, tagInfo );
    }
    private TagLibraryValidator createValidator ( final ValidatorXml validatorXml ) throws JasperException {
        if ( validatorXml == null ) {
            return null;
        }
        final String validatorClass = validatorXml.getValidatorClass();
        if ( validatorClass == null || validatorClass.isEmpty() ) {
            return null;
        }
        final Map<String, Object> initParams = new Hashtable<String, Object>();
        initParams.putAll ( validatorXml.getInitParams() );
        try {
            final Class<?> tlvClass = this.ctxt.getClassLoader().loadClass ( validatorClass );
            final TagLibraryValidator tlv = ( TagLibraryValidator ) tlvClass.newInstance();
            tlv.setInitParameters ( ( Map ) initParams );
            return tlv;
        } catch ( Exception e ) {
            this.err.jspError ( e, "jsp.error.tlvclass.instantiation", validatorClass );
            return null;
        }
    }
    public TagLibraryValidator getTagLibraryValidator() {
        return this.tagLibraryValidator;
    }
    public ValidationMessage[] validate ( final PageData thePage ) {
        final TagLibraryValidator tlv = this.getTagLibraryValidator();
        if ( tlv == null ) {
            return null;
        }
        String uri = this.getURI();
        if ( uri.startsWith ( "/" ) ) {
            uri = "urn:jsptld:" + uri;
        }
        return tlv.validate ( this.getPrefixString(), uri, thePage );
    }
}
