package org.apache.jasper.compiler;
import org.xml.sax.Attributes;
import java.util.ArrayList;
import org.apache.jasper.JasperException;
import java.util.HashMap;
private static class SmapGenVisitor extends Node.Visitor {
    private SmapStratum smap;
    private final boolean breakAtLF;
    private final HashMap<String, SmapStratum> innerClassMap;
    SmapGenVisitor ( final SmapStratum s, final boolean breakAtLF, final HashMap<String, SmapStratum> map ) {
        this.smap = s;
        this.breakAtLF = breakAtLF;
        this.innerClassMap = map;
    }
    public void visitBody ( final Node n ) throws JasperException {
        final SmapStratum smapSave = this.smap;
        final String innerClass = n.getInnerClassName();
        if ( innerClass != null ) {
            this.smap = this.innerClassMap.get ( innerClass );
        }
        super.visitBody ( n );
        this.smap = smapSave;
    }
    @Override
    public void visit ( final Node.Declaration n ) throws JasperException {
        this.doSmapText ( n );
    }
    @Override
    public void visit ( final Node.Expression n ) throws JasperException {
        this.doSmapText ( n );
    }
    @Override
    public void visit ( final Node.Scriptlet n ) throws JasperException {
        this.doSmapText ( n );
    }
    @Override
    public void visit ( final Node.IncludeAction n ) throws JasperException {
        this.doSmap ( n );
        this.visitBody ( n );
    }
    @Override
    public void visit ( final Node.ForwardAction n ) throws JasperException {
        this.doSmap ( n );
        this.visitBody ( n );
    }
    @Override
    public void visit ( final Node.GetProperty n ) throws JasperException {
        this.doSmap ( n );
        this.visitBody ( n );
    }
    @Override
    public void visit ( final Node.SetProperty n ) throws JasperException {
        this.doSmap ( n );
        this.visitBody ( n );
    }
    @Override
    public void visit ( final Node.UseBean n ) throws JasperException {
        this.doSmap ( n );
        this.visitBody ( n );
    }
    @Override
    public void visit ( final Node.PlugIn n ) throws JasperException {
        this.doSmap ( n );
        this.visitBody ( n );
    }
    @Override
    public void visit ( final Node.CustomTag n ) throws JasperException {
        this.doSmap ( n );
        this.visitBody ( n );
    }
    @Override
    public void visit ( final Node.UninterpretedTag n ) throws JasperException {
        this.doSmap ( n );
        this.visitBody ( n );
    }
    @Override
    public void visit ( final Node.JspElement n ) throws JasperException {
        this.doSmap ( n );
        this.visitBody ( n );
    }
    @Override
    public void visit ( final Node.JspText n ) throws JasperException {
        this.doSmap ( n );
        this.visitBody ( n );
    }
    @Override
    public void visit ( final Node.NamedAttribute n ) throws JasperException {
        this.visitBody ( n );
    }
    @Override
    public void visit ( final Node.JspBody n ) throws JasperException {
        this.doSmap ( n );
        this.visitBody ( n );
    }
    @Override
    public void visit ( final Node.InvokeAction n ) throws JasperException {
        this.doSmap ( n );
        this.visitBody ( n );
    }
    @Override
    public void visit ( final Node.DoBodyAction n ) throws JasperException {
        this.doSmap ( n );
        this.visitBody ( n );
    }
    @Override
    public void visit ( final Node.ELExpression n ) throws JasperException {
        this.doSmap ( n );
    }
    @Override
    public void visit ( final Node.TemplateText n ) throws JasperException {
        final Mark mark = n.getStart();
        if ( mark == null ) {
            return;
        }
        final String fileName = mark.getFile();
        this.smap.addFile ( SmapUtil.access$100 ( fileName ), fileName );
        final int iInputStartLine = mark.getLineNumber();
        int iOutputStartLine = n.getBeginJavaLine();
        final int iOutputLineIncrement = this.breakAtLF ? 1 : 0;
        this.smap.addLineData ( iInputStartLine, fileName, 1, iOutputStartLine, iOutputLineIncrement );
        final ArrayList<Integer> extraSmap = n.getExtraSmap();
        if ( extraSmap != null ) {
            for ( int i = 0; i < extraSmap.size(); ++i ) {
                iOutputStartLine += iOutputLineIncrement;
                this.smap.addLineData ( iInputStartLine + extraSmap.get ( i ), fileName, 1, iOutputStartLine, iOutputLineIncrement );
            }
        }
    }
    private void doSmap ( final Node n, final int inLineCount, final int outIncrement, final int skippedLines ) {
        final Mark mark = n.getStart();
        if ( mark == null ) {
            return;
        }
        final String unqualifiedName = SmapUtil.access$100 ( mark.getFile() );
        this.smap.addFile ( unqualifiedName, mark.getFile() );
        this.smap.addLineData ( mark.getLineNumber() + skippedLines, mark.getFile(), inLineCount - skippedLines, n.getBeginJavaLine() + skippedLines, outIncrement );
    }
    private void doSmap ( final Node n ) {
        this.doSmap ( n, 1, n.getEndJavaLine() - n.getBeginJavaLine(), 0 );
    }
    private void doSmapText ( final Node n ) {
        final String text = n.getText();
        int index = 0;
        int next = 0;
        int lineCount = 1;
        int skippedLines = 0;
        boolean slashStarSeen = false;
        boolean beginning = true;
        while ( ( next = text.indexOf ( 10, index ) ) > -1 ) {
            if ( beginning ) {
                final String line = text.substring ( index, next ).trim();
                if ( !slashStarSeen && line.startsWith ( "/*" ) ) {
                    slashStarSeen = true;
                }
                if ( slashStarSeen ) {
                    ++skippedLines;
                    final int endIndex = line.indexOf ( "*/" );
                    if ( endIndex >= 0 ) {
                        slashStarSeen = false;
                        if ( endIndex < line.length() - 2 ) {
                            --skippedLines;
                            beginning = false;
                        }
                    }
                } else if ( line.length() == 0 || line.startsWith ( "//" ) ) {
                    ++skippedLines;
                } else {
                    beginning = false;
                }
            }
            ++lineCount;
            index = next + 1;
        }
        this.doSmap ( n, lineCount, 1, skippedLines );
    }
}
