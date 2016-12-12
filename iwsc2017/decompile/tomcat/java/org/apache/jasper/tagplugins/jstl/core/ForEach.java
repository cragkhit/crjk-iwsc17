package org.apache.jasper.tagplugins.jstl.core;
import org.apache.jasper.compiler.tagplugin.TagPluginContext;
import org.apache.jasper.compiler.tagplugin.TagPlugin;
public final class ForEach implements TagPlugin {
    private boolean hasVar;
    private boolean hasBegin;
    private boolean hasEnd;
    private boolean hasStep;
    @Override
    public void doTag ( final TagPluginContext ctxt ) {
        String index = null;
        final boolean hasVarStatus = ctxt.isAttributeSpecified ( "varStatus" );
        if ( hasVarStatus ) {
            ctxt.dontUseTagPlugin();
            return;
        }
        this.hasVar = ctxt.isAttributeSpecified ( "var" );
        this.hasBegin = ctxt.isAttributeSpecified ( "begin" );
        this.hasEnd = ctxt.isAttributeSpecified ( "end" );
        this.hasStep = ctxt.isAttributeSpecified ( "step" );
        final boolean hasItems = ctxt.isAttributeSpecified ( "items" );
        if ( hasItems ) {
            this.doCollection ( ctxt );
            return;
        }
        index = ctxt.getTemporaryVariableName();
        ctxt.generateJavaSource ( "for (int " + index + " = " );
        ctxt.generateAttribute ( "begin" );
        ctxt.generateJavaSource ( "; " + index + " <= " );
        ctxt.generateAttribute ( "end" );
        if ( this.hasStep ) {
            ctxt.generateJavaSource ( "; " + index + "+=" );
            ctxt.generateAttribute ( "step" );
            ctxt.generateJavaSource ( ") {" );
        } else {
            ctxt.generateJavaSource ( "; " + index + "++) {" );
        }
        if ( this.hasVar ) {
            ctxt.generateJavaSource ( "_jspx_page_context.setAttribute(" );
            ctxt.generateAttribute ( "var" );
            ctxt.generateJavaSource ( ", String.valueOf(" + index + "));" );
        }
        ctxt.generateBody();
        ctxt.generateJavaSource ( "}" );
    }
    private void doCollection ( final TagPluginContext ctxt ) {
        ctxt.generateImport ( "java.util.*" );
        this.generateIterators ( ctxt );
        final String itemsV = ctxt.getTemporaryVariableName();
        ctxt.generateJavaSource ( "Object " + itemsV + "= " );
        ctxt.generateAttribute ( "items" );
        ctxt.generateJavaSource ( ";" );
        String indexV = null;
        String beginV = null;
        String endV = null;
        String stepV = null;
        if ( this.hasBegin ) {
            beginV = ctxt.getTemporaryVariableName();
            ctxt.generateJavaSource ( "int " + beginV + " = " );
            ctxt.generateAttribute ( "begin" );
            ctxt.generateJavaSource ( ";" );
        }
        if ( this.hasEnd ) {
            indexV = ctxt.getTemporaryVariableName();
            ctxt.generateJavaSource ( "int " + indexV + " = 0;" );
            endV = ctxt.getTemporaryVariableName();
            ctxt.generateJavaSource ( "int " + endV + " = " );
            ctxt.generateAttribute ( "end" );
            ctxt.generateJavaSource ( ";" );
        }
        if ( this.hasStep ) {
            stepV = ctxt.getTemporaryVariableName();
            ctxt.generateJavaSource ( "int " + stepV + " = " );
            ctxt.generateAttribute ( "step" );
            ctxt.generateJavaSource ( ";" );
        }
        final String iterV = ctxt.getTemporaryVariableName();
        ctxt.generateJavaSource ( "Iterator " + iterV + " = null;" );
        ctxt.generateJavaSource ( "if (" + itemsV + " instanceof Object[])" );
        ctxt.generateJavaSource ( iterV + "=toIterator((Object[])" + itemsV + ");" );
        ctxt.generateJavaSource ( "else if (" + itemsV + " instanceof boolean[])" );
        ctxt.generateJavaSource ( iterV + "=toIterator((boolean[])" + itemsV + ");" );
        ctxt.generateJavaSource ( "else if (" + itemsV + " instanceof byte[])" );
        ctxt.generateJavaSource ( iterV + "=toIterator((byte[])" + itemsV + ");" );
        ctxt.generateJavaSource ( "else if (" + itemsV + " instanceof char[])" );
        ctxt.generateJavaSource ( iterV + "=toIterator((char[])" + itemsV + ");" );
        ctxt.generateJavaSource ( "else if (" + itemsV + " instanceof short[])" );
        ctxt.generateJavaSource ( iterV + "=toIterator((short[])" + itemsV + ");" );
        ctxt.generateJavaSource ( "else if (" + itemsV + " instanceof int[])" );
        ctxt.generateJavaSource ( iterV + "=toIterator((int[])" + itemsV + ");" );
        ctxt.generateJavaSource ( "else if (" + itemsV + " instanceof long[])" );
        ctxt.generateJavaSource ( iterV + "=toIterator((long[])" + itemsV + ");" );
        ctxt.generateJavaSource ( "else if (" + itemsV + " instanceof float[])" );
        ctxt.generateJavaSource ( iterV + "=toIterator((float[])" + itemsV + ");" );
        ctxt.generateJavaSource ( "else if (" + itemsV + " instanceof double[])" );
        ctxt.generateJavaSource ( iterV + "=toIterator((double[])" + itemsV + ");" );
        ctxt.generateJavaSource ( "else if (" + itemsV + " instanceof Collection)" );
        ctxt.generateJavaSource ( iterV + "=((Collection)" + itemsV + ").iterator();" );
        ctxt.generateJavaSource ( "else if (" + itemsV + " instanceof Iterator)" );
        ctxt.generateJavaSource ( iterV + "=(Iterator)" + itemsV + ";" );
        ctxt.generateJavaSource ( "else if (" + itemsV + " instanceof Enumeration)" );
        ctxt.generateJavaSource ( iterV + "=toIterator((Enumeration)" + itemsV + ");" );
        ctxt.generateJavaSource ( "else if (" + itemsV + " instanceof Map)" );
        ctxt.generateJavaSource ( iterV + "=((Map)" + itemsV + ").entrySet().iterator();" );
        ctxt.generateJavaSource ( "else if (" + itemsV + " instanceof String)" );
        ctxt.generateJavaSource ( iterV + "=toIterator(new StringTokenizer((String)" + itemsV + ", \",\"));" );
        ctxt.generateJavaSource ( "if (" + iterV + " != null) {" );
        if ( this.hasBegin ) {
            final String tV = ctxt.getTemporaryVariableName();
            ctxt.generateJavaSource ( "for (int " + tV + "=" + beginV + ";" + tV + ">0 && " + iterV + ".hasNext(); " + tV + "--)" );
            ctxt.generateJavaSource ( iterV + ".next();" );
        }
        ctxt.generateJavaSource ( "while (" + iterV + ".hasNext()){" );
        if ( this.hasVar ) {
            ctxt.generateJavaSource ( "_jspx_page_context.setAttribute(" );
            ctxt.generateAttribute ( "var" );
            ctxt.generateJavaSource ( ", " + iterV + ".next());" );
        }
        ctxt.generateBody();
        if ( this.hasStep ) {
            final String tV = ctxt.getTemporaryVariableName();
            ctxt.generateJavaSource ( "for (int " + tV + "=" + stepV + "-1;" + tV + ">0 && " + iterV + ".hasNext(); " + tV + "--)" );
            ctxt.generateJavaSource ( iterV + ".next();" );
        }
        if ( this.hasEnd ) {
            if ( this.hasStep ) {
                ctxt.generateJavaSource ( indexV + "+=" + stepV + ";" );
            } else {
                ctxt.generateJavaSource ( indexV + "++;" );
            }
            if ( this.hasBegin ) {
                ctxt.generateJavaSource ( "if(" + beginV + "+" + indexV + ">" + endV + ")" );
            } else {
                ctxt.generateJavaSource ( "if(" + indexV + ">" + endV + ")" );
            }
            ctxt.generateJavaSource ( "break;" );
        }
        ctxt.generateJavaSource ( "}" );
        ctxt.generateJavaSource ( "}" );
    }
    private void generateIterators ( final TagPluginContext ctxt ) {
        ctxt.generateDeclaration ( "ObjectArrayIterator", "private Iterator toIterator(final Object[] a){\n  return (new Iterator() {\n    int index=0;\n    public boolean hasNext() {\n      return index < a.length;}\n    public Object next() {\n      return a[index++];}\n    public void remove() {}\n  });\n}" );
        ctxt.generateDeclaration ( "booleanArrayIterator", "private Iterator toIterator(final boolean[] a){\n  return (new Iterator() {\n    int index=0;\n    public boolean hasNext() {\n      return index < a.length;}\n    public Object next() {\n      return new Boolean(a[index++]);}\n    public void remove() {}\n  });\n}" );
        ctxt.generateDeclaration ( "byteArrayIterator", "private Iterator toIterator(final byte[] a){\n  return (new Iterator() {\n    int index=0;\n    public boolean hasNext() {\n      return index < a.length;}\n    public Object next() {\n      return new Byte(a[index++]);}\n    public void remove() {}\n  });\n}" );
        ctxt.generateDeclaration ( "charArrayIterator", "private Iterator toIterator(final char[] a){\n  return (new Iterator() {\n    int index=0;\n    public boolean hasNext() {\n      return index < a.length;}\n    public Object next() {\n      return new Character(a[index++]);}\n    public void remove() {}\n  });\n}" );
        ctxt.generateDeclaration ( "shortArrayIterator", "private Iterator toIterator(final short[] a){\n  return (new Iterator() {\n    int index=0;\n    public boolean hasNext() {\n      return index < a.length;}\n    public Object next() {\n      return new Short(a[index++]);}\n    public void remove() {}\n  });\n}" );
        ctxt.generateDeclaration ( "intArrayIterator", "private Iterator toIterator(final int[] a){\n  return (new Iterator() {\n    int index=0;\n    public boolean hasNext() {\n      return index < a.length;}\n    public Object next() {\n      return new Integer(a[index++]);}\n    public void remove() {}\n  });\n}" );
        ctxt.generateDeclaration ( "longArrayIterator", "private Iterator toIterator(final long[] a){\n  return (new Iterator() {\n    int index=0;\n    public boolean hasNext() {\n      return index < a.length;}\n    public Object next() {\n      return new Long(a[index++]);}\n    public void remove() {}\n  });\n}" );
        ctxt.generateDeclaration ( "floatArrayIterator", "private Iterator toIterator(final float[] a){\n  return (new Iterator() {\n    int index=0;\n    public boolean hasNext() {\n      return index < a.length;}\n    public Object next() {\n      return new Float(a[index++]);}\n    public void remove() {}\n  });\n}" );
        ctxt.generateDeclaration ( "doubleArrayIterator", "private Iterator toIterator(final double[] a){\n  return (new Iterator() {\n    int index=0;\n    public boolean hasNext() {\n      return index < a.length;}\n    public Object next() {\n      return new Double(a[index++]);}\n    public void remove() {}\n  });\n}" );
        ctxt.generateDeclaration ( "enumIterator", "private Iterator toIterator(final Enumeration e){\n  return (new Iterator() {\n    public boolean hasNext() {\n      return e.hasMoreElements();}\n    public Object next() {\n      return e.nextElement();}\n    public void remove() {}\n  });\n}" );
    }
}
