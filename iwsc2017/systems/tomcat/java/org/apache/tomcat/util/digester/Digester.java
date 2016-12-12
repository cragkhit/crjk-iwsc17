package org.apache.tomcat.util.digester;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.net.URI;
import java.net.URISyntaxException;
import java.security.Permission;
import java.util.EmptyStackException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.PropertyPermission;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import org.apache.juli.logging.Log;
import org.apache.juli.logging.LogFactory;
import org.apache.tomcat.util.ExceptionUtils;
import org.apache.tomcat.util.IntrospectionUtils;
import org.apache.tomcat.util.security.PermissionCheck;
import org.xml.sax.Attributes;
import org.xml.sax.EntityResolver;
import org.xml.sax.ErrorHandler;
import org.xml.sax.InputSource;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;
import org.xml.sax.SAXNotRecognizedException;
import org.xml.sax.SAXNotSupportedException;
import org.xml.sax.SAXParseException;
import org.xml.sax.XMLReader;
import org.xml.sax.ext.DefaultHandler2;
import org.xml.sax.ext.Locator2;
import org.xml.sax.helpers.AttributesImpl;
public class Digester extends DefaultHandler2 {
    protected static IntrospectionUtils.PropertySource propertySource = null;
    static {
        String className = System.getProperty ( "org.apache.tomcat.util.digester.PROPERTY_SOURCE" );
        if ( className != null ) {
            ClassLoader[] cls = new ClassLoader[] { Digester.class.getClassLoader(),
                                                    Thread.currentThread().getContextClassLoader()
                                                  };
            for ( int i = 0; i < cls.length; i++ ) {
                try {
                    Class<?> clazz = Class.forName ( className, true, cls[i] );
                    propertySource = ( IntrospectionUtils.PropertySource ) clazz.newInstance();
                    break;
                } catch ( Throwable t ) {
                    ExceptionUtils.handleThrowable ( t );
                    LogFactory.getLog ( "org.apache.tomcat.util.digester.Digester" )
                    .error ( "Unable to load property source[" + className + "].", t );
                }
            }
        }
    }
    private class SystemPropertySource implements IntrospectionUtils.PropertySource {
        @Override
        public String getProperty ( String key ) {
            ClassLoader cl = getClassLoader();
            if ( cl instanceof PermissionCheck ) {
                Permission p = new PropertyPermission ( key, "read" );
                if ( ! ( ( PermissionCheck ) cl ).check ( p ) ) {
                    return null;
                }
            }
            return System.getProperty ( key );
        }
    }
    protected IntrospectionUtils.PropertySource source[] = new IntrospectionUtils.PropertySource[] {
        new SystemPropertySource()
    };
    protected StringBuilder bodyText = new StringBuilder();
    protected ArrayStack<StringBuilder> bodyTexts = new ArrayStack<>();
    protected ArrayStack<List<Rule>> matches = new ArrayStack<> ( 10 );
    protected ClassLoader classLoader = null;
    protected boolean configured = false;
    protected EntityResolver entityResolver;
    protected HashMap<String, String> entityValidator = new HashMap<>();
    protected ErrorHandler errorHandler = null;
    protected SAXParserFactory factory = null;
    protected Locator locator = null;
    protected String match = "";
    protected boolean namespaceAware = false;
    protected HashMap<String, ArrayStack<String>> namespaces = new HashMap<>();
    protected ArrayStack<Object> params = new ArrayStack<>();
    protected SAXParser parser = null;
    protected String publicId = null;
    protected XMLReader reader = null;
    protected Object root = null;
    protected Rules rules = null;
    protected ArrayStack<Object> stack = new ArrayStack<>();
    protected boolean useContextClassLoader = false;
    protected boolean validating = false;
    protected boolean rulesValidation = false;
    protected Map<Class<?>, List<String>> fakeAttributes = null;
    protected Log log = LogFactory.getLog ( "org.apache.tomcat.util.digester.Digester" );
    protected Log saxLog = LogFactory.getLog ( "org.apache.tomcat.util.digester.Digester.sax" );
    public Digester() {
        if ( propertySource != null ) {
            source = new IntrospectionUtils.PropertySource[] { propertySource, source[0] };
        }
    }
    public String findNamespaceURI ( String prefix ) {
        ArrayStack<String> stack = namespaces.get ( prefix );
        if ( stack == null ) {
            return ( null );
        }
        try {
            return stack.peek();
        } catch ( EmptyStackException e ) {
            return ( null );
        }
    }
    public ClassLoader getClassLoader() {
        if ( this.classLoader != null ) {
            return ( this.classLoader );
        }
        if ( this.useContextClassLoader ) {
            ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
            if ( classLoader != null ) {
                return ( classLoader );
            }
        }
        return ( this.getClass().getClassLoader() );
    }
    public void setClassLoader ( ClassLoader classLoader ) {
        this.classLoader = classLoader;
    }
    public int getCount() {
        return ( stack.size() );
    }
    public String getCurrentElementName() {
        String elementName = match;
        int lastSlash = elementName.lastIndexOf ( '/' );
        if ( lastSlash >= 0 ) {
            elementName = elementName.substring ( lastSlash + 1 );
        }
        return ( elementName );
    }
    public ErrorHandler getErrorHandler() {
        return ( this.errorHandler );
    }
    public void setErrorHandler ( ErrorHandler errorHandler ) {
        this.errorHandler = errorHandler;
    }
    public SAXParserFactory getFactory() throws SAXNotRecognizedException, SAXNotSupportedException,
        ParserConfigurationException {
        if ( factory == null ) {
            factory = SAXParserFactory.newInstance();
            factory.setNamespaceAware ( namespaceAware );
            if ( namespaceAware ) {
                factory.setFeature ( "http://xml.org/sax/features/namespace-prefixes", true );
            }
            factory.setValidating ( validating );
            if ( validating ) {
                factory.setFeature ( "http://xml.org/sax/features/validation", true );
                factory.setFeature ( "http://apache.org/xml/features/validation/schema", true );
            }
        }
        return ( factory );
    }
    public void setFeature ( String feature, boolean value ) throws ParserConfigurationException,
        SAXNotRecognizedException, SAXNotSupportedException {
        getFactory().setFeature ( feature, value );
    }
    public Log getLogger() {
        return log;
    }
    public void setLogger ( Log log ) {
        this.log = log;
    }
    public Log getSAXLogger() {
        return saxLog;
    }
    public void setSAXLogger ( Log saxLog ) {
        this.saxLog = saxLog;
    }
    public String getMatch() {
        return match;
    }
    public boolean getNamespaceAware() {
        return ( this.namespaceAware );
    }
    public void setNamespaceAware ( boolean namespaceAware ) {
        this.namespaceAware = namespaceAware;
    }
    public void setPublicId ( String publicId ) {
        this.publicId = publicId;
    }
    public String getPublicId() {
        return ( this.publicId );
    }
    public String getRuleNamespaceURI() {
        return ( getRules().getNamespaceURI() );
    }
    public void setRuleNamespaceURI ( String ruleNamespaceURI ) {
        getRules().setNamespaceURI ( ruleNamespaceURI );
    }
    public SAXParser getParser() {
        if ( parser != null ) {
            return ( parser );
        }
        try {
            parser = getFactory().newSAXParser();
        } catch ( Exception e ) {
            log.error ( "Digester.getParser: ", e );
            return ( null );
        }
        return ( parser );
    }
    public Object getProperty ( String property )
    throws SAXNotRecognizedException, SAXNotSupportedException {
        return ( getParser().getProperty ( property ) );
    }
    public Rules getRules() {
        if ( this.rules == null ) {
            this.rules = new RulesBase();
            this.rules.setDigester ( this );
        }
        return ( this.rules );
    }
    public void setRules ( Rules rules ) {
        this.rules = rules;
        this.rules.setDigester ( this );
    }
    public boolean getUseContextClassLoader() {
        return useContextClassLoader;
    }
    public void setUseContextClassLoader ( boolean use ) {
        useContextClassLoader = use;
    }
    public boolean getValidating() {
        return ( this.validating );
    }
    public void setValidating ( boolean validating ) {
        this.validating = validating;
    }
    public boolean getRulesValidation() {
        return ( this.rulesValidation );
    }
    public void setRulesValidation ( boolean rulesValidation ) {
        this.rulesValidation = rulesValidation;
    }
    public Map<Class<?>, List<String>> getFakeAttributes() {
        return ( this.fakeAttributes );
    }
    public boolean isFakeAttribute ( Object object, String name ) {
        if ( fakeAttributes == null ) {
            return false;
        }
        List<String> result = fakeAttributes.get ( object.getClass() );
        if ( result == null ) {
            result = fakeAttributes.get ( Object.class );
        }
        if ( result == null ) {
            return false;
        } else {
            return result.contains ( name );
        }
    }
    public void setFakeAttributes ( Map<Class<?>, List<String>> fakeAttributes ) {
        this.fakeAttributes = fakeAttributes;
    }
    public XMLReader getXMLReader() throws SAXException {
        if ( reader == null ) {
            reader = getParser().getXMLReader();
        }
        reader.setDTDHandler ( this );
        reader.setContentHandler ( this );
        if ( entityResolver == null ) {
            reader.setEntityResolver ( this );
        } else {
            reader.setEntityResolver ( entityResolver );
        }
        reader.setProperty ( "http://xml.org/sax/properties/lexical-handler", this );
        reader.setErrorHandler ( this );
        return reader;
    }
    @Override
    public void characters ( char buffer[], int start, int length ) throws SAXException {
        if ( saxLog.isDebugEnabled() ) {
            saxLog.debug ( "characters(" + new String ( buffer, start, length ) + ")" );
        }
        bodyText.append ( buffer, start, length );
    }
    @Override
    public void endDocument() throws SAXException {
        if ( saxLog.isDebugEnabled() ) {
            if ( getCount() > 1 ) {
                saxLog.debug ( "endDocument():  " + getCount() + " elements left" );
            } else {
                saxLog.debug ( "endDocument()" );
            }
        }
        while ( getCount() > 1 ) {
            pop();
        }
        Iterator<Rule> rules = getRules().rules().iterator();
        while ( rules.hasNext() ) {
            Rule rule = rules.next();
            try {
                rule.finish();
            } catch ( Exception e ) {
                log.error ( "Finish event threw exception", e );
                throw createSAXException ( e );
            } catch ( Error e ) {
                log.error ( "Finish event threw error", e );
                throw e;
            }
        }
        clear();
    }
    @Override
    public void endElement ( String namespaceURI, String localName, String qName )
    throws SAXException {
        boolean debug = log.isDebugEnabled();
        if ( debug ) {
            if ( saxLog.isDebugEnabled() ) {
                saxLog.debug ( "endElement(" + namespaceURI + "," + localName + "," + qName + ")" );
            }
            log.debug ( "  match='" + match + "'" );
            log.debug ( "  bodyText='" + bodyText + "'" );
        }
        bodyText = updateBodyText ( bodyText );
        String name = localName;
        if ( ( name == null ) || ( name.length() < 1 ) ) {
            name = qName;
        }
        List<Rule> rules = matches.pop();
        if ( ( rules != null ) && ( rules.size() > 0 ) ) {
            String bodyText = this.bodyText.toString();
            for ( int i = 0; i < rules.size(); i++ ) {
                try {
                    Rule rule = rules.get ( i );
                    if ( debug ) {
                        log.debug ( "  Fire body() for " + rule );
                    }
                    rule.body ( namespaceURI, name, bodyText );
                } catch ( Exception e ) {
                    log.error ( "Body event threw exception", e );
                    throw createSAXException ( e );
                } catch ( Error e ) {
                    log.error ( "Body event threw error", e );
                    throw e;
                }
            }
        } else {
            if ( debug ) {
                log.debug ( "  No rules found matching '" + match + "'." );
            }
            if ( rulesValidation ) {
                log.warn ( "  No rules found matching '" + match + "'." );
            }
        }
        bodyText = bodyTexts.pop();
        if ( rules != null ) {
            for ( int i = 0; i < rules.size(); i++ ) {
                int j = ( rules.size() - i ) - 1;
                try {
                    Rule rule = rules.get ( j );
                    if ( debug ) {
                        log.debug ( "  Fire end() for " + rule );
                    }
                    rule.end ( namespaceURI, name );
                } catch ( Exception e ) {
                    log.error ( "End event threw exception", e );
                    throw createSAXException ( e );
                } catch ( Error e ) {
                    log.error ( "End event threw error", e );
                    throw e;
                }
            }
        }
        int slash = match.lastIndexOf ( '/' );
        if ( slash >= 0 ) {
            match = match.substring ( 0, slash );
        } else {
            match = "";
        }
    }
    @Override
    public void endPrefixMapping ( String prefix ) throws SAXException {
        if ( saxLog.isDebugEnabled() ) {
            saxLog.debug ( "endPrefixMapping(" + prefix + ")" );
        }
        ArrayStack<String> stack = namespaces.get ( prefix );
        if ( stack == null ) {
            return;
        }
        try {
            stack.pop();
            if ( stack.empty() ) {
                namespaces.remove ( prefix );
            }
        } catch ( EmptyStackException e ) {
            throw createSAXException ( "endPrefixMapping popped too many times" );
        }
    }
    @Override
    public void ignorableWhitespace ( char buffer[], int start, int len ) throws SAXException {
        if ( saxLog.isDebugEnabled() ) {
            saxLog.debug ( "ignorableWhitespace(" + new String ( buffer, start, len ) + ")" );
        }
    }
    @Override
    public void processingInstruction ( String target, String data ) throws SAXException {
        if ( saxLog.isDebugEnabled() ) {
            saxLog.debug ( "processingInstruction('" + target + "','" + data + "')" );
        }
    }
    public Locator getDocumentLocator() {
        return locator;
    }
    @Override
    public void setDocumentLocator ( Locator locator ) {
        if ( saxLog.isDebugEnabled() ) {
            saxLog.debug ( "setDocumentLocator(" + locator + ")" );
        }
        this.locator = locator;
    }
    @Override
    public void skippedEntity ( String name ) throws SAXException {
        if ( saxLog.isDebugEnabled() ) {
            saxLog.debug ( "skippedEntity(" + name + ")" );
        }
    }
    @Override
    public void startDocument() throws SAXException {
        if ( saxLog.isDebugEnabled() ) {
            saxLog.debug ( "startDocument()" );
        }
        if ( locator instanceof Locator2 && root instanceof DocumentProperties.Encoding ) {
            ( ( DocumentProperties.Encoding ) root ).setEncoding ( ( ( Locator2 ) locator ).getEncoding() );
        }
        configure();
    }
    @Override
    public void startElement ( String namespaceURI, String localName, String qName, Attributes list )
    throws SAXException {
        boolean debug = log.isDebugEnabled();
        if ( saxLog.isDebugEnabled() ) {
            saxLog.debug ( "startElement(" + namespaceURI + "," + localName + "," + qName + ")" );
        }
        list = updateAttributes ( list );
        bodyTexts.push ( bodyText );
        bodyText = new StringBuilder();
        String name = localName;
        if ( ( name == null ) || ( name.length() < 1 ) ) {
            name = qName;
        }
        StringBuilder sb = new StringBuilder ( match );
        if ( match.length() > 0 ) {
            sb.append ( '/' );
        }
        sb.append ( name );
        match = sb.toString();
        if ( debug ) {
            log.debug ( "  New match='" + match + "'" );
        }
        List<Rule> rules = getRules().match ( namespaceURI, match );
        matches.push ( rules );
        if ( ( rules != null ) && ( rules.size() > 0 ) ) {
            for ( int i = 0; i < rules.size(); i++ ) {
                try {
                    Rule rule = rules.get ( i );
                    if ( debug ) {
                        log.debug ( "  Fire begin() for " + rule );
                    }
                    rule.begin ( namespaceURI, name, list );
                } catch ( Exception e ) {
                    log.error ( "Begin event threw exception", e );
                    throw createSAXException ( e );
                } catch ( Error e ) {
                    log.error ( "Begin event threw error", e );
                    throw e;
                }
            }
        } else {
            if ( debug ) {
                log.debug ( "  No rules found matching '" + match + "'." );
            }
        }
    }
    @Override
    public void startPrefixMapping ( String prefix, String namespaceURI ) throws SAXException {
        if ( saxLog.isDebugEnabled() ) {
            saxLog.debug ( "startPrefixMapping(" + prefix + "," + namespaceURI + ")" );
        }
        ArrayStack<String> stack = namespaces.get ( prefix );
        if ( stack == null ) {
            stack = new ArrayStack<>();
            namespaces.put ( prefix, stack );
        }
        stack.push ( namespaceURI );
    }
    @Override
    public void notationDecl ( String name, String publicId, String systemId ) {
        if ( saxLog.isDebugEnabled() ) {
            saxLog.debug ( "notationDecl(" + name + "," + publicId + "," + systemId + ")" );
        }
    }
    @Override
    public void unparsedEntityDecl ( String name, String publicId, String systemId, String notation ) {
        if ( saxLog.isDebugEnabled() ) {
            saxLog.debug ( "unparsedEntityDecl(" + name + "," + publicId + "," + systemId + ","
                           + notation + ")" );
        }
    }
    public void setEntityResolver ( EntityResolver entityResolver ) {
        this.entityResolver = entityResolver;
    }
    public EntityResolver getEntityResolver() {
        return entityResolver;
    }
    @Override
    public InputSource resolveEntity ( String name, String publicId, String baseURI, String systemId )
    throws SAXException, IOException {
        if ( saxLog.isDebugEnabled() ) {
            saxLog.debug (
                "resolveEntity('" + publicId + "', '" + systemId + "', '" + baseURI + "')" );
        }
        String entityURL = null;
        if ( publicId != null ) {
            entityURL = entityValidator.get ( publicId );
        }
        if ( entityURL == null ) {
            if ( systemId == null ) {
                if ( log.isDebugEnabled() ) {
                    log.debug ( " Cannot resolve entity: '" + publicId + "'" );
                }
                return ( null );
            } else {
                if ( log.isDebugEnabled() ) {
                    log.debug ( " Trying to resolve using system ID '" + systemId + "'" );
                }
                entityURL = systemId;
                if ( baseURI != null ) {
                    try {
                        URI uri = new URI ( systemId );
                        if ( !uri.isAbsolute() ) {
                            entityURL = new URI ( baseURI ).resolve ( uri ).toString();
                        }
                    } catch ( URISyntaxException e ) {
                        if ( log.isDebugEnabled() ) {
                            log.debug ( "Invalid URI '" + baseURI + "' or '" + systemId + "'" );
                        }
                    }
                }
            }
        }
        if ( log.isDebugEnabled() ) {
            log.debug ( " Resolving to alternate DTD '" + entityURL + "'" );
        }
        try {
            return ( new InputSource ( entityURL ) );
        } catch ( Exception e ) {
            throw createSAXException ( e );
        }
    }
    @Override
    public void startDTD ( String name, String publicId, String systemId ) throws SAXException {
        setPublicId ( publicId );
    }
    @Override
    public void error ( SAXParseException exception ) throws SAXException {
        log.error ( "Parse Error at line " + exception.getLineNumber() + " column "
                    + exception.getColumnNumber() + ": " + exception.getMessage(), exception );
        if ( errorHandler != null ) {
            errorHandler.error ( exception );
        }
    }
    @Override
    public void fatalError ( SAXParseException exception ) throws SAXException {
        log.error ( "Parse Fatal Error at line " + exception.getLineNumber() + " column "
                    + exception.getColumnNumber() + ": " + exception.getMessage(), exception );
        if ( errorHandler != null ) {
            errorHandler.fatalError ( exception );
        }
    }
    @Override
    public void warning ( SAXParseException exception ) throws SAXException {
        if ( errorHandler != null ) {
            log.warn (
                "Parse Warning Error at line " + exception.getLineNumber() + " column "
                + exception.getColumnNumber() + ": " + exception.getMessage(),
                exception );
            errorHandler.warning ( exception );
        }
    }
    public Object parse ( File file ) throws IOException, SAXException {
        configure();
        InputSource input = new InputSource ( new FileInputStream ( file ) );
        input.setSystemId ( "file://" + file.getAbsolutePath() );
        getXMLReader().parse ( input );
        return ( root );
    }
    public Object parse ( InputSource input ) throws IOException, SAXException {
        configure();
        getXMLReader().parse ( input );
        return ( root );
    }
    public Object parse ( InputStream input ) throws IOException, SAXException {
        configure();
        InputSource is = new InputSource ( input );
        getXMLReader().parse ( is );
        return ( root );
    }
    public void register ( String publicId, String entityURL ) {
        if ( log.isDebugEnabled() ) {
            log.debug ( "register('" + publicId + "', '" + entityURL + "'" );
        }
        entityValidator.put ( publicId, entityURL );
    }
    public void addRule ( String pattern, Rule rule ) {
        rule.setDigester ( this );
        getRules().add ( pattern, rule );
    }
    public void addRuleSet ( RuleSet ruleSet ) {
        String oldNamespaceURI = getRuleNamespaceURI();
        String newNamespaceURI = ruleSet.getNamespaceURI();
        if ( log.isDebugEnabled() ) {
            if ( newNamespaceURI == null ) {
                log.debug ( "addRuleSet() with no namespace URI" );
            } else {
                log.debug ( "addRuleSet() with namespace URI " + newNamespaceURI );
            }
        }
        setRuleNamespaceURI ( newNamespaceURI );
        ruleSet.addRuleInstances ( this );
        setRuleNamespaceURI ( oldNamespaceURI );
    }
    public void addCallMethod ( String pattern, String methodName ) {
        addRule ( pattern, new CallMethodRule ( methodName ) );
    }
    public void addCallMethod ( String pattern, String methodName, int paramCount ) {
        addRule ( pattern, new CallMethodRule ( methodName, paramCount ) );
    }
    public void addCallParam ( String pattern, int paramIndex ) {
        addRule ( pattern, new CallParamRule ( paramIndex ) );
    }
    public void addFactoryCreate ( String pattern, ObjectCreationFactory creationFactory,
                                   boolean ignoreCreateExceptions ) {
        creationFactory.setDigester ( this );
        addRule ( pattern, new FactoryCreateRule ( creationFactory, ignoreCreateExceptions ) );
    }
    public void addObjectCreate ( String pattern, String className ) {
        addRule ( pattern, new ObjectCreateRule ( className ) );
    }
    public void addObjectCreate ( String pattern, String className, String attributeName ) {
        addRule ( pattern, new ObjectCreateRule ( className, attributeName ) );
    }
    public void addSetNext ( String pattern, String methodName, String paramType ) {
        addRule ( pattern, new SetNextRule ( methodName, paramType ) );
    }
    public void addSetProperties ( String pattern ) {
        addRule ( pattern, new SetPropertiesRule() );
    }
    public void clear() {
        match = "";
        bodyTexts.clear();
        params.clear();
        publicId = null;
        stack.clear();
        log = null;
        saxLog = null;
        configured = false;
    }
    public void reset() {
        root = null;
        setErrorHandler ( null );
        clear();
    }
    public Object peek() {
        try {
            return ( stack.peek() );
        } catch ( EmptyStackException e ) {
            log.warn ( "Empty stack (returning null)" );
            return ( null );
        }
    }
    public Object peek ( int n ) {
        try {
            return ( stack.peek ( n ) );
        } catch ( EmptyStackException e ) {
            log.warn ( "Empty stack (returning null)" );
            return ( null );
        }
    }
    public Object pop() {
        try {
            return ( stack.pop() );
        } catch ( EmptyStackException e ) {
            log.warn ( "Empty stack (returning null)" );
            return ( null );
        }
    }
    public void push ( Object object ) {
        if ( stack.size() == 0 ) {
            root = object;
        }
        stack.push ( object );
    }
    public Object getRoot() {
        return root;
    }
    protected void configure() {
        if ( configured ) {
            return;
        }
        log = LogFactory.getLog ( "org.apache.tomcat.util.digester.Digester" );
        saxLog = LogFactory.getLog ( "org.apache.tomcat.util.digester.Digester.sax" );
        configured = true;
    }
    public Object peekParams() {
        try {
            return ( params.peek() );
        } catch ( EmptyStackException e ) {
            log.warn ( "Empty stack (returning null)" );
            return ( null );
        }
    }
    public Object popParams() {
        try {
            if ( log.isTraceEnabled() ) {
                log.trace ( "Popping params" );
            }
            return ( params.pop() );
        } catch ( EmptyStackException e ) {
            log.warn ( "Empty stack (returning null)" );
            return ( null );
        }
    }
    public void pushParams ( Object object ) {
        if ( log.isTraceEnabled() ) {
            log.trace ( "Pushing params" );
        }
        params.push ( object );
    }
    public SAXException createSAXException ( String message, Exception e ) {
        if ( ( e != null ) && ( e instanceof InvocationTargetException ) ) {
            Throwable t = e.getCause();
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
        if ( locator != null ) {
            String error = "Error at (" + locator.getLineNumber() + ", " + locator.getColumnNumber()
                           + ") : " + message;
            if ( e != null ) {
                return new SAXParseException ( error, locator, e );
            } else {
                return new SAXParseException ( error, locator );
            }
        }
        log.error ( "No Locator!" );
        if ( e != null ) {
            return new SAXException ( message, e );
        } else {
            return new SAXException ( message );
        }
    }
    public SAXException createSAXException ( Exception e ) {
        if ( e instanceof InvocationTargetException ) {
            Throwable t = e.getCause();
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
        return createSAXException ( e.getMessage(), e );
    }
    public SAXException createSAXException ( String message ) {
        return createSAXException ( message, null );
    }
    private Attributes updateAttributes ( Attributes list ) {
        if ( list.getLength() == 0 ) {
            return list;
        }
        AttributesImpl newAttrs = new AttributesImpl ( list );
        int nAttributes = newAttrs.getLength();
        for ( int i = 0; i < nAttributes; ++i ) {
            String value = newAttrs.getValue ( i );
            try {
                String newValue = IntrospectionUtils.replaceProperties ( value, null, source );
                if ( value != newValue ) {
                    newAttrs.setValue ( i, newValue );
                }
            } catch ( Exception e ) {
            }
        }
        return newAttrs;
    }
    private StringBuilder updateBodyText ( StringBuilder bodyText ) {
        String in = bodyText.toString();
        String out;
        try {
            out = IntrospectionUtils.replaceProperties ( in, null, source );
        } catch ( Exception e ) {
            return bodyText;
        }
        if ( out == in ) {
            return bodyText;
        } else {
            return new StringBuilder ( out );
        }
    }
}
