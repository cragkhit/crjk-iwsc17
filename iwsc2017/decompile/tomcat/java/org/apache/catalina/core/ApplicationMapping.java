package org.apache.catalina.core;
import javax.servlet.http.MappingMatch;
import javax.servlet.http.Mapping;
import org.apache.catalina.mapper.MappingData;
public class ApplicationMapping {
    private final MappingData mappingData;
    private volatile Mapping mapping;
    public ApplicationMapping ( final MappingData mappingData ) {
        this.mapping = null;
        this.mappingData = mappingData;
    }
    public Mapping getMapping() {
        if ( this.mapping == null ) {
            String servletName;
            if ( this.mappingData.wrapper == null ) {
                servletName = "";
            } else {
                servletName = this.mappingData.wrapper.getName();
            }
            switch ( this.mappingData.matchType ) {
            case CONTEXT_ROOT: {
                this.mapping = ( Mapping ) new MappingImpl ( "", "", this.mappingData.matchType, servletName );
                break;
            }
            case DEFAULT: {
                this.mapping = ( Mapping ) new MappingImpl ( "/", "/", this.mappingData.matchType, servletName );
                break;
            }
            case EXACT: {
                this.mapping = ( Mapping ) new MappingImpl ( this.mappingData.wrapperPath.toString(), this.mappingData.wrapperPath.toString(), this.mappingData.matchType, servletName );
                break;
            }
            case EXTENSION: {
                final String path = this.mappingData.wrapperPath.toString();
                final int extIndex = path.lastIndexOf ( 46 );
                this.mapping = ( Mapping ) new MappingImpl ( path.substring ( 0, extIndex ), "*" + path.substring ( extIndex ), this.mappingData.matchType, servletName );
                break;
            }
            case PATH: {
                this.mapping = ( Mapping ) new MappingImpl ( this.mappingData.pathInfo.toString(), this.mappingData.wrapperPath.toString() + "/*", this.mappingData.matchType, servletName );
                break;
            }
            case UNKNOWN: {
                this.mapping = ( Mapping ) new MappingImpl ( "", "", this.mappingData.matchType, servletName );
                break;
            }
            }
        }
        return this.mapping;
    }
    public void recycle() {
        this.mapping = null;
    }
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
}
