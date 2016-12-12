package org.apache.catalina.authenticator.jaspic;
import java.util.HashMap;
import java.util.ArrayList;
import java.util.List;
import org.apache.juli.logging.LogFactory;
import java.util.Iterator;
import java.io.Writer;
import java.io.OutputStream;
import java.util.Map;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.io.FileOutputStream;
import java.io.InputStream;
import org.xml.sax.SAXException;
import java.io.IOException;
import org.apache.tomcat.util.digester.Digester;
import java.io.FileInputStream;
import java.io.File;
import org.apache.tomcat.util.res.StringManager;
import org.apache.juli.logging.Log;
final class PersistentProviderRegistrations {
    private static final Log log;
    private static final StringManager sm;
    static Providers loadProviders ( final File configFile ) {
        try ( final InputStream is = new FileInputStream ( configFile ) ) {
            final Digester digester = new Digester();
            try {
                digester.setFeature ( "http://apache.org/xml/features/allow-java-encodings", true );
                digester.setValidating ( true );
                digester.setNamespaceAware ( true );
            } catch ( Exception e ) {
                throw new SecurityException ( e );
            }
            final Providers result = new Providers();
            digester.push ( result );
            digester.addObjectCreate ( "jaspic-providers/provider", Provider.class.getName() );
            digester.addSetProperties ( "jaspic-providers/provider" );
            digester.addSetNext ( "jaspic-providers/provider", "addProvider", Provider.class.getName() );
            digester.addObjectCreate ( "jaspic-providers/provider/property", Property.class.getName() );
            digester.addSetProperties ( "jaspic-providers/provider/property" );
            digester.addSetNext ( "jaspic-providers/provider/property", "addProperty", Property.class.getName() );
            digester.parse ( is );
            return result;
        } catch ( IOException | SAXException e2 ) {
            throw new SecurityException ( e2 );
        }
    }
    static void writeProviders ( final Providers providers, final File configFile ) {
        final File configFileOld = new File ( configFile.getAbsolutePath() + ".old" );
        final File configFileNew = new File ( configFile.getAbsolutePath() + ".new" );
        if ( configFileOld.exists() && configFileOld.delete() ) {
            throw new SecurityException ( PersistentProviderRegistrations.sm.getString ( "persistentProviderRegistrations.existsDeleteFail", configFileOld.getAbsolutePath() ) );
        }
        if ( configFileNew.exists() && configFileNew.delete() ) {
            throw new SecurityException ( PersistentProviderRegistrations.sm.getString ( "persistentProviderRegistrations.existsDeleteFail", configFileNew.getAbsolutePath() ) );
        }
        try ( final OutputStream fos = new FileOutputStream ( configFileNew );
                    final Writer writer = new OutputStreamWriter ( fos, StandardCharsets.UTF_8 ) ) {
            writer.write ( "<?xml version='1.0' encoding='utf-8'?>\n<jaspic-providers\n    xmlns=\"http://tomcat.apache.org/xml\"\n    xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"\n    xsi:schemaLocation=\"http://tomcat.apache.org/xml jaspic-providers.xsd\"\n    version=\"1.0\">\n" );
            for ( final Provider provider : providers.providers ) {
                writer.write ( "  <provider className=\"" );
                writer.write ( provider.getClassName() );
                writer.write ( "\" layer=\"" );
                writer.write ( provider.getLayer() );
                writer.write ( "\" appContext=\"" );
                writer.write ( provider.getAppContext() );
                if ( provider.getDescription() != null ) {
                    writer.write ( "\" description=\"" );
                    writer.write ( provider.getDescription() );
                }
                writer.write ( "\">\n" );
                for ( final Map.Entry<String, String> entry : provider.getProperties().entrySet() ) {
                    writer.write ( "    <property name=\"" );
                    writer.write ( entry.getKey() );
                    writer.write ( "\" value=\"" );
                    writer.write ( entry.getValue() );
                    writer.write ( "\"/>\n" );
                }
                writer.write ( "  </provider>\n" );
            }
            writer.write ( "</jaspic-providers>\n" );
        } catch ( IOException e ) {
            configFileNew.delete();
            throw new SecurityException ( e );
        }
        if ( configFile.isFile() && !configFile.renameTo ( configFileOld ) ) {
            throw new SecurityException ( PersistentProviderRegistrations.sm.getString ( "persistentProviderRegistrations.moveFail", configFile.getAbsolutePath(), configFileOld.getAbsolutePath() ) );
        }
        if ( !configFileNew.renameTo ( configFile ) ) {
            throw new SecurityException ( PersistentProviderRegistrations.sm.getString ( "persistentProviderRegistrations.moveFail", configFileNew.getAbsolutePath(), configFile.getAbsolutePath() ) );
        }
        if ( configFileOld.exists() && !configFileOld.delete() ) {
            PersistentProviderRegistrations.log.warn ( PersistentProviderRegistrations.sm.getString ( "persistentProviderRegistrations.deleteFail", configFileOld.getAbsolutePath() ) );
        }
    }
    static {
        log = LogFactory.getLog ( PersistentProviderRegistrations.class );
        sm = StringManager.getManager ( PersistentProviderRegistrations.class );
    }
    public static class Providers {
        private final List<Provider> providers;
        public Providers() {
            this.providers = new ArrayList<Provider>();
        }
        public void addProvider ( final Provider provider ) {
            this.providers.add ( provider );
        }
        public List<Provider> getProviders() {
            return this.providers;
        }
    }
    public static class Provider {
        private String className;
        private String layer;
        private String appContext;
        private String description;
        private final Map<String, String> properties;
        public Provider() {
            this.properties = new HashMap<String, String>();
        }
        public String getClassName() {
            return this.className;
        }
        public void setClassName ( final String className ) {
            this.className = className;
        }
        public String getLayer() {
            return this.layer;
        }
        public void setLayer ( final String layer ) {
            this.layer = layer;
        }
        public String getAppContext() {
            return this.appContext;
        }
        public void setAppContext ( final String appContext ) {
            this.appContext = appContext;
        }
        public String getDescription() {
            return this.description;
        }
        public void setDescription ( final String description ) {
            this.description = description;
        }
        public void addProperty ( final Property property ) {
            this.properties.put ( property.getName(), property.getValue() );
        }
        void addProperty ( final String name, final String value ) {
            this.properties.put ( name, value );
        }
        public Map<String, String> getProperties() {
            return this.properties;
        }
    }
    public static class Property {
        private String name;
        private String value;
        public String getName() {
            return this.name;
        }
        public void setName ( final String name ) {
            this.name = name;
        }
        public String getValue() {
            return this.value;
        }
        public void setValue ( final String value ) {
            this.value = value;
        }
    }
}
