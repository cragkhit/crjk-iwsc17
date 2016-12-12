package org.apache.tomcat.util.descriptor.web;
public abstract class XmlEncodingBase {
    private String encoding;
    public XmlEncodingBase() {
        this.encoding = null;
    }
    public void setEncoding ( final String encoding ) {
        this.encoding = encoding;
    }
    public String getEncoding() {
        if ( this.encoding == null || this.encoding.length() == 0 ) {
            return "UTF-8";
        }
        return this.encoding;
    }
}
