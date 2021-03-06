package org.apache.catalina.startup;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import javax.annotation.Resource;
import javax.annotation.Resources;
import javax.annotation.security.DeclareRoles;
import javax.annotation.security.RunAs;
import org.apache.catalina.Container;
import org.apache.catalina.Context;
import org.apache.catalina.Wrapper;
import org.apache.catalina.util.Introspection;
import org.apache.tomcat.util.descriptor.web.ContextEnvironment;
import org.apache.tomcat.util.descriptor.web.ContextResource;
import org.apache.tomcat.util.descriptor.web.ContextResourceEnvRef;
import org.apache.tomcat.util.descriptor.web.ContextService;
import org.apache.tomcat.util.descriptor.web.FilterDef;
import org.apache.tomcat.util.descriptor.web.MessageDestinationRef;
import org.apache.tomcat.util.res.StringManager;
public class WebAnnotationSet {
    private static final String SEPARATOR = "/";
    protected static final StringManager sm =
        StringManager.getManager ( Constants.Package );
    public static void loadApplicationAnnotations ( Context context ) {
        loadApplicationListenerAnnotations ( context );
        loadApplicationFilterAnnotations ( context );
        loadApplicationServletAnnotations ( context );
    }
    protected static void loadApplicationListenerAnnotations ( Context context ) {
        String[] applicationListeners = context.findApplicationListeners();
        for ( String className : applicationListeners ) {
            Class<?> classClass = Introspection.loadClass ( context, className );
            if ( classClass == null ) {
                continue;
            }
            loadClassAnnotation ( context, classClass );
            loadFieldsAnnotation ( context, classClass );
            loadMethodsAnnotation ( context, classClass );
        }
    }
    protected static void loadApplicationFilterAnnotations ( Context context ) {
        FilterDef[] filterDefs = context.findFilterDefs();
        for ( FilterDef filterDef : filterDefs ) {
            Class<?> classClass = Introspection.loadClass ( context,
                                  filterDef.getFilterClass() );
            if ( classClass == null ) {
                continue;
            }
            loadClassAnnotation ( context, classClass );
            loadFieldsAnnotation ( context, classClass );
            loadMethodsAnnotation ( context, classClass );
        }
    }
    protected static void loadApplicationServletAnnotations ( Context context ) {
        Container[] children = context.findChildren();
        for ( Container child : children ) {
            if ( child instanceof Wrapper ) {
                Wrapper wrapper = ( Wrapper ) child;
                if ( wrapper.getServletClass() == null ) {
                    continue;
                }
                Class<?> classClass = Introspection.loadClass ( context,
                                      wrapper.getServletClass() );
                if ( classClass == null ) {
                    continue;
                }
                loadClassAnnotation ( context, classClass );
                loadFieldsAnnotation ( context, classClass );
                loadMethodsAnnotation ( context, classClass );
                RunAs annotation = classClass.getAnnotation ( RunAs.class );
                if ( annotation != null ) {
                    wrapper.setRunAs ( annotation.value() );
                }
            }
        }
    }
    protected static void loadClassAnnotation ( Context context,
            Class<?> classClass ) {
        Resource resourceAnnotation = classClass.getAnnotation ( Resource.class );
        if ( resourceAnnotation != null ) {
            addResource ( context, resourceAnnotation );
        }
        Resources resourcesAnnotation = classClass.getAnnotation ( Resources.class );
        if ( resourcesAnnotation != null && resourcesAnnotation.value() != null ) {
            for ( Resource resource : resourcesAnnotation.value() ) {
                addResource ( context, resource );
            }
        }
        DeclareRoles declareRolesAnnotation = classClass
                                              .getAnnotation ( DeclareRoles.class );
        if ( declareRolesAnnotation != null && declareRolesAnnotation.value() != null ) {
            for ( String role : declareRolesAnnotation.value() ) {
                context.addSecurityRole ( role );
            }
        }
    }
    protected static void loadFieldsAnnotation ( Context context,
            Class<?> classClass ) {
        Field[] fields = Introspection.getDeclaredFields ( classClass );
        if ( fields != null && fields.length > 0 ) {
            for ( Field field : fields ) {
                Resource annotation = field.getAnnotation ( Resource.class );
                if ( annotation != null ) {
                    String defaultName = classClass.getName() + SEPARATOR + field.getName();
                    Class<?> defaultType = field.getType();
                    addResource ( context, annotation, defaultName, defaultType );
                }
            }
        }
    }
    protected static void loadMethodsAnnotation ( Context context,
            Class<?> classClass ) {
        Method[] methods = Introspection.getDeclaredMethods ( classClass );
        if ( methods != null && methods.length > 0 ) {
            for ( Method method : methods ) {
                Resource annotation = method.getAnnotation ( Resource.class );
                if ( annotation != null ) {
                    if ( !Introspection.isValidSetter ( method ) ) {
                        throw new IllegalArgumentException ( sm.getString (
                                "webAnnotationSet.invalidInjection" ) );
                    }
                    String defaultName = classClass.getName() + SEPARATOR +
                                         Introspection.getPropertyName ( method );
                    Class<?> defaultType =
                        ( method.getParameterTypes() [0] );
                    addResource ( context, annotation, defaultName, defaultType );
                }
            }
        }
    }
    protected static void addResource ( Context context, Resource annotation ) {
        addResource ( context, annotation, null, null );
    }
    protected static void addResource ( Context context, Resource annotation,
                                        String defaultName, Class<?> defaultType ) {
        String name = getName ( annotation, defaultName );
        String type = getType ( annotation, defaultType );
        if ( type.equals ( "java.lang.String" ) ||
                type.equals ( "java.lang.Character" ) ||
                type.equals ( "java.lang.Integer" ) ||
                type.equals ( "java.lang.Boolean" ) ||
                type.equals ( "java.lang.Double" ) ||
                type.equals ( "java.lang.Byte" ) ||
                type.equals ( "java.lang.Short" ) ||
                type.equals ( "java.lang.Long" ) ||
                type.equals ( "java.lang.Float" ) ) {
            ContextEnvironment resource = new ContextEnvironment();
            resource.setName ( name );
            resource.setType ( type );
            resource.setDescription ( annotation.description() );
            resource.setValue ( annotation.mappedName() );
            context.getNamingResources().addEnvironment ( resource );
        } else if ( type.equals ( "javax.xml.rpc.Service" ) ) {
            ContextService service = new ContextService();
            service.setName ( name );
            service.setWsdlfile ( annotation.mappedName() );
            service.setType ( type );
            service.setDescription ( annotation.description() );
            context.getNamingResources().addService ( service );
        } else if ( type.equals ( "javax.sql.DataSource" ) ||
                    type.equals ( "javax.jms.ConnectionFactory" ) ||
                    type.equals ( "javax.jms.QueueConnectionFactory" ) ||
                    type.equals ( "javax.jms.TopicConnectionFactory" ) ||
                    type.equals ( "javax.mail.Session" ) ||
                    type.equals ( "java.net.URL" ) ||
                    type.equals ( "javax.resource.cci.ConnectionFactory" ) ||
                    type.equals ( "org.omg.CORBA_2_3.ORB" ) ||
                    type.endsWith ( "ConnectionFactory" ) ) {
            ContextResource resource = new ContextResource();
            resource.setName ( name );
            resource.setType ( type );
            if ( annotation.authenticationType()
                    == Resource.AuthenticationType.CONTAINER ) {
                resource.setAuth ( "Container" );
            } else if ( annotation.authenticationType()
                        == Resource.AuthenticationType.APPLICATION ) {
                resource.setAuth ( "Application" );
            }
            resource.setScope ( annotation.shareable() ? "Shareable" : "Unshareable" );
            resource.setProperty ( "mappedName", annotation.mappedName() );
            resource.setDescription ( annotation.description() );
            context.getNamingResources().addResource ( resource );
        } else if ( type.equals ( "javax.jms.Queue" ) ||
                    type.equals ( "javax.jms.Topic" ) ) {
            MessageDestinationRef resource = new MessageDestinationRef();
            resource.setName ( name );
            resource.setType ( type );
            resource.setUsage ( annotation.mappedName() );
            resource.setDescription ( annotation.description() );
            context.getNamingResources().addMessageDestinationRef ( resource );
        } else if ( type.equals ( "javax.resource.cci.InteractionSpec" ) ||
                    type.equals ( "javax.transaction.UserTransaction" ) ||
                    true ) {
            ContextResourceEnvRef resource = new ContextResourceEnvRef();
            resource.setName ( name );
            resource.setType ( type );
            resource.setProperty ( "mappedName", annotation.mappedName() );
            resource.setDescription ( annotation.description() );
            context.getNamingResources().addResourceEnvRef ( resource );
        }
    }
    private static String getType ( Resource annotation, Class<?> defaultType ) {
        Class<?> type = annotation.type();
        if ( type == null || type.equals ( Object.class ) ) {
            if ( defaultType != null ) {
                type = defaultType;
            }
        }
        return Introspection.convertPrimitiveType ( type ).getCanonicalName();
    }
    private static String getName ( Resource annotation, String defaultName ) {
        String name = annotation.name();
        if ( name == null || name.equals ( "" ) ) {
            if ( defaultName != null ) {
                name = defaultName;
            }
        }
        return name;
    }
}
