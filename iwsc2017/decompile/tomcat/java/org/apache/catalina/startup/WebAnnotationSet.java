package org.apache.catalina.startup;
import org.apache.tomcat.util.descriptor.web.ContextResourceEnvRef;
import org.apache.tomcat.util.descriptor.web.MessageDestinationRef;
import org.apache.tomcat.util.descriptor.web.ContextResource;
import org.apache.tomcat.util.descriptor.web.ContextService;
import org.apache.tomcat.util.descriptor.web.ContextEnvironment;
import java.lang.reflect.Method;
import java.lang.reflect.Field;
import javax.annotation.security.DeclareRoles;
import javax.annotation.Resources;
import javax.annotation.Resource;
import org.apache.catalina.Container;
import javax.annotation.security.RunAs;
import org.apache.catalina.Wrapper;
import org.apache.tomcat.util.descriptor.web.FilterDef;
import org.apache.catalina.util.Introspection;
import org.apache.catalina.Context;
import org.apache.tomcat.util.res.StringManager;
public class WebAnnotationSet {
    private static final String SEPARATOR = "/";
    protected static final StringManager sm;
    public static void loadApplicationAnnotations ( final Context context ) {
        loadApplicationListenerAnnotations ( context );
        loadApplicationFilterAnnotations ( context );
        loadApplicationServletAnnotations ( context );
    }
    protected static void loadApplicationListenerAnnotations ( final Context context ) {
        final String[] applicationListeners2;
        final String[] applicationListeners = applicationListeners2 = context.findApplicationListeners();
        for ( final String className : applicationListeners2 ) {
            final Class<?> classClass = Introspection.loadClass ( context, className );
            if ( classClass != null ) {
                loadClassAnnotation ( context, classClass );
                loadFieldsAnnotation ( context, classClass );
                loadMethodsAnnotation ( context, classClass );
            }
        }
    }
    protected static void loadApplicationFilterAnnotations ( final Context context ) {
        final FilterDef[] filterDefs2;
        final FilterDef[] filterDefs = filterDefs2 = context.findFilterDefs();
        for ( final FilterDef filterDef : filterDefs2 ) {
            final Class<?> classClass = Introspection.loadClass ( context, filterDef.getFilterClass() );
            if ( classClass != null ) {
                loadClassAnnotation ( context, classClass );
                loadFieldsAnnotation ( context, classClass );
                loadMethodsAnnotation ( context, classClass );
            }
        }
    }
    protected static void loadApplicationServletAnnotations ( final Context context ) {
        final Container[] children2;
        final Container[] children = children2 = context.findChildren();
        for ( final Container child : children2 ) {
            if ( child instanceof Wrapper ) {
                final Wrapper wrapper = ( Wrapper ) child;
                if ( wrapper.getServletClass() != null ) {
                    final Class<?> classClass = Introspection.loadClass ( context, wrapper.getServletClass() );
                    if ( classClass != null ) {
                        loadClassAnnotation ( context, classClass );
                        loadFieldsAnnotation ( context, classClass );
                        loadMethodsAnnotation ( context, classClass );
                        final RunAs annotation = classClass.getAnnotation ( RunAs.class );
                        if ( annotation != null ) {
                            wrapper.setRunAs ( annotation.value() );
                        }
                    }
                }
            }
        }
    }
    protected static void loadClassAnnotation ( final Context context, final Class<?> classClass ) {
        final Resource resourceAnnotation = classClass.getAnnotation ( Resource.class );
        if ( resourceAnnotation != null ) {
            addResource ( context, resourceAnnotation );
        }
        final Resources resourcesAnnotation = classClass.getAnnotation ( Resources.class );
        if ( resourcesAnnotation != null && resourcesAnnotation.value() != null ) {
            for ( final Resource resource : resourcesAnnotation.value() ) {
                addResource ( context, resource );
            }
        }
        final DeclareRoles declareRolesAnnotation = classClass.getAnnotation ( DeclareRoles.class );
        if ( declareRolesAnnotation != null && declareRolesAnnotation.value() != null ) {
            for ( final String role : declareRolesAnnotation.value() ) {
                context.addSecurityRole ( role );
            }
        }
    }
    protected static void loadFieldsAnnotation ( final Context context, final Class<?> classClass ) {
        final Field[] fields = Introspection.getDeclaredFields ( classClass );
        if ( fields != null && fields.length > 0 ) {
            for ( final Field field : fields ) {
                final Resource annotation = field.getAnnotation ( Resource.class );
                if ( annotation != null ) {
                    final String defaultName = classClass.getName() + "/" + field.getName();
                    final Class<?> defaultType = field.getType();
                    addResource ( context, annotation, defaultName, defaultType );
                }
            }
        }
    }
    protected static void loadMethodsAnnotation ( final Context context, final Class<?> classClass ) {
        final Method[] methods = Introspection.getDeclaredMethods ( classClass );
        if ( methods != null && methods.length > 0 ) {
            for ( final Method method : methods ) {
                final Resource annotation = method.getAnnotation ( Resource.class );
                if ( annotation != null ) {
                    if ( !Introspection.isValidSetter ( method ) ) {
                        throw new IllegalArgumentException ( WebAnnotationSet.sm.getString ( "webAnnotationSet.invalidInjection" ) );
                    }
                    final String defaultName = classClass.getName() + "/" + Introspection.getPropertyName ( method );
                    final Class<?> defaultType = method.getParameterTypes() [0];
                    addResource ( context, annotation, defaultName, defaultType );
                }
            }
        }
    }
    protected static void addResource ( final Context context, final Resource annotation ) {
        addResource ( context, annotation, null, null );
    }
    protected static void addResource ( final Context context, final Resource annotation, final String defaultName, final Class<?> defaultType ) {
        final String name = getName ( annotation, defaultName );
        final String type = getType ( annotation, defaultType );
        if ( type.equals ( "java.lang.String" ) || type.equals ( "java.lang.Character" ) || type.equals ( "java.lang.Integer" ) || type.equals ( "java.lang.Boolean" ) || type.equals ( "java.lang.Double" ) || type.equals ( "java.lang.Byte" ) || type.equals ( "java.lang.Short" ) || type.equals ( "java.lang.Long" ) || type.equals ( "java.lang.Float" ) ) {
            final ContextEnvironment resource = new ContextEnvironment();
            resource.setName ( name );
            resource.setType ( type );
            resource.setDescription ( annotation.description() );
            resource.setValue ( annotation.mappedName() );
            context.getNamingResources().addEnvironment ( resource );
        } else if ( type.equals ( "javax.xml.rpc.Service" ) ) {
            final ContextService service = new ContextService();
            service.setName ( name );
            service.setWsdlfile ( annotation.mappedName() );
            service.setType ( type );
            service.setDescription ( annotation.description() );
            context.getNamingResources().addService ( service );
        } else if ( type.equals ( "javax.sql.DataSource" ) || type.equals ( "javax.jms.ConnectionFactory" ) || type.equals ( "javax.jms.QueueConnectionFactory" ) || type.equals ( "javax.jms.TopicConnectionFactory" ) || type.equals ( "javax.mail.Session" ) || type.equals ( "java.net.URL" ) || type.equals ( "javax.resource.cci.ConnectionFactory" ) || type.equals ( "org.omg.CORBA_2_3.ORB" ) || type.endsWith ( "ConnectionFactory" ) ) {
            final ContextResource resource2 = new ContextResource();
            resource2.setName ( name );
            resource2.setType ( type );
            if ( annotation.authenticationType() == Resource.AuthenticationType.CONTAINER ) {
                resource2.setAuth ( "Container" );
            } else if ( annotation.authenticationType() == Resource.AuthenticationType.APPLICATION ) {
                resource2.setAuth ( "Application" );
            }
            resource2.setScope ( annotation.shareable() ? "Shareable" : "Unshareable" );
            resource2.setProperty ( "mappedName", annotation.mappedName() );
            resource2.setDescription ( annotation.description() );
            context.getNamingResources().addResource ( resource2 );
        } else if ( type.equals ( "javax.jms.Queue" ) || type.equals ( "javax.jms.Topic" ) ) {
            final MessageDestinationRef resource3 = new MessageDestinationRef();
            resource3.setName ( name );
            resource3.setType ( type );
            resource3.setUsage ( annotation.mappedName() );
            resource3.setDescription ( annotation.description() );
            context.getNamingResources().addMessageDestinationRef ( resource3 );
        } else {
            if ( !type.equals ( "javax.resource.cci.InteractionSpec" ) && type.equals ( "javax.transaction.UserTransaction" ) ) {}
            final ContextResourceEnvRef resource4 = new ContextResourceEnvRef();
            resource4.setName ( name );
            resource4.setType ( type );
            resource4.setProperty ( "mappedName", annotation.mappedName() );
            resource4.setDescription ( annotation.description() );
            context.getNamingResources().addResourceEnvRef ( resource4 );
        }
    }
    private static String getType ( final Resource annotation, final Class<?> defaultType ) {
        Class<?> type = annotation.type();
        if ( ( type == null || type.equals ( Object.class ) ) && defaultType != null ) {
            type = defaultType;
        }
        return Introspection.convertPrimitiveType ( type ).getCanonicalName();
    }
    private static String getName ( final Resource annotation, final String defaultName ) {
        String name = annotation.name();
        if ( ( name == null || name.equals ( "" ) ) && defaultName != null ) {
            name = defaultName;
        }
        return name;
    }
    static {
        sm = StringManager.getManager ( "org.apache.catalina.startup" );
    }
}
