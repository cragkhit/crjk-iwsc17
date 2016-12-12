package org.apache.catalina.startup;
public static class Repository {
    private final String location;
    private final RepositoryType type;
    public Repository ( final String location, final RepositoryType type ) {
        this.location = location;
        this.type = type;
    }
    public String getLocation() {
        return this.location;
    }
    public RepositoryType getType() {
        return this.type;
    }
}
