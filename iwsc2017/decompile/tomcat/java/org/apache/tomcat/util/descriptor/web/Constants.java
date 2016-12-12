package org.apache.tomcat.util.descriptor.web;
public class Constants {
    public static final String PACKAGE_NAME;
    public static final String WEB_XML_LOCATION = "/WEB-INF/web.xml";
    static {
        PACKAGE_NAME = Constants.class.getPackage().getName();
    }
}
