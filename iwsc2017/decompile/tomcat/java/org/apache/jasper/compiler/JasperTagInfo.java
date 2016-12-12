package org.apache.jasper.compiler;
import javax.servlet.jsp.tagext.TagVariableInfo;
import javax.servlet.jsp.tagext.TagAttributeInfo;
import javax.servlet.jsp.tagext.TagExtraInfo;
import javax.servlet.jsp.tagext.TagLibraryInfo;
import javax.servlet.jsp.tagext.TagInfo;
class JasperTagInfo extends TagInfo {
    private final String dynamicAttrsMapName;
    public JasperTagInfo ( final String tagName, final String tagClassName, final String bodyContent, final String infoString, final TagLibraryInfo taglib, final TagExtraInfo tagExtraInfo, final TagAttributeInfo[] attributeInfo, final String displayName, final String smallIcon, final String largeIcon, final TagVariableInfo[] tvi, final String mapName ) {
        super ( tagName, tagClassName, bodyContent, infoString, taglib, tagExtraInfo, attributeInfo, displayName, smallIcon, largeIcon, tvi );
        this.dynamicAttrsMapName = mapName;
    }
    public String getDynamicAttributesMapName() {
        return this.dynamicAttrsMapName;
    }
    public boolean hasDynamicAttributes() {
        return this.dynamicAttrsMapName != null;
    }
}
