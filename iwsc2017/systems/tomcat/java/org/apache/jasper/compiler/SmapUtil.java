package org.apache.jasper.compiler;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import org.apache.jasper.JasperException;
import org.apache.jasper.JspCompilationContext;
import org.apache.juli.logging.Log;
import org.apache.juli.logging.LogFactory;
public class SmapUtil {
    private static final String SMAP_ENCODING = "UTF-8";
    public static String[] generateSmap (
        JspCompilationContext ctxt,
        Node.Nodes pageNodes )
    throws IOException {
        PreScanVisitor psVisitor = new PreScanVisitor();
        try {
            pageNodes.visit ( psVisitor );
        } catch ( JasperException ex ) {
        }
        HashMap<String, SmapStratum> map = psVisitor.getMap();
        SmapGenerator g = new SmapGenerator();
        SmapStratum s = new SmapStratum ( "JSP" );
        g.setOutputFileName ( unqualify ( ctxt.getServletJavaFileName() ) );
        evaluateNodes ( pageNodes, s, map, ctxt.getOptions().getMappedFile() );
        s.optimizeLineSection();
        g.addStratum ( s, true );
        if ( ctxt.getOptions().isSmapDumped() ) {
            File outSmap = new File ( ctxt.getClassFileName() + ".smap" );
            PrintWriter so =
                new PrintWriter (
                new OutputStreamWriter (
                    new FileOutputStream ( outSmap ),
                    SMAP_ENCODING ) );
            so.print ( g.getString() );
            so.close();
        }
        String classFileName = ctxt.getClassFileName();
        int innerClassCount = map.size();
        String [] smapInfo = new String[2 + innerClassCount * 2];
        smapInfo[0] = classFileName;
        smapInfo[1] = g.getString();
        int count = 2;
        Iterator<Map.Entry<String, SmapStratum>> iter = map.entrySet().iterator();
        while ( iter.hasNext() ) {
            Map.Entry<String, SmapStratum> entry = iter.next();
            String innerClass = entry.getKey();
            s = entry.getValue();
            s.optimizeLineSection();
            g = new SmapGenerator();
            g.setOutputFileName ( unqualify ( ctxt.getServletJavaFileName() ) );
            g.addStratum ( s, true );
            String innerClassFileName =
                classFileName.substring ( 0, classFileName.indexOf ( ".class" ) ) +
                '$' + innerClass + ".class";
            if ( ctxt.getOptions().isSmapDumped() ) {
                File outSmap = new File ( innerClassFileName + ".smap" );
                PrintWriter so =
                    new PrintWriter (
                    new OutputStreamWriter (
                        new FileOutputStream ( outSmap ),
                        SMAP_ENCODING ) );
                so.print ( g.getString() );
                so.close();
            }
            smapInfo[count] = innerClassFileName;
            smapInfo[count + 1] = g.getString();
            count += 2;
        }
        return smapInfo;
    }
    public static void installSmap ( String[] smap )
    throws IOException {
        if ( smap == null ) {
            return;
        }
        for ( int i = 0; i < smap.length; i += 2 ) {
            File outServlet = new File ( smap[i] );
            SDEInstaller.install ( outServlet,
                                   smap[i + 1].getBytes ( StandardCharsets.ISO_8859_1 ) );
        }
    }
    private static String unqualify ( String path ) {
        path = path.replace ( '\\', '/' );
        return path.substring ( path.lastIndexOf ( '/' ) + 1 );
    }
    private static class SDEInstaller {
        private final Log log = LogFactory.getLog ( SDEInstaller.class );
        static final String nameSDE = "SourceDebugExtension";
        byte[] orig;
        byte[] sdeAttr;
        byte[] gen;
        int origPos = 0;
        int genPos = 0;
        int sdeIndex;
        static void install ( File classFile, byte[] smap ) throws IOException {
            File tmpFile = new File ( classFile.getPath() + "tmp" );
            SDEInstaller installer = new SDEInstaller ( classFile, smap );
            installer.install ( tmpFile );
            if ( !classFile.delete() ) {
                throw new IOException ( "classFile.delete() failed" );
            }
            if ( !tmpFile.renameTo ( classFile ) ) {
                throw new IOException ( "tmpFile.renameTo(classFile) failed" );
            }
        }
        SDEInstaller ( File inClassFile, byte[] sdeAttr )
        throws IOException {
            if ( !inClassFile.exists() ) {
                throw new FileNotFoundException ( "no such file: " + inClassFile );
            }
            this.sdeAttr = sdeAttr;
            orig = readWhole ( inClassFile );
            gen = new byte[orig.length + sdeAttr.length + 100];
        }
        void install ( File outClassFile ) throws IOException {
            addSDE();
            try ( FileOutputStream outStream = new FileOutputStream ( outClassFile ); ) {
                outStream.write ( gen, 0, genPos );
            }
        }
        static byte[] readWhole ( File input ) throws IOException {
            int len = ( int ) input.length();
            byte[] bytes = new byte[len];
            try ( FileInputStream inStream = new FileInputStream ( input ) ) {
                if ( inStream.read ( bytes, 0, len ) != len ) {
                    throw new IOException ( "expected size: " + len );
                }
            }
            return bytes;
        }
        void addSDE() throws UnsupportedEncodingException, IOException {
            copy ( 4 + 2 + 2 );
            int constantPoolCountPos = genPos;
            int constantPoolCount = readU2();
            if ( log.isDebugEnabled() ) {
                log.debug ( "constant pool count: " + constantPoolCount );
            }
            writeU2 ( constantPoolCount );
            sdeIndex = copyConstantPool ( constantPoolCount );
            if ( sdeIndex < 0 ) {
                writeUtf8ForSDE();
                sdeIndex = constantPoolCount;
                ++constantPoolCount;
                randomAccessWriteU2 ( constantPoolCountPos, constantPoolCount );
                if ( log.isDebugEnabled() ) {
                    log.debug ( "SourceDebugExtension not found, installed at: " + sdeIndex );
                }
            } else {
                if ( log.isDebugEnabled() ) {
                    log.debug ( "SourceDebugExtension found at: " + sdeIndex );
                }
            }
            copy ( 2 + 2 + 2 );
            int interfaceCount = readU2();
            writeU2 ( interfaceCount );
            if ( log.isDebugEnabled() ) {
                log.debug ( "interfaceCount: " + interfaceCount );
            }
            copy ( interfaceCount * 2 );
            copyMembers();
            copyMembers();
            int attrCountPos = genPos;
            int attrCount = readU2();
            writeU2 ( attrCount );
            if ( log.isDebugEnabled() ) {
                log.debug ( "class attrCount: " + attrCount );
            }
            if ( !copyAttrs ( attrCount ) ) {
                ++attrCount;
                randomAccessWriteU2 ( attrCountPos, attrCount );
                if ( log.isDebugEnabled() ) {
                    log.debug ( "class attrCount incremented" );
                }
            }
            writeAttrForSDE ( sdeIndex );
        }
        void copyMembers() {
            int count = readU2();
            writeU2 ( count );
            if ( log.isDebugEnabled() ) {
                log.debug ( "members count: " + count );
            }
            for ( int i = 0; i < count; ++i ) {
                copy ( 6 );
                int attrCount = readU2();
                writeU2 ( attrCount );
                if ( log.isDebugEnabled() ) {
                    log.debug ( "member attr count: " + attrCount );
                }
                copyAttrs ( attrCount );
            }
        }
        boolean copyAttrs ( int attrCount ) {
            boolean sdeFound = false;
            for ( int i = 0; i < attrCount; ++i ) {
                int nameIndex = readU2();
                if ( nameIndex == sdeIndex ) {
                    sdeFound = true;
                    if ( log.isDebugEnabled() ) {
                        log.debug ( "SDE attr found" );
                    }
                } else {
                    writeU2 ( nameIndex );
                    int len = readU4();
                    writeU4 ( len );
                    copy ( len );
                    if ( log.isDebugEnabled() ) {
                        log.debug ( "attr len: " + len );
                    }
                }
            }
            return sdeFound;
        }
        void writeAttrForSDE ( int index ) {
            writeU2 ( index );
            writeU4 ( sdeAttr.length );
            for ( int i = 0; i < sdeAttr.length; ++i ) {
                writeU1 ( sdeAttr[i] );
            }
        }
        void randomAccessWriteU2 ( int pos, int val ) {
            int savePos = genPos;
            genPos = pos;
            writeU2 ( val );
            genPos = savePos;
        }
        int readU1() {
            return orig[origPos++] & 0xFF;
        }
        int readU2() {
            int res = readU1();
            return ( res << 8 ) + readU1();
        }
        int readU4() {
            int res = readU2();
            return ( res << 16 ) + readU2();
        }
        void writeU1 ( int val ) {
            gen[genPos++] = ( byte ) val;
        }
        void writeU2 ( int val ) {
            writeU1 ( val >> 8 );
            writeU1 ( val & 0xFF );
        }
        void writeU4 ( int val ) {
            writeU2 ( val >> 16 );
            writeU2 ( val & 0xFFFF );
        }
        void copy ( int count ) {
            for ( int i = 0; i < count; ++i ) {
                gen[genPos++] = orig[origPos++];
            }
        }
        byte[] readBytes ( int count ) {
            byte[] bytes = new byte[count];
            for ( int i = 0; i < count; ++i ) {
                bytes[i] = orig[origPos++];
            }
            return bytes;
        }
        void writeBytes ( byte[] bytes ) {
            for ( int i = 0; i < bytes.length; ++i ) {
                gen[genPos++] = bytes[i];
            }
        }
        int copyConstantPool ( int constantPoolCount )
        throws UnsupportedEncodingException, IOException {
            int sdeIndex = -1;
            for ( int i = 1; i < constantPoolCount; ++i ) {
                int tag = readU1();
                writeU1 ( tag );
                switch ( tag ) {
                case 7 :
                case 8 :
                case 16 :
                    if ( log.isDebugEnabled() ) {
                        log.debug ( i + " copying 2 bytes" );
                    }
                    copy ( 2 );
                    break;
                case 15 :
                    if ( log.isDebugEnabled() ) {
                        log.debug ( i + " copying 3 bytes" );
                    }
                    copy ( 3 );
                    break;
                case 9 :
                case 10 :
                case 11 :
                case 3 :
                case 4 :
                case 12 :
                case 18 :
                    if ( log.isDebugEnabled() ) {
                        log.debug ( i + " copying 4 bytes" );
                    }
                    copy ( 4 );
                    break;
                case 5 :
                case 6 :
                    if ( log.isDebugEnabled() ) {
                        log.debug ( i + " copying 8 bytes" );
                    }
                    copy ( 8 );
                    i++;
                    break;
                case 1 :
                    int len = readU2();
                    writeU2 ( len );
                    byte[] utf8 = readBytes ( len );
                    String str = new String ( utf8, "UTF-8" );
                    if ( log.isDebugEnabled() ) {
                        log.debug ( i + " read class attr -- '" + str + "'" );
                    }
                    if ( str.equals ( nameSDE ) ) {
                        sdeIndex = i;
                    }
                    writeBytes ( utf8 );
                    break;
                default :
                    throw new IOException ( "unexpected tag: " + tag );
                }
            }
            return sdeIndex;
        }
        void writeUtf8ForSDE() {
            int len = nameSDE.length();
            writeU1 ( 1 );
            writeU2 ( len );
            for ( int i = 0; i < len; ++i ) {
                writeU1 ( nameSDE.charAt ( i ) );
            }
        }
    }
    public static void evaluateNodes (
        Node.Nodes nodes,
        SmapStratum s,
        HashMap<String, SmapStratum> innerClassMap,
        boolean breakAtLF ) {
        try {
            nodes.visit ( new SmapGenVisitor ( s, breakAtLF, innerClassMap ) );
        } catch ( JasperException ex ) {
        }
    }
    private static class SmapGenVisitor extends Node.Visitor {
        private SmapStratum smap;
        private final boolean breakAtLF;
        private final HashMap<String, SmapStratum> innerClassMap;
        SmapGenVisitor ( SmapStratum s, boolean breakAtLF, HashMap<String, SmapStratum> map ) {
            this.smap = s;
            this.breakAtLF = breakAtLF;
            this.innerClassMap = map;
        }
        @Override
        public void visitBody ( Node n ) throws JasperException {
            SmapStratum smapSave = smap;
            String innerClass = n.getInnerClassName();
            if ( innerClass != null ) {
                this.smap = innerClassMap.get ( innerClass );
            }
            super.visitBody ( n );
            smap = smapSave;
        }
        @Override
        public void visit ( Node.Declaration n ) throws JasperException {
            doSmapText ( n );
        }
        @Override
        public void visit ( Node.Expression n ) throws JasperException {
            doSmapText ( n );
        }
        @Override
        public void visit ( Node.Scriptlet n ) throws JasperException {
            doSmapText ( n );
        }
        @Override
        public void visit ( Node.IncludeAction n ) throws JasperException {
            doSmap ( n );
            visitBody ( n );
        }
        @Override
        public void visit ( Node.ForwardAction n ) throws JasperException {
            doSmap ( n );
            visitBody ( n );
        }
        @Override
        public void visit ( Node.GetProperty n ) throws JasperException {
            doSmap ( n );
            visitBody ( n );
        }
        @Override
        public void visit ( Node.SetProperty n ) throws JasperException {
            doSmap ( n );
            visitBody ( n );
        }
        @Override
        public void visit ( Node.UseBean n ) throws JasperException {
            doSmap ( n );
            visitBody ( n );
        }
        @Override
        public void visit ( Node.PlugIn n ) throws JasperException {
            doSmap ( n );
            visitBody ( n );
        }
        @Override
        public void visit ( Node.CustomTag n ) throws JasperException {
            doSmap ( n );
            visitBody ( n );
        }
        @Override
        public void visit ( Node.UninterpretedTag n ) throws JasperException {
            doSmap ( n );
            visitBody ( n );
        }
        @Override
        public void visit ( Node.JspElement n ) throws JasperException {
            doSmap ( n );
            visitBody ( n );
        }
        @Override
        public void visit ( Node.JspText n ) throws JasperException {
            doSmap ( n );
            visitBody ( n );
        }
        @Override
        public void visit ( Node.NamedAttribute n ) throws JasperException {
            visitBody ( n );
        }
        @Override
        public void visit ( Node.JspBody n ) throws JasperException {
            doSmap ( n );
            visitBody ( n );
        }
        @Override
        public void visit ( Node.InvokeAction n ) throws JasperException {
            doSmap ( n );
            visitBody ( n );
        }
        @Override
        public void visit ( Node.DoBodyAction n ) throws JasperException {
            doSmap ( n );
            visitBody ( n );
        }
        @Override
        public void visit ( Node.ELExpression n ) throws JasperException {
            doSmap ( n );
        }
        @Override
        public void visit ( Node.TemplateText n ) throws JasperException {
            Mark mark = n.getStart();
            if ( mark == null ) {
                return;
            }
            String fileName = mark.getFile();
            smap.addFile ( unqualify ( fileName ), fileName );
            int iInputStartLine = mark.getLineNumber();
            int iOutputStartLine = n.getBeginJavaLine();
            int iOutputLineIncrement = breakAtLF ? 1 : 0;
            smap.addLineData ( iInputStartLine, fileName, 1, iOutputStartLine,
                               iOutputLineIncrement );
            java.util.ArrayList<Integer> extraSmap = n.getExtraSmap();
            if ( extraSmap != null ) {
                for ( int i = 0; i < extraSmap.size(); i++ ) {
                    iOutputStartLine += iOutputLineIncrement;
                    smap.addLineData (
                        iInputStartLine + extraSmap.get ( i ).intValue(),
                        fileName,
                        1,
                        iOutputStartLine,
                        iOutputLineIncrement );
                }
            }
        }
        private void doSmap (
            Node n,
            int inLineCount,
            int outIncrement,
            int skippedLines ) {
            Mark mark = n.getStart();
            if ( mark == null ) {
                return;
            }
            String unqualifiedName = unqualify ( mark.getFile() );
            smap.addFile ( unqualifiedName, mark.getFile() );
            smap.addLineData (
                mark.getLineNumber() + skippedLines,
                mark.getFile(),
                inLineCount - skippedLines,
                n.getBeginJavaLine() + skippedLines,
                outIncrement );
        }
        private void doSmap ( Node n ) {
            doSmap ( n, 1, n.getEndJavaLine() - n.getBeginJavaLine(), 0 );
        }
        private void doSmapText ( Node n ) {
            String text = n.getText();
            int index = 0;
            int next = 0;
            int lineCount = 1;
            int skippedLines = 0;
            boolean slashStarSeen = false;
            boolean beginning = true;
            while ( ( next = text.indexOf ( '\n', index ) ) > -1 ) {
                if ( beginning ) {
                    String line = text.substring ( index, next ).trim();
                    if ( !slashStarSeen && line.startsWith ( "/*" ) ) {
                        slashStarSeen = true;
                    }
                    if ( slashStarSeen ) {
                        skippedLines++;
                        int endIndex = line.indexOf ( "*/" );
                        if ( endIndex >= 0 ) {
                            slashStarSeen = false;
                            if ( endIndex < line.length() - 2 ) {
                                skippedLines--;
                                beginning = false;
                            }
                        }
                    } else if ( line.length() == 0 || line.startsWith ( "//" ) ) {
                        skippedLines++;
                    } else {
                        beginning = false;
                    }
                }
                lineCount++;
                index = next + 1;
            }
            doSmap ( n, lineCount, 1, skippedLines );
        }
    }
    private static class PreScanVisitor extends Node.Visitor {
        HashMap<String, SmapStratum> map = new HashMap<>();
        @Override
        public void doVisit ( Node n ) {
            String inner = n.getInnerClassName();
            if ( inner != null && !map.containsKey ( inner ) ) {
                map.put ( inner, new SmapStratum ( "JSP" ) );
            }
        }
        HashMap<String, SmapStratum> getMap() {
            return map;
        }
    }
}
