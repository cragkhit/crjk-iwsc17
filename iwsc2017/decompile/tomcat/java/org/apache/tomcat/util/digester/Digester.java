package org.apache.tomcat.util.digester;
import java.security.Permission;
import java.util.PropertyPermission;
import org.apache.tomcat.util.security.PermissionCheck;
import org.apache.tomcat.util.ExceptionUtils;
import java.util.Hashtable;
import org.xml.sax.helpers.AttributesImpl;
import java.lang.reflect.InvocationTargetException;
import java.io.InputStream;
import java.io.FileInputStream;
import java.io.File;
import org.xml.sax.SAXParseException;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URI;
import org.xml.sax.InputSource;
import org.xml.sax.Attributes;
import org.xml.sax.ext.Locator2;
import java.util.Iterator;
import org.xml.sax.SAXException;
import org.xml.sax.ContentHandler;
import org.xml.sax.DTDHandler;
import javax.xml.parsers.ParserConfigurationException;
import org.xml.sax.SAXNotSupportedException;
import org.xml.sax.SAXNotRecognizedException;
import java.util.EmptyStackException;
import org.apache.juli.logging.LogFactory;
import org.apache.juli.logging.Log;
import java.util.Map;
import org.xml.sax.XMLReader;
import javax.xml.parsers.SAXParser;
import org.xml.sax.Locator;
import javax.xml.parsers.SAXParserFactory;
import org.xml.sax.ErrorHandler;
import java.util.HashMap;
import org.xml.sax.EntityResolver;
import java.util.List;
import org.apache.tomcat.util.IntrospectionUtils;
import org.xml.sax.ext.DefaultHandler2;
public class Digester extends DefaultHandler2 {
    protected static IntrospectionUtils.PropertySource propertySource;
    protected IntrospectionUtils.PropertySource[] source;
    protected StringBuilder bodyText;
    protected ArrayStack<StringBuilder> bodyTexts;
    protected ArrayStack<List<Rule>> matches;
    protected ClassLoader classLoader;
    protected boolean configured;
    protected EntityResolver entityResolver;
    protected HashMap<String, String> entityValidator;
    protected ErrorHandler errorHandler;
    protected SAXParserFactory factory;
    protected Locator locator;
    protected String match;
    protected boolean namespaceAware;
    protected HashMap<String, ArrayStack<String>> namespaces;
    protected ArrayStack<Object> params;
    protected SAXParser parser;
    protected String publicId;
    protected XMLReader reader;
    protected Object root;
    protected Rules rules;
    protected ArrayStack<Object> stack;
    protected boolean useContextClassLoader;
    protected boolean validating;
    protected boolean rulesValidation;
    protected Map<Class<?>, List<String>> fakeAttributes;
    protected Log log;
    protected Log saxLog;
    public Digester() {
        this.source = new IntrospectionUtils.PropertySource[] { new SystemPropertySource() };
        this.bodyText = new StringBuilder();
        this.bodyTexts = new ArrayStack<StringBuilder>();
        this.matches = new ArrayStack<List<Rule>> ( 10 );
        this.classLoader = null;
        this.configured = false;
        this.entityValidator = new HashMap<String, String>();
        this.errorHandler = null;
        this.factory = null;
        this.locator = null;
        this.match = "";
        this.namespaceAware = false;
        this.namespaces = new HashMap<String, ArrayStack<String>>();
        this.params = new ArrayStack<Object>();
        this.parser = null;
        this.publicId = null;
        this.reader = null;
        this.root = null;
        this.rules = null;
        this.stack = new ArrayStack<Object>();
        this.useContextClassLoader = false;
        this.validating = false;
        this.rulesValidation = false;
        this.fakeAttributes = null;
        this.log = LogFactory.getLog ( "org.apache.tomcat.util.digester.Digester" );
        this.saxLog = LogFactory.getLog ( "org.apache.tomcat.util.digester.Digester.sax" );
        if ( Digester.propertySource != null ) {
            this.source = new IntrospectionUtils.PropertySource[] { Digester.propertySource, this.source[0] };
        }
    }
    public String findNamespaceURI ( final String prefix ) {
        final ArrayStack<String> stack = this.namespaces.get ( prefix );
        if ( stack == null ) {
            return null;
        }
        try {
            return stack.peek();
        } catch ( EmptyStackException e ) {
            return null;
        }
    }
    public ClassLoader getClassLoader() {
        if ( this.classLoader != null ) {
            return this.classLoader;
        }
        if ( this.useContextClassLoader ) {
            final ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
            if ( classLoader != null ) {
                return classLoader;
            }
        }
        return this.getClass().getClassLoader();
    }
    public void setClassLoader ( final ClassLoader classLoader ) {
        this.classLoader = classLoader;
    }
    public int getCount() {
        return this.stack.size();
    }
    public String getCurrentElementName() {
        String elementName = this.match;
        final int lastSlash = elementName.lastIndexOf ( 47 );
        if ( lastSlash >= 0 ) {
            elementName = elementName.substring ( lastSlash + 1 );
        }
        return elementName;
    }
    public ErrorHandler getErrorHandler() {
        return this.errorHandler;
    }
    public void setErrorHandler ( final ErrorHandler errorHandler ) {
        this.errorHandler = errorHandler;
    }
    public SAXParserFactory getFactory() throws SAXNotRecognizedException, SAXNotSupportedException, ParserConfigurationException {
        if ( this.factory == null ) {
            ( this.factory = SAXParserFactory.newInstance() ).setNamespaceAware ( this.namespaceAware );
            if ( this.namespaceAware ) {
                this.factory.setFeature ( "http://xml.org/sax/features/namespace-prefixes", true );
            }
            this.factory.setValidating ( this.validating );
            if ( this.validating ) {
                this.factory.setFeature ( "http://xml.org/sax/features/validation", true );
                this.factory.setFeature ( "http://apache.org/xml/features/validation/schema", true );
            }
        }
        return this.factory;
    }
    public void setFeature ( final String feature, final boolean value ) throws ParserConfigurationException, SAXNotRecognizedException, SAXNotSupportedException {
        this.getFactory().setFeature ( feature, value );
    }
    public Log getLogger() {
        return this.log;
    }
    public void setLogger ( final Log log ) {
        this.log = log;
    }
    public Log getSAXLogger() {
        return this.saxLog;
    }
    public void setSAXLogger ( final Log saxLog ) {
        this.saxLog = saxLog;
    }
    public String getMatch() {
        return this.match;
    }
    public boolean getNamespaceAware() {
        return this.namespaceAware;
    }
    public void setNamespaceAware ( final boolean namespaceAware ) {
        this.namespaceAware = namespaceAware;
    }
    public void setPublicId ( final String publicId ) {
        this.publicId = publicId;
    }
    public String getPublicId() {
        return this.publicId;
    }
    public String getRuleNamespaceURI() {
        return this.getRules().getNamespaceURI();
    }
    public void setRuleNamespaceURI ( final String ruleNamespaceURI ) {
        this.getRules().setNamespaceURI ( ruleNamespaceURI );
    }
    public SAXParser getParser() {
        if ( this.parser != null ) {
            return this.parser;
        }
        try {
            this.parser = this.getFactory().newSAXParser();
        } catch ( Exception e ) {
            this.log.error ( "Digester.getParser: ", e );
            return null;
        }
        return this.parser;
    }
    public Object getProperty ( final String property ) throws SAXNotRecognizedException, SAXNotSupportedException {
        return this.getParser().getProperty ( property );
    }
    public Rules getRules() {
        if ( this.rules == null ) {
            ( this.rules = new RulesBase() ).setDigester ( this );
        }
        return this.rules;
    }
    public void setRules ( final Rules rules ) {
        ( this.rules = rules ).setDigester ( this );
    }
    public boolean getUseContextClassLoader() {
        return this.useContextClassLoader;
    }
    public void setUseContextClassLoader ( final boolean use ) {
        this.useContextClassLoader = use;
    }
    public boolean getValidating() {
        return this.validating;
    }
    public void setValidating ( final boolean validating ) {
        this.validating = validating;
    }
    public boolean getRulesValidation() {
        return this.rulesValidation;
    }
    public void setRulesValidation ( final boolean rulesValidation ) {
        this.rulesValidation = rulesValidation;
    }
    public Map<Class<?>, List<String>> getFakeAttributes() {
        return this.fakeAttributes;
    }
    public boolean isFakeAttribute ( final Object object, final String name ) {
        if ( this.fakeAttributes == null ) {
            return false;
        }
        List<String> result = this.fakeAttributes.get ( object.getClass() );
        if ( result == null ) {
            result = this.fakeAttributes.get ( Object.class );
        }
        return result != null && result.contains ( name );
    }
    public void setFakeAttributes ( final Map<Class<?>, List<String>> fakeAttributes ) {
        this.fakeAttributes = fakeAttributes;
    }
    public XMLReader getXMLReader() throws SAXException {
        if ( this.reader == null ) {
            this.reader = this.getParser().getXMLReader();
        }
        this.reader.setDTDHandler ( this );
        this.reader.setContentHandler ( this );
        if ( this.entityResolver == null ) {
            this.reader.setEntityResolver ( this );
        } else {
            this.reader.setEntityResolver ( this.entityResolver );
        }
        this.reader.setProperty ( "http://xml.org/sax/properties/lexical-handler", this );
        this.reader.setErrorHandler ( this );
        return this.reader;
    }
    @Override
    public void characters ( final char[] buffer, final int start, final int length ) throws SAXException {
        if ( this.saxLog.isDebugEnabled() ) {
            this.saxLog.debug ( "characters(" + new String ( buffer, start, length ) + ")" );
        }
        this.bodyText.append ( buffer, start, length );
    }
    @Override
    public void endDocument() throws SAXException {
        if ( this.saxLog.isDebugEnabled() ) {
            if ( this.getCount() > 1 ) {
                this.saxLog.debug ( "endDocument():  " + this.getCount() + " elements left" );
            } else {
                this.saxLog.debug ( "endDocument()" );
            }
        }
        while ( this.getCount() > 1 ) {
            this.pop();
        }
        for ( final Rule rule : this.getRules().rules() ) {
            try {
                rule.finish();
            } catch ( Exception e ) {
                this.log.error ( "Finish event threw exception", e );
                throw this.createSAXException ( e );
            } catch ( Error e2 ) {
                this.log.error ( "Finish event threw error", e2 );
                throw e2;
            }
        }
        this.clear();
    }
    @Override
    public void endElement ( final String namespaceURI, final String localName, final String qName ) throws SAXException {
        final boolean debug = this.log.isDebugEnabled();
        if ( debug ) {
            if ( this.saxLog.isDebugEnabled() ) {
                this.saxLog.debug ( "endElement(" + namespaceURI + "," + localName + "," + qName + ")" );
            }
            this.log.debug ( "  match='" + this.match + "'" );
            this.log.debug ( "  bodyText='" + ( Object ) this.bodyText + "'" );
        }
        this.bodyText = this.updateBodyText ( this.bodyText );
        String name = localName;
        if ( name == null || name.length() < 1 ) {
            name = qName;
        }
        final List<Rule> rules = this.matches.pop();
        if ( rules != null && rules.size() > 0 ) {
            final String bodyText = this.bodyText.toString();
            for ( int i = 0; i < rules.size(); ++i ) {
                try {
                    final Rule rule = rules.get ( i );
                    if ( debug ) {
                        this.log.debug ( "  Fire body() for " + rule );
                    }
                    rule.body ( namespaceURI, name, bodyText );
                } catch ( Exception e ) {
                    this.log.error ( "Body event threw exception", e );
                    throw this.createSAXException ( e );
                } catch ( Error e2 ) {
                    this.log.error ( "Body event threw error", e2 );
                    throw e2;
                }
            }
        } else {
            if ( debug ) {
                this.log.debug ( "  No rules found matching '" + this.match + "'." );
            }
            if ( this.rulesValidation ) {
                this.log.warn ( "  No rules found matching '" + this.match + "'." );
            }
        }
        this.bodyText = this.bodyTexts.pop();
        if ( rules != null ) {
            for ( int j = 0; j < rules.size(); ++j ) {
                final int k = rules.size() - j - 1;
                try {
                    final Rule rule = rules.get ( k );
                    if ( debug ) {
                        this.log.debug ( "  Fire end() for " + rule );
                    }
                    rule.end ( namespaceURI, name );
                } catch ( Exception e ) {
                    this.log.error ( "End event threw exception", e );
                    throw this.createSAXException ( e );
                } catch ( Error e2 ) {
                    this.log.error ( "End event threw error", e2 );
                    throw e2;
                }
            }
        }
        final int slash = this.match.lastIndexOf ( 47 );
        if ( slash >= 0 ) {
            this.match = this.match.substring ( 0, slash );
        } else {
            this.match = "";
        }
    }
    @Override
    public void endPrefixMapping ( final String prefix ) throws SAXException {
        if ( this.saxLog.isDebugEnabled() ) {
            this.saxLog.debug ( "endPrefixMapping(" + prefix + ")" );
        }
        final ArrayStack<String> stack = this.namespaces.get ( prefix );
        if ( stack == null ) {
            return;
        }
        try {
            stack.pop();
            if ( stack.empty() ) {
                this.namespaces.remove ( prefix );
            }
        } catch ( EmptyStackException e ) {
            throw this.createSAXException ( "endPrefixMapping popped too many times" );
        }
    }
    @Override
    public void ignorableWhitespace ( final char[] buffer, final int start, final int len ) throws SAXException {
        if ( this.saxLog.isDebugEnabled() ) {
            this.saxLog.debug ( "ignorableWhitespace(" + new String ( buffer, start, len ) + ")" );
        }
    }
    @Override
    public void processingInstruction ( final String target, final String data ) throws SAXException {
        if ( this.saxLog.isDebugEnabled() ) {
            this.saxLog.debug ( "processingInstruction('" + target + "','" + data + "')" );
        }
    }
    public Locator getDocumentLocator() {
        return this.locator;
    }
    @Override
    public void setDocumentLocator ( final Locator locator ) {
        if ( this.saxLog.isDebugEnabled() ) {
            this.saxLog.debug ( "setDocumentLocator(" + locator + ")" );
        }
        this.locator = locator;
    }
    @Override
    public void skippedEntity ( final String name ) throws SAXException {
        if ( this.saxLog.isDebugEnabled() ) {
            this.saxLog.debug ( "skippedEntity(" + name + ")" );
        }
    }
    @Override
    public void startDocument() throws SAXException {
        if ( this.saxLog.isDebugEnabled() ) {
            this.saxLog.debug ( "startDocument()" );
        }
        if ( this.locator instanceof Locator2 && this.root instanceof DocumentProperties.Encoding ) {
            ( ( DocumentProperties.Encoding ) this.root ).setEncoding ( ( ( Locator2 ) this.locator ).getEncoding() );
        }
        this.configure();
    }
    @Override
    public void startElement ( final String namespaceURI, final String localName, final String qName, Attributes list ) throws SAXException {
        final boolean debug = this.log.isDebugEnabled();
        if ( this.saxLog.isDebugEnabled() ) {
            this.saxLog.debug ( "startElement(" + namespaceURI + "," + localName + "," + qName + ")" );
        }
        list = this.updateAttributes ( list );
        this.bodyTexts.push ( this.bodyText );
        this.bodyText = new StringBuilder();
        String name = localName;
        if ( name == null || name.length() < 1 ) {
            name = qName;
        }
        final StringBuilder sb = new StringBuilder ( this.match );
        if ( this.match.length() > 0 ) {
            sb.append ( '/' );
        }
        sb.append ( name );
        this.match = sb.toString();
        if ( debug ) {
            this.log.debug ( "  New match='" + this.match + "'" );
        }
        final List<Rule> rules = this.getRules().match ( namespaceURI, this.match );
        this.matches.push ( rules );
        if ( rules != null && rules.size() > 0 ) {
            for ( int i = 0; i < rules.size(); ++i ) {
                try {
                    final Rule rule = rules.get ( i );
                    if ( debug ) {
                        this.log.debug ( "  Fire begin() for " + rule );
                    }
                    rule.begin ( namespaceURI, name, list );
                } catch ( Exception e ) {
                    this.log.error ( "Begin event threw exception", e );
                    throw this.createSAXException ( e );
                } catch ( Error e2 ) {
                    this.log.error ( "Begin event threw error", e2 );
                    throw e2;
                }
            }
        } else if ( debug ) {
            this.log.debug ( "  No rules found matching '" + this.match + "'." );
        }
    }
    @Override
    public void startPrefixMapping ( final String prefix, final String namespaceURI ) throws SAXException {
        if ( this.saxLog.isDebugEnabled() ) {
            this.saxLog.debug ( "startPrefixMapping(" + prefix + "," + namespaceURI + ")" );
        }
        ArrayStack<String> stack = this.namespaces.get ( prefix );
        if ( stack == null ) {
            stack = new ArrayStack<String>();
            this.namespaces.put ( prefix, stack );
        }
        stack.push ( namespaceURI );
    }
    @Override
    public void notationDecl ( final String name, final String publicId, final String systemId ) {
        if ( this.saxLog.isDebugEnabled() ) {
            this.saxLog.debug ( "notationDecl(" + name + "," + publicId + "," + systemId + ")" );
        }
    }
    @Override
    public void unparsedEntityDecl ( final String name, final String publicId, final String systemId, final String notation ) {
        if ( this.saxLog.isDebugEnabled() ) {
            this.saxLog.debug ( "unparsedEntityDecl(" + name + "," + publicId + "," + systemId + "," + notation + ")" );
        }
    }
    public void setEntityResolver ( final EntityResolver entityResolver ) {
        this.entityResolver = entityResolver;
    }
    public EntityResolver getEntityResolver() {
        return this.entityResolver;
    }
    @Override
    public InputSource resolveEntity ( final String name, final String publicId, final String baseURI, final String systemId ) throws SAXException, IOException {
        if ( this.saxLog.isDebugEnabled() ) {
            this.saxLog.debug ( "resolveEntity('" + publicId + "', '" + systemId + "', '" + baseURI + "')" );
        }
        String entityURL = null;
        if ( publicId != null ) {
            entityURL = this.entityValidator.get ( publicId );
        }
        if ( entityURL == null ) {
            if ( systemId == null ) {
                if ( this.log.isDebugEnabled() ) {
                    this.log.debug ( " Cannot resolve entity: '" + publicId + "'" );
                }
                return null;
            }
            if ( this.log.isDebugEnabled() ) {
                this.log.debug ( " Trying to resolve using system ID '" + systemId + "'" );
            }
            entityURL = systemId;
            if ( baseURI != null ) {
                try {
                    final URI uri = new URI ( systemId );
                    if ( !uri.isAbsolute() ) {
                        entityURL = new URI ( baseURI ).resolve ( uri ).toString();
                    }
                } catch ( URISyntaxException e2 ) {
                    if ( this.log.isDebugEnabled() ) {
                        this.log.debug ( "Invalid URI '" + baseURI + "' or '" + systemId + "'" );
                    }
                }
            }
        }
        if ( this.log.isDebugEnabled() ) {
            this.log.debug ( " Resolving to alternate DTD '" + entityURL + "'" );
        }
        try {
            return new InputSource ( entityURL );
        } catch ( Exception e ) {
            throw this.createSAXException ( e );
        }
    }
    @Override
    public void startDTD ( final String name, final String publicId, final String systemId ) throws SAXException {
        this.setPublicId ( publicId );
    }
    @Override
    public void error ( final SAXParseException exception ) throws SAXException {
        this.log.error ( "Parse Error at line " + exception.getLineNumber() + " column " + exception.getColumnNumber() + ": " + exception.getMessage(), exception );
        if ( this.errorHandler != null ) {
            this.errorHandler.error ( exception );
        }
    }
    @Override
    public void fatalError ( final SAXParseException exception ) throws SAXException {
        this.log.error ( "Parse Fatal Error at line " + exception.getLineNumber() + " column " + exception.getColumnNumber() + ": " + exception.getMessage(), exception );
        if ( this.errorHandler != null ) {
            this.errorHandler.fatalError ( exception );
        }
    }
    @Override
    public void warning ( final SAXParseException exception ) throws SAXException {
        if ( this.errorHandler != null ) {
            this.log.warn ( "Parse Warning Error at line " + exception.getLineNumber() + " column " + exception.getColumnNumber() + ": " + exception.getMessage(), exception );
            this.errorHandler.warning ( exception );
        }
    }
    public Object parse ( final File file ) throws IOException, SAXException {
        this.configure();
        final InputSource input = new InputSource ( new FileInputStream ( file ) );
        input.setSystemId ( "file://" + file.getAbsolutePath() );
        this.getXMLReader().parse ( input );
        return this.root;
    }
    public Object parse ( final InputSource input ) throws IOException, SAXException {
        this.configure();
        this.getXMLReader().parse ( input );
        return this.root;
    }
    public Object parse ( final InputStream input ) throws IOException, SAXException {
        this.configure();
        final InputSource is = new InputSource ( input );
        this.getXMLReader().parse ( is );
        return this.root;
    }
    public void register ( final String publicId, final String entityURL ) {
        if ( this.log.isDebugEnabled() ) {
            this.log.debug ( "register('" + publicId + "', '" + entityURL + "'" );
        }
        this.entityValidator.put ( publicId, entityURL );
    }
    public void addRule ( final String pattern, final Rule rule ) {
        rule.setDigester ( this );
        this.getRules().add ( pattern, rule );
    }
    public void addRuleSet ( final RuleSet ruleSet ) {
        final String oldNamespaceURI = this.getRuleNamespaceURI();
        final String newNamespaceURI = ruleSet.getNamespaceURI();
        if ( this.log.isDebugEnabled() ) {
            if ( newNamespaceURI == null ) {
                this.log.debug ( "addRuleSet() with no namespace URI" );
            } else {
                this.log.debug ( "addRuleSet() with namespace URI " + newNamespaceURI );
            }
        }
        this.setRuleNamespaceURI ( newNamespaceURI );
        ruleSet.addRuleInstances ( this );
        this.setRuleNamespaceURI ( oldNamespaceURI );
    }
    public void addCallMethod ( final String pattern, final String methodName ) {
        this.addRule ( pattern, new CallMethodRule ( methodName ) );
    }
    public void addCallMethod ( final String pattern, final String methodName, final int paramCount ) {
        this.addRule ( pattern, new CallMethodRule ( methodName, paramCount ) );
    }
    public void addCallParam ( final String pattern, final int paramIndex ) {
        this.addRule ( pattern, new CallParamRule ( paramIndex ) );
    }
    public void addFactoryCreate ( final String pattern, final ObjectCreationFactory creationFactory, final boolean ignoreCreateExceptions ) {
        creationFactory.setDigester ( this );
        this.addRule ( pattern, new FactoryCreateRule ( creationFactory, ignoreCreateExceptions ) );
    }
    public void addObjectCreate ( final String pattern, final String className ) {
        this.addRule ( pattern, new ObjectCreateRule ( className ) );
    }
    public void addObjectCreate ( final String pattern, final String className, final String attributeName ) {
        this.addRule ( pattern, new ObjectCreateRule ( className, attributeName ) );
    }
    public void addSetNext ( final String pattern, final String methodName, final String paramType ) {
        this.addRule ( pattern, new SetNextRule ( methodName, paramType ) );
    }
    public void addSetProperties ( final String pattern ) {
        this.addRule ( pattern, new SetPropertiesRule() );
    }
    public void clear() {
        this.match = "";
        this.bodyTexts.clear();
        this.params.clear();
        this.publicId = null;
        this.stack.clear();
        this.log = null;
        this.saxLog = null;
        this.configured = false;
    }
    public void reset() {
        this.root = null;
        this.setErrorHandler ( null );
        this.clear();
    }
    public Object peek() {
        try {
            return this.stack.peek();
        } catch ( EmptyStackException e ) {
            this.log.warn ( "Empty stack (returning null)" );
            return null;
        }
    }
    public Object peek ( final int n ) {
        try {
            return this.stack.peek ( n );
        } catch ( EmptyStackException e ) {
            this.log.warn ( "Empty stack (returning null)" );
            return null;
        }
    }
    public Object pop() {
        try {
            return this.stack.pop();
        } catch ( EmptyStackException e ) {
            this.log.warn ( "Empty stack (returning null)" );
            return null;
        }
    }
    public void push ( final Object object ) {
        if ( this.stack.size() == 0 ) {
            this.root = object;
        }
        this.stack.push ( object );
    }
    public Object getRoot() {
        return this.root;
    }
    protected void configure() {
        if ( this.configured ) {
            return;
        }
        this.log = LogFactory.getLog ( "org.apache.tomcat.util.digester.Digester" );
        this.saxLog = LogFactory.getLog ( "org.apache.tomcat.util.digester.Digester.sax" );
        this.configured = true;
    }
    public Object peekParams() {
        try {
            return this.params.peek();
        } catch ( EmptyStackException e ) {
            this.log.warn ( "Empty stack (returning null)" );
            return null;
        }
    }
    public Object popParams() {
        try {
            if ( this.log.isTraceEnabled() ) {
                this.log.trace ( "Popping params" );
            }
            return this.params.pop();
        } catch ( EmptyStackException e ) {
            this.log.warn ( "Empty stack (returning null)" );
            return null;
        }
    }
    public void pushParams ( final Object object ) {
        if ( this.log.isTraceEnabled() ) {
            this.log.trace ( "Pushing params" );
        }
        this.params.push ( object );
    }
    public SAXException createSAXException ( final String message, Exception e ) {
        if ( e != null && e instanceof InvocationTargetException ) {
            final Throwable t = e.getCause();
            if ( t instanceof ThreadDeath ) {
                throw ( ThreadDeath ) t;
            }
            if ( t instanceof VirtualMachineError ) {
                throw ( VirtualMachineError ) t;
            }
            if ( t instanceof Exception ) {
                e = ( Exception ) t;
            }
        }
        if ( this.locator != null ) {
            final String error = "Error at (" + this.locator.getLineNumber() + ", " + this.locator.getColumnNumber() + ") : " + message;
            if ( e != null ) {
                return new SAXParseException ( error, this.locator, e );
            }
            return new SAXParseException ( error, this.locator );
        } else {
            this.log.error ( "No Locator!" );
            if ( e != null ) {
                return new SAXException ( message, e );
            }
            return new SAXException ( message );
        }
    }
    public SAXException createSAXException ( Exception e ) {
        if ( e instanceof InvocationTargetException ) {
            final Throwable t = e.getCause();
            if ( t instanceof ThreadDeath ) {
                throw ( ThreadDeath ) t;
            }
            if ( t instanceof VirtualMachineError ) {
                throw ( VirtualMachineError ) t;
            }
            if ( t instanceof Exception ) {
                e = ( Exception ) t;
            }
        }
        return this.createSAXException ( e.getMessage(), e );
    }
    public SAXException createSAXException ( final String message ) {
        return this.createSAXException ( message, null );
    }
    private Attributes updateAttributes ( final Attributes list ) {
        if ( list.getLength() == 0 ) {
            return list;
        }
        final AttributesImpl newAttrs = new AttributesImpl ( list );
        for ( int nAttributes = newAttrs.getLength(), i = 0; i < nAttributes; ++i ) {
            final String value = newAttrs.getValue ( i );
            try {
                final String newValue = IntrospectionUtils.replaceProperties ( value, null, this.source );
                if ( value != newValue ) {
                    newAttrs.setValue ( i, newValue );
                }
            } catch ( Exception ex ) {}
        }
        return newAttrs;
    }
    private StringBuilder updateBodyText ( final StringBuilder bodyText ) {
        final String in = bodyText.toString();
        String out;
        try {
            out = IntrospectionUtils.replaceProperties ( in, null, this.source );
        } catch ( Exception e ) {
            return bodyText;
        }
        if ( out == in ) {
            return bodyText;
        }
        return new StringBuilder ( out );
    }
    static {
        Digester.propertySource = null;
        final String className = System.getProperty ( "org.apache.tomcat.util.digester.PROPERTY_SOURCE" );
        if ( className != null ) {
            final ClassLoader[] cls = { Digester.class.getClassLoader(), Thread.currentThread().getContextClassLoader() };
            int i = 0;
            while ( i < cls.length ) {
                try {
                    final Class<?> clazz = Class.forName ( className, true, cls[i] );
                    Digester.propertySource = ( IntrospectionUtils.PropertySource ) clazz.newInstance();
                } catch ( Throwable t ) {
                    ExceptionUtils.handleThrowable ( t );
                    LogFactory.getLog ( "org.apache.tomcat.util.digester.Digester" ).error ( "Unable to load property source[" + className + "].", t );
                    ++i;
                    continue;
                }
                break;
            }
        }
    }
    private class SystemPropertySource implements IntrospectionUtils.PropertySource {
        @Override
        public String getProperty ( final String key ) {
            final ClassLoader cl = Digester.this.getClassLoader();
            if ( cl instanceof PermissionCheck ) {
                final Permission p = new PropertyPermission ( key, "read" );
                if ( ! ( ( PermissionCheck ) cl ).check ( p ) ) {
                    return null;
                }
            }
            return System.getProperty ( key );
        }
    }
}
