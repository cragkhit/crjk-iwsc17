package org.apache.catalina.authenticator.jaspic;
import java.util.ArrayList;
import java.util.List;
public static class Providers {
    private final List<Provider> providers;
    public Providers() {
        this.providers = new ArrayList<Provider>();
    }
    public void addProvider ( final Provider provider ) {
        this.providers.add ( provider );
    }
    public List<Provider> getProviders() {
        return this.providers;
    }
}
