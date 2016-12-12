package org.apache.jasper.compiler;
import java.util.ArrayList;
import java.util.List;
public class SmapGenerator {
    private String outputFileName;
    private String defaultStratum;
    private final List<SmapStratum> strata;
    private final List<String> embedded;
    private boolean doEmbedded;
    public SmapGenerator() {
        this.defaultStratum = "Java";
        this.strata = new ArrayList<SmapStratum>();
        this.embedded = new ArrayList<String>();
        this.doEmbedded = true;
    }
    public synchronized void setOutputFileName ( final String x ) {
        this.outputFileName = x;
    }
    public synchronized void addStratum ( final SmapStratum stratum, final boolean defaultStratum ) {
        this.strata.add ( stratum );
        if ( defaultStratum ) {
            this.defaultStratum = stratum.getStratumName();
        }
    }
    public synchronized void addSmap ( final String smap, final String stratumName ) {
        this.embedded.add ( "*O " + stratumName + "\n" + smap + "*C " + stratumName + "\n" );
    }
    public void setDoEmbedded ( final boolean status ) {
        this.doEmbedded = status;
    }
    public synchronized String getString() {
        if ( this.outputFileName == null ) {
            throw new IllegalStateException();
        }
        final StringBuilder out = new StringBuilder();
        out.append ( "SMAP\n" );
        out.append ( this.outputFileName + '\n' );
        out.append ( this.defaultStratum + '\n' );
        if ( this.doEmbedded ) {
            for ( int nEmbedded = this.embedded.size(), i = 0; i < nEmbedded; ++i ) {
                out.append ( this.embedded.get ( i ) );
            }
        }
        for ( int nStrata = this.strata.size(), i = 0; i < nStrata; ++i ) {
            final SmapStratum s = this.strata.get ( i );
            out.append ( s.getString() );
        }
        out.append ( "*E\n" );
        return out.toString();
    }
    @Override
    public String toString() {
        return this.getString();
    }
    public static void main ( final String[] args ) {
        final SmapGenerator g = new SmapGenerator();
        g.setOutputFileName ( "foo.java" );
        SmapStratum s = new SmapStratum ( "JSP" );
        s.addFile ( "foo.jsp" );
        s.addFile ( "bar.jsp", "/foo/foo/bar.jsp" );
        s.addLineData ( 1, "foo.jsp", 1, 1, 1 );
        s.addLineData ( 2, "foo.jsp", 1, 6, 1 );
        s.addLineData ( 3, "foo.jsp", 2, 10, 5 );
        s.addLineData ( 20, "bar.jsp", 1, 30, 1 );
        g.addStratum ( s, true );
        System.out.print ( g );
        System.out.println ( "---" );
        final SmapGenerator embedded = new SmapGenerator();
        embedded.setOutputFileName ( "blargh.tier2" );
        s = new SmapStratum ( "Tier2" );
        s.addFile ( "1.tier2" );
        s.addLineData ( 1, "1.tier2", 1, 1, 1 );
        embedded.addStratum ( s, true );
        g.addSmap ( embedded.toString(), "JSP" );
        System.out.println ( g );
    }
}
