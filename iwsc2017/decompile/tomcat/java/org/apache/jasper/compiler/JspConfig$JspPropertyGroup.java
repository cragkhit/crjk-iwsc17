package org.apache.jasper.compiler;
public static class JspPropertyGroup {
    private final String path;
    private final String extension;
    private final JspProperty jspProperty;
    JspPropertyGroup ( final String path, final String extension, final JspProperty jspProperty ) {
        this.path = path;
        this.extension = extension;
        this.jspProperty = jspProperty;
    }
    public String getPath() {
        return this.path;
    }
    public String getExtension() {
        return this.extension;
    }
    public JspProperty getJspProperty() {
        return this.jspProperty;
    }
}
