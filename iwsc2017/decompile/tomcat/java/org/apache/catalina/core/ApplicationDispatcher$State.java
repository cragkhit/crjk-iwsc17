package org.apache.catalina.core;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.ServletRequest;
private static class State {
    ServletRequest outerRequest;
    ServletResponse outerResponse;
    ServletRequest wrapRequest;
    ServletResponse wrapResponse;
    boolean including;
    HttpServletRequest hrequest;
    HttpServletResponse hresponse;
    State ( final ServletRequest request, final ServletResponse response, final boolean including ) {
        this.outerRequest = null;
        this.outerResponse = null;
        this.wrapRequest = null;
        this.wrapResponse = null;
        this.including = false;
        this.hrequest = null;
        this.hresponse = null;
        this.outerRequest = request;
        this.outerResponse = response;
        this.including = including;
    }
}
