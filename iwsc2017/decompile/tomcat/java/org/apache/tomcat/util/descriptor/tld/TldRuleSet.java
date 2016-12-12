package org.apache.tomcat.util.descriptor.tld;
import java.lang.reflect.Method;
import javax.servlet.jsp.tagext.TagVariableInfo;
import javax.servlet.jsp.tagext.TagAttributeInfo;
import org.xml.sax.Attributes;
import org.apache.tomcat.util.digester.Rule;
import org.apache.tomcat.util.digester.Digester;
import org.apache.tomcat.util.digester.RuleSetBase;
public class TldRuleSet extends RuleSetBase {
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
        digester.addCallMethod ( "taglib/uri", "setUri", 0 );
        digester.addCallMethod ( "taglib/info", "setInfo", 0 );
        digester.addCallMethod ( "taglib/description", "setInfo", 0 );
        digester.addCallMethod ( "taglib/listener/listener-class", "addListener", 0 );
        digester.addObjectCreate ( "taglib/validator", ValidatorXml.class.getName() );
        digester.addCallMethod ( "taglib/validator/validator-class", "setValidatorClass", 0 );
        digester.addCallMethod ( "taglib/validator/init-param", "addInitParam", 2 );
        digester.addCallParam ( "taglib/validator/init-param/param-name", 0 );
        digester.addCallParam ( "taglib/validator/init-param/param-value", 1 );
        digester.addSetNext ( "taglib/validator", "setValidator", ValidatorXml.class.getName() );
        digester.addObjectCreate ( "taglib/tag", TagXml.class.getName() );
        this.addDescriptionGroup ( digester, "taglib/tag" );
        digester.addCallMethod ( "taglib/tag/name", "setName", 0 );
        digester.addCallMethod ( "taglib/tag/tagclass", "setTagClass", 0 );
        digester.addCallMethod ( "taglib/tag/tag-class", "setTagClass", 0 );
        digester.addCallMethod ( "taglib/tag/teiclass", "setTeiClass", 0 );
        digester.addCallMethod ( "taglib/tag/tei-class", "setTeiClass", 0 );
        digester.addCallMethod ( "taglib/tag/bodycontent", "setBodyContent", 0 );
        digester.addCallMethod ( "taglib/tag/body-content", "setBodyContent", 0 );
        digester.addRule ( "taglib/tag/variable", new ScriptVariableRule() );
        digester.addCallMethod ( "taglib/tag/variable/name-given", "setNameGiven", 0 );
        digester.addCallMethod ( "taglib/tag/variable/name-from-attribute", "setNameFromAttribute", 0 );
        digester.addCallMethod ( "taglib/tag/variable/variable-class", "setClassName", 0 );
        digester.addRule ( "taglib/tag/variable/declare", new GenericBooleanRule ( ( Class ) Variable.class, "setDeclare" ) );
        digester.addCallMethod ( "taglib/tag/variable/scope", "setScope", 0 );
        digester.addRule ( "taglib/tag/attribute", new TagAttributeRule() );
        digester.addCallMethod ( "taglib/tag/attribute/description", "setDescription", 0 );
        digester.addCallMethod ( "taglib/tag/attribute/name", "setName", 0 );
        digester.addRule ( "taglib/tag/attribute/required", new GenericBooleanRule ( ( Class ) Attribute.class, "setRequired" ) );
        digester.addRule ( "taglib/tag/attribute/rtexprvalue", new GenericBooleanRule ( ( Class ) Attribute.class, "setRequestTime" ) );
        digester.addCallMethod ( "taglib/tag/attribute/type", "setType", 0 );
        digester.addCallMethod ( "taglib/tag/attribute/deferred-value", "setDeferredValue" );
        digester.addCallMethod ( "taglib/tag/attribute/deferred-value/type", "setExpectedTypeName", 0 );
        digester.addCallMethod ( "taglib/tag/attribute/deferred-method", "setDeferredMethod" );
        digester.addCallMethod ( "taglib/tag/attribute/deferred-method/method-signature", "setMethodSignature", 0 );
        digester.addRule ( "taglib/tag/attribute/fragment", new GenericBooleanRule ( ( Class ) Attribute.class, "setFragment" ) );
        digester.addRule ( "taglib/tag/dynamic-attributes", new GenericBooleanRule ( ( Class ) TagXml.class, "setDynamicAttributes" ) );
        digester.addSetNext ( "taglib/tag", "addTag", TagXml.class.getName() );
        digester.addObjectCreate ( "taglib/tag-file", TagFileXml.class.getName() );
        this.addDescriptionGroup ( digester, "taglib/tag-file" );
        digester.addCallMethod ( "taglib/tag-file/name", "setName", 0 );
        digester.addCallMethod ( "taglib/tag-file/path", "setPath", 0 );
        digester.addSetNext ( "taglib/tag-file", "addTagFile", TagFileXml.class.getName() );
        digester.addCallMethod ( "taglib/function", "addFunction", 3 );
        digester.addCallParam ( "taglib/function/name", 0 );
        digester.addCallParam ( "taglib/function/function-class", 1 );
        digester.addCallParam ( "taglib/function/function-signature", 2 );
    }
    private void addDescriptionGroup ( final Digester digester, final String prefix ) {
        digester.addCallMethod ( prefix + "/info", "setInfo", 0 );
        digester.addCallMethod ( prefix + "small-icon", "setSmallIcon", 0 );
        digester.addCallMethod ( prefix + "large-icon", "setLargeIcon", 0 );
        digester.addCallMethod ( prefix + "/description", "setInfo", 0 );
        digester.addCallMethod ( prefix + "/display-name", "setDisplayName", 0 );
        digester.addCallMethod ( prefix + "/icon/small-icon", "setSmallIcon", 0 );
        digester.addCallMethod ( prefix + "/icon/large-icon", "setLargeIcon", 0 );
    }
    private static class TagAttributeRule extends Rule {
        @Override
        public void begin ( final String namespace, final String name, final Attributes attributes ) throws Exception {
            final TaglibXml taglibXml = ( TaglibXml ) this.digester.peek ( this.digester.getCount() - 1 );
            this.digester.push ( new Attribute ( "1.2".equals ( taglibXml.getJspVersion() ) ) );
        }
        @Override
        public void end ( final String namespace, final String name ) throws Exception {
            final Attribute attribute = ( Attribute ) this.digester.pop();
            final TagXml tag = ( TagXml ) this.digester.peek();
            tag.getAttributes().add ( attribute.toTagAttributeInfo() );
        }
    }
    public static class Attribute {
        private final boolean allowShortNames;
        private String name;
        private boolean required;
        private String type;
        private boolean requestTime;
        private boolean fragment;
        private String description;
        private boolean deferredValue;
        private boolean deferredMethod;
        private String expectedTypeName;
        private String methodSignature;
        private Attribute ( final boolean allowShortNames ) {
            this.allowShortNames = allowShortNames;
        }
        public void setName ( final String name ) {
            this.name = name;
        }
        public void setRequired ( final boolean required ) {
            this.required = required;
        }
        public void setType ( final String type ) {
            if ( this.allowShortNames ) {
                switch ( type ) {
                case "Boolean": {
                    this.type = "java.lang.Boolean";
                    break;
                }
                case "Character": {
                    this.type = "java.lang.Character";
                    break;
                }
                case "Byte": {
                    this.type = "java.lang.Byte";
                    break;
                }
                case "Short": {
                    this.type = "java.lang.Short";
                    break;
                }
                case "Integer": {
                    this.type = "java.lang.Integer";
                    break;
                }
                case "Long": {
                    this.type = "java.lang.Long";
                    break;
                }
                case "Float": {
                    this.type = "java.lang.Float";
                    break;
                }
                case "Double": {
                    this.type = "java.lang.Double";
                    break;
                }
                case "String": {
                    this.type = "java.lang.String";
                    break;
                }
                case "Object": {
                    this.type = "java.lang.Object";
                    break;
                }
                default: {
                    this.type = type;
                    break;
                }
                }
            } else {
                this.type = type;
            }
        }
        public void setRequestTime ( final boolean requestTime ) {
            this.requestTime = requestTime;
        }
        public void setFragment ( final boolean fragment ) {
            this.fragment = fragment;
        }
        public void setDescription ( final String description ) {
            this.description = description;
        }
        public void setDeferredValue() {
            this.deferredValue = true;
        }
        public void setDeferredMethod() {
            this.deferredMethod = true;
        }
        public void setExpectedTypeName ( final String expectedTypeName ) {
            this.expectedTypeName = expectedTypeName;
        }
        public void setMethodSignature ( final String methodSignature ) {
            this.methodSignature = methodSignature;
        }
        public TagAttributeInfo toTagAttributeInfo() {
            if ( this.fragment ) {
                this.type = "javax.servlet.jsp.tagext.JspFragment";
                this.requestTime = true;
            } else if ( this.deferredValue ) {
                this.type = "javax.el.ValueExpression";
                if ( this.expectedTypeName == null ) {
                    this.expectedTypeName = "java.lang.Object";
                }
            } else if ( this.deferredMethod ) {
                this.type = "javax.el.MethodExpression";
                if ( this.methodSignature == null ) {
                    this.methodSignature = "java.lang.Object method()";
                }
            }
            if ( !this.requestTime && this.type == null ) {
                this.type = "java.lang.String";
            }
            return new TagAttributeInfo ( this.name, this.required, this.type, this.requestTime, this.fragment, this.description, this.deferredValue, this.deferredMethod, this.expectedTypeName, this.methodSignature );
        }
    }
    private static class ScriptVariableRule extends Rule {
        @Override
        public void begin ( final String namespace, final String name, final Attributes attributes ) throws Exception {
            this.digester.push ( new Variable() );
        }
        @Override
        public void end ( final String namespace, final String name ) throws Exception {
            final Variable variable = ( Variable ) this.digester.pop();
            final TagXml tag = ( TagXml ) this.digester.peek();
            tag.getVariables().add ( variable.toTagVariableInfo() );
        }
    }
    public static class Variable {
        private String nameGiven;
        private String nameFromAttribute;
        private String className;
        private boolean declare;
        private int scope;
        public Variable() {
            this.className = "java.lang.String";
            this.declare = true;
            this.scope = 0;
        }
        public void setNameGiven ( final String nameGiven ) {
            this.nameGiven = nameGiven;
        }
        public void setNameFromAttribute ( final String nameFromAttribute ) {
            this.nameFromAttribute = nameFromAttribute;
        }
        public void setClassName ( final String className ) {
            this.className = className;
        }
        public void setDeclare ( final boolean declare ) {
            this.declare = declare;
        }
        public void setScope ( final String scopeName ) {
            switch ( scopeName ) {
            case "NESTED": {
                this.scope = 0;
                break;
            }
            case "AT_BEGIN": {
                this.scope = 1;
                break;
            }
            case "AT_END": {
                this.scope = 2;
                break;
            }
            }
        }
        public TagVariableInfo toTagVariableInfo() {
            return new TagVariableInfo ( this.nameGiven, this.nameFromAttribute, this.className, this.declare, this.scope );
        }
    }
    private static class GenericBooleanRule extends Rule {
        private final Method setter;
        private GenericBooleanRule ( final Class<?> type, final String setterName ) {
            try {
                this.setter = type.getMethod ( setterName, Boolean.TYPE );
            } catch ( NoSuchMethodException e ) {
                throw new IllegalArgumentException ( e );
            }
        }
        @Override
        public void body ( final String namespace, final String name, String text ) throws Exception {
            if ( null != text ) {
                text = text.trim();
            }
            final boolean value = "true".equalsIgnoreCase ( text ) || "yes".equalsIgnoreCase ( text );
            this.setter.invoke ( this.digester.peek(), value );
        }
    }
}
