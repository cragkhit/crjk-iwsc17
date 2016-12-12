package org.apache.jasper.compiler;
import org.xml.sax.Attributes;
import java.util.ArrayList;
import java.io.UnsupportedEncodingException;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import org.apache.juli.logging.LogFactory;
import org.apache.juli.logging.Log;
import java.nio.charset.StandardCharsets;
import java.io.IOException;
import java.util.Iterator;
import java.util.HashMap;
import java.util.Map;
import java.io.Writer;
import java.io.PrintWriter;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.FileOutputStream;
import java.io.File;
import org.apache.jasper.JasperException;
import org.apache.jasper.JspCompilationContext;
public class SmapUtil {
    private static final String SMAP_ENCODING = "UTF-8";
    public static String[] generateSmap ( final JspCompilationContext ctxt, final Node.Nodes pageNodes ) throws IOException {
        final PreScanVisitor psVisitor = new PreScanVisitor();
        try {
            pageNodes.visit ( psVisitor );
        } catch ( JasperException ex ) {}
        final HashMap<String, SmapStratum> map = psVisitor.getMap();
        SmapGenerator g = new SmapGenerator();
        SmapStratum s = new SmapStratum ( "JSP" );
        g.setOutputFileName ( unqualify ( ctxt.getServletJavaFileName() ) );
        evaluateNodes ( pageNodes, s, map, ctxt.getOptions().getMappedFile() );
        s.optimizeLineSection();
        g.addStratum ( s, true );
        if ( ctxt.getOptions().isSmapDumped() ) {
            final File outSmap = new File ( ctxt.getClassFileName() + ".smap" );
            final PrintWriter so = new PrintWriter ( new OutputStreamWriter ( new FileOutputStream ( outSmap ), "UTF-8" ) );
            so.print ( g.getString() );
            so.close();
        }
        final String classFileName = ctxt.getClassFileName();
        final int innerClassCount = map.size();
        final String[] smapInfo = new String[2 + innerClassCount * 2];
        smapInfo[0] = classFileName;
        smapInfo[1] = g.getString();
        int count = 2;
        for ( final Map.Entry<String, SmapStratum> entry : map.entrySet() ) {
            final String innerClass = entry.getKey();
            s = entry.getValue();
            s.optimizeLineSection();
            g = new SmapGenerator();
            g.setOutputFileName ( unqualify ( ctxt.getServletJavaFileName() ) );
            g.addStratum ( s, true );
            final String innerClassFileName = classFileName.substring ( 0, classFileName.indexOf ( ".class" ) ) + '$' + innerClass + ".class";
            if ( ctxt.getOptions().isSmapDumped() ) {
                final File outSmap2 = new File ( innerClassFileName + ".smap" );
                final PrintWriter so2 = new PrintWriter ( new OutputStreamWriter ( new FileOutputStream ( outSmap2 ), "UTF-8" ) );
                so2.print ( g.getString() );
                so2.close();
            }
            smapInfo[count] = innerClassFileName;
            smapInfo[count + 1] = g.getString();
            count += 2;
        }
        return smapInfo;
    }
    public static void installSmap ( final String[] smap ) throws IOException {
        if ( smap == null ) {
            return;
        }
        for ( int i = 0; i < smap.length; i += 2 ) {
            final File outServlet = new File ( smap[i] );
            SDEInstaller.install ( outServlet, smap[i + 1].getBytes ( StandardCharsets.ISO_8859_1 ) );
        }
    }
    private static String unqualify ( String path ) {
        path = path.replace ( '\\', '/' );
        return path.substring ( path.lastIndexOf ( 47 ) + 1 );
    }
    public static void evaluateNodes ( final Node.Nodes nodes, final SmapStratum s, final HashMap<String, SmapStratum> innerClassMap, final boolean breakAtLF ) {
        try {
            nodes.visit ( new SmapGenVisitor ( s, breakAtLF, innerClassMap ) );
        } catch ( JasperException ex ) {}
    }
    private static class SDEInstaller {
        private final Log log;
        static final String nameSDE = "SourceDebugExtension";
        byte[] orig;
        byte[] sdeAttr;
        byte[] gen;
        int origPos;
        int genPos;
        int sdeIndex;
        static void install ( final File classFile, final byte[] smap ) throws IOException {
            final File tmpFile = new File ( classFile.getPath() + "tmp" );
            final SDEInstaller installer = new SDEInstaller ( classFile, smap );
            installer.install ( tmpFile );
            if ( !classFile.delete() ) {
                throw new IOException ( "classFile.delete() failed" );
            }
            if ( !tmpFile.renameTo ( classFile ) ) {
                throw new IOException ( "tmpFile.renameTo(classFile) failed" );
            }
        }
        SDEInstaller ( final File inClassFile, final byte[] sdeAttr ) throws IOException {
            this.log = LogFactory.getLog ( SDEInstaller.class );
            this.origPos = 0;
            this.genPos = 0;
            if ( !inClassFile.exists() ) {
                throw new FileNotFoundException ( "no such file: " + inClassFile );
            }
            this.sdeAttr = sdeAttr;
            this.orig = readWhole ( inClassFile );
            this.gen = new byte[this.orig.length + sdeAttr.length + 100];
        }
        void install ( final File outClassFile ) throws IOException {
            this.addSDE();
            try ( final FileOutputStream outStream = new FileOutputStream ( outClassFile ) ) {
                outStream.write ( this.gen, 0, this.genPos );
            }
        }
        static byte[] readWhole ( final File input ) throws IOException {
            final int len = ( int ) input.length();
            final byte[] bytes = new byte[len];
            try ( final FileInputStream inStream = new FileInputStream ( input ) ) {
                if ( inStream.read ( bytes, 0, len ) != len ) {
                    throw new IOException ( "expected size: " + len );
                }
            }
            return bytes;
        }
        void addSDE() throws UnsupportedEncodingException, IOException {
            this.copy ( 8 );
            final int constantPoolCountPos = this.genPos;
            int constantPoolCount = this.readU2();
            if ( this.log.isDebugEnabled() ) {
                this.log.debug ( "constant pool count: " + constantPoolCount );
            }
            this.writeU2 ( constantPoolCount );
            this.sdeIndex = this.copyConstantPool ( constantPoolCount );
            if ( this.sdeIndex < 0 ) {
                this.writeUtf8ForSDE();
                this.sdeIndex = constantPoolCount;
                ++constantPoolCount;
                this.randomAccessWriteU2 ( constantPoolCountPos, constantPoolCount );
                if ( this.log.isDebugEnabled() ) {
                    this.log.debug ( "SourceDebugExtension not found, installed at: " + this.sdeIndex );
                }
            } else if ( this.log.isDebugEnabled() ) {
                this.log.debug ( "SourceDebugExtension found at: " + this.sdeIndex );
            }
            this.copy ( 6 );
            final int interfaceCount = this.readU2();
            this.writeU2 ( interfaceCount );
            if ( this.log.isDebugEnabled() ) {
                this.log.debug ( "interfaceCount: " + interfaceCount );
            }
            this.copy ( interfaceCount * 2 );
            this.copyMembers();
            this.copyMembers();
            final int attrCountPos = this.genPos;
            int attrCount = this.readU2();
            this.writeU2 ( attrCount );
            if ( this.log.isDebugEnabled() ) {
                this.log.debug ( "class attrCount: " + attrCount );
            }
            if ( !this.copyAttrs ( attrCount ) ) {
                ++attrCount;
                this.randomAccessWriteU2 ( attrCountPos, attrCount );
                if ( this.log.isDebugEnabled() ) {
                    this.log.debug ( "class attrCount incremented" );
                }
            }
            this.writeAttrForSDE ( this.sdeIndex );
        }
        void copyMembers() {
            final int count = this.readU2();
            this.writeU2 ( count );
            if ( this.log.isDebugEnabled() ) {
                this.log.debug ( "members count: " + count );
            }
            for ( int i = 0; i < count; ++i ) {
                this.copy ( 6 );
                final int attrCount = this.readU2();
                this.writeU2 ( attrCount );
                if ( this.log.isDebugEnabled() ) {
                    this.log.debug ( "member attr count: " + attrCount );
                }
                this.copyAttrs ( attrCount );
            }
        }
        boolean copyAttrs ( final int attrCount ) {
            boolean sdeFound = false;
            for ( int i = 0; i < attrCount; ++i ) {
                final int nameIndex = this.readU2();
                if ( nameIndex == this.sdeIndex ) {
                    sdeFound = true;
                    if ( this.log.isDebugEnabled() ) {
                        this.log.debug ( "SDE attr found" );
                    }
                } else {
                    this.writeU2 ( nameIndex );
                    final int len = this.readU4();
                    this.writeU4 ( len );
                    this.copy ( len );
                    if ( this.log.isDebugEnabled() ) {
                        this.log.debug ( "attr len: " + len );
                    }
                }
            }
            return sdeFound;
        }
        void writeAttrForSDE ( final int index ) {
            this.writeU2 ( index );
            this.writeU4 ( this.sdeAttr.length );
            for ( int i = 0; i < this.sdeAttr.length; ++i ) {
                this.writeU1 ( this.sdeAttr[i] );
            }
        }
        void randomAccessWriteU2 ( final int pos, final int val ) {
            final int savePos = this.genPos;
            this.genPos = pos;
            this.writeU2 ( val );
            this.genPos = savePos;
        }
        int readU1() {
            return this.orig[this.origPos++] & 0xFF;
        }
        int readU2() {
            final int res = this.readU1();
            return ( res << 8 ) + this.readU1();
        }
        int readU4() {
            final int res = this.readU2();
            return ( res << 16 ) + this.readU2();
        }
        void writeU1 ( final int val ) {
            this.gen[this.genPos++] = ( byte ) val;
        }
        void writeU2 ( final int val ) {
            this.writeU1 ( val >> 8 );
            this.writeU1 ( val & 0xFF );
        }
        void writeU4 ( final int val ) {
            this.writeU2 ( val >> 16 );
            this.writeU2 ( val & 0xFFFF );
        }
        void copy ( final int count ) {
            for ( int i = 0; i < count; ++i ) {
                this.gen[this.genPos++] = this.orig[this.origPos++];
            }
        }
        byte[] readBytes ( final int count ) {
            final byte[] bytes = new byte[count];
            for ( int i = 0; i < count; ++i ) {
                bytes[i] = this.orig[this.origPos++];
            }
            return bytes;
        }
        void writeBytes ( final byte[] bytes ) {
            for ( int i = 0; i < bytes.length; ++i ) {
                this.gen[this.genPos++] = bytes[i];
            }
        }
        int copyConstantPool ( final int constantPoolCount ) throws UnsupportedEncodingException, IOException {
            int sdeIndex = -1;
            for ( int i = 1; i < constantPoolCount; ++i ) {
                final int tag = this.readU1();
                this.writeU1 ( tag );
                switch ( tag ) {
                case 7:
                case 8:
                case 16: {
                    if ( this.log.isDebugEnabled() ) {
                        this.log.debug ( i + " copying 2 bytes" );
                    }
                    this.copy ( 2 );
                    break;
                }
                case 15: {
                    if ( this.log.isDebugEnabled() ) {
                        this.log.debug ( i + " copying 3 bytes" );
                    }
                    this.copy ( 3 );
                    break;
                }
                case 3:
                case 4:
                case 9:
                case 10:
                case 11:
                case 12:
                case 18: {
                    if ( this.log.isDebugEnabled() ) {
                        this.log.debug ( i + " copying 4 bytes" );
                    }
                    this.copy ( 4 );
                    break;
                }
                case 5:
                case 6: {
                    if ( this.log.isDebugEnabled() ) {
                        this.log.debug ( i + " copying 8 bytes" );
                    }
                    this.copy ( 8 );
                    ++i;
                    break;
                }
                case 1: {
                    final int len = this.readU2();
                    this.writeU2 ( len );
                    final byte[] utf8 = this.readBytes ( len );
                    final String str = new String ( utf8, "UTF-8" );
                    if ( this.log.isDebugEnabled() ) {
                        this.log.debug ( i + " read class attr -- '" + str + "'" );
                    }
                    if ( str.equals ( "SourceDebugExtension" ) ) {
                        sdeIndex = i;
                    }
                    this.writeBytes ( utf8 );
                    break;
                }
                default: {
                    throw new IOException ( "unexpected tag: " + tag );
                }
                }
            }
            return sdeIndex;
        }
        void writeUtf8ForSDE() {
            final int len = "SourceDebugExtension".length();
            this.writeU1 ( 1 );
            this.writeU2 ( len );
            for ( int i = 0; i < len; ++i ) {
                this.writeU1 ( "SourceDebugExtension".charAt ( i ) );
            }
        }
    }
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
            this.smap.addFile ( unqualify ( fileName ), fileName );
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
            final String unqualifiedName = unqualify ( mark.getFile() );
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
    private static class PreScanVisitor extends Node.Visitor {
        HashMap<String, SmapStratum> map;
        private PreScanVisitor() {
            this.map = new HashMap<String, SmapStratum>();
        }
        public void doVisit ( final Node n ) {
            final String inner = n.getInnerClassName();
            if ( inner != null && !this.map.containsKey ( inner ) ) {
                this.map.put ( inner, new SmapStratum ( "JSP" ) );
            }
        }
        HashMap<String, SmapStratum> getMap() {
            return this.map;
        }
    }
}
