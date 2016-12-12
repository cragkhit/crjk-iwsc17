package org.apache.jasper.compiler;
import org.apache.jasper.JasperException;
import java.util.ArrayList;
private static class FragmentHelperClass {
    private boolean used;
    private ArrayList<Fragment> fragments;
    private String className;
    private GenBuffer classBuffer;
    public FragmentHelperClass ( final String className ) {
        this.used = false;
        this.fragments = new ArrayList<Fragment>();
        this.classBuffer = new GenBuffer();
        this.className = className;
    }
    public String getClassName() {
        return this.className;
    }
    public boolean isUsed() {
        return this.used;
    }
    public void generatePreamble() {
        final ServletWriter out = this.classBuffer.getOut();
        out.println();
        out.pushIndent();
        out.printil ( "private class " + this.className );
        out.printil ( "    extends org.apache.jasper.runtime.JspFragmentHelper" );
        out.printil ( "{" );
        out.pushIndent();
        out.printil ( "private javax.servlet.jsp.tagext.JspTag _jspx_parent;" );
        out.printil ( "private int[] _jspx_push_body_count;" );
        out.println();
        out.printil ( "public " + this.className + "( int discriminator, javax.servlet.jsp.JspContext jspContext, javax.servlet.jsp.tagext.JspTag _jspx_parent, int[] _jspx_push_body_count ) {" );
        out.pushIndent();
        out.printil ( "super( discriminator, jspContext, _jspx_parent );" );
        out.printil ( "this._jspx_parent = _jspx_parent;" );
        out.printil ( "this._jspx_push_body_count = _jspx_push_body_count;" );
        out.popIndent();
        out.printil ( "}" );
    }
    public Fragment openFragment ( final Node parent, final int methodNesting ) throws JasperException {
        final Fragment result = new Fragment ( this.fragments.size(), parent );
        this.fragments.add ( result );
        this.used = true;
        parent.setInnerClassName ( this.className );
        final ServletWriter out = result.getGenBuffer().getOut();
        out.pushIndent();
        out.pushIndent();
        if ( methodNesting > 0 ) {
            out.printin ( "public boolean invoke" );
        } else {
            out.printin ( "public void invoke" );
        }
        out.println ( result.getId() + "( javax.servlet.jsp.JspWriter out ) " );
        out.pushIndent();
        out.printil ( "throws java.lang.Throwable" );
        out.popIndent();
        out.printil ( "{" );
        out.pushIndent();
        Generator.access$1000 ( out, parent );
        return result;
    }
    public void closeFragment ( final Fragment fragment, final int methodNesting ) {
        final ServletWriter out = fragment.getGenBuffer().getOut();
        if ( methodNesting > 0 ) {
            out.printil ( "return false;" );
        } else {
            out.printil ( "return;" );
        }
        out.popIndent();
        out.printil ( "}" );
    }
    public void generatePostamble() {
        final ServletWriter out = this.classBuffer.getOut();
        for ( int i = 0; i < this.fragments.size(); ++i ) {
            final Fragment fragment = this.fragments.get ( i );
            fragment.getGenBuffer().adjustJavaLines ( out.getJavaLine() - 1 );
            out.printMultiLn ( fragment.getGenBuffer().toString() );
        }
        out.printil ( "public void invoke( java.io.Writer writer )" );
        out.pushIndent();
        out.printil ( "throws javax.servlet.jsp.JspException" );
        out.popIndent();
        out.printil ( "{" );
        out.pushIndent();
        out.printil ( "javax.servlet.jsp.JspWriter out = null;" );
        out.printil ( "if( writer != null ) {" );
        out.pushIndent();
        out.printil ( "out = this.jspContext.pushBody(writer);" );
        out.popIndent();
        out.printil ( "} else {" );
        out.pushIndent();
        out.printil ( "out = this.jspContext.getOut();" );
        out.popIndent();
        out.printil ( "}" );
        out.printil ( "try {" );
        out.pushIndent();
        out.printil ( "Object _jspx_saved_JspContext = this.jspContext.getELContext().getContext(javax.servlet.jsp.JspContext.class);" );
        out.printil ( "this.jspContext.getELContext().putContext(javax.servlet.jsp.JspContext.class,this.jspContext);" );
        out.printil ( "switch( this.discriminator ) {" );
        out.pushIndent();
        for ( int i = 0; i < this.fragments.size(); ++i ) {
            out.printil ( "case " + i + ":" );
            out.pushIndent();
            out.printil ( "invoke" + i + "( out );" );
            out.printil ( "break;" );
            out.popIndent();
        }
        out.popIndent();
        out.printil ( "}" );
        out.printil ( "jspContext.getELContext().putContext(javax.servlet.jsp.JspContext.class,_jspx_saved_JspContext);" );
        out.popIndent();
        out.printil ( "}" );
        out.printil ( "catch( java.lang.Throwable e ) {" );
        out.pushIndent();
        out.printil ( "if (e instanceof javax.servlet.jsp.SkipPageException)" );
        out.printil ( "    throw (javax.servlet.jsp.SkipPageException) e;" );
        out.printil ( "throw new javax.servlet.jsp.JspException( e );" );
        out.popIndent();
        out.printil ( "}" );
        out.printil ( "finally {" );
        out.pushIndent();
        out.printil ( "if( writer != null ) {" );
        out.pushIndent();
        out.printil ( "this.jspContext.popBody();" );
        out.popIndent();
        out.printil ( "}" );
        out.popIndent();
        out.printil ( "}" );
        out.popIndent();
        out.printil ( "}" );
        out.popIndent();
        out.printil ( "}" );
        out.popIndent();
    }
    @Override
    public String toString() {
        return this.classBuffer.toString();
    }
    public void adjustJavaLines ( final int offset ) {
        for ( int i = 0; i < this.fragments.size(); ++i ) {
            final Fragment fragment = this.fragments.get ( i );
            fragment.getGenBuffer().adjustJavaLines ( offset );
        }
    }
    private static class Fragment {
        private GenBuffer genBuffer;
        private int id;
        public Fragment ( final int id, final Node node ) {
            this.id = id;
            this.genBuffer = new GenBuffer ( null, node.getBody() );
        }
        public GenBuffer getGenBuffer() {
            return this.genBuffer;
        }
        public int getId() {
            return this.id;
        }
    }
}
