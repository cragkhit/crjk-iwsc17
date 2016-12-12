// 
// Decompiled by Procyon v0.5.29
// 

package examples;

import javax.servlet.jsp.tagext.VariableInfo;
import javax.servlet.jsp.tagext.TagData;
import javax.servlet.jsp.tagext.TagExtraInfo;

public class FooTagExtraInfo extends TagExtraInfo
{
    public VariableInfo[] getVariableInfo(final TagData data) {
        return new VariableInfo[] { new VariableInfo("member", "String", true, 0) };
    }
}
