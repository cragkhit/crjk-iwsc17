// 
// Decompiled by Procyon v0.5.29
// 

package compressionFilters;

import javax.servlet.ServletException;
import java.io.IOException;
import java.util.Enumeration;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.FilterChain;
import javax.servlet.ServletResponse;
import javax.servlet.ServletRequest;
import java.util.List;
import java.util.Arrays;
import java.util.StringTokenizer;
import java.util.ArrayList;
import javax.servlet.GenericFilter;

public class CompressionFilter extends GenericFilter
{
    private static final long serialVersionUID = 1L;
    private final int minThreshold = 128;
    protected int compressionThreshold;
    private final int minBuffer = 8192;
    protected int compressionBuffer;
    protected String[] compressionMimeTypes;
    private int debug;
    
    public CompressionFilter() {
        this.compressionThreshold = 0;
        this.compressionBuffer = 0;
        this.compressionMimeTypes = new String[] { "text/html", "text/xml", "text/plain" };
        this.debug = 0;
    }
    
    public void init() {
        String str = this.getInitParameter("debug");
        if (str != null) {
            this.debug = Integer.parseInt(str);
        }
        str = this.getInitParameter("compressionThreshold");
        if (str != null) {
            this.compressionThreshold = Integer.parseInt(str);
            if (this.compressionThreshold != 0 && this.compressionThreshold < 128) {
                if (this.debug > 0) {
                    System.out.println("compressionThreshold should be either 0 - no compression or >= 128");
                    System.out.println("compressionThreshold set to 128");
                }
                this.compressionThreshold = 128;
            }
        }
        str = this.getInitParameter("compressionBuffer");
        if (str != null) {
            this.compressionBuffer = Integer.parseInt(str);
            if (this.compressionBuffer < 8192) {
                if (this.debug > 0) {
                    System.out.println("compressionBuffer should be >= 8192");
                    System.out.println("compressionBuffer set to 8192");
                }
                this.compressionBuffer = 8192;
            }
        }
        str = this.getInitParameter("compressionMimeTypes");
        if (str != null) {
            final List<String> values = new ArrayList<String>();
            final StringTokenizer st = new StringTokenizer(str, ",");
            while (st.hasMoreTokens()) {
                final String token = st.nextToken().trim();
                if (token.length() > 0) {
                    values.add(token);
                }
            }
            if (values.size() > 0) {
                this.compressionMimeTypes = values.toArray(new String[values.size()]);
            }
            else {
                this.compressionMimeTypes = null;
            }
            if (this.debug > 0) {
                System.out.println("compressionMimeTypes set to " + Arrays.toString(this.compressionMimeTypes));
            }
        }
    }
    
    public void doFilter(final ServletRequest request, final ServletResponse response, final FilterChain chain) throws IOException, ServletException {
        if (this.debug > 0) {
            System.out.println("@doFilter");
        }
        if (this.compressionThreshold == 0) {
            if (this.debug > 0) {
                System.out.println("doFilter got called, but compressionTreshold is set to 0 - no compression");
            }
            chain.doFilter(request, response);
            return;
        }
        boolean supportCompression = false;
        if (request instanceof HttpServletRequest) {
            if (this.debug > 1) {
                System.out.println("requestURI = " + ((HttpServletRequest)request).getRequestURI());
            }
            final String s = ((HttpServletRequest)request).getParameter("gzip");
            if ("false".equals(s)) {
                if (this.debug > 0) {
                    System.out.println("got parameter gzip=false --> don't compress, just chain filter");
                }
                chain.doFilter(request, response);
                return;
            }
            final Enumeration<String> e = (Enumeration<String>)((HttpServletRequest)request).getHeaders("Accept-Encoding");
            while (e.hasMoreElements()) {
                final String name = e.nextElement();
                if (name.indexOf("gzip") != -1) {
                    if (this.debug > 0) {
                        System.out.println("supports compression");
                    }
                    supportCompression = true;
                }
                else {
                    if (this.debug <= 0) {
                        continue;
                    }
                    System.out.println("no support for compression");
                }
            }
        }
        if (!supportCompression) {
            if (this.debug > 0) {
                System.out.println("doFilter gets called w/o compression");
            }
            chain.doFilter(request, response);
            return;
        }
        if (response instanceof HttpServletResponse) {
            final CompressionServletResponseWrapper wrappedResponse = new CompressionServletResponseWrapper((HttpServletResponse)response);
            wrappedResponse.setDebugLevel(this.debug);
            wrappedResponse.setCompressionThreshold(this.compressionThreshold);
            wrappedResponse.setCompressionBuffer(this.compressionBuffer);
            wrappedResponse.setCompressionMimeTypes(this.compressionMimeTypes);
            if (this.debug > 0) {
                System.out.println("doFilter gets called with compression");
            }
            try {
                chain.doFilter(request, (ServletResponse)wrappedResponse);
            }
            finally {
                wrappedResponse.finishResponse();
            }
        }
    }
}
