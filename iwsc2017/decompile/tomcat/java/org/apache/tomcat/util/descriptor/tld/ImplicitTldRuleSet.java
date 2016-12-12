package org.apache.tomcat.util.descriptor.tld;
import org.xml.sax.Attributes;
import org.apache.tomcat.util.digester.Rule;
import org.apache.tomcat.util.digester.Digester;
import org.apache.tomcat.util.res.StringManager;
import org.apache.tomcat.util.digester.RuleSetBase;
public class ImplicitTldRuleSet extends RuleSetBase {
    private static final StringManager sm;
    private static final String PREFIX = "taglib";
    private static final String VALIDATOR_PREFIX = "taglib/validator";
    private static final String TAG_PREFIX = "taglib/tag";
    private static final String TAGFILE_PREFIX = "taglib/tag-file";
    private static final String FUNCTION_PREFIX = "taglib/function";
    @Override
    public void addRuleInstances ( final Digester digester ) {
        digester.addCallMethod ( "taglib/tlibversion", "setTlibVersion", 0 );
        digester.addCallMethod ( "taglib/tlib-version", "setTlibVersion", 0 );
        digester.addCallMethod ( "taglib/jspversion", "setJspVersion", 0 );
        digester.addCallMethod ( "taglib/jsp-version", "setJspVersion", 0 );
        digester.addRule ( "taglib", new Rule() {
            @Override
            public void begin ( final String namespace, final String name, final Attributes attributes ) {
                final TaglibXml taglibXml = ( TaglibXml ) this.digester.peek();
                taglibXml.setJspVersion ( attributes.getValue ( "version" ) );
            }
        } );
        digester.addCallMethod ( "taglib/shortname", "setShortName", 0 );
        digester.addCallMethod ( "taglib/short-name", "setShortName", 0 );
        digester.addRule ( "taglib/uri", new ElementNotAllowedRule() );
        digester.addRule ( "taglib/info", new ElementNotAllowedRule() );
        digester.addRule ( "taglib/description", new ElementNotAllowedRule() );
        digester.addRule ( "taglib/listener/listener-class", new ElementNotAllowedRule() );
        digester.addRule ( "taglib/validator", new ElementNotAllowedRule() );
        digester.addRule ( "taglib/tag", new ElementNotAllowedRule() );
        digester.addRule ( "taglib/tag-file", new ElementNotAllowedRule() );
        digester.addRule ( "taglib/function", new ElementNotAllowedRule() );
    }
    static {
        sm = StringManager.getManager ( ImplicitTldRuleSet.class );
    }
    private static class ElementNotAllowedRule extends Rule {
        @Override
        public void begin ( final String namespace, final String name, final Attributes attributes ) throws Exception {
            throw new IllegalArgumentException ( ImplicitTldRuleSet.sm.getString ( "implicitTldRule.elementNotAllowed", name ) );
        }
    }
}
