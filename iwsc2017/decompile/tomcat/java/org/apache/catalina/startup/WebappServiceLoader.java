package org.apache.catalina.startup;
import java.util.ArrayList;
import java.io.InputStream;
import java.io.Reader;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.io.IOException;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.Collections;
import java.util.Collection;
import java.io.FileNotFoundException;
import org.apache.tomcat.util.scan.JarFactory;
import java.net.URL;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.regex.Pattern;
import javax.servlet.ServletContext;
import org.apache.catalina.Context;
public class WebappServiceLoader<T> {
    private static final String LIB = "/WEB-INF/lib/";
    private static final String SERVICES = "META-INF/services/";
    private final Context context;
    private final ServletContext servletContext;
    private final Pattern containerSciFilterPattern;
    public WebappServiceLoader ( final Context context ) {
        this.context = context;
        this.servletContext = context.getServletContext();
        final String containerSciFilter = context.getContainerSciFilter();
        if ( containerSciFilter != null && containerSciFilter.length() > 0 ) {
            this.containerSciFilterPattern = Pattern.compile ( containerSciFilter );
        } else {
            this.containerSciFilterPattern = null;
        }
    }
    public List<T> load ( final Class<T> serviceType ) throws IOException {
        final String configFile = "META-INF/services/" + serviceType.getName();
        final LinkedHashSet<String> applicationServicesFound = new LinkedHashSet<String>();
        final LinkedHashSet<String> containerServicesFound = new LinkedHashSet<String>();
        ClassLoader loader = this.servletContext.getClassLoader();
        final List<String> orderedLibs = ( List<String> ) this.servletContext.getAttribute ( "javax.servlet.context.orderedLibs" );
        if ( orderedLibs != null ) {
            for ( final String lib : orderedLibs ) {
                final URL jarUrl = this.servletContext.getResource ( "/WEB-INF/lib/" + lib );
                if ( jarUrl == null ) {
                    continue;
                }
                final String base = jarUrl.toExternalForm();
                URL url;
                if ( base.endsWith ( "/" ) ) {
                    url = new URL ( base + configFile );
                } else {
                    url = JarFactory.getJarEntryURL ( jarUrl, configFile );
                }
                try {
                    this.parseConfigFile ( applicationServicesFound, url );
                } catch ( FileNotFoundException ex ) {}
            }
            loader = this.context.getParentClassLoader();
        }
        Enumeration<URL> resources;
        if ( loader == null ) {
            resources = ClassLoader.getSystemResources ( configFile );
        } else {
            resources = loader.getResources ( configFile );
        }
        while ( resources.hasMoreElements() ) {
            this.parseConfigFile ( containerServicesFound, resources.nextElement() );
        }
        if ( this.containerSciFilterPattern != null ) {
            final Iterator<String> iter = containerServicesFound.iterator();
            while ( iter.hasNext() ) {
                if ( this.containerSciFilterPattern.matcher ( iter.next() ).find() ) {
                    iter.remove();
                }
            }
        }
        containerServicesFound.addAll ( ( Collection<?> ) applicationServicesFound );
        if ( containerServicesFound.isEmpty() ) {
            return Collections.emptyList();
        }
        return this.loadServices ( serviceType, containerServicesFound );
    }
    void parseConfigFile ( final LinkedHashSet<String> servicesFound, final URL url ) throws IOException {
        try ( final InputStream is = url.openStream();
                    final InputStreamReader in = new InputStreamReader ( is, StandardCharsets.UTF_8 );
                    final BufferedReader reader = new BufferedReader ( in ) ) {
            String line;
            while ( ( line = reader.readLine() ) != null ) {
                final int i = line.indexOf ( 35 );
                if ( i >= 0 ) {
                    line = line.substring ( 0, i );
                }
                line = line.trim();
                if ( line.length() == 0 ) {
                    continue;
                }
                servicesFound.add ( line );
            }
        }
    }
    List<T> loadServices ( final Class<T> serviceType, final LinkedHashSet<String> servicesFound ) throws IOException {
        final ClassLoader loader = this.servletContext.getClassLoader();
        final List<T> services = new ArrayList<T> ( servicesFound.size() );
        for ( final String serviceClass : servicesFound ) {
            try {
                final Class<?> clazz = Class.forName ( serviceClass, true, loader );
                services.add ( serviceType.cast ( clazz.newInstance() ) );
            } catch ( ClassNotFoundException | InstantiationException | IllegalAccessException | ClassCastException e ) {
                throw new IOException ( e );
            }
        }
        return Collections.unmodifiableList ( ( List<? extends T> ) services );
    }
}
