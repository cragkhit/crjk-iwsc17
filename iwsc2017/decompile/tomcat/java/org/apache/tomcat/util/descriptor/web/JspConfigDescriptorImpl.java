package org.apache.tomcat.util.descriptor.web;
import java.util.ArrayList;
import javax.servlet.descriptor.TaglibDescriptor;
import javax.servlet.descriptor.JspPropertyGroupDescriptor;
import java.util.Collection;
import javax.servlet.descriptor.JspConfigDescriptor;
public class JspConfigDescriptorImpl implements JspConfigDescriptor {
    private final Collection<JspPropertyGroupDescriptor> jspPropertyGroups;
    private final Collection<TaglibDescriptor> taglibs;
    public JspConfigDescriptorImpl ( final Collection<JspPropertyGroupDescriptor> jspPropertyGroups, final Collection<TaglibDescriptor> taglibs ) {
        this.jspPropertyGroups = jspPropertyGroups;
        this.taglibs = taglibs;
    }
    public Collection<JspPropertyGroupDescriptor> getJspPropertyGroups() {
        return new ArrayList<JspPropertyGroupDescriptor> ( this.jspPropertyGroups );
    }
    public Collection<TaglibDescriptor> getTaglibs() {
        return new ArrayList<TaglibDescriptor> ( this.taglibs );
    }
}
