package org.apache.tomcat.websocket.server;
import java.util.Comparator;
private static class TemplatePathMatchComparator implements Comparator<TemplatePathMatch> {
    private static final TemplatePathMatchComparator INSTANCE;
    public static TemplatePathMatchComparator getInstance() {
        return TemplatePathMatchComparator.INSTANCE;
    }
    @Override
    public int compare ( final TemplatePathMatch tpm1, final TemplatePathMatch tpm2 ) {
        return tpm1.getUriTemplate().getNormalizedPath().compareTo ( tpm2.getUriTemplate().getNormalizedPath() );
    }
    static {
        INSTANCE = new TemplatePathMatchComparator();
    }
}
