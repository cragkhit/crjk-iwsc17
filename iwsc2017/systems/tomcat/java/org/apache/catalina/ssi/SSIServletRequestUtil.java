package org.apache.catalina.ssi;
import javax.servlet.RequestDispatcher;
import javax.servlet.http.HttpServletRequest;
import org.apache.tomcat.util.http.RequestUtil;
public class SSIServletRequestUtil {
    public static String getRelativePath ( HttpServletRequest request ) {
        if ( request.getAttribute (
                    RequestDispatcher.INCLUDE_REQUEST_URI ) != null ) {
            String result = ( String ) request.getAttribute (
                                RequestDispatcher.INCLUDE_PATH_INFO );
            if ( result == null )
                result = ( String ) request.getAttribute (
                             RequestDispatcher.INCLUDE_SERVLET_PATH );
            if ( ( result == null ) || ( result.equals ( "" ) ) ) {
                result = "/";
            }
            return ( result );
        }
        String result = request.getPathInfo();
        if ( result == null ) {
            result = request.getServletPath();
        }
        if ( ( result == null ) || ( result.equals ( "" ) ) ) {
            result = "/";
        }
        return RequestUtil.normalize ( result );
    }
}
