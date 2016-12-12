package org.apache.tomcat.util.descriptor.web;
public abstract class XmlEncodingBase {
    private String encoding = null;
    public void setEncoding ( String encoding ) {
        this.encoding = encoding;
    }
    public String getEncoding() {
        if ( encoding == null || encoding.length() == 0 ) {
            return "UTF-8";
        }
        return encoding;
    }
}
