package org.apache.jasper.compiler.tagplugin;
public interface TagPluginContext {
    boolean isScriptless();
    boolean isAttributeSpecified ( String attribute );
    String getTemporaryVariableName();
    void generateImport ( String s );
    void generateDeclaration ( String id, String text );
    void generateJavaSource ( String s );
    boolean isConstantAttribute ( String attribute );
    String getConstantAttribute ( String attribute );
    void generateAttribute ( String attribute );
    void generateBody();
    void dontUseTagPlugin();
    TagPluginContext getParentContext();
    void setPluginAttribute ( String attr, Object value );
    Object getPluginAttribute ( String attr );
    boolean isTagFile();
}
