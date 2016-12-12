package org.apache.catalina.authenticator.jaspic;
import java.util.HashMap;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletRequest;
import java.util.Map;
import org.apache.tomcat.util.res.StringManager;
import javax.security.auth.message.MessageInfo;
public class MessageInfoImpl implements MessageInfo {
    protected static final StringManager sm;
    public static final String IS_MANDATORY = "javax.security.auth.message.MessagePolicy.isMandatory";
    private final Map<String, Object> map;
    private HttpServletRequest request;
    private HttpServletResponse response;
    public MessageInfoImpl() {
        this.map = new HashMap<String, Object>();
    }
    public MessageInfoImpl ( final HttpServletRequest request, final HttpServletResponse response, final boolean authMandatory ) {
        this.map = new HashMap<String, Object>();
        this.request = request;
        this.response = response;
        this.map.put ( "javax.security.auth.message.MessagePolicy.isMandatory", Boolean.toString ( authMandatory ) );
    }
    public Map getMap() {
        return this.map;
    }
    public Object getRequestMessage() {
        return this.request;
    }
    public Object getResponseMessage() {
        return this.response;
    }
    public void setRequestMessage ( final Object request ) {
        if ( ! ( request instanceof HttpServletRequest ) ) {
            throw new IllegalArgumentException ( MessageInfoImpl.sm.getString ( "authenticator.jaspic.badRequestType", request.getClass().getName() ) );
        }
        this.request = ( HttpServletRequest ) request;
    }
    public void setResponseMessage ( final Object response ) {
        if ( ! ( response instanceof HttpServletResponse ) ) {
            throw new IllegalArgumentException ( MessageInfoImpl.sm.getString ( "authenticator.jaspic.badResponseType", response.getClass().getName() ) );
        }
        this.response = ( HttpServletResponse ) response;
    }
    static {
        sm = StringManager.getManager ( MessageInfoImpl.class );
    }
}
