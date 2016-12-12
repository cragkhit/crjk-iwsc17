package org.apache.jasper.compiler;
import javax.servlet.jsp.tagext.TagAttributeInfo;
import javax.servlet.jsp.tagext.TagExtraInfo;
import javax.servlet.jsp.tagext.TagInfo;
import javax.servlet.jsp.tagext.TagLibraryInfo;
import javax.servlet.jsp.tagext.TagVariableInfo;
class JasperTagInfo extends TagInfo {
    private final String dynamicAttrsMapName;
    public JasperTagInfo ( String tagName,
                           String tagClassName,
                           String bodyContent,
                           String infoString,
                           TagLibraryInfo taglib,
                           TagExtraInfo tagExtraInfo,
                           TagAttributeInfo[] attributeInfo,
                           String displayName,
                           String smallIcon,
                           String largeIcon,
                           TagVariableInfo[] tvi,
                           String mapName ) {
        super ( tagName, tagClassName, bodyContent, infoString, taglib,
                tagExtraInfo, attributeInfo, displayName, smallIcon, largeIcon,
                tvi );
        this.dynamicAttrsMapName = mapName;
    }
    public String getDynamicAttributesMapName() {
        return dynamicAttrsMapName;
    }
    @Override
    public boolean hasDynamicAttributes() {
        return dynamicAttrsMapName != null;
    }
}
