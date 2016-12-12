package org.apache.catalina.manager;
import org.apache.catalina.manager.util.SessionUtils;
import java.util.Date;
import org.apache.catalina.manager.util.BaseSessionComparator;
import javax.servlet.http.HttpSession;
import java.util.Comparator;
import java.util.Collections;
import java.util.Iterator;
import java.util.Set;
import java.util.Collection;
import java.util.ArrayList;
import org.apache.catalina.Session;
import java.util.List;
import javax.servlet.ServletResponse;
import javax.servlet.ServletRequest;
import org.apache.catalina.Manager;
import org.apache.catalina.Container;
import java.net.UnknownHostException;
import java.net.InetAddress;
import org.apache.catalina.util.ServerInfo;
import org.apache.catalina.DistributedManager;
import org.apache.catalina.Context;
import java.util.Arrays;
import org.apache.catalina.util.RequestUtil;
import java.text.MessageFormat;
import java.io.Writer;
import java.io.PrintWriter;
import java.io.StringWriter;
import javax.servlet.http.Part;
import java.io.File;
import javax.servlet.ServletException;
import java.io.IOException;
import org.apache.catalina.util.ContextName;
import java.util.Locale;
import java.util.Enumeration;
import org.apache.tomcat.util.res.StringManager;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletRequest;
import org.apache.catalina.util.URLEncoder;
public final class HTMLManagerServlet extends ManagerServlet {
    private static final long serialVersionUID = 1L;
    static final URLEncoder URL_ENCODER;
    static final String APPLICATION_MESSAGE = "message";
    static final String APPLICATION_ERROR = "error";
    static final String sessionsListJspPath = "/WEB-INF/jsp/sessionsList.jsp";
    static final String sessionDetailJspPath = "/WEB-INF/jsp/sessionDetail.jsp";
    static final String connectorCiphersJspPath = "/WEB-INF/jsp/connectorCiphers.jsp";
    private boolean showProxySessions;
    private static final String APPS_HEADER_SECTION = "<table border=\"1\" cellspacing=\"0\" cellpadding=\"3\">\n<tr>\n <td colspan=\"6\" class=\"title\">{0}</td>\n</tr>\n<tr>\n <td class=\"header-left\"><small>{1}</small></td>\n <td class=\"header-left\"><small>{2}</small></td>\n <td class=\"header-center\"><small>{3}</small></td>\n <td class=\"header-center\"><small>{4}</small></td>\n <td class=\"header-left\"><small>{5}</small></td>\n <td class=\"header-left\"><small>{6}</small></td>\n</tr>\n";
    private static final String APPS_ROW_DETAILS_SECTION = "<tr>\n <td class=\"row-left\" bgcolor=\"{6}\" rowspan=\"2\"><small>{0}</small></td>\n <td class=\"row-left\" bgcolor=\"{6}\" rowspan=\"2\"><small>{1}</small></td>\n <td class=\"row-left\" bgcolor=\"{6}\" rowspan=\"2\"><small>{2}</small></td>\n <td class=\"row-center\" bgcolor=\"{6}\" rowspan=\"2\"><small>{3}</small></td>\n <td class=\"row-center\" bgcolor=\"{6}\" rowspan=\"2\"><small><a href=\"{4}\">{5}</a></small></td>\n";
    private static final String MANAGER_APP_ROW_BUTTON_SECTION = " <td class=\"row-left\" bgcolor=\"{13}\">\n  <small>\n  &nbsp;{1}&nbsp;\n  &nbsp;{3}&nbsp;\n  &nbsp;{5}&nbsp;\n  &nbsp;{7}&nbsp;\n  </small>\n </td>\n</tr><tr>\n <td class=\"row-left\" bgcolor=\"{13}\">\n  <form method=\"POST\" action=\"{8}\">\n  <small>\n  &nbsp;<input type=\"submit\" value=\"{9}\">&nbsp;{10}&nbsp;<input type=\"text\" name=\"idle\" size=\"5\" value=\"{11}\">&nbsp;{12}&nbsp;\n  </small>\n  </form>\n </td>\n</tr>\n";
    private static final String STARTED_DEPLOYED_APPS_ROW_BUTTON_SECTION = " <td class=\"row-left\" bgcolor=\"{13}\">\n  &nbsp;<small>{1}</small>&nbsp;\n  <form class=\"inline\" method=\"POST\" action=\"{2}\">  <small><input type=\"submit\" value=\"{3}\"></small>  </form>\n  <form class=\"inline\" method=\"POST\" action=\"{4}\">  <small><input type=\"submit\" value=\"{5}\"></small>  </form>\n  <form class=\"inline\" method=\"POST\" action=\"{6}\">  <small><input type=\"submit\" value=\"{7}\"></small>  </form>\n </td>\n </tr><tr>\n <td class=\"row-left\" bgcolor=\"{13}\">\n  <form method=\"POST\" action=\"{8}\">\n  <small>\n  &nbsp;<input type=\"submit\" value=\"{9}\">&nbsp;{10}&nbsp;<input type=\"text\" name=\"idle\" size=\"5\" value=\"{11}\">&nbsp;{12}&nbsp;\n  </small>\n  </form>\n </td>\n</tr>\n";
    private static final String STOPPED_DEPLOYED_APPS_ROW_BUTTON_SECTION = " <td class=\"row-left\" bgcolor=\"{13}\" rowspan=\"2\">\n  <form class=\"inline\" method=\"POST\" action=\"{0}\">  <small><input type=\"submit\" value=\"{1}\"></small>  </form>\n  &nbsp;<small>{3}</small>&nbsp;\n  &nbsp;<small>{5}</small>&nbsp;\n  <form class=\"inline\" method=\"POST\" action=\"{6}\">  <small><input type=\"submit\" value=\"{7}\"></small>  </form>\n </td>\n</tr>\n<tr></tr>\n";
    private static final String STARTED_NONDEPLOYED_APPS_ROW_BUTTON_SECTION = " <td class=\"row-left\" bgcolor=\"{13}\">\n  &nbsp;<small>{1}</small>&nbsp;\n  <form class=\"inline\" method=\"POST\" action=\"{2}\">  <small><input type=\"submit\" value=\"{3}\"></small>  </form>\n  <form class=\"inline\" method=\"POST\" action=\"{4}\">  <small><input type=\"submit\" value=\"{5}\"></small>  </form>\n  &nbsp;<small>{7}</small>&nbsp;\n </td>\n </tr><tr>\n <td class=\"row-left\" bgcolor=\"{13}\">\n  <form method=\"POST\" action=\"{8}\">\n  <small>\n  &nbsp;<input type=\"submit\" value=\"{9}\">&nbsp;{10}&nbsp;<input type=\"text\" name=\"idle\" size=\"5\" value=\"{11}\">&nbsp;{12}&nbsp;\n  </small>\n  </form>\n </td>\n</tr>\n";
    private static final String STOPPED_NONDEPLOYED_APPS_ROW_BUTTON_SECTION = " <td class=\"row-left\" bgcolor=\"{13}\" rowspan=\"2\">\n  <form class=\"inline\" method=\"POST\" action=\"{0}\">  <small><input type=\"submit\" value=\"{1}\"></small>  </form>\n  &nbsp;<small>{3}</small>&nbsp;\n  &nbsp;<small>{5}</small>&nbsp;\n  &nbsp;<small>{7}</small>&nbsp;\n </td>\n</tr>\n<tr></tr>\n";
    private static final String DEPLOY_SECTION = "</table>\n<br>\n<table border=\"1\" cellspacing=\"0\" cellpadding=\"3\">\n<tr>\n <td colspan=\"2\" class=\"title\">{0}</td>\n</tr>\n<tr>\n <td colspan=\"2\" class=\"header-left\"><small>{1}</small></td>\n</tr>\n<tr>\n <td colspan=\"2\">\n<form method=\"post\" action=\"{2}\">\n<table cellspacing=\"0\" cellpadding=\"3\">\n<tr>\n <td class=\"row-right\">\n  <small>{3}</small>\n </td>\n <td class=\"row-left\">\n  <input type=\"text\" name=\"deployPath\" size=\"20\">\n </td>\n</tr>\n<tr>\n <td class=\"row-right\">\n  <small>{4}</small>\n </td>\n <td class=\"row-left\">\n  <input type=\"text\" name=\"deployConfig\" size=\"20\">\n </td>\n</tr>\n<tr>\n <td class=\"row-right\">\n  <small>{5}</small>\n </td>\n <td class=\"row-left\">\n  <input type=\"text\" name=\"deployWar\" size=\"40\">\n </td>\n</tr>\n<tr>\n <td class=\"row-right\">\n  &nbsp;\n </td>\n <td class=\"row-left\">\n  <input type=\"submit\" value=\"{6}\">\n </td>\n</tr>\n</table>\n</form>\n</td>\n</tr>\n";
    private static final String UPLOAD_SECTION = "<tr>\n <td colspan=\"2\" class=\"header-left\"><small>{0}</small></td>\n</tr>\n<tr>\n <td colspan=\"2\">\n<form method=\"post\" action=\"{1}\" enctype=\"multipart/form-data\">\n<table cellspacing=\"0\" cellpadding=\"3\">\n<tr>\n <td class=\"row-right\">\n  <small>{2}</small>\n </td>\n <td class=\"row-left\">\n  <input type=\"file\" name=\"deployWar\" size=\"40\">\n </td>\n</tr>\n<tr>\n <td class=\"row-right\">\n  &nbsp;\n </td>\n <td class=\"row-left\">\n  <input type=\"submit\" value=\"{3}\">\n </td>\n</tr>\n</table>\n</form>\n</td>\n</tr>\n</table>\n<br>\n\n";
    private static final String DIAGNOSTICS_SECTION = "<table border=\"1\" cellspacing=\"0\" cellpadding=\"3\">\n<tr>\n <td colspan=\"2\" class=\"title\">{0}</td>\n</tr>\n<tr>\n <td colspan=\"2\" class=\"header-left\"><small>{1}</small></td>\n</tr>\n<tr>\n <td colspan=\"2\">\n<form method=\"post\" action=\"{2}\">\n<table cellspacing=\"0\" cellpadding=\"3\">\n<tr>\n <td class=\"row-left\">\n  <input type=\"submit\" value=\"{4}\">\n </td>\n <td class=\"row-left\">\n  <small>{3}</small>\n </td>\n</tr>\n</table>\n</form>\n</td>\n</tr>\n<tr>\n <td colspan=\"2\" class=\"header-left\"><small>{5}</small></td>\n</tr>\n<tr>\n <td colspan=\"2\">\n<form method=\"post\" action=\"{6}\">\n<table cellspacing=\"0\" cellpadding=\"3\">\n<tr>\n <td class=\"row-left\">\n  <input type=\"submit\" value=\"{7}\">\n </td>\n <td class=\"row-left\">\n  <small>{8}</small>\n </td>\n</tr>\n</table>\n</form>\n</td>\n</tr>\n</table>\n<br>";
    public HTMLManagerServlet() {
        this.showProxySessions = false;
    }
    @Override
    public void doGet ( final HttpServletRequest request, final HttpServletResponse response ) throws IOException, ServletException {
        final StringManager smClient = StringManager.getManager ( "org.apache.catalina.manager", request.getLocales() );
        final String command = request.getPathInfo();
        final String path = request.getParameter ( "path" );
        ContextName cn = null;
        if ( path != null ) {
            cn = new ContextName ( path, request.getParameter ( "version" ) );
        }
        response.setContentType ( "text/html; charset=utf-8" );
        String message = "";
        Label_0302: {
            if ( command != null ) {
                if ( !command.equals ( "/" ) ) {
                    if ( !command.equals ( "/list" ) ) {
                        if ( command.equals ( "/sessions" ) ) {
                            try {
                                this.doSessions ( cn, request, response, smClient );
                                return;
                            } catch ( Exception e ) {
                                this.log ( "HTMLManagerServlet.sessions[" + cn + "]", ( Throwable ) e );
                                message = smClient.getString ( "managerServlet.exception", e.toString() );
                                break Label_0302;
                            }
                        }
                        if ( command.equals ( "/sslConnectorCiphers" ) ) {
                            this.sslConnectorCiphers ( request, response );
                        } else if ( command.equals ( "/upload" ) || command.equals ( "/deploy" ) || command.equals ( "/reload" ) || command.equals ( "/undeploy" ) || command.equals ( "/expire" ) || command.equals ( "/start" ) || command.equals ( "/stop" ) ) {
                            message = smClient.getString ( "managerServlet.postCommand", command );
                        } else {
                            message = smClient.getString ( "managerServlet.unknownCommand", command );
                        }
                    }
                }
            }
        }
        this.list ( request, response, message, smClient );
    }
    public void doPost ( final HttpServletRequest request, final HttpServletResponse response ) throws IOException, ServletException {
        final StringManager smClient = StringManager.getManager ( "org.apache.catalina.manager", request.getLocales() );
        final String command = request.getPathInfo();
        final String path = request.getParameter ( "path" );
        ContextName cn = null;
        if ( path != null ) {
            cn = new ContextName ( path, request.getParameter ( "version" ) );
        }
        final String deployPath = request.getParameter ( "deployPath" );
        ContextName deployCn = null;
        if ( deployPath != null ) {
            deployCn = new ContextName ( deployPath, request.getParameter ( "deployVersion" ) );
        }
        final String deployConfig = request.getParameter ( "deployConfig" );
        final String deployWar = request.getParameter ( "deployWar" );
        response.setContentType ( "text/html; charset=utf-8" );
        String message = "";
        if ( command != null ) {
            if ( command.length() != 0 ) {
                if ( command.equals ( "/upload" ) ) {
                    message = this.upload ( request, smClient );
                } else if ( command.equals ( "/deploy" ) ) {
                    message = this.deployInternal ( deployConfig, deployCn, deployWar, smClient );
                } else if ( command.equals ( "/reload" ) ) {
                    message = this.reload ( cn, smClient );
                } else if ( command.equals ( "/undeploy" ) ) {
                    message = this.undeploy ( cn, smClient );
                } else if ( command.equals ( "/expire" ) ) {
                    message = this.expireSessions ( cn, request, smClient );
                } else if ( command.equals ( "/start" ) ) {
                    message = this.start ( cn, smClient );
                } else if ( command.equals ( "/stop" ) ) {
                    message = this.stop ( cn, smClient );
                } else {
                    if ( !command.equals ( "/findleaks" ) ) {
                        this.doGet ( request, response );
                        return;
                    }
                    message = this.findleaks ( smClient );
                }
            }
        }
        this.list ( request, response, message, smClient );
    }
    protected String upload ( final HttpServletRequest request, final StringManager smClient ) {
        String message = "";
        try {
            final Part warPart = request.getPart ( "deployWar" );
            if ( warPart == null ) {
                message = smClient.getString ( "htmlManagerServlet.deployUploadNoFile" );
            } else {
                String filename = warPart.getSubmittedFileName();
                if ( !filename.toLowerCase ( Locale.ENGLISH ).endsWith ( ".war" ) ) {
                    message = smClient.getString ( "htmlManagerServlet.deployUploadNotWar", filename );
                } else {
                    if ( filename.lastIndexOf ( 92 ) >= 0 ) {
                        filename = filename.substring ( filename.lastIndexOf ( 92 ) + 1 );
                    }
                    if ( filename.lastIndexOf ( 47 ) >= 0 ) {
                        filename = filename.substring ( filename.lastIndexOf ( 47 ) + 1 );
                    }
                    final File file = new File ( this.host.getAppBaseFile(), filename );
                    if ( file.exists() ) {
                        message = smClient.getString ( "htmlManagerServlet.deployUploadWarExists", filename );
                    } else {
                        final ContextName cn = new ContextName ( filename, true );
                        final String name = cn.getName();
                        if ( this.host.findChild ( name ) != null && !this.isDeployed ( name ) ) {
                            message = smClient.getString ( "htmlManagerServlet.deployUploadInServerXml", filename );
                        } else if ( this.isServiced ( name ) ) {
                            message = smClient.getString ( "managerServlet.inService", name );
                        } else {
                            this.addServiced ( name );
                            try {
                                warPart.write ( file.getAbsolutePath() );
                                this.check ( name );
                            } finally {
                                this.removeServiced ( name );
                            }
                        }
                    }
                }
            }
        } catch ( Exception e ) {
            message = smClient.getString ( "htmlManagerServlet.deployUploadFail", e.getMessage() );
            this.log ( message, ( Throwable ) e );
        }
        return message;
    }
    protected String deployInternal ( final String config, final ContextName cn, final String war, final StringManager smClient ) {
        final StringWriter stringWriter = new StringWriter();
        final PrintWriter printWriter = new PrintWriter ( stringWriter );
        super.deploy ( printWriter, config, cn, war, false, smClient );
        return stringWriter.toString();
    }
    protected void list ( final HttpServletRequest request, final HttpServletResponse response, final String message, final StringManager smClient ) throws IOException {
        if ( this.debug >= 1 ) {
            this.log ( "list: Listing contexts for virtual host '" + this.host.getName() + "'" );
        }
        final PrintWriter writer = response.getWriter();
        writer.print ( Constants.HTML_HEADER_SECTION );
        Object[] args = { request.getContextPath(), smClient.getString ( "htmlManagerServlet.title" ) };
        writer.print ( MessageFormat.format ( Constants.BODY_HEADER_SECTION, args ) );
        args = new Object[] { smClient.getString ( "htmlManagerServlet.messageLabel" ), null, null };
        if ( message == null || message.length() == 0 ) {
            args[1] = "OK";
        } else {
            args[1] = RequestUtil.filter ( message );
        }
        writer.print ( MessageFormat.format ( Constants.MESSAGE_SECTION, args ) );
        args = new Object[] { smClient.getString ( "htmlManagerServlet.manager" ), response.encodeURL ( request.getContextPath() + "/html/list" ), smClient.getString ( "htmlManagerServlet.list" ), response.encodeURL ( request.getContextPath() + "/" + smClient.getString ( "htmlManagerServlet.helpHtmlManagerFile" ) ), smClient.getString ( "htmlManagerServlet.helpHtmlManager" ), response.encodeURL ( request.getContextPath() + "/" + smClient.getString ( "htmlManagerServlet.helpManagerFile" ) ), smClient.getString ( "htmlManagerServlet.helpManager" ), response.encodeURL ( request.getContextPath() + "/status" ), smClient.getString ( "statusServlet.title" ) };
        writer.print ( MessageFormat.format ( Constants.MANAGER_SECTION, args ) );
        args = new Object[] { smClient.getString ( "htmlManagerServlet.appsTitle" ), smClient.getString ( "htmlManagerServlet.appsPath" ), smClient.getString ( "htmlManagerServlet.appsVersion" ), smClient.getString ( "htmlManagerServlet.appsName" ), smClient.getString ( "htmlManagerServlet.appsAvailable" ), smClient.getString ( "htmlManagerServlet.appsSessions" ), smClient.getString ( "htmlManagerServlet.appsTasks" ) };
        writer.print ( MessageFormat.format ( "<table border=\"1\" cellspacing=\"0\" cellpadding=\"3\">\n<tr>\n <td colspan=\"6\" class=\"title\">{0}</td>\n</tr>\n<tr>\n <td class=\"header-left\"><small>{1}</small></td>\n <td class=\"header-left\"><small>{2}</small></td>\n <td class=\"header-center\"><small>{3}</small></td>\n <td class=\"header-center\"><small>{4}</small></td>\n <td class=\"header-left\"><small>{5}</small></td>\n <td class=\"header-left\"><small>{6}</small></td>\n</tr>\n", args ) );
        final Container[] children = this.host.findChildren();
        final String[] contextNames = new String[children.length];
        for ( int i = 0; i < children.length; ++i ) {
            contextNames[i] = children[i].getName();
        }
        Arrays.sort ( contextNames );
        final String appsStart = smClient.getString ( "htmlManagerServlet.appsStart" );
        final String appsStop = smClient.getString ( "htmlManagerServlet.appsStop" );
        final String appsReload = smClient.getString ( "htmlManagerServlet.appsReload" );
        final String appsUndeploy = smClient.getString ( "htmlManagerServlet.appsUndeploy" );
        final String appsExpire = smClient.getString ( "htmlManagerServlet.appsExpire" );
        final String noVersion = "<i>" + smClient.getString ( "htmlManagerServlet.noVersion" ) + "</i>";
        boolean isHighlighted = true;
        boolean isDeployed = true;
        String highlightColor = null;
        for ( final String contextName : contextNames ) {
            final Context ctxt = ( Context ) this.host.findChild ( contextName );
            if ( ctxt != null ) {
                isHighlighted = !isHighlighted;
                if ( isHighlighted ) {
                    highlightColor = "#C3F3C3";
                } else {
                    highlightColor = "#FFFFFF";
                }
                String displayPath;
                final String contextPath = displayPath = ctxt.getPath();
                if ( displayPath.equals ( "" ) ) {
                    displayPath = "/";
                }
                final StringBuilder tmp = new StringBuilder();
                tmp.append ( "path=" );
                tmp.append ( HTMLManagerServlet.URL_ENCODER.encode ( displayPath, "UTF-8" ) );
                if ( ctxt.getWebappVersion().length() > 0 ) {
                    tmp.append ( "&version=" );
                    tmp.append ( HTMLManagerServlet.URL_ENCODER.encode ( ctxt.getWebappVersion(), "UTF-8" ) );
                }
                final String pathVersion = tmp.toString();
                try {
                    isDeployed = this.isDeployed ( contextName );
                } catch ( Exception e ) {
                    isDeployed = false;
                }
                args = new Object[7];
                args[0] = "<a href=\"" + HTMLManagerServlet.URL_ENCODER.encode ( contextPath + "/", "UTF-8" ) + "\">" + RequestUtil.filter ( displayPath ) + "</a>";
                if ( "".equals ( ctxt.getWebappVersion() ) ) {
                    args[1] = noVersion;
                } else {
                    args[1] = RequestUtil.filter ( ctxt.getWebappVersion() );
                }
                if ( ctxt.getDisplayName() == null ) {
                    args[2] = "&nbsp;";
                } else {
                    args[2] = RequestUtil.filter ( ctxt.getDisplayName() );
                }
                args[3] = ctxt.getState().isAvailable();
                args[4] = RequestUtil.filter ( response.encodeURL ( request.getContextPath() + "/html/sessions?" + pathVersion ) );
                final Manager manager = ctxt.getManager();
                if ( manager instanceof DistributedManager && this.showProxySessions ) {
                    args[5] = ( ( DistributedManager ) manager ).getActiveSessionsFull();
                } else if ( manager != null ) {
                    args[5] = manager.getActiveSessions();
                } else {
                    args[5] = 0;
                }
                args[6] = highlightColor;
                writer.print ( MessageFormat.format ( "<tr>\n <td class=\"row-left\" bgcolor=\"{6}\" rowspan=\"2\"><small>{0}</small></td>\n <td class=\"row-left\" bgcolor=\"{6}\" rowspan=\"2\"><small>{1}</small></td>\n <td class=\"row-left\" bgcolor=\"{6}\" rowspan=\"2\"><small>{2}</small></td>\n <td class=\"row-center\" bgcolor=\"{6}\" rowspan=\"2\"><small>{3}</small></td>\n <td class=\"row-center\" bgcolor=\"{6}\" rowspan=\"2\"><small><a href=\"{4}\">{5}</a></small></td>\n", args ) );
                args = new Object[] { RequestUtil.filter ( response.encodeURL ( request.getContextPath() + "/html/start?" + pathVersion ) ), appsStart, RequestUtil.filter ( response.encodeURL ( request.getContextPath() + "/html/stop?" + pathVersion ) ), appsStop, RequestUtil.filter ( response.encodeURL ( request.getContextPath() + "/html/reload?" + pathVersion ) ), appsReload, RequestUtil.filter ( response.encodeURL ( request.getContextPath() + "/html/undeploy?" + pathVersion ) ), appsUndeploy, RequestUtil.filter ( response.encodeURL ( request.getContextPath() + "/html/expire?" + pathVersion ) ), appsExpire, smClient.getString ( "htmlManagerServlet.expire.explain" ), null, null, null };
                if ( manager == null ) {
                    args[11] = smClient.getString ( "htmlManagerServlet.noManager" );
                } else {
                    args[11] = ctxt.getSessionTimeout();
                }
                args[12] = smClient.getString ( "htmlManagerServlet.expire.unit" );
                args[13] = highlightColor;
                if ( ctxt.getName().equals ( this.context.getName() ) ) {
                    writer.print ( MessageFormat.format ( " <td class=\"row-left\" bgcolor=\"{13}\">\n  <small>\n  &nbsp;{1}&nbsp;\n  &nbsp;{3}&nbsp;\n  &nbsp;{5}&nbsp;\n  &nbsp;{7}&nbsp;\n  </small>\n </td>\n</tr><tr>\n <td class=\"row-left\" bgcolor=\"{13}\">\n  <form method=\"POST\" action=\"{8}\">\n  <small>\n  &nbsp;<input type=\"submit\" value=\"{9}\">&nbsp;{10}&nbsp;<input type=\"text\" name=\"idle\" size=\"5\" value=\"{11}\">&nbsp;{12}&nbsp;\n  </small>\n  </form>\n </td>\n</tr>\n", args ) );
                } else if ( ctxt.getState().isAvailable() && isDeployed ) {
                    writer.print ( MessageFormat.format ( " <td class=\"row-left\" bgcolor=\"{13}\">\n  &nbsp;<small>{1}</small>&nbsp;\n  <form class=\"inline\" method=\"POST\" action=\"{2}\">  <small><input type=\"submit\" value=\"{3}\"></small>  </form>\n  <form class=\"inline\" method=\"POST\" action=\"{4}\">  <small><input type=\"submit\" value=\"{5}\"></small>  </form>\n  <form class=\"inline\" method=\"POST\" action=\"{6}\">  <small><input type=\"submit\" value=\"{7}\"></small>  </form>\n </td>\n </tr><tr>\n <td class=\"row-left\" bgcolor=\"{13}\">\n  <form method=\"POST\" action=\"{8}\">\n  <small>\n  &nbsp;<input type=\"submit\" value=\"{9}\">&nbsp;{10}&nbsp;<input type=\"text\" name=\"idle\" size=\"5\" value=\"{11}\">&nbsp;{12}&nbsp;\n  </small>\n  </form>\n </td>\n</tr>\n", args ) );
                } else if ( ctxt.getState().isAvailable() && !isDeployed ) {
                    writer.print ( MessageFormat.format ( " <td class=\"row-left\" bgcolor=\"{13}\">\n  &nbsp;<small>{1}</small>&nbsp;\n  <form class=\"inline\" method=\"POST\" action=\"{2}\">  <small><input type=\"submit\" value=\"{3}\"></small>  </form>\n  <form class=\"inline\" method=\"POST\" action=\"{4}\">  <small><input type=\"submit\" value=\"{5}\"></small>  </form>\n  &nbsp;<small>{7}</small>&nbsp;\n </td>\n </tr><tr>\n <td class=\"row-left\" bgcolor=\"{13}\">\n  <form method=\"POST\" action=\"{8}\">\n  <small>\n  &nbsp;<input type=\"submit\" value=\"{9}\">&nbsp;{10}&nbsp;<input type=\"text\" name=\"idle\" size=\"5\" value=\"{11}\">&nbsp;{12}&nbsp;\n  </small>\n  </form>\n </td>\n</tr>\n", args ) );
                } else if ( !ctxt.getState().isAvailable() && isDeployed ) {
                    writer.print ( MessageFormat.format ( " <td class=\"row-left\" bgcolor=\"{13}\" rowspan=\"2\">\n  <form class=\"inline\" method=\"POST\" action=\"{0}\">  <small><input type=\"submit\" value=\"{1}\"></small>  </form>\n  &nbsp;<small>{3}</small>&nbsp;\n  &nbsp;<small>{5}</small>&nbsp;\n  <form class=\"inline\" method=\"POST\" action=\"{6}\">  <small><input type=\"submit\" value=\"{7}\"></small>  </form>\n </td>\n</tr>\n<tr></tr>\n", args ) );
                } else {
                    writer.print ( MessageFormat.format ( " <td class=\"row-left\" bgcolor=\"{13}\" rowspan=\"2\">\n  <form class=\"inline\" method=\"POST\" action=\"{0}\">  <small><input type=\"submit\" value=\"{1}\"></small>  </form>\n  &nbsp;<small>{3}</small>&nbsp;\n  &nbsp;<small>{5}</small>&nbsp;\n  &nbsp;<small>{7}</small>&nbsp;\n </td>\n</tr>\n<tr></tr>\n", args ) );
                }
            }
        }
        args = new Object[] { smClient.getString ( "htmlManagerServlet.deployTitle" ), smClient.getString ( "htmlManagerServlet.deployServer" ), response.encodeURL ( request.getContextPath() + "/html/deploy" ), smClient.getString ( "htmlManagerServlet.deployPath" ), smClient.getString ( "htmlManagerServlet.deployConfig" ), smClient.getString ( "htmlManagerServlet.deployWar" ), smClient.getString ( "htmlManagerServlet.deployButton" ) };
        writer.print ( MessageFormat.format ( "</table>\n<br>\n<table border=\"1\" cellspacing=\"0\" cellpadding=\"3\">\n<tr>\n <td colspan=\"2\" class=\"title\">{0}</td>\n</tr>\n<tr>\n <td colspan=\"2\" class=\"header-left\"><small>{1}</small></td>\n</tr>\n<tr>\n <td colspan=\"2\">\n<form method=\"post\" action=\"{2}\">\n<table cellspacing=\"0\" cellpadding=\"3\">\n<tr>\n <td class=\"row-right\">\n  <small>{3}</small>\n </td>\n <td class=\"row-left\">\n  <input type=\"text\" name=\"deployPath\" size=\"20\">\n </td>\n</tr>\n<tr>\n <td class=\"row-right\">\n  <small>{4}</small>\n </td>\n <td class=\"row-left\">\n  <input type=\"text\" name=\"deployConfig\" size=\"20\">\n </td>\n</tr>\n<tr>\n <td class=\"row-right\">\n  <small>{5}</small>\n </td>\n <td class=\"row-left\">\n  <input type=\"text\" name=\"deployWar\" size=\"40\">\n </td>\n</tr>\n<tr>\n <td class=\"row-right\">\n  &nbsp;\n </td>\n <td class=\"row-left\">\n  <input type=\"submit\" value=\"{6}\">\n </td>\n</tr>\n</table>\n</form>\n</td>\n</tr>\n", args ) );
        args = new Object[] { smClient.getString ( "htmlManagerServlet.deployUpload" ), response.encodeURL ( request.getContextPath() + "/html/upload" ), smClient.getString ( "htmlManagerServlet.deployUploadFile" ), smClient.getString ( "htmlManagerServlet.deployButton" ) };
        writer.print ( MessageFormat.format ( "<tr>\n <td colspan=\"2\" class=\"header-left\"><small>{0}</small></td>\n</tr>\n<tr>\n <td colspan=\"2\">\n<form method=\"post\" action=\"{1}\" enctype=\"multipart/form-data\">\n<table cellspacing=\"0\" cellpadding=\"3\">\n<tr>\n <td class=\"row-right\">\n  <small>{2}</small>\n </td>\n <td class=\"row-left\">\n  <input type=\"file\" name=\"deployWar\" size=\"40\">\n </td>\n</tr>\n<tr>\n <td class=\"row-right\">\n  &nbsp;\n </td>\n <td class=\"row-left\">\n  <input type=\"submit\" value=\"{3}\">\n </td>\n</tr>\n</table>\n</form>\n</td>\n</tr>\n</table>\n<br>\n\n", args ) );
        args = new Object[] { smClient.getString ( "htmlManagerServlet.diagnosticsTitle" ), smClient.getString ( "htmlManagerServlet.diagnosticsLeak" ), response.encodeURL ( request.getContextPath() + "/html/findleaks" ), smClient.getString ( "htmlManagerServlet.diagnosticsLeakWarning" ), smClient.getString ( "htmlManagerServlet.diagnosticsLeakButton" ), smClient.getString ( "htmlManagerServlet.diagnosticsSsl" ), response.encodeURL ( request.getContextPath() + "/html/sslConnectorCiphers" ), smClient.getString ( "htmlManagerServlet.diagnosticsSslConnectorCipherButton" ), smClient.getString ( "htmlManagerServlet.diagnosticsSslConnectorCipherText" ) };
        writer.print ( MessageFormat.format ( "<table border=\"1\" cellspacing=\"0\" cellpadding=\"3\">\n<tr>\n <td colspan=\"2\" class=\"title\">{0}</td>\n</tr>\n<tr>\n <td colspan=\"2\" class=\"header-left\"><small>{1}</small></td>\n</tr>\n<tr>\n <td colspan=\"2\">\n<form method=\"post\" action=\"{2}\">\n<table cellspacing=\"0\" cellpadding=\"3\">\n<tr>\n <td class=\"row-left\">\n  <input type=\"submit\" value=\"{4}\">\n </td>\n <td class=\"row-left\">\n  <small>{3}</small>\n </td>\n</tr>\n</table>\n</form>\n</td>\n</tr>\n<tr>\n <td colspan=\"2\" class=\"header-left\"><small>{5}</small></td>\n</tr>\n<tr>\n <td colspan=\"2\">\n<form method=\"post\" action=\"{6}\">\n<table cellspacing=\"0\" cellpadding=\"3\">\n<tr>\n <td class=\"row-left\">\n  <input type=\"submit\" value=\"{7}\">\n </td>\n <td class=\"row-left\">\n  <small>{8}</small>\n </td>\n</tr>\n</table>\n</form>\n</td>\n</tr>\n</table>\n<br>", args ) );
        args = new Object[] { smClient.getString ( "htmlManagerServlet.serverTitle" ), smClient.getString ( "htmlManagerServlet.serverVersion" ), smClient.getString ( "htmlManagerServlet.serverJVMVersion" ), smClient.getString ( "htmlManagerServlet.serverJVMVendor" ), smClient.getString ( "htmlManagerServlet.serverOSName" ), smClient.getString ( "htmlManagerServlet.serverOSVersion" ), smClient.getString ( "htmlManagerServlet.serverOSArch" ), smClient.getString ( "htmlManagerServlet.serverHostname" ), smClient.getString ( "htmlManagerServlet.serverIPAddress" ) };
        writer.print ( MessageFormat.format ( Constants.SERVER_HEADER_SECTION, args ) );
        args = new Object[] { ServerInfo.getServerInfo(), System.getProperty ( "java.runtime.version" ), System.getProperty ( "java.vm.vendor" ), System.getProperty ( "os.name" ), System.getProperty ( "os.version" ), System.getProperty ( "os.arch" ), null, null };
        try {
            final InetAddress address = InetAddress.getLocalHost();
            args[6] = address.getHostName();
            args[7] = address.getHostAddress();
        } catch ( UnknownHostException e2 ) {
            args[7] = ( args[6] = "-" );
        }
        writer.print ( MessageFormat.format ( Constants.SERVER_ROW_SECTION, args ) );
        writer.print ( Constants.HTML_TAIL_SECTION );
        writer.flush();
        writer.close();
    }
    protected String reload ( final ContextName cn, final StringManager smClient ) {
        final StringWriter stringWriter = new StringWriter();
        final PrintWriter printWriter = new PrintWriter ( stringWriter );
        super.reload ( printWriter, cn, smClient );
        return stringWriter.toString();
    }
    protected String undeploy ( final ContextName cn, final StringManager smClient ) {
        final StringWriter stringWriter = new StringWriter();
        final PrintWriter printWriter = new PrintWriter ( stringWriter );
        super.undeploy ( printWriter, cn, smClient );
        return stringWriter.toString();
    }
    protected String sessions ( final ContextName cn, final int idle, final StringManager smClient ) {
        final StringWriter stringWriter = new StringWriter();
        final PrintWriter printWriter = new PrintWriter ( stringWriter );
        super.sessions ( printWriter, cn, idle, smClient );
        return stringWriter.toString();
    }
    protected String start ( final ContextName cn, final StringManager smClient ) {
        final StringWriter stringWriter = new StringWriter();
        final PrintWriter printWriter = new PrintWriter ( stringWriter );
        super.start ( printWriter, cn, smClient );
        return stringWriter.toString();
    }
    protected String stop ( final ContextName cn, final StringManager smClient ) {
        final StringWriter stringWriter = new StringWriter();
        final PrintWriter printWriter = new PrintWriter ( stringWriter );
        super.stop ( printWriter, cn, smClient );
        return stringWriter.toString();
    }
    protected String findleaks ( final StringManager smClient ) {
        final StringBuilder msg = new StringBuilder();
        final StringWriter stringWriter = new StringWriter();
        final PrintWriter printWriter = new PrintWriter ( stringWriter );
        super.findleaks ( false, printWriter, smClient );
        final String writerText = stringWriter.toString();
        if ( writerText.length() > 0 ) {
            if ( !writerText.startsWith ( "FAIL -" ) ) {
                msg.append ( smClient.getString ( "htmlManagerServlet.findleaksList" ) );
            }
            msg.append ( writerText );
        } else {
            msg.append ( smClient.getString ( "htmlManagerServlet.findleaksNone" ) );
        }
        return msg.toString();
    }
    protected void sslConnectorCiphers ( final HttpServletRequest request, final HttpServletResponse response ) throws ServletException, IOException {
        request.setAttribute ( "cipherList", ( Object ) this.getConnectorCiphers() );
        this.getServletContext().getRequestDispatcher ( "/WEB-INF/jsp/connectorCiphers.jsp" ).forward ( ( ServletRequest ) request, ( ServletResponse ) response );
    }
    public String getServletInfo() {
        return "HTMLManagerServlet, Copyright (c) 1999-2016, The Apache Software Foundation";
    }
    @Override
    public void init() throws ServletException {
        super.init();
        String value = null;
        value = this.getServletConfig().getInitParameter ( "showProxySessions" );
        this.showProxySessions = Boolean.parseBoolean ( value );
    }
    protected String expireSessions ( final ContextName cn, final HttpServletRequest req, final StringManager smClient ) {
        int idle = -1;
        final String idleParam = req.getParameter ( "idle" );
        if ( idleParam != null ) {
            try {
                idle = Integer.parseInt ( idleParam );
            } catch ( NumberFormatException e ) {
                this.log ( "Could not parse idle parameter to an int: " + idleParam );
            }
        }
        return this.sessions ( cn, idle, smClient );
    }
    protected void doSessions ( final ContextName cn, final HttpServletRequest req, final HttpServletResponse resp, final StringManager smClient ) throws ServletException, IOException {
        req.setAttribute ( "path", ( Object ) cn.getPath() );
        req.setAttribute ( "version", ( Object ) cn.getVersion() );
        final String action = req.getParameter ( "action" );
        if ( this.debug >= 1 ) {
            this.log ( "sessions: Session action '" + action + "' for web application '" + cn.getDisplayName() + "'" );
        }
        if ( "sessionDetail".equals ( action ) ) {
            final String sessionId = req.getParameter ( "sessionId" );
            this.displaySessionDetailPage ( req, resp, cn, sessionId, smClient );
            return;
        }
        if ( "invalidateSessions".equals ( action ) ) {
            final String[] sessionIds = req.getParameterValues ( "sessionIds" );
            final int i = this.invalidateSessions ( cn, sessionIds, smClient );
            req.setAttribute ( "message", ( Object ) ( "" + i + " sessions invalidated." ) );
        } else if ( "removeSessionAttribute".equals ( action ) ) {
            final String sessionId = req.getParameter ( "sessionId" );
            final String name = req.getParameter ( "attributeName" );
            final boolean removed = this.removeSessionAttribute ( cn, sessionId, name, smClient );
            final String outMessage = removed ? ( "Session attribute '" + name + "' removed." ) : ( "Session did not contain any attribute named '" + name + "'" );
            req.setAttribute ( "message", ( Object ) outMessage );
            this.displaySessionDetailPage ( req, resp, cn, sessionId, smClient );
            return;
        }
        this.displaySessionsListPage ( cn, req, resp, smClient );
    }
    protected List<Session> getSessionsForName ( final ContextName cn, final StringManager smClient ) {
        if ( cn == null || ( !cn.getPath().startsWith ( "/" ) && !cn.getPath().equals ( "" ) ) ) {
            String path = null;
            if ( cn != null ) {
                path = cn.getPath();
            }
            throw new IllegalArgumentException ( smClient.getString ( "managerServlet.invalidPath", RequestUtil.filter ( path ) ) );
        }
        final Context ctxt = ( Context ) this.host.findChild ( cn.getName() );
        if ( null == ctxt ) {
            throw new IllegalArgumentException ( smClient.getString ( "managerServlet.noContext", RequestUtil.filter ( cn.getDisplayName() ) ) );
        }
        final Manager manager = ctxt.getManager();
        final List<Session> sessions = new ArrayList<Session>();
        sessions.addAll ( Arrays.asList ( manager.findSessions() ) );
        if ( manager instanceof DistributedManager && this.showProxySessions ) {
            final Set<String> sessionIds = ( ( DistributedManager ) manager ).getSessionIdsFull();
            for ( final Session session : sessions ) {
                sessionIds.remove ( session.getId() );
            }
            for ( final String sessionId : sessionIds ) {
                sessions.add ( new DummyProxySession ( sessionId ) );
            }
        }
        return sessions;
    }
    protected Session getSessionForNameAndId ( final ContextName cn, final String id, final StringManager smClient ) {
        final List<Session> sessions = this.getSessionsForName ( cn, smClient );
        if ( sessions.isEmpty() ) {
            return null;
        }
        for ( final Session session : sessions ) {
            if ( session.getId().equals ( id ) ) {
                return session;
            }
        }
        return null;
    }
    protected void displaySessionsListPage ( final ContextName cn, final HttpServletRequest req, final HttpServletResponse resp, final StringManager smClient ) throws ServletException, IOException {
        final List<Session> sessions = this.getSessionsForName ( cn, smClient );
        final String sortBy = req.getParameter ( "sort" );
        String orderBy = null;
        if ( null != sortBy && !"".equals ( sortBy.trim() ) ) {
            Comparator<Session> comparator = this.getComparator ( sortBy );
            if ( comparator != null ) {
                orderBy = req.getParameter ( "order" );
                if ( "DESC".equalsIgnoreCase ( orderBy ) ) {
                    comparator = Collections.reverseOrder ( comparator );
                    orderBy = "ASC";
                } else {
                    orderBy = "DESC";
                }
                try {
                    Collections.sort ( sessions, comparator );
                } catch ( IllegalStateException ise ) {
                    req.setAttribute ( "error", ( Object ) "Can't sort session list: one session is invalidated" );
                }
            } else {
                this.log ( "WARNING: unknown sort order: " + sortBy );
            }
        }
        req.setAttribute ( "sort", ( Object ) sortBy );
        req.setAttribute ( "order", ( Object ) orderBy );
        req.setAttribute ( "activeSessions", ( Object ) sessions );
        resp.setHeader ( "Pragma", "No-cache" );
        resp.setHeader ( "Cache-Control", "no-cache,no-store,max-age=0" );
        resp.setDateHeader ( "Expires", 0L );
        this.getServletContext().getRequestDispatcher ( "/WEB-INF/jsp/sessionsList.jsp" ).include ( ( ServletRequest ) req, ( ServletResponse ) resp );
    }
    protected void displaySessionDetailPage ( final HttpServletRequest req, final HttpServletResponse resp, final ContextName cn, final String sessionId, final StringManager smClient ) throws ServletException, IOException {
        final Session session = this.getSessionForNameAndId ( cn, sessionId, smClient );
        resp.setHeader ( "Pragma", "No-cache" );
        resp.setHeader ( "Cache-Control", "no-cache,no-store,max-age=0" );
        resp.setDateHeader ( "Expires", 0L );
        req.setAttribute ( "currentSession", ( Object ) session );
        this.getServletContext().getRequestDispatcher ( resp.encodeURL ( "/WEB-INF/jsp/sessionDetail.jsp" ) ).include ( ( ServletRequest ) req, ( ServletResponse ) resp );
    }
    protected int invalidateSessions ( final ContextName cn, final String[] sessionIds, final StringManager smClient ) {
        if ( null == sessionIds ) {
            return 0;
        }
        int nbAffectedSessions = 0;
        for ( int i = 0; i < sessionIds.length; ++i ) {
            final String sessionId = sessionIds[i];
            final HttpSession session = this.getSessionForNameAndId ( cn, sessionId, smClient ).getSession();
            if ( null == session ) {
                if ( this.debug >= 1 ) {
                    this.log ( "WARNING: can't invalidate null session " + sessionId );
                }
            } else {
                try {
                    session.invalidate();
                    ++nbAffectedSessions;
                    if ( this.debug >= 1 ) {
                        this.log ( "Invalidating session id " + sessionId );
                    }
                } catch ( IllegalStateException ise ) {
                    if ( this.debug >= 1 ) {
                        this.log ( "Can't invalidate already invalidated session id " + sessionId );
                    }
                }
            }
        }
        return nbAffectedSessions;
    }
    protected boolean removeSessionAttribute ( final ContextName cn, final String sessionId, final String attributeName, final StringManager smClient ) {
        final HttpSession session = this.getSessionForNameAndId ( cn, sessionId, smClient ).getSession();
        if ( null == session ) {
            if ( this.debug >= 1 ) {
                this.log ( "WARNING: can't remove attribute '" + attributeName + "' for null session " + sessionId );
            }
            return false;
        }
        final boolean wasPresent = null != session.getAttribute ( attributeName );
        try {
            session.removeAttribute ( attributeName );
        } catch ( IllegalStateException ise ) {
            if ( this.debug >= 1 ) {
                this.log ( "Can't remote attribute '" + attributeName + "' for invalidated session id " + sessionId );
            }
        }
        return wasPresent;
    }
    protected Comparator<Session> getComparator ( final String sortBy ) {
        Comparator<Session> comparator = null;
        if ( "CreationTime".equalsIgnoreCase ( sortBy ) ) {
            comparator = new BaseSessionComparator<Date>() {
                @Override
                public Comparable<Date> getComparableObject ( final Session session ) {
                    return new Date ( session.getCreationTime() );
                }
            };
        } else if ( "id".equalsIgnoreCase ( sortBy ) ) {
            comparator = new BaseSessionComparator<String>() {
                @Override
                public Comparable<String> getComparableObject ( final Session session ) {
                    return session.getId();
                }
            };
        } else if ( "LastAccessedTime".equalsIgnoreCase ( sortBy ) ) {
            comparator = new BaseSessionComparator<Date>() {
                @Override
                public Comparable<Date> getComparableObject ( final Session session ) {
                    return new Date ( session.getLastAccessedTime() );
                }
            };
        } else if ( "MaxInactiveInterval".equalsIgnoreCase ( sortBy ) ) {
            comparator = new BaseSessionComparator<Integer>() {
                @Override
                public Comparable<Integer> getComparableObject ( final Session session ) {
                    return session.getMaxInactiveInterval();
                }
            };
        } else if ( "new".equalsIgnoreCase ( sortBy ) ) {
            comparator = new BaseSessionComparator<Boolean>() {
                @Override
                public Comparable<Boolean> getComparableObject ( final Session session ) {
                    return session.getSession().isNew();
                }
            };
        } else if ( "locale".equalsIgnoreCase ( sortBy ) ) {
            comparator = new BaseSessionComparator<String>() {
                @Override
                public Comparable<String> getComparableObject ( final Session session ) {
                    return JspHelper.guessDisplayLocaleFromSession ( session );
                }
            };
        } else if ( "user".equalsIgnoreCase ( sortBy ) ) {
            comparator = new BaseSessionComparator<String>() {
                @Override
                public Comparable<String> getComparableObject ( final Session session ) {
                    return JspHelper.guessDisplayUserFromSession ( session );
                }
            };
        } else if ( "UsedTime".equalsIgnoreCase ( sortBy ) ) {
            comparator = new BaseSessionComparator<Date>() {
                @Override
                public Comparable<Date> getComparableObject ( final Session session ) {
                    return new Date ( SessionUtils.getUsedTimeForSession ( session ) );
                }
            };
        } else if ( "InactiveTime".equalsIgnoreCase ( sortBy ) ) {
            comparator = new BaseSessionComparator<Date>() {
                @Override
                public Comparable<Date> getComparableObject ( final Session session ) {
                    return new Date ( SessionUtils.getInactiveTimeForSession ( session ) );
                }
            };
        } else if ( "TTL".equalsIgnoreCase ( sortBy ) ) {
            comparator = new BaseSessionComparator<Date>() {
                @Override
                public Comparable<Date> getComparableObject ( final Session session ) {
                    return new Date ( SessionUtils.getTTLForSession ( session ) );
                }
            };
        }
        return comparator;
    }
    static {
        ( URL_ENCODER = new URLEncoder() ).addSafeCharacter ( '/' );
    }
}
