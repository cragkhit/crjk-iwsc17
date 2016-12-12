package org.apache.catalina.core;
private static final class AnnotationCacheEntry {
    private final String accessibleObjectName;
    private final Class<?>[] paramTypes;
    private final String name;
    private final AnnotationCacheEntryType type;
    public AnnotationCacheEntry ( final String accessibleObjectName, final Class<?>[] paramTypes, final String name, final AnnotationCacheEntryType type ) {
        this.accessibleObjectName = accessibleObjectName;
        this.paramTypes = paramTypes;
        this.name = name;
        this.type = type;
    }
    public String getAccessibleObjectName() {
        return this.accessibleObjectName;
    }
    public Class<?>[] getParamTypes() {
        return this.paramTypes;
    }
    public String getName() {
        return this.name;
    }
    public AnnotationCacheEntryType getType() {
        return this.type;
    }
}
