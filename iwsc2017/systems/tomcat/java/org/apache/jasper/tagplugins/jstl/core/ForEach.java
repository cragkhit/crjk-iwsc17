package org.apache.jasper.tagplugins.jstl.core;
import org.apache.jasper.compiler.tagplugin.TagPlugin;
import org.apache.jasper.compiler.tagplugin.TagPluginContext;
public final class ForEach implements TagPlugin {
    private boolean hasVar, hasBegin, hasEnd, hasStep;
    @Override
    public void doTag ( TagPluginContext ctxt ) {
        String index = null;
        boolean hasVarStatus = ctxt.isAttributeSpecified ( "varStatus" );
        if ( hasVarStatus ) {
            ctxt.dontUseTagPlugin();
            return;
        }
        hasVar = ctxt.isAttributeSpecified ( "var" );
        hasBegin = ctxt.isAttributeSpecified ( "begin" );
        hasEnd = ctxt.isAttributeSpecified ( "end" );
        hasStep = ctxt.isAttributeSpecified ( "step" );
        boolean hasItems = ctxt.isAttributeSpecified ( "items" );
        if ( hasItems ) {
            doCollection ( ctxt );
            return;
        }
        index = ctxt.getTemporaryVariableName();
        ctxt.generateJavaSource ( "for (int " + index + " = " );
        ctxt.generateAttribute ( "begin" );
        ctxt.generateJavaSource ( "; " + index + " <= " );
        ctxt.generateAttribute ( "end" );
        if ( hasStep ) {
            ctxt.generateJavaSource ( "; " + index + "+=" );
            ctxt.generateAttribute ( "step" );
            ctxt.generateJavaSource ( ") {" );
        } else {
            ctxt.generateJavaSource ( "; " + index + "++) {" );
        }
        if ( hasVar ) {
            ctxt.generateJavaSource ( "_jspx_page_context.setAttribute(" );
            ctxt.generateAttribute ( "var" );
            ctxt.generateJavaSource ( ", String.valueOf(" + index + "));" );
        }
        ctxt.generateBody();
        ctxt.generateJavaSource ( "}" );
    }
    private void doCollection ( TagPluginContext ctxt ) {
        ctxt.generateImport ( "java.util.*" );
        generateIterators ( ctxt );
        String itemsV = ctxt.getTemporaryVariableName();
        ctxt.generateJavaSource ( "Object " + itemsV + "= " );
        ctxt.generateAttribute ( "items" );
        ctxt.generateJavaSource ( ";" );
        String indexV = null, beginV = null, endV = null, stepV = null;
        if ( hasBegin ) {
            beginV = ctxt.getTemporaryVariableName();
            ctxt.generateJavaSource ( "int " + beginV + " = " );
            ctxt.generateAttribute ( "begin" );
            ctxt.generateJavaSource ( ";" );
        }
        if ( hasEnd ) {
            indexV = ctxt.getTemporaryVariableName();
            ctxt.generateJavaSource ( "int " + indexV + " = 0;" );
            endV = ctxt.getTemporaryVariableName();
            ctxt.generateJavaSource ( "int " + endV + " = " );
            ctxt.generateAttribute ( "end" );
            ctxt.generateJavaSource ( ";" );
        }
        if ( hasStep ) {
            stepV = ctxt.getTemporaryVariableName();
            ctxt.generateJavaSource ( "int " + stepV + " = " );
            ctxt.generateAttribute ( "step" );
            ctxt.generateJavaSource ( ";" );
        }
        String iterV = ctxt.getTemporaryVariableName();
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
        if ( hasBegin ) {
            String tV = ctxt.getTemporaryVariableName();
            ctxt.generateJavaSource ( "for (int " + tV + "=" + beginV + ";" +
                                      tV + ">0 && " + iterV + ".hasNext(); " +
                                      tV + "--)" );
            ctxt.generateJavaSource ( iterV + ".next();" );
        }
        ctxt.generateJavaSource ( "while (" + iterV + ".hasNext()){" );
        if ( hasVar ) {
            ctxt.generateJavaSource ( "_jspx_page_context.setAttribute(" );
            ctxt.generateAttribute ( "var" );
            ctxt.generateJavaSource ( ", " + iterV + ".next());" );
        }
        ctxt.generateBody();
        if ( hasStep ) {
            String tV = ctxt.getTemporaryVariableName();
            ctxt.generateJavaSource ( "for (int " + tV + "=" + stepV + "-1;" +
                                      tV + ">0 && " + iterV + ".hasNext(); " +
                                      tV + "--)" );
            ctxt.generateJavaSource ( iterV + ".next();" );
        }
        if ( hasEnd ) {
            if ( hasStep ) {
                ctxt.generateJavaSource ( indexV + "+=" + stepV + ";" );
            } else {
                ctxt.generateJavaSource ( indexV + "++;" );
            }
            if ( hasBegin ) {
                ctxt.generateJavaSource ( "if(" + beginV + "+" + indexV +
                                          ">" + endV + ")" );
            } else {
                ctxt.generateJavaSource ( "if(" + indexV + ">" + endV + ")" );
            }
            ctxt.generateJavaSource ( "break;" );
        }
        ctxt.generateJavaSource ( "}" );
        ctxt.generateJavaSource ( "}" );
    }
    private void generateIterators ( TagPluginContext ctxt ) {
        ctxt.generateDeclaration ( "ObjectArrayIterator",
                                   "private Iterator toIterator(final Object[] a){\n" +
                                   "  return (new Iterator() {\n" +
                                   "    int index=0;\n" +
                                   "    public boolean hasNext() {\n" +
                                   "      return index < a.length;}\n" +
                                   "    public Object next() {\n" +
                                   "      return a[index++];}\n" +
                                   "    public void remove() {}\n" +
                                   "  });\n" +
                                   "}"
                                 );
        ctxt.generateDeclaration ( "booleanArrayIterator",
                                   "private Iterator toIterator(final boolean[] a){\n" +
                                   "  return (new Iterator() {\n" +
                                   "    int index=0;\n" +
                                   "    public boolean hasNext() {\n" +
                                   "      return index < a.length;}\n" +
                                   "    public Object next() {\n" +
                                   "      return new Boolean(a[index++]);}\n" +
                                   "    public void remove() {}\n" +
                                   "  });\n" +
                                   "}"
                                 );
        ctxt.generateDeclaration ( "byteArrayIterator",
                                   "private Iterator toIterator(final byte[] a){\n" +
                                   "  return (new Iterator() {\n" +
                                   "    int index=0;\n" +
                                   "    public boolean hasNext() {\n" +
                                   "      return index < a.length;}\n" +
                                   "    public Object next() {\n" +
                                   "      return new Byte(a[index++]);}\n" +
                                   "    public void remove() {}\n" +
                                   "  });\n" +
                                   "}"
                                 );
        ctxt.generateDeclaration ( "charArrayIterator",
                                   "private Iterator toIterator(final char[] a){\n" +
                                   "  return (new Iterator() {\n" +
                                   "    int index=0;\n" +
                                   "    public boolean hasNext() {\n" +
                                   "      return index < a.length;}\n" +
                                   "    public Object next() {\n" +
                                   "      return new Character(a[index++]);}\n" +
                                   "    public void remove() {}\n" +
                                   "  });\n" +
                                   "}"
                                 );
        ctxt.generateDeclaration ( "shortArrayIterator",
                                   "private Iterator toIterator(final short[] a){\n" +
                                   "  return (new Iterator() {\n" +
                                   "    int index=0;\n" +
                                   "    public boolean hasNext() {\n" +
                                   "      return index < a.length;}\n" +
                                   "    public Object next() {\n" +
                                   "      return new Short(a[index++]);}\n" +
                                   "    public void remove() {}\n" +
                                   "  });\n" +
                                   "}"
                                 );
        ctxt.generateDeclaration ( "intArrayIterator",
                                   "private Iterator toIterator(final int[] a){\n" +
                                   "  return (new Iterator() {\n" +
                                   "    int index=0;\n" +
                                   "    public boolean hasNext() {\n" +
                                   "      return index < a.length;}\n" +
                                   "    public Object next() {\n" +
                                   "      return new Integer(a[index++]);}\n" +
                                   "    public void remove() {}\n" +
                                   "  });\n" +
                                   "}"
                                 );
        ctxt.generateDeclaration ( "longArrayIterator",
                                   "private Iterator toIterator(final long[] a){\n" +
                                   "  return (new Iterator() {\n" +
                                   "    int index=0;\n" +
                                   "    public boolean hasNext() {\n" +
                                   "      return index < a.length;}\n" +
                                   "    public Object next() {\n" +
                                   "      return new Long(a[index++]);}\n" +
                                   "    public void remove() {}\n" +
                                   "  });\n" +
                                   "}"
                                 );
        ctxt.generateDeclaration ( "floatArrayIterator",
                                   "private Iterator toIterator(final float[] a){\n" +
                                   "  return (new Iterator() {\n" +
                                   "    int index=0;\n" +
                                   "    public boolean hasNext() {\n" +
                                   "      return index < a.length;}\n" +
                                   "    public Object next() {\n" +
                                   "      return new Float(a[index++]);}\n" +
                                   "    public void remove() {}\n" +
                                   "  });\n" +
                                   "}"
                                 );
        ctxt.generateDeclaration ( "doubleArrayIterator",
                                   "private Iterator toIterator(final double[] a){\n" +
                                   "  return (new Iterator() {\n" +
                                   "    int index=0;\n" +
                                   "    public boolean hasNext() {\n" +
                                   "      return index < a.length;}\n" +
                                   "    public Object next() {\n" +
                                   "      return new Double(a[index++]);}\n" +
                                   "    public void remove() {}\n" +
                                   "  });\n" +
                                   "}"
                                 );
        ctxt.generateDeclaration ( "enumIterator",
                                   "private Iterator toIterator(final Enumeration e){\n" +
                                   "  return (new Iterator() {\n" +
                                   "    public boolean hasNext() {\n" +
                                   "      return e.hasMoreElements();}\n" +
                                   "    public Object next() {\n" +
                                   "      return e.nextElement();}\n" +
                                   "    public void remove() {}\n" +
                                   "  });\n" +
                                   "}"
                                 );
    }
}
