package org.apache.jasper.compiler;
import java.io.ObjectInputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import javax.servlet.jsp.tagext.TagVariableInfo;
import javax.servlet.jsp.tagext.JspIdConsumer;
import javax.servlet.jsp.tagext.DynamicAttributes;
import javax.servlet.jsp.tagext.SimpleTag;
import javax.servlet.jsp.tagext.TryCatchFinally;
import javax.servlet.jsp.tagext.BodyTag;
import javax.servlet.jsp.tagext.IterationTag;
import org.apache.jasper.compiler.tagplugin.TagPluginContext;
import javax.servlet.jsp.tagext.VariableInfo;
import javax.servlet.jsp.tagext.TagFileInfo;
import java.util.List;
import java.util.Vector;
import java.util.HashMap;
import java.lang.reflect.Method;
import javax.el.FunctionMapper;
import java.util.ArrayList;
import javax.servlet.jsp.tagext.FunctionInfo;
import javax.el.ELException;
import javax.el.ELContext;
import org.apache.jasper.el.ELContextImpl;
import javax.servlet.jsp.tagext.TagExtraInfo;
import javax.servlet.jsp.tagext.TagInfo;
import javax.servlet.jsp.tagext.TagData;
import java.util.Hashtable;
import javax.servlet.jsp.tagext.TagAttributeInfo;
import javax.servlet.jsp.JspFactory;
import javax.el.ExpressionFactory;
import java.util.Locale;
import org.xml.sax.Attributes;
import javax.servlet.jsp.tagext.ValidationMessage;
import javax.servlet.jsp.tagext.TagLibraryInfo;
import java.util.Iterator;
import javax.servlet.jsp.tagext.PageData;
import org.apache.jasper.JasperException;
class Validator {
    public static void validateDirectives ( final Compiler compiler, final Node.Nodes page ) throws JasperException {
        page.visit ( new DirectiveVisitor ( compiler ) );
    }
    public static void validateExDirectives ( final Compiler compiler, final Node.Nodes page ) throws JasperException {
        final PageInfo pageInfo = compiler.getPageInfo();
        final String contentType = pageInfo.getContentType();
        if ( contentType == null || contentType.indexOf ( "charset=" ) < 0 ) {
            final boolean isXml = page.getRoot().isXmlSyntax();
            String defaultType;
            if ( contentType == null ) {
                defaultType = ( isXml ? "text/xml" : "text/html" );
            } else {
                defaultType = contentType;
            }
            String charset = null;
            if ( isXml ) {
                charset = "UTF-8";
            } else if ( !page.getRoot().isDefaultPageEncoding() ) {
                charset = page.getRoot().getPageEncoding();
            }
            if ( charset != null ) {
                pageInfo.setContentType ( defaultType + ";charset=" + charset );
            } else {
                pageInfo.setContentType ( defaultType );
            }
        }
        page.visit ( new ValidateVisitor ( compiler ) );
        validateXmlView ( new PageDataImpl ( page, compiler ), compiler );
        page.visit ( new TagExtraInfoVisitor ( compiler ) );
    }
    private static void validateXmlView ( final PageData xmlView, final Compiler compiler ) throws JasperException {
        StringBuilder errMsg = null;
        final ErrorDispatcher errDisp = compiler.getErrorDispatcher();
        for ( final Object o : compiler.getPageInfo().getTaglibs() ) {
            if ( ! ( o instanceof TagLibraryInfoImpl ) ) {
                continue;
            }
            final TagLibraryInfoImpl tli = ( TagLibraryInfoImpl ) o;
            final ValidationMessage[] errors = tli.validate ( xmlView );
            if ( errors == null || errors.length == 0 ) {
                continue;
            }
            if ( errMsg == null ) {
                errMsg = new StringBuilder();
            }
            errMsg.append ( "<h3>" );
            errMsg.append ( Localizer.getMessage ( "jsp.error.tlv.invalid.page", tli.getShortName(), compiler.getPageInfo().getJspFile() ) );
            errMsg.append ( "</h3>" );
            for ( int i = 0; i < errors.length; ++i ) {
                if ( errors[i] != null ) {
                    errMsg.append ( "<p>" );
                    errMsg.append ( errors[i].getId() );
                    errMsg.append ( ": " );
                    errMsg.append ( errors[i].getMessage() );
                    errMsg.append ( "</p>" );
                }
            }
        }
        if ( errMsg != null ) {
            errDisp.jspError ( errMsg.toString(), new String[0] );
        }
    }
    protected static String xmlEscape ( final String s ) {
        if ( s == null ) {
            return null;
        }
        for ( int len = s.length(), i = 0; i < len; ++i ) {
            char c = s.charAt ( i );
            if ( c >= '\"' && c <= '>' && ( c == '<' || c == '>' || c == '\'' || c == '&' || c == '\"' ) ) {
                final StringBuilder sb = new StringBuilder ( ( int ) ( len * 1.2 ) );
                sb.append ( s, 0, i );
                int pos = i + 1;
                for ( int j = i; j < len; ++j ) {
                    c = s.charAt ( j );
                    if ( c >= '\"' && c <= '>' ) {
                        if ( c == '<' ) {
                            if ( j > pos ) {
                                sb.append ( s, pos, j );
                            }
                            sb.append ( "&lt;" );
                            pos = j + 1;
                        } else if ( c == '>' ) {
                            if ( j > pos ) {
                                sb.append ( s, pos, j );
                            }
                            sb.append ( "&gt;" );
                            pos = j + 1;
                        } else if ( c == '\'' ) {
                            if ( j > pos ) {
                                sb.append ( s, pos, j );
                            }
                            sb.append ( "&#039;" );
                            pos = j + 1;
                        } else if ( c == '&' ) {
                            if ( j > pos ) {
                                sb.append ( s, pos, j );
                            }
                            sb.append ( "&amp;" );
                            pos = j + 1;
                        } else if ( c == '\"' ) {
                            if ( j > pos ) {
                                sb.append ( s, pos, j );
                            }
                            sb.append ( "&#034;" );
                            pos = j + 1;
                        }
                    }
                }
                if ( pos < len ) {
                    sb.append ( s, pos, len );
                }
                return sb.toString();
            }
        }
        return s;
    }
    private static class DirectiveVisitor extends Node.Visitor {
        private final PageInfo pageInfo;
        private final ErrorDispatcher err;
        private static final JspUtil.ValidAttribute[] pageDirectiveAttrs;
        private boolean pageEncodingSeen;
        DirectiveVisitor ( final Compiler compiler ) {
            this.pageEncodingSeen = false;
            this.pageInfo = compiler.getPageInfo();
            this.err = compiler.getErrorDispatcher();
        }
        @Override
        public void visit ( final Node.IncludeDirective n ) throws JasperException {
            final boolean pageEncodingSeenSave = this.pageEncodingSeen;
            this.pageEncodingSeen = false;
            this.visitBody ( n );
            this.pageEncodingSeen = pageEncodingSeenSave;
        }
        @Override
        public void visit ( final Node.PageDirective n ) throws JasperException {
            JspUtil.checkAttributes ( "Page directive", n, DirectiveVisitor.pageDirectiveAttrs, this.err );
            final Attributes attrs = n.getAttributes();
            for ( int i = 0; attrs != null && i < attrs.getLength(); ++i ) {
                final String attr = attrs.getQName ( i );
                final String value = attrs.getValue ( i );
                if ( "language".equals ( attr ) ) {
                    if ( this.pageInfo.getLanguage ( false ) == null ) {
                        this.pageInfo.setLanguage ( value, n, this.err, true );
                    } else if ( !this.pageInfo.getLanguage ( false ).equals ( value ) ) {
                        this.err.jspError ( n, "jsp.error.page.conflict.language", this.pageInfo.getLanguage ( false ), value );
                    }
                } else if ( "extends".equals ( attr ) ) {
                    if ( this.pageInfo.getExtends ( false ) == null ) {
                        this.pageInfo.setExtends ( value );
                    } else if ( !this.pageInfo.getExtends ( false ).equals ( value ) ) {
                        this.err.jspError ( n, "jsp.error.page.conflict.extends", this.pageInfo.getExtends ( false ), value );
                    }
                } else if ( "contentType".equals ( attr ) ) {
                    if ( this.pageInfo.getContentType() == null ) {
                        this.pageInfo.setContentType ( value );
                    } else if ( !this.pageInfo.getContentType().equals ( value ) ) {
                        this.err.jspError ( n, "jsp.error.page.conflict.contenttype", this.pageInfo.getContentType(), value );
                    }
                } else if ( "session".equals ( attr ) ) {
                    if ( this.pageInfo.getSession() == null ) {
                        this.pageInfo.setSession ( value, n, this.err );
                    } else if ( !this.pageInfo.getSession().equals ( value ) ) {
                        this.err.jspError ( n, "jsp.error.page.conflict.session", this.pageInfo.getSession(), value );
                    }
                } else if ( "buffer".equals ( attr ) ) {
                    if ( this.pageInfo.getBufferValue() == null ) {
                        this.pageInfo.setBufferValue ( value, n, this.err );
                    } else if ( !this.pageInfo.getBufferValue().equals ( value ) ) {
                        this.err.jspError ( n, "jsp.error.page.conflict.buffer", this.pageInfo.getBufferValue(), value );
                    }
                } else if ( "autoFlush".equals ( attr ) ) {
                    if ( this.pageInfo.getAutoFlush() == null ) {
                        this.pageInfo.setAutoFlush ( value, n, this.err );
                    } else if ( !this.pageInfo.getAutoFlush().equals ( value ) ) {
                        this.err.jspError ( n, "jsp.error.page.conflict.autoflush", this.pageInfo.getAutoFlush(), value );
                    }
                } else if ( "isThreadSafe".equals ( attr ) ) {
                    if ( this.pageInfo.getIsThreadSafe() == null ) {
                        this.pageInfo.setIsThreadSafe ( value, n, this.err );
                    } else if ( !this.pageInfo.getIsThreadSafe().equals ( value ) ) {
                        this.err.jspError ( n, "jsp.error.page.conflict.isthreadsafe", this.pageInfo.getIsThreadSafe(), value );
                    }
                } else if ( "isELIgnored".equals ( attr ) ) {
                    if ( this.pageInfo.getIsELIgnored() == null ) {
                        this.pageInfo.setIsELIgnored ( value, n, this.err, true );
                    } else if ( !this.pageInfo.getIsELIgnored().equals ( value ) ) {
                        this.err.jspError ( n, "jsp.error.page.conflict.iselignored", this.pageInfo.getIsELIgnored(), value );
                    }
                } else if ( "isErrorPage".equals ( attr ) ) {
                    if ( this.pageInfo.getIsErrorPage() == null ) {
                        this.pageInfo.setIsErrorPage ( value, n, this.err );
                    } else if ( !this.pageInfo.getIsErrorPage().equals ( value ) ) {
                        this.err.jspError ( n, "jsp.error.page.conflict.iserrorpage", this.pageInfo.getIsErrorPage(), value );
                    }
                } else if ( "errorPage".equals ( attr ) ) {
                    if ( this.pageInfo.getErrorPage() == null ) {
                        this.pageInfo.setErrorPage ( value );
                    } else if ( !this.pageInfo.getErrorPage().equals ( value ) ) {
                        this.err.jspError ( n, "jsp.error.page.conflict.errorpage", this.pageInfo.getErrorPage(), value );
                    }
                } else if ( "info".equals ( attr ) ) {
                    if ( this.pageInfo.getInfo() == null ) {
                        this.pageInfo.setInfo ( value );
                    } else if ( !this.pageInfo.getInfo().equals ( value ) ) {
                        this.err.jspError ( n, "jsp.error.page.conflict.info", this.pageInfo.getInfo(), value );
                    }
                } else if ( "pageEncoding".equals ( attr ) ) {
                    if ( this.pageEncodingSeen ) {
                        this.err.jspError ( n, "jsp.error.page.multi.pageencoding", new String[0] );
                    }
                    this.pageEncodingSeen = true;
                    final String actual = this.comparePageEncodings ( value, n );
                    n.getRoot().setPageEncoding ( actual );
                } else if ( "deferredSyntaxAllowedAsLiteral".equals ( attr ) ) {
                    if ( this.pageInfo.getDeferredSyntaxAllowedAsLiteral() == null ) {
                        this.pageInfo.setDeferredSyntaxAllowedAsLiteral ( value, n, this.err, true );
                    } else if ( !this.pageInfo.getDeferredSyntaxAllowedAsLiteral().equals ( value ) ) {
                        this.err.jspError ( n, "jsp.error.page.conflict.deferredsyntaxallowedasliteral", this.pageInfo.getDeferredSyntaxAllowedAsLiteral(), value );
                    }
                } else if ( "trimDirectiveWhitespaces".equals ( attr ) ) {
                    if ( this.pageInfo.getTrimDirectiveWhitespaces() == null ) {
                        this.pageInfo.setTrimDirectiveWhitespaces ( value, n, this.err, true );
                    } else if ( !this.pageInfo.getTrimDirectiveWhitespaces().equals ( value ) ) {
                        this.err.jspError ( n, "jsp.error.page.conflict.trimdirectivewhitespaces", this.pageInfo.getTrimDirectiveWhitespaces(), value );
                    }
                }
            }
            if ( this.pageInfo.getBuffer() == 0 && !this.pageInfo.isAutoFlush() ) {
                this.err.jspError ( n, "jsp.error.page.badCombo", new String[0] );
            }
            this.pageInfo.addImports ( n.getImports() );
        }
        @Override
        public void visit ( final Node.TagDirective n ) throws JasperException {
            final Attributes attrs = n.getAttributes();
            for ( int i = 0; attrs != null && i < attrs.getLength(); ++i ) {
                final String attr = attrs.getQName ( i );
                final String value = attrs.getValue ( i );
                if ( "language".equals ( attr ) ) {
                    if ( this.pageInfo.getLanguage ( false ) == null ) {
                        this.pageInfo.setLanguage ( value, n, this.err, false );
                    } else if ( !this.pageInfo.getLanguage ( false ).equals ( value ) ) {
                        this.err.jspError ( n, "jsp.error.tag.conflict.language", this.pageInfo.getLanguage ( false ), value );
                    }
                } else if ( "isELIgnored".equals ( attr ) ) {
                    if ( this.pageInfo.getIsELIgnored() == null ) {
                        this.pageInfo.setIsELIgnored ( value, n, this.err, false );
                    } else if ( !this.pageInfo.getIsELIgnored().equals ( value ) ) {
                        this.err.jspError ( n, "jsp.error.tag.conflict.iselignored", this.pageInfo.getIsELIgnored(), value );
                    }
                } else if ( "pageEncoding".equals ( attr ) ) {
                    if ( this.pageEncodingSeen ) {
                        this.err.jspError ( n, "jsp.error.tag.multi.pageencoding", new String[0] );
                    }
                    this.pageEncodingSeen = true;
                    this.compareTagEncodings ( value, n );
                    n.getRoot().setPageEncoding ( value );
                } else if ( "deferredSyntaxAllowedAsLiteral".equals ( attr ) ) {
                    if ( this.pageInfo.getDeferredSyntaxAllowedAsLiteral() == null ) {
                        this.pageInfo.setDeferredSyntaxAllowedAsLiteral ( value, n, this.err, false );
                    } else if ( !this.pageInfo.getDeferredSyntaxAllowedAsLiteral().equals ( value ) ) {
                        this.err.jspError ( n, "jsp.error.tag.conflict.deferredsyntaxallowedasliteral", this.pageInfo.getDeferredSyntaxAllowedAsLiteral(), value );
                    }
                } else if ( "trimDirectiveWhitespaces".equals ( attr ) ) {
                    if ( this.pageInfo.getTrimDirectiveWhitespaces() == null ) {
                        this.pageInfo.setTrimDirectiveWhitespaces ( value, n, this.err, false );
                    } else if ( !this.pageInfo.getTrimDirectiveWhitespaces().equals ( value ) ) {
                        this.err.jspError ( n, "jsp.error.tag.conflict.trimdirectivewhitespaces", this.pageInfo.getTrimDirectiveWhitespaces(), value );
                    }
                }
            }
            this.pageInfo.addImports ( n.getImports() );
        }
        @Override
        public void visit ( final Node.AttributeDirective n ) throws JasperException {
        }
        @Override
        public void visit ( final Node.VariableDirective n ) throws JasperException {
        }
        private String comparePageEncodings ( final String thePageDirEnc, final Node.PageDirective pageDir ) throws JasperException {
            final Node.Root root = pageDir.getRoot();
            String configEnc = root.getJspConfigPageEncoding();
            final String pageDirEnc = thePageDirEnc.toUpperCase ( Locale.ENGLISH );
            if ( configEnc != null ) {
                configEnc = configEnc.toUpperCase ( Locale.ENGLISH );
                if ( pageDirEnc.equals ( configEnc ) || ( pageDirEnc.startsWith ( "UTF-16" ) && configEnc.startsWith ( "UTF-16" ) ) ) {
                    return configEnc;
                }
                this.err.jspError ( pageDir, "jsp.error.config_pagedir_encoding_mismatch", configEnc, pageDirEnc );
            }
            if ( ( root.isXmlSyntax() && root.isEncodingSpecifiedInProlog() ) || root.isBomPresent() ) {
                final String pageEnc = root.getPageEncoding().toUpperCase ( Locale.ENGLISH );
                if ( pageDirEnc.equals ( pageEnc ) || ( pageDirEnc.startsWith ( "UTF-16" ) && pageEnc.startsWith ( "UTF-16" ) ) ) {
                    return pageEnc;
                }
                this.err.jspError ( pageDir, "jsp.error.prolog_pagedir_encoding_mismatch", pageEnc, pageDirEnc );
            }
            return pageDirEnc;
        }
        private void compareTagEncodings ( final String thePageDirEnc, final Node.TagDirective pageDir ) throws JasperException {
            final Node.Root root = pageDir.getRoot();
            final String pageDirEnc = thePageDirEnc.toUpperCase ( Locale.ENGLISH );
            if ( ( root.isXmlSyntax() && root.isEncodingSpecifiedInProlog() ) || root.isBomPresent() ) {
                final String pageEnc = root.getPageEncoding().toUpperCase ( Locale.ENGLISH );
                if ( !pageDirEnc.equals ( pageEnc ) && ( !pageDirEnc.startsWith ( "UTF-16" ) || !pageEnc.startsWith ( "UTF-16" ) ) ) {
                    this.err.jspError ( pageDir, "jsp.error.prolog_pagedir_encoding_mismatch", pageEnc, pageDirEnc );
                }
            }
        }
        static {
            pageDirectiveAttrs = new JspUtil.ValidAttribute[] { new JspUtil.ValidAttribute ( "language" ), new JspUtil.ValidAttribute ( "extends" ), new JspUtil.ValidAttribute ( "import" ), new JspUtil.ValidAttribute ( "session" ), new JspUtil.ValidAttribute ( "buffer" ), new JspUtil.ValidAttribute ( "autoFlush" ), new JspUtil.ValidAttribute ( "isThreadSafe" ), new JspUtil.ValidAttribute ( "info" ), new JspUtil.ValidAttribute ( "errorPage" ), new JspUtil.ValidAttribute ( "isErrorPage" ), new JspUtil.ValidAttribute ( "contentType" ), new JspUtil.ValidAttribute ( "pageEncoding" ), new JspUtil.ValidAttribute ( "isELIgnored" ), new JspUtil.ValidAttribute ( "deferredSyntaxAllowedAsLiteral" ), new JspUtil.ValidAttribute ( "trimDirectiveWhitespaces" ) };
        }
    }
    private static class ValidateVisitor extends Node.Visitor {
        private final PageInfo pageInfo;
        private final ErrorDispatcher err;
        private final ClassLoader loader;
        private final StringBuilder buf;
        private static final JspUtil.ValidAttribute[] jspRootAttrs;
        private static final JspUtil.ValidAttribute[] includeDirectiveAttrs;
        private static final JspUtil.ValidAttribute[] taglibDirectiveAttrs;
        private static final JspUtil.ValidAttribute[] includeActionAttrs;
        private static final JspUtil.ValidAttribute[] paramActionAttrs;
        private static final JspUtil.ValidAttribute[] forwardActionAttrs;
        private static final JspUtil.ValidAttribute[] getPropertyAttrs;
        private static final JspUtil.ValidAttribute[] setPropertyAttrs;
        private static final JspUtil.ValidAttribute[] useBeanAttrs;
        private static final JspUtil.ValidAttribute[] plugInAttrs;
        private static final JspUtil.ValidAttribute[] attributeAttrs;
        private static final JspUtil.ValidAttribute[] invokeAttrs;
        private static final JspUtil.ValidAttribute[] doBodyAttrs;
        private static final JspUtil.ValidAttribute[] jspOutputAttrs;
        private final ExpressionFactory expressionFactory;
        ValidateVisitor ( final Compiler compiler ) {
            this.buf = new StringBuilder ( 32 );
            this.pageInfo = compiler.getPageInfo();
            this.err = compiler.getErrorDispatcher();
            this.loader = compiler.getCompilationContext().getClassLoader();
            this.expressionFactory = JspFactory.getDefaultFactory().getJspApplicationContext ( compiler.getCompilationContext().getServletContext() ).getExpressionFactory();
        }
        @Override
        public void visit ( final Node.JspRoot n ) throws JasperException {
            JspUtil.checkAttributes ( "Jsp:root", n, ValidateVisitor.jspRootAttrs, this.err );
            final String version = n.getTextAttribute ( "version" );
            if ( !version.equals ( "1.2" ) && !version.equals ( "2.0" ) && !version.equals ( "2.1" ) && !version.equals ( "2.2" ) && !version.equals ( "2.3" ) ) {
                this.err.jspError ( n, "jsp.error.jsproot.version.invalid", version );
            }
            this.visitBody ( n );
        }
        @Override
        public void visit ( final Node.IncludeDirective n ) throws JasperException {
            JspUtil.checkAttributes ( "Include directive", n, ValidateVisitor.includeDirectiveAttrs, this.err );
            this.visitBody ( n );
        }
        @Override
        public void visit ( final Node.TaglibDirective n ) throws JasperException {
            JspUtil.checkAttributes ( "Taglib directive", n, ValidateVisitor.taglibDirectiveAttrs, this.err );
            final String uri = n.getAttributeValue ( "uri" );
            final String tagdir = n.getAttributeValue ( "tagdir" );
            if ( uri == null && tagdir == null ) {
                this.err.jspError ( n, "jsp.error.taglibDirective.missing.location", new String[0] );
            }
            if ( uri != null && tagdir != null ) {
                this.err.jspError ( n, "jsp.error.taglibDirective.both_uri_and_tagdir", new String[0] );
            }
        }
        @Override
        public void visit ( final Node.ParamAction n ) throws JasperException {
            JspUtil.checkAttributes ( "Param action", n, ValidateVisitor.paramActionAttrs, this.err );
            this.throwErrorIfExpression ( n, "name", "jsp:param" );
            n.setValue ( this.getJspAttribute ( null, "value", null, null, n.getAttributeValue ( "value" ), n, null, false ) );
            this.visitBody ( n );
        }
        @Override
        public void visit ( final Node.ParamsAction n ) throws JasperException {
            final Node.Nodes subElems = n.getBody();
            if ( subElems == null ) {
                this.err.jspError ( n, "jsp.error.params.emptyBody", new String[0] );
            }
            this.visitBody ( n );
        }
        @Override
        public void visit ( final Node.IncludeAction n ) throws JasperException {
            JspUtil.checkAttributes ( "Include action", n, ValidateVisitor.includeActionAttrs, this.err );
            n.setPage ( this.getJspAttribute ( null, "page", null, null, n.getAttributeValue ( "page" ), n, null, false ) );
            this.visitBody ( n );
        }
        @Override
        public void visit ( final Node.ForwardAction n ) throws JasperException {
            JspUtil.checkAttributes ( "Forward", n, ValidateVisitor.forwardActionAttrs, this.err );
            n.setPage ( this.getJspAttribute ( null, "page", null, null, n.getAttributeValue ( "page" ), n, null, false ) );
            this.visitBody ( n );
        }
        @Override
        public void visit ( final Node.GetProperty n ) throws JasperException {
            JspUtil.checkAttributes ( "GetProperty", n, ValidateVisitor.getPropertyAttrs, this.err );
        }
        @Override
        public void visit ( final Node.SetProperty n ) throws JasperException {
            JspUtil.checkAttributes ( "SetProperty", n, ValidateVisitor.setPropertyAttrs, this.err );
            final String property = n.getTextAttribute ( "property" );
            final String param = n.getTextAttribute ( "param" );
            final String value = n.getAttributeValue ( "value" );
            n.setValue ( this.getJspAttribute ( null, "value", null, null, value, n, null, false ) );
            final boolean valueSpecified = n.getValue() != null;
            if ( "*".equals ( property ) ) {
                if ( param != null || valueSpecified ) {
                    this.err.jspError ( n, "jsp.error.setProperty.invalid", new String[0] );
                }
            } else if ( param != null && valueSpecified ) {
                this.err.jspError ( n, "jsp.error.setProperty.invalid", new String[0] );
            }
            this.visitBody ( n );
        }
        @Override
        public void visit ( final Node.UseBean n ) throws JasperException {
            JspUtil.checkAttributes ( "UseBean", n, ValidateVisitor.useBeanAttrs, this.err );
            final String name = n.getTextAttribute ( "id" );
            final String scope = n.getTextAttribute ( "scope" );
            JspUtil.checkScope ( scope, n, this.err );
            String className = n.getTextAttribute ( "class" );
            final String type = n.getTextAttribute ( "type" );
            final BeanRepository beanInfo = this.pageInfo.getBeanRepository();
            if ( className == null && type == null ) {
                this.err.jspError ( n, "jsp.error.usebean.missingType", new String[0] );
            }
            if ( beanInfo.checkVariable ( name ) ) {
                this.err.jspError ( n, "jsp.error.usebean.duplicate", new String[0] );
            }
            if ( "session".equals ( scope ) && !this.pageInfo.isSession() ) {
                this.err.jspError ( n, "jsp.error.usebean.noSession", new String[0] );
            }
            final Node.JspAttribute jattr = this.getJspAttribute ( null, "beanName", null, null, n.getAttributeValue ( "beanName" ), n, null, false );
            n.setBeanName ( jattr );
            if ( className != null && jattr != null ) {
                this.err.jspError ( n, "jsp.error.usebean.notBoth", new String[0] );
            }
            if ( className == null ) {
                className = type;
            }
            beanInfo.addBean ( n, name, className, scope );
            this.visitBody ( n );
        }
        @Override
        public void visit ( final Node.PlugIn n ) throws JasperException {
            JspUtil.checkAttributes ( "Plugin", n, ValidateVisitor.plugInAttrs, this.err );
            this.throwErrorIfExpression ( n, "type", "jsp:plugin" );
            this.throwErrorIfExpression ( n, "code", "jsp:plugin" );
            this.throwErrorIfExpression ( n, "codebase", "jsp:plugin" );
            this.throwErrorIfExpression ( n, "align", "jsp:plugin" );
            this.throwErrorIfExpression ( n, "archive", "jsp:plugin" );
            this.throwErrorIfExpression ( n, "hspace", "jsp:plugin" );
            this.throwErrorIfExpression ( n, "jreversion", "jsp:plugin" );
            this.throwErrorIfExpression ( n, "name", "jsp:plugin" );
            this.throwErrorIfExpression ( n, "vspace", "jsp:plugin" );
            this.throwErrorIfExpression ( n, "nspluginurl", "jsp:plugin" );
            this.throwErrorIfExpression ( n, "iepluginurl", "jsp:plugin" );
            final String type = n.getTextAttribute ( "type" );
            if ( type == null ) {
                this.err.jspError ( n, "jsp.error.plugin.notype", new String[0] );
            }
            if ( !type.equals ( "bean" ) && !type.equals ( "applet" ) ) {
                this.err.jspError ( n, "jsp.error.plugin.badtype", new String[0] );
            }
            if ( n.getTextAttribute ( "code" ) == null ) {
                this.err.jspError ( n, "jsp.error.plugin.nocode", new String[0] );
            }
            final Node.JspAttribute width = this.getJspAttribute ( null, "width", null, null, n.getAttributeValue ( "width" ), n, null, false );
            n.setWidth ( width );
            final Node.JspAttribute height = this.getJspAttribute ( null, "height", null, null, n.getAttributeValue ( "height" ), n, null, false );
            n.setHeight ( height );
            this.visitBody ( n );
        }
        @Override
        public void visit ( final Node.NamedAttribute n ) throws JasperException {
            JspUtil.checkAttributes ( "Attribute", n, ValidateVisitor.attributeAttrs, this.err );
            n.setOmit ( this.getJspAttribute ( null, "omit", null, null, n.getAttributeValue ( "omit" ), n, null, false ) );
            this.visitBody ( n );
        }
        @Override
        public void visit ( final Node.JspBody n ) throws JasperException {
            this.visitBody ( n );
        }
        @Override
        public void visit ( final Node.Declaration n ) throws JasperException {
            if ( this.pageInfo.isScriptingInvalid() ) {
                this.err.jspError ( n.getStart(), "jsp.error.no.scriptlets", new String[0] );
            }
        }
        @Override
        public void visit ( final Node.Expression n ) throws JasperException {
            if ( this.pageInfo.isScriptingInvalid() ) {
                this.err.jspError ( n.getStart(), "jsp.error.no.scriptlets", new String[0] );
            }
        }
        @Override
        public void visit ( final Node.Scriptlet n ) throws JasperException {
            if ( this.pageInfo.isScriptingInvalid() ) {
                this.err.jspError ( n.getStart(), "jsp.error.no.scriptlets", new String[0] );
            }
        }
        @Override
        public void visit ( final Node.ELExpression n ) throws JasperException {
            if ( this.pageInfo.isELIgnored() ) {
                return;
            }
            if ( n.getType() == '#' ) {
                if ( this.pageInfo.isDeferredSyntaxAllowedAsLiteral() ) {
                    return;
                }
                this.err.jspError ( n, "jsp.error.el.template.deferred", new String[0] );
            }
            final StringBuilder expr = this.getBuffer();
            expr.append ( n.getType() ).append ( '{' ).append ( n.getText() ).append ( '}' );
            final ELNode.Nodes el = ELParser.parse ( expr.toString(), this.pageInfo.isDeferredSyntaxAllowedAsLiteral() );
            this.prepareExpression ( el, n, expr.toString() );
            n.setEL ( el );
        }
        @Override
        public void visit ( final Node.UninterpretedTag n ) throws JasperException {
            if ( n.getNamedAttributeNodes().size() != 0 ) {
                this.err.jspError ( n, "jsp.error.namedAttribute.invalidUse", new String[0] );
            }
            final Attributes attrs = n.getAttributes();
            if ( attrs != null ) {
                final int attrSize = attrs.getLength();
                final Node.JspAttribute[] jspAttrs = new Node.JspAttribute[attrSize];
                for ( int i = 0; i < attrSize; ++i ) {
                    final String value = attrs.getValue ( i );
                    if ( !this.pageInfo.isDeferredSyntaxAllowedAsLiteral() && this.containsDeferredSyntax ( value ) ) {
                        this.err.jspError ( n, "jsp.error.el.template.deferred", new String[0] );
                    }
                    jspAttrs[i] = this.getJspAttribute ( null, attrs.getQName ( i ), attrs.getURI ( i ), attrs.getLocalName ( i ), value, n, null, false );
                }
                n.setJspAttributes ( jspAttrs );
            }
            this.visitBody ( n );
        }
        private boolean containsDeferredSyntax ( final String value ) {
            if ( value == null ) {
                return false;
            }
            int i = 0;
            final int len = value.length();
            boolean prevCharIsEscape = false;
            while ( i < value.length() ) {
                final char c = value.charAt ( i );
                if ( c == '#' && i + 1 < len && value.charAt ( i + 1 ) == '{' && !prevCharIsEscape ) {
                    return true;
                }
                prevCharIsEscape = ( c == '\\' );
                ++i;
            }
            return false;
        }
        @Override
        public void visit ( final Node.CustomTag n ) throws JasperException {
            final TagInfo tagInfo = n.getTagInfo();
            if ( tagInfo == null ) {
                this.err.jspError ( n, "jsp.error.missing.tagInfo", n.getQName() );
            }
            if ( n.implementsSimpleTag() && tagInfo.getBodyContent().equalsIgnoreCase ( "JSP" ) ) {
                this.err.jspError ( n, "jsp.error.simpletag.badbodycontent", tagInfo.getTagClassName() );
            }
            if ( tagInfo.hasDynamicAttributes() && !n.implementsDynamicAttributes() ) {
                this.err.jspError ( n, "jsp.error.dynamic.attributes.not.implemented", n.getQName() );
            }
            final TagAttributeInfo[] tldAttrs = tagInfo.getAttributes();
            final String customActionUri = n.getURI();
            final Attributes attrs = n.getAttributes();
            final int attrsSize = ( attrs == null ) ? 0 : attrs.getLength();
            for ( int i = 0; i < tldAttrs.length; ++i ) {
                String attr = null;
                if ( attrs != null ) {
                    attr = attrs.getValue ( tldAttrs[i].getName() );
                    if ( attr == null ) {
                        attr = attrs.getValue ( customActionUri, tldAttrs[i].getName() );
                    }
                }
                final Node.NamedAttribute na = n.getNamedAttributeNode ( tldAttrs[i].getName() );
                if ( tldAttrs[i].isRequired() && attr == null && na == null ) {
                    this.err.jspError ( n, "jsp.error.missing_attribute", tldAttrs[i].getName(), n.getLocalName() );
                }
                if ( attr != null && na != null ) {
                    this.err.jspError ( n, "jsp.error.duplicate.name.jspattribute", tldAttrs[i].getName() );
                }
            }
            final Node.Nodes naNodes = n.getNamedAttributeNodes();
            final int jspAttrsSize = naNodes.size() + attrsSize;
            Node.JspAttribute[] jspAttrs = null;
            if ( jspAttrsSize > 0 ) {
                jspAttrs = new Node.JspAttribute[jspAttrsSize];
            }
            final Hashtable<String, Object> tagDataAttrs = new Hashtable<String, Object> ( attrsSize );
            this.checkXmlAttributes ( n, jspAttrs, tagDataAttrs );
            this.checkNamedAttributes ( n, jspAttrs, attrsSize, tagDataAttrs );
            final TagData tagData = new TagData ( ( Hashtable ) tagDataAttrs );
            final TagExtraInfo tei = tagInfo.getTagExtraInfo();
            if ( tei != null && tei.getVariableInfo ( tagData ) != null && tei.getVariableInfo ( tagData ).length > 0 && tagInfo.getTagVariableInfos().length > 0 ) {
                this.err.jspError ( "jsp.error.non_null_tei_and_var_subelems", n.getQName() );
            }
            n.setTagData ( tagData );
            n.setJspAttributes ( jspAttrs );
            this.visitBody ( n );
        }
        @Override
        public void visit ( final Node.JspElement n ) throws JasperException {
            final Attributes attrs = n.getAttributes();
            if ( attrs == null ) {
                this.err.jspError ( n, "jsp.error.jspelement.missing.name", new String[0] );
            }
            final int xmlAttrLen = attrs.getLength();
            final Node.Nodes namedAttrs = n.getNamedAttributeNodes();
            final int jspAttrSize = xmlAttrLen - 1 + namedAttrs.size();
            final Node.JspAttribute[] jspAttrs = new Node.JspAttribute[jspAttrSize];
            int jspAttrIndex = 0;
            for ( int i = 0; i < xmlAttrLen; ++i ) {
                if ( "name".equals ( attrs.getLocalName ( i ) ) ) {
                    n.setNameAttribute ( this.getJspAttribute ( null, attrs.getQName ( i ), attrs.getURI ( i ), attrs.getLocalName ( i ), attrs.getValue ( i ), n, null, false ) );
                } else if ( jspAttrIndex < jspAttrSize ) {
                    jspAttrs[jspAttrIndex++] = this.getJspAttribute ( null, attrs.getQName ( i ), attrs.getURI ( i ), attrs.getLocalName ( i ), attrs.getValue ( i ), n, null, false );
                }
            }
            if ( n.getNameAttribute() == null ) {
                this.err.jspError ( n, "jsp.error.jspelement.missing.name", new String[0] );
            }
            for ( int i = 0; i < namedAttrs.size(); ++i ) {
                final Node.NamedAttribute na = ( Node.NamedAttribute ) namedAttrs.getNode ( i );
                jspAttrs[jspAttrIndex++] = new Node.JspAttribute ( na, null, false );
            }
            n.setJspAttributes ( jspAttrs );
            this.visitBody ( n );
        }
        @Override
        public void visit ( final Node.JspOutput n ) throws JasperException {
            JspUtil.checkAttributes ( "jsp:output", n, ValidateVisitor.jspOutputAttrs, this.err );
            if ( n.getBody() != null ) {
                this.err.jspError ( n, "jsp.error.jspoutput.nonemptybody", new String[0] );
            }
            final String omitXmlDecl = n.getAttributeValue ( "omit-xml-declaration" );
            final String doctypeName = n.getAttributeValue ( "doctype-root-element" );
            final String doctypePublic = n.getAttributeValue ( "doctype-public" );
            final String doctypeSystem = n.getAttributeValue ( "doctype-system" );
            final String omitXmlDeclOld = this.pageInfo.getOmitXmlDecl();
            final String doctypeNameOld = this.pageInfo.getDoctypeName();
            final String doctypePublicOld = this.pageInfo.getDoctypePublic();
            final String doctypeSystemOld = this.pageInfo.getDoctypeSystem();
            if ( omitXmlDecl != null && omitXmlDeclOld != null && !omitXmlDecl.equals ( omitXmlDeclOld ) ) {
                this.err.jspError ( n, "jsp.error.jspoutput.conflict", "omit-xml-declaration", omitXmlDeclOld, omitXmlDecl );
            }
            if ( doctypeName != null && doctypeNameOld != null && !doctypeName.equals ( doctypeNameOld ) ) {
                this.err.jspError ( n, "jsp.error.jspoutput.conflict", "doctype-root-element", doctypeNameOld, doctypeName );
            }
            if ( doctypePublic != null && doctypePublicOld != null && !doctypePublic.equals ( doctypePublicOld ) ) {
                this.err.jspError ( n, "jsp.error.jspoutput.conflict", "doctype-public", doctypePublicOld, doctypePublic );
            }
            if ( doctypeSystem != null && doctypeSystemOld != null && !doctypeSystem.equals ( doctypeSystemOld ) ) {
                this.err.jspError ( n, "jsp.error.jspoutput.conflict", "doctype-system", doctypeSystemOld, doctypeSystem );
            }
            if ( ( doctypeName == null && doctypeSystem != null ) || ( doctypeName != null && doctypeSystem == null ) ) {
                this.err.jspError ( n, "jsp.error.jspoutput.doctypenamesystem", new String[0] );
            }
            if ( doctypePublic != null && doctypeSystem == null ) {
                this.err.jspError ( n, "jsp.error.jspoutput.doctypepulicsystem", new String[0] );
            }
            if ( omitXmlDecl != null ) {
                this.pageInfo.setOmitXmlDecl ( omitXmlDecl );
            }
            if ( doctypeName != null ) {
                this.pageInfo.setDoctypeName ( doctypeName );
            }
            if ( doctypeSystem != null ) {
                this.pageInfo.setDoctypeSystem ( doctypeSystem );
            }
            if ( doctypePublic != null ) {
                this.pageInfo.setDoctypePublic ( doctypePublic );
            }
        }
        @Override
        public void visit ( final Node.InvokeAction n ) throws JasperException {
            JspUtil.checkAttributes ( "Invoke", n, ValidateVisitor.invokeAttrs, this.err );
            final String scope = n.getTextAttribute ( "scope" );
            JspUtil.checkScope ( scope, n, this.err );
            final String var = n.getTextAttribute ( "var" );
            final String varReader = n.getTextAttribute ( "varReader" );
            if ( scope != null && var == null && varReader == null ) {
                this.err.jspError ( n, "jsp.error.missing_var_or_varReader", new String[0] );
            }
            if ( var != null && varReader != null ) {
                this.err.jspError ( n, "jsp.error.var_and_varReader", new String[0] );
            }
        }
        @Override
        public void visit ( final Node.DoBodyAction n ) throws JasperException {
            JspUtil.checkAttributes ( "DoBody", n, ValidateVisitor.doBodyAttrs, this.err );
            final String scope = n.getTextAttribute ( "scope" );
            JspUtil.checkScope ( scope, n, this.err );
            final String var = n.getTextAttribute ( "var" );
            final String varReader = n.getTextAttribute ( "varReader" );
            if ( scope != null && var == null && varReader == null ) {
                this.err.jspError ( n, "jsp.error.missing_var_or_varReader", new String[0] );
            }
            if ( var != null && varReader != null ) {
                this.err.jspError ( n, "jsp.error.var_and_varReader", new String[0] );
            }
        }
        private void checkXmlAttributes ( final Node.CustomTag n, final Node.JspAttribute[] jspAttrs, final Hashtable<String, Object> tagDataAttrs ) throws JasperException {
            final TagInfo tagInfo = n.getTagInfo();
            final TagAttributeInfo[] tldAttrs = tagInfo.getAttributes();
            final Attributes attrs = n.getAttributes();
            for ( int i = 0; attrs != null && i < attrs.getLength(); ++i ) {
                boolean found = false;
                final boolean runtimeExpression = ( n.getRoot().isXmlSyntax() && attrs.getValue ( i ).startsWith ( "%=" ) ) || ( !n.getRoot().isXmlSyntax() && attrs.getValue ( i ).startsWith ( "<%=" ) );
                boolean elExpression = false;
                boolean deferred = false;
                final double libraryVersion = Double.parseDouble ( tagInfo.getTagLibrary().getRequiredVersion() );
                final boolean deferredSyntaxAllowedAsLiteral = this.pageInfo.isDeferredSyntaxAllowedAsLiteral() || libraryVersion < 2.1;
                final String xmlAttributeValue = attrs.getValue ( i );
                ELNode.Nodes el = null;
                if ( !runtimeExpression && !this.pageInfo.isELIgnored() ) {
                    el = ELParser.parse ( xmlAttributeValue, deferredSyntaxAllowedAsLiteral );
                    for ( final ELNode node : el ) {
                        if ( node instanceof ELNode.Root ) {
                            if ( ( ( ELNode.Root ) node ).getType() == '$' ) {
                                if ( elExpression && deferred ) {
                                    this.err.jspError ( n, "jsp.error.attribute.deferredmix", new String[0] );
                                }
                                elExpression = true;
                            } else {
                                if ( ( ( ELNode.Root ) node ).getType() != '#' ) {
                                    continue;
                                }
                                if ( elExpression && !deferred ) {
                                    this.err.jspError ( n, "jsp.error.attribute.deferredmix", new String[0] );
                                }
                                elExpression = true;
                                deferred = true;
                            }
                        }
                    }
                }
                final boolean expression = runtimeExpression || elExpression;
                String textAttributeValue;
                if ( !elExpression && el != null ) {
                    final Iterator<ELNode> it = el.iterator();
                    if ( it.hasNext() ) {
                        textAttributeValue = it.next().getText();
                    } else {
                        textAttributeValue = "";
                    }
                } else {
                    textAttributeValue = xmlAttributeValue;
                }
                for ( int j = 0; tldAttrs != null && j < tldAttrs.length; ++j ) {
                    if ( attrs.getLocalName ( i ).equals ( tldAttrs[j].getName() ) && ( attrs.getURI ( i ) == null || attrs.getURI ( i ).length() == 0 || attrs.getURI ( i ).equals ( n.getURI() ) ) ) {
                        final TagAttributeInfo tldAttr = tldAttrs[j];
                        if ( tldAttr.canBeRequestTime() || tldAttr.isDeferredMethod() || tldAttr.isDeferredValue() ) {
                            if ( !expression ) {
                                String expectedType = null;
                                if ( tldAttr.isDeferredMethod() ) {
                                    String m = tldAttr.getMethodSignature();
                                    if ( m != null ) {
                                        m = m.trim();
                                        final int rti = m.indexOf ( 32 );
                                        if ( rti > 0 ) {
                                            expectedType = m.substring ( 0, rti ).trim();
                                        }
                                    } else {
                                        expectedType = "java.lang.Object";
                                    }
                                    if ( "void".equals ( expectedType ) ) {
                                        this.err.jspError ( n, "jsp.error.literal_with_void", tldAttr.getName() );
                                    }
                                }
                                if ( tldAttr.isDeferredValue() ) {
                                    expectedType = tldAttr.getExpectedTypeName();
                                }
                                Label_0838: {
                                    if ( expectedType != null ) {
                                        Class<?> expectedClass = String.class;
                                        try {
                                            expectedClass = JspUtil.toClass ( expectedType, this.loader );
                                        } catch ( ClassNotFoundException e ) {
                                            this.err.jspError ( n, "jsp.error.unknown_attribute_type", tldAttr.getName(), expectedType );
                                        }
                                        if ( !String.class.equals ( expectedClass ) && expectedClass != Long.TYPE && expectedClass != Double.TYPE && expectedClass != Byte.TYPE && expectedClass != Short.TYPE && expectedClass != Integer.TYPE && expectedClass != Float.TYPE && !Number.class.isAssignableFrom ( expectedClass ) && !Character.class.equals ( expectedClass ) && Character.TYPE != expectedClass && !Boolean.class.equals ( expectedClass ) && Boolean.TYPE != expectedClass ) {
                                            if ( !expectedClass.isEnum() ) {
                                                break Label_0838;
                                            }
                                        }
                                        try {
                                            this.expressionFactory.coerceToType ( ( Object ) textAttributeValue, ( Class ) expectedClass );
                                        } catch ( Exception e2 ) {
                                            this.err.jspError ( n, "jsp.error.coerce_to_type", tldAttr.getName(), expectedType, textAttributeValue );
                                        }
                                    }
                                }
                                jspAttrs[i] = new Node.JspAttribute ( tldAttr, attrs.getQName ( i ), attrs.getURI ( i ), attrs.getLocalName ( i ), textAttributeValue, false, null, false );
                            } else {
                                if ( deferred && !tldAttr.isDeferredMethod() && !tldAttr.isDeferredValue() ) {
                                    this.err.jspError ( n, "jsp.error.attribute.custom.non_rt_with_expr", tldAttr.getName() );
                                }
                                if ( !deferred && !tldAttr.canBeRequestTime() ) {
                                    this.err.jspError ( n, "jsp.error.attribute.custom.non_rt_with_expr", tldAttr.getName() );
                                }
                                jspAttrs[i] = this.getJspAttribute ( tldAttr, attrs.getQName ( i ), attrs.getURI ( i ), attrs.getLocalName ( i ), xmlAttributeValue, n, el, false );
                            }
                        } else {
                            if ( expression ) {
                                this.err.jspError ( n, "jsp.error.attribute.custom.non_rt_with_expr", tldAttr.getName() );
                            }
                            jspAttrs[i] = new Node.JspAttribute ( tldAttr, attrs.getQName ( i ), attrs.getURI ( i ), attrs.getLocalName ( i ), textAttributeValue, false, null, false );
                        }
                        if ( expression ) {
                            tagDataAttrs.put ( attrs.getQName ( i ), TagData.REQUEST_TIME_VALUE );
                        } else {
                            tagDataAttrs.put ( attrs.getQName ( i ), textAttributeValue );
                        }
                        found = true;
                        break;
                    }
                }
                if ( !found ) {
                    if ( tagInfo.hasDynamicAttributes() ) {
                        jspAttrs[i] = this.getJspAttribute ( null, attrs.getQName ( i ), attrs.getURI ( i ), attrs.getLocalName ( i ), xmlAttributeValue, n, el, true );
                    } else {
                        this.err.jspError ( n, "jsp.error.bad_attribute", attrs.getQName ( i ), n.getLocalName() );
                    }
                }
            }
        }
        private void checkNamedAttributes ( final Node.CustomTag n, final Node.JspAttribute[] jspAttrs, final int start, final Hashtable<String, Object> tagDataAttrs ) throws JasperException {
            final TagInfo tagInfo = n.getTagInfo();
            final TagAttributeInfo[] tldAttrs = tagInfo.getAttributes();
            final Node.Nodes naNodes = n.getNamedAttributeNodes();
            for ( int i = 0; i < naNodes.size(); ++i ) {
                final Node.NamedAttribute na = ( Node.NamedAttribute ) naNodes.getNode ( i );
                boolean found = false;
                for ( int j = 0; j < tldAttrs.length; ++j ) {
                    final String attrPrefix = na.getPrefix();
                    if ( na.getLocalName().equals ( tldAttrs[j].getName() ) && ( attrPrefix == null || attrPrefix.length() == 0 || attrPrefix.equals ( n.getPrefix() ) ) ) {
                        jspAttrs[start + i] = new Node.JspAttribute ( na, tldAttrs[j], false );
                        NamedAttributeVisitor nav = null;
                        if ( na.getBody() != null ) {
                            nav = new NamedAttributeVisitor();
                            na.getBody().visit ( nav );
                        }
                        if ( nav != null && nav.hasDynamicContent() ) {
                            tagDataAttrs.put ( na.getName(), TagData.REQUEST_TIME_VALUE );
                        } else {
                            tagDataAttrs.put ( na.getName(), na.getText() );
                        }
                        found = true;
                        break;
                    }
                }
                if ( !found ) {
                    if ( tagInfo.hasDynamicAttributes() ) {
                        jspAttrs[start + i] = new Node.JspAttribute ( na, null, true );
                    } else {
                        this.err.jspError ( n, "jsp.error.bad_attribute", na.getName(), n.getLocalName() );
                    }
                }
            }
        }
        private Node.JspAttribute getJspAttribute ( final TagAttributeInfo tai, final String qName, final String uri, final String localName, String value, final Node n, ELNode.Nodes el, final boolean dynamic ) throws JasperException {
            Node.JspAttribute result = null;
            if ( value != null ) {
                if ( n.getRoot().isXmlSyntax() && value.startsWith ( "%=" ) ) {
                    result = new Node.JspAttribute ( tai, qName, uri, localName, value.substring ( 2, value.length() - 1 ), true, null, dynamic );
                } else if ( !n.getRoot().isXmlSyntax() && value.startsWith ( "<%=" ) ) {
                    result = new Node.JspAttribute ( tai, qName, uri, localName, value.substring ( 3, value.length() - 2 ), true, null, dynamic );
                } else {
                    if ( !this.pageInfo.isELIgnored() ) {
                        if ( el == null ) {
                            el = ELParser.parse ( value, this.pageInfo.isDeferredSyntaxAllowedAsLiteral() );
                        }
                        if ( el.containsEL() ) {
                            this.validateFunctions ( el, n );
                        } else {
                            final Iterator<ELNode> it = el.iterator();
                            if ( it.hasNext() ) {
                                value = it.next().getText();
                            } else {
                                value = "";
                            }
                            el = null;
                        }
                    }
                    if ( n instanceof Node.UninterpretedTag && n.getRoot().isXmlSyntax() ) {
                        if ( el != null ) {
                            final XmlEscapeNonELVisitor v = new XmlEscapeNonELVisitor ( this.pageInfo.isDeferredSyntaxAllowedAsLiteral() );
                            el.visit ( v );
                            value = v.getText();
                        } else {
                            value = Validator.xmlEscape ( value );
                        }
                    }
                    result = new Node.JspAttribute ( tai, qName, uri, localName, value, false, el, dynamic );
                    if ( el != null ) {
                        final ELContextImpl ctx = new ELContextImpl ( this.expressionFactory );
                        ctx.setFunctionMapper ( this.getFunctionMapper ( el ) );
                        try {
                            result.validateEL ( this.pageInfo.getExpressionFactory(), ctx );
                        } catch ( ELException e ) {
                            this.err.jspError ( n.getStart(), "jsp.error.invalid.expression", value, e.toString() );
                        }
                    }
                }
            } else {
                final Node.NamedAttribute namedAttributeNode = n.getNamedAttributeNode ( qName );
                if ( namedAttributeNode != null ) {
                    result = new Node.JspAttribute ( namedAttributeNode, tai, dynamic );
                }
            }
            return result;
        }
        private StringBuilder getBuffer() {
            this.buf.setLength ( 0 );
            return this.buf;
        }
        private boolean isExpression ( final Node n, final String value, final boolean checkDeferred ) {
            final boolean runtimeExpression = ( n.getRoot().isXmlSyntax() && value.startsWith ( "%=" ) ) || ( !n.getRoot().isXmlSyntax() && value.startsWith ( "<%=" ) );
            boolean elExpression = false;
            if ( !runtimeExpression && !this.pageInfo.isELIgnored() ) {
                for ( final ELNode node : ELParser.parse ( value, this.pageInfo.isDeferredSyntaxAllowedAsLiteral() ) ) {
                    if ( node instanceof ELNode.Root ) {
                        if ( ( ( ELNode.Root ) node ).getType() == '$' ) {
                            elExpression = true;
                            break;
                        }
                        if ( checkDeferred && !this.pageInfo.isDeferredSyntaxAllowedAsLiteral() && ( ( ELNode.Root ) node ).getType() == '#' ) {
                            elExpression = true;
                            break;
                        }
                        continue;
                    }
                }
            }
            return runtimeExpression || elExpression;
        }
        private void throwErrorIfExpression ( final Node n, final String attrName, final String actionName ) throws JasperException {
            if ( n.getAttributes() != null && n.getAttributes().getValue ( attrName ) != null && this.isExpression ( n, n.getAttributes().getValue ( attrName ), true ) ) {
                this.err.jspError ( n, "jsp.error.attribute.standard.non_rt_with_expr", attrName, actionName );
            }
        }
        private String findUri ( final String prefix, final Node n ) {
            for ( Node p = n; p != null; p = p.getParent() ) {
                final Attributes attrs = p.getTaglibAttributes();
                if ( attrs != null ) {
                    for ( int i = 0; i < attrs.getLength(); ++i ) {
                        final String name = attrs.getQName ( i );
                        final int k = name.indexOf ( 58 );
                        if ( prefix == null && k < 0 ) {
                            return attrs.getValue ( i );
                        }
                        if ( prefix != null && k >= 0 && prefix.equals ( name.substring ( k + 1 ) ) ) {
                            return attrs.getValue ( i );
                        }
                    }
                }
            }
            return null;
        }
        private void validateFunctions ( final ELNode.Nodes el, final Node n ) throws JasperException {
            class FVVisitor extends ELNode.Visitor {
                private Node n = n;
                @Override
                public void visit ( final ELNode.Function func ) throws JasperException {
                    final String prefix = func.getPrefix();
                    final String function = func.getName();
                    String uri = null;
                    if ( this.n.getRoot().isXmlSyntax() ) {
                        uri = ValidateVisitor.this.findUri ( prefix, this.n );
                    } else if ( prefix != null ) {
                        uri = ValidateVisitor.this.pageInfo.getURI ( prefix );
                    }
                    if ( uri == null ) {
                        if ( prefix == null ) {
                            return;
                        }
                        ValidateVisitor.this.err.jspError ( this.n, "jsp.error.attribute.invalidPrefix", prefix );
                    }
                    final TagLibraryInfo taglib = ValidateVisitor.this.pageInfo.getTaglib ( uri );
                    FunctionInfo funcInfo = null;
                    if ( taglib != null ) {
                        funcInfo = taglib.getFunction ( function );
                    }
                    if ( funcInfo == null ) {
                        ValidateVisitor.this.err.jspError ( this.n, "jsp.error.noFunction", function );
                    }
                    func.setUri ( uri );
                    func.setFunctionInfo ( funcInfo );
                    ValidateVisitor.this.processSignature ( func );
                }
            }
            el.visit ( new FVVisitor() );
        }
        private void prepareExpression ( final ELNode.Nodes el, final Node n, final String expr ) throws JasperException {
            this.validateFunctions ( el, n );
            final ELContextImpl ctx = new ELContextImpl ( this.expressionFactory );
            ctx.setFunctionMapper ( this.getFunctionMapper ( el ) );
            final ExpressionFactory ef = this.pageInfo.getExpressionFactory();
            try {
                ef.createValueExpression ( ( ELContext ) ctx, expr, ( Class ) Object.class );
            } catch ( ELException ex ) {}
        }
        private void processSignature ( final ELNode.Function func ) throws JasperException {
            func.setMethodName ( this.getMethod ( func ) );
            func.setParameters ( this.getParameters ( func ) );
        }
        private String getMethod ( final ELNode.Function func ) throws JasperException {
            final FunctionInfo funcInfo = func.getFunctionInfo();
            final String signature = funcInfo.getFunctionSignature();
            final int start = signature.indexOf ( 32 );
            if ( start < 0 ) {
                this.err.jspError ( "jsp.error.tld.fn.invalid.signature", func.getPrefix(), func.getName() );
            }
            final int end = signature.indexOf ( 40 );
            if ( end < 0 ) {
                this.err.jspError ( "jsp.error.tld.fn.invalid.signature.parenexpected", func.getPrefix(), func.getName() );
            }
            return signature.substring ( start + 1, end ).trim();
        }
        private String[] getParameters ( final ELNode.Function func ) throws JasperException {
            final FunctionInfo funcInfo = func.getFunctionInfo();
            final String signature = funcInfo.getFunctionSignature();
            final ArrayList<String> params = new ArrayList<String>();
            int start = signature.indexOf ( 40 ) + 1;
            boolean lastArg = false;
            while ( true ) {
                int p = signature.indexOf ( 44, start );
                if ( p < 0 ) {
                    p = signature.indexOf ( 41, start );
                    if ( p < 0 ) {
                        this.err.jspError ( "jsp.error.tld.fn.invalid.signature", func.getPrefix(), func.getName() );
                    }
                    lastArg = true;
                }
                final String arg = signature.substring ( start, p ).trim();
                if ( !"".equals ( arg ) ) {
                    params.add ( arg );
                }
                if ( lastArg ) {
                    break;
                }
                start = p + 1;
            }
            return params.toArray ( new String[params.size()] );
        }
        private FunctionMapper getFunctionMapper ( final ELNode.Nodes el ) throws JasperException {
            class ValidateFunctionMapper extends FunctionMapper {
                private HashMap<String, Method> fnmap;
                ValidateFunctionMapper() {
                    this.fnmap = new HashMap<String, Method>();
                }
                public void mapFunction ( final String prefix, final String localName, final Method method ) {
                    this.fnmap.put ( prefix + ":" + localName, method );
                }
                public Method resolveFunction ( final String prefix, final String localName ) {
                    return this.fnmap.get ( prefix + ":" + localName );
                }
            }
            final ValidateFunctionMapper fmapper = new ValidateFunctionMapper();
            class MapperELVisitor extends ELNode.Visitor {
                private ValidateFunctionMapper fmapper = fmapper;
                @Override
                public void visit ( final ELNode.Function n ) throws JasperException {
                    if ( n.getFunctionInfo() == null ) {
                        return;
                    }
                    Class<?> c = null;
                    Method method = null;
                    try {
                        c = ValidateVisitor.this.loader.loadClass ( n.getFunctionInfo().getFunctionClass() );
                    } catch ( ClassNotFoundException e ) {
                        ValidateVisitor.this.err.jspError ( "jsp.error.function.classnotfound", n.getFunctionInfo().getFunctionClass(), n.getPrefix() + ':' + n.getName(), e.getMessage() );
                    }
                    final String[] paramTypes = n.getParameters();
                    final int size = paramTypes.length;
                    final Class<?>[] params = ( Class<?>[] ) new Class[size];
                    int i = 0;
                    try {
                        for ( i = 0; i < size; ++i ) {
                            params[i] = JspUtil.toClass ( paramTypes[i], ValidateVisitor.this.loader );
                        }
                        method = c.getDeclaredMethod ( n.getMethodName(), params );
                    } catch ( ClassNotFoundException e2 ) {
                        ValidateVisitor.this.err.jspError ( "jsp.error.signature.classnotfound", paramTypes[i], n.getPrefix() + ':' + n.getName(), e2.getMessage() );
                    } catch ( NoSuchMethodException e3 ) {
                        ValidateVisitor.this.err.jspError ( "jsp.error.noFunctionMethod", n.getMethodName(), n.getName(), c.getName() );
                    }
                    this.fmapper.mapFunction ( n.getPrefix(), n.getName(), method );
                }
            }
            el.visit ( new MapperELVisitor() );
            return fmapper;
        }
        static {
            jspRootAttrs = new JspUtil.ValidAttribute[] { new JspUtil.ValidAttribute ( "xsi:schemaLocation" ), new JspUtil.ValidAttribute ( "version", true ) };
            includeDirectiveAttrs = new JspUtil.ValidAttribute[] { new JspUtil.ValidAttribute ( "file", true ) };
            taglibDirectiveAttrs = new JspUtil.ValidAttribute[] { new JspUtil.ValidAttribute ( "uri" ), new JspUtil.ValidAttribute ( "tagdir" ), new JspUtil.ValidAttribute ( "prefix", true ) };
            includeActionAttrs = new JspUtil.ValidAttribute[] { new JspUtil.ValidAttribute ( "page", true ), new JspUtil.ValidAttribute ( "flush" ) };
            paramActionAttrs = new JspUtil.ValidAttribute[] { new JspUtil.ValidAttribute ( "name", true ), new JspUtil.ValidAttribute ( "value", true ) };
            forwardActionAttrs = new JspUtil.ValidAttribute[] { new JspUtil.ValidAttribute ( "page", true ) };
            getPropertyAttrs = new JspUtil.ValidAttribute[] { new JspUtil.ValidAttribute ( "name", true ), new JspUtil.ValidAttribute ( "property", true ) };
            setPropertyAttrs = new JspUtil.ValidAttribute[] { new JspUtil.ValidAttribute ( "name", true ), new JspUtil.ValidAttribute ( "property", true ), new JspUtil.ValidAttribute ( "value", false ), new JspUtil.ValidAttribute ( "param" ) };
            useBeanAttrs = new JspUtil.ValidAttribute[] { new JspUtil.ValidAttribute ( "id", true ), new JspUtil.ValidAttribute ( "scope" ), new JspUtil.ValidAttribute ( "class" ), new JspUtil.ValidAttribute ( "type" ), new JspUtil.ValidAttribute ( "beanName", false ) };
            plugInAttrs = new JspUtil.ValidAttribute[] { new JspUtil.ValidAttribute ( "type", true ), new JspUtil.ValidAttribute ( "code", true ), new JspUtil.ValidAttribute ( "codebase" ), new JspUtil.ValidAttribute ( "align" ), new JspUtil.ValidAttribute ( "archive" ), new JspUtil.ValidAttribute ( "height", false ), new JspUtil.ValidAttribute ( "hspace" ), new JspUtil.ValidAttribute ( "jreversion" ), new JspUtil.ValidAttribute ( "name" ), new JspUtil.ValidAttribute ( "vspace" ), new JspUtil.ValidAttribute ( "width", false ), new JspUtil.ValidAttribute ( "nspluginurl" ), new JspUtil.ValidAttribute ( "iepluginurl" ) };
            attributeAttrs = new JspUtil.ValidAttribute[] { new JspUtil.ValidAttribute ( "name", true ), new JspUtil.ValidAttribute ( "trim" ), new JspUtil.ValidAttribute ( "omit" ) };
            invokeAttrs = new JspUtil.ValidAttribute[] { new JspUtil.ValidAttribute ( "fragment", true ), new JspUtil.ValidAttribute ( "var" ), new JspUtil.ValidAttribute ( "varReader" ), new JspUtil.ValidAttribute ( "scope" ) };
            doBodyAttrs = new JspUtil.ValidAttribute[] { new JspUtil.ValidAttribute ( "var" ), new JspUtil.ValidAttribute ( "varReader" ), new JspUtil.ValidAttribute ( "scope" ) };
            jspOutputAttrs = new JspUtil.ValidAttribute[] { new JspUtil.ValidAttribute ( "omit-xml-declaration" ), new JspUtil.ValidAttribute ( "doctype-root-element" ), new JspUtil.ValidAttribute ( "doctype-public" ), new JspUtil.ValidAttribute ( "doctype-system" ) };
        }
        private static class XmlEscapeNonELVisitor extends ELParser.TextBuilder {
            protected XmlEscapeNonELVisitor ( final boolean isDeferredSyntaxAllowedAsLiteral ) {
                super ( isDeferredSyntaxAllowedAsLiteral );
            }
            @Override
            public void visit ( final ELNode.Text n ) throws JasperException {
                this.output.append ( ELParser.escapeLiteralExpression ( Validator.xmlEscape ( n.getText() ), this.isDeferredSyntaxAllowedAsLiteral ) );
            }
        }
        private static class NamedAttributeVisitor extends Node.Visitor {
            private boolean hasDynamicContent;
            public void doVisit ( final Node n ) throws JasperException {
                if ( ! ( n instanceof Node.JspText ) && ! ( n instanceof Node.TemplateText ) ) {
                    this.hasDynamicContent = true;
                }
                this.visitBody ( n );
            }
            public boolean hasDynamicContent() {
                return this.hasDynamicContent;
            }
        }
    }
    private static class TagExtraInfoVisitor extends Node.Visitor {
        private final ErrorDispatcher err;
        TagExtraInfoVisitor ( final Compiler compiler ) {
            this.err = compiler.getErrorDispatcher();
        }
        @Override
        public void visit ( final Node.CustomTag n ) throws JasperException {
            final TagInfo tagInfo = n.getTagInfo();
            if ( tagInfo == null ) {
                this.err.jspError ( n, "jsp.error.missing.tagInfo", n.getQName() );
            }
            final ValidationMessage[] errors = tagInfo.validate ( n.getTagData() );
            if ( errors != null && errors.length != 0 ) {
                final StringBuilder errMsg = new StringBuilder();
                errMsg.append ( "<h3>" );
                errMsg.append ( Localizer.getMessage ( "jsp.error.tei.invalid.attributes", n.getQName() ) );
                errMsg.append ( "</h3>" );
                for ( int i = 0; i < errors.length; ++i ) {
                    errMsg.append ( "<p>" );
                    if ( errors[i].getId() != null ) {
                        errMsg.append ( errors[i].getId() );
                        errMsg.append ( ": " );
                    }
                    errMsg.append ( errors[i].getMessage() );
                    errMsg.append ( "</p>" );
                }
                this.err.jspError ( n, errMsg.toString(), new String[0] );
            }
            this.visitBody ( n );
        }
    }
}
