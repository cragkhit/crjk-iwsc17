package org.apache.catalina.filters;
import java.util.List;
protected static class ExpiresConfiguration {
    private final List<Duration> durations;
    private final StartingPoint startingPoint;
    public ExpiresConfiguration ( final StartingPoint startingPoint, final List<Duration> durations ) {
        this.startingPoint = startingPoint;
        this.durations = durations;
    }
    public List<Duration> getDurations() {
        return this.durations;
    }
    public StartingPoint getStartingPoint() {
        return this.startingPoint;
    }
    @Override
    public String toString() {
        return "ExpiresConfiguration[startingPoint=" + this.startingPoint + ", duration=" + this.durations + "]";
    }
}
