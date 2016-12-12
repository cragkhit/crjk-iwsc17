package org.apache.jasper.compiler.tagplugin;
public interface TagPluginContext {
    boolean isScriptless();
    boolean isAttributeSpecified ( String p0 );
    String getTemporaryVariableName();
    void generateImport ( String p0 );
    void generateDeclaration ( String p0, String p1 );
    void generateJavaSource ( String p0 );
    boolean isConstantAttribute ( String p0 );
    String getConstantAttribute ( String p0 );
    void generateAttribute ( String p0 );
    void generateBody();
    void dontUseTagPlugin();
    TagPluginContext getParentContext();
    void setPluginAttribute ( String p0, Object p1 );
    Object getPluginAttribute ( String p0 );
    boolean isTagFile();
}
