package org.apache.catalina.webresources;
import java.util.Comparator;
private static class EvictionOrder implements Comparator<CachedResource> {
    @Override
    public int compare ( final CachedResource cr1, final CachedResource cr2 ) {
        final long nc1 = cr1.getNextCheck();
        final long nc2 = cr2.getNextCheck();
        if ( nc1 == nc2 ) {
            return 0;
        }
        if ( nc1 > nc2 ) {
            return -1;
        }
        return 1;
    }
}
