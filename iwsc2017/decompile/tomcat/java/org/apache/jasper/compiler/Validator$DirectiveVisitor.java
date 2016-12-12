package org.apache.jasper.compiler;
import java.util.List;
import java.util.Vector;
import java.util.Locale;
import org.xml.sax.Attributes;
import org.apache.jasper.JasperException;
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
