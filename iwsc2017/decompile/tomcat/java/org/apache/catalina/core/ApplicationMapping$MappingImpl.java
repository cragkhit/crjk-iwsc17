package org.apache.catalina.core;
import javax.servlet.http.MappingMatch;
import javax.servlet.http.Mapping;
private static class MappingImpl implements Mapping {
    private final String matchValue;
    private final String pattern;
    private final MappingMatch mappingType;
    private final String servletName;
    public MappingImpl ( final String matchValue, final String pattern, final MappingMatch mappingType, final String servletName ) {
        this.matchValue = matchValue;
        this.pattern = pattern;
        this.mappingType = mappingType;
        this.servletName = servletName;
    }
    public String getMatchValue() {
        return this.matchValue;
    }
    public String getPattern() {
        return this.pattern;
    }
    public MappingMatch getMappingMatch() {
        return this.mappingType;
    }
    public String getServletName() {
        return this.servletName;
    }
}
